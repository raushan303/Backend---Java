# Message Brokers: Queues, Pub-Sub, and Streaming (Kafka, SQS/SNS, RabbitMQ, Azure)

This note explains message brokers from the basics. The goal is to answer questions like:

- What is a broker?
- What is a topic, queue, partition, offset, consumer group, and append-only log?
- Why does ordering matter?
- When should we use Kafka, RabbitMQ, SQS, SNS, Azure Event Hub, or Azure Service Bus?

## 1. What problem does a message broker solve?

Without a broker, one service usually calls another service directly:

```text
Order Service  --->  Email Service
```

That works, but it creates tight coupling:

- If `Email Service` is down, `Order Service` may fail or need complicated retry logic.
- If order creation is fast but emails are slow, users wait longer.
- If later we also need analytics, invoice generation, fraud checks, and notifications, `Order Service` must know about all of them.

A **message broker** sits between services:

```text
Order Service  --->  Message Broker  --->  Email Worker
Order Service  --->  Message Broker  --->  Analytics Worker
Order Service  --->  Message Broker  --->  Invoice Worker
```

A broker is usually a separate server or managed cloud service. Internally it stores messages on disk or in a replicated storage system, tracks which consumers have processed them, and delivers messages to consumers according to the broker's rules.

Think of it like a post office:

- Producers drop letters/messages at the post office.
- The post office stores and routes them.
- Consumers pick up the messages meant for them.

## 2. Message, event, producer, and consumer

| Term | Simple meaning | Example |
|---|---|---|
| **Message** | Any data sent through a broker | `SendEmail(userId=10)` |
| **Event** | A message saying something already happened | `OrderPlaced(orderId=123)` |
| **Producer / Publisher** | The service that sends the message | `Order Service` publishes `OrderPlaced` |
| **Consumer / Subscriber** | The service that reads and processes the message | `Email Service` sends an email |

A useful distinction:

- **Command/task message**: "Please do this work" (`SendWelcomeEmail`).
- **Event message**: "This happened" (`UserRegistered`).

## 3. Three common broker models

### A. Message queue: one message is handled by one worker

A **queue** is good for work distribution.

```text
Queue: [job1, job2, job3]
          |     |     |
       worker worker worker
```

If 10 workers read from the same queue, each job is normally processed by only one worker. This is useful when the work should not be repeated by every worker.

Real-world examples:

- Send one email per signup.
- Process one image upload once.
- Generate one invoice once.
- Run background jobs from a web application.

Common tools: **AWS SQS**, **Azure Service Bus queue**, **RabbitMQ queue**.

### B. Pub-sub / event bus: every subscriber gets the event

In **publish-subscribe**, a producer publishes an event to a topic/channel, and multiple independent subscribers can receive it.

```text
Order Service publishes: OrderPlaced

Topic/Event Bus ---> Email Service gets it
Topic/Event Bus ---> Analytics Service gets it
Topic/Event Bus ---> Invoice Service gets it
```

This is useful when many systems need to react to the same business event.

Real-world examples:

- After `OrderPlaced`, send email, update analytics, reserve inventory, and notify warehouse.
- After `PaymentFailed`, notify customer support and send a customer notification.

Common tools: **AWS SNS**, **Azure Event Grid**, **RabbitMQ exchanges**, and also **Kafka** when different consumer groups read the same topic.

In this repository, [`BackInStockNotificationExample.java`](../../designpatterns/observablepattern/example/BackInStockNotificationExample.java) uses an in-memory event bus to demonstrate this pub-sub idea: one stock-change event can notify multiple subscribers. It is only a teaching example, not a real broker, because it has no persistence, replay, partitions, or separate broker machine.

### C. Event streaming: a durable history of events

An **event streaming platform** stores events as a durable stream/history that consumers can read at their own speed.

```text
Kafka topic: orders
[0] OrderPlaced(123)
[1] OrderAccepted(123)
[2] OrderShipped(123)
[3] OrderPlaced(124)
```

Consumers do not necessarily remove messages when they read them. The broker keeps events for a configured retention period, such as 7 days, 30 days, or sometimes much longer.

This is useful when:

- Many services need the same events.
- New services may need to replay old events.
- The system has very high message volume.
- We need event history for analytics or rebuilding derived state.

Common tools: **Kafka** and **Azure Event Hub**.

## 4. What does "append-only log" mean?

A **log** is a sequence of records. **Append-only** means new records are added at the end; old records are not updated in place.

