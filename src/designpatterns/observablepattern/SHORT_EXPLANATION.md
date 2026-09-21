# Observable Pattern

This pattern is more commonly called the **Observer Pattern**. An object that is being watched is often called the observable, subject, or publisher.

## Start with the problem

Suppose a stock-price class sends several alerts directly:

```java
void setStockPrice(double newPrice) {
	stockPrice = newPrice;
	emailService.send(newPrice);
	mobileApp.refresh(newPrice);
	pushService.notify(newPrice);
}
```

The stock class now depends on every notification channel. Adding SMS requires changing the stock class, even though stock-price logic itself did not change.

## The solution

Observers register themselves with the observable:

```java
StocksObservable stocks = new StocksObservable();

stocks.addObserver(new EmailAlertObserverImpl(stocks));
stocks.addObserver(new MobileAlertObserverImpl(stocks));
stocks.setStockPrice(199.99);
```

When the price changes, `StocksObservable` calls `update()` on every registered `Observer`. It knows the common interface, not the details of email or mobile alerts.

This creates a **one-to-many relationship**: one publisher can notify many subscribers.

## Roles in this example

- **Observable interface:** operations for registering, removing, and notifying observers.
- **Concrete observable:** `StocksObservable`, which owns the price and observer list.
- **Observer interface:** the `update()` operation.
- **Concrete observers:** email, mobile, push, and iPhone alert implementations.

## Pull versus push

In this implementation, `update()` has no price parameter. Each observer keeps a reference to `StockPriceObservable` and **pulls** the current value by calling `getStockPrice()`. Another design could **push** the new price as an argument to `update(newPrice)`.

## Observer versus a message broker

This example is in-memory Observer: the publisher directly calls observer objects in the same application. Kafka, RabbitMQ, or an event bus can provide similar publish/subscribe behavior across processes, but adds message delivery, storage, and failure concerns.

## When Observer is useful

Use it when a changing object should notify a dynamic list of independent listeners. Remember to support removal of observers; otherwise long-lived publishers can retain unused listeners.

## Interview description

The **Observer Pattern** defines a one-to-many dependency. When a publisher changes, it notifies all registered observers through a common interface, so new observer types can be added without changing the publisher.
