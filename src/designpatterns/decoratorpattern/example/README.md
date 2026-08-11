# Real-World Order Pricing Example

This folder shows how the **Decorator Pattern** can be used in an e-commerce or ordering system where customers choose optional services at runtime.

## Files

- `OrderPricingDecoratorExample.java` - runnable example of an order enhanced with gift wrap, priority shipping, and purchase protection.

---

## Why decoration helps here

In real systems, every order does not need the same extra features.

Examples:

- some customers want only gift wrap
- some want gift wrap + fast delivery
- some want all options together

If we used inheritance alone, we would need many classes for every combination.

With decorators:

- start with `StandardOrder`
- wrap it with only the options selected for that order
- calculate final summary and total price dynamically

---

## Mapping to decorator concepts

- `Order` -> Component
- `StandardOrder` -> Concrete Component
- `OrderOptionDecorator` -> Abstract Decorator
- `GiftWrapDecorator`, `PriorityShippingDecorator`, `PurchaseProtectionDecorator` -> Concrete Decorators

---

## Core decorator Q&A

### Q1) What relation does `MilkDecorator` have with `Coffee`?

**Answer:** `MilkDecorator` has both relations:

- **is-a `Coffee`** because `MilkDecorator` extends `CoffeeDecorator`, and `CoffeeDecorator` implements `Coffee`
- **has-a `Coffee`** because the decorator stores a wrapped `Coffee` object in the `coffee` field

This combination is the key idea of the Decorator Pattern:

- **is-a** lets the decorator be used anywhere a `Coffee` is expected
- **has-a** lets the decorator reuse the wrapped object's behavior and add extra behavior on top

### Q2) Why does the decorator implement `Coffee` if it already has the same methods?

**Answer:** It implements `Coffee` so the decorated object and the base object share the same contract.

That means client code can write:

- `Coffee coffee = new BasicCoffee()`
- `Coffee coffee = new MilkDecorator(new BasicCoffee())`

without changing how it uses the object.

This is not useless duplication:

- `BasicCoffee` provides the base behavior
- `MilkDecorator` calls the wrapped `Coffee` and then adds its own behavior
- both follow the same `Coffee` contract, so they are interchangeable from the client's point of view

---

## Class diagram for core coffee decorators

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

## Runtime flow

```text
1. Create a base order with base amount.
2. Choose optional services based on user request.
3. Wrap the base order with decorators.
4. Ask the final wrapped object for summary and total cost.
5. Final result includes all selected options without changing the base class.
```

This is a practical example of dynamic behavior composition in a business workflow.
