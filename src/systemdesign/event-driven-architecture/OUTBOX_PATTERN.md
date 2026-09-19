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

Instead of an app polling the table, a CDC tool (most commonly **Debezium**) reads the database's
**write-ahead log / binlog / WAL** directly (the same internal log the DB uses for replication) and
streams every insert into the `outbox` table straight into Kafka, in near real time, without adding
query load on the database. This is the more scalable, lower-latency version of the same idea.

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
