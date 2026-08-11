package designpatterns.decoratorpattern.decorator;

import java.util.Objects;

import designpatterns.decoratorpattern.component.Coffee;

/**
 * Base decorator that wraps another coffee object.
 */
public abstract class CoffeeDecorator implements Coffee {

    protected final Coffee coffee;

    protected CoffeeDecorator(Coffee coffee) {
        this.coffee = Objects.requireNonNull(coffee, "Coffee cannot be null");
    }
}
