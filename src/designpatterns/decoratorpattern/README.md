# Decorator Pattern

This README gives a quick explanation of the core coffee-based Decorator Pattern example in this folder.

## Q1) What relation does `MilkDecorator` have with `Coffee`?

**Answer:** `MilkDecorator` has both relations:

- **is-a `Coffee`** because `MilkDecorator` extends `CoffeeDecorator`, and `CoffeeDecorator` implements `Coffee`
- **has-a `Coffee`** because the decorator stores a wrapped `Coffee` object in the `coffee` field

This combination is what makes the Decorator Pattern work:

- **is-a** lets the decorator be used anywhere a `Coffee` is expected
- **has-a** lets the decorator reuse the wrapped object's behavior and add more behavior on top

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
