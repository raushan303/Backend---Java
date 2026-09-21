# Abstract Factory Pattern - Detailed Guide

## 1) What problem it solves

Applications sometimes use several related product types that must remain compatible. Creating each concrete product separately can accidentally mix families and couples the client to many classes.

```java
Chair chair = new ModernChair();
Sofa sofa = new VictorianSofa(); // Valid types, invalid combination.
```

The compiler cannot identify that design mistake because both objects implement the correct individual interfaces. The missing rule is that they must come from the same family.

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
- adding a new product type is harder: adding `Table` requires `createTable()` in `FurnitureFactory`, `ModernFurnitureFactory`, and `VictorianFurnitureFactory`
- the number of interfaces and classes is unnecessary for very small systems

## 10) Abstract Factory vs. Factory

Factory focuses on creating one product behind one product interface. Abstract Factory creates multiple related product types behind multiple product interfaces and guarantees that they belong to the same family.

| Question | Factory example | Abstract Factory example |
| --- | --- | --- |
| What does the caller choose? | A vehicle type | A furniture family |
| What comes from that choice? | One `Vehicle` | A matching `Chair` and `Sofa` |
| Main rule being centralized | Which class to construct | Which products are compatible |

## 11) Real-world use cases

- Windows and macOS UI controls
- database-specific connections, commands, and readers
- cloud-provider storage and compute services
- regional payment and tax component families