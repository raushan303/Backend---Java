package designpatterns.observablepattern;

import java.util.Objects;

/**
 * Observer implementation that sends stock updates through email.
 */
public class EmailAlertObserverImpl implements Observer {

    private final Observable observable;
    private final String email;

    public EmailAlertObserverImpl(String email, Observable observable) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.observable = Objects.requireNonNull(observable, "Observable cannot be null");
    }

    @Override
    public void update() {
        System.out.println("Email sent to " + email + ": Stock price changed to " + observable.getStockPrice());
    }
}
