package designpatterns.factorypattern.example;

import designpatterns.factorypattern.factory.VehicleFactory;
import designpatterns.factorypattern.factory.VehicleType;
import designpatterns.factorypattern.product.Vehicle;

public class FactoryPatternDemo {

    public static void main(String[] args) {
        VehicleFactory vehicleFactory = new VehicleFactory();

        for (VehicleType vehicleType : VehicleType.values()) {
            Vehicle vehicle = vehicleFactory.createVehicle(vehicleType);
            vehicle.drive();
        }
    }
}