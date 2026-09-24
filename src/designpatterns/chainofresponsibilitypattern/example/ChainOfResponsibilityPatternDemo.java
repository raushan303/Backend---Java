package designpatterns.chainofresponsibilitypattern.example;

import designpatterns.chainofresponsibilitypattern.handler.BasicSupportHandler;
import designpatterns.chainofresponsibilitypattern.handler.ManagerSupportHandler;
import designpatterns.chainofresponsibilitypattern.handler.SupportHandler;
import designpatterns.chainofresponsibilitypattern.handler.SupervisorSupportHandler;
import designpatterns.chainofresponsibilitypattern.model.Priority;
import designpatterns.chainofresponsibilitypattern.model.SupportRequest;

public class ChainOfResponsibilityPatternDemo {

    public static void main(String[] args) {
        SupportHandler basicSupport = new BasicSupportHandler();
        SupportHandler supervisor = new SupervisorSupportHandler();
        SupportHandler manager = new ManagerSupportHandler();

        basicSupport.setNext(supervisor).setNext(manager);

        handle(basicSupport, new SupportRequest("Reset my password", Priority.LOW));
        handle(basicSupport, new SupportRequest("Refund was not received", Priority.MEDIUM));
        handle(basicSupport, new SupportRequest("Account data may be compromised", Priority.HIGH));
    }

    private static void handle(SupportHandler firstHandler, SupportRequest request) {
        System.out.println("\nNew " + request.priority() + " priority request: " + request.issue());
        firstHandler.handle(request);
    }
}
