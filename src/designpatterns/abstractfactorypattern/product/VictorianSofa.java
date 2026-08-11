package designpatterns.abstractfactorypattern.product;

public class VictorianSofa implements Sofa {
    @Override
    public void relaxOn() {
        System.out.println("Relaxing on a Victorian sofa");
    }
}