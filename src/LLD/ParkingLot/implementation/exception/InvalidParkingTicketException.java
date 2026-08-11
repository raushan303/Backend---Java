package LLD.ParkingLot.implementation.exception;

public class InvalidParkingTicketException extends RuntimeException {

    public InvalidParkingTicketException(String message) {
        super(message);
    }
}