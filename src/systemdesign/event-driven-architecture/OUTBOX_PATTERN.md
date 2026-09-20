# Outbox Pattern (Transactional Outbox + Outbox Relay)

## The problem it solves: the "dual-write" problem

A service often needs to do two things when something happens (e.g., stock becomes available):

1. Update its own database (e.g., `variants.quantity`).
2. Publish an event to a message broker (Kafka/SQS/etc.) so other services can react (e.g., send a
   back-in-stock notification).

These are **two different systems** (your database, and the broker). There is no built-in way to make
"write to the DB" and "publish to the broker" happen as a single atomic operation. If the process
crashes between step 1 and step 2:

- DB says "in stock", but the event was never published → other services never find out. A user misses
  their back-in-stock alert.
- Or the event gets published, but the DB transaction then fails/rolls back → you told the world
  something happened that isn't actually true in your own data.

This inconsistency risk is called the **dual-write problem**.

## The fix: only write to ONE system, transactionally

Instead of writing to the DB and the broker separately, write **both pieces of information to the same
database, in the same transaction**:

1. Update `quantity` in the `variants` table.
2. In the **same transaction**, insert a row into a new table — usually called the **outbox table** —
   describing the event that needs to be sent later.

```sql
BEGIN;

UPDATE variants SET quantity = 10 WHERE variant_id = 'V-TSHIRT-M';

INSERT INTO outbox (id, aggregate_id, event_type, payload, created_at, sent)
VALUES (gen_uuid(), 'V-TSHIRT-M', 'VARIANT_STOCK_CHANGED',
        '{"variantId":"V-TSHIRT-M","oldQuantity":0,"newQuantity":10}', now(), false);

COMMIT;
```

Both writes are plain SQL statements against the **same database**, so the database's normal
transaction guarantees (atomicity — part of **ACID**) apply: either both rows are saved, or neither is.
There is no longer a window where "DB updated" and "event will be sent" can disagree, because
publishing to the broker hasn't happened yet — it's just a row waiting to be sent.

## Who actually sends it to Kafka/SQS? → The Outbox Relay

The **Outbox Relay** (also called a **message relay**) is a separate, independent background
process/service whose only job is:

1. Continuously read new, unsent rows from the `outbox` table (`WHERE sent = false`, ordered by
   `created_at`).
2. Publish each row's event to the real message broker (Kafka topic, SQS queue, etc.).
3. Mark the row as sent (`sent = true`, or delete it) once the publish is confirmed.

```
[Your service]                     [Outbox Relay]                  [Kafka / SQS / Event Hub]
     |                                    |                                    |
     | 1. DB txn: update stock            |                                    |
     |    + insert outbox row             |                                    |
     |----------------------------------->|                                    |
     |                                    | 2. poll outbox table               |
     |                                    | 3. publish event ---------------->|
     |                                    | 4. mark row as sent                |
```

If the relay crashes mid-way through step 3/4, nothing is lost — on restart it simply re-reads
still-unsent rows and republishes them. This can occasionally cause the **same event to be published
more than once**, which is why consumers of these events need to be **idempotent** (see
[`IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md`](./IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md)) — "processing the
same event twice causes no incorrect side effect."

## How is the outbox table actually read? Two common approaches

### 1. Polling publisher (simple, most common for smaller scale)

A scheduled job (e.g., every 500ms) runs `SELECT * FROM outbox WHERE sent = false ORDER BY created_at
LIMIT 100`, publishes each, marks them sent. Simple to build, but adds polling latency and DB read load
as volume grows.

### 2. Change Data Capture (CDC) — e.g., Debezium (common at larger scale)

Instead of repeatedly querying the `outbox` table, a CDC tool watches the database's own transaction
log. When it sees a committed insert into `outbox`, it converts that change into an event and publishes
it to Kafka. **Debezium** is a common tool that does this.

### What is a write-ahead log (WAL)?

A database usually stores table data in large data files made of pages. Rewriting all affected pages
on every `COMMIT` would be slow. Instead, it first appends a smaller description of the change to a
sequential, durable log on disk:

