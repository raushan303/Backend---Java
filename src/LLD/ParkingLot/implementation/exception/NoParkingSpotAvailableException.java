package LLD.ParkingLot.implementation.exception;

public class NoParkingSpotAvailableException extends RuntimeException {

    public NoParkingSpotAvailableException(String message) {
        super(message);
    }
}