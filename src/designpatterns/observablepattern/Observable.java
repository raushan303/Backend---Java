package designpatterns.observablepattern;

/**
 * Contract for observable objects that maintain and notify observers.
 */
public interface Observable {

    /**
     * Registers an observer for stock updates.
     *
     * @param observer observer to register
     */
    void addObserver(Observer observer);

    /**
     * Removes an already registered observer.
     *
     * @param observer observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Notifies all registered observers about a stock change.
     */
    void notifyObservers();

    /**
     * Updates the latest stock price and triggers observer notification.
     *
     * @param stockPrice latest stock price
     */
    void setStockPrice(double stockPrice);

    /**
     * Returns the current stock price.
     *
     * @return current stock price
     */
    double getStockPrice();
}
