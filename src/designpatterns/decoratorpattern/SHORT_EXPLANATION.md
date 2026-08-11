# Decorator Pattern

The **Decorator Pattern** lets you add behavior or responsibilities to an object dynamically without modifying its original class.

In this implementation:
- `component/` contains the base contract and simple object (`Coffee`, `BasicCoffee`).
- `decorator/` contains the wrapping abstraction and add-on implementations (`CoffeeDecorator`, `MilkDecorator`, `SugarDecorator`, `WhippedCreamDecorator`).
- `example/` contains a real-world style pricing/customization example showing runtime composition.

This pattern is useful when you want flexible combinations of features like coffee add-ons, order options, notifications, or UI capabilities without creating many subclasses.
