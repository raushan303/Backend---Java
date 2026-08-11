# Decorator Pattern

## Interview description

The **Decorator Pattern** adds behavior to an object dynamically by wrapping it with another object that follows the same interface. It extends functionality without modifying the original class or creating many subclasses.

In this implementation:
- `component/` contains the base contract and simple object (`Coffee`, `BasicCoffee`).
- `decorator/` contains the wrapping abstraction and add-on implementations (`CoffeeDecorator`, `MilkDecorator`, `SugarDecorator`, `WhippedCreamDecorator`).
- `example/` contains a real-world style pricing/customization example showing runtime composition.

This pattern is useful when you want flexible combinations of features like coffee add-ons, order options, notifications, or UI capabilities without creating many subclasses.
