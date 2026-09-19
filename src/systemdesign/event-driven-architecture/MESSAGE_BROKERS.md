# Message Brokers: Event Bus vs Queue vs Streaming — Kafka, SQS/SNS, Event Hub/Service Bus

## First: three terms that get used loosely — clarified

| Term | What it really means |
|---|---|
| **Message queue** | Point-to-point delivery. A message is typically consumed (and removed) by **one** consumer. Good for task/work distribution. (e.g., SQS) |
| **Event bus / Pub-Sub** | One publisher, many independent subscribers, usually topic-based. Every subscriber of a topic gets its own copy of the message. (e.g., SNS, `InMemoryEventBus` in this repo's example) |
| **Event streaming platform** | A durable, ordered, replayable log of events that many consumer groups can read **independently and at their own pace**, and can be **replayed** from any earlier point. (e.g., Kafka) |

`InMemoryEventBus` in
[`BackInStockNotificationExample.java`](../../designpatterns/observablepattern/example/BackInStockNotificationExample.java)
is explicitly **not** a queue (no persistence, no replay, synchronous, in-process) — it's a simulation of
the "event bus" row above, simplified for teaching the Observer/Pub-Sub pattern.

## Kafka — the most commonly asked-about system in interviews

Kafka is an **event streaming platform**: a distributed, append-only, persistent log.

Key concepts (know these cold for interviews):

- **Topic** — a named stream of events (e.g., `variant-stock-changed`). Analogous to the `EventType` in
  this repo's example, but as a first-class broker concept.
- **Partition** — a topic is split into partitions for parallelism and scale. Each partition is an
  ordered, append-only log. Ordering is guaranteed **within a partition**, not across the whole topic.
- **Offset** — each message in a partition has a sequential ID (offset). Consumers track which offset
  they've processed up to, so they can resume after a restart.
- **Producer** — publishes events to a topic (optionally to a specific partition, often by hashing a
  "key" like `variantId`, so all events for the same variant land in the same partition and stay
  ordered relative to each other).
- **Consumer group** — a set of consumer instances sharing the work of reading a topic; Kafka assigns
  each partition to exactly one consumer within the group, enabling horizontal scaling. Multiple
  *different* consumer groups can each independently read the entire topic (this is what makes Kafka
  "pub-sub" capable, not just a queue).
- **Replication** — each partition is copied across multiple brokers so data survives a broker failure.
- **Retention** — Kafka keeps messages for a configured period (e.g., 7 days) or size limit, regardless
  of whether they've been consumed — this is what enables **replay** (a new consumer, or a consumer
  fixing a bug, can re-read historical events).
- **Log compaction** — an alternative retention mode that keeps only the latest event per key forever
  (useful for "current state" topics, e.g., latest stock level per variant).

### Why Kafka over a simple queue for many systems?

- Multiple independent services can consume the *same* events without competing for them (e.g., both a
  `BackInStockWorker` and an `AnalyticsWorker` can independently process every stock-change event).
- Replay capability — a new service launched later can re-process historical events.
- Very high throughput, designed for large-scale event pipelines.

## SQS (Simple Queue Service) and SNS (Simple Notification Service) — AWS

- **SQS** = a **queue**. A message is delivered to (typically) one consumer and then removed/deleted.
  Great for task distribution, decoupling producer/consumer load, and built-in retry + **Dead-Letter
  Queue (DLQ)** support for messages that repeatedly fail processing.
- **SNS** = a **pub-sub topic**. One published message can fan out to many different destinations
  (multiple SQS queues, Lambda functions, HTTP endpoints, etc.) — each subscriber gets its own copy.
- Common pattern: **"fan-out" = SNS topic → multiple SQS queues** (one per consumer service), combining
  pub-sub fan-out with per-consumer queue durability/retry.
- No built-in long-term replay like Kafka — once a message is consumed/deleted from SQS, it's gone
  (SQS also isn't an ordered log by default, though FIFO queues add strict ordering + dedup for a
  throughput trade-off).

## Azure equivalents

- **Azure Service Bus** — closest analog to SQS/queues (plus topics/subscriptions for pub-sub), with
  strong enterprise messaging features (sessions, dead-lettering, scheduled delivery).
- **Azure Event Hub** — closest analog to Kafka (partitioned, durable, replayable event streaming log,
  designed for high-throughput event ingestion). In fact, Event Hub has a "Kafka-compatible" endpoint,
  since the concepts map almost 1:1 (partitions, consumer groups, offsets/checkpoints).
- **Azure Event Grid** — closest analog to SNS (lightweight, low-latency pub-sub/event routing for
  reactive, low-volume event notification scenarios, not high-throughput streaming).

## Quick comparison table

| | Model | Ordering | Replay | Typical use |
|---|---|---|---|---|
| **Kafka** | Streaming log | Per-partition | Yes (retention window / compacted) | High-throughput event pipelines, multiple independent consumers |
| **SQS** | Queue | FIFO queues only | No | Task/work distribution, decoupling services |
| **SNS** | Pub-Sub | No | No | Fan-out notifications to multiple subscribers |
| **Azure Event Hub** | Streaming log | Per-partition | Yes | Same use case as Kafka |
| **Azure Service Bus** | Queue / Topic | FIFO sessions optional | No | Same use case as SQS/SNS |
| **RabbitMQ** | Queue (with flexible routing/exchanges) | Per-queue | No (by default) | Complex routing, lower-throughput reliable messaging |

## How to choose (a common interview question)

- Need multiple independent teams/services to consume the same event stream, possibly replay history,
  and handle very high throughput? → **Kafka / Event Hub**.
- Need simple, reliable task distribution to worker instances, each task processed once? → **SQS /
  Service Bus queue**.
- Need to notify multiple different downstream systems the instant something happens, without needing
  replay or huge throughput? → **SNS / Event Grid** (pub-sub).

See also: [`OUTBOX_PATTERN.md`](./OUTBOX_PATTERN.md) for how events safely get *into* any of these
brokers from your database, and
[`IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md`](./IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md) for how consumers
safely handle a message that arrives more than once.
