package designpatterns.observablepattern;

/**
 * Contract for observer objects that receive stock change notifications.
 */
public interface Observer {

    /**
     * Invoked when observable state changes.
     */
    void update();
}
