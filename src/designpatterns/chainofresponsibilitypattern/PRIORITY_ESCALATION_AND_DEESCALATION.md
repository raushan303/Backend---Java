# Implementing Priority Escalation and De-escalation

The current implementation only supports forwarding through a one-way chain.
It cannot properly reprioritize a request, especially during de-escalation,
because a manager cannot send a request backward to basic support.

The clean solution is to separate two responsibilities:

1. **Handlers** resolve a request or suggest a priority change.
2. **A coordinator** authorizes the change and restarts the chain with a new
   immutable request.

This document describes how that extension could be implemented. The current
example remains intentionally small. A separate executable implementation is
available in [`advanced/`](./advanced/README.md).

## 1) Represent a handler decision

Instead of returning `void`, a handler returns an explicit result:

```java
public sealed interface HandlingResult {

    record Resolved(String handledBy) implements HandlingResult {}

    record ChangePriority(
            Priority newPriority,
            String requestedBy,
            String reason
    ) implements HandlingResult {}
}
```

A handler can now produce one of two outcomes:

- `Resolved` means that processing is complete.
- `ChangePriority` means that the handler recommends reprioritization.

Returning a result makes the decision visible. The handler does not silently
modify the request.

## 2) Keep the request immutable

Add a method that creates a new request instead of mutating the existing one:

```java
public record SupportRequest(String issue, Priority priority) {

    public SupportRequest withPriority(Priority newPriority) {
        return new SupportRequest(issue, newPriority);
    }
}
```

The original request remains unchanged:

```java
SupportRequest original =
        new SupportRequest("Account may be compromised", Priority.LOW);

SupportRequest escalated = original.withPriority(Priority.HIGH);
```

Now `original.priority()` is still `LOW`, while `escalated.priority()` is
`HIGH`. This makes the state transition easier to understand, test, and audit.

## 3) Return a result from each handler

Change the handler contract from:

```java
void handle(SupportRequest request);
```

to:

```java
public interface SupportHandler {

    SupportHandler setNext(SupportHandler nextHandler);

    HandlingResult handle(SupportRequest request);
}
```

The abstract handler still forwards requests that it cannot process:

```java
@Override
public final HandlingResult handle(SupportRequest request) {
    if (canHandle(request)) {
        return process(request);
    }

    if (nextHandler == null) {
        throw new IllegalStateException("No handler accepted the request");
    }

    return nextHandler.handle(request);
}

protected abstract HandlingResult process(SupportRequest request);
```

The request continues forward until a handler either resolves it or asks for a
priority change.

## 4) Let a handler request escalation

Basic support could discover that an apparently low-priority request is
actually a security incident:

```java
@Override
protected HandlingResult process(SupportRequest request) {
    if (request.issue().contains("compromised")) {
        return new HandlingResult.ChangePriority(
                Priority.HIGH,
                "BASIC_SUPPORT",
                "Possible security incident"
        );
    }

    return new HandlingResult.Resolved("BASIC_SUPPORT");
}
```

The handler does not change the request itself. It returns a proposal that
another component must authorize.

## 5) Let a handler request de-escalation

A manager might inspect a high-priority request and determine that it is a
routine password reset:

```java
return new HandlingResult.ChangePriority(
        Priority.LOW,
        "MANAGER",
        "Confirmed to be a routine password reset"
);
```

Calling `nextHandler` would not be sufficient here. The next link only moves
forward, so a request changed from `HIGH` to `LOW` would never return to basic
support.

## 6) Use a coordinator to restart the chain

A coordinator retains the first handler and restarts processing whenever an
approved priority change creates a new request:

