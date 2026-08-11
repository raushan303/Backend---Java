package LLD.ParkingLot.implementation.model;

import LLD.ParkingLot.implementation.exception.ParkingSpotOperationException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public abstract class ParkingSpot {

    private final String id;
    private final ParkingSpotType type;
    private final BigDecimal hourlyRate;
    private final Map<String, Integer> distanceByEntrance;
    private Vehicle parkedVehicle;

    protected ParkingSpot(
            String id,
            ParkingSpotType type,
            BigDecimal hourlyRate,
            Map<String, Integer> distanceByEntrance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Parking spot ID cannot be blank");
        }
        if (hourlyRate == null || hourlyRate.signum() < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        this.id = id;
        this.type = Objects.requireNonNull(type, "Parking spot type cannot be null");
        this.hourlyRate = hourlyRate;
        this.distanceByEntrance = Map.copyOf(
                Objects.requireNonNull(distanceByEntrance, "Entrance distances cannot be null"));
    }

    public synchronized void park(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        if (!isCompatible(vehicle)) {
            throw new ParkingSpotOperationException(
                    "Vehicle " + vehicle.registrationNumber() + " is incompatible with spot " + id);
        }
        if (parkedVehicle != null) {
            throw new ParkingSpotOperationException("Parking spot " + id + " is already occupied");
        }
        parkedVehicle = vehicle;
    }

    public synchronized Vehicle vacate(String expectedRegistrationNumber) {
        if (parkedVehicle == null) {
            throw new ParkingSpotOperationException("Parking spot " + id + " is already empty");
        }
        if (!parkedVehicle.registrationNumber().equals(expectedRegistrationNumber)) {
            throw new ParkingSpotOperationException(
                    "Vehicle in spot " + id + " does not match ticket vehicle");
        }
        Vehicle removedVehicle = parkedVehicle;
        parkedVehicle = null;
        return removedVehicle;
    }

    public synchronized boolean isAvailable() {
        return parkedVehicle == null;
    }

    public synchronized Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public boolean isCompatible(Vehicle vehicle) {
        return vehicle.type().getRequiredSpotType() == type;
    }

    public int distanceFrom(String entranceId) {
        Integer distance = distanceByEntrance.get(entranceId);
        if (distance == null) {
            throw new IllegalArgumentException(
                    "No distance configured from entrance " + entranceId + " to spot " + id);
        }
        return distance;
    }

    public String getId() {
        return id;
    }

    public ParkingSpotType getType() {
        return type;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
}