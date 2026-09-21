# Decorator Pattern

## Start with the problem

Suppose coffee can have milk, sugar, or whipped cream. Inheritance may begin with `MilkCoffee` and `SugarCoffee`, but combinations quickly require more classes:

```text
MilkSugarCoffee
MilkWhippedCreamCoffee
SugarWhippedCreamCoffee
MilkSugarWhippedCreamCoffee
```

Every new add-on multiplies the possible subclasses. Most of those classes only combine behavior that already exists.

## The solution

Start with a `Coffee`, then wrap it with only the add-ons chosen for that order:

```java
Coffee coffee = new BasicCoffee();
coffee = new MilkDecorator(coffee);
coffee = new SugarDecorator(coffee);

System.out.println(coffee.getDescription());
System.out.println(coffee.getCost());
```

Each decorator:

- **is a `Coffee`**, so callers can use it like the original object;
- **has a `Coffee`**, so it can call the wrapped object and add its own result.

For example, `MilkDecorator.getCost()` gets the wrapped coffee's cost and adds the milk price. If that wrapped coffee is another decorator, the calls continue inward until they reach `BasicCoffee`.

## Roles in this example

- **Component:** `Coffee`, the interface shared by base objects and wrappers.
- **Concrete component:** `BasicCoffee`, the object being extended.
- **Base decorator:** `CoffeeDecorator`, which stores the wrapped `Coffee`.
- **Concrete decorators:** `MilkDecorator`, `SugarDecorator`, and `WhippedCreamDecorator`.

## When Decorator is useful

Use it when features are optional, can be combined in many ways, and may be selected at runtime. Java I/O streams use the same idea: streams wrap other streams to add buffering or data conversion.

Do not use it when there are only a few fixed combinations or when many wrapper layers would make the object difficult to understand and debug.

## Interview description

The **Decorator Pattern** adds optional behavior by wrapping an object with another object that implements the same interface. It favors runtime composition over creating a subclass for every feature combination.
