# Java Syntax Guide for This Repository

This guide explains the Java syntax used in this repository. It assumes you
already understand OOP from C++ or JavaScript, so it focuses on what looks new
or behaves differently in Java.

## Recommended order

1. [Java file anatomy](01_FILE_ANATOMY_AND_CLASSES.md)
2. [Access modifiers, `final`, and `static`](02_MODIFIERS_FINAL_AND_STATIC.md)
3. [Interfaces, abstract classes, and inheritance](03_INTERFACES_AND_INHERITANCE.md)
4. [Constructors and dependency injection](04_CONSTRUCTORS_AND_DEPENDENCIES.md)
5. [Enums, records, generics, and collections](05_TYPES_AND_COLLECTIONS.md)
6. [Exceptions, null, and `Optional`](06_ERRORS_NULL_AND_OPTIONAL.md)
7. [Lambdas, method references, and streams](07_LAMBDAS_AND_STREAMS.md)
8. [Annotations, modern syntax, and concurrency](08_MORE_REPOSITORY_SYNTAX.md)

## Quick translation table

| Java | Meaning | Rough C++ / JavaScript comparison |
|---|---|---|
| `package` | Namespace and expected folder | C++ namespace / JS module path |
| `import` | Lets a file use a short type name | C++ `#include` is only a rough comparison; JS `import` is closer |
| `extends` | Inherits one class | C++ class inheritance |
| `implements` | Promises to satisfy an interface | C++ pure-virtual contract / TypeScript `implements` |
| `final` field | Reference can be assigned once | C++ `const`-like, but the referenced object may still mutate |
| `final` class | Cannot be subclassed | C++ `final` class |
| `static` | Belongs to the class, not an object | C++ static member |
| `abstract` | Incomplete class or method | C++ abstract base / pure virtual method |
| `record` | Compact immutable data carrier | Similar goal to a C++ value type or a frozen JS data object |
| `enum` | Fixed set of typed constants | C++ scoped enum, but Java enums can have fields and methods |
| `@Override` | Compiler-checked override marker | C++ `override` |
| `synchronized` | Locks on an object while a method/block runs | Similar purpose to using a mutex |
| `->` | Lambda or modern `switch` branch | JS/C++ lambda arrow; context determines the meaning |
| `::` | Method reference | C++ function reference / JS passing a function |

## A useful reading habit

Read a Java declaration from left to right:

```java
public final class PaymentProcessorFactory
```

- `public`: code in other packages may use it.
- `final`: another class cannot extend it.
- `class`: this declares a class.
- `PaymentProcessorFactory`: the type name; the public type and file name match.

For a field:

```java
private final Map<PaymentMethod, PaymentProcessor> processors;
```

- only this class can access it directly;
- its reference is assigned once;
- it maps `PaymentMethod` keys to `PaymentProcessor` values;
- it is declared here and assigned by the constructor.

Use this guide beside the code. Each chapter points to examples already present
under `src/designpatterns`, `src/solidprinciples`, and `src/LLD`.