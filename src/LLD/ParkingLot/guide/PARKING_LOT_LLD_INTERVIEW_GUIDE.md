# Parking Lot LLD: 45-50 Minute Interview Guide

## 1. What the Interviewer Is Evaluating

This question is not mainly about writing twenty classes. The interviewer is evaluating whether you can:

- Turn an ambiguous statement into explicit requirements.
- Identify entities, responsibilities, relationships, and invariants.
- Keep core workflows correct before discussing extensions.
- Use patterns because variation exists, not merely to name patterns.
- Prevent double booking and invalid ticket/payment transitions.
- Explain tradeoffs and evolve the design without rewriting it.
- Communicate in a deliberate sequence while managing time.

The strongest answer starts simple, states assumptions, completes entry and exit, and then spends remaining time on concurrency and extensions.

## 2. Recommended Timeline

| Time | Activity | Output |
|---|---|---|
| 0-3 min | Restate problem and ask scope questions | Agreed functional boundary |
| 3-8 min | Gather functional and non-functional requirements | Prioritized requirements |
| 8-10 min | State assumptions and exclusions | Controlled interview scope |
| 10-14 min | Describe entry/exit use cases | End-to-end flow |
| 14-19 min | Identify entities and invariants | Core object model |
| 19-31 min | Draw classes and APIs | Detailed design |
| 31-37 min | Walk entry and exit sequences | Behavior validation |
| 37-42 min | Explain patterns and SOLID choices | Design rationale |
| 42-46 min | Concurrency, failures, and edge cases | Correctness under stress |
| 46-50 min | Follow-ups, tradeoffs, and summary | Extensibility and closure |

Do not spend fifteen minutes collecting every possible requirement. Clarify the decisions that change the object model, agree on scope, and proceed.

## 3. Opening Statement

A strong opening is:

> I will first clarify the required vehicle and spot types, entrances and floors, allocation policy, pricing, and payment behavior. Then I will identify the core objects, design the entry and exit workflows, and finish with concurrency, error cases, and extensibility. I will keep the initial implementation to one floor unless you want floors in scope.

This signals structure without prematurely claiming a pattern or implementation.

## 4. Requirements Gathering

Ask the highest-impact questions first.

### 4.1 Physical Layout

**Question:** Is this one parking lot or a platform managing many parking lots?

**Default assumption:** One parking lot instance. A higher-level service can own multiple instances later.

**Question:** Do we support one or multiple floors?

**Default assumption:** One floor in the core design. Add `ParkingFloor` when floor-specific inventory and displays are required.

**Question:** How many entrance and exit gates exist?

**Default assumption:** Multiple gates must be representable, even if the example configures one of each.

### 4.2 Vehicles and Spots

**Question:** Which vehicle categories are required?

**Default assumption:** Two-wheelers and four-wheelers. Truck, electric, compact, and accessible categories are extensions.

**Question:** Can a smaller vehicle use a larger spot?

This is a business rule, not an obvious technical truth. For the base design, require an exact type match. If fallback is allowed, introduce a `SpotCompatibilityPolicy` that returns ranked acceptable spot types.

**Question:** Are accessible spots tied to vehicle type or a permit?

Treat accessibility as an eligibility attribute/policy, not simply another vehicle size. Mentioning this distinction is a good domain-modeling point.

### 4.3 Allocation

**Question:** Can any available spot be assigned, or must it be nearest to the entrance/elevator?

**Default assumption:** Nearest compatible available spot from the entry gate.

**Question:** Is pre-booking required?

**Default assumption:** No reservations. Parking occurs on arrival. Reservations require holds, expiry, cancellation, and no-show rules.

### 4.4 Tickets and Exit

**Question:** Is the ticket physical, digital, or identified by license plate?

**Default assumption:** A unique ticket ID references vehicle, spot, gate, and entry time. License-plate lookup can be a secondary index.

**Question:** What happens for a lost ticket?

Keep it outside the core workflow. A follow-up policy could locate the active ticket by registration and charge a penalty after operator verification.

### 4.5 Pricing and Payment

**Question:** Is pricing hourly, per minute, flat, slab-based, or dynamic?

**Default assumption:** Pricing varies by vehicle type: hourly for two-wheelers and per-minute for four-wheelers, with partial units rounded up.

