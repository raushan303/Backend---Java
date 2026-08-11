package LLD.ParkingLot.implementation.strategy.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public final class PerMinutePricingStrategy implements PricingStrategy {

    private static final long MILLIS_PER_MINUTE = Duration.ofMinutes(1).toMillis();

    @Override
    public BigDecimal calculate(Instant entryTime, Instant exitTime, BigDecimal hourlyRate) {
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        long durationMillis = Duration.between(entryTime, exitTime).toMillis();
        long billableMinutes = Math.max(1, divideRoundingUp(durationMillis, MILLIS_PER_MINUTE));
        BigDecimal minuteRate = hourlyRate.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return minuteRate.multiply(BigDecimal.valueOf(billableMinutes)).setScale(2, RoundingMode.HALF_UP);
    }

    private long divideRoundingUp(long value, long divisor) {
        return value / divisor + (value % divisor == 0 ? 0 : 1);
    }
}