Example:

```text
orders partition 0
offset 0: OrderPlaced(orderId=123)
offset 1: OrderAccepted(orderId=123)
offset 2: OrderShipped(orderId=123)
```

If a new event arrives, Kafka appends it:

```text
offset 3: OrderDelivered(orderId=123)
```

It does not insert the new event in the middle or rewrite offset 1. This makes Kafka fast and makes replay possible because consumers can say, "start reading again from offset 0" or "continue from offset 3."

Related term: **log compaction** is a Kafka cleanup mode that keeps the latest message for each key instead of keeping every old message forever. For example, a compacted `latest-stock-level` topic can keep the newest stock value for each `variantId`.

This does **not** mean Kafka is a normal relational database. Kafka stores event records and metadata; it is optimized for sequential event storage and delivery. A database is optimized for querying and updating current application state. Many systems use both:

```text
Application DB stores current order row
Kafka stores the history of order events for downstream systems
```

## 5. Kafka basics, explained slowly

Kafka is a distributed event streaming platform. A Kafka cluster has multiple **brokers**. A broker is a server/machine running Kafka.

```text
Kafka cluster
+----------+   +----------+   +----------+
| Broker 1 |   | Broker 2 |   | Broker 3 |
+----------+   +----------+   +----------+
```

### Topic

A **topic** is a named stream of related events.

Examples:

- `orders`
- `payments`
- `inventory-events`
- `variant-stock-changed`

A producer writes to a topic. Consumers read from a topic.

### Partition

A **partition** is one ordered slice of a topic.

A topic can have multiple partitions:

```text
Topic: orders

Partition 0: offsets 0,1,2,3 (its own messages)
Partition 1: offsets 0,1,2,3 (its own messages)
Partition 2: offsets 0,1,2,3 (its own messages)
```

The producer's Kafka client chooses the partition by:

- a message key, commonly hashed, such as `orderId`; or
- the default partitioner if no key is provided (modern clients use sticky batching for efficiency); or
- an explicit partition chosen by the producer.

Example using `orderId` as the key:

```text
Order 123 events ---> partition 1
Order 456 events ---> partition 0
Order 789 events ---> partition 2
```

Using a stable key is important because it keeps related events in the same partition.

### Why partitions exist

Partitions provide parallelism and scale.

If one partition can be read by one consumer in a consumer group, then three partitions can be read by up to three consumers in the same group:

```text
Topic: orders, Consumer group: notification-service

Partition 0 ---> Consumer A
Partition 1 ---> Consumer B
Partition 2 ---> Consumer C
```

In Kafka, partitions are the unit of parallelism inside a consumer group. If the same group has more consumers than partitions, extra consumers sit idle:

```text
3 partitions, 5 consumers in same group

Partition 0 ---> Consumer A
Partition 1 ---> Consumer B
Partition 2 ---> Consumer C
Consumer D: idle
Consumer E: idle
```

### Offset

An **offset** is the position number of a message inside one partition.

```text
Partition 0
Offset 0: OrderPlaced(456)
Offset 1: OrderAccepted(456)
Offset 2: OrderShipped(456)
```

Consumers store/commit offsets to remember how far they have processed. If a consumer crashes, it can restart from the last committed offset.

### Consumer group

A **consumer group** is a group of consumer instances that share the work of reading a topic.

Important rule:

> Inside one consumer group, a partition is assigned to at most one consumer at a time.

That prevents two workers in the same service from processing the same partition messages as duplicate work.

But different consumer groups are independent:

```text
Topic: orders

Consumer group: notification-service
Partition 0 ---> Notification Consumer A
Partition 1 ---> Notification Consumer B

Consumer group: analytics-service
Partition 0 ---> Analytics Consumer A
Partition 1 ---> Analytics Consumer B
```

Both services can read all `orders` events, but each service scales its own workers separately.

Consumer groups are not mainly about making every consumer receive every message. They are about distributing partitions of the **same topic** among instances of the **same logical service**, so the service can scale without duplicating the same work inside that group.

### How do topics and partitions relate?

Partitions are inside topics, and one event is written to one partition of its topic.

Correct mental model:

```text
Topic orders
  partition 0
  partition 1
  partition 2

Topic payments
  partition 0
  partition 1
```

Partitions belong to a topic. A partition is not a global bucket containing all topics. Events for `orders` go only to partitions of the `orders` topic. Events for `payments` go only to partitions of the `payments` topic.

