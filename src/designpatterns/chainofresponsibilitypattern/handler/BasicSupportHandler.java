package designpatterns.chainofresponsibilitypattern.handler;

import designpatterns.chainofresponsibilitypattern.model.Priority;
import designpatterns.chainofresponsibilitypattern.model.SupportRequest;

public class BasicSupportHandler extends AbstractSupportHandler {

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.priority() == Priority.LOW;
    }

    @Override
    protected void process(SupportRequest request) {
        System.out.println("Basic support resolved: " + request.issue());
    }

    @Override
    protected String getHandlerName() {
        return "Basic support";
    }
}
