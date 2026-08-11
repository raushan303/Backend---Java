package LLD.ParkingLot.implementation.gate;

import LLD.ParkingLot.implementation.manager.ParkingSpotManager;
import LLD.ParkingLot.implementation.manager.ParkingSpotManagerFactory;
import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.model.Vehicle;
import LLD.ParkingLot.implementation.service.TicketService;

import java.time.Clock;
import java.util.Objects;

public final class EntranceGate {

    private final String id;
    private final ParkingSpotManagerFactory managerFactory;
    private final TicketService ticketService;
    private final Clock clock;

    public EntranceGate(
            String id,
            ParkingSpotManagerFactory managerFactory,
            TicketService ticketService,
            Clock clock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Entrance gate ID cannot be blank");
        }
        this.id = id;
        this.managerFactory = Objects.requireNonNull(managerFactory, "Manager factory cannot be null");
        this.ticketService = Objects.requireNonNull(ticketService, "Ticket service cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
    }

    public Ticket admit(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        ParkingSpotManager manager = managerFactory.getManager(vehicle.type());
        ParkingSpot spot = manager.allocateSpot(vehicle, id);
        try {
            return ticketService.issueTicket(id, vehicle, spot, clock);
        } catch (RuntimeException exception) {
            manager.releaseSpot(spot, vehicle.registrationNumber());
            throw exception;
        }
    }

    public String getId() {
        return id;
    }
}