### Ordering issue

Kafka guarantees ordering only **within one partition**.

If all events for `orderId=123` use the same key, they go to the same partition:

```text
Partition 1
Offset 10: OrderPlaced(123)
Offset 11: OrderAccepted(123)
Offset 12: OrderShipped(123)
```

A consumer reading partition 1 sees those events in that order.

But Kafka does not guarantee total order across different partitions:

```text
Partition 0: OrderPlaced(456), OrderAccepted(456)
Partition 1: OrderPlaced(123), OrderAccepted(123)
```

There is no single global order between partition 0 and partition 1. That is usually fine because order 123 and order 456 are different business entities.

So the ordering issue usually means **business ordering for the same entity**, not the order of subscribers.

Example:

- Bad: `OrderAccepted(123)` is processed before `OrderPlaced(123)`.
- Good: use `orderId=123` as the Kafka key so all events for order 123 stay in one partition.

For notifications, if a user must receive `OrderPlaced` before `OrderAccepted` for the same order, key by `orderId` or another key that represents the ordering boundary.

### Replication: "each partition is copied across multiple brokers"

Kafka stores copies of each partition on multiple brokers for fault tolerance.

Example with replication factor 3:

```text
orders partition 0 copies:
- Broker 1: leader copy
- Broker 2: follower copy
- Broker 3: follower copy
```

By default, producers and consumers talk to the leader copy. Followers keep copying data from the leader.

If Broker 1 dies, Kafka can elect one follower as the new leader:

```text
Broker 2 becomes leader for orders partition 0
```

That is what "data survives a broker failure" means. The data is not stored on only one machine.

### Retention and replay

Kafka keeps messages for a configured retention policy, even after consumers read them.

Example:

- Keep events for 7 days.
- Or keep up to 500 GB.
- Or compact by key and keep the latest value for each key.

Because Kafka keeps history, a consumer can replay:

- A new analytics service can start reading from last week.
- A bug-fixed consumer can reprocess old events.
- A service can rebuild a read model/cache from past events.

Kafka does store event history, but only according to retention settings. It is not automatically a permanent database for all business data unless configured and designed that way.

## 6. Real-world Kafka example: food delivery order flow

Imagine a food delivery app.

`Order Service` publishes events to topic `orders`:

```text
OrderPlaced(orderId=123)
OrderAccepted(orderId=123)
DriverAssigned(orderId=123)
OrderPickedUp(orderId=123)
OrderDelivered(orderId=123)
```

Different services consume the same topic:

- `Notification Service`: sends push notifications to the customer.
- `Analytics Service`: updates dashboards.
- `Driver Service`: reacts to order state changes.
- `Customer Support Service`: builds a timeline agents can view.

Why Kafka helps:

- Each service can read the same event stream independently.
- Events for one order can stay ordered by using `orderId` as the key.
- Analytics can replay old events if it needs to rebuild reports.
- The system can handle large traffic by splitting the topic into partitions.

## 7. SQS and SNS, explained

### SQS: queue for work distribution

AWS SQS is a managed queue.

```text
Order Service ---> SQS queue: [email job 1] [email job 2] [email job 3]
                                  |             |             |
                              Worker 1      Worker 2      Worker 3
                         (each job goes to one worker, not all workers)
```

If there are 1,000 email jobs and 3 workers, SQS distributes jobs across workers. Each email job is normally handled by one worker.

This is what "good for task/work distribution" means: many workers can share a backlog of tasks, and each task should be done once.

Use SQS for:

- Sending emails in background.
- Processing uploaded files.
- Running retryable jobs.
- Decoupling a web request from slow work.

- After a worker successfully processes an SQS message, it explicitly deletes the message with `DeleteMessage`; otherwise, the message can become visible again and be retried.
- SQS is not meant to be a long-term replayable event history like Kafka.
- SQS also supports a **Dead-Letter Queue (DLQ)**: a separate queue where messages can be moved after they fail processing too many times, so teams can inspect or replay the failed work later.

### SNS: pub-sub fan-out

AWS SNS is a managed pub-sub topic.

```text
Order Service ---> SNS topic: order-events
                       |---> SQS queue for Email Service
                       |---> SQS queue for Analytics Service
                       |---> HTTPS endpoint for Partner System
```

SNS is useful when one event should be delivered to multiple subscribers.

A common AWS pattern is:

```text
SNS topic ---> multiple SQS queues ---> different worker services
```

