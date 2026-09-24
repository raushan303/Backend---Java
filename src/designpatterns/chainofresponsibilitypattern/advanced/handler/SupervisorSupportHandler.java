package designpatterns.chainofresponsibilitypattern.advanced.handler;

import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;

public class SupervisorSupportHandler extends AbstractSupportHandler {

    public SupervisorSupportHandler(SupportActor actor) {
        super(actor, SupportRole.SUPERVISOR);
    }

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.priority() == Priority.MEDIUM;
    }

    @Override
    protected HandlingResult process(SupportRequest request) {
        return new HandlingResult.Resolved(actor(), "Supervisor resolved the request");
    }
}
