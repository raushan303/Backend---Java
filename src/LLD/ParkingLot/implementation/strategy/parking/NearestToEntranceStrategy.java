package LLD.ParkingLot.implementation.strategy.parking;

import LLD.ParkingLot.implementation.model.ParkingSpot;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class NearestToEntranceStrategy implements ParkingAllocationStrategy {

    @Override
    public Optional<ParkingSpot> selectSpot(Collection<ParkingSpot> spots, String entranceId) {
        return spots.stream()
                .filter(ParkingSpot::isAvailable)
                .min(Comparator.comparingInt((ParkingSpot spot) -> spot.distanceFrom(entranceId))
                        .thenComparing(ParkingSpot::getId));
    }
}