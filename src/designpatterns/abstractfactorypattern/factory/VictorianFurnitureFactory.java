package designpatterns.abstractfactorypattern.factory;

import designpatterns.abstractfactorypattern.product.Chair;
import designpatterns.abstractfactorypattern.product.Sofa;
import designpatterns.abstractfactorypattern.product.VictorianChair;
import designpatterns.abstractfactorypattern.product.VictorianSofa;

public class VictorianFurnitureFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new VictorianChair();
    }

    @Override
    public Sofa createSofa() {
        return new VictorianSofa();
    }
}