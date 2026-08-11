package LLD.ParkingLot.implementation.service;

import LLD.ParkingLot.implementation.exception.InvalidParkingTicketException;
import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.model.TicketStatus;
import LLD.ParkingLot.implementation.model.Vehicle;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TicketService {

    private final Map<String, Ticket> ticketsById = new HashMap<>();
    private final Map<String, String> openTicketByRegistration = new HashMap<>();

    public synchronized Ticket issueTicket(
            String entranceGateId, Vehicle vehicle, ParkingSpot parkingSpot, Clock clock) {
        if (openTicketByRegistration.containsKey(vehicle.registrationNumber())) {
            throw new InvalidParkingTicketException(
                    "Vehicle " + vehicle.registrationNumber() + " already has an open ticket");
        }
        Ticket ticket = new Ticket(
                UUID.randomUUID().toString(),
                entranceGateId,
                vehicle,
                parkingSpot,
                Instant.now(clock));
        ticketsById.put(ticket.getId(), ticket);
        openTicketByRegistration.put(vehicle.registrationNumber(), ticket.getId());
        return ticket;
    }

    public synchronized Ticket getTicket(String ticketId) {
        Ticket ticket = ticketsById.get(ticketId);
        if (ticket == null) {
            throw new InvalidParkingTicketException("Unknown parking ticket " + ticketId);
        }
        return ticket;
    }

    public synchronized void markClosed(Ticket ticket) {
        Ticket storedTicket = ticketsById.get(ticket.getId());
        if (storedTicket != ticket) {
            throw new InvalidParkingTicketException("Ticket is not managed by this parking lot");
        }
        ticket.close();
        openTicketByRegistration.remove(ticket.getVehicle().registrationNumber(), ticket.getId());
    }

    public synchronized void cancelIssuedTicket(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new InvalidParkingTicketException("Only an active ticket can be cancelled");
        }
        ticketsById.remove(ticket.getId(), ticket);
        openTicketByRegistration.remove(ticket.getVehicle().registrationNumber(), ticket.getId());
    }
}