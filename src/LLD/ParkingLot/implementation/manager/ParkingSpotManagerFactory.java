package LLD.ParkingLot.implementation.manager;

import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.model.VehicleType;

import java.util.EnumMap;
import java.util.Map;

public final class ParkingSpotManagerFactory {

    private final Map<ParkingSpotType, ParkingSpotManager> managers;

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
}