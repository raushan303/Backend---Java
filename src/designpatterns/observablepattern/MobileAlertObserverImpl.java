package designpatterns.observablepattern;

import java.util.Objects;

/**
 * Observer implementation that displays stock alerts on mobile.
 */
public class MobileAlertObserverImpl implements Observer {

    private final StocksObservable observable;
    private final String mobileNumber;

    public MobileAlertObserverImpl(String mobileNumber, StocksObservable observable) {
        this.mobileNumber = Objects.requireNonNull(mobileNumber, "Mobile number cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println(
                "Mobile alert for " + mobileNumber + ": Stock price updated to " + observable.getStockPrice());
    }
}
