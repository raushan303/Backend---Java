package LLD.ParkingLot.implementation.gate;

import LLD.ParkingLot.implementation.exception.PaymentFailedException;
import LLD.ParkingLot.implementation.manager.ParkingSpotManager;
import LLD.ParkingLot.implementation.manager.ParkingSpotManagerFactory;
import LLD.ParkingLot.implementation.model.ExitReceipt;
import LLD.ParkingLot.implementation.model.Payment;
import LLD.ParkingLot.implementation.model.PaymentMethod;
import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.model.TicketStatus;
import LLD.ParkingLot.implementation.payment.PaymentProcessor;
import LLD.ParkingLot.implementation.payment.PaymentProcessorFactory;
import LLD.ParkingLot.implementation.pricing.CostComputation;
import LLD.ParkingLot.implementation.pricing.CostComputationFactory;
import LLD.ParkingLot.implementation.service.TicketService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class ExitGate {

    private final String id;
    private final TicketService ticketService;
    private final ParkingSpotManagerFactory managerFactory;
    private final CostComputationFactory costComputationFactory;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final Clock clock;

    public ExitGate(
            String id,
            TicketService ticketService,
            ParkingSpotManagerFactory managerFactory,
            CostComputationFactory costComputationFactory,
            PaymentProcessorFactory paymentProcessorFactory,
            Clock clock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Exit gate ID cannot be blank");
        }
        this.id = id;
        this.ticketService = Objects.requireNonNull(ticketService, "Ticket service cannot be null");
        this.managerFactory = Objects.requireNonNull(managerFactory, "Manager factory cannot be null");
        this.costComputationFactory = Objects.requireNonNull(
                costComputationFactory, "Cost computation factory cannot be null");
        this.paymentProcessorFactory = Objects.requireNonNull(
                paymentProcessorFactory, "Payment processor factory cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
    }

    public ExitReceipt checkout(String ticketId, PaymentMethod paymentMethod) {
        Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        Ticket ticket = ticketService.getTicket(ticketId);

        synchronized (ticket) {
            if (ticket.getStatus() == TicketStatus.ACTIVE) {
                Instant exitTime = Instant.now(clock);
                CostComputation computation = costComputationFactory
                        .getComputation(ticket.getVehicle().type());
                BigDecimal amount = computation.calculate(ticket, exitTime);
                ticket.beginPayment(exitTime, amount);
            }

            if (ticket.getStatus() == TicketStatus.PAYMENT_PENDING) {
                processPayment(ticket, paymentMethod);
            }

            if (ticket.getStatus() == TicketStatus.PAID) {
                ParkingSpotManager manager = managerFactory
                        .getManager(ticket.getParkingSpot().getType());
                if (!ticket.getParkingSpot().isAvailable()) {
                    manager.releaseSpot(
                            ticket.getParkingSpot(), ticket.getVehicle().registrationNumber());
                }
                ticketService.markClosed(ticket);
            }

            return createReceipt(ticket);
        }
    }

    private void processPayment(Ticket ticket, PaymentMethod paymentMethod) {
        PaymentProcessor processor = paymentProcessorFactory.getProcessor(paymentMethod);
        try {
            Payment payment = processor.process(ticket.getId(), ticket.getAmount(), clock);
            ticket.recordSuccessfulPayment(payment);
        } catch (PaymentFailedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new PaymentFailedException("Payment failed for ticket " + ticket.getId(), exception);
        }
    }

    private ExitReceipt createReceipt(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.CLOSED) {
            throw new IllegalStateException("Receipt is available only for a closed ticket");
        }
        return new ExitReceipt(
                ticket.getId(),
                id,
                ticket.getVehicle().registrationNumber(),
                ticket.getParkingSpot().getId(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                Duration.between(ticket.getEntryTime(), ticket.getExitTime()),
                ticket.getAmount(),
                ticket.getPayment());
    }

    public String getId() {
        return id;
    }
}