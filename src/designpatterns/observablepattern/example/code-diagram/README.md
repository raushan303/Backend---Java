# Code Diagram - BackInStockNotificationExample.java

This README is focused only on the Java code structure and call flow from:

- `src/designpatterns/observablepattern/example/BackInStockNotificationExample.java`

## 1) Class/Type Relationship Diagram

```mermaid
classDiagram
    class BackInStockNotificationExample {
      +main(String[] args)
    }

    class EventType {
      <<enum>>
      VARIANT_STOCK_CHANGED
      ORDER_PLACED
      PRICE_CHANGED
    }

    class DomainEvent {
      <<interface>>
      +type() EventType
    }

    class VariantStockChangedEvent {
      +variantId() String
      +oldQuantity() int
      +newQuantity() int
      +type() EventType
      +movedFromOutOfStockToInStock() boolean
    }

    class EventSubscriber {
      <<interface>>
      +subscribedEventTypes() Set~EventType~
      +onEvent(DomainEvent)
    }

    class InMemoryEventBus {
      -subscriberMap Map~EventType,List~EventSubscriber~~
      +register(EventSubscriber)
      +publish(DomainEvent)
    }

    class Product {
      +productId() String
      +name() String
    }

    class Variant {
      -variantId String
      -productId String
      -attributes Map~String,String~
      -quantity int
      +variantId() String
      +quantity() int
      +updateQuantity(int) int
    }

    class Subscription {
      +subscriptionId() String
      +userId() String
      +variantId() String
      +channel() String
    }

    class ProductCatalogRepository {
      -productsById Map~String,Product~
      -variantsById Map~String,Variant~
      +saveProduct(Product)
      +saveVariant(Variant)
      +getVariant(String) Variant
    }

    class SubscriptionRepository {
      -subscriptionsByVariantId Map~String,List~Subscription~~
      +addSubscription(Subscription)
      +findByVariantId(String) List~Subscription~
    }

    class InventoryService {
      -catalogRepository ProductCatalogRepository
      -eventBus InMemoryEventBus
      +updateVariantStock(String,int)
    }

    class BackInStockWorker {
      -subscriptionRepository SubscriptionRepository
      -notificationService NotificationService
      +subscribedEventTypes() Set~EventType~
      +onEvent(DomainEvent)
    }

    class NotificationService {
      +send(Subscription,String)
    }

    DomainEvent <|.. VariantStockChangedEvent
    EventSubscriber <|.. BackInStockWorker

    InventoryService --> ProductCatalogRepository
    InventoryService --> InMemoryEventBus

    InMemoryEventBus --> EventSubscriber

    ProductCatalogRepository --> Product
    ProductCatalogRepository --> Variant

    SubscriptionRepository --> Subscription

    BackInStockWorker --> SubscriptionRepository
    BackInStockWorker --> NotificationService
```

## 2) Runtime Flow in This Code

```mermaid
sequenceDiagram
    autonumber
    participant Main as main()
    participant CatalogRepo as ProductCatalogRepository
    participant SubRepo as SubscriptionRepository
    participant Bus as InMemoryEventBus
    participant Worker as BackInStockWorker
    participant InvService as InventoryService
    participant Notifier as NotificationService

    Main->>Worker: create worker(SubRepo, Notifier)
    Main->>Bus: register(worker)
    Main->>CatalogRepo: saveProduct(product rows)
    Main->>CatalogRepo: saveVariant(variant rows)
    Main->>SubRepo: addSubscription(user, variant, channel)

    Main->>InvService: updateVariantStock(variantId, newQty)
    InvService->>CatalogRepo: getVariant(variantId)
    InvService->>CatalogRepo: variant.updateQuantity(newQty)
    InvService->>Bus: publish(VariantStockChangedEvent)

    Bus->>Worker: onEvent(stockChanged)
    Worker->>Worker: check old<=0 && new>0
    Worker->>SubRepo: findByVariantId(variantId)

    loop each subscription
      Worker->>Notifier: send(subscription, variantId)
    end
```

## 3) Minimal responsibility map

- `InventoryService`: updates stock and emits stock-change event.
- `InMemoryEventBus`: routes events only to subscribers of that event type.
- `BackInStockWorker`: listens only for stock-change events and applies back-in-stock condition.
- `SubscriptionRepository`: resolves who subscribed to that exact variant.
- `NotificationService`: sends final user notification on selected channel.
