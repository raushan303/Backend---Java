package designpatterns.decoratorpattern.component;

/**
 * Basic coffee implementation that can be decorated at runtime.
 */
public class BasicCoffee implements Coffee {
    private static final double BASE_COST = 80.0;

    @Override
    public String getDescription() {
        return "Basic Coffee";
    }

    @Override
    public double getCost() {
        return BASE_COST;
    }
}
