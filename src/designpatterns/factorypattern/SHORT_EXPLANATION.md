# Factory Pattern

## Start with the problem

Suppose several parts of an application create vehicles like this:

```java
Vehicle vehicle;

if (requestedType.equals("CAR")) {
	vehicle = new Car();
} else if (requestedType.equals("BIKE")) {
	vehicle = new Bike();
} else {
	vehicle = new Truck();
}
```

Every caller now knows all concrete vehicle classes. If construction changes or a new type is added, the same decision may need to change in several places.

## The solution

Move the creation decision to one class:

```java
VehicleFactory factory = new VehicleFactory();
Vehicle vehicle = factory.createVehicle(VehicleType.CAR);
vehicle.drive();
```

The caller asks for a kind of vehicle and receives the common `Vehicle` interface. Only `VehicleFactory` decides whether to construct a `Car`, `Bike`, or `Truck`.

## Roles in this example

- **Product interface:** `Vehicle`, which defines `drive()`.
- **Concrete products:** `Car`, `Bike`, and `Truck`.
- **Factory:** `VehicleFactory`, which contains the construction decision.
- **Creation choice:** `VehicleType`, an enum that prevents invalid string values.
- **Client:** `FactoryPatternDemo`, which uses the returned `Vehicle`.

## When Factory is useful

Use it when creating an object involves a decision, setup steps, validation, or dependencies that should not be repeated in callers. Do not add a factory when `new SomeClass()` is simple and callers genuinely need that exact class.

This repository demonstrates a **Simple Factory**: one factory method contains the selection logic. The classic **Factory Method** pattern is a related variant where subclasses override the method that creates the product.

## Interview description

The **Factory Pattern** centralizes object creation and returns objects through a common interface, so client code uses products without knowing which concrete class was constructed.