```
Application: UPDATE variants ... + INSERT INTO outbox ...
                         |
                         v
Database appends change records to its transaction log
                         |
                    COMMIT record
                         |
                         v
Database confirms COMMIT to the application
                         |
                         v
Changed table pages may be flushed to their data files later
```

It is called **write-ahead** because the log record is made durable *before* the changed table page
must be written to its final data file. If the database process or machine crashes after `COMMIT` but
before those pages are flushed, the database reads the log during startup and restores the committed
changes. It can also identify transactions that never committed and keep them from becoming visible.

The log is not a temporary file created by the application between an SQL statement and the database.
It is a set of durable files managed internally by the database itself. Old log segments are normally
archived, recycled, or deleted after the database no longer needs them for crash recovery,
replication, backups, or CDC consumers. A stalled CDC consumer can therefore increase log storage
usage until it catches up, depending on the database's retention configuration.

Different databases use related but not identical logs:

| Database | Log commonly used for CDC |
|---|---|
| PostgreSQL | WAL, exposed as logical changes through logical decoding |
| MySQL | Binary log (`binlog`); this is distinct from InnoDB's internal redo log |
| SQL Server | Transaction log |
| MongoDB | Oplog |

### How Debezium reads an outbox change

Consider the transaction from the earlier example:

1. The application starts one database transaction.
2. It updates `variants` and inserts an `outbox` row.
3. The database records both changes in its transaction log and records the commit.
4. Debezium's database connector reads the log from its last saved position.
5. Debezium ignores uncommitted/rolled-back changes and receives the committed `outbox` insert.
6. The Debezium outbox event router extracts fields such as event ID, type, aggregate ID, and payload.
7. Kafka Connect publishes the resulting event to the configured Kafka topic.
8. The connector saves how far it has read (for example, a PostgreSQL log position or MySQL binlog
   file and offset), so after a restart it continues from that position.

```text
Application -> Database transaction -> WAL/binlog -> Debezium connector -> Kafka Connect -> Kafka
                     |                      |
                     |                      +-- reads committed changes in order
                     +-- variants row and outbox row commit together
```

This gives low-latency publication without repeatedly running `SELECT ... WHERE sent = false` against
the business tables. It also captures the order in which changes were committed.

### Does CDC make delivery exactly-once?

No. Debezium and Kafka Connect persist their read position, but a crash can still occur around
"publish event" and "save new position." On recovery, a change may be published again. The outbox row
must therefore have a stable event ID, and consumers must still be idempotent.

With CDC, the application usually does not use the polling relay's `sent` flag. It keeps outbox rows
for an agreed retention period and deletes or archives them with a separate cleanup job only after the
CDC pipeline has had enough time to capture them. The transaction log and the outbox table are
different durable records: the log drives CDC; the table is the application's transactional event
record.

## Why not just publish directly and retry on failure?

You could try: "update DB, then publish to Kafka; if publish fails, retry the whole thing." But this
doesn't remove the underlying issue — a crash can still happen after DB commit but before the retry
loop confirms the publish, and you're back to an inconsistent state with no record that a publish is
still owed. The outbox row **is** that record — it survives crashes because it's stored durably in the
same transaction as the actual state change.

## Summary

| Term | Meaning |
|---|---|
| Dual-write problem | Writing to two independent systems (DB + broker) can't be made atomic, causing inconsistency across a crash |
| Transactional Outbox | Write the "event to be published" as a row in your own DB, in the same transaction as the real state change |
| Outbox Relay / Message Relay | Background process that reads unsent outbox rows and publishes them to the real broker |
| CDC (Change Data Capture) | Reading a DB's internal replication log to stream row changes (e.g., new outbox rows) into a broker without polling |
| Debezium | The most widely used open-source CDC tool, commonly paired with Kafka (this combo is often called "Debezium + Kafka Connect") |

See also: [`INTERVIEW_QA.md`](./INTERVIEW_QA.md) for common interview questions on this pattern.
