# Event-Driven Architecture — Outbox, Message Brokers, Idempotency

This folder collects the backend/system-design concepts that come up naturally once you go past the
in-memory Observer example in
[`src/designpatterns/observablepattern/example/BackInStockNotificationExample.java`](../../designpatterns/observablepattern/example/BackInStockNotificationExample.java)
and ask "how would this actually work in production, across real services?"

These are also some of the most frequently asked backend/system-design interview topics.

## Why this exists

`InMemoryEventBus` in the example is a **topic-based Observer/Pub-Sub simulation** — one process,
synchronous, in-memory. Real systems replace it with a network message broker (Kafka, SQS, Event Hub,
etc.) and need extra patterns to stay correct and safe across process/service boundaries. This folder
explains those pieces.

## Files

| File | What it covers |
|---|---|
| [`OUTBOX_PATTERN.md`](./OUTBOX_PATTERN.md) | The dual-write problem, Transactional Outbox pattern, Outbox Relay, CDC/Debezium |
| [`MESSAGE_BROKERS.md`](./MESSAGE_BROKERS.md) | Message queue vs event bus vs event streaming; Kafka core concepts; SQS/SNS; Azure Event Hub/Service Bus; how to choose |
| [`IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md`](./IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md) | At-most-once / at-least-once / exactly-once, idempotency keys, dedup tables, DLQs, retries |
| [`INTERVIEW_QA.md`](./INTERVIEW_QA.md) | Rapid-fire Q&A commonly asked in backend/system-design interviews on these topics |

## Suggested reading order

1. `MESSAGE_BROKERS.md` — understand what a broker/event bus actually is and the main options.
2. `OUTBOX_PATTERN.md` — understand how you safely get an event *into* that broker from your database.
3. `IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md` — understand how consumers safely handle an event that might
   arrive more than once.
4. `INTERVIEW_QA.md` — quick revision before an interview.

## How this connects to the Observer pattern example

| In `BackInStockNotificationExample.java` | In a real production system |
|---|---|
| `InMemoryEventBus.publish(event)` | Producer publishes to Kafka topic / SQS queue / Event Hub |
| `InMemoryEventBus.register(subscriber)` | Consumer group / SQS consumer / Event Hub processor subscribes |
| `EventSubscriber.subscribedEventTypes()` | Topic name / routing key / event type filter |
| `BackInStockWorker.onEvent(event)` | Consumer service processing a message off the broker |
| *(not modeled — event is published directly)* | Transactional Outbox + Outbox Relay (see `OUTBOX_PATTERN.md`) |
| *(not modeled — single process, no duplicates)* | Idempotency handling for at-least-once delivery (see `IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md`) |

So: `InMemoryEventBus` + `BackInStockWorker` is a correct, simplified teaching model of the
**topic-based Observer / Publish-Subscribe pattern**. The docs here fill in what's needed to make that
pattern hold up across real, independently-deployed, independently-failing services.
