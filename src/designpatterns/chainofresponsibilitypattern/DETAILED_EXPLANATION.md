# Chain of Responsibility Pattern - Detailed Guide

## 1) What problem it solves

The Chain of Responsibility Pattern is useful when more than one object may be
able to process a request, but the sender should not contain the logic for
selecting that object.

Without the pattern, routing often becomes one large conditional:

```java
if (request.priority() == Priority.LOW) {
    // basic support logic
} else if (request.priority() == Priority.MEDIUM) {
    // supervisor logic
} else {
    // manager logic
}
```

With the pattern, each support level owns one decision. A handler processes a
matching request and forwards every other request.

---

## 2) Problem statement

Design a support-ticket flow where requests are escalated according to their
priority.

The solution should:

- give each support level one responsibility;
- keep handler-selection logic away from the client;
- allow the chain order to be configured at runtime;
- stop after one handler processes the request;
- fail clearly if no handler is available.

---

## 3) Core participants in this folder

- `SupportRequest` - request data containing an issue and priority
- `Priority` - low, medium, or high request classification
- `SupportHandler` - handler contract
- `AbstractSupportHandler` - shared processing and forwarding algorithm
- `BasicSupportHandler` - handles low-priority requests
- `SupervisorSupportHandler` - handles medium-priority requests
- `ManagerSupportHandler` - handles high-priority requests
- `ChainOfResponsibilityPatternDemo` - builds and runs the chain

### Folder grouping

```text
chainofresponsibilitypattern/
├── advanced/
│   └── README.md
├── example/
│   └── ChainOfResponsibilityPatternDemo.java
├── handler/
│   ├── SupportHandler.java
│   ├── AbstractSupportHandler.java
│   ├── BasicSupportHandler.java
│   ├── SupervisorSupportHandler.java
│   └── ManagerSupportHandler.java
├── model/
│   ├── Priority.java
│   └── SupportRequest.java
├── README.md
├── QNA.md
├── PRIORITY_ESCALATION_AND_DEESCALATION.md
├── SHORT_EXPLANATION.md
└── DETAILED_EXPLANATION.md
```

---

## 4) Class diagram

```text
                 +-------------------------+
                 |     SupportHandler      |
                 +-------------------------+
                 | +setNext(handler)       |
                 | +handle(request)        |
                 +------------^------------+
                              |
                 +-------------------------+
                 | AbstractSupportHandler  |
                 +-------------------------+
                 | -nextHandler            |
                 | +handle(request)        |
                 | #canHandle(request)     |
                 | #process(request)       |
                 +------------^------------+
                              |
           +------------------+------------------+
           |                  |                  |
  +----------------+  +----------------+  +----------------+
  | Basic Support  |  |   Supervisor   |  |    Manager     |
  +----------------+  +----------------+  +----------------+
```

---

## 5) Request flow

For a high-priority request:

```text
Client
  |
  v
BasicSupportHandler -- cannot handle, forwards
  |
  v
SupervisorSupportHandler -- cannot handle, forwards
  |
  v
ManagerSupportHandler -- handles request, chain stops
```

The steps are:

1. The client creates each handler.
2. The client links them with `setNext`.
3. The client sends a request to the first handler.
4. `AbstractSupportHandler.handle` calls `canHandle`.
5. A matching handler calls `process` and returns.
6. A non-matching handler calls `handle` on its next handler.
7. If no next handler exists, the code throws an exception instead of losing
   the request.

---

## 6) Why `setNext` returns a handler

Returning the next handler makes chain construction readable:

```java
basicSupport.setNext(supervisor).setNext(manager);
```

The first call links basic support to the supervisor and returns the
supervisor. The second call therefore links the supervisor to the manager.
This syntax is convenient, but separate calls work too:

```java
basicSupport.setNext(supervisor);
supervisor.setNext(manager);
```

---

## 7) Error handling choices

This implementation rejects invalid state early:

- `SupportRequest` rejects a blank issue or `null` priority.
- `setNext` rejects a `null` next handler.
- `handle` rejects a `null` request.
- the final handler throws `IllegalStateException` when no handler accepts the
  request.

The last rule is important. A request should not disappear just because the
chain was assembled incorrectly.

---

## 8) Benefits

- Decouples the sender from the final receiver.
- Gives each handler a focused responsibility.
- Allows handlers to be reordered at runtime.
- Reuses common forwarding logic.
- Supports adding handlers without changing the sender.

## 9) Trade-offs

- The processing route can be harder to see than a direct method call.
- A badly configured chain may leave a request unhandled.
- Long chains may perform unnecessary checks.
- The chain order affects behavior and therefore needs testing.

---

## 10) Real-world use cases

- Servlet filters and web middleware
- Authentication and authorization checks
- Purchase or leave-approval workflows
- Logger levels
- Customer-support escalation
- Validation pipelines

### Chain of Responsibility compared with nearby ideas

- **Decorator:** every wrapper usually adds behavior; a chain handler may do
  nothing except forward the request.
- **Strategy:** the client or context selects one algorithm; a chain discovers
  the appropriate handler by passing the request along.
- **Command:** packages an action as an object; a command may itself be sent
  through a chain of handlers.

---

## 11) Best practices

1. Keep each handler focused on one rule.
2. Make the chain order clear where it is assembled.
3. Decide explicitly what happens when no handler accepts a request.
4. Avoid hidden cycles when linking handlers.
5. Add tests for both handled and unhandled requests when using the pattern in
   production code.
