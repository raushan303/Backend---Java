# Interview Q&A — Event-Driven Architecture, Messaging, Idempotency

Quick-revision Q&A format. See the other files in this folder for full explanations.

---

**Q1. What is the "dual-write problem"?**
When a service must update its own database and publish an event to a message broker as if it were one
atomic step, but the two are separate systems — a crash between the two operations can leave them
inconsistent (DB updated but event lost, or event published but DB write rolled back).

**Q2. What is the Transactional Outbox pattern?**
Instead of writing to the DB and the broker separately, write the event as a row in an `outbox` table
in the **same database transaction** as the actual state change. Since it's one transaction in one
database, atomicity is guaranteed by the DB itself.

**Q3. What is an Outbox Relay?**
A separate background process that polls (or uses CDC to stream) unsent rows from the `outbox` table
and publishes them to the real broker (Kafka/SQS/etc.), marking them sent afterward.

**Q4. What is CDC (Change Data Capture) and how does Debezium fit in?**
CDC means reading a database's internal write-ahead log/binlog (the log the DB already uses for
replication) to capture row-level changes in near real time, instead of polling tables with SQL
queries. Debezium is the most common open-source CDC tool, typically paired with Kafka Connect to
stream outbox rows into Kafka with low latency and no extra query load on the database.

**Q5. Why can duplicate events happen even with the outbox pattern?**
If the outbox relay crashes after publishing but before marking a row as sent, it will republish that
row on restart — the event is delivered twice. This is why consumers must be idempotent.

**Q6. What's the difference between at-most-once, at-least-once, and exactly-once delivery?**
At-most-once: may lose messages, never duplicates. At-least-once: never loses messages, may duplicate
(retries until acknowledged). Exactly-once: no loss, no duplication — very hard to guarantee end-to-end
across independent systems, so it's usually approximated as "at-least-once + idempotent consumer".

**Q7. How do you make a notification-sending consumer idempotent?**
Derive a deterministic idempotency key from the event (e.g., hash of userId + variantId + eventType).
Before sending, insert that key into a dedup table with a unique constraint; if the insert fails
(duplicate key), skip sending — it was already handled. Optionally also pass the same idempotency key
to the external provider's (WhatsApp/SMS/etc.) send API so retried HTTP calls don't cause duplicate
sends at the network layer too.

**Q8. Why use a unique DB constraint instead of a "check-then-send" in-memory or read-then-write
check?**
A read-then-write check has a race condition: two near-simultaneous duplicate deliveries could both
pass the "not yet sent" check before either writes its result. A unique constraint enforced by the
database makes the check-and-record atomic — only one of the two inserts can succeed.

**Q9. What's the difference between a message queue and an event bus/pub-sub system?**
A queue (e.g., SQS) typically delivers each message to exactly one consumer, good for distributing
work. An event bus/pub-sub system (e.g., SNS, or `InMemoryEventBus` in this repo's example) delivers
each event to every interested subscriber independently — good for fan-out/notification-style
scenarios.

**Q10. What's the difference between an event bus/pub-sub system and an event streaming platform like
Kafka?**
A basic pub-sub system typically delivers events live to whoever is currently subscribed, with no
persistence/replay. Kafka additionally persists events as an ordered, durable, replayable log —
multiple independent consumer groups can each read the whole history at their own pace, and new
consumers can replay past events.

**Q11. What is a Kafka partition, and why does it matter for ordering?**
A topic is split into partitions, each an ordered append-only log. Kafka guarantees order **within a
partition**, not across an entire topic. Producers typically key messages (e.g., by `variantId`) so all
events for the same entity land in the same partition and stay ordered relative to each other.

**Q12. What is a Kafka consumer group?**
A set of consumer instances that split the partitions of a topic between them so each partition is
processed by exactly one consumer in the group — this is how Kafka scales consumption horizontally.
Different consumer groups are independent of each other and can each read the full topic.

**Q13. What is Kafka offset/checkpointing used for?**
Each partition assigns a sequential offset to every message. Consumers track the last offset they
successfully processed so that after a restart/crash they can resume from where they left off instead
of reprocessing everything or skipping messages.

**Q14. What's the AWS equivalent of Kafka? What's the Azure equivalent?**
AWS: closest analog is not a single service — Kinesis is the closest to Kafka; SNS+SQS together
approximate simpler pub-sub/queue needs. Azure: Event Hub is the direct Kafka analog (partitions,
consumer groups/checkpoints, replay), and it even exposes a Kafka-compatible endpoint.

**Q15. What is a Dead-Letter Queue (DLQ)?**
A separate queue where messages are moved after repeatedly failing processing (exceeding a retry
limit), so one bad/poison message doesn't block the whole queue or partition indefinitely. Engineers
can inspect and optionally reprocess DLQ messages later.

**Q16. Why not just retry publishing to the broker directly instead of using an outbox table?**
A crash can happen after the DB commit but before the retry logic confirms the publish succeeded, and
there's no durable record that a publish is still owed — you'd be relying on in-memory/application
state that doesn't survive a crash. The outbox row is that durable record, persisted in the same
transaction as the real state change.

**Q17. If Kafka/SQS retries can cause duplicate delivery, why not just make the broker guarantee
exactly-once instead of pushing idempotency onto consumers?**
Some brokers (e.g., Kafka) offer "exactly-once semantics" for broker-internal processing (e.g.,
read-process-write within Kafka), but this cannot extend to external side effects like "send a WhatsApp
message" or "call a third-party API" — the broker has no way to make an external system's action
atomic with its own offset commit. So consumer-side idempotency remains necessary for any workflow that
has external side effects.

**Q18. In the `BackInStockNotificationExample.java` code, is `InMemoryEventBus` a real event bus/queue?**
No — it's a simplified, in-process, synchronous simulation of the topic-based Observer/Pub-Sub pattern
(register subscribers per event type, publish calls each matching subscriber directly, no persistence,
no replay, no cross-process delivery). It illustrates the pattern that a real broker like Kafka/SNS/SQS
implements at a networked, durable, multi-process scale.
