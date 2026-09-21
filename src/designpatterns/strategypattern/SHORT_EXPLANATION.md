# Strategy Pattern

## Start with the problem

Imagine that checkout supports card, UPI, and cash payments. A first attempt may put every payment rule in one method:

```java
void checkout(String paymentType, int amount) {
   if (paymentType.equals("CARD")) {
      // validate card and charge it
   } else if (paymentType.equals("UPI")) {
      // validate UPI ID and request payment
   } else if (paymentType.equals("CASH")) {
      // record a cash payment
   }
}
```

This works at first, but the checkout class now knows the details of every payment method. Adding net banking means editing the same method again. Testing one payment method also means going through this large conditional.

The part that changes is **how the payment is performed**. The rest of checkout does not need to change.

## Why not put the behavior in a parent class?

Inheritance is useful when behavior is truly shared by every child. It becomes awkward when only some children need a behavior or when each child performs it differently.

For example, suppose `CardPayment`, `UpiPayment`, and `CashPayment` are possible behaviors used by several kinds of checkout. Making a separate checkout subclass for every payment method would duplicate the normal checkout code. Putting card or UPI rules in a general checkout parent would give that parent responsibilities that do not apply to every child.

Strategy uses **composition** instead: checkout receives a small object whose only job is to perform payment.

## The solution

First, define the operation that every payment method must support:

```java
public interface PaymentStrategy {
   void pay(int amount);
}
```

Each payment method implements that operation in its own class:

```java
public class UpiPayment implements PaymentStrategy {
   @Override
   public void pay(int amount) {
      System.out.println("Paid " + amount + " using UPI");
   }
}
```

The context delegates payment instead of deciding how payment works:

```java
public class App {
   private final PaymentStrategy paymentStrategy;

   public App(PaymentStrategy paymentStrategy) {
      this.paymentStrategy = paymentStrategy;
   }

   public void checkout(int amount) {
      paymentStrategy.pay(amount);
   }
}
```

The caller chooses the behavior:

```java
App cardCheckout = new App(new CardPayment());
cardCheckout.checkout(1_000);

App upiCheckout = new App(new UpiPayment());
upiCheckout.checkout(500);
```

`App` does not change when another payment strategy is added. This is the main benefit: behavior that varies is kept outside the class that uses it.

## Roles in this example

- **Strategy:** `PaymentStrategy`, the common operation.
- **Concrete strategies:** `CardPayment`, `UpiPayment`, and `CashPayment`.
- **Context:** `App`, which holds and uses a `PaymentStrategy`.
- **Client:** the code that selects a strategy and passes it to `App`.

## When Strategy is a good fit

Use it when:

- one task has several meaningful implementations, such as paying, sorting, compressing, or calculating a discount;
- a class contains a growing conditional that selects one of those implementations;
- behavior must be selected or replaced at runtime;
- only some classes need a behavior, so placing it in a shared parent would be misleading.

Do not add Strategy for one tiny behavior that is unlikely to change. An extra interface and class should remove real branching, duplication, or coupling.

## Interview description

The **Strategy Pattern** represents interchangeable implementations behind one interface. A context receives the strategy it should use and delegates the work to it, avoiding hardcoded conditionals and inappropriate inheritance.