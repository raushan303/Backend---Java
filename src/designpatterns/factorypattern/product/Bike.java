package designpatterns.factorypattern.product;

public class Bike implements Vehicle {
    @Override
    public void drive() {
        System.out.println("Riding a bike through traffic");
    }
}