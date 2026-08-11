# Interviewer Q&A, Tricky Parts, and Brownie Points

## 1. Hot Points to Proactively Mention

Mention these without waiting for a follow-up:

1. **Allocation must be atomic.** `find` followed later by `park` can double-assign under concurrency.
2. **Managers are shared state.** A factory must return the configured manager, not create an empty manager per request.
3. **Payment precedes release.** Failed payment keeps the spot occupied.
4. **Checkout is idempotent.** Capture exit time/amount once and record payment before release.
5. **Money and time are explicit.** Use `BigDecimal`/minor units, `Duration`, rounding rules, and an injected `Clock`.
6. **Ticket has a state machine.** Generic setters permit illegal transitions.
7. **Nearest lookup is policy, not core spot behavior.** Keep it behind an allocation strategy.
8. **A heap is only an index.** Authoritative availability still needs synchronized/transactional state.
9. **Accessibility is eligibility, not merely size.** Model a policy/permit if it enters scope.
10. **Scope is intentional.** Finish one-floor entry/exit before adding floors, displays, and reservations.

## 2. Non-Negotiable Points

An interview answer is incomplete if it omits these:

- Requirement clarification.
- A complete entry workflow.
- A complete exit workflow.
- Vehicle-to-spot compatibility.
- No-capacity behavior.
- Ticket ID, entry time, vehicle, and assigned spot.
- Pricing boundary and rounding policy.
- Payment failure behavior.
- Spot availability update on both entry and successful exit.
- Prevention of duplicate spot assignment.
- Clear ownership of the parking spot collection.

Patterns, floors, and heaps do not compensate for a missing core invariant.

## 3. Common Interviewer Questions and Model Answers

### Q1. Why do you need a ParkingSpotManager?

**Answer:** The manager is the controlled owner of spot inventory for a category. It encapsulates allocation, release, capacity updates, and synchronization. Gates coordinate workflows but should not scan or mutate raw spot lists. This boundary also lets the allocation index change from a list to a heap without changing gate code.

### Q2. Why separate two-wheeler and four-wheeler managers?

**Answer:** It partitions inventory and lock contention and makes exact compatibility straightforward. An alternative is one manager indexed by `ParkingSpotType`; I would choose that if most behavior is shared and vehicle categories are highly dynamic. The important property is one authoritative inventory, not the number of manager classes.

### Q3. Why is separate `findParkingSpot()` and `parkVehicle()` unsafe?

**Answer:** Availability can change between the calls. Two entrance threads can both select the same spot. The manager should expose `allocateSpot`, which selects and occupies under one lock or transaction. `ParkingSpot.park` should still validate emptiness as defense in depth.

### Q4. Why is `ParkingSpot.park()` synchronized if the manager is synchronized?

**Answer:** The manager provides operation-level atomicity, while the spot protects its own invariant from other callers. In a tightly encapsulated production model we might restrict spot mutation to the manager and rely on one boundary, but defensive entity validation prevents accidental corruption.

### Q5. Why use a Strategy for allocation?

**Answer:** Allocation policy is expected to vary: nearest entrance, nearest elevator, first available, accessible eligibility, or EV charging. The manager should own inventory but delegate ranking. Strategy keeps policy changes independent of occupancy mechanics.

### Q6. Why not always use a priority queue?

**Answer:** A linear scan is simple, correct, and often sufficient for an interview-sized lot. A heap reduces selection cost but adds maintenance: each spot may appear in multiple entrance heaps, releases must update indexes, and stale entries need validation. I would add it based on measured scale and keep authoritative state outside the heap.

### Q7. What is the complexity of nearest allocation?

**Answer:** The base scan is $O(n)$ time and $O(1)$ extra space per allocation. A prebuilt heap per entrance and type gives roughly $O(\log n)$ allocation/release, with $O(e \cdot n)$ index space for $e$ entrances and additional stale-entry coordination.

### Q8. How do multiple entrances affect nearest allocation?

**Answer:** A spot stores or can derive distance from each entrance. The allocation method receives the entrance ID. At larger scale, maintain separate availability indexes per entrance/type while checking one authoritative spot status before reservation.

### Q9. What if a spot has equal distance from an entrance?

**Answer:** Use a deterministic tie-breaker such as spot ID. Determinism simplifies testing and operational debugging.

### Q10. Why use factories?

**Answer:** Gates should request the manager, cost computation, or processor for a type without embedding selection switches. The manager factory acts as a registry of shared managers; payment and computation factories select stateless/configured services. A factory does not automatically make the design fully open for extension because registration still changes.

### Q11. Is adding a vehicle type Open/Closed?

