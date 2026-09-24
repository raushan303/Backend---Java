package designpatterns.chainofresponsibilitypattern.advanced;

import designpatterns.chainofresponsibilitypattern.advanced.audit.PriorityChangeAudit;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import java.util.List;
import java.util.Objects;

public record SupportOutcome(
        SupportRequest finalRequest,
        SupportActor handledBy,
        String message,
        List<PriorityChangeAudit> priorityChanges
) {

    public SupportOutcome {
        Objects.requireNonNull(finalRequest, "Final request must not be null");
        Objects.requireNonNull(handledBy, "Handler must not be null");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Outcome message must not be blank");
        }
        priorityChanges = List.copyOf(
                Objects.requireNonNull(priorityChanges, "Priority changes must not be null"));
    }
}
