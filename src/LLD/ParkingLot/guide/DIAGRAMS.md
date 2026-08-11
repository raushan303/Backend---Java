# Parking Lot LLD Diagrams

These diagrams match the runnable implementation. The final section labels future extensions separately so they are not confused with implemented behavior.

## 1. System Context

```mermaid
flowchart LR
    Driver[Driver / Vehicle]
    Entry[Entrance Gate]
    Lot[Parking Lot]
    Exit[Exit Gate]
    Pay[Payment Processor]

    Driver -->|vehicle details| Entry
    Entry -->|admit| Lot
    Lot -->|ticket and assigned spot| Driver
    Driver -->|ticket and method| Exit
    Exit -->|calculate and charge| Pay
    Pay -->|payment result| Exit
    Exit -->|receipt| Driver
```

## 2. Core Class Diagram

```mermaid
classDiagram
    direction LR

    class ParkingLot {
        -Map~String, EntranceGate~ entranceGates
        -Map~String, ExitGate~ exitGates
        -ParkingSpotManagerFactory managerFactory
        +enter(entranceGateId, vehicle) Ticket
        +exit(exitGateId, ticketId, method) ExitReceipt
        +availableSpots(spotType) long
    }

    class Vehicle {
        <<record>>
        +String registrationNumber
        +VehicleType type
    }

    class VehicleType {
        <<enumeration>>
        TWO_WHEELER
        FOUR_WHEELER
        +getRequiredSpotType() ParkingSpotType
    }

    class ParkingSpotType {
        <<enumeration>>
        TWO_WHEELER
        FOUR_WHEELER
    }

    class ParkingSpot {
        <<abstract>>
        -String id
        -ParkingSpotType type
        -BigDecimal hourlyRate
        -Map~String, Integer~ distanceByEntrance
        -Vehicle parkedVehicle
        +park(vehicle)
        +vacate(registrationNumber) Vehicle
        +isAvailable() boolean
        +isCompatible(vehicle) boolean
        +distanceFrom(entranceId) int
    }

    class TwoWheelerParkingSpot
    class FourWheelerParkingSpot

    class ParkingSpotManager {
        <<abstract>>
        -ParkingSpotType managedType
        -List~ParkingSpot~ spots
        -ParkingAllocationStrategy allocationStrategy
        +allocateSpot(vehicle, entranceId) ParkingSpot
        +releaseSpot(spot, registrationNumber)
        +addSpot(spot)
        +removeSpot(spotId)
        +availableSpotCount() long
    }

    class TwoWheelerParkingSpotManager
    class FourWheelerParkingSpotManager

    class ParkingSpotManagerFactory {
        -Map~ParkingSpotType, ParkingSpotManager~ managers
        +getManager(vehicleType) ParkingSpotManager
        +getManager(spotType) ParkingSpotManager
    }

    class ParkingAllocationStrategy {
        <<interface>>
        +selectSpot(spots, entranceId) Optional~ParkingSpot~
    }

    class NearestToEntranceStrategy {
        +selectSpot(spots, entranceId) Optional~ParkingSpot~
    }

    class EntranceGate {
        -String id
        -ParkingSpotManagerFactory managerFactory
        -TicketService ticketService
        -Clock clock
        +admit(vehicle) Ticket
    }

    class ExitGate {
        -String id
        -TicketService ticketService
        -ParkingSpotManagerFactory managerFactory
        -CostComputationFactory costComputationFactory
        -PaymentProcessorFactory paymentProcessorFactory
        -Clock clock
        +checkout(ticketId, paymentMethod) ExitReceipt
    }

    class Ticket {
        -String id
        -String entranceGateId
        -Vehicle vehicle
        -ParkingSpot parkingSpot
        -Instant entryTime
        -TicketStatus status
        -Instant exitTime
        -BigDecimal amount
        -Payment payment
        +beginPayment(exitTime, amount)
        +recordSuccessfulPayment(payment)
        +close()
    }

    class TicketStatus {
        <<enumeration>>
        ACTIVE
        PAYMENT_PENDING
        PAID
        CLOSED
    }

    class TicketService {
        -Map~String, Ticket~ ticketsById
        -Map~String, String~ openTicketByRegistration
        +issueTicket(gateId, vehicle, spot, clock) Ticket
        +getTicket(ticketId) Ticket
        +markClosed(ticket)
        +cancelIssuedTicket(ticket)
    }

    class CostComputation {
        <<abstract>>
        -PricingStrategy pricingStrategy
        +calculate(ticket, exitTime) BigDecimal
    }

    class TwoWheelerCostComputation
    class FourWheelerCostComputation
    class CostComputationFactory {
        +getComputation(vehicleType) CostComputation
    }

    class PricingStrategy {
        <<interface>>
        +calculate(entry, exit, hourlyRate) BigDecimal
    }
    class HourlyPricingStrategy
    class PerMinutePricingStrategy

    class PaymentProcessor {
        <<interface>>
        +process(ticketId, amount, clock) Payment
    }
    class CashPaymentProcessor
    class CardPaymentProcessor
    class UpiPaymentProcessor
    class PaymentProcessorFactory {
        +getProcessor(method) PaymentProcessor
    }

    class Payment {
        <<record>>
        +String id
        +String ticketId
        +BigDecimal amount
        +PaymentMethod method
        +PaymentStatus status
        +Instant paidAt
    }

    class ExitReceipt {
        <<record>>
        +String ticketId
        +String exitGateId
        +Duration parkedDuration
        +BigDecimal amountPaid
        +Payment payment
    }

    ParkingLot *-- EntranceGate
    ParkingLot *-- ExitGate
    ParkingLot *-- ParkingSpotManagerFactory

    Vehicle --> VehicleType
    VehicleType --> ParkingSpotType
    ParkingSpot --> ParkingSpotType
    ParkingSpot o-- Vehicle : occupied by
    ParkingSpot <|-- TwoWheelerParkingSpot
    ParkingSpot <|-- FourWheelerParkingSpot

    ParkingSpotManager o-- ParkingSpot
    ParkingSpotManager --> ParkingAllocationStrategy
    ParkingSpotManager <|-- TwoWheelerParkingSpotManager
    ParkingSpotManager <|-- FourWheelerParkingSpotManager
    ParkingAllocationStrategy <|.. NearestToEntranceStrategy
    ParkingSpotManagerFactory o-- ParkingSpotManager

    EntranceGate --> ParkingSpotManagerFactory
    EntranceGate --> TicketService
    ExitGate --> TicketService
    ExitGate --> ParkingSpotManagerFactory
    ExitGate --> CostComputationFactory
    ExitGate --> PaymentProcessorFactory

    Ticket --> Vehicle
    Ticket --> ParkingSpot
    Ticket --> TicketStatus
    Ticket --> Payment
    TicketService o-- Ticket

    CostComputation --> PricingStrategy
    CostComputation <|-- TwoWheelerCostComputation
    CostComputation <|-- FourWheelerCostComputation
    PricingStrategy <|.. HourlyPricingStrategy
    PricingStrategy <|.. PerMinutePricingStrategy
    CostComputationFactory o-- CostComputation

    PaymentProcessor <|.. CashPaymentProcessor
    PaymentProcessor <|.. CardPaymentProcessor
    PaymentProcessor <|.. UpiPaymentProcessor
    PaymentProcessorFactory o-- PaymentProcessor
    ExitGate --> ExitReceipt
```

