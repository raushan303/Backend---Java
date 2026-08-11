package LLD.ParkingLot.implementation.model;

import LLD.ParkingLot.implementation.exception.InvalidParkingTicketException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class Ticket {

    private final String id;
    private final String entranceGateId;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private final Instant entryTime;
    private TicketStatus status;
    private Instant exitTime;
    private BigDecimal amount;
    private Payment payment;

    public Ticket(
            String id,
            String entranceGateId,
            Vehicle vehicle,
            ParkingSpot parkingSpot,
            Instant entryTime) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be blank");
        }
        if (entranceGateId == null || entranceGateId.isBlank()) {
            throw new IllegalArgumentException("Entrance gate ID cannot be blank");
        }
        this.id = id;
        this.entranceGateId = entranceGateId;
        this.vehicle = Objects.requireNonNull(vehicle, "Ticket vehicle cannot be null");
        this.parkingSpot = Objects.requireNonNull(parkingSpot, "Ticket parking spot cannot be null");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time cannot be null");
        this.status = TicketStatus.ACTIVE;
    }

    public synchronized void beginPayment(Instant requestedExitTime, BigDecimal calculatedAmount) {
        Objects.requireNonNull(requestedExitTime, "Exit time cannot be null");
        Objects.requireNonNull(calculatedAmount, "Calculated amount cannot be null");
        if (requestedExitTime.isBefore(entryTime)) {
            throw new InvalidParkingTicketException("Exit time cannot be before entry time");
        }
        if (status == TicketStatus.ACTIVE) {
            exitTime = requestedExitTime;
            amount = calculatedAmount;
            status = TicketStatus.PAYMENT_PENDING;
            return;
        }
        if (status != TicketStatus.PAYMENT_PENDING) {
            throw new InvalidParkingTicketException(
                    "Cannot begin payment for ticket " + id + " in status " + status);
        }
    }

    public synchronized void recordSuccessfulPayment(Payment successfulPayment) {
        Objects.requireNonNull(successfulPayment, "Payment cannot be null");
        if (status != TicketStatus.PAYMENT_PENDING) {
            throw new InvalidParkingTicketException(
                    "Ticket " + id + " is not waiting for payment");
        }
        if (!successfulPayment.ticketId().equals(id)
                || successfulPayment.status() != PaymentStatus.SUCCESSFUL
                || successfulPayment.amount().compareTo(amount) != 0) {
            throw new InvalidParkingTicketException("Payment does not match ticket " + id);
        }
        payment = successfulPayment;
        status = TicketStatus.PAID;
    }

    public synchronized void close() {
        if (status == TicketStatus.CLOSED) {
            return;
        }
        if (status != TicketStatus.PAID) {
            throw new InvalidParkingTicketException("Cannot close unpaid ticket " + id);
        }
        status = TicketStatus.CLOSED;
    }

    public String getId() {
        return id;
    }

    public String getEntranceGateId() {
        return entranceGateId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public synchronized TicketStatus getStatus() {
        return status;
    }

    public synchronized Instant getExitTime() {
        return exitTime;
    }

    public synchronized BigDecimal getAmount() {
        return amount;
    }

    public synchronized Payment getPayment() {
        return payment;
    }
}