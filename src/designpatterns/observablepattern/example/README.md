# Real-World Back-in-Stock Notification Example

This folder gives a concrete version of how Amazon-like "notify me when available" flow is implemented.

## Files

- `BackInStockNotificationExample.java` - runnable in-memory simulation of product/variant/subscription + event bus + worker.

## 1) Product + Variant table design (direct answer)

Yes, usually there is:

- **one `products` table** for all products (common fields like id, title, brand, category)
- **one `product_variants` table** for all variants across all products

One-line handling of many variant shapes:

- Different products can have different properties by storing variant attributes as key-value data (`attributes_json`) or by using category-specific attribute tables.

Example idea:

- T-shirt variant attributes: `{ "size": "M", "color": "black" }`
- Phone variant attributes: `{ "ram_gb": "8", "storage_gb": "128", "camera_mp": "50" }`

So the table is still one `product_variants` table; only attribute content changes per category.

## 2) Subscription table

Yes, one subscription table keeps user interest in a specific variant.

Typical columns:

- `id`
- `user_id`
- `variant_id`
- `channel` (email/push/sms)
- `status` (active/notified/unsubscribed)
- timestamps

## 3) What events worker receives (and how it avoids unrelated events)

You are right that many event types exist.

In real systems, worker subscribes only to required event topics/routing keys.

- Back-in-stock worker subscribes to something like `inventory.variant.stock-changed`
- Payment worker subscribes to payment events
- Shipping worker subscribes to shipping events

In the Java example, this is modeled by:

- `EventType` enum with multiple event kinds
- `BackInStockWorker.subscribedEventTypes()` returning only `VARIANT_STOCK_CHANGED`

This is the mechanism that prevents one worker from processing thousands of unrelated events.

## 4) App publishes event when stock changes (most common)

Typical flow:

1. Inventory service updates stock in DB
2. Same operation also publishes `VariantStockChangedEvent` (often via outbox pattern for reliability)
3. Queue/stream delivers event to worker
4. Worker checks transition (`0 -> >0`), fetches matching subscriptions, sends notifications

### Infra choices

Common managed tools (examples):

- **AWS**: SNS + SQS, EventBridge, Kinesis, MSK (Kafka)
- **Azure**: Service Bus, Event Grid, Event Hubs
- **GCP**: Pub/Sub

Binding mechanism is usually:

- app/service code uses SDK/client library to publish events
- worker uses consumer SDK to subscribe specific topic/subscription
- infra handles delivery, retries, dead-letter queues, scaling

## 5) CDC/binlog path (alternative)

Instead of app code publishing directly:

1. DB row changes (inventory update)
2. CDC tool (Debezium, etc.) reads binlog/redo log
3. CDC emits structured events to Kafka/Event Hub/PubSub
4. Worker consumes only inventory-related event stream

Use CDC when you want centralized change capture or cannot modify every writer service easily.

## 6) How this maps to Observer Pattern idea

Classic in-memory Observer pattern is still conceptually valid, but at scale:

- DB tables replace in-memory subscriber lists
- event bus/queue replaces direct `notify()` loops
- workers are distributed observers

So the concept is same; implementation is persistence + messaging + background consumers.
