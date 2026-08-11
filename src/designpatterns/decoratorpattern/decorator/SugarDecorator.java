package designpatterns.decoratorpattern.decorator;

import designpatterns.decoratorpattern.component.Coffee;

/**
 * Adds sugar to an existing coffee order.
 */
public class SugarDecorator extends CoffeeDecorator {
    private static final double SUGAR_COST = 5.0;

    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Sugar";
    }

    @Override
    public double getCost() {
        return coffee.getCost() + SUGAR_COST;
    }
}
