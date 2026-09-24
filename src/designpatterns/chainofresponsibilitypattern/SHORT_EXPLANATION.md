# Chain of Responsibility Pattern

## Start with the problem

Imagine a customer-support system with three support levels:

- basic support handles low-priority requests;
- a supervisor handles medium-priority requests;
- a manager handles high-priority requests.

The client could use a large `if/else` block to choose a support level, but then
the client must understand every routing rule. Those rules become harder to
maintain when support levels are added or reordered.

## The solution

Connect the handlers into a chain:

```java
SupportHandler basicSupport = new BasicSupportHandler();
SupportHandler supervisor = new SupervisorSupportHandler();
SupportHandler manager = new ManagerSupportHandler();

basicSupport.setNext(supervisor).setNext(manager);
basicSupport.handle(request);
```

The request always enters through `basicSupport`. If basic support cannot
handle it, the request goes to the supervisor. If the supervisor cannot handle
it, the request goes to the manager.

## Roles in this example

- **Handler:** `SupportHandler`, the common contract for handling and linking.
- **Base handler:** `AbstractSupportHandler`, which contains forwarding logic.
- **Concrete handlers:** `BasicSupportHandler`,
  `SupervisorSupportHandler`, and `ManagerSupportHandler`.
- **Request:** `SupportRequest`, the object moving through the chain.
- **Client:** `ChainOfResponsibilityPatternDemo`, which builds and starts the
  chain.

## When Chain of Responsibility is useful

Use it when:

- several objects may handle a request;
- the correct handler depends on runtime data;
- handlers may be added, removed, or reordered;
- the sender should not choose the final receiver.

Common examples include approval workflows, logging levels, authentication
filters, and customer-support escalation.

Do not use it when routing is simple and fixed, or when silently unhandled
requests would be dangerous. This example throws an exception when no handler
can process a request so that a missing chain configuration is visible.

## Interview description

The **Chain of Responsibility Pattern** passes a request through a linked
sequence of handlers. Each handler either processes the request or forwards it
to the next handler, which decouples the sender from the final receiver.
