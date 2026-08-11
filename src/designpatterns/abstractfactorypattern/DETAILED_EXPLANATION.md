# Abstract Factory Pattern - Detailed Guide

## 1) What problem it solves

Applications sometimes use several related product types that must remain compatible. Creating each concrete product separately can accidentally mix families and couples the client to many classes.

Abstract Factory gives the client one factory interface for creating a complete, matching product family.

## 2) Problem statement

Build a furniture store that can use either Modern or Victorian furniture. Every selected family must provide both a Chair and a Sofa without changing the store logic.

The solution should:
- keep each furniture family consistent
- hide concrete product construction
- allow the complete family to change through one dependency
- keep the client dependent on interfaces

## 3) Core participants

- `Chair`, `Sofa` - abstract product interfaces
- `ModernChair`, `ModernSofa` - Modern product family
- `VictorianChair`, `VictorianSofa` - Victorian product family
- `FurnitureFactory` - abstract factory contract
- `ModernFurnitureFactory`, `VictorianFurnitureFactory` - concrete family factories
- `FurnitureStore` - client that uses abstract products
- `AbstractFactoryPatternDemo` - selects and demonstrates each family

## 4) Folder structure

```text
abstractfactorypattern/
├── product/
│   ├── Chair.java
│   ├── Sofa.java
│   ├── ModernChair.java
│   ├── ModernSofa.java
│   ├── VictorianChair.java
│   └── VictorianSofa.java
├── factory/
│   ├── FurnitureFactory.java
│   ├── ModernFurnitureFactory.java
│   └── VictorianFurnitureFactory.java
├── example/
│   ├── FurnitureStore.java
│   └── AbstractFactoryPatternDemo.java
├── README.md
├── SHORT_EXPLANATION.md
└── DETAILED_EXPLANATION.md
```

## 5) Creation flow

```text
1. Application chooses a concrete FurnitureFactory.
2. FurnitureStore receives it through the FurnitureFactory interface.
3. The store asks the factory for a Chair and a Sofa.
4. The concrete factory creates products from one matching family.
5. The store uses only the Chair and Sofa interfaces.
```

## 6) Family consistency

Each concrete factory owns all creation methods for one style. `ModernFurnitureFactory` returns only Modern products, and `VictorianFurnitureFactory` returns only Victorian products. The client cannot mix them accidentally when it obtains products from one factory.

## 7) Error handling

`FurnitureStore` rejects a `null` factory with `IllegalArgumentException`. This prevents an incomplete store from being created.

## 8) Benefits

- keeps related products consistent
- replaces an entire family through one factory dependency
- hides concrete classes from the client
- supports Dependency Inversion and Open/Closed design

## 9) Tradeoffs

- adding a new family is easy because it needs another concrete factory
- adding a new product type is harder because every factory must implement a new creation method
- the number of interfaces and classes is unnecessary for very small systems

## 10) Abstract Factory vs. Factory

Factory focuses on creating one product behind one product interface. Abstract Factory creates multiple related product types behind multiple product interfaces and guarantees that they belong to the same family.

## 11) Real-world use cases

- Windows and macOS UI controls
- database-specific connections, commands, and readers
- cloud-provider storage and compute services
- regional payment and tax component families