**Answer:** Partly. Strategies and interfaces are extensible, but an enum and explicit factory map require registration changes. For a bounded domain that is acceptable and safer than reflection. For runtime plugins, use configuration and dependency injection keyed by external type identifiers.

### Q12. Why not use a Singleton ParkingLot?

**Answer:** Object lifetime belongs to application configuration. There may be multiple parking lots, and tests need isolated instances. A Singleton introduces global state and hidden dependencies without solving any domain requirement.

### Q13. Why store a spot reference in the ticket?

**Answer:** Exit must release the exact assigned spot and calculate against the agreed spot/rate context. In persisted systems the ticket stores a spot ID and tariff snapshot rather than an in-memory object reference.

### Q14. Should rate be stored on the spot or pricing strategy?

**Answer:** The spot can carry its configured base rate while strategy owns duration/slab calculation. For production, snapshot tariff ID/version and effective rate on ticket entry to prevent later configuration changes from altering an active or historical ticket.

### Q15. Why use `BigDecimal` instead of `double`?

**Answer:** Binary floating point cannot exactly represent many decimal currency values. `BigDecimal` or integer minor units provides explicit decimal precision and rounding. The pricing contract must define scale and rounding mode.

### Q16. What should happen for a one-second stay?

**Answer:** That is a policy question. In this design, each strategy charges a minimum of one unit, and partial units round up. I would confirm grace periods or free windows during requirements gathering.

### Q17. Why inject `Clock`?

**Answer:** Pricing depends on time, so time is an external dependency. Injection makes boundary tests deterministic, avoids sleeping, and enables simulation of payment retries. Production uses `Clock.systemUTC()`.

### Q18. Why capture exit time before payment?

**Answer:** Payment can fail or take time. The fee should represent arrival at exit and remain stable across retries. Recomputing with current time can increase the amount after each failed attempt and create poor user experience/reconciliation issues.

### Q19. Why have ticket statuses?

**Answer:** They make legal transitions explicit and support recovery. `PAYMENT_PENDING` freezes price, `PAID` proves no second charge is needed, and `CLOSED` proves release completed. A boolean `paid` cannot distinguish payment success from completed checkout.

### Q20. What happens when payment fails?

**Answer:** Keep the ticket `PAYMENT_PENDING`, preserve exit time and amount, and keep the spot occupied. Return a recoverable failure and allow another processor/method to retry. Do not close the ticket or free capacity.

### Q21. What if payment succeeds but releasing the spot fails?

**Answer:** Persist payment and move to `PAID` before release. A retry sees `PAID`, skips charging, and retries release/closure. In production use durable state, an outbox/job for recovery, and alerts for tickets stuck in `PAID`.

### Q22. What if releasing succeeds but closing the ticket fails?

**Answer:** In memory, those operations are adjacent under a ticket lock. In a database, release and ticket closure should be one transaction after durable payment success. If an external split is unavoidable, use idempotent operations and reconciliation to detect a free spot with a non-closed paid ticket.

### Q23. What if two exit gates process the same ticket?

**Answer:** Serialize per ticket and inspect state. One request performs payment; the other sees `PAID`/`CLOSED` and does not charge again. In distributed deployment, use a database row lock/version check or distributed idempotency record, not a JVM monitor.

### Q24. How do you make payment idempotent with an external gateway?

**Answer:** Send a stable idempotency key derived from ticket/payment attempt identity, persist request and gateway transaction ID, and treat duplicate callbacks as upserts/state transitions. Never generate a new idempotency key on an HTTP retry.

### Q25. What if a payment gateway times out?

**Answer:** Timeout is ambiguous, not necessarily failure. Mark the attempt `UNKNOWN/PENDING_CONFIRMATION`, query the gateway or await a webhook using the same idempotency key, and do not issue another independent charge until reconciled. The simplified code models a definite failure; production needs the additional state.

### Q26. How do you persist entry atomically?

**Answer:** In one transaction, conditionally mark a selected spot occupied and insert the active ticket. If the conditional spot update affects zero rows, retry allocation. A uniqueness constraint on open vehicle registration adds defense in depth.

### Q27. What database constraints would you add?

**Answer:** Unique spot ID; unique ticket ID; one active occupancy per spot; one open ticket per registration; non-negative rates/amounts; foreign keys from ticket to vehicle/spot and payment to ticket; version column for optimistic locking; indexed status/type/floor fields.

### Q28. Optimistic or pessimistic locking?

**Answer:** Optimistic conditional updates work well when collisions are low. Under very high contention for a small remaining inventory, `SELECT ... FOR UPDATE SKIP LOCKED` or a reservation queue can reduce retries. Choose based on database and measured contention.

### Q29. How would you add floors?

**Answer:** Add `ParkingFloor` as inventory owner, not just a floor number on every class. A lot-level strategy selects a floor based on capacity/distance; the floor manager selects a spot. Each floor can own its display board and per-type counts.

