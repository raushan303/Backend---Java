package designpatterns.observablepattern;

import java.util.Objects;

/**
 * Observer implementation that sends push notifications for stock updates.
 */
public class NotificationAlertObserver implements Observer {

    private final Observable observable;
    private final String deviceToken;

    public NotificationAlertObserver(String deviceToken, Observable observable) {
        this.deviceToken = Objects.requireNonNull(deviceToken, "Device token cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println(
                "Push notification sent to token " + deviceToken + ": Latest stock price is " + observable.getStockPrice());
    }
}
