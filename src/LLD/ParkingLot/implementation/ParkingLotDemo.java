package LLD.ParkingLot.implementation;

import LLD.ParkingLot.implementation.exception.NoParkingSpotAvailableException;
import LLD.ParkingLot.implementation.model.ExitReceipt;
import LLD.ParkingLot.implementation.model.FourWheelerParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.model.PaymentMethod;
import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.model.TwoWheelerParkingSpot;
import LLD.ParkingLot.implementation.model.Vehicle;
import LLD.ParkingLot.implementation.model.VehicleType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class ParkingLotDemo {

    private ParkingLotDemo() {
    }

    public static void main(String[] args) {
        List<ParkingSpot> spots = List.of(
                new TwoWheelerParkingSpot("T-01", new BigDecimal("30.00"),
                        Map.of("ENTRY-A", 5, "ENTRY-B", 40)),
                new TwoWheelerParkingSpot("T-02", new BigDecimal("30.00"),
                        Map.of("ENTRY-A", 25, "ENTRY-B", 6)),
                new FourWheelerParkingSpot("F-01", new BigDecimal("120.00"),
                        Map.of("ENTRY-A", 8, "ENTRY-B", 30)));

        ParkingLot parkingLot = new ParkingLot(
                spots, List.of("ENTRY-A", "ENTRY-B"), List.of("EXIT-A", "EXIT-B"));

        Ticket bikeTicket = parkingLot.enter(
                "ENTRY-B", new Vehicle("BIKE-101", VehicleType.TWO_WHEELER));
        Ticket carTicket = parkingLot.enter(
                "ENTRY-A", new Vehicle("CAR-202", VehicleType.FOUR_WHEELER));

        System.out.println("Bike assigned to nearest spot: " + bikeTicket.getParkingSpot().getId());
        System.out.println("Car assigned to: " + carTicket.getParkingSpot().getId());
        System.out.println("Available two-wheeler spots: "
                + parkingLot.availableSpots(ParkingSpotType.TWO_WHEELER));

        ExitReceipt bikeReceipt = parkingLot.exit("EXIT-B", bikeTicket.getId(), PaymentMethod.UPI);
        System.out.println("Bike paid: " + bikeReceipt.amountPaid()
                + " via " + bikeReceipt.payment().method());

        try {
            parkingLot.enter("ENTRY-A", new Vehicle("CAR-303", VehicleType.FOUR_WHEELER));
        } catch (NoParkingSpotAvailableException exception) {
            System.out.println("Expected full-lot response: " + exception.getMessage());
        }

        ExitReceipt carReceipt = parkingLot.exit("EXIT-A", carTicket.getId(), PaymentMethod.CARD);
        System.out.println("Car exited from " + carReceipt.parkingSpotId()
                + "; available car spots: "
                + parkingLot.availableSpots(ParkingSpotType.FOUR_WHEELER));
    }
}