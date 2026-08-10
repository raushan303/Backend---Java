# Observable Pattern - Detailed Guide

## 1) What problem it solves

The Observable Pattern is used when one object (publisher) changes state and many other objects (subscribers) need to react.

Without this pattern, the publisher often depends directly on multiple channels (`if-else` blocks for email, mobile, push, etc.), making code rigid.

With this pattern, subscribers register once, and the publisher only notifies through an abstraction.

---

## 2) Core participants in this folder

- `Observable` - Publisher contract (`addObserver`, `removeObserver`, `notifyObservers`)
- `StockPriceObservable` - Stock-specific extension contract (`setStockPrice`, `getStockPrice`)
- `Observer` - Subscriber contract (`update`)
- `StocksObservable` - Concrete publisher that stores observers and broadcasts stock price changes
- `IphoneObserverImpl` - iPhone display alert subscriber
- `EmailAlertObserverImpl` - Email alert subscriber
- `NotificationAlertObserver` - Push notification subscriber
- `MobileAlertObserverImpl` - Mobile alert subscriber

### Folder grouping

```text
observablepattern/
├── observable/
│   ├── Observable.java
│   ├── StockPriceObservable.java
│   └── StocksObservable.java
├── observer/
│   ├── Observer.java
│   ├── IphoneObserverImpl.java
│   ├── EmailAlertObserverImpl.java
│   ├── NotificationAlertObserver.java
│   └── MobileAlertObserverImpl.java
├── SHORT_EXPLANATION.md
└── DETAILED_EXPLANATION.md
```

---

## 3) Class diagram (text)

```text
                      +------------------+
                      |    Observable    |
                      +------------------+
                      | +addObserver()   |
                      | +removeObserver()|
                      | +notifyObservers()|
                      +---------^--------+
                                |
                                |
                    +----------------------+
                    | StockPriceObservable |
                    +----------^-----------+
                               |
                               |
                      +------------------+
                      | StocksObservable |
                      +------------------+
                      | -observers       |
                      | -stockPrice      |
                      | +setStockPrice() |
                      | +getStockPrice() |
                      +---------+--------+
                                |
                        notifies| 
                                v
                       +----------------+
                       |    Observer    |
                       +----------------+
                       | +update()      |
                       +-------^--------+
                               |
      +------------------------+------------------------+
      |                        |                        |
+--------------------+ +----------------------+ +---------------------------+
| IphoneObserverImpl | | EmailAlertObserverImpl| | NotificationAlertObserver |
+--------------------+ +----------------------+ +---------------------------+
                               |
                      +-----------------------+
                      | MobileAlertObserverImpl|
                      +-----------------------+
```

---

## 4) Sequence flow

```text
1. Client creates StocksObservable.
2. Client creates observer objects and registers them via addObserver().
3. Client calls setStockPrice(newPrice).
4. StocksObservable validates and stores the new value.
5. StocksObservable calls notifyObservers().
6. Each observer receives update() and reads current price via getStockPrice().
```

---

## 5) Error handling choices

This implementation includes defensive checks:

- Rejects `null` observers in `addObserver` / `removeObserver`
- Rejects `null` constructor parameters for observer implementations
- Rejects negative stock price values
- Avoids duplicate observer registration
- Notifies observers only when the price actually changes

These checks prevent invalid state and unnecessary notification noise.

---

## 6) Real-world use cases

- Inventory restock alerts
- Price tick broadcasts in trading systems
- User presence/status updates
- Event bus style notifications in apps
- Multi-channel alert systems (email/SMS/push)

---

## 7) Benefits

- Loose coupling between publisher and subscribers
- Easy extensibility (add new observer type without changing `StocksObservable` logic)
- Better maintainability and testability
- Runtime flexibility (register/remove observers dynamically)

---

## 8) Best practices

1. Depend on abstractions (`Observable`, `Observer`) rather than concrete classes.
2. Keep observer `update()` lightweight.
3. Validate incoming data before notify.
4. Prevent duplicate registration unless duplicates are explicitly needed.
5. Provide unsubscribe support to avoid stale listeners.

---

## 9) Extension ideas

- Add thresholds per observer (notify only above/below certain price)
- Include previous/new price in update payload
- Persist observer metadata
- Add asynchronous notification dispatch

The current design already supports these enhancements while keeping existing contracts clear.
