# Observable Pattern

The **Observable Pattern** (Observer Pattern) defines a one-to-many relationship between objects. When the observable object changes state, all registered observers are notified automatically.

In this implementation:
- `StocksObservable` is the source of stock price updates.
- `Observer` implementations (`IphoneObserverImpl`, `EmailAlertObserverImpl`, `NotificationAlertObserver`, `MobileAlertObserverImpl`) react to those updates.
- `Observable` keeps the publisher side decoupled from concrete consumers.

This pattern is useful when multiple outputs (email, mobile, push, device alerts) must stay synchronized with one changing value.