**Question:** Which payment methods and what happens when payment fails?

**Default assumption:** Cash, card, and UPI. A failed payment leaves the ticket open and spot occupied. A retry must not duplicate a successful payment.

### 4.6 Non-Functional Requirements

Ask explicitly:

- Can vehicles arrive concurrently at different entrances?
- Is persistence or process-restart recovery required?
- What is the expected lot size and arrival rate?
- Is nearest-spot lookup latency important?
- Do we need an audit history of tickets and payments?

For an in-memory LLD, guarantee thread safety within one process and discuss database transactions/distributed coordination as the production evolution.

## 5. Confirmed Scope

Before drawing classes, summarize:

### In Scope

- One floor and one parking lot instance.
- Multiple configurable entrance and exit gates.
- Two-wheeler and four-wheeler spots.
- Nearest compatible available spot.
- Entry ticket and exit receipt.
- Hourly and per-minute pricing.
- Cash, card, and UPI payment abstractions.
- Concurrent arrivals without double booking.
- Failed-payment retry and duplicate-checkout safety.

### Out of Scope

- Reservations and monthly passes.
- Valet workflows.
- Real payment gateway integration.
- Database schema and REST APIs.
- Multi-floor routing and display boards.
- Dynamic/surge pricing.
- License-plate recognition hardware.

Say that exclusions are sequencing decisions, not design impossibilities.

## 6. Core Use Cases

### UC1: Vehicle Entry

1. Vehicle reaches an entrance gate.
2. Gate resolves the manager for the vehicle type.
3. Manager atomically selects and occupies the nearest compatible spot.
4. Ticket service creates and registers a unique active ticket.
5. Gate returns the ticket.
6. If ticket creation fails, the gate releases the allocated spot.

### UC2: Vehicle Exit

1. Driver presents the ticket and chooses a payment method.
2. Exit gate validates and loads the authoritative ticket.
3. Cost computation calculates the amount at a captured exit time.
4. Ticket moves from `ACTIVE` to `PAYMENT_PENDING`.
5. Payment processor charges once and records success.
6. Ticket moves to `PAID`.
7. Manager releases the exact assigned spot.
8. Ticket moves to `CLOSED`; gate returns a receipt.

### UC3: No Capacity

If no compatible spot exists, reject entry with a clear no-capacity result. Do not issue a ticket.

### UC4: Payment Failure

Keep the ticket `PAYMENT_PENDING`, retain its calculated amount and exit time, and keep the spot occupied. Retry payment without recalculating a different amount.

## 7. Bottom-Up Object Discovery

Use nouns from the workflows, then assign one clear responsibility to each.

### Vehicle

Data: registration number and `VehicleType`.

Keep it immutable. A vehicle does not find a spot or calculate fees.

### ParkingSpot

Data: ID, type, rate, entrance distances, current vehicle.

Behavior:

- Check compatibility.
- Occupy only if empty.
- Vacate only for the expected vehicle.
- Report availability.

Its occupancy field is private. External code should not set `isEmpty` and `vehicle` independently because those fields can disagree.

### ParkingSpotManager

Owns the inventory for one spot type and the allocation strategy.

Behavior:

- `allocateSpot(vehicle, entranceId)`
- `releaseSpot(spot, registrationNumber)`
- Add/remove spots.
- Report availability.

The important API is `allocateSpot`, not separate public `find` and `park` calls.

### Ticket

Carries immutable entry context and controlled mutable exit state:

- Ticket ID and entrance gate ID.
- Vehicle and assigned spot.
- Entry time.
- Exit time and calculated amount.
- Payment record.
- Status.

Ticket methods enforce legal transitions instead of exposing generic setters.

### TicketService

Owns ticket identity and authoritative lookup. It prevents a registration number from holding multiple open tickets and retains closed tickets for idempotent retries/audit.

### EntranceGate

Coordinates admission. It does not own spot lists or pricing.

### ExitGate

Coordinates validation, pricing, payment, release, and receipt creation. Detailed algorithms remain delegated to collaborators.

### ParkingLot

Composition root and facade. It owns shared managers/services and configured gates. Avoid a mandatory Singleton because tests and multi-lot applications need multiple instances.

## 8. Key API Sketch

