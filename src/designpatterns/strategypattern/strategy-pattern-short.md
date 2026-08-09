# Strategy Pattern

## Intent

The **Strategy Pattern** lets you define a family of algorithms, put each one in a separate class, and make them interchangeable at runtime.

It helps you avoid:
- large `if-else` blocks
- hardcoded behavior
- tightly coupled code

Instead, the behavior is moved into separate strategy classes.

---

## When to use it

Use Strategy when:
- you have multiple ways to do the same thing
- the algorithm can vary
- you want to switch behavior at runtime
- you want to follow the Open/Closed Principle

---

## Simple idea

Think of payment:

- Card payment
- UPI payment
- Cash payment

The checkout process stays the same, but the payment method changes.

---

## Core components

A Strategy Pattern usually has:

1. **Strategy interface**
   - defines a common operation

2. **Concrete strategies**
   - different implementations of the operation

3. **Context**
   - uses a strategy object
   - does not know the internal details

---

## Example: payment system

### Strategy interface

```java
package designpatterns.strategypattern;

public interface PaymentStrategy {
    void pay(int amount);
}