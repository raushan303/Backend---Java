package LLD.ParkingLot.implementation.model;

import java.math.BigDecimal;
import java.util.Map;

public final class TwoWheelerParkingSpot extends ParkingSpot {

    public TwoWheelerParkingSpot(
            String id, BigDecimal hourlyRate, Map<String, Integer> distanceByEntrance) {
        super(id, ParkingSpotType.TWO_WHEELER, hourlyRate, distanceByEntrance);
    }
}