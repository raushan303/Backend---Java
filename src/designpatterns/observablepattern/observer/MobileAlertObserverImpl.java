package designpatterns.observablepattern.observer;

import java.util.Objects;

import designpatterns.observablepattern.observable.StockPriceObservable;

/**
 * Observer implementation that displays stock alerts on mobile.
 */
public class MobileAlertObserverImpl implements Observer {

    private final StockPriceObservable observable;
    private final String mobileNumber;

    public MobileAlertObserverImpl(String mobileNumber, StockPriceObservable observable) {
        this.mobileNumber = Objects.requireNonNull(mobileNumber, "Mobile number cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println(
                "Mobile alert for " + mobileNumber + ": Stock price updated to " + observable.getStockPrice());
    }
}
