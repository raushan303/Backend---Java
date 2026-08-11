package designpatterns.abstractfactorypattern.factory;

import designpatterns.abstractfactorypattern.product.Chair;
import designpatterns.abstractfactorypattern.product.Sofa;

public interface FurnitureFactory {
    Chair createChair();

    Sofa createSofa();
}