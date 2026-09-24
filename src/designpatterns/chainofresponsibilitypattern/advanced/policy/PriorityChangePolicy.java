package designpatterns.chainofresponsibilitypattern.advanced.policy;

import designpatterns.chainofresponsibilitypattern.advanced.audit.PriorityChangeAudit;
import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult.PriorityChangeRequested;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class PriorityChangePolicy {

    private final Clock clock;

    public PriorityChangePolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public PriorityChangeResult authorizeAndApply(
            SupportRequest request,
            PriorityChangeRequested change
    ) {
        Objects.requireNonNull(request, "Support request must not be null");
        Objects.requireNonNull(change, "Priority change must not be null");

        Priority oldPriority = request.priority();
        Priority newPriority = change.newPriority();

        if (oldPriority == newPriority) {
            throw new IllegalArgumentException("New priority must differ from current priority");
        }

        boolean isDeescalation = newPriority.severity() < oldPriority.severity();
        if (isDeescalation && change.requestedBy().role() != SupportRole.MANAGER) {
            throw new SecurityException("Only a manager may decrease request priority");
        }

        SupportRequest updatedRequest = request.withPriority(newPriority);
        PriorityChangeAudit audit = new PriorityChangeAudit(
                request.id(),
                oldPriority,
                newPriority,
                change.requestedBy(),
                change.reason(),
                Instant.now(clock)
        );

        return new PriorityChangeResult(updatedRequest, audit);
    }
}
