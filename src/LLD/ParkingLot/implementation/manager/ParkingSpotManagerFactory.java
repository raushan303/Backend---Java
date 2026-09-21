package LLD.ParkingLot.implementation.manager;

import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.model.VehicleType;
import LLD.ParkingLot.implementation.strategy.parking.NearestToEntranceStrategy;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ParkingSpotManagerFactory {

    private final Map<ParkingSpotType, ParkingSpotManager> managers;

    public ParkingSpotManagerFactory(Collection<? extends ParkingSpot> parkingSpots) {
        this(defaultManagers(parkingSpots));
    }

    public ParkingSpotManagerFactory(Iterable<? extends ParkingSpotManager> managers) {
        this.managers = new EnumMap<>(ParkingSpotType.class);
        for (ParkingSpotManager manager : managers) {
            ParkingSpotManager previous = this.managers.put(manager.getManagedType(), manager);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "Duplicate manager for spot type " + manager.getManagedType());
            }
        }
    }

    public ParkingSpotManager getManager(VehicleType vehicleType) {
        return getManager(vehicleType.getRequiredSpotType());
    }

    public ParkingSpotManager getManager(ParkingSpotType spotType) {
        ParkingSpotManager manager = managers.get(spotType);
        if (manager == null) {
            throw new IllegalArgumentException("No manager registered for spot type " + spotType);
        }
        return manager;
    }

    private static List<ParkingSpot> spotsOfType(
            Collection<? extends ParkingSpot> parkingSpots, ParkingSpotType type) {
        return parkingSpots.stream()
                .filter(spot -> spot.getType() == type)
                .map(spot -> (ParkingSpot) spot)
                .toList();
    }

    private static List<ParkingSpotManager> defaultManagers(
        Collection<? extends ParkingSpot> parkingSpots) {

        NearestToEntranceStrategy allocationStrategy = new NearestToEntranceStrategy();
        return List.of(
            new TwoWheelerParkingSpotManager(
                spotsOfType(parkingSpots, ParkingSpotType.TWO_WHEELER), allocationStrategy),
            new FourWheelerParkingSpotManager(
                spotsOfType(parkingSpots, ParkingSpotType.FOUR_WHEELER), allocationStrategy));
    }
}