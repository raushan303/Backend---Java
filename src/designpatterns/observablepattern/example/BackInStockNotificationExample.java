package designpatterns.observablepattern.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Real-world style back-in-stock notification flow using Product + Variant + Subscription + Event Worker.
 */
public class BackInStockNotificationExample {

    public static void main(String[] args) {
        // Simulated repositories (in real systems these are DB-backed repositories).
        ProductCatalogRepository catalogRepository = new ProductCatalogRepository();
        SubscriptionRepository subscriptionRepository = new SubscriptionRepository();

        // Simulated event infrastructure (in real systems this is Kafka/SNS+SQS/Service Bus etc.).
        InMemoryEventBus eventBus = new InMemoryEventBus();

        // Worker subscribes only to stock-change events and sends notifications.
        NotificationService notificationService = new NotificationService();
        BackInStockWorker backInStockWorker = new BackInStockWorker(subscriptionRepository, notificationService);
        eventBus.register(backInStockWorker);

        InventoryService inventoryService = new InventoryService(catalogRepository, eventBus);

        Product tshirt = new Product("P-TSHIRT", "Cotton T-Shirt");
        Product phone = new Product("P-PHONE", "SuperPhone X");

        catalogRepository.saveProduct(tshirt);
        catalogRepository.saveProduct(phone);

        // One Variant table stores all variant rows for all products. Attributes define differences.
        Variant tshirtM = new Variant("V-TSHIRT-M", "P-TSHIRT", Map.of("size", "M", "color", "black"), 0);
        Variant phone128 = new Variant("V-PHONE-128-8", "P-PHONE",
                Map.of("ram_gb", "8", "storage_gb", "128", "camera_mp", "50"), 0);

        catalogRepository.saveVariant(tshirtM);
        catalogRepository.saveVariant(phone128);

        subscriptionRepository.addSubscription(new Subscription("S-1", "USER-101", "V-TSHIRT-M", "email"));
        subscriptionRepository.addSubscription(new Subscription("S-2", "USER-202", "V-PHONE-128-8", "push"));

        // Inventory update publishes stock-changed events.
        inventoryService.updateVariantStock("V-TSHIRT-M", 10);
        inventoryService.updateVariantStock("V-PHONE-128-8", 7);
    }

    enum EventType {
        VARIANT_STOCK_CHANGED,
        ORDER_PLACED,
        PRICE_CHANGED
    }

    interface DomainEvent {
        EventType type();
    }

    record VariantStockChangedEvent(String variantId, int oldQuantity, int newQuantity) implements DomainEvent {
        @Override
        public EventType type() {
            return EventType.VARIANT_STOCK_CHANGED;
        }

        boolean movedFromOutOfStockToInStock() {
            // Notify only when variant becomes available.
            return oldQuantity <= 0 && newQuantity > 0;
        }
    }

    interface EventSubscriber {
        Set<EventType> subscribedEventTypes();

        void onEvent(DomainEvent event);
    }

    static class InMemoryEventBus {

        // Event-type-wise subscriber registry. This is equivalent to topic-based routing.
        private final Map<EventType, List<EventSubscriber>> subscriberMap = new EnumMap<>(EventType.class);

        void register(EventSubscriber subscriber) {
            Objects.requireNonNull(subscriber, "Subscriber cannot be null");
            for (EventType type : subscriber.subscribedEventTypes()) {
                subscriberMap.computeIfAbsent(type, ignored -> new ArrayList<>()).add(subscriber);
            }
        }

        void publish(DomainEvent event) {
            Objects.requireNonNull(event, "Event cannot be null");
            // Only subscribers for this exact event type will receive the event.
            List<EventSubscriber> subscribers = subscriberMap.getOrDefault(event.type(), Collections.emptyList());
            for (EventSubscriber subscriber : subscribers) {
                subscriber.onEvent(event);
            }
        }
    }

    record Product(String productId, String name) {
        Product {
            Objects.requireNonNull(productId, "Product ID cannot be null");
            Objects.requireNonNull(name, "Name cannot be null");
        }
    }

    static class Variant {
        private final String variantId;
        private final String productId;
        private final Map<String, String> attributes;
        private int quantity;

        Variant(String variantId, String productId, Map<String, String> attributes, int quantity) {
            this.variantId = Objects.requireNonNull(variantId, "Variant ID cannot be null");
            this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
            this.attributes = new HashMap<>(Objects.requireNonNull(attributes, "Attributes cannot be null"));
            this.quantity = quantity;
        }

