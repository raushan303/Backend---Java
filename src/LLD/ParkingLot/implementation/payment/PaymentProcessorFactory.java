package LLD.ParkingLot.implementation.payment;

import LLD.ParkingLot.implementation.model.PaymentMethod;

import java.util.EnumMap;
import java.util.Map;

public final class PaymentProcessorFactory {

    private final Map<PaymentMethod, PaymentProcessor> processors;

    public PaymentProcessorFactory() {
        this(Map.of(
                PaymentMethod.CASH, new CashPaymentProcessor(),
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor()));
    }

    public PaymentProcessorFactory(Map<PaymentMethod, PaymentProcessor> processors) {
        this.processors = new EnumMap<>(PaymentMethod.class);
        this.processors.putAll(processors);
    }

    public PaymentProcessor getProcessor(PaymentMethod paymentMethod) {
        PaymentProcessor processor = processors.get(paymentMethod);
        if (processor == null) {
            throw new IllegalArgumentException("No processor for payment method " + paymentMethod);
        }
        return processor;
    }
}