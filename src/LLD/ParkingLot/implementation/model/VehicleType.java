package LLD.ParkingLot.implementation.model;

public enum VehicleType {
    TWO_WHEELER(ParkingSpotType.TWO_WHEELER),
    FOUR_WHEELER(ParkingSpotType.FOUR_WHEELER);

    private final ParkingSpotType requiredSpotType;

    VehicleType(ParkingSpotType requiredSpotType) {
        this.requiredSpotType = requiredSpotType;
    }

    public ParkingSpotType getRequiredSpotType() {
        return requiredSpotType;
    }
}