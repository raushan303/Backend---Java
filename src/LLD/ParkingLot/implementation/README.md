# Parking Lot - Full Java Implementation

This folder contains a runnable, dependency-free implementation of a single-floor parking lot. It supports multiple gates, two vehicle categories, nearest-spot allocation, duration-based pricing, multiple payment methods, ticket lifecycle management, and concurrent arrivals.

The implementation is intentionally richer than a whiteboard-only design while remaining small enough to explain in a 45-50 minute LLD interview.

## Run

From the repository root:

```bash
rm -rf out
mkdir -p out
javac -d out $(find src/LLD/ParkingLot/implementation -name '*.java')
java -cp out LLD.ParkingLot.implementation.ParkingLotDemo
java -cp out LLD.ParkingLot.implementation.ParkingLotScenarioTest
```

No Maven, Gradle, JUnit, database, or external library is required.

## Package Map

```text
implementation/
|-- ParkingLot.java                    Facade and composition root
|-- ParkingLotDemo.java                End-to-end example
|-- ParkingLotScenarioTest.java        Dependency-free scenario suite
|-- gate/
|   |-- EntranceGate.java              Atomic allocation + ticket issue
|   `-- ExitGate.java                  Price + payment + release + receipt
|-- manager/
|   |-- ParkingSpotManager.java        Owns and synchronizes spot inventory
|   |-- TwoWheelerParkingSpotManager.java
|   |-- FourWheelerParkingSpotManager.java
|   `-- ParkingSpotManagerFactory.java Returns shared configured managers
|-- model/
|   |-- Vehicle.java, VehicleType.java
|   |-- ParkingSpot.java and concrete spot classes
|   |-- Ticket.java, TicketStatus.java
|   |-- Payment.java and payment enums
|   `-- ExitReceipt.java
|-- strategy/
|   |-- parking/                        Spot-allocation Strategy pattern
|   `-- pricing/                        Pricing Strategy pattern
|-- pricing/                            Cost-computation hierarchy + factory
|-- payment/                            Payment processors + factory
|-- service/TicketService.java         Ticket registry and lifecycle owner
`-- exception/                          Domain-specific failures
```

## Implemented Requirements

- Two-wheeler and four-wheeler parking spots.
- Multiple entrance and exit gates.
- Nearest available compatible spot from the selected entrance.
- Unique tickets with entry time, vehicle, gate, and assigned spot.
- Hourly pricing for two-wheelers; partial hours round up.
- Per-minute pricing for four-wheelers; partial minutes round up.
- Cash, card, and UPI payment processors.
- Spot release only after successful payment.
- Retry-safe checkout: a paid ticket is not charged twice.
- Thread-safe allocation: selecting and occupying a spot is one operation.
- Exact decimal money using `BigDecimal`.
- Injectable `Clock` for deterministic tests.

## Important Invariants

1. A vehicle can park only in a compatible spot.
2. A spot has at most one vehicle.
3. One registration number has at most one open ticket.
4. Selecting and occupying a spot is atomic inside its manager.
5. A failed payment does not release the spot.
6. A ticket cannot close before successful payment.
7. Retrying checkout after payment reuses the recorded payment.
8. A manager factory returns shared managers; it never creates empty manager state per request.

## Patterns Used

### Strategy

`ParkingAllocationStrategy` isolates how a spot is selected. The current implementation uses `NearestToEntranceStrategy`.

`PricingStrategy` isolates duration and rounding policy. It has hourly and per-minute implementations.

### Factory

`ParkingSpotManagerFactory`, `CostComputationFactory`, and `PaymentProcessorFactory` centralize selection by type. The gates depend on abstractions and factories instead of concrete implementations.

### Facade / Composition Root

`ParkingLot` builds and owns the shared object graph, registers gates, and exposes a compact API. It is deliberately not a Singleton; an application may operate several parking lots or create isolated lots in tests.

## Deliberate Design Corrections

The source transcript describes `findParkingSpot()` followed by `parkVehicle()`. Exposing these as separate normal operations permits two threads to find the same free spot. This implementation exposes `allocateSpot()`, which performs selection and occupation under one manager lock.

The factories return configured shared managers. Creating a new manager at each gate would create a new list and lose the real occupancy state.

Payment is recorded before a spot is released. If release fails after payment, checkout can be retried without another charge because the ticket remains `PAID`.

## Scope Boundaries

The code models one floor. Floors, display boards, reservations, persistence, dynamic pricing, real payment gateways, and distributed locks are discussed in the interview guide as extensions rather than mixed into the core model.

See [../guide/README.md](../guide/README.md) for the complete interview preparation material.