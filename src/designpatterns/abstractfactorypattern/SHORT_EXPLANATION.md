# Abstract Factory Pattern

## Start with the problem

Suppose a furniture store displays one chair and one sofa. Creating each object separately allows an invalid combination:

```java
Chair chair = new ModernChair();
Sofa sofa = new VictorianSofa(); // The styles do not match.
```

The objects work individually, but the application has failed to keep the selected family consistent.

## The solution

Choose one factory for the whole family:

```java
FurnitureFactory factory = new ModernFurnitureFactory();

Chair chair = factory.createChair();
Sofa sofa = factory.createSofa();
```

`ModernFurnitureFactory` creates only Modern products. Replacing it with `VictorianFurnitureFactory` changes both products together, while the store code continues to use `Chair` and `Sofa` interfaces.

## Why is it called "abstract" factory?

The client depends on the `FurnitureFactory` interface, not a particular factory implementation. Each concrete factory represents one product family.

## Factory versus Abstract Factory

- **Factory:** choose one product, such as a `Car`, `Bike`, or `Truck`.
- **Abstract Factory:** choose a family, then create several matching product types, such as a Chair and Sofa.

## Roles in this example

- **Abstract products:** `Chair` and `Sofa`.
- **Product families:** Modern products and Victorian products.
- **Abstract factory:** `FurnitureFactory`.
- **Concrete factories:** `ModernFurnitureFactory` and `VictorianFurnitureFactory`.
- **Client:** `FurnitureStore`, which only knows the interfaces.

## Tradeoff

Adding a new family is straightforward: add another concrete factory. Adding a new product type, such as `Table`, is more expensive because `FurnitureFactory` and every existing concrete factory must gain a `createTable()` method.

## Interview description

The **Abstract Factory Pattern** creates several related objects through one factory interface. Selecting a concrete factory selects a complete compatible product family without exposing concrete product classes to the client.