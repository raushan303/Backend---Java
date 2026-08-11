package designpatterns.abstractfactorypattern.example;

import designpatterns.abstractfactorypattern.factory.FurnitureFactory;
import designpatterns.abstractfactorypattern.product.Chair;
import designpatterns.abstractfactorypattern.product.Sofa;

public class FurnitureStore {
    private final Chair chair;
    private final Sofa sofa;

    public FurnitureStore(FurnitureFactory furnitureFactory) {
        if (furnitureFactory == null) {
            throw new IllegalArgumentException("Furniture factory cannot be null");
        }

        chair = furnitureFactory.createChair();
        sofa = furnitureFactory.createSofa();
    }

    public void tryFurniture() {
        chair.sitOn();
        sofa.relaxOn();
    }
}