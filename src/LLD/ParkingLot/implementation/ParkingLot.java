package LLD.ParkingLot.implementation;

import LLD.ParkingLot.implementation.gate.EntranceGate;
import LLD.ParkingLot.implementation.gate.ExitGate;
import LLD.ParkingLot.implementation.manager.FourWheelerParkingSpotManager;
import LLD.ParkingLot.implementation.manager.ParkingSpotManager;
import LLD.ParkingLot.implementation.manager.ParkingSpotManagerFactory;
import LLD.ParkingLot.implementation.manager.TwoWheelerParkingSpotManager;
import LLD.ParkingLot.implementation.model.ExitReceipt;
import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.model.PaymentMethod;
import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.model.Vehicle;
import LLD.ParkingLot.implementation.payment.PaymentProcessorFactory;
import LLD.ParkingLot.implementation.pricing.CostComputationFactory;
import LLD.ParkingLot.implementation.service.TicketService;
import LLD.ParkingLot.implementation.strategy.parking.NearestToEntranceStrategy;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ParkingLot {

    private final Map<String, EntranceGate> entranceGates;
    private final Map<String, ExitGate> exitGates;
    private final ParkingSpotManagerFactory managerFactory;

    public ParkingLot(
            Collection<? extends ParkingSpot> parkingSpots,
            Collection<String> entranceGateIds,
            Collection<String> exitGateIds) {
        this(parkingSpots, entranceGateIds, exitGateIds, Clock.systemUTC(),
                new PaymentProcessorFactory());
    }

    public ParkingLot(
            Collection<? extends ParkingSpot> parkingSpots,
            Collection<String> entranceGateIds,
            Collection<String> exitGateIds,
            Clock clock,
            PaymentProcessorFactory paymentProcessorFactory) {
        Objects.requireNonNull(parkingSpots, "Parking spots cannot be null");
        Objects.requireNonNull(clock, "Clock cannot be null");

        NearestToEntranceStrategy allocationStrategy = new NearestToEntranceStrategy();
        ParkingSpotManager twoWheelerManager = new TwoWheelerParkingSpotManager(
                spotsOfType(parkingSpots, ParkingSpotType.TWO_WHEELER), allocationStrategy);
        ParkingSpotManager fourWheelerManager = new FourWheelerParkingSpotManager(
                spotsOfType(parkingSpots, ParkingSpotType.FOUR_WHEELER), allocationStrategy);
        managerFactory = new ParkingSpotManagerFactory(List.of(twoWheelerManager, fourWheelerManager));

        TicketService ticketService = new TicketService();
        CostComputationFactory costComputationFactory = new CostComputationFactory();
        entranceGates = createEntranceGates(entranceGateIds, ticketService, clock);
        exitGates = createExitGates(
                exitGateIds, ticketService, costComputationFactory, paymentProcessorFactory, clock);
    }

    public Ticket enter(String entranceGateId, Vehicle vehicle) {
        return requireEntranceGate(entranceGateId).admit(vehicle);
    }

    public ExitReceipt exit(String exitGateId, String ticketId, PaymentMethod paymentMethod) {
        return requireExitGate(exitGateId).checkout(ticketId, paymentMethod);
    }

    public long availableSpots(ParkingSpotType spotType) {
        return managerFactory.getManager(spotType).availableSpotCount();
    }

    private List<ParkingSpot> spotsOfType(
            Collection<? extends ParkingSpot> parkingSpots, ParkingSpotType type) {
        return parkingSpots.stream().filter(spot -> spot.getType() == type).map(spot -> (ParkingSpot) spot).toList();
    }

    private Map<String, EntranceGate> createEntranceGates(
            Collection<String> gateIds, TicketService ticketService, Clock clock) {
        Map<String, EntranceGate> gates = new HashMap<>();
        for (String gateId : gateIds) {
            EntranceGate previous = gates.put(
                    gateId, new EntranceGate(gateId, managerFactory, ticketService, clock));
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate entrance gate ID " + gateId);
            }
        }
        return Map.copyOf(gates);
    }

    private Map<String, ExitGate> createExitGates(
            Collection<String> gateIds,
            TicketService ticketService,
            CostComputationFactory costComputationFactory,
            PaymentProcessorFactory paymentProcessorFactory,
            Clock clock) {
        Map<String, ExitGate> gates = new HashMap<>();
        for (String gateId : gateIds) {
            ExitGate previous = gates.put(gateId, new ExitGate(
                    gateId,
                    ticketService,
                    managerFactory,
                    costComputationFactory,
                    paymentProcessorFactory,
                    clock));
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate exit gate ID " + gateId);
            }
        }
        return Map.copyOf(gates);
    }

    private EntranceGate requireEntranceGate(String gateId) {
        EntranceGate gate = entranceGates.get(gateId);
        if (gate == null) {
            throw new IllegalArgumentException("Unknown entrance gate " + gateId);
        }
        return gate;
    }

    private ExitGate requireExitGate(String gateId) {
        ExitGate gate = exitGates.get(gateId);
        if (gate == null) {
            throw new IllegalArgumentException("Unknown exit gate " + gateId);
        }
        return gate;
    }
}