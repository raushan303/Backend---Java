package designpatterns.chainofresponsibilitypattern.advanced.handler;

import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;
import java.util.Locale;

public class ManagerSupportHandler extends AbstractSupportHandler {

    public ManagerSupportHandler(SupportActor actor) {
        super(actor, SupportRole.MANAGER);
    }

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.priority() == Priority.HIGH;
    }

    @Override
    protected HandlingResult process(SupportRequest request) {
        String normalizedIssue = request.issue().toLowerCase(Locale.ROOT);
        if (normalizedIssue.contains("routine password reset")) {
            return new HandlingResult.PriorityChangeRequested(
                    Priority.LOW,
                    actor(),
                    "Manager confirmed that the request is routine"
            );
        }

        return new HandlingResult.Resolved(actor(), "Manager resolved the request");
    }
}
