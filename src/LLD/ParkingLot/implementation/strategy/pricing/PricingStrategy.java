package LLD.ParkingLot.implementation.strategy.pricing;

import java.math.BigDecimal;
import java.time.Instant;

public interface PricingStrategy {

    BigDecimal calculate(Instant entryTime, Instant exitTime, BigDecimal hourlyRate);
}