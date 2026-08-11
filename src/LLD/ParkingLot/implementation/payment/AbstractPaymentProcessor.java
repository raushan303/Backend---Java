package LLD.ParkingLot.implementation.payment;

import LLD.ParkingLot.implementation.model.Payment;
import LLD.ParkingLot.implementation.model.PaymentMethod;
import LLD.ParkingLot.implementation.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

abstract class AbstractPaymentProcessor implements PaymentProcessor {

    private final PaymentMethod method;

    protected AbstractPaymentProcessor(PaymentMethod method) {
        this.method = method;
    }

    @Override
    public Payment process(String ticketId, BigDecimal amount, Clock clock) {
        return new Payment(
                UUID.randomUUID().toString(),
                ticketId,
                amount,
                method,
                PaymentStatus.SUCCESSFUL,
                Instant.now(clock));
    }
}