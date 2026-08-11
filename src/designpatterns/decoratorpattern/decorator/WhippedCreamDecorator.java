package designpatterns.decoratorpattern.decorator;

import designpatterns.decoratorpattern.component.Coffee;

/**
 * Adds whipped cream to an existing coffee order.
 */
public class WhippedCreamDecorator extends CoffeeDecorator {
    private static final double WHIPPED_CREAM_COST = 25.0;

    public WhippedCreamDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Whipped Cream";
    }

    @Override
    public double getCost() {
        return coffee.getCost() + WHIPPED_CREAM_COST;
    }
}