SNS does the fan-out. SQS gives each subscriber its own durable queue and retry handling.

## 8. RabbitMQ, explained

RabbitMQ is a message broker often used for queues and routing.

A core RabbitMQ concept is the **exchange**.

```text
Producer ---> Exchange ---> Queue A ---> Consumer A
Producer ---> Exchange ---> Queue B ---> Consumer B
```

The producer sends a message to an exchange. The exchange decides which queue(s) should receive the message.

"Flexible routing/exchanges" means RabbitMQ can route messages in different ways:

| Exchange type | Simple meaning | Example |
|---|---|---|
| **Direct** | Route by exact routing key | `email` messages go to email queue |
| **Fanout** | Send to all bound queues | broadcast `OrderPlaced` to many queues |
| **Topic** | Route by pattern | `order.*` or `payment.failed` |
| **Headers** | Route by message headers | route by region, format, or priority |

RabbitMQ is often chosen when you need flexible routing rules, classic work queues, request/reply messaging, or lower/medium throughput reliable messaging. Kafka is usually preferred for high-throughput replayable event streams.

## 9. Azure equivalents

| Azure service | Similar idea | Use when |
|---|---|---|
| **Azure Service Bus Queue** | SQS / RabbitMQ queue | One task should be processed by one worker |
| **Azure Service Bus Topic + Subscriptions** | Pub-sub with durable subscriber queues | Multiple services need the same message, with enterprise messaging features |
| **Azure Event Hub** | Kafka-like event streaming | High-throughput event ingestion, partitions, offsets, replay within retention |
| **Azure Event Grid** | SNS-like event notification | Lightweight event notifications between Azure services or webhooks |

## 10. Quick comparison

| Tool | Main model | Does one message go to one worker or many subscribers? | Ordering | Replay/history | Typical use |
|---|---|---|---|---|---|
| **Kafka** | Streaming log | Many consumer groups can each read the same events | Within a partition | Yes, within retention | High-volume event streams, analytics, event-driven microservices |
| **Azure Event Hub** | Streaming log | Many consumer groups can each read the same events | Within a partition | Yes, within retention | Kafka-like cloud event ingestion |
| **SQS** | Queue | Typically one worker per message; at-least-once delivery means duplicates are possible | Standard: no strict order; FIFO: ordered | No long-term replay after delete | Background jobs and task distribution |
| **SNS** | Pub-sub topic | Many subscribers | No strong ordering | No | Fan-out notifications |
| **Azure Service Bus** | Queue or topic/subscription | Queue: typically one worker; topic: many subscriptions | Sessions can preserve order | Not a Kafka-style replay log | Enterprise queues/pub-sub |
| **RabbitMQ** | Queue with exchanges | Depends on exchange and queues | Usually per queue | No Kafka-style replay by default | Flexible routing and reliable work queues |

## 11. How to choose

Use **SQS / Service Bus queue / RabbitMQ queue** when:

- You have tasks/jobs to process.
- Each task should be handled by one worker.
- You mainly need retries, dead-letter queues, and worker scaling.

Use **SNS / Event Grid / RabbitMQ fanout** when:

- One event should notify multiple subscribers.
- You do not need long-term replay.
- You want simple fan-out.

Use **Kafka / Azure Event Hub** when:

- You need high-throughput event streams.
- Multiple independent services need the same events.
- Consumers may need to replay history.
- Ordering matters within a business key like `orderId`, `userId`, or `variantId`.

## 12. Common interview-level summary

- A **broker** is an external system that stores/routes messages between producers and consumers.
- A **queue** distributes work so one message is processed by one worker.
- **Pub-sub** broadcasts one event to multiple subscribers.
- **Kafka** stores events in topics split into partitions.
- A **partition** is an ordered append-only log for one slice of one topic.
- An event goes to one partition of a topic, not to every partition.
- Ordering is guaranteed inside one partition, not across all partitions.
- A **consumer group** shares partitions among instances of the same logical consumer service.
- Different consumer groups can independently read the same Kafka topic.
- **Replication** copies partition data across brokers so one broker failure does not lose data.
- **Retention** controls how long Kafka keeps old events for replay.

See also: [`OUTBOX_PATTERN.md`](./OUTBOX_PATTERN.md) for how events safely get *into* a broker from your database, and [`IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md`](./IDEMPOTENCY_AND_DELIVERY_GUARANTEES.md) for how consumers safely handle retries and duplicate delivery.
