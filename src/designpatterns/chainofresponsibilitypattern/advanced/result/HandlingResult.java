package designpatterns.chainofresponsibilitypattern.advanced.result;

import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import java.util.Objects;

public sealed interface HandlingResult
        permits HandlingResult.Resolved, HandlingResult.PriorityChangeRequested {

    record Resolved(SupportActor handledBy, String message) implements HandlingResult {

        public Resolved {
            Objects.requireNonNull(handledBy, "Handler must not be null");
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Resolution message must not be blank");
            }
        }
    }

    record PriorityChangeRequested(
            Priority newPriority,
            SupportActor requestedBy,
            String reason
    ) implements HandlingResult {

        public PriorityChangeRequested {
            Objects.requireNonNull(newPriority, "New priority must not be null");
            Objects.requireNonNull(requestedBy, "Requester must not be null");
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("Priority change reason must not be blank");
            }
        }
    }
}
