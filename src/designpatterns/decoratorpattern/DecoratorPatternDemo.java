package designpatterns.decoratorpattern;

import designpatterns.decoratorpattern.component.BasicCoffee;
import designpatterns.decoratorpattern.component.Coffee;
import designpatterns.decoratorpattern.decorator.MilkDecorator;
import designpatterns.decoratorpattern.decorator.SugarDecorator;
import designpatterns.decoratorpattern.decorator.WhippedCreamDecorator;

/**
 * Small demo showing how decorators can be composed dynamically.
 */
public class DecoratorPatternDemo {

    public static void main(String[] args) {
        Coffee basicCoffee = new BasicCoffee();
        printOrder("Plain order", basicCoffee);

        Coffee milkCoffee = new MilkDecorator(basicCoffee);
        printOrder("Milk order", milkCoffee);

        Coffee customCoffee = new WhippedCreamDecorator(new SugarDecorator(new MilkDecorator(new BasicCoffee())));
        printOrder("Custom order", customCoffee);
    }

    private static void printOrder(String label, Coffee coffee) {
        System.out.println(label + " -> " + coffee.getDescription() + " | Cost: " + coffee.getCost());
    }
}
