package LLD.ParkingLot.implementation.strategy.parking;

import LLD.ParkingLot.implementation.model.ParkingSpot;

import java.util.Collection;
import java.util.Optional;

public interface ParkingAllocationStrategy {

    Optional<ParkingSpot> selectSpot(Collection<ParkingSpot> spots, String entranceId);
}