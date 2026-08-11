# Abstract Factory Pattern

This example creates matching Modern or Victorian furniture without coupling the store to concrete products.

## Q1) What problem does Abstract Factory solve?

**Answer:** It creates groups of related products that should be used together. The client selects a family factory instead of selecting each concrete product separately.

## Q2) Why does `FurnitureFactory` create both a Chair and a Sofa?

**Answer:** Chair and Sofa are two product types in the same furniture family. A concrete factory guarantees that both products use the same style.

## Q3) How does the client switch families?

**Answer:** `FurnitureStore` receives a different `FurnitureFactory`. Passing `ModernFurnitureFactory` creates Modern products, while passing `VictorianFurnitureFactory` creates Victorian products.

## Q4) How is this different from Factory Pattern?

**Answer:** A simple factory usually creates one product selected from several implementations. An abstract factory creates several related product types as one compatible family.

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