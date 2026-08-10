# Observable Pattern

The **Observable Pattern** (Observer Pattern) defines a one-to-many relationship between objects. When the observable object changes state, all registered observers are notified automatically.

In this implementation:
- `observable/` contains publisher-side contracts and implementation (`Observable`, `StockPriceObservable`, `StocksObservable`).
- `observer/` contains subscriber-side contract and implementations (`Observer`, `IphoneObserverImpl`, `EmailAlertObserverImpl`, `NotificationAlertObserver`, `MobileAlertObserverImpl`).
- This keeps publisher and subscriber concerns clearly separated.

This pattern is useful when multiple outputs (email, mobile, push, device alerts) must stay synchronized with one changing value.
