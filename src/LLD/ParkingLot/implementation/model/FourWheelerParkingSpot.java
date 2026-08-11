package LLD.ParkingLot.implementation.model;

import java.math.BigDecimal;
import java.util.Map;

public final class FourWheelerParkingSpot extends ParkingSpot {

    public FourWheelerParkingSpot(
            String id, BigDecimal hourlyRate, Map<String, Integer> distanceByEntrance) {
        super(id, ParkingSpotType.FOUR_WHEELER, hourlyRate, distanceByEntrance);
    }
}