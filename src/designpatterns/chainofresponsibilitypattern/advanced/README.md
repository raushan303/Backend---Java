# Advanced Chain of Responsibility Example

This folder keeps the original beginner example intact and adds executable code
for priority escalation and de-escalation.

## What this version demonstrates

- immutable support requests;
- handlers that return explicit results;
- escalation from `LOW` to `HIGH`;
- manager-authorized de-escalation from `HIGH` to `LOW`;
- restarting the chain after a priority change;
- an audit record for every approved change;
- a limit that prevents endless reprioritization loops;
- scenario tests for routing and authorization.

## Folder structure

```text
advanced/
├── audit/
│   └── PriorityChangeAudit.java
├── example/
│   ├── AdvancedChainOfResponsibilityDemo.java
│   └── AdvancedSupportChainScenarioTest.java
├── handler/
│   ├── SupportHandler.java
│   ├── AbstractSupportHandler.java
│   ├── BasicSupportHandler.java
│   ├── SupervisorSupportHandler.java
│   └── ManagerSupportHandler.java
├── model/
│   ├── Priority.java
│   ├── SupportActor.java
│   ├── SupportRequest.java
│   └── SupportRole.java
├── policy/
│   ├── PriorityChangePolicy.java
│   └── PriorityChangeResult.java
├── result/
│   └── HandlingResult.java
├── SupportChainCoordinator.java
├── SupportOutcome.java
└── README.md
```

## How the flow works

Handlers return either:

```text
Resolved
```

or:

```text
PriorityChangeRequested
```

The `SupportChainCoordinator` receives the result. A resolved request becomes a
`SupportOutcome`. A requested priority change goes through
`PriorityChangePolicy`, which authorizes it, creates an audit record, and
returns a new immutable request.

The coordinator then starts that new request from the beginning of the chain.
Restarting allows both forward escalation and backward de-escalation:

```text
LOW security request
    -> Basic support requests HIGH
    -> Policy approves
    -> Chain restarts
    -> Manager resolves

HIGH routine password reset
    -> Manager requests LOW
    -> Policy approves
    -> Chain restarts
    -> Basic support resolves
```

## Authorization boundary

This learning example gives each handler a `SupportActor` and checks that only
a manager can lower priority. In production, the actor should come from an
authenticated application context rather than being created by the handler or
trusted from request input.

The audit record captures:

- request id;
- old and new priority;
- requesting actor;
- reason;
- timestamp.

A production application would persist that record in the same transaction as
the priority update or publish it through a reliable outbox.

## Run the demo

From the repository root:

```bash
build_dir=$(mktemp -d)
javac -d "$build_dir" \
  $(find src/designpatterns/chainofresponsibilitypattern/advanced -name '*.java')
java -cp "$build_dir" \
  designpatterns.chainofresponsibilitypattern.advanced.example.AdvancedChainOfResponsibilityDemo
rm -rf "$build_dir"
```

## Run the scenario tests

```bash
build_dir=$(mktemp -d)
javac -d "$build_dir" \
  $(find src/designpatterns/chainofresponsibilitypattern/advanced -name '*.java')
java -ea -cp "$build_dir" \
  designpatterns.chainofresponsibilitypattern.advanced.example.AdvancedSupportChainScenarioTest
rm -rf "$build_dir"
```
