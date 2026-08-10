package designpatterns.observablepattern.observer;

import java.util.Objects;

import designpatterns.observablepattern.observable.StockPriceObservable;

/**
 * Observer implementation that sends push notifications for stock updates.
 */
public class NotificationAlertObserver implements Observer {

    private final StockPriceObservable observable;
    private final String deviceToken;

    public NotificationAlertObserver(String deviceToken, StockPriceObservable observable) {
        this.deviceToken = Objects.requireNonNull(deviceToken, "Device token cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println(
                "Push notification sent to token " + deviceToken + ": Latest stock price is " + observable.getStockPrice());
    }
}
