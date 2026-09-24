package designpatterns.chainofresponsibilitypattern.handler;

import designpatterns.chainofresponsibilitypattern.model.SupportRequest;
import java.util.Objects;

public abstract class AbstractSupportHandler implements SupportHandler {

    private SupportHandler nextHandler;

    @Override
    public SupportHandler setNext(SupportHandler nextHandler) {
        this.nextHandler = Objects.requireNonNull(nextHandler, "Next handler must not be null");
        return nextHandler;
    }

    @Override
    public final void handle(SupportRequest request) {
        Objects.requireNonNull(request, "Support request must not be null");

        if (canHandle(request)) {
            process(request);
            return;
        }

        if (nextHandler == null) {
            throw new IllegalStateException(
                    "No handler is available for " + request.priority() + " priority");
        }

        System.out.println(getHandlerName() + " forwards the request.");
        nextHandler.handle(request);
    }

    protected abstract boolean canHandle(SupportRequest request);

    protected abstract void process(SupportRequest request);

    protected abstract String getHandlerName();
}