```java
public final class ParkingLot {
    Ticket enter(String entranceGateId, Vehicle vehicle);
    ExitReceipt exit(String exitGateId, String ticketId, PaymentMethod method);
    long availableSpots(ParkingSpotType type);
}

public abstract class ParkingSpotManager {
    synchronized ParkingSpot allocateSpot(Vehicle vehicle, String entranceId);
    synchronized void releaseSpot(ParkingSpot spot, String registrationNumber);
}

public interface ParkingAllocationStrategy {
    Optional<ParkingSpot> selectSpot(Collection<ParkingSpot> spots, String entranceId);
}

public interface PricingStrategy {
    BigDecimal calculate(Instant entry, Instant exit, BigDecimal hourlyRate);
}

public interface PaymentProcessor {
    Payment process(String ticketId, BigDecimal amount, Clock clock);
}
```

During an interview, write signatures before method bodies. Signatures expose responsibility boundaries faster than boilerplate.

## 9. Class-by-Class Design Sequence

### Step 1: Enums and Value Objects

Start with `VehicleType`, `ParkingSpotType`, `PaymentMethod`, `TicketStatus`, and `Vehicle`. This gives later classes stable vocabulary.

Do not put mutable prices directly into enums if pricing will evolve independently. Vehicle type is identity; pricing is policy.

### Step 2: Parking Spots

Create an abstract/base `ParkingSpot` containing common identity and occupancy behavior. Concrete spot classes fix their type. If subclasses contain no differentiated behavior, an alternative is one final spot class with a type field; acknowledge both options.

The implementation uses subclasses to mirror the interview transcript and make type-specific extension visible.

### Step 3: Allocation Strategy and Manager

Inject `ParkingAllocationStrategy` into a manager. A linear scan sorted by configured distance is simple and correct for the base design.

Complexity is $O(n)$ per allocation. Do not introduce a heap before establishing whether the expected scale requires it.

### Step 4: Factory Registry

The manager factory maps type to an already-created shared manager. It must not call `new Manager(emptyList)` for every vehicle.

This is a subtle but important ownership point: factories can select services as well as construct objects, but stateful inventory must have one authoritative owner.

### Step 5: Ticket Lifecycle

Model legal states:

```text
ACTIVE -> PAYMENT_PENDING -> PAID -> CLOSED
```

Retain exit time and amount at `PAYMENT_PENDING` so payment retries are stable.

### Step 6: Pricing and Cost Computation

`CostComputation` selects the pricing behavior associated with a vehicle type. `PricingStrategy` performs duration and rounding mathematics.

Use `BigDecimal` or integer minor units for money. State rounding explicitly:

$$
\text{billableHours} = \max\left(1, \left\lceil\frac{\text{durationMillis}}{3{,}600{,}000}\right\rceil\right)
$$

$$
\text{hourlyCharge} = \text{billableHours} \times \text{hourlyRate}
$$

For minute pricing:

$$
\text{billableMinutes} = \max\left(1, \left\lceil\frac{\text{durationMillis}}{60{,}000}\right\rceil\right)
$$

### Step 7: Payment

Use a processor abstraction because cash collection, card gateways, and UPI integrations have different implementations. In interview code, processors may return success records; in production they carry external transaction IDs and idempotency keys.

### Step 8: Gates and Facade

Connect dependencies through constructors. The gate methods become readable workflow orchestration, and the `ParkingLot` builds one shared object graph.

## 10. Entry Workflow Deep Dive

Pseudocode:

```java
Ticket admit(Vehicle vehicle) {
    ParkingSpotManager manager = managerFactory.getManager(vehicle.type());
    ParkingSpot spot = manager.allocateSpot(vehicle, gateId);
    try {
        return ticketService.issueTicket(gateId, vehicle, spot, clock);
    } catch (RuntimeException failure) {
        manager.releaseSpot(spot, vehicle.registrationNumber());
        throw failure;
    }
}
```

Points to explain:

- Allocation chooses only a compatible manager.
- Selection and occupation are atomic.
- Ticket creation happens after occupation so the ticket references a guaranteed spot.
- Compensation releases the spot if ticket registration fails.
- In a database-backed system, spot update and ticket insert belong in one transaction.

## 11. Exit Workflow Deep Dive

Pseudocode:

