# 1. Java File Anatomy and Classes

## A typical file

From `src/LLD/ParkingLot/implementation/payment/PaymentProcessorFactory.java`:

```java
package LLD.ParkingLot.implementation.payment;

import LLD.ParkingLot.implementation.model.PaymentMethod;
import java.util.Map;

public final class PaymentProcessorFactory {
    // fields, constructors, and methods
}
```

Java conventionally allows one top-level `public` class per file. Its name must
match the file name exactly. Java is case-sensitive.

## `package`

`package` gives a type its full name and organizes it into a namespace:

```text
LLD.ParkingLot.implementation.payment.PaymentProcessorFactory
```

The directory path normally matches the package. Unlike a JavaScript module,
the package does not make each file private by itself. Access modifiers decide
which types and members are visible.

## `import`

An import allows the short name `Map` instead of `java.util.Map`. It does not
copy code into the file. Types in `java.lang`, including `String`, `Object`,
`System`, and common exceptions, are imported automatically.

```java
import java.util.Map;   // one type
import java.util.*;    // all public types in this package, not subpackages
```

Explicit imports are usually easier to read than `*` imports.

## Basic declarations

```java
private final String id;
public long availableSpotCount() { ... }
```

Java places the type before a variable or method name. A method declaration is:

```text
access-modifiers return-type method-name(parameters) { body }
```

`void` means the method returns no value. Statements normally end with `;`.
Blocks use `{}`. Java does not have JavaScript's automatic semicolon insertion.

## Primitive and reference types

Primitives hold values directly: `boolean`, `byte`, `short`, `int`, `long`,
`float`, `double`, and `char`. Reference variables point to objects, arrays, or
`null`: `String`, `Vehicle`, `List<ParkingSpot>`, and so on.

```java
long count = 0;                    // primitive
Vehicle vehicle = new Vehicle(...); // reference to a new object
```

Java is garbage-collected, so normal objects do not use C++ `delete`. Java has
no user-visible pointer arithmetic and no C++-style stack object syntax.

## `new`, `.`, and method calls

```java
PaymentProcessor processor = new CashPaymentProcessor();
processor.process(payment);
```

`new` constructs an object. `.` accesses a member. Parentheses call a method or
constructor. JavaScript looks similar, but Java checks the declared types at
compile time.

## `this`

`this` is the current object:

```java
this.processors = new EnumMap<>(PaymentMethod.class);
```

Here `this.processors` is the field, while a plain `processors` refers to the
constructor parameter with the same name. `this(...)` has a separate meaning:
it calls another constructor in the same class; chapter 4 explains it.

## Equality: `==` versus `.equals()`

- For primitives, `==` compares values.
- For object references, `==` asks whether both references point to the exact
  same object.
- `.equals()` asks whether objects are logically equal, when the type defines it.
- Enum constants are single instances, so comparing enums with `==` is correct.

This is why the repository uses `spot.getType() != managedType` for enums, but
`existing.getId().equals(spot.getId())` for strings.

## Fields, local variables, and parameters

- A **field** belongs to an object (or to a class when `static`).
- A **parameter** receives a value when a method/constructor is called.
- A **local variable** exists only inside its block.

Fields receive default values such as `0`, `false`, or `null`. Local variables
must be assigned before use. Constructors are commonly used to establish valid
field values immediately.