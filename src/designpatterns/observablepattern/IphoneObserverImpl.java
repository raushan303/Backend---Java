package designpatterns.observablepattern;

import java.util.Objects;

/**
 * Observer implementation that displays stock updates on iPhone.
 */
public class IphoneObserverImpl implements Observer {

    private final Observable observable;
    private final String userName;

    public IphoneObserverImpl(String userName, Observable observable) {
        this.userName = Objects.requireNonNull(userName, "User name cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println("iPhone alert for " + userName + ": Stock price is now " + observable.getStockPrice());
    }
}
