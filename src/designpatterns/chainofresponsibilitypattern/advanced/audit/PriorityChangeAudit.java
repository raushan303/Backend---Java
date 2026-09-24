package designpatterns.chainofresponsibilitypattern.advanced.audit;

import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import java.time.Instant;
import java.util.Objects;

public record PriorityChangeAudit(
        String requestId,
        Priority oldPriority,
        Priority newPriority,
        SupportActor requestedBy,
        String reason,
        Instant changedAt
) {

    public PriorityChangeAudit {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request id must not be blank");
        }
        Objects.requireNonNull(oldPriority, "Old priority must not be null");
        Objects.requireNonNull(newPriority, "New priority must not be null");
        Objects.requireNonNull(requestedBy, "Requester must not be null");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason must not be blank");
        }
        Objects.requireNonNull(changedAt, "Change time must not be null");
    }
}
