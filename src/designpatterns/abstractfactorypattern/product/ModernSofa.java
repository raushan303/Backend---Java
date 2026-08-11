package designpatterns.abstractfactorypattern.product;

public class ModernSofa implements Sofa {
    @Override
    public void relaxOn() {
        System.out.println("Relaxing on a modern sofa");
    }
}