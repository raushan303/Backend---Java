package LLD.ParkingLot.implementation.strategy.pricing;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public final class HourlyPricingStrategy implements PricingStrategy {

    private static final long MILLIS_PER_HOUR = Duration.ofHours(1).toMillis();

    @Override
    public BigDecimal calculate(Instant entryTime, Instant exitTime, BigDecimal hourlyRate) {
        long durationMillis = validatedDurationMillis(entryTime, exitTime);
        long billableHours = Math.max(1, divideRoundingUp(durationMillis, MILLIS_PER_HOUR));
        return hourlyRate.multiply(BigDecimal.valueOf(billableHours));
    }

    private long validatedDurationMillis(Instant entryTime, Instant exitTime) {
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        return Duration.between(entryTime, exitTime).toMillis();
    }

    private long divideRoundingUp(long value, long divisor) {
        return value / divisor + (value % divisor == 0 ? 0 : 1);
    }
}