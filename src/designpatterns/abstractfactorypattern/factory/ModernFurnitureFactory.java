package designpatterns.abstractfactorypattern.factory;

import designpatterns.abstractfactorypattern.product.Chair;
import designpatterns.abstractfactorypattern.product.ModernChair;
import designpatterns.abstractfactorypattern.product.ModernSofa;
import designpatterns.abstractfactorypattern.product.Sofa;

public class ModernFurnitureFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new ModernChair();
    }

    @Override
    public Sofa createSofa() {
        return new ModernSofa();
    }
}