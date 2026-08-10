package designpatterns.observablepattern.observer;

import java.util.Objects;

import designpatterns.observablepattern.observable.StockPriceObservable;

/**
 * Observer implementation that displays stock updates on iPhone.
 */
public class IphoneObserverImpl implements Observer {

    private final StockPriceObservable observable;
    private final String userName;

    public IphoneObserverImpl(String userName, StockPriceObservable observable) {
        this.userName = Objects.requireNonNull(userName, "User name cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println("iPhone alert for " + userName + ": Stock price is now " + observable.getStockPrice());
    }
}
