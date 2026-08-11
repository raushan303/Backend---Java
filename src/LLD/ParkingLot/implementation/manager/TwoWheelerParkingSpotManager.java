package LLD.ParkingLot.implementation.manager;

import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.strategy.parking.ParkingAllocationStrategy;

import java.util.Collection;

public final class TwoWheelerParkingSpotManager extends ParkingSpotManager {

    public TwoWheelerParkingSpotManager(
            Collection<? extends ParkingSpot> spots,
            ParkingAllocationStrategy allocationStrategy) {
        super(ParkingSpotType.TWO_WHEELER, spots, allocationStrategy);
    }
}