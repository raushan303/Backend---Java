# SOLID Principles

SOLID is a set of five object-oriented design principles that help you write code that is easier to maintain, extend, test, and understand.

The five principles are:

- **S** — Single Responsibility Principle
- **O** — Open/Closed Principle
- **L** — Liskov Substitution Principle
- **I** — Interface Segregation Principle
- **D** — Dependency Inversion Principle

## Interview-ready descriptions

- **Single Responsibility Principle (SRP):** A class should have one clear responsibility and only one reason to change.
- **Open/Closed Principle (OCP):** Code should allow new behavior to be added without changing existing, tested behavior.
- **Liskov Substitution Principle (LSP):** A child class should work correctly anywhere its parent type is expected without breaking the program.
- **Interface Segregation Principle (ISP):** Classes should depend only on small, focused interfaces that contain the methods they actually need.
- **Dependency Inversion Principle (DIP):** High-level and low-level classes should depend on abstractions, such as interfaces, instead of depending directly on each other.

---

# Why SOLID matters

Without SOLID, code often becomes:
- tightly coupled
- hard to change
- hard to test
- difficult to reuse
- full of large classes doing too many things

SOLID helps you design code that:
- changes safely
- scales better
- is easier to reason about
- supports clean architecture

---

# 1. Single Responsibility Principle (SRP)

## Definition

A class should have **only one reason to change**.

That means a class should do **one job**.

---

## Bad example

```java
public class Invoice {
    public void calculateTotal() {
        System.out.println("Calculating total");
    }

    public void saveToDatabase() {
        System.out.println("Saving invoice to database");
    }

    public void printInvoice() {
        System.out.println("Printing invoice");
    }
}
```

This class is doing too many things:
- calculation
- database saving
- printing

That means it has multiple responsibilities.

---

## Why this is bad

If printing changes, you must modify the same class.
If database logic changes, you must modify the same class.
If calculation changes, you must modify the same class.

This makes the class harder to maintain.

---

## Better example

```java
public class InvoiceCalculator {
    public void calculateTotal() {
        System.out.println("Calculating total");
    }
}
```

```java
public class InvoiceRepository {
    public void saveToDatabase() {
        System.out.println("Saving invoice to database");
    }
}
```

```java
public class InvoicePrinter {
    public void printInvoice() {
        System.out.println("Printing invoice");
    }
}
```

Now each class has one job.

---

## What SRP gives you

- easier maintenance
- easier testing
- smaller classes
- cleaner code

---

## Interview line for SRP

**A class should have only one reason to change.**

---

# 2. Open/Closed Principle (OCP)

## Definition

Software entities should be:
- **open for extension**
- **closed for modification**

That means you should be able to add new behavior without changing existing code.

---

## Bad example

```java
public class DiscountCalculator {
    public double calculateDiscount(String customerType, double amount) {
        if (customerType.equals("REGULAR")) {
            return amount * 0.1;
        } else if (customerType.equals("PREMIUM")) {
            return amount * 0.2;
        }
        return 0;
    }
}
```

If you add a new customer type, you must modify this class.

---

## Why this is bad

Every new rule changes old code.
That increases the chance of breaking something.

---

## Better example

Use an interface:

```java
public interface DiscountStrategy {
    double calculateDiscount(double amount);
}
```

```java
public class RegularDiscount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.1;
    }
}
```

```java
public class PremiumDiscount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.2;
    }
}
```

```java
public class DiscountService {
    private final DiscountStrategy discountStrategy;

    public DiscountService(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public double getDiscount(double amount) {
        return discountStrategy.calculateDiscount(amount);
    }
}
```

Now adding a new discount type only needs a new class.

---

## What OCP gives you

- safer changes
- easier extension
- better code organization
- less risk of breaking existing behavior

---

## Interview line for OCP

**Classes should be open for extension but closed for modification.**

---

# 3. Liskov Substitution Principle (LSP)

## Definition

If class B is a subtype of class A, then objects of class A should be replaceable with objects of class B without breaking the program.

In simple words:
- a child class should work wherever the parent class is expected

---

## Bad example

```java
public class Bird {
    public void fly() {
        System.out.println("Bird is flying");
    }
}
```

```java
public class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins cannot fly");
    }
}
```

This violates LSP.

Why?
Because `Penguin` is a `Bird`, but it cannot behave like a flying bird.

---

## Why this is bad

If code expects a `Bird` and calls `fly()`, it should work.
But with `Penguin`, it fails.

That means the subtype cannot fully substitute the parent type.

---

## Better example

Split the behavior:

```java
public class Bird {
    public void eat() {
        System.out.println("Bird is eating");
    }
}
```

```java
public class FlyingBird extends Bird {
    public void fly() {
        System.out.println("Flying bird is flying");
    }
}
```

