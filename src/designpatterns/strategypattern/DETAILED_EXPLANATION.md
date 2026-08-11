# Strategy Pattern

## Definition

The Strategy Pattern is a behavioral design pattern that defines a family of algorithms, puts each one in a separate class, and makes them interchangeable at runtime.

In simpler words:
- same task
- different ways to do it
- choose the behavior when needed

---

## Why Strategy Pattern exists

Without Strategy Pattern, we often write logic like this:

```java
if (paymentType.equals("CARD")) {
    ...
} else if (paymentType.equals("UPI")) {
    ...
} else if (paymentType.equals("CASH")) {
    ...
}
```

This works for small examples, but as the system grows, it becomes messy.

### Problems with this approach
- code becomes large
- adding a new behavior requires modifying existing code
- logic is harder to test separately
- the class starts handling too many responsibilities

Strategy Pattern solves this by moving each behavior into its own class.

---

## Main idea

Instead of one class holding all logic, we create:

1. **Strategy interface**
   - defines a common method

2. **Concrete strategies**
   - each class implements the method differently

3. **Context**
   - uses the strategy through the interface
   - does not know the internal implementation

---

## Example: Payment system

Suppose the system supports:
- Card payment
- UPI payment
- Cash payment

The checkout flow is same, but payment method changes.

---

## Project structure

```text
designpatterns/
  strategypattern/
    PaymentStrategy.java
    CardPayment.java
    UpiPayment.java
    CashPayment.java
    App.java
```

---

## Strategy interface

```java
package designpatterns.strategypattern;

public interface PaymentStrategy {
    void pay(int amount);
}
```

This interface defines the contract that all payment types must follow.

---

## Concrete strategies

### CardPayment

```java
package designpatterns.strategypattern;

public class CardPayment implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using Card");
    }
}
```

### UpiPayment

```java
package designpatterns.strategypattern;

public class UpiPayment implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using UPI");
    }
}
```

### CashPayment

```java
package designpatterns.strategypattern;

public class CashPayment implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using Cash");
    }
}
```

Each class implements the same method, but behavior is different.

---

## Context class

```java
package designpatterns.strategypattern;

public class App {
    private PaymentStrategy paymentStrategy;

    public App(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(int amount) {
        paymentStrategy.pay(amount);
    }

    public static void main(String[] args) {
        App app1 = new App(new CardPayment());
        app1.checkout(500);

        App app2 = new App(new UpiPayment());
        app2.checkout(300);

        App app3 = new App(new CashPayment());
        app3.checkout(100);
    }
}
```

---

## Output

```text
Paid 500 using Card
Paid 300 using UPI
Paid 100 using Cash
```

---

## Why this is Strategy Pattern

This is Strategy Pattern because:

- `PaymentStrategy` defines the contract
- each payment class is a different behavior/algorithm
- `App` uses the behavior through the interface
- behavior can be changed without changing `App`

---

## Why this is better than if-else

Without Strategy, you may write:

```java
if (paymentType.equals("CARD")) {
    ...
} else if (paymentType.equals("UPI")) {
    ...
} else if (paymentType.equals("CASH")) {
    ...
}
```

### Problems with this approach
- class becomes large
- new payment types require changing existing code
- each behavior is harder to test independently
- logic is tightly coupled
- violates Open/Closed Principle

### With Strategy
- each behavior is isolated
- new payment types can be added as new classes
- existing code does not need to change
- behavior can be selected at runtime

---

## Principles followed by Strategy Pattern

### 1. Single Responsibility Principle
Each class has one job:
- `CardPayment` handles card payment only
- `UpiPayment` handles UPI payment only
- `CashPayment` handles cash payment only
- `App` coordinates checkout only

### 2. Open/Closed Principle
You can add a new payment strategy like `WalletPayment` without changing the existing `App` class.

### 3. Dependency Inversion Principle
`App` depends on the abstraction:

```java
PaymentStrategy
```

not on concrete classes directly.

This makes the code flexible.

---

## If Strategy is not used, what gets violated?

### Open/Closed Principle
The checkout class must be changed every time a new payment type is added.

### Single Responsibility Principle
One class ends up doing too many things.

### Dependency Inversion Principle
The class depends on concrete behavior instead of abstraction.

---

## What if two strategies have the same `pay()` logic?

Yes, duplicate code can happen.

This is a real design concern.

Example:
- `VisaPayment`
- `MasterCardPayment`
- `AmexPayment`

