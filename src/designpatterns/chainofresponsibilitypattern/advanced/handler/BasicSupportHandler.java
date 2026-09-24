package designpatterns.chainofresponsibilitypattern.advanced.handler;

import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;
import java.util.Locale;

public class BasicSupportHandler extends AbstractSupportHandler {

    public BasicSupportHandler(SupportActor actor) {
        super(actor, SupportRole.BASIC_SUPPORT);
    }

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.priority() == Priority.LOW;
    }

    @Override
    protected HandlingResult process(SupportRequest request) {
        String normalizedIssue = request.issue().toLowerCase(Locale.ROOT);
        if (normalizedIssue.contains("compromised") || normalizedIssue.contains("data breach")) {
            return new HandlingResult.PriorityChangeRequested(
                    Priority.HIGH,
                    actor(),
                    "Basic support detected a possible security incident"
            );
        }

        return new HandlingResult.Resolved(actor(), "Basic support resolved the request");
    }
}
