# Back-in-Stock Notification - Full Flow Diagram

This file explains complete end-to-end flow for variant-level back-in-stock notifications.

## 1) Data Model Diagram

```mermaid
erDiagram
    PRODUCTS ||--o{ PRODUCT_VARIANTS : has
    PRODUCT_VARIANTS ||--o{ SUBSCRIPTIONS : watched_by

    PRODUCTS {
      string product_id PK
      string title
      string brand
      string category
    }

    PRODUCT_VARIANTS {
      string variant_id PK
      string product_id FK
      int quantity
      string attributes_json
      datetime updated_at
    }

    SUBSCRIPTIONS {
      string subscription_id PK
      string user_id
      string variant_id FK
      string channel
      string status
      datetime created_at
      datetime updated_at
    }
```

Notes:
- `PRODUCTS` is one table for all products.
- `PRODUCT_VARIANTS` is one table for all variants; `attributes_json` captures category-specific properties.
- `SUBSCRIPTIONS` stores user-to-variant interest.

---

## 2) App-Published Event Architecture (most common)

```mermaid
flowchart LR
    U[User clicks Notify Me] --> API1[Subscription API]
    API1 --> DB[(Database)]

    ERP[Inventory source/ERP/WMS] --> API2[Inventory Service]
    API2 --> DB

    DB --> OUTBOX[(Outbox table optional)]
    API2 --> BUS[(Topic/Queue/Event Bus)]
    OUTBOX --> RELAY[Outbox Relay]
    RELAY --> BUS

    BUS --> W1[BackInStock Worker]
    BUS --> W2[Other Workers Payment Shipping etc]

    W1 --> DB
    W1 --> N[Notification Service]
    N --> EMAIL[Email Provider]
    N --> PUSH[Push Provider]
    N --> SMS[SMS Provider]
```

Notes:
- You either publish directly from service or via outbox relay.
- Multiple workers consume from same platform but different topics/subscriptions.

---

## 3) Worker Event Filtering Diagram

```mermaid
flowchart TD
    E1[Event: inventory.variant.stock-changed] --> ROUTER[Broker routing]
    E2[Event: payment.completed] --> ROUTER
    E3[Event: shipping.created] --> ROUTER
    E4[Event: price.changed] --> ROUTER

    ROUTER --> SUB1[Subscription A: BackInStock Worker]
    ROUTER --> SUB2[Subscription B: Payment Worker]
    ROUTER --> SUB3[Subscription C: Shipping Worker]

    SUB1 -->|receives only inventory.variant.stock-changed| BSW[BackInStock Worker]
    SUB2 -->|receives only payment.*| PW[Payment Worker]
    SUB3 -->|receives only shipping.*| SW[Shipping Worker]
```

This is why back-in-stock worker does not process thousands of unrelated events.

---

## 4) Runtime Sequence (App-Published Event)

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant UI
    participant SubscriptionAPI
    participant DB
    participant InventoryService
    participant Bus as Event Bus
    participant Worker as BackInStock Worker
    participant NotificationService
    participant Provider as Email/Push/SMS

    User->>UI: Clicks "Notify me"
    UI->>SubscriptionAPI: Create subscription(user_id, variant_id, channel)
    SubscriptionAPI->>DB: INSERT into subscriptions

    Note over InventoryService,DB: Later inventory is replenished
    InventoryService->>DB: UPDATE product_variants SET quantity = 10
    InventoryService->>Bus: Publish VariantStockChanged(variant_id, old=0, new=10)

    Bus->>Worker: Deliver stock-changed event
    Worker->>Worker: Check transition old<=0 and new>0
    Worker->>DB: SELECT subscriptions WHERE variant_id=? AND status='active'
    DB-->>Worker: Matching subscribers

    loop For each subscriber
      Worker->>NotificationService: send(subscription, variant)
      NotificationService->>Provider: Send email/push/sms
      Provider-->>NotificationService: delivery status
    end

    Worker->>DB: Mark status notified or keep active per policy
```

---

## 5) CDC/Binlog Alternative Sequence

```mermaid
sequenceDiagram
    autonumber
    participant InventoryService
    participant DB
    participant CDC as CDC Connector (Debezium)
    participant Bus as Kafka/EventHub/PubSub
    participant Worker as BackInStock Worker

    InventoryService->>DB: UPDATE product_variants SET quantity = 10
    CDC->>DB: Read binlog/redo log changes
    CDC->>Bus: Publish stock-change event derived from DB log
    Bus->>Worker: Deliver inventory event
    Worker->>Worker: Same filtering + notify logic
```

Use CDC when you want standardized event capture from DB changes without modifying all writer services.

---

## 6) Reliability and Control Points

- **Idempotency:** Worker should avoid duplicate notifications for same user+variant+version.
- **Retries + DLQ:** Broker retries transient failures; poison messages go to dead-letter queue.
- **Backpressure:** Queue decouples spike in stock updates from notification throughput.
- **Observability:** Track publish lag, consumer lag, success/failure rate, and DLQ count.
- **Policy:** Decide whether to keep subscription active after first notify.

---

## 7) Terminology notes (quick one-liners)

- **Outbox table:** DB table storing pending events that must be published reliably.
- **Outbox relay:** Background process that reads outbox rows and publishes them to broker.
- **Broker:** Messaging system that accepts events and routes them to consumers.
- **Topic/stream:** Named channel where related events are published.
- **Subscription (broker-side):** Consumer binding that receives events from selected topic/routing rules.
- **Routing:** Rule-based delivery of events to matching queues/subscriptions.
- **Consumer:** Worker process that reads and handles events.
- **DLQ (Dead-Letter Queue):** Queue for failed messages after retry limit.
- **Retry:** Attempt to process same failed event again after delay/backoff.
- **Idempotency:** Safe repeated handling of same event without duplicate user notifications.
- **CDC:** Change Data Capture; turns database row changes into external events.
- **Binlog/redo log:** Database internal transaction log used by CDC connectors.
- **Debezium:** CDC connector that reads DB logs and emits structured events.
- **ERP/WMS:** Upstream inventory systems (Enterprise Resource Planning / Warehouse Management System).
- **Backpressure:** Throughput control to prevent worker/provider overload during event spikes.
