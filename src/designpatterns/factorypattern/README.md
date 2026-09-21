# Factory Pattern

This example uses a vehicle factory to separate **creating an object** from **using the object**.

## See the problem first

Without a factory, every client may repeat the same selection logic:

```java
Vehicle vehicle;

switch (vehicleType) {
   case CAR -> vehicle = new Car();
   case BIKE -> vehicle = new Bike();
   case TRUCK -> vehicle = new Truck();
}

vehicle.drive();
```

The client only wants to call `drive()`, but it must import and understand every concrete class. Repeating this code spreads the construction rule across the application.

With the factory, the client becomes:

```java
VehicleFactory factory = new VehicleFactory();
Vehicle vehicle = factory.createVehicle(VehicleType.CAR);
vehicle.drive();
```

The creation rule has one owner: `VehicleFactory`.

## Q1) What problem does the Factory Pattern solve?

**Answer:** It prevents each client from repeating constructor and selection logic. The client requests a product from the factory and works with the common product interface.

## Q2) What does `VehicleFactory` do?

**Answer:** `VehicleFactory` receives a `VehicleType`, chooses the matching concrete class, and returns it as a `Vehicle`. It is the only class that needs to know how `Car`, `Bike`, and `Truck` are constructed.

## Q3) Why return `Vehicle` instead of a concrete class?

**Answer:** Returning the interface lets the client use every vehicle in the same way. A new vehicle can be added without changing code that only calls `Vehicle.drive()`.

The factory itself still needs to change when a new value is added to `VehicleType`. The benefit is that this change is centralized instead of repeated in every client.

## Q4) Is this Factory Method or Simple Factory?

**Answer:** This example is commonly called a **Simple Factory** because one factory object contains the creation decision. Factory Method usually lets subclasses override a creation method. Both move concrete object creation away from client code.

## When should I not use it?

If construction is simply `new Car()` and no selection or setup is being hidden, a factory may add ceremony without solving a real problem. Introduce it when creation logic is repeated, likely to change, or should be kept away from business logic.

## Class diagram

```text
                    +------------------+
                    |     Vehicle      |
                    +------------------+
                    | +drive()         |
                    +--------^---------+
                             |
                 +-----------+-----------+
                 |           |           |
              +-----+     +------+    +-------+
              | Car |     | Bike |    | Truck |
              +-----+     +------+    +-------+
                             ^
                             |
                +-------------------------+
                |     VehicleFactory      |
                +-------------------------+
                | +createVehicle(type)    |
                +-------------------------+
```

Run `designpatterns.factorypattern.example.FactoryPatternDemo` to see all products created through the factory.