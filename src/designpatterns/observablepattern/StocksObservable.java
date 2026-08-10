package designpatterns.observablepattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Observable implementation that tracks stock price updates.
 */
public class StocksObservable implements Observable {

    private final List<Observer> observers = new ArrayList<>();
    private double stockPrice;

    @Override
    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }

        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    /**
     * Updates the latest stock price and notifies observers when value changes.
     *
     * @param stockPrice latest stock price
     */
    public void setStockPrice(double stockPrice) {
        if (stockPrice < 0) {
            throw new IllegalArgumentException("Stock price cannot be negative");
        }

        if (Double.compare(this.stockPrice, stockPrice) != 0) {
            this.stockPrice = stockPrice;
            notifyObservers();
        }
    }

    /**
     * Returns the current stock price.
     *
     * @return current stock price
     */
    public double getStockPrice() {
        return stockPrice;
    }
}
