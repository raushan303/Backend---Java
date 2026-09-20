# Idempotency and Delivery Guarantees

## Delivery guarantee terms (asked constantly in interviews)

| Guarantee | Meaning | Risk |
|---|---|---|
| **At-most-once** | A message is delivered 0 or 1 times — never redelivered, even after a failure | Messages can be **lost** |
| **At-least-once** | A message is guaranteed to be delivered 1 or more times — retried until confirmed | Messages can be **duplicated** |
| **Exactly-once** | A message is delivered and processed exactly one time, no loss, no duplication | Hardest to achieve; usually means "effectively-once" via at-least-once + idempotency |

Most real-world systems (Kafka, SQS, Event Hub) default to **at-least-once** delivery, because
guaranteeing "exactly-once" end-to-end (including the consumer's side effects, like sending a WhatsApp
message) is extremely hard across independent systems. Instead of trying to prevent all duplicates at
the broker level, the standard approach is: **accept that duplicates can happen, and make consumers
idempotent** so a duplicate causes no incorrect effect.

## What "idempotent" means here

An operation is **idempotent** if performing it multiple times has the same effect as performing it
once. `PUT /users/5 {name: "Alex"}` is idempotent (repeating it doesn't change the outcome). `POST
/send-whatsapp-message` is **not** naturally idempotent — calling it twice sends two messages. Making a
notification system idempotent means adding logic so that a second call for the "same" logical
notification is a safe no-op.

## How idempotency is actually implemented for notifications (WhatsApp/SMS/email/etc.)

### Step 1 — Derive a unique, deterministic idempotency key

```
idempotencyKey = hash(userId + variantId + eventType + "back_in_stock")
```

Deterministic means: if the *same* event is processed twice (e.g., due to at-least-once redelivery),
recomputing the key from the same event data gives the **same key** both times — it's not a random
UUID generated fresh per attempt.

### Step 2 — Store the key together with processing status

```sql
CREATE TABLE notification_deliveries (
    idempotency_key VARCHAR PRIMARY KEY,
    user_id VARCHAR NOT NULL,
    channel VARCHAR NOT NULL,
    status VARCHAR NOT NULL,          -- PENDING or SENT
    provider_message_id VARCHAR,
    updated_at TIMESTAMP NOT NULL
);
```

When a worker receives the event, it tries to create a `PENDING` row:

```sql
INSERT INTO notification_deliveries
    (idempotency_key, user_id, channel, status, updated_at)
VALUES (?, ?, ?, 'PENDING', now());
```

- **Insert succeeds:** this worker reserved the notification and may send it.
- **The key already exists with `SENT`:** the notification is complete, so skip it.
- **The key already exists with `PENDING`:** the previous worker may have crashed. Do not permanently
  skip it; retry it after a timeout or let a recovery job pick it up.

The primary key still prevents two workers from creating two records for the same logical
notification. In a real implementation, workers also use a lease, row lock, or atomic status update so
that only one worker actively processes an old `PENDING` row at a time.

### Step 3 — Providers (Twilio, WhatsApp Business API, etc.) often support idempotency keys too

You can pass your idempotency key directly to the provider's send API:

```
POST /send-message
Idempotency-Key: <same key as above>
```

If your own service retries the HTTP call (e.g., because it didn't get a response and doesn't know if
the first attempt succeeded), the provider recognizes the repeated key and does **not** send the
message twice — it just returns the result of the original call. This protects against duplication at
the network layer between your service and the provider, which the dedup table alone doesn't cover.

### Step 4 — Mark the row `SENT` only after the provider confirms success

```sql
UPDATE notification_deliveries
SET status = 'SENT', provider_message_id = ?, updated_at = now()
WHERE idempotency_key = ?;
```

## The important crash window: DB row created, but notification not yet sent

Yes, inserting the key and then blindly skipping every duplicate does **not** solve this case:

```
1. Insert key into DB
2. Process crashes
3. WhatsApp was never called
4. Redelivery sees the key and skips it forever  -> notification is lost
```

That is why a row should not mean "sent" merely because it exists. Its status tells us whether the
work is only reserved (`PENDING`) or actually confirmed (`SENT`). After a crash, a retry or recovery
worker sees the stale `PENDING` row and calls the provider again with the **same idempotency key**.

There is a second, harder crash window:

```
1. Provider accepts and sends the WhatsApp message
2. Process crashes before changing PENDING to SENT
3. Recovery worker cannot tell from its own DB whether the send happened
```

If the provider supports idempotency keys, retrying with the same key is safe: the provider returns
the first result rather than sending twice. If it supports querying by a client request ID, the worker
can query the original result before retrying.

If the provider supports neither feature, there is no perfect general solution across these two
independent systems. Marking `SENT` before the API call can lose a message; marking it afterward can
occasionally duplicate one. The system must choose the acceptable trade-off and use reconciliation,
provider delivery records, or manual handling for uncertain outcomes. A local database transaction
cannot atomically include an external WhatsApp API call.

## Full realistic flow

```
1. Worker receives "stock changed" event (possibly delivered more than once, per at-least-once)
2. Compute idempotencyKey = hash(userId, variantId, notificationType)
3. INSERT a PENDING delivery row
  -> key is SENT?     acknowledge the broker event and stop
  -> key is PENDING?  resume/retry it when no other worker owns it
  -> new key?         continue
4. Call the provider, passing idempotencyKey as the provider's idempotency key
5. Provider confirms success (or returns the earlier result for the same key)
6. UPDATE the row to SENT
7. Acknowledge/commit the broker message
```

If the worker crashes before step 7, the broker may redeliver the event. The `SENT` row makes that
redelivery a no-op. If it crashes during steps 4-6, the `PENDING` row and provider-side idempotency make
the operation safely resumable.

## Dead-Letter Queue (DLQ) — related, frequently asked concept

If a consumer keeps failing to process a message (e.g., due to a bug or invalid data), most brokers
support a **DLQ**: after N failed retries, the message is moved to a separate "dead letter" queue
instead of retrying forever. This prevents one bad message from blocking the whole queue/partition, and
lets engineers inspect/reprocess failed messages later.

## Summary table

| Layer | Protects against |
|---|---|
| Idempotency key + `PENDING`/`SENT` state in your DB | Coordinates workers and remembers completed events across broker redelivery |
| Provider-side idempotency key | Makes retrying an uncertain external API call safe after a timeout or crash |
| DLQ | A single bad/failing message blocking the queue/partition forever |

See also: [`OUTBOX_PATTERN.md`](./OUTBOX_PATTERN.md) (the outbox relay can redeliver a row if it
crashes mid-publish — this is exactly the kind of duplicate that idempotency here is meant to absorb),
and [`INTERVIEW_QA.md`](./INTERVIEW_QA.md).
