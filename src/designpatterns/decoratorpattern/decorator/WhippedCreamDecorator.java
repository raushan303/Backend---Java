package designpatterns.decoratorpattern.decorator;

import designpatterns.decoratorpattern.component.Coffee;

/**
 * Adds whipped cream to an existing coffee order.
 */
public class WhippedCreamDecorator extends CoffeeDecorator {

    public WhippedCreamDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Whipped Cream";
    }

    @Override
    public double getCost() {
        return coffee.getCost() + 25.0;
    }
}
