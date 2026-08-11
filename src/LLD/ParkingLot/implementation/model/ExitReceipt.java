package LLD.ParkingLot.implementation.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record ExitReceipt(
        String ticketId,
        String exitGateId,
        String vehicleRegistrationNumber,
        String parkingSpotId,
        Instant entryTime,
        Instant exitTime,
        Duration parkedDuration,
        BigDecimal amountPaid,
        Payment payment) {

    public ExitReceipt {
        Objects.requireNonNull(ticketId, "Ticket ID cannot be null");
        Objects.requireNonNull(exitGateId, "Exit gate ID cannot be null");
        Objects.requireNonNull(vehicleRegistrationNumber, "Vehicle registration cannot be null");
        Objects.requireNonNull(parkingSpotId, "Parking spot ID cannot be null");
        Objects.requireNonNull(entryTime, "Entry time cannot be null");
        Objects.requireNonNull(exitTime, "Exit time cannot be null");
        Objects.requireNonNull(parkedDuration, "Parking duration cannot be null");
        Objects.requireNonNull(amountPaid, "Amount paid cannot be null");
        Objects.requireNonNull(payment, "Payment cannot be null");
    }
}