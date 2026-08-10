package designpatterns.observablepattern;

/**
 * Stock-specific observable contract exposing stock price data.
 */
public interface StockPriceObservable extends Observable {

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
