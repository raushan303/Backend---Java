# 5. Enums, Records, Generics, and Collections

## Enums

An enum defines a closed set of typed instances:

```java
public enum PaymentMethod {
    CASH,
    CARD,
    UPI
}
```

Use `PaymentMethod.CARD`, not an arbitrary string such as `"card"`. The
compiler can check it, `switch` can cover it, and `==` comparison is safe.

Java enums are classes. They may have fields, constructors, and methods, as
`VehicleType` does. Enum constructors are not called with `new`; Java creates
the declared constants.

Useful generated methods include `PaymentMethod.values()` (all constants) and
`PaymentMethod.valueOf("CARD")` (exact name lookup, or an exception).

## Records

```java
public record Vehicle(String registrationNumber, VehicleType type) {
    public Vehicle {
        // validation
    }
}
```

A record is a concise data carrier. That declaration provides:

- private final component fields;
- accessor methods `registrationNumber()` and `type()`;
- a constructor;
- value-based `equals`, `hashCode`, and `toString`.

Record accessors are not JavaBean getters: use `vehicle.type()`, not
`vehicle.getType()`. The compact constructor `public Vehicle { ... }` validates
constructor arguments without repeating the parameter list. Java assigns the
components after the compact constructor body completes.

A record is implicitly final. Its component references cannot be reassigned,
but a referenced mutable object can still mutate, so this is shallow
immutability.

## Generics

```java
Map<PaymentMethod, PaymentProcessor>
List<ParkingSpot>
Optional<ParkingSpot>
```

Angle brackets specify type parameters. They make invalid combinations fail at
compile time and reduce casts. Java generics are mainly implemented with type
erasure, so `List<String>` and `List<Integer>` are not distinct runtime classes.
Primitive types cannot be generic arguments; use wrapper types such as
`Integer`, `Long`, `Double`, and `Boolean`.

The diamond operator lets Java infer constructor arguments:

```java
List<ParkingSpot> spots = new ArrayList<>();
```

## Bounded wildcards

```java
Collection<? extends ParkingSpot> spots
```

This accepts a collection of `ParkingSpot` or any subclass. It is useful for
reading values as `ParkingSpot`; adding values is generally unsafe because the
exact element subtype is unknown.

A practical memory aid is PECS:

- producer: `? extends T`;
- consumer: `? super T`.

Unlike arrays, generic collections are invariant: `List<CarSpot>` is not a
subtype of `List<ParkingSpot>`.

## Collection interfaces and implementations

Declare the broad interface and construct a suitable implementation:

```java
Map<String, EntranceGate> gates = new HashMap<>();
Map<PaymentMethod, PaymentProcessor> processors =
        new EnumMap<>(PaymentMethod.class);
List<ParkingSpot> spots = new ArrayList<>();
```

- `List`: ordered sequence, duplicates allowed.
- `Set`: unique elements.
- `Map`: key-value associations; it is not a subtype of `Collection`.
- `ArrayList`: resizable array implementation.
- `HashMap`: general hash-based map.
- `EnumMap`: compact, fast map whose keys belong to one enum type.

## Collection factory and copy methods

```java
List.of(a, b)
Map.of(key1, value1, key2, value2)
List.copyOf(existingList)
Map.copyOf(existingMap)
```

These return unmodifiable collections and reject `null`. `of` creates from
listed values; `copyOf` takes a snapshot of another collection. “Unmodifiable”
does not make contained objects deeply immutable.

## Enhanced `for`, arrays, and varargs

```java
for (String gateId : gateIds) {
    // use gateId
}
```

This is Java's for-each loop. Arrays use `String[]`; their size is fixed and
their length is `array.length`. A parameter such as `String... ids` is varargs:
callers may pass several strings, while the method receives an array.

## Autoboxing

Java can convert between a primitive and its wrapper in common contexts:

```java
Integer boxed = 42; // boxes int
int value = boxed;  // unboxes Integer; fails if boxed is null
```

Use `.equals()` for wrapper value comparison rather than relying on `==`.