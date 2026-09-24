package designpatterns.chainofresponsibilitypattern.handler;

import designpatterns.chainofresponsibilitypattern.model.Priority;
import designpatterns.chainofresponsibilitypattern.model.SupportRequest;

public class SupervisorSupportHandler extends AbstractSupportHandler {

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.priority() == Priority.MEDIUM;
    }

    @Override
    protected void process(SupportRequest request) {
        System.out.println("Supervisor resolved: " + request.issue());
    }

    @Override
    protected String getHandlerName() {
        return "Supervisor";
    }
}
