# Factory Pattern - Detailed Guide

## 1) What problem it solves

Creating concrete objects directly in many clients spreads construction decisions across the application. Those clients must change whenever product selection or construction changes.

The Factory Pattern moves that decision into a dedicated factory and returns products through a shared interface.

## 2) Problem statement

Create a vehicle based on a requested type while keeping the client independent of `Car`, `Bike`, and `Truck` constructors.

The solution should:
- centralize the creation decision
- give every vehicle a common contract
- keep concrete product classes out of client code
- reject invalid input clearly

## 3) Core participants

- `Vehicle` - common product interface
- `Car`, `Bike`, `Truck` - concrete products
- `VehicleType` - supported creation choices
- `VehicleFactory` - creates the requested concrete product
- `FactoryPatternDemo` - client that requests and uses vehicles

## 4) Folder structure

```text
factorypattern/
├── product/
│   ├── Vehicle.java
│   ├── Car.java
│   ├── Bike.java
│   └── Truck.java
├── factory/
│   ├── VehicleType.java
│   └── VehicleFactory.java
├── example/
│   └── FactoryPatternDemo.java
├── README.md
├── SHORT_EXPLANATION.md
└── DETAILED_EXPLANATION.md
```

## 5) Creation flow

```text
1. Client selects a VehicleType.
2. Client passes the type to VehicleFactory.
3. VehicleFactory creates the matching concrete product.
4. The factory returns it as a Vehicle.
5. Client calls drive() without knowing the concrete class.
```

## 6) Error handling

`VehicleFactory` throws `IllegalArgumentException` when the requested type is `null`. The enum limits normal calls to supported vehicle types.

## 7) Benefits

- centralizes construction decisions
- reduces coupling to concrete classes
- gives clients a stable product interface
- makes creation logic easier to find and test

## 8) Tradeoffs

- the factory can grow when many product types are added
- adding a product usually requires updating the factory selection logic
- it adds an extra class when object creation is otherwise trivial

## 9) Factory vs. Abstract Factory

This Factory example selects one product, such as a `Car` or `Truck`, behind one `Vehicle` interface. Abstract Factory creates a complete family of related products, such as a matching Chair and Sofa, through several product interfaces.

## 10) Real-world use cases

- selecting parsers by file type
- creating notifications by channel
- choosing database connectors by provider
- constructing payment handlers by payment type