# Real-World Back-in-Stock Notification Example

This folder gives a concrete version of how Amazon-like "notify me when available" flow is implemented.

## Files

- `BackInStockNotificationExample.java` - runnable in-memory simulation of product/variant/subscription + event bus + worker.
- `NOTIFICATION_FLOW_DIAGRAM.md` - full end-to-end diagrams of data flow, event flow, and worker filtering.

---

## Q1) Do we have one product table for all products and one variants table for all variants?

**Answer:** Yes, usually:

- one `products` table for all products (common fields like id, title, brand, category)
- one `product_variants` table for all variants across all products

**How different variants are handled in one variants table (one-line):**
Different products can have different variant properties by storing attributes as key-value data (for example `attributes_json`) or via category-specific attribute tables.

Example:

- T-shirt variant attributes: `{ "size": "M", "color": "black" }`
- Phone variant attributes: `{ "ram_gb": "8", "storage_gb": "128", "camera_mp": "50" }`

---

## Q2) There would be a subscription table that keeps all this info, right?

**Answer:** Yes. A subscription table stores which user wants notification for which variant.

Typical columns:

- `id`
- `user_id`
- `variant_id`
- `channel` (email/push/sms)
- `status` (active/notified/unsubscribed)
- timestamps

---

## Q3) Background worker: what events does it receive, and how does it avoid thousands of unrelated events?

**Answer:** Worker subscribes only to required event types/topics/routing keys.

Examples:

- Back-in-stock worker subscribes to `inventory.variant.stock-changed`
- Payment worker subscribes to payment topics
- Shipping worker subscribes to shipping topics

In this Java example this is represented by:

- `EventType` enum containing multiple event kinds
- `BackInStockWorker.subscribedEventTypes()` returning only `VARIANT_STOCK_CHANGED`

So the worker naturally ignores unrelated events.

---

## Q4) App publishes event when stock changes - how does this work in real systems?

**Answer (common path):**

1. Inventory service updates stock in DB
2. Same operation also publishes `VariantStockChangedEvent` (often with outbox pattern for reliability)
3. Queue/stream delivers event to subscribed worker
4. Worker checks transition (`0 -> >0`), fetches matching subscriptions, sends notifications

### Managed infra options

- **AWS**: SNS + SQS, EventBridge, Kinesis, MSK (Kafka)
- **Azure**: Service Bus, Event Grid, Event Hubs
- **GCP**: Pub/Sub

### How binding works

- App/service uses SDK/client to publish to topic/queue
- Worker uses consumer SDK and subscribes to specific topic/subscription
- Infra handles retries, DLQ, scaling, and delivery guarantees

---

## Q5) CDC/binlog flow (alternative to app-published events)

1. DB row changes (inventory update)
2. CDC tool (Debezium, etc.) reads binlog/redo log
3. CDC emits events to Kafka/Event Hub/PubSub
4. Worker consumes only inventory-related stream and processes it

Use CDC when centralized change capture is preferred or writer services cannot be easily modified.

---

## How this maps to Observer Pattern

- DB tables replace in-memory subscriber lists
- event bus/queue replaces direct in-memory `notify()` loops
- distributed workers act as observers

So concept is same as Observer Pattern, but production implementation uses persistence + messaging + background consumers.
