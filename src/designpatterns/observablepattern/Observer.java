package designpatterns.observablepattern;

/**
 * Contract for observer objects that receive state change notifications.
 */
public interface Observer {

    /**
     * Invoked when observable state changes.
     */
    void update();
}
