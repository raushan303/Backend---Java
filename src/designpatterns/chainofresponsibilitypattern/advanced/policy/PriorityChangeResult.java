package designpatterns.chainofresponsibilitypattern.advanced.policy;

import designpatterns.chainofresponsibilitypattern.advanced.audit.PriorityChangeAudit;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import java.util.Objects;

public record PriorityChangeResult(
        SupportRequest updatedRequest,
        PriorityChangeAudit audit
) {

    public PriorityChangeResult {
        Objects.requireNonNull(updatedRequest, "Updated request must not be null");
        Objects.requireNonNull(audit, "Priority change audit must not be null");
    }
}
