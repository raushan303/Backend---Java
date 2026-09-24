package designpatterns.chainofresponsibilitypattern.advanced.example;

import designpatterns.chainofresponsibilitypattern.advanced.SupportChainCoordinator;
import designpatterns.chainofresponsibilitypattern.advanced.SupportOutcome;
import designpatterns.chainofresponsibilitypattern.advanced.handler.BasicSupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.handler.ManagerSupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.handler.SupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.handler.SupervisorSupportHandler;
import designpatterns.chainofresponsibilitypattern.advanced.model.Priority;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportActor;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRole;
import designpatterns.chainofresponsibilitypattern.advanced.policy.PriorityChangePolicy;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class AdvancedSupportChainScenarioTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-24T08:00:00Z"), ZoneOffset.UTC);

    public static void main(String[] args) {
        verifiesEscalation();
        verifiesDeescalation();
        verifiesNormalRouting();
        rejectsUnauthorizedDeescalation();
        rejectsEndlessPriorityChanges();
        System.out.println("All advanced support-chain scenarios passed.");
    }

    private static void verifiesEscalation() {
        SupportRequest originalRequest = new SupportRequest(
                "TEST-1",
                "Account data may be compromised",
                Priority.LOW
        );
        SupportOutcome outcome = createCoordinator().handle(originalRequest);

        assertEquals(Priority.LOW, originalRequest.priority(), "original priority");
        assertEquals(Priority.HIGH, outcome.finalRequest().priority(), "final priority");
        assertEquals(SupportRole.MANAGER, outcome.handledBy().role(), "final handler");
        assertEquals(1, outcome.priorityChanges().size(), "priority change count");
        assertEquals(
                Priority.LOW,
                outcome.priorityChanges().getFirst().oldPriority(),
                "old priority"
        );
        assertEquals(
                Priority.HIGH,
                outcome.priorityChanges().getFirst().newPriority(),
                "new priority"
        );
    }

    private static void verifiesDeescalation() {
        SupportOutcome outcome = createCoordinator().handle(new SupportRequest(
                "TEST-2",
                "Routine password reset request",
                Priority.HIGH
        ));

        assertEquals(Priority.LOW, outcome.finalRequest().priority(), "final priority");
        assertEquals(SupportRole.BASIC_SUPPORT, outcome.handledBy().role(), "final handler");
        assertEquals(1, outcome.priorityChanges().size(), "priority change count");
        assertEquals(
                SupportRole.MANAGER,
                outcome.priorityChanges().getFirst().requestedBy().role(),
                "change requester"
        );
    }

    private static void verifiesNormalRouting() {
        SupportOutcome outcome = createCoordinator().handle(new SupportRequest(
                "TEST-3",
                "Refund was not received",
                Priority.MEDIUM
        ));

        assertEquals(Priority.MEDIUM, outcome.finalRequest().priority(), "final priority");
        assertEquals(SupportRole.SUPERVISOR, outcome.handledBy().role(), "final handler");
        assertEquals(0, outcome.priorityChanges().size(), "priority change count");
    }

    private static void rejectsUnauthorizedDeescalation() {
        PriorityChangePolicy policy = new PriorityChangePolicy(FIXED_CLOCK);
        SupportRequest request = new SupportRequest(
                "TEST-4",
                "High priority request",
                Priority.HIGH
        );
        HandlingResult.PriorityChangeRequested change =
                new HandlingResult.PriorityChangeRequested(
                        Priority.LOW,
                        new SupportActor("agent-1", SupportRole.BASIC_SUPPORT),
                        "Attempted unauthorized de-escalation"
                );

        try {
            policy.authorizeAndApply(request, change);
            throw new AssertionError("Expected unauthorized de-escalation to be rejected");
        } catch (SecurityException expected) {
            assertEquals(
                    "Only a manager may decrease request priority",
                    expected.getMessage(),
                    "security error"
            );
        }
    }

    private static void rejectsEndlessPriorityChanges() {
        SupportActor basicActor =
                new SupportActor("agent-1", SupportRole.BASIC_SUPPORT);
        SupportActor managerActor =
                new SupportActor("manager-1", SupportRole.MANAGER);

        SupportHandler loopingHandler = new SupportHandler() {
            @Override
            public SupportHandler setNext(SupportHandler nextHandler) {
                return nextHandler;
            }

            @Override
            public HandlingResult handle(SupportRequest request) {
                boolean shouldEscalate = request.priority() == Priority.LOW;
                return new HandlingResult.PriorityChangeRequested(
                        shouldEscalate ? Priority.HIGH : Priority.LOW,
                        shouldEscalate ? basicActor : managerActor,
                        "Test repeated priority change"
                );
            }
        };

        SupportChainCoordinator coordinator = new SupportChainCoordinator(
                loopingHandler,
                new PriorityChangePolicy(FIXED_CLOCK)
        );

        try {
            coordinator.handle(new SupportRequest(
                    "TEST-5",
                    "Request with conflicting routing rules",
                    Priority.LOW
            ));
            throw new AssertionError("Expected repeated priority changes to be rejected");
        } catch (IllegalStateException expected) {
            assertEquals(
                    "Request exceeded the maximum number of priority changes",
                    expected.getMessage(),
                    "priority change limit error"
            );
        }
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
                new PriorityChangePolicy(FIXED_CLOCK)
        );
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    description + ": expected " + expected + " but was " + actual);
        }
    }
}
