package designpatterns.observablepattern.observer;

import java.util.Objects;

import designpatterns.observablepattern.observable.StockPriceObservable;

/**
 * Observer implementation that sends stock updates through email.
 */
public class EmailAlertObserverImpl implements Observer {

    private final StockPriceObservable observable;
    private final String email;

    public EmailAlertObserverImpl(String email, StockPriceObservable observable) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println("Email sent to " + email + ": Stock price changed to " + observable.getStockPrice());
    }
}
