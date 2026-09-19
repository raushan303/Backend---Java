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

### Step 2 — Use a dedup table with a unique constraint as the "have I already done this?" check

```sql
CREATE TABLE sent_notifications (
    idempotency_key VARCHAR PRIMARY KEY,
    user_id VARCHAR NOT NULL,
    channel VARCHAR NOT NULL,
    sent_at TIMESTAMP NOT NULL
);
```

Before sending:

```sql
INSERT INTO sent_notifications (idempotency_key, user_id, channel, sent_at)
VALUES (?, ?, ?, now());
```

- **First time:** insert succeeds → proceed to call the WhatsApp/SMS provider's send API.
- **Second time (duplicate event):** insert **fails** (unique/primary key constraint violation) → code
  catches this and simply **skips sending** — the failed insert itself proves it was already handled.

This is far more reliable than an in-memory flag or a "check-then-send" read followed by a separate
write, because a unique constraint enforced by the database prevents a race condition where two
duplicate deliveries are processed at almost the same time (both could pass a "have I sent it?" read
check before either writes, without a atomic uniqueness guarantee).

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

## Full realistic flow

```
1. Worker receives "stock changed" event (possibly delivered more than once, per at-least-once)
2. Compute idempotencyKey = hash(userId, variantId, notificationType)
3. INSERT INTO sent_notifications (idempotency_key, ...)
   -> fails (duplicate key)?  STOP — already handled, do nothing
   -> succeeds?  continue
4. Call WhatsApp/SMS provider's send API, passing idempotencyKey as the request's idempotency key
5. Provider guarantees: even if this exact API call is retried, only one message reaches the user
```

## Dead-Letter Queue (DLQ) — related, frequently asked concept

If a consumer keeps failing to process a message (e.g., due to a bug or invalid data), most brokers
support a **DLQ**: after N failed retries, the message is moved to a separate "dead letter" queue
instead of retrying forever. This prevents one bad message from blocking the whole queue/partition, and
lets engineers inspect/reprocess failed messages later.

## Summary table

| Layer | Protects against |
|---|---|
| Idempotency key + dedup table (your own DB) | Your own system processing the same event twice due to broker redelivery |
| Provider-side idempotency key | Network retries between your service and the external provider (e.g., unclear if the first HTTP call succeeded) |
| DLQ | A single bad/failing message blocking the queue/partition forever |

See also: [`OUTBOX_PATTERN.md`](./OUTBOX_PATTERN.md) (the outbox relay can redeliver a row if it
crashes mid-publish — this is exactly the kind of duplicate that idempotency here is meant to absorb),
and [`INTERVIEW_QA.md`](./INTERVIEW_QA.md).