```java
synchronized (ticket) {
    if (ticket.status() == ACTIVE) {
        Instant exitTime = clock.instant();
        BigDecimal amount = costFactory.forType(ticket.vehicle().type())
                .calculate(ticket, exitTime);
        ticket.beginPayment(exitTime, amount);
    }

    if (ticket.status() == PAYMENT_PENDING) {
        Payment payment = paymentFactory.forMethod(method)
                .process(ticket.id(), ticket.amount());
        ticket.recordSuccessfulPayment(payment);
    }

    if (ticket.status() == PAID) {
        manager.releaseSpot(ticket.spot(), ticket.vehicle().registration());
        ticketService.markClosed(ticket);
    }
}
```

Points to explain:

- Capture exit time once; retries must not produce a growing price.
- Serialize checkout for one ticket to prevent duplicate charges.
- Do not free a spot on payment failure.
- Persist payment success before release.
- A failure after payment but before release is recoverable: status remains `PAID`, so retry skips charging and performs release.
- A second request after `CLOSED` returns the existing receipt/payment.

## 12. Non-Negotiable Invariants

State at least the first five during the interview:

1. A spot can contain at most one vehicle.
2. A vehicle can have at most one open ticket.
3. Ticket vehicle and occupied spot vehicle must match.
4. A vehicle can use only a compatible spot under the current policy.
5. Spot selection and occupation form one atomic operation.
6. No ticket is issued when no spot is assigned.
7. A spot is released only after successful payment.
8. One ticket is charged at most once.
9. Exit time and amount are stable across payment retries.
10. Only legal ticket state transitions are allowed.

These invariants matter more than the number of patterns in the diagram.

## 13. Concurrency Discussion

### The Race

Unsafe code does this:

```text
Thread A: find S1 free
Thread B: find S1 free
Thread A: occupy S1
Thread B: occupy S1
```

Synchronizing only `ParkingSpot.park()` prevents corrupted spot state, but one request then fails after being told a spot was selected. The better manager-level contract atomically selects and occupies.

### In-Memory Solution

Synchronize `allocateSpot` and `releaseSpot` per type manager. Two-wheelers and four-wheelers can still allocate concurrently because they use different managers.

This coarse lock is correct and explainable. For higher contention, evolve to a concurrent availability index plus compare-and-set spot reservation.

### Database Solution

Use a transaction with conditional state update:

```sql
UPDATE parking_spot
SET status = 'OCCUPIED', vehicle_id = ?
WHERE id = ? AND status = 'AVAILABLE';
```

Proceed only when one row is updated. Insert the ticket in the same transaction. In distributed deployments, the database is normally the source of truth; a process-local lock is insufficient.

## 14. Patterns and Their Justification

### Strategy: Strong Choice

Variation exists in allocation and pricing. Strategies let the manager/cost computation depend on stable contracts while policies change independently.

### Factory/Registry: Useful Choice

Gates should not contain switches for every vehicle or payment type. Factories centralize selection. Mention that adding an enum value still requires factory registration; true plugin discovery would require configuration or dependency injection.

### Facade: Useful Choice

Callers use `ParkingLot.enter/exit` rather than navigating manager and gate internals.

### Inheritance: Use Carefully

It is appropriate when subtypes have substitutable behavior. If spot subclasses only carry different enum values, composition may be simpler. Saying this shows judgment rather than pattern enthusiasm.

### Singleton: Usually Avoid

The world may contain multiple parking lots, and tests need isolated instances. Application configuration can decide object lifetime without hard-coding a Singleton.

## 15. SOLID Discussion

- **SRP:** Spot owns occupancy invariants; manager owns inventory; pricing owns charge calculation; processor owns payment integration; gate owns workflow coordination.
- **OCP:** New allocation/pricing/payment strategies are added behind interfaces. New vehicle types still require explicit registration, which is an honest bounded tradeoff.
- **LSP:** Concrete managers and cost computations honor base contracts. Avoid subclasses that reject valid base operations.
- **ISP:** Small strategy/processor interfaces expose one capability each.
- **DIP:** Gates depend on factories/services and interfaces; strategies and clocks are injected.

Do not claim perfect OCP merely because a factory exists. A switch-based factory changes when a new enum type is introduced.

## 16. Edge Cases to Cover

