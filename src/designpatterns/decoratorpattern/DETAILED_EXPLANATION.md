# Decorator Pattern - Detailed Guide

## 1) What problem it solves

The Decorator Pattern is used when you want to add responsibilities to an object at runtime without changing its original implementation.

Without this pattern, each combination often leads to subclass explosion such as `MilkCoffee`, `MilkSugarCoffee`, `MilkSugarWhippedCreamCoffee`, and many more.

With this pattern, each add-on is wrapped around the same base abstraction, so behavior can be composed dynamically.

```java
Coffee coffee = new SugarDecorator(
   new MilkDecorator(new BasicCoffee()));
```

Read this from the inside out: make a basic coffee, wrap it with milk, then wrap that result with sugar. The final object is still a `Coffee`.

---

## 2) Problem statement

Design a coffee ordering flow where the same base coffee can be enhanced with optional add-ons like milk, sugar, and whipped cream.

The solution should:

- keep the base component simple
- allow multiple add-ons in any order
- avoid creating separate subclasses for every combination
- support runtime composition based on customer choices

---

## 3) Core participants in this folder

- `Coffee` - Component contract (`getDescription`, `getCost`)
- `BasicCoffee` - Concrete component
- `CoffeeDecorator` - Base decorator that wraps another `Coffee`
- `MilkDecorator` - Concrete decorator that adds milk
- `SugarDecorator` - Concrete decorator that adds sugar
- `WhippedCreamDecorator` - Concrete decorator that adds whipped cream
- `DecoratorPatternDemo` - Small runner that shows dynamic composition

### Folder grouping

```text
decoratorpattern/
├── component/
│   ├── Coffee.java
│   └── BasicCoffee.java
├── decorator/
│   ├── CoffeeDecorator.java
│   ├── MilkDecorator.java
│   ├── SugarDecorator.java
│   └── WhippedCreamDecorator.java
├── example/
│   ├── OrderPricingDecoratorExample.java
│   └── README.md
├── DecoratorPatternDemo.java
├── README.md
├── SHORT_EXPLANATION.md
└── DETAILED_EXPLANATION.md
```

---

## 4) Class diagram (text)

```text
                    +------------------+
                    |      Coffee      |
                    +------------------+
                    | +getDescription()|
                    | +getCost()       |
                    +--------^---------+
                             |
               +-------------+-------------+
               |                           |
      +-------------------+      +----------------------+
      |    BasicCoffee    |      |   CoffeeDecorator    |
      +-------------------+      +----------------------+
                                 | -coffee: Coffee      |
                                 +----------^-----------+
                                            |
             +------------------------------+-----------------------------+
             |                              |                             |
   +--------------------+         +--------------------+      +-------------------------+
   |   MilkDecorator    |         |   SugarDecorator   |      | WhippedCreamDecorator   |
   +--------------------+         +--------------------+      +-------------------------+
```

---

## 5) Sequence flow

```text
1. Client creates a BasicCoffee object.
2. Client decides which features are needed at runtime.
3. Client wraps the base object with one or more decorators.
4. Each decorator forwards existing behavior to the wrapped object.
5. Each decorator adds its own description and extra cost.
6. Final client call to getDescription() / getCost() returns the combined result.
```

For `getCost()`, the outer decorator asks its wrapped object for a cost and adds its own amount. The call travels inward to `BasicCoffee`; the accumulated result travels back outward. This delegation is why each add-on only needs to know its own price.

---

## 6) Error handling choices

This implementation includes defensive checks:

- Rejects `null` wrapped objects in `CoffeeDecorator`
- Keeps decorators small and focused on one responsibility
- Preserves the same `Coffee` abstraction across all wrappers

These checks help prevent invalid composition and keep runtime decoration predictable.

---

## 7) Real-world use cases

- Coffee or pizza customization
- E-commerce order options (gift wrap, insurance, priority shipping)
- Notification systems with optional channels
- Input/output streams in Java
- UI components with scrollbars, borders, or theming layers

---

## 8) Benefits

- Follows Open/Closed Principle
- Avoids subclass explosion
- Supports dynamic feature composition
- Keeps each behavior isolated and reusable

---

## 9) Best practices

1. Keep the component contract small and stable.
2. Keep each decorator responsible for one behavior.
3. Validate wrapped dependencies before composing.
4. Prefer composition at runtime over many inheritance combinations.
5. Use decorators when features are optional and combinable.

### Decorator compared with nearby ideas

- **Subclassing:** fixes behavior in a class definition; Decorator combines behavior per object at runtime.
- **Builder:** helps construct a final object step by step; Decorator produces wrappers that continue to share the component interface.
- **Adapter:** wraps an object to expose a different interface; Decorator preserves the same interface and adds behavior.

---

## 10) Real-world example in `example/`

The `example/OrderPricingDecoratorExample.java` file shows how the same pattern can be used for an online order.

- `StandardOrder` acts as the base component
- `OrderOptionDecorator` acts as the abstract decorator
- `GiftWrapDecorator`, `PriorityShippingDecorator`, and `PurchaseProtectionDecorator` add optional behavior

This demonstrates why decoration is useful in business systems where options are chosen dynamically per order.
