package LLD.ParkingLot.implementation.model;

import java.util.Objects;

public record Vehicle(String registrationNumber, VehicleType type) {

    public Vehicle {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration number cannot be blank");
        }
        Objects.requireNonNull(type, "Vehicle type cannot be null");
    }
}