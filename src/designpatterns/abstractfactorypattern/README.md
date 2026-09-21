# Abstract Factory Pattern

This example creates matching Modern or Victorian furniture without coupling the store to concrete products.

## See the problem first

If the store chooses every item independently, it can accidentally mix families:

```java
Chair chair = new ModernChair();
Sofa sofa = new VictorianSofa();
```

Adding more products makes the risk larger. Every client must remember which chair, sofa, table, and lamp belong together.

An abstract factory turns the family into one choice:

```java
FurnitureFactory factory = new ModernFurnitureFactory();
FurnitureStore store = new FurnitureStore(factory);
store.displayFurniture();
```

The store asks the selected factory for every product, so the family cannot be mixed inside `FurnitureStore`.

## Q1) What problem does Abstract Factory solve?

**Answer:** It gives one object responsibility for creating a compatible group. The client selects a family factory instead of remembering the concrete class for each product.

## Q2) Why does `FurnitureFactory` create both a Chair and a Sofa?

**Answer:** Chair and Sofa are two product types in the same furniture family. A concrete factory guarantees that both products use the same style.

## Q3) How does the client switch families?

**Answer:** `FurnitureStore` receives a different `FurnitureFactory`. Passing `ModernFurnitureFactory` creates Modern products, while passing `VictorianFurnitureFactory` creates Victorian products.

Receiving the factory in the constructor is important: `FurnitureStore` does not hardcode either family and can be reused with future families.

## Q4) How is this different from Factory Pattern?

**Answer:** A simple factory usually creates one product selected from several implementations. An abstract factory creates several related product types as one compatible family.

| Question | Factory | Abstract Factory |
| --- | --- | --- |
| What is selected? | One product type | One product family |
| What is created? | One kind of product interface | Several related product interfaces |
| Repository example | Car, Bike, or Truck | Matching Chair and Sofa |

## What is the main tradeoff?

Adding an Art Deco family only requires another `FurnitureFactory` implementation. Adding a new product type such as `Table` requires changing the factory interface and every concrete factory. Abstract Factory is most useful when family consistency is more important than frequently adding new product types.

## Class diagram

```text
                 +-----------------------+
                 |   FurnitureFactory    |
                 +-----------------------+
                 | +createChair()        |
                 | +createSofa()         |
                 +-----------^-----------+
                             |
              +--------------+---------------+
              |                              |
 +--------------------------+   +-----------------------------+
 | ModernFurnitureFactory   |   | VictorianFurnitureFactory  |
 +--------------------------+   +-----------------------------+
       | creates                         | creates
       v                                 v
 ModernChair + ModernSofa      VictorianChair + VictorianSofa
       |             |                  |                |
       v             v                  v                v
     Chair          Sofa              Chair             Sofa
```

Run `designpatterns.abstractfactorypattern.example.AbstractFactoryPatternDemo` to switch between both families.