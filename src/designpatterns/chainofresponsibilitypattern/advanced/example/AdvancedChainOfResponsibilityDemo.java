package designpatterns.chainofresponsibilitypattern.advanced.example;

import designpatterns.chainofresponsibilitypattern.advanced.SupportChainCoordinator;
import designpatterns.chainofresponsibilitypattern.advanced.SupportOutcome;
import designpatterns.chainofresponsibilitypattern.advanced.audit.PriorityChangeAudit;
import designpatterns.chainofresponsibilitypattern.advanced.handler.BasicSupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.handler.ManagerSupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.handler.SupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.handler.SupervisorSupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.policy.PriorityChangePolicy;
import java.time.Clock;

public class AdvancedChainOfResponsibilityDemo {

    public static void main(String[] args) {
        SupportChainCoordinator coordinator = createCoordinator();

        printOutcome(coordinator.handle(new SupportRequest(
                "REQ-101",
                "Customer account may be compromised",
                Priority.LOW
        )));

        printOutcome(coordinator.handle(new SupportRequest(
                "REQ-102",
                "Routine password reset request",
                Priority.HIGH
        )));

        printOutcome(coordinator.handle(new SupportRequest(
                "REQ-103",
                "Refund was not received",
                Priority.MEDIUM
        )));
    }

    private static SupportChainCoordinator createCoordinator() {
        SupportHandler basicSupport = new BasicSupportHandler(
                new SupportActor("agent-1", SupportRole.BASIC_SUPPORT));
        SupportHandler supervisor = new SupervisorSupportHandler(
                new SupportActor("supervisor-1", SupportRole.SUPERVISOR));
        SupportHandler manager = new ManagerSupportHandler(
                new SupportActor("manager-1", SupportRole.MANAGER));

        basicSupport.setNext(supervisor).setNext(manager);

        return new SupportChainCoordinator(
                basicSupport,
                new PriorityChangePolicy(Clock.systemUTC())
        );
    }

    private static void printOutcome(SupportOutcome outcome) {
        System.out.println("\nRequest: " + outcome.finalRequest().id());

        for (PriorityChangeAudit change : outcome.priorityChanges()) {
            System.out.println(
                    "Priority changed from " + change.oldPriority()
                            + " to " + change.newPriority()
                            + " by " + change.requestedBy().id()
                            + " because: " + change.reason()
            );
        }

        System.out.println(
                outcome.message() + " by " + outcome.handledBy().id()
                        + " at " + outcome.finalRequest().priority() + " priority"
        );
    }
}
