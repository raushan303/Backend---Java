# Factory Pattern

This example uses a vehicle factory to show how object creation can be separated from object usage.

## Q1) What problem does the Factory Pattern solve?

**Answer:** It prevents client code from being tightly coupled to concrete classes. The client requests a product from the factory and works with the common product interface.

## Q2) What does `VehicleFactory` do?

**Answer:** `VehicleFactory` receives a `VehicleType`, chooses the matching concrete class, and returns it as a `Vehicle`. It is the only class that needs to know how `Car`, `Bike`, and `Truck` are constructed.

## Q3) Why return `Vehicle` instead of a concrete class?

**Answer:** Returning the interface lets the client use every vehicle in the same way. A new vehicle can be added without changing code that only calls `Vehicle.drive()`.

## Q4) Is this Factory Method or Simple Factory?

**Answer:** This example is commonly called a **Simple Factory** because one factory object contains the creation decision. Factory Method usually lets subclasses override a creation method. Both move concrete object creation away from client code.

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