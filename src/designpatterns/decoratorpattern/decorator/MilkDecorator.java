package designpatterns.decoratorpattern.decorator;

import designpatterns.decoratorpattern.component.Coffee;

/**
 * Adds milk to an existing coffee order.
 */
public class MilkDecorator extends CoffeeDecorator {
    private static final double MILK_COST = 20.0;

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Milk";
    }

    @Override
    public double getCost() {
        return coffee.getCost() + MILK_COST;
    }
}
