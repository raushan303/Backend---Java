package LLD.ParkingLot.implementation.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Payment(
        String id,
        String ticketId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        Instant paidAt) {

    public Payment {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be blank");
        }
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be blank");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        Objects.requireNonNull(method, "Payment method cannot be null");
        Objects.requireNonNull(status, "Payment status cannot be null");
        Objects.requireNonNull(paidAt, "Payment time cannot be null");
    }
}