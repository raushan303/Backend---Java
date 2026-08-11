package LLD.ParkingLot.implementation.manager;

import LLD.ParkingLot.implementation.exception.NoParkingSpotAvailableException;
import LLD.ParkingLot.implementation.exception.ParkingSpotOperationException;
import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.model.Vehicle;
import LLD.ParkingLot.implementation.strategy.parking.ParkingAllocationStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class ParkingSpotManager {

    private final ParkingSpotType managedType;
    private final List<ParkingSpot> spots;
    private final ParkingAllocationStrategy allocationStrategy;

    protected ParkingSpotManager(
            ParkingSpotType managedType,
            Collection<? extends ParkingSpot> spots,
            ParkingAllocationStrategy allocationStrategy) {
        this.managedType = Objects.requireNonNull(managedType, "Managed spot type cannot be null");
        this.allocationStrategy = Objects.requireNonNull(
                allocationStrategy, "Parking allocation strategy cannot be null");
        this.spots = new ArrayList<>();
        spots.forEach(this::validateAndAdd);
    }

    public synchronized ParkingSpot allocateSpot(Vehicle vehicle, String entranceId) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        if (vehicle.type().getRequiredSpotType() != managedType) {
            throw new ParkingSpotOperationException(
                    "Manager for " + managedType + " cannot park " + vehicle.type());
        }

        ParkingSpot spot = allocationStrategy.selectSpot(spots, entranceId)
                .orElseThrow(() -> new NoParkingSpotAvailableException(
                        "No " + managedType + " parking spot is available"));
        spot.park(vehicle);
        return spot;
    }

    public synchronized void releaseSpot(ParkingSpot spot, String registrationNumber) {
        if (!spots.contains(spot)) {
            throw new ParkingSpotOperationException(
                    "Parking spot " + spot.getId() + " is not managed by this manager");
        }
        spot.vacate(registrationNumber);
    }

    public synchronized void addSpot(ParkingSpot spot) {
        validateAndAdd(spot);
    }

    public synchronized void removeSpot(String spotId) {
        ParkingSpot spot = spots.stream()
                .filter(candidate -> candidate.getId().equals(spotId))
                .findFirst()
                .orElseThrow(() -> new ParkingSpotOperationException("Unknown parking spot " + spotId));
        if (!spot.isAvailable()) {
            throw new ParkingSpotOperationException("Cannot remove occupied parking spot " + spotId);
        }
        spots.remove(spot);
    }

    public synchronized long availableSpotCount() {
        return spots.stream().filter(ParkingSpot::isAvailable).count();
    }

    public ParkingSpotType getManagedType() {
        return managedType;
    }

    private void validateAndAdd(ParkingSpot spot) {
        Objects.requireNonNull(spot, "Parking spot cannot be null");
        if (spot.getType() != managedType) {
            throw new IllegalArgumentException("Manager cannot contain spot type " + spot.getType());
        }
        boolean duplicateId = spots.stream().anyMatch(existing -> existing.getId().equals(spot.getId()));
        if (duplicateId) {
            throw new IllegalArgumentException("Duplicate parking spot ID " + spot.getId());
        }
        spots.add(spot);
    }
}