### Q30. How would you add display boards?

**Answer:** Managers maintain authoritative counters or emit spot-state events. Displays subscribe to updates and periodically reconcile from the source of truth. Display failure must not block entry/exit transactions.

### Q31. How would you add reservations?

**Answer:** Introduce reservation identity, hold expiry, `HELD/RESERVED` state, confirmation, cancellation/no-show policy, and an expiry worker. Walk-ins and reservations must compete through one atomic inventory service.

### Q32. How would you add accessible parking?

**Answer:** Store spot capabilities and vehicle/user eligibility separately. A compatibility policy checks permit and ranks eligible accessible spots. Do not infer disability eligibility from vehicle size.

### Q33. How would you add EV charging?

**Answer:** Model spot charging capability, charger state, connector type, and charging session. Parking allocation considers compatibility; charging price/lifecycle remains a separate concern because a car can be parked while charging fails or completes.

### Q34. How would you handle a lost ticket?

**Answer:** Locate the single open ticket by registration, verify identity through an attendant/workflow, apply configured lost-ticket policy, and audit the override. Do not create a second ticket or silently choose any active ticket.

### Q35. Can a bike use a car spot?

**Answer:** Ask the business. Exact matching maximizes predictable capacity. If fallback is allowed, a compatibility strategy ranks exact type before larger alternatives and may apply different pricing. Avoid hard-coded `if bike then car spot` inside the gate.

### Q36. How do you remove a spot for maintenance?

**Answer:** An available spot can transition to `OUT_OF_SERVICE`. An occupied spot is marked pending maintenance and removed after exit. Removing it from a list without status/history can invalidate active tickets.

### Q37. How do you model a full lot response?

**Answer:** Return a domain no-capacity result/exception with requested type and optionally alternatives. Do not issue a ticket. At an API boundary map it to an appropriate response; at a physical gate show capacity and keep the barrier closed.

### Q38. What metrics would you monitor?

**Answer:** Occupancy by floor/type, allocation latency, no-capacity rejection rate, average parking duration, payment success/timeout rate, tickets stuck in payment states, release failures, revenue reconciliation mismatch, and stale index count.

### Q39. How would you recover after process restart?

**Answer:** Persist spots, tickets, and payments as authoritative state. On startup rebuild in-memory availability indexes from occupied/available records and reconcile pending payment attempts with the gateway. Do not rely on in-memory tickets for production recovery.

### Q40. Would you use events?

**Answer:** Use events for secondary effects such as displays, analytics, notifications, and audit pipelines. Keep spot/ticket consistency transactional. Publish events through an outbox so a committed state change is not lost if message publication fails.

## 4. Tricky Failure Windows

### Window A: Spot Occupied, Ticket Not Created

**Risk:** Capacity leaks and vehicle has no valid ticket.

**In-memory answer:** Catch ticket issue failure and release the spot.

**Production answer:** Update spot and insert ticket in one transaction.

### Window B: Ticket Created, Barrier Does Not Open

**Risk:** Driver retries and receives another ticket.

**Answer:** Use an idempotency key from gate request/vehicle read, return the existing active ticket, and provide operator cancellation if entry did not occur.

### Window C: Payment Request Timed Out

**Risk:** Charging again may duplicate a successful but unacknowledged payment.

**Answer:** Keep an `UNKNOWN` attempt and reconcile by stable gateway idempotency key before another charge.

### Window D: Paid, Spot Not Released

**Risk:** Customer paid but inventory remains unavailable.

**Answer:** Durable `PAID` state; retry cleanup without payment; background reconciliation and alerts.

### Window E: Spot Released, Ticket Not Closed

**Risk:** Inventory and ticket disagree.

**Answer:** One database transaction for release/closure, or an idempotent reconciliation process if split.

### Window F: Stale Nearest-Spot Heap

**Risk:** Heap returns an occupied or out-of-service spot.

**Answer:** Heap is advisory. Validate and atomically reserve authoritative spot state; lazily discard stale entries and measure index drift.

### Window G: Pricing Configuration Changes Mid-Stay

**Risk:** Same stay receives unexpected historical/current rate.

**Answer:** Define policy and snapshot tariff version/rate at entry, or store enough effective-date data to calculate deterministically.

## 5. Brownie Points

These earn value only after the core flow is complete.

### High-Value Brownie Points

- State explicit invariants before implementation.
- Make allocation atomic rather than merely synchronizing getters/setters.
- Explain payment idempotency and ambiguous gateway timeout.
- Capture one exit time and retain the calculated amount.
- Inject `Clock` and use deterministic concurrency tests.
- Explain that a factory must return shared stateful managers.
- Distinguish authoritative state from a priority-queue index.
- Use transaction/conditional-update language for distributed correctness.
- Snapshot tariff version for auditability.
- Discuss coarse locks first, then optimize based on contention.

