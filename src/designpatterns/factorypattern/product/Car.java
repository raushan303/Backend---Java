package designpatterns.factorypattern.product;

public class Car implements Vehicle {
    @Override
    public void drive() {
        System.out.println("Driving a car on the road");
    }
}