Maybe two or more have the same transaction flow but different extra functionality.

That does **not** mean Strategy is wrong.

### Important point
The Strategy Pattern is about:
- interchangeable behavior
- runtime selection
- abstraction over implementation

It is **not** about guaranteeing that no two strategy classes will ever share code.

So if strategies are similar, you can handle it in several ways.

---

## Option 1: extract common code into a base class

If the shared logic is truly common, move it into an abstract class.

### Base class

```java
package designpatterns.strategypattern;

public abstract class CreditCardPaymentBase implements PaymentStrategy {
    protected void validateCard() {
        System.out.println("Validating card...");
    }

    protected void processTransaction(int amount) {
        System.out.println("Processing credit card transaction of " + amount);
    }
}
```

### VisaPayment

```java
package designpatterns.strategypattern;

public class VisaPayment extends CreditCardPaymentBase {
    @Override
    public void pay(int amount) {
        validateCard();
        processTransaction(amount);
        System.out.println("Paid " + amount + " using Visa");
    }
}
```

### MasterCardPayment

```java
package designpatterns.strategypattern;

public class MasterCardPayment extends CreditCardPaymentBase {
    @Override
    public void pay(int amount) {
        validateCard();
        processTransaction(amount);
        System.out.println("Paid " + amount + " using MasterCard");
    }
}
```

This avoids repeating the shared flow.

---

## Option 2: use helper classes / composition

If only part of the logic is common, extract it into helper classes.

### Helper example

```java
package designpatterns.strategypattern;

public class PaymentValidator {
    public void validate() {
        System.out.println("Validating payment...");
    }
}
```

Then a strategy can use the helper:

```java
package designpatterns.strategypattern;

public class VisaPayment implements PaymentStrategy {
    private final PaymentValidator validator = new PaymentValidator();

    @Override
    public void pay(int amount) {
        validator.validate();
        System.out.println("Paid " + amount + " using Visa");
    }
}
```

This keeps strategies focused on their own behavior.

---

## Option 3: split responsibilities into different interfaces

Sometimes one interface is too broad.

For example, not every class needs to support all of these:
- payment
- refund
- recurring payment
- invoice generation

In such cases, use different interfaces like:
- `PaymentStrategy`
- `RefundStrategy`
- `RecurringPaymentStrategy`

This prevents forcing unrelated classes to implement unnecessary methods.

---

## Does this still count as Strategy Pattern?

Yes.

It still fits Strategy if:
- you are selecting among interchangeable algorithms
- the client only depends on the interface
- each implementation is one way of doing the same task

Shared logic does not break the pattern.

You just need to structure the common parts well.

---

## Example where this is still Strategy

Suppose you have:
- `VisaPayment`
- `MasterCardPayment`
- `CorporateCardPayment`

All three:
- authorize payment similarly
- process amount similarly

But:
- `CorporateCardPayment` has extra invoice handling
- `VisaPayment` has reward points
- `MasterCardPayment` has international fee rules

This still works well with Strategy.

You can:
- keep the shared flow in a base class
- keep the strategy interface for payment behavior
- add specialized logic in each concrete class

---

## Another example where Strategy is useful

### Sorting
Different sorting behaviors:
- ascending
- descending
- sort by name
- sort by date

### Logging
Different logging behaviors:
- console logger
- file logger
- database logger

### Compression
Different compression behaviors:
- zip
- gzip
- lz4

### Routing / Shipping
Different route choices:
- fast delivery
- cheap delivery
- safe delivery

These are all Strategy use cases because the task is same, but the behavior changes.

---

## When Strategy is not the best choice

Do not use Strategy when:
- the problem is too simple
- there is only one behavior
- the behaviors are not really interchangeable
- the extra classes would make the design harder to understand
- you are over-engineering

In such cases, a simple method may be enough.

---

## Summary

Strategy Pattern:
- defines a family of algorithms
- encapsulates each one in a separate class
- makes them interchangeable
- lets the client choose behavior at runtime

### It helps:
- remove if-else chains
- support OCP
- improve maintainability
- make code easier to test
- reduce coupling

### About duplicate logic:
If strategies share code:
- extract common behavior into a base class
- or use helper classes / composition
- or split interfaces if responsibilities differ

So yes, shared logic and duplication concerns are normal in real-world Strategy usage.

---

## Interview one-liner

**Strategy Pattern defines a family of algorithms, encapsulates each one, and makes them interchangeable so the client can choose behavior at runtime without changing the consuming code.**