```java
public class Penguin extends Bird {
    // Penguin only has Bird behavior like eat()
}
```

Now only flying birds have the `fly()` behavior.

---

## What LSP gives you

- safer inheritance
- fewer runtime surprises
- cleaner hierarchy
- better substitutability

---

## Interview line for LSP

**Subclasses must be substitutable for their base classes without breaking the program.**

---

# 4. Interface Segregation Principle (ISP)

## Definition

Clients should not be forced to depend on interfaces they do not use.

In simple words:
- prefer small, focused interfaces
- avoid large “fat” interfaces

---

## Bad example

```java
public interface Worker {
    void work();
    void eat();
}
```

Now a robot may implement this interface.

```java
public class RobotWorker implements Worker {
    @Override
    public void work() {
        System.out.println("Robot is working");
    }

    @Override
    public void eat() {
        throw new UnsupportedOperationException("Robot does not eat");
    }
}
```

This is not good design.

---

## Why this is bad

The robot is forced to implement a method it does not need.

That means the interface is too broad.

---

## Better example

Split the interface:

```java
public interface Workable {
    void work();
}
```

```java
public interface Eatable {
    void eat();
}
```

Now:

```java
public class HumanWorker implements Workable, Eatable {
    @Override
    public void work() {
        System.out.println("Human is working");
    }

    @Override
    public void eat() {
        System.out.println("Human is eating");
    }
}
```

```java
public class RobotWorker implements Workable {
    @Override
    public void work() {
        System.out.println("Robot is working");
    }
}
```

Now each class only depends on what it actually needs.

---

## What ISP gives you

- smaller interfaces
- less unnecessary implementation
- cleaner code
- easier maintenance

---

## Interview line for ISP

**No client should be forced to depend on methods it does not use.**

---

# 5. Dependency Inversion Principle (DIP)

## Definition

High-level modules should not depend on low-level modules.
Both should depend on abstractions.

Also:
- abstractions should not depend on details
- details should depend on abstractions

---

## Bad example

```java
public class MySQLDatabase {
    public void save() {
        System.out.println("Saving to MySQL");
    }
}
```

```java
public class OrderService {
    private MySQLDatabase mySQLDatabase = new MySQLDatabase();

    public void placeOrder() {
        mySQLDatabase.save();
    }
}
```

This is tightly coupled.

`OrderService` depends directly on `MySQLDatabase`.

---

## Why this is bad

If you want to change MySQL to PostgreSQL:
- you must change `OrderService`

That violates DIP.

---

## Better example

Create an abstraction:

```java
public interface OrderRepository {
    void save();
}
```

Then implement it:

```java
public class MySQLOrderRepository implements OrderRepository {
    @Override
    public void save() {
        System.out.println("Saving order to MySQL");
    }
}
```

```java
public class PostgreSQLOrderRepository implements OrderRepository {
    @Override
    public void save() {
        System.out.println("Saving order to PostgreSQL");
    }
}
```

Now the service depends on the interface:

```java
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void placeOrder() {
        orderRepository.save();
    }
}
```

Now the service is not tied to a specific database.

---

## What DIP gives you

- loose coupling
- easy swapping of implementations
- easier unit testing
- cleaner architecture

---

## Interview line for DIP

**High-level modules should depend on abstractions, not on concrete implementations.**

---

# How SOLID fits together

SOLID is not five separate tricks.
They work together to make your code:
- easier to extend
- easier to test
- easier to maintain
- less coupled
- more flexible

---

# Quick summary

## SRP
One class = one responsibility.

## OCP
Add new behavior without modifying existing code.

## LSP
Child classes must be substitutable for parent classes.

## ISP
Keep interfaces small and focused.

## DIP
Depend on abstractions, not concrete implementations.

---

# Real-world examples

## SRP
- invoice calculator
- invoice printer
- invoice repository

## OCP
- payment strategies
- discount strategies
- sorting strategies

## LSP
- bird hierarchy done correctly
- shape hierarchy done correctly

## ISP
- small service interfaces
- role-based interfaces

## DIP
- repository abstraction
- service layer depending on interfaces
- dependency injection

---

# Important note

A lot of design patterns and clean architecture practices exist to help you follow SOLID better.

For example:
- Strategy Pattern supports OCP and DIP
- Factory Pattern helps with DIP
- small interfaces support ISP
- good inheritance design supports LSP
- separated classes support SRP

---

# Final interview-ready summary

SOLID is a set of five object-oriented principles that help you write clean, maintainable, and flexible code:

- **S**: Single Responsibility Principle
- **O**: Open/Closed Principle
- **L**: Liskov Substitution Principle
- **I**: Interface Segregation Principle
- **D**: Dependency Inversion Principle

Together, they help reduce coupling, improve readability, and make software easier to grow.