- Null or blank registration number.
- Unknown entrance/exit gate.
- Missing entrance-to-spot distance.
- No compatible capacity.
- Duplicate spot IDs.
- Wrong manager or incompatible spot.
- Same vehicle entering twice.
- Unknown or forged ticket.
- Exit time before entry time due to bad clock data.
- Partial billing unit and zero-duration parking.
- Payment gateway timeout/failure.
- Two simultaneous checkout requests.
- Release called for the wrong vehicle.
- Removing an occupied spot from inventory.
- Retry after payment but before release.
- Retry after ticket is already closed.

## 17. Testing Strategy

Start with behavior, then add concurrency.

### Unit Tests

- Spot accepts compatible vehicle and rejects incompatible vehicle.
- Occupied spot rejects a second vehicle.
- Allocation strategy selects minimum distance and deterministic ID on ties.
- Hourly and minute policies round boundaries correctly.
- Ticket rejects illegal transitions.
- Manager rejects release by the wrong registration.

### Workflow Tests

- Entry reduces availability and exit restores it.
- Full lot rejects entry without creating a ticket.
- Failed payment retains occupation.
- Retried payment succeeds at the original amount.
- Repeated checkout reuses the payment.

### Concurrency Test

Start two threads against one spot using a latch. Exactly one admission succeeds and availability becomes zero.

Inject `Clock` rather than sleeping in pricing tests. Time is a dependency.

## 18. Scaling the Allocation Index

The base nearest strategy scans $n$ spots, so allocation is $O(n)$.

For a large lot, maintain an availability index per entrance and spot type:

```text
Map<EntranceId,
    Map<ParkingSpotType,
        PriorityQueue<SpotDistance>>>
```

Allocation and release become approximately $O(\log n)$. But one physical spot appears in several entrance heaps, creating stale-entry and synchronization problems. Two common approaches are:

1. Lazy deletion: pop entries until the authoritative spot state is available.
2. Indexed balanced sets: remove/reinsert a spot in every relevant entrance index.

Call out that the heap is a performance optimization, not the source of truth.

## 19. Extension Roadmap

### Multiple Floors

Add `ParkingFloor`, which owns per-type managers and a display board. A lot-level allocation strategy first selects a floor, then a floor manager selects a spot. Do not place a `floorNumber` field everywhere without defining ownership.

### Display Boards

Subscribe to spot state changes or query manager counters. For production, publish occupancy events and rebuild displays from authoritative state after failures.

### Electric Charging

Model charging capability and charger availability separately from vehicle size. Parking and charging may have separate fees and states.

### Reservations

Add `HELD`/`RESERVED` spot states, reservation expiry, idempotent confirmation, cancellation, and no-show policy. Reservation and walk-in allocation must use the same authoritative inventory.

### Persistence and APIs

Repositories persist spots, tickets, and payments. Application services own transactions. REST controllers validate transport input and call the same use cases; domain classes should not know HTTP.

### Dynamic Pricing

Compose strategies or inject a tariff schedule. Snapshot the applied tariff/version on the ticket so later rule changes do not rewrite historical charges.

## 20. Common Mistakes

- Writing classes before clarifying vehicle, floor, and pricing scope.
- Modeling `isEmpty` and `vehicle` with independent public setters.
- Separating `findSpot` and `parkVehicle` across a concurrency boundary.
- Creating a new stateful manager from the factory per request.
- Releasing the spot before payment succeeds.
- Recalculating price on every payment retry.
- Using `double` for money without discussing precision.
- Using current time directly everywhere, making tests flaky.
- Claiming a heap solves concurrency.
- Adding every possible pattern and losing the main workflow.
- Forgetting no-capacity, invalid-ticket, and payment-failure paths.
- Calling the whole design extensible without showing where registration changes.

## 21. Closing Pitch

End with a compact validation:

> The design completes entry and exit around four invariants: compatible capacity, atomic allocation, controlled ticket transitions, and payment-before-release. Managers own shared inventory; strategies vary allocation and pricing; factories select configured collaborators; and the ParkingLot facade owns the object graph. The in-memory implementation uses manager and ticket synchronization. At production scale I would move authoritative state into transactional persistence, add idempotency keys for payment, and optimize nearest lookup with per-gate availability indexes only after measuring contention.

Then ask which follow-up the interviewer wants to explore: floors, persistence, nearest-index performance, or distributed concurrency.