# 6. Exceptions, `null`, and `Optional`

## Throwing an exception

```java
if (processor == null) {
    throw new IllegalArgumentException(
            "No processor for payment method " + paymentMethod);
}
```

`throw` stops normal control flow and propagates an exception until matching
handling code catches it. `+` concatenates strings here.

## Checked and unchecked exceptions

Java has two main categories:

- Checked exceptions extend `Exception` but not `RuntimeException`. A method
  must catch them or declare them with `throws`.
- Unchecked exceptions extend `RuntimeException`. Declaring or catching them is
  optional; they usually signal bad arguments, invalid state, or programming
  errors.

The repository's custom parking exceptions extend `RuntimeException`, so calls
remain uncluttered while failures still have domain-specific names.

```java
public final class InvalidParkingTicketException extends RuntimeException {
    public InvalidParkingTicketException(String message) {
        super(message);
    }
}
```

`super(message)` passes the message to the parent exception constructor.

## `try`, `catch`, `finally`

```java
try {
    riskyOperation();
} catch (SomeException exception) {
    handle(exception);
} finally {
    cleanup();
}
```

`catch` handles matching failures. `finally` runs whether the operation succeeds
or throws. For resources such as files and streams, prefer try-with-resources:

```java
try (BufferedReader reader = Files.newBufferedReader(path)) {
    return reader.readLine();
}
```

Java automatically closes resources implementing `AutoCloseable`.

## `null`

Any reference type may hold `null`; primitives cannot. Calling a method through
`null` causes `NullPointerException`. JavaScript's `undefined` does not have a
direct Java equivalent.

Validate required values early:

```java
this.managedType = Objects.requireNonNull(
        managedType, "Managed spot type cannot be null");
```

`requireNonNull` returns the value when valid and throws
`NullPointerException` with the supplied message otherwise. Returning the value
allows validation and assignment in one expression.

## `Optional<T>`

`Optional<ParkingSpot>` represents either one spot or no spot. It is commonly
used as a return type when absence is normal:

```java
ParkingSpot spot = strategy.selectSpot(spots, entranceId)
        .orElseThrow(() -> new NoParkingSpotAvailableException("No spot"));
```

Common operations:

- `Optional.of(value)`: definitely non-null value;
- `Optional.ofNullable(value)`: value or empty when null;
- `Optional.empty()`: no value;
- `map(...)`: transform a present value;
- `orElse(defaultValue)`: eager default;
- `orElseGet(() -> defaultValue)`: lazy default;
- `orElseThrow(...)`: create an exception when absent.

Avoid calling `.get()` without first proving a value is present. Also avoid
using `Optional` for every field and parameter; it works best as a return type.

## Validation style in the repository

- `IllegalArgumentException`: caller supplied an invalid value.
- `Objects.requireNonNull`: a required reference was null.
- Domain exception: the operation failed for a meaningful business reason.
- Record compact constructor: reject invalid data at creation time.

This keeps invalid objects from spreading deeper into the system.