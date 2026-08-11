package designpatterns.decoratorpattern.component;

/**
 * Contract for coffee objects that expose description and total cost.
 */
public interface Coffee {

    /**
     * Returns the current coffee description including add-ons.
     *
     * @return coffee description
     */
    String getDescription();

    /**
     * Returns the current coffee cost including add-ons.
     *
     * @return total cost
     */
    double getCost();
}