## 3. Entry Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Driver
    participant Lot as ParkingLot
    participant Gate as EntranceGate
    participant Factory as ManagerFactory
    participant Manager as ParkingSpotManager
    participant Strategy as AllocationStrategy
    participant Spot as ParkingSpot
    participant Tickets as TicketService

    Driver->>Lot: enter(entranceId, vehicle)
    Lot->>Gate: admit(vehicle)
    Gate->>Factory: getManager(vehicle.type)
    Factory-->>Gate: shared manager
    Gate->>Manager: allocateSpot(vehicle, entranceId)
    Note over Manager: synchronized selection + occupation
    Manager->>Strategy: selectSpot(spots, entranceId)
    Strategy-->>Manager: nearest available spot
    Manager->>Spot: park(vehicle)
    Spot-->>Manager: occupied
    Manager-->>Gate: spot
    Gate->>Tickets: issueTicket(gateId, vehicle, spot, clock)

    alt ticket issued
        Tickets-->>Gate: ACTIVE ticket
        Gate-->>Lot: ticket
        Lot-->>Driver: ticket + spot ID
    else ticket registration fails
        Gate->>Manager: releaseSpot(spot, registration)
        Gate-->>Driver: admission failure
    end
```

## 4. No-Capacity Entry

```mermaid
sequenceDiagram
    actor Driver
    participant Gate as EntranceGate
    participant Manager as ParkingSpotManager
    participant Strategy as AllocationStrategy

    Driver->>Gate: admit(vehicle)
    Gate->>Manager: allocateSpot(vehicle, entranceId)
    Manager->>Strategy: selectSpot(spots, entranceId)
    Strategy-->>Manager: Optional.empty
    Manager-->>Gate: NoParkingSpotAvailableException
    Gate-->>Driver: entry rejected; no ticket created
