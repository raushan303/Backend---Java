# 8. Annotations, Modern Syntax, and Concurrency

## Annotations

Annotations are metadata beginning with `@`:

```java
@Override
public double cost() { ... }
```

They may guide the compiler, tools, tests, or frameworks. `@Override` is used
throughout the patterns code. An annotation is not a comment: code can inspect
some annotations, and tools can act on them.

## Modern `switch` expressions

The factory-pattern code uses arrow-style switch branches:

```java
return switch (type) {
    case CAR -> new Car();
    case BIKE -> new Bike();
    case TRUCK -> new Truck();
};
```

Unlike an old C/C++-style switch, this form returns a value and does not fall
through, so no `break` is needed. A multi-statement branch uses `yield`:

```java
case CARD -> {
    audit();
    yield new CardPaymentProcessor();
}
```

## Casts and `instanceof`

A cast asks Java to view a reference as a more specific compatible type:

```java
ParkingSpot spot = (ParkingSpot) value;
```

An invalid runtime cast throws `ClassCastException`. Prefer polymorphism and
generic types when they remove the need to cast.

Pattern matching combines a type test and safe local variable:

```java
if (value instanceof Ticket ticket) {
    use(ticket);
}
```

## `synchronized`

```java
public synchronized ParkingSpot allocateSpot(...) { ... }
```

For an instance method, Java acquires the current object's monitor before
running the method and releases it afterward, including when an exception is
thrown. Only one thread at a time can execute code guarded by that same monitor.

For a static synchronized method, the monitor belongs to the `Class` object.
A synchronized block can lock a chosen object:

```java
synchronized (lock) {
    // guarded state
}
```

Synchronization provides mutual exclusion and memory visibility, but it does
not make a multi-object workflow automatically thread-safe. Keep locked regions
small and consistently protect the same shared state.

## Type inference with `var`

Modern Java permits `var` for initialized local variables:

```java
var processor = new CashPaymentProcessor();
```

The type is still static and fixed; Java infers it at compile time. `var` is not
JavaScript `var`, and it cannot be used for fields, parameters, or uninitialized
locals. The current repository generally spells out types, which can be clearer
while learning.

## Time API

The parking-lot implementation uses `Instant`, `Duration`, and `Clock` from
`java.time`:

- `Instant`: one point on the UTC timeline;
- `Duration`: elapsed time between instants;
- `Clock`: source of the current time.

Passing a `Clock` into constructors avoids hard-coding `Instant.now()`, making
tests repeatable.

## Common operators and control flow

- `&&`, `||`, `!`: short-circuit boolean operators.
- `==`, `!=`: primitive value or object identity comparison.
- `?:`: ternary expression, `condition ? whenTrue : whenFalse`.
- `++`, `--`, `+=`: update operators familiar from C++/JavaScript.
- `if` / `else`, `for`, `while`, `do`, `switch`: standard control flow.
- `return`: exits a method, optionally with its result.
- `break`: exits a loop or old-style switch.
- `continue`: starts the next loop iteration.

## Naming conventions seen here

- `PaymentProcessorFactory`: classes, interfaces, enums, and records use
  `UpperCamelCase`.
- `getProcessor`, `paymentMethod`: methods and variables use `lowerCamelCase`.
- `CASH`, `MAX_RETRIES`: enum constants and constants use `UPPER_SNAKE_CASE`.
- package names are lowercase by convention. The existing uppercase `LLD`
  package works but is unconventional Java style.

## How to read a dense line

```java
private final Map<PaymentMethod, PaymentProcessor> processors;
```

Translate in layers:

1. `processors`: the field's name.
2. `Map<K, V>`: a generic key-value type.
3. `PaymentMethod`: keys are enum values.
4. `PaymentProcessor`: values satisfy this interface.
5. `final`: assign the map reference once.
6. `private`: only this class accesses the field directly.

That same outside-to-inside approach works for most long Java declarations.