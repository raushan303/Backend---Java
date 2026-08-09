# Backend---Java

A hands-on Java backend learning repository for practicing design patterns, SOLID principles, low-level design (LLD), and later backend/API development.

## Goal

This repo is organized for progressive learning:

1. Design patterns
2. SOLID principles
3. LLD exercises
4. Java backend/API projects

## Environment

This repo is intended to be used in GitHub Codespaces or any local Java environment.

Installed / verified:
- Java: `openjdk 25.0.2`
- `javac`: `25.0.2`

## Suggested folder structure

```text
Backend---Java/
├── design-patterns/
│   ├── factory/
│   ├── strategy/
│   └── observer/
├── solid-principles/
│   ├── srp/
│   ├── ocp/
│   ├── lsp/
│   ├── isp/
│   └── dip/
├── lld/
│   ├── parking-lot/
│   ├── splitwise/
│   └── tic-tac-toe/
└── backend-projects/
    ├── basic-api/
    └── spring-boot-api/
```

## How to use this repo

### 1. Start with design patterns
Create small examples that:
- define classes and interfaces
- create objects
- print outputs
- help you understand object relationships

### 2. Practice SOLID
Refactor the design pattern examples to see:
- single responsibility
- open/closed
- Liskov substitution
- interface segregation
- dependency inversion

### 3. Move to LLD
Use class diagrams and code implementations for problems like:
- parking lot
- splitwise
- tic-tac-toe
- elevator
- snake and ladder

### 4. Build backend projects
Later, use this repo for:
- REST APIs
- service/repository layers
- validation
- exception handling
- testing

## Running Java code

If you are using a plain Java file:

```bash
javac App.java
java App
```

If you are using packages, make sure:
- the folder structure matches the package name
- you compile from the correct root folder

## Learning approach

For each topic:
- write a first version
- run it
- observe output
- refactor it
- compare the design

## Notes

This repo is meant for experimentation and learning, not production code.

## Next steps

- Add your first design pattern example
- Add SOLID practice examples
- Add LLD problem implementations
- Create a Maven-based backend project when ready