```

## 5. Successful Exit Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Driver
    participant Lot as ParkingLot
    participant Gate as ExitGate
    participant Tickets as TicketService
    participant CostFactory as CostComputationFactory
    participant Cost as CostComputation
    participant PaymentFactory as PaymentProcessorFactory
    participant Processor as PaymentProcessor
    participant Manager as ParkingSpotManager
    participant Spot as ParkingSpot

    Driver->>Lot: exit(exitId, ticketId, method)
    Lot->>Gate: checkout(ticketId, method)
    Gate->>Tickets: getTicket(ticketId)
    Tickets-->>Gate: ACTIVE ticket
    Note over Gate: synchronize on ticket
    Gate->>CostFactory: getComputation(vehicle.type)
    CostFactory-->>Gate: cost computation
    Gate->>Cost: calculate(ticket, capturedExitTime)
    Cost-->>Gate: exact amount
    Gate->>Gate: ticket.beginPayment(exitTime, amount)
    Gate->>PaymentFactory: getProcessor(method)
    PaymentFactory-->>Gate: processor
    Gate->>Processor: process(ticketId, amount, clock)
    Processor-->>Gate: successful payment
    Gate->>Gate: ticket.recordSuccessfulPayment(payment)
    Gate->>Manager: releaseSpot(spot, registration)
    Manager->>Spot: vacate(registration)
    Gate->>Tickets: markClosed(ticket)
    Gate-->>Lot: ExitReceipt
    Lot-->>Driver: receipt
```

## 6. Payment Failure and Retry

```mermaid
sequenceDiagram
    autonumber
    actor Driver
    participant Gate as ExitGate
    participant Ticket
    participant Processor as PaymentProcessor
    participant Manager as ParkingSpotManager

    Driver->>Gate: checkout(ticketId, CARD)
    Gate->>Ticket: beginPayment(exitTime, amount)
    Ticket-->>Gate: PAYMENT_PENDING
    Gate->>Processor: process(ticketId, amount)
    Processor-->>Gate: failure / timeout
    Gate-->>Driver: PaymentFailedException
    Note over Ticket,Manager: amount and exit time retained; spot remains occupied

    Driver->>Gate: checkout(ticketId, UPI)
    Note over Gate: sees PAYMENT_PENDING; does not recalculate
    Gate->>Processor: process(ticketId, same amount)
    Processor-->>Gate: successful payment
    Gate->>Ticket: recordSuccessfulPayment(payment)
    Gate->>Manager: releaseSpot(spot, registration)
    Gate->>Ticket: close()
    Gate-->>Driver: receipt
```

