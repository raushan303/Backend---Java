package designpatterns.chainofresponsibilitypattern.advanced;

import designpatterns.chainofresponsibilitypattern.advanced.audit.PriorityChangeAudit;
import designpatterns.chainofresponsibilitypattern.advanced.handler.SupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.policy.PriorityChangePolicy;
import designpatterns.chainofresponsibilitypattern.advanced.policy.PriorityChangeResult;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SupportChainCoordinator {

    private static final int MAX_PRIORITY_CHANGES = 3;

    private final SupportHandler firstHandler;
    private final PriorityChangePolicy priorityChangePolicy;

    public SupportChainCoordinator(
            SupportHandler firstHandler,
            PriorityChangePolicy priorityChangePolicy
    ) {
        this.firstHandler = Objects.requireNonNull(
                firstHandler, "First handler must not be null");
        this.priorityChangePolicy = Objects.requireNonNull(
                priorityChangePolicy, "Priority change policy must not be null");
    }

    public SupportOutcome handle(SupportRequest originalRequest) {
        Objects.requireNonNull(originalRequest, "Support request must not be null");

        SupportRequest currentRequest = originalRequest;
        List<PriorityChangeAudit> priorityChanges = new ArrayList<>();

        while (true) {
            HandlingResult result = firstHandler.handle(currentRequest);

            if (result instanceof HandlingResult.Resolved resolved) {
                return new SupportOutcome(
                        currentRequest,
                        resolved.handledBy(),
                        resolved.message(),
                        priorityChanges
                );
            }

            if (priorityChanges.size() == MAX_PRIORITY_CHANGES) {
                throw new IllegalStateException(
                        "Request exceeded the maximum number of priority changes");
            }

            HandlingResult.PriorityChangeRequested change =
                    (HandlingResult.PriorityChangeRequested) result;
            PriorityChangeResult changeResult =
                    priorityChangePolicy.authorizeAndApply(currentRequest, change);

            currentRequest = changeResult.updatedRequest();
            priorityChanges.add(changeResult.audit());
        }
    }
}
