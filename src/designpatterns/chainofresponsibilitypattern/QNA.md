# Chain of Responsibility Pattern - Questions and Answers

## Why not use a Factory that returns a handler based on priority?

Yes, a Factory would be simpler **for this exact example** because each
priority maps directly to one known handler:

```java
SupportHandler handler = SupportHandlerFactory.getHandler(request.priority());
handler.handle(request);
```

The Factory would centralize this mapping:

```text
LOW    -> BasicSupportHandler
MEDIUM -> SupervisorSupportHandler
HIGH   -> ManagerSupportHandler
```

### Factory and Chain solve different problems

- **Factory:** "Which object should I create or return?"
- **Chain of Responsibility:** "I do not know which object can handle this
  request, so let the handlers decide."

A chain is more useful when:

- eligibility involves more than one simple field;
- rules can change independently;
- handlers can be reordered or inserted dynamically;
- one handler may attempt processing and then escalate;
- several handlers may inspect or process a request;
- the sender should not contain handler-selection knowledge.

For example, basic support might handle a medium-priority request for a premium
customer, while a payment-related request might need to skip directly to a
specialist. A simple priority-to-handler Factory would become increasingly full
of business-routing conditions. In a chain, each handler can own its eligibility
rule.

### Which approach is better for the current example?

In this learning example, each `canHandle` method only compares the request's
priority with one enum value. The chain is intentionally simple so that its
mechanics are easy to see, but this is not the strongest real-world reason to
use the pattern.

If this were production code with a permanent one-to-one mapping between
priority and handler, the Factory and direct-dispatch approach would be clearer
and would avoid unnecessary traversal.

The chain is used here to teach this flow:

```text
receive -> decide -> handle or forward -> stop
```

### Can Factory and Chain be used together?

Yes. A Factory can create or assemble a configured chain and return its first
handler. The request would still travel through that chain:

```java
SupportHandler supportChain = SupportChainFactory.create();
supportChain.handle(request);
```

However, if the Factory examines the priority and returns the final handler
directly, it replaces the Chain of Responsibility behavior rather than
complementing it.

### Simple decision guide

Use a **Factory with direct dispatch** when:

- one input value maps directly to one handler;
- the mapping is fixed and easy to understand;
- no fallback or escalation is needed.

Use a **Chain of Responsibility** when:

- several handlers may be eligible;
- the rules belong to individual handlers;
- fallback or escalation is required;
- handlers may be added, removed, or reordered;
- the sender should not select the final receiver.

---

## Can a handler increase or decrease the request priority?

Yes. A handler may discover information that justifies escalation or
de-escalation. For example:

- basic support may detect a possible security incident and request an increase
  from `LOW` to `HIGH`;
- a manager may determine that a high-priority request is routine and request a
  decrease to `LOW`.

However, a handler should normally **suggest** the change rather than silently
mutating the request.

The current `SupportRequest` is a Java `record`, so it is immutable. A new
request would need to be created:

```java
SupportRequest escalatedRequest =
        new SupportRequest(request.issue(), Priority.HIGH);
```

An even clearer API is:

```java
SupportRequest escalatedRequest =
        request.withPriority(Priority.HIGH);
```

The second form still creates a new immutable request; it does not modify the
original object.

## Should a handler be trusted to change priority?

Not merely because it implements `SupportHandler`. Trust should come from
explicit authorization rules.

Possible rules include:

- basic support may increase priority but may not decrease it;
- only a manager may de-escalate a request;
- a fraud detector may escalate any request to `HIGH`;
- every change must include a reason;
- every approved change must be recorded in an audit history.

Avoid exposing a general method such as:

```java
request.setPriority(Priority.HIGH);
```

If every handler can call that method freely, business rules can be bypassed
and changes become difficult to audit.

A better flow is:

```text
Handler suggests a priority change
        |
        v
Priority policy validates authorization
        |
        v
Audit event is recorded
        |
        v
A new immutable request is created
        |
        v
Processing restarts with the new priority
```

The detailed implementation is explained in
[`PRIORITY_ESCALATION_AND_DEESCALATION.md`](./PRIORITY_ESCALATION_AND_DEESCALATION.md).