### Product/Domain Brownie Points

- Accessibility eligibility is separate from vehicle size.
- EV parking and charging have related but distinct lifecycles.
- Lost-ticket overrides require audit and identity verification.
- Maintenance is a spot state, not deletion of an active spot.
- Display and analytics failures must not block gate transactions.
- Grace periods and maximum daily caps are pricing policy questions.

### Operational Brownie Points

- Outbox for reliable occupancy/payment events.
- Reconciliation jobs for tickets stuck in `PAID` or payment unknown.
- Metrics for no-capacity rate and allocation latency.
- Unique constraints as defense in depth.
- Rebuild availability indexes from persistent truth after restart.
- Redact/tokenize payment details; never store raw card data.

## 6. Tradeoffs Worth Saying Out Loud

### Linear Scan vs Heap

| Linear scan | Heap/index |
|---|---|
| Simple and always reflects current list | Faster nearest lookup |
| $O(n)$ allocation | Roughly $O(\log n)$ updates |
| Low maintenance complexity | Multiple entrances duplicate index entries |
| Good interview baseline | Needs stale-entry and synchronization strategy |

### One Manager vs Manager Per Type

| One indexed manager | Manager per type |
|---|---|
| Fewer classes | Clear inventory partition |
| Dynamic categories easier | Separate locks reduce unrelated contention |
| More internal branching/indexing | Factory registration required |

Both can be valid. Defend ownership and invariants rather than class count.

### Inheritance vs Composition for Spots

Use inheritance if subtype behavior genuinely differs and remains substitutable. Use a final `ParkingSpot` with `ParkingSpotType` and capability policies when subclasses would only pass constants to `super`.

### Coarse vs Fine-Grained Locking

Start with one manager lock because it is easy to prove correct. Fine-grained locking/indexes improve throughput but increase deadlock and stale-state complexity. State the migration path rather than prematurely implementing it.

## 7. Weak Answers to Avoid

### Weak: "I used Factory, Strategy, Singleton, Observer, and Decorator."

Why weak: It lists patterns without proving variation or correctness.

Better: Explain the changing policy each abstraction isolates and avoid Singleton unless lifetime is a requirement.

### Weak: "Two threads cannot book the same spot because `isEmpty` is boolean."

Why weak: Read-then-write is not atomic.

Better: Put selection and reservation in one lock/transaction with a conditional update.

### Weak: "After payment API call, I free the spot."

Why weak: Timeout may be ambiguous, and success may not be durably recorded.

Better: Persist idempotent payment outcome, then release; retry from state.

### Weak: "I will use a min-heap for every entrance."

Why weak: It ignores release updates, duplicate indexes, stale entries, and concurrency.

Better: First define authoritative occupancy, then use heaps as validated indexes.

### Weak: "Adding a type requires no code change."

Why weak: Enums and factory registrations generally change.

Better: Be precise about which policies are open and where explicit registration remains.

### Weak: "Price is `hours * double rate`."

Why weak: Duration rounding and money precision are undefined.

Better: State minimum unit, ceiling/floor behavior, timezone, decimal precision, and tariff version.

### Weak: Drawing Floors Before Completing Exit

Why weak: Breadth replaces a complete primary use case.

Better: Complete entry, payment, release, and failure behavior before extensions.

## 8. Whiteboard Checklist

Before finishing, check that the board contains:

- [ ] Agreed scope and assumptions.
- [ ] Entry and exit use cases.
- [ ] Vehicle, spot, manager, ticket, gates, pricing, and payment.
- [ ] Associations and ownership arrows.
- [ ] Main method signatures.
- [ ] Atomic allocation boundary.
- [ ] Ticket states.
- [ ] Payment failure path.
- [ ] No-capacity path.
- [ ] Money/time policy.
- [ ] Complexity of current allocation.
- [ ] One production scaling direction.
- [ ] Tests or edge cases.

## 9. Final 60-Second Answer

> The ParkingLot facade owns configured entrance/exit gates and shared spot managers. At entry, the gate resolves the compatible manager, which atomically selects and occupies the nearest spot, then registers an active ticket; ticket failure compensates by releasing the spot. At exit, the gate captures one exit time, calculates an exact amount through a pricing strategy, records payment, releases the assigned spot, and closes the ticket. Ticket states make failed payments and post-payment retries safe. The in-memory version synchronizes per manager and ticket; production moves those guarantees into conditional database updates, transactions, and payment idempotency records. Floors, reservations, display boards, and heap indexes can be added around these same invariants.