# 2. Access Modifiers, `final`, and `static`

## Access modifiers

Java controls access on top-level types and on class members.

| Modifier | Same class | Same package | Subclass elsewhere | Everywhere |
|---|---:|---:|---:|---:|
| `private` | yes | no | no | no |
| no modifier (package-private) | yes | yes | no | no |
| `protected` | yes | yes | yes, through inheritance | no |
| `public` | yes | yes | yes | yes |

Package-private is written by omitting a modifier:

```java
abstract class AbstractPaymentProcessor { ... }
```

That class can be used only inside its package. This is different from C++,
where class members default to `private`; Java class members with no modifier
are package-private.

Typical choices in this repository:

- `private` fields hide implementation details.
- `public` methods form the API other objects call.
- `protected` constructors/methods support subclasses.
- package-private helper types stay internal to one package.

## The three meanings of `final`

### Final variable or field

```java
private final Map<PaymentMethod, PaymentProcessor> processors;
```

The field must be assigned exactly once, either at declaration or during every
constructor path. The reference cannot later point to another map.

`final` does **not** make the object immutable:

```java
final List<String> names = new ArrayList<>();
names.add("Gate A");       // allowed: the same list is mutated
// names = new ArrayList<>(); // not allowed: reassignment
```

Use `List.copyOf`, `Map.copyOf`, `List.of`, or `Map.of` when the collection
itself should be unmodifiable.

### Final method

A `final` method cannot be overridden by a subclass. This form is uncommon in
the current repository, but it is valid Java.

### Final class

```java
public final class PaymentProcessorFactory { ... }
```

No class can `extend PaymentProcessorFactory`. This communicates that the type
is complete rather than a base class. It does not make every object field
immutable.

## `static`: class-level instead of object-level

An instance member needs an object:

```java
factory.getProcessor(PaymentMethod.CARD);
```

A static member belongs to the class and is called through the type:

```java
Map.of(...);
Math.max(4, 9);
```

Common uses are constants, stateless utility methods, factory methods, and the
program entry point:

```java
public static void main(String[] args) { ... }
```

- `public`: the launcher can call it.
- `static`: no application object is needed first.
- `void`: it returns nothing.
- `String[] args`: command-line arguments as an array of strings.

## Static fields and constants

One static field is shared by all instances. A Java constant is conventionally
`static final` and named in uppercase:

```java
private static final int MAX_RETRIES = 3;
```

Be careful with mutable static fields: they are global shared state.

## Static nested classes

A class declared inside another class can be `static`:

```java
static class InMemoryEventBus { ... }
```

It is namespaced inside the outer class but does not carry a hidden reference
to an outer-class object. A non-static inner class does carry that reference.
The observable-pattern example uses static nested classes to keep supporting
demo types together.