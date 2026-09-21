# 4. Constructors and Dependencies

## Constructor rules

A constructor has the class name and no return type, not even `void`:

```java
public PaymentProcessorFactory(...) { ... }
```

Calling `new PaymentProcessorFactory(...)` allocates an object and runs the
matching constructor. Constructors commonly validate parameters and establish
all required fields.

If a class declares no constructor, Java provides a no-argument constructor.
As soon as you declare any constructor, that automatic constructor disappears.

## Why `PaymentProcessorFactory` has two constructors

The no-argument constructor is convenient production setup:

```java
public PaymentProcessorFactory() {
    this(Map.of(
            PaymentMethod.CASH, new CashPaymentProcessor(),
            PaymentMethod.CARD, new CardPaymentProcessor(),
            PaymentMethod.UPI, new UpiPaymentProcessor()));
}
```

`this(...)` calls another constructor in the **same** class. It must be the
first statement. Here it creates the normal processor map and passes it to the
second constructor. This is constructor delegation, not assignment to a field.

The parameterized constructor is the single initialization path:

```java
public PaymentProcessorFactory(Map<PaymentMethod, PaymentProcessor> processors) {
    this.processors = new EnumMap<>(PaymentMethod.class);
    this.processors.putAll(processors);
}
```

Line by line:

1. It accepts any `Map` whose keys are `PaymentMethod` and values implement
   `PaymentProcessor`.
2. `this.processors` means the field; plain `processors` means the parameter.
3. `new EnumMap<>(PaymentMethod.class)` creates a map optimized for enum keys.
4. `<>` is the diamond operator. Java infers its generic types from the left.
5. `PaymentMethod.class` is the runtime `Class<PaymentMethod>` object, needed
   because generic type arguments are mostly erased at runtime.
6. `putAll` makes a defensive copy, so later changes to the caller's map do not
   change the factory's map.

The two-constructor design gives both:

- a simple default: `new PaymentProcessorFactory()`;
- dependency injection: tests or callers can provide custom processors.

Keeping the actual field initialization in one constructor avoids duplicated
setup and prevents the constructors from drifting apart.

## Is the factory immutable?

The field reference is `final`, and the map is private, so callers cannot
replace or directly mutate it after construction. Internally it is still a
mutable `EnumMap`. This is effective encapsulation, not deep immutability.

The custom constructor permits a partial map. That is intentional if unsupported
methods should fail only when requested; otherwise it could validate that every
`PaymentMethod.values()` entry is present during construction.

## Overloading

Two methods or constructors may share a name when their parameter lists differ:

```java
ParkingLot(spots, entrances, exits)
ParkingLot(spots, entrances, exits, clock, paymentProcessorFactory)
```

This is overload resolution at compile time. It differs from overriding, where
a subclass replaces inherited behavior at runtime. Java has no JavaScript-style
default parameter values, so overloads often provide convenient defaults.

## `this(...)` versus `super(...)`

- `this(...)`: another constructor in the same class.
- `super(...)`: a constructor in the parent class.
- either call must be first;
- one constructor cannot directly call both, but a delegated constructor can
  eventually call `super(...)`.

If no explicit call appears, Java inserts `super()` when that parent constructor
exists.

## Dependency injection without a framework

Constructor parameters can supply dependencies:

```java
public ParkingLot(..., Clock clock,
        PaymentProcessorFactory paymentProcessorFactory) { ... }
```

This is dependency injection even without Spring. The class receives what it
needs instead of always constructing it internally. Injecting `Clock` makes
time-based behavior deterministic in tests; injecting a processor map makes
payment implementations replaceable.