package designpatterns.abstractfactorypattern.example;

import designpatterns.abstractfactorypattern.factory.ModernFurnitureFactory;
import designpatterns.abstractfactorypattern.factory.VictorianFurnitureFactory;

public class AbstractFactoryPatternDemo {

    public static void main(String[] args) {
        System.out.println("Modern furniture family:");
        FurnitureStore modernStore = new FurnitureStore(new ModernFurnitureFactory());
        modernStore.tryFurniture();

        System.out.println("\nVictorian furniture family:");
        FurnitureStore victorianStore = new FurnitureStore(new VictorianFurnitureFactory());
        victorianStore.tryFurniture();
    }
}