## 7. Retry After Payment but Before Release

```mermaid
sequenceDiagram
    actor Client
    participant Gate as ExitGate
    participant Ticket
    participant Processor as PaymentProcessor
    participant Manager as ParkingSpotManager

    Client->>Gate: checkout(ticketId, CARD)
    Gate->>Processor: charge once
    Processor-->>Gate: success
    Gate->>Ticket: status = PAID
    Gate-xManager: release fails / process stops

    Client->>Gate: retry checkout(ticketId, CARD)
    Note over Gate,Ticket: status is PAID, so payment is skipped
    Gate->>Manager: releaseSpot(...)
    Gate->>Ticket: close()
    Gate-->>Client: original payment in receipt
```

## 8. Ticket State Diagram

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: ticket issued after spot occupation
    ACTIVE --> PAYMENT_PENDING: capture exit time and amount
    PAYMENT_PENDING --> PAYMENT_PENDING: payment failed; retry allowed
    PAYMENT_PENDING --> PAID: successful matching payment recorded
    PAID --> PAID: release failed; retry cleanup
    PAID --> CLOSED: assigned spot released
    CLOSED --> CLOSED: duplicate checkout returns existing receipt
    CLOSED --> [*]
```

Illegal transitions to mention:

- `ACTIVE -> CLOSED`: unpaid exit.
- `PAYMENT_PENDING -> CLOSED`: payment not confirmed.
- `PAID -> PAYMENT_PENDING`: duplicate charge risk.
- Replacing the amount or exit time during a retry.

## 9. Atomic Allocation Race

```mermaid
sequenceDiagram
    participant A as Entrance Thread A
    participant Manager as Spot Manager Lock
    participant Spot as Only Available Spot
    participant B as Entrance Thread B

    A->>Manager: allocateSpot(vehicle A)
    activate Manager
    Manager->>Spot: check available + park A
    Spot-->>Manager: occupied by A
    Manager-->>A: spot
    deactivate Manager

    B->>Manager: allocateSpot(vehicle B)
    activate Manager
    Manager->>Spot: check availability
    Spot-->>Manager: occupied
    Manager-->>B: NoParkingSpotAvailableException
    deactivate Manager
```

The lock covers selection and occupation. Locking only the spot does not make a separate `find` operation atomic.

## 10. Future Multi-Floor Extension

This is not part of the current implementation.

```mermaid
classDiagram
    class ParkingLot {
        -List~ParkingFloor~ floors
        -FloorSelectionStrategy floorStrategy
    }
    class ParkingFloor {
        -String id
        -Map~ParkingSpotType, ParkingSpotManager~ managers
        -DisplayBoard displayBoard
    }
    class FloorSelectionStrategy {
        <<interface>>
        +selectFloor(vehicleType, entranceId) ParkingFloor
    }
    class NearestFloorWithCapacityStrategy
    class DisplayBoard {
        +updateAvailability(type, count)
    }

    ParkingLot *-- ParkingFloor
    ParkingLot --> FloorSelectionStrategy
    FloorSelectionStrategy <|.. NearestFloorWithCapacityStrategy
    ParkingFloor *-- ParkingSpotManager
    ParkingFloor *-- DisplayBoard
```

## 11. Production Persistence Boundary

```mermaid
flowchart TB
    API[REST / Gate Adapter]
    App[Parking Application Service]
    Domain[Domain Model and Policies]
    SpotRepo[ParkingSpotRepository]
    TicketRepo[TicketRepository]
    PaymentRepo[PaymentRepository]
    Gateway[External Payment Gateway]
    DB[(Transactional Database)]

    API --> App
    App --> Domain
    App --> SpotRepo
    App --> TicketRepo
    App --> PaymentRepo
    App --> Gateway
    SpotRepo --> DB
    TicketRepo --> DB
    PaymentRepo --> DB
```

In production, application services own transaction boundaries. Domain entities should not execute SQL or call HTTP payment APIs directly.