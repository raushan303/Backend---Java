# 7. Lambdas, Method References, and Streams

## Lambdas

A lambda is a function value that implements a compatible functional interface
(an interface with one abstract method):

```java
candidate -> candidate.getId().equals(spotId)
```

This takes `candidate` and returns a boolean. Java usually infers the parameter
type from context. Other forms are:

```java
() -> createDefault()
(left, right) -> left.compareTo(right)
value -> {
    log(value);
    return transform(value);
}
```

Unlike JavaScript functions, a lambda cannot freely mutate captured local
variables. Captured locals must be final or **effectively final** (assigned only
once). Object state referenced by such a variable may still mutate.

## Method references

When a lambda only calls an existing method, `::` can be shorter:

```java
spots.forEach(this::validateAndAdd);
spots.stream().filter(ParkingSpot::isAvailable);
```

These correspond roughly to:

```java
spots.forEach(spot -> this.validateAndAdd(spot));
spots.stream().filter(spot -> spot.isAvailable());
```

Common forms are `object::instanceMethod`, `Type::staticMethod`,
`Type::instanceMethod`, and `Type::new`.

## Streams

A stream is a pipeline over data, not a collection that stores data:

```java
return parkingSpots.stream()
        .filter(spot -> spot.getType() == type)
        .map(spot -> (ParkingSpot) spot)
        .toList();
```

Read it as:

1. obtain a stream from the collection;
2. keep matching spots;
3. transform each element;
4. collect the result into a list.

Intermediate operations such as `filter` and `map` are lazy. Work begins when
a terminal operation such as `toList`, `count`, `findFirst`, `anyMatch`, or
`forEach` requests a result.

## Operations used in this repository

| Operation | Purpose |
|---|---|
| `filter(predicate)` | Keep matching elements |
| `map(function)` | Transform each element |
| `sorted(comparator)` | Order elements |
| `findFirst()` | Return an `Optional` containing the first item |
| `anyMatch(predicate)` | Test whether at least one item matches |
| `count()` | Count elements and return `long` |
| `forEach(consumer)` | Perform an action for each item |
| `toList()` | Produce an unmodifiable list in modern Java |

## Chaining and indentation

Java allows a newline before each `.` in a chain. The expression remains one
statement until its semicolon:

```java
ParkingSpot spot = spots.stream()
        .filter(candidate -> candidate.getId().equals(spotId))
        .findFirst()
        .orElseThrow(() -> new ParkingSpotOperationException("Unknown spot"));
```

Each step's return type supplies the next step's methods. Here `findFirst`
returns `Optional<ParkingSpot>`, then `orElseThrow` returns `ParkingSpot` or
throws.

## Comparators

A comparator defines ordering without changing the class:

```java
Comparator.comparingInt(spot -> distanceFromEntrance(spot, entranceId))
```

Comparators can be chained with `thenComparing`. This is the Java equivalent of
passing a comparison callback to a JavaScript sort, but it is strongly typed.

## Streams versus loops

Use a stream when the code naturally reads as filter/transform/aggregate. Use a
loop when control flow, mutation, early exits, or debugging would be clearer.
Streams are not automatically faster, and parallel streams are not a default
performance solution.