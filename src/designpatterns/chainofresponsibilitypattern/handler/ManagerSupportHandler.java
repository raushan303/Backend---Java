package designpatterns.chainofresponsibilitypattern.handler;

import designpatterns.chainofresponsibilitypattern.model.Priority;
import designpatterns.chainofresponsibilitypattern.model.SupportRequest;

public class ManagerSupportHandler extends AbstractSupportHandler {

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.priority() == Priority.HIGH;
    }

    @Override
    protected void process(SupportRequest request) {
        System.out.println("Manager resolved: " + request.issue());
    }

    @Override
    protected String getHandlerName() {
        return "Manager";
    }
}
