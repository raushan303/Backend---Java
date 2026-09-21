# Decorator Pattern

This README explains the core coffee example from the problem it solves.

## Why not use subclasses?

With three optional add-ons, inheritance can require classes such as `MilkCoffee`, `SugarCoffee`, `MilkSugarCoffee`, and `MilkSugarWhippedCreamCoffee`. Adding a fourth option creates even more combinations.

Decorator represents each option once and combines the selected objects at runtime:

```java
Coffee coffee = new WhippedCreamDecorator(
   new SugarDecorator(
      new MilkDecorator(new BasicCoffee())));
```

Reading from the inside out: create basic coffee, add milk, add sugar, then add whipped cream.

## Q1) What relation does `MilkDecorator` have with `Coffee`?

**Answer:** `MilkDecorator` has both relations:

- **is-a `Coffee`** because `MilkDecorator` extends `CoffeeDecorator`, and `CoffeeDecorator` implements `Coffee`
- **has-a `Coffee`** because the decorator stores a wrapped `Coffee` object in the `coffee` field

This combination is what makes the Decorator Pattern work:

- **is-a** lets the decorator be used anywhere a `Coffee` is expected
- **has-a** lets the decorator reuse the wrapped object's behavior and add more behavior on top

Inheritance provides the common type; composition provides the flexible combination.

---

## Q2) Why does the decorator implement `Coffee` if it already has the same methods?

**Answer:** The decorator implements `Coffee` so the base object and decorated object follow the same contract.

That means client code can treat both of these in the same way:

- `Coffee coffee = new BasicCoffee()`
- `Coffee coffee = new MilkDecorator(new BasicCoffee())`

This is not useless duplication:

- `BasicCoffee` provides the base behavior
- `MilkDecorator` delegates to the wrapped `Coffee`
- `MilkDecorator` then adds extra behavior such as extra description and extra cost

Because both objects share the same interface, decorators remain interchangeable with the base component.

## Q3) How does a method call move through the wrappers?

For `coffee.getCost()`, the outer decorator calls `getCost()` on the object it wraps and adds its own price. That call repeats through each decorator until `BasicCoffee` returns the starting cost. The results are then added while the calls return outward.

## Q4) When is Decorator a poor fit?

Avoid it when options cannot be meaningfully combined or when callers must frequently inspect the exact concrete type. A long wrapper chain is flexible, but it can also be harder to debug than a simple object.

---

## Class diagram for the coffee decorators

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
