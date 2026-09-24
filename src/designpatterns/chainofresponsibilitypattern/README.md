# Chain of Responsibility Pattern

This example uses a customer-support request to show how several handlers can
get a chance to process the same request.

For common design questions, including when a Factory would be simpler, see
[`QNA.md`](./QNA.md).

For a production-oriented extension that supports authorized priority changes,
see
[`PRIORITY_ESCALATION_AND_DEESCALATION.md`](./PRIORITY_ESCALATION_AND_DEESCALATION.md).
Its executable version is kept separately in
[`advanced/`](./advanced/README.md), so the original beginner example remains
unchanged.

## See the problem first

Without this pattern, one large method may contain every routing rule:

```java
if (request.priority() == Priority.LOW) {
    basicSupport.resolve(request);
} else if (request.priority() == Priority.MEDIUM) {
    supervisor.resolve(request);
} else if (request.priority() == Priority.HIGH) {
    manager.resolve(request);
}
```

As more priorities and rules are added, this method becomes harder to change
and test. It also needs to know every concrete support level.

With Chain of Responsibility, the client sends the request to the first
handler:

```java
basicSupport.setNext(supervisor).setNext(manager);
basicSupport.handle(request);
```

Each handler makes one decision:

1. Can I handle this request?
2. If yes, process it.
3. If no, pass it to the next handler.

## Q1) What is the "chain"?

**Answer:** The chain is a sequence of handler objects:

```text
Basic support -> Supervisor -> Manager
```

Each handler stores a reference to the next handler. The request starts at
basic support and moves forward only when the current handler cannot process it.

## Q2) Why does the client call only the first handler?

**Answer:** The first handler is the entry point to the complete chain. The
client does not need to choose a specific handler because each object applies
its own rule and forwards the request when necessary.

This keeps routing knowledge out of the client.

## Q3) Why use `AbstractSupportHandler`?

**Answer:** Every handler needs the same forwarding steps. The abstract class
implements those steps once:

- check whether the current handler can process the request;
- process it when possible;
- otherwise forward it to the next handler;
- report an error if the chain ends without a suitable handler.

Concrete handlers only define what they can handle and how they process it.

## Q4) Does every request visit every handler?

**Answer:** No. Processing stops as soon as one handler accepts the request.
A low-priority request stops at basic support, while a high-priority request
travels through basic support and the supervisor before reaching the manager.

## When should I not use it?

Avoid this pattern when there is only one fixed handler or when the caller must
know exactly which object will process the request. A chain can make the route
less obvious, especially when it is assembled dynamically.

## Class diagram

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
                 +------------^------------+
                              |
           +------------------+------------------+
           |                  |                  |
  +----------------+  +----------------+  +----------------+
  | Basic Support  |  |   Supervisor   |  |    Manager     |
  +----------------+  +----------------+  +----------------+

Request flow:
Basic Support -> Supervisor -> Manager
```

Run `designpatterns.chainofresponsibilitypattern.example.ChainOfResponsibilityPatternDemo`
to see low-, medium-, and high-priority requests move through the chain.
