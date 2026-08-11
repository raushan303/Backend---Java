package designpatterns.factorypattern.factory;

import designpatterns.factorypattern.product.Bike;
import designpatterns.factorypattern.product.Car;
import designpatterns.factorypattern.product.Truck;
import designpatterns.factorypattern.product.Vehicle;

public class VehicleFactory {

    public Vehicle createVehicle(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        return switch (vehicleType) {
            case CAR -> new Car();
            case BIKE -> new Bike();
            case TRUCK -> new Truck();
        };
    }
}