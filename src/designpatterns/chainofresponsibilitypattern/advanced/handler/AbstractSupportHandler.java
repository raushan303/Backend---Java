package designpatterns.chainofresponsibilitypattern.advanced.handler;

import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;
import java.util.Objects;

public abstract class AbstractSupportHandler implements SupportHandler {

    private final SupportActor actor;
    private SupportHandler nextHandler;

    protected AbstractSupportHandler(SupportActor actor, SupportRole requiredRole) {
        this.actor = Objects.requireNonNull(actor, "Support actor must not be null");
        if (actor.role() != requiredRole) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " requires role " + requiredRole);
        }
    }

    @Override
    public SupportHandler setNext(SupportHandler nextHandler) {
        this.nextHandler = Objects.requireNonNull(nextHandler, "Next handler must not be null");
        return nextHandler;
    }

    @Override
    public final HandlingResult handle(SupportRequest request) {
        Objects.requireNonNull(request, "Support request must not be null");

        if (canHandle(request)) {
            return process(request);
        }

        if (nextHandler == null) {
            throw new IllegalStateException(
                    "No handler is available for " + request.priority() + " priority");
        }

        return nextHandler.handle(request);
    }

    protected final SupportActor actor() {
        return actor;
    }

    protected abstract boolean canHandle(SupportRequest request);

    protected abstract HandlingResult process(SupportRequest request);
}
