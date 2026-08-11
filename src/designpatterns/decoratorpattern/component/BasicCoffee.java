package designpatterns.decoratorpattern.component;

/**
 * Basic coffee implementation that can be decorated at runtime.
 */
public class BasicCoffee implements Coffee {

    @Override
    public String getDescription() {
        return "Basic Coffee";
    }

    @Override
    public double getCost() {
        return 80.0;
    }
}
