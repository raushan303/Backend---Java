# 3. Interfaces, Abstract Classes, and Inheritance

## Interfaces define contracts

```java
public interface PaymentProcessor {
    Payment process(Payment payment);
}
```

An interface says what an implementation can do. A class adopts the contract
with `implements`:

```java
public final class CashPaymentProcessor implements PaymentProcessor { ... }
```

Code can depend on `PaymentProcessor` instead of a concrete payment class. That
enables polymorphism: the same call works for cash, card, or UPI processors.

Interface methods are implicitly `public abstract` unless they are declared
`default`, `static`, or `private`. Interface fields are implicitly constants
(`public static final`). A class may implement multiple interfaces.

Think of an interface as similar to a TypeScript interface that also exists as
a Java type contract, or a C++ class made only of pure virtual operations.

## Abstract classes provide a partial implementation

```java
public abstract class ParkingSpotManager {
    private final List<ParkingSpot> spots;

    public synchronized long availableSpotCount() {
        return spots.stream().filter(ParkingSpot::isAvailable).count();
    }
}
```

You cannot instantiate an abstract class with `new`. It may contain fields,
constructors, complete methods, and abstract methods. It is useful when related
subclasses share state or implementation.

## `extends`

```java
public final class FourWheelerParkingSpotManager extends ParkingSpotManager {
    ...
}
```

The subclass inherits accessible behavior from its parent. Java classes have
single inheritance: a class can extend only one class. Every class ultimately
extends `Object`, directly or indirectly.

An interface may `extend` one or more interfaces. A class uses `implements` for
interfaces and `extends` for its parent class.

## `abstract` methods

An abstract method has a signature but no body:

```java
protected abstract BigDecimal calculateFee(Ticket ticket, Instant exitTime);
```

A concrete subclass must implement it unless that subclass is also abstract.
This resembles a C++ pure virtual function.

## `super`

`super` refers to the parent-class portion of the current object:

```java
public CardPaymentProcessor() {
    super(PaymentMethod.CARD);
}
```

`super(...)` calls a parent constructor and must be the first constructor
statement. `super.someMethod()` calls a parent implementation. Constructors are
not inherited, so a subclass explicitly chooses how its parent is initialized.

## `@Override`

```java
@Override
public double getCost() { ... }
```

This annotation asks the compiler to verify that the method really overrides
or implements a parent method. It catches misspellings and wrong parameter
types. Use it even though Java can sometimes infer the override without it.

## Polymorphism and dynamic dispatch

```java
PaymentProcessor processor = new CardPaymentProcessor();
processor.process(payment);
```

The variable's declared type controls which members are available at compile
time. The actual object's type selects the overridden method at runtime.

## Interface or abstract class?

Choose an interface for a capability or role that unrelated classes could
implement. Choose an abstract class when subclasses need shared fields,
constructor rules, or substantial shared code.

Repository examples:

- `PaymentProcessor` is a replaceable capability.
- `ParkingSpotManager` owns shared spot state and allocation behavior.
- `CoffeeDecorator` is an abstract class because decorators share a wrapped
  `Coffee` field and constructor logic.