```java
public class SupportChain {

    private static final int MAX_PRIORITY_CHANGES = 3;

    private final SupportHandler firstHandler;
    private final PriorityChangePolicy priorityChangePolicy;

    public SupportChain(
            SupportHandler firstHandler,
            PriorityChangePolicy priorityChangePolicy
    ) {
        this.firstHandler = firstHandler;
        this.priorityChangePolicy = priorityChangePolicy;
    }

    public void handle(SupportRequest originalRequest) {
        SupportRequest currentRequest = originalRequest;

        for (int changes = 0; changes < MAX_PRIORITY_CHANGES; changes++) {
            HandlingResult result = firstHandler.handle(currentRequest);

            if (result instanceof HandlingResult.Resolved resolved) {
                System.out.println(
                        resolved.handledBy() + " resolved: "
                                + currentRequest.issue()
                );
                return;
            }

            HandlingResult.ChangePriority change =
                    (HandlingResult.ChangePriority) result;

            priorityChangePolicy.authorize(currentRequest, change);

            currentRequest = currentRequest.withPriority(
                    change.newPriority()
            );

            System.out.println(
                    "Priority changed to " + currentRequest.priority()
                            + ": " + change.reason()
            );
        }

        throw new IllegalStateException("Too many priority changes");
    }
}
```

The maximum protects the application from a badly configured flow that
repeatedly changes a request from `LOW` to `HIGH` and back to `LOW`.

## 7) Escalation flow

```text
LOW request
    |
    v
Basic support detects a security risk
    |
    v
Returns a request to change priority to HIGH
    |
    v
Priority policy authorizes and audits the change
    |
    v
Coordinator creates an immutable HIGH-priority request
    |
    v
Coordinator restarts the chain
    |
    v
Basic support skips -> Supervisor skips -> Manager resolves
```

## 8) De-escalation flow

```text
HIGH request
    |
    v
Manager determines that the issue is routine
    |
    v
Returns a request to change priority to LOW
    |
    v
Priority policy authorizes and audits the change
    |
    v
Coordinator creates an immutable LOW-priority request
    |
    v
Coordinator restarts the chain
    |
    v
Basic support resolves
```

Restarting is important because a linked chain naturally moves in only one
direction.

## 9) Put authorization in a policy

Handlers should not automatically authorize their own requested changes:

```java
public class PriorityChangePolicy {

    public void authorize(
            SupportRequest request,
            HandlingResult.ChangePriority change
    ) {
        boolean isDeescalation =
                change.newPriority().severity()
                        < request.priority().severity();

        if (isDeescalation
                && !change.requestedBy().equals("MANAGER")) {
            throw new SecurityException(
                    "Only a manager may decrease priority"
            );
        }

        if (change.reason() == null || change.reason().isBlank()) {
            throw new IllegalArgumentException(
                    "A priority change requires a reason"
            );
        }

        // Record old priority, new priority, requester, reason, and time.
    }
}
```

Give each priority an explicit severity instead of relying on enum declaration
order:

```java
public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int severity;

    Priority(int severity) {
        this.severity = severity;
    }

    public int severity() {
        return severity;
    }
}
```

## 10) Do not trust a requester string in production

The string `"MANAGER"` is suitable only for explaining the idea. A handler
could supply that string even when it is not operating with manager authority.

A production application should pass an authenticated actor or capability to
the policy:

```java
priorityChangePolicy.authorize(
        currentRequest,
        change,
        authenticatedActor
);
```

The policy can then check permissions owned by the authenticated actor rather
than trusting a value created by the handler.

## 11) Store an audit record

Every approved change should capture at least:

```text
request identifier
old priority
new priority
requested by
approved by
reason
timestamp
```

This provides a history of what changed and why. It is especially important
when priority affects response-time guarantees, security handling, or customer
impact.

## Why this design is preferable

- The request remains immutable.
- Handlers suggest changes but do not authorize themselves.
- Escalation and de-escalation both work because the chain restarts.
- Every priority change requires a reason and can be audited.
- The coordinator prevents an infinite reprioritization loop.
- Routing and authorization remain separate responsibilities.

The key idea is that **detecting the need for a priority change**, **approving
that change**, and **routing the updated request** are three different jobs.