        String variantId() {
            return variantId;
        }

        int quantity() {
            return quantity;
        }

        int updateQuantity(int newQuantity) {
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            // Return old quantity so publisher can emit before/after values in event payload.
            int oldQuantity = this.quantity;
            this.quantity = newQuantity;
            return oldQuantity;
        }

        @Override
        public String toString() {
            return "Variant{" +
                    "variantId='" + variantId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", attributes=" + attributes +
                    ", quantity=" + quantity +
                    '}';
        }
    }

    record Subscription(String subscriptionId, String userId, String variantId, String channel) {
        Subscription {
            Objects.requireNonNull(subscriptionId, "Subscription ID cannot be null");
            Objects.requireNonNull(userId, "User ID cannot be null");
            Objects.requireNonNull(variantId, "Variant ID cannot be null");
            Objects.requireNonNull(channel, "Channel cannot be null");
        }
    }

    static class ProductCatalogRepository {

        private final Map<String, Product> productsById = new HashMap<>();
        private final Map<String, Variant> variantsById = new HashMap<>();

        void saveProduct(Product product) {
            productsById.put(product.productId(), product);
        }

        void saveVariant(Variant variant) {
            if (!productsById.containsKey(variant.productId)) {
                throw new IllegalArgumentException("Unknown product ID for variant: " + variant.productId);
            }
            variantsById.put(variant.variantId(), variant);
        }

        Variant getVariant(String variantId) {
            Variant variant = variantsById.get(variantId);
            if (variant == null) {
                throw new IllegalArgumentException("Unknown variant ID: " + variantId);
            }
            return variant;
        }
    }

    static class SubscriptionRepository {

        private final Map<String, List<Subscription>> subscriptionsByVariantId = new HashMap<>();

        void addSubscription(Subscription subscription) {
            subscriptionsByVariantId
                    .computeIfAbsent(subscription.variantId(), ignored -> new ArrayList<>())
                    .add(subscription);
        }

        List<Subscription> findByVariantId(String variantId) {
            // DB equivalent: SELECT * FROM subscriptions WHERE variant_id = ? AND status = 'active'
            return subscriptionsByVariantId.getOrDefault(variantId, List.of());
        }
    }

    static class InventoryService {
        private final ProductCatalogRepository catalogRepository;
        private final InMemoryEventBus eventBus;

        InventoryService(ProductCatalogRepository catalogRepository, InMemoryEventBus eventBus) {
            this.catalogRepository = Objects.requireNonNull(catalogRepository, "Catalog repository cannot be null");
            this.eventBus = Objects.requireNonNull(eventBus, "Event bus cannot be null");
        }

        void updateVariantStock(String variantId, int newQuantity) {
            Variant variant = catalogRepository.getVariant(variantId);
            int oldQuantity = variant.updateQuantity(newQuantity);
            // App-published event path: service writes state change and publishes integration event.
            eventBus.publish(new VariantStockChangedEvent(variantId, oldQuantity, newQuantity));
        }
    }

    static class BackInStockWorker implements EventSubscriber {

        private final SubscriptionRepository subscriptionRepository;
        private final NotificationService notificationService;

        BackInStockWorker(SubscriptionRepository subscriptionRepository, NotificationService notificationService) {
            this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository,
                    "Subscription repository cannot be null");
            this.notificationService = Objects.requireNonNull(notificationService,
                    "Notification service cannot be null");
        }

        @Override
        public Set<EventType> subscribedEventTypes() {
            // Worker declares explicit interest to avoid handling unrelated events.
            return Set.of(EventType.VARIANT_STOCK_CHANGED);
        }

        @Override
        public void onEvent(DomainEvent event) {
            if (!(event instanceof VariantStockChangedEvent stockEvent)) {
                return;
            }
            if (!stockEvent.movedFromOutOfStockToInStock()) {
                // Ignore in-stock->in-stock and in-stock->out-of-stock for this use case.
                return;
            }

            // Fetch only subscribers of this specific variant and notify through configured channels.
            List<Subscription> subscriptions = subscriptionRepository.findByVariantId(stockEvent.variantId());
            for (Subscription subscription : subscriptions) {
                notificationService.send(subscription, stockEvent.variantId());
            }
        }
    }

    static class NotificationService {
        void send(Subscription subscription, String variantId) {
            System.out.println("Notify user=" + subscription.userId()
                    + " via " + subscription.channel()
                    + " for variant=" + variantId);
        }
    }
}
