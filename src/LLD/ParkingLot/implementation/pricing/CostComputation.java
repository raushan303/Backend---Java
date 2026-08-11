package LLD.ParkingLot.implementation.pricing;

import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.strategy.pricing.PricingStrategy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public abstract class CostComputation {

    private final PricingStrategy pricingStrategy;

    protected CostComputation(PricingStrategy pricingStrategy) {
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "Pricing strategy cannot be null");
    }

    public BigDecimal calculate(Ticket ticket, Instant exitTime) {
        return pricingStrategy.calculate(
                ticket.getEntryTime(), exitTime, ticket.getParkingSpot().getHourlyRate());
    }
}