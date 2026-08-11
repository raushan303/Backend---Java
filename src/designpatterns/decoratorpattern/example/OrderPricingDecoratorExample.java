package designpatterns.decoratorpattern.example;

import java.util.Objects;

/**
 * Real-world style example showing dynamic order option composition at runtime.
 */
public class OrderPricingDecoratorExample {

    public static void main(String[] args) {
        Order plainOrder = new StandardOrder("ORD-101", 1200.0);
        printSummary("Standard order", plainOrder);

        Order festivalOrder = new GiftWrapDecorator(
                new PriorityShippingDecorator(
                        new PurchaseProtectionDecorator(new StandardOrder("ORD-102", 2400.0))));
        printSummary("Festival order", festivalOrder);
    }

    private static void printSummary(String label, Order order) {
        System.out.println(label + " -> " + order.summary() + " | Payable: " + order.totalCost());
    }

    interface Order {
        String summary();

        double totalCost();
    }

    static class StandardOrder implements Order {
        private final String orderId;
        private final double baseAmount;

        StandardOrder(String orderId, double baseAmount) {
            this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
            if (baseAmount < 0) {
                throw new IllegalArgumentException("Base amount cannot be negative");
            }
            this.baseAmount = baseAmount;
        }

        @Override
        public String summary() {
            return "Order " + orderId + " (base amount)";
        }

        @Override
        public double totalCost() {
            return baseAmount;
        }
    }

    abstract static class OrderOptionDecorator implements Order {
        protected final Order order;

        OrderOptionDecorator(Order order) {
            this.order = Objects.requireNonNull(order, "Order cannot be null");
        }
    }

    static class GiftWrapDecorator extends OrderOptionDecorator {
        private static final double GIFT_WRAP_COST = 80.0;

        GiftWrapDecorator(Order order) {
            super(order);
        }

        @Override
        public String summary() {
            return order.summary() + ", gift wrap";
        }

        @Override
        public double totalCost() {
            return order.totalCost() + GIFT_WRAP_COST;
        }
    }

    static class PriorityShippingDecorator extends OrderOptionDecorator {
        private static final double PRIORITY_SHIPPING_COST = 150.0;

        PriorityShippingDecorator(Order order) {
            super(order);
        }

        @Override
        public String summary() {
            return order.summary() + ", priority shipping";
        }

        @Override
        public double totalCost() {
            return order.totalCost() + PRIORITY_SHIPPING_COST;
        }
    }

    static class PurchaseProtectionDecorator extends OrderOptionDecorator {
        private static final double PURCHASE_PROTECTION_COST = 60.0;

        PurchaseProtectionDecorator(Order order) {
            super(order);
        }

        @Override
        public String summary() {
            return order.summary() + ", purchase protection";
        }

        @Override
        public double totalCost() {
            return order.totalCost() + PURCHASE_PROTECTION_COST;
        }
    }
}
