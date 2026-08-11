# Factory Pattern

## Interview description

The **Factory Pattern** keeps object-creation logic in one place and returns objects through a common interface. The client asks for a type of object without directly creating or depending on its concrete class.

In this implementation:
- `product/` contains the `Vehicle` contract and `Car`, `Bike`, and `Truck` products.
- `factory/` contains `VehicleFactory`, which decides which vehicle to create.
- `example/` contains a runnable client that uses only the common `Vehicle` interface.

Use this pattern when object creation has decisions or setup logic that should not be spread across client code.