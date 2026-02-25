## Optional Important Methods in Java

> `Optional` is a class in Java (`java.util.Optional`).
> It is a **container object** that may or may not contain a non-null value.
> It is used to avoid `NullPointerException` and represent the absence of a value explicitly.
> It should be used as a **return type**, not as method parameters or fields.
```java
import java.util.Optional;

Optional<String> optional = Optional.of("Hello");
Optional<String> empty    = Optional.empty();
Optional<String> nullable = Optional.ofNullable(null);
```

---

### 1. Creation Methods

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `Optional.of(value)` | Creates Optional with non-null value | Yes (`NullPointerException` if null) |
| `Optional.ofNullable(value)` | Creates Optional with nullable value | No (returns empty if null) |
| `Optional.empty()` | Creates empty Optional | No |
```java
// of — non-null value
Optional<String> opt1 = Optional.of("Hello");         // Optional["Hello"]
Optional<String> opt2 = Optional.of(null);             // throws NullPointerException

// ofNullable — nullable value
Optional<String> opt3 = Optional.ofNullable("Hello"); // Optional["Hello"]
Optional<String> opt4 = Optional.ofNullable(null);    // Optional.empty

// empty
Optional<String> opt5 = Optional.empty();             // Optional.empty
```

---

### 2. Check Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `isPresent()` | Returns `true` if value is present | `boolean` |
| `isEmpty()` | Returns `true` if value is absent (Java 11+) | `boolean` |
```java
Optional<String> opt = Optional.of("Hello");
Optional<String> empty = Optional.empty();

opt.isPresent();    // true
opt.isEmpty();      // false

empty.isPresent();  // false
empty.isEmpty();    // true
```

---

### 3. Retrieval Methods

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `get()` | Returns value if present | Yes (`NoSuchElementException` if empty) |
| `orElse(defaultValue)` | Returns value or default | No |
| `orElseGet(supplier)` | Returns value or supplier result | No |
| `orElseThrow()` | Returns value or throws `NoSuchElementException` (Java 10+) | Yes |
| `orElseThrow(supplier)` | Returns value or throws custom exception | Yes |
```java
Optional<String> opt   = Optional.of("Hello");
Optional<String> empty = Optional.empty();

// get — unsafe, avoid if possible
opt.get();          // "Hello"
empty.get();        // throws NoSuchElementException

// orElse — returns default value (always evaluated)
opt.orElse("Default");      // "Hello"
empty.orElse("Default");    // "Default"

// orElseGet — returns supplier result (lazy evaluation, preferred)
opt.orElseGet(() -> "Generated Default");      // "Hello"
empty.orElseGet(() -> "Generated Default");    // "Generated Default"

// orElseThrow (Java 10+)
opt.orElseThrow();          // "Hello"
empty.orElseThrow();        // throws NoSuchElementException

// orElseThrow with custom exception
opt.orElseThrow(() -> new IllegalArgumentException("Value not found"));   // "Hello"
empty.orElseThrow(() -> new IllegalArgumentException("Value not found")); // throws
```

---

### 4. Transformation Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `map(function)` | Transforms value if present | `Optional<U>` |
| `flatMap(function)` | Transforms to Optional if present (avoids nested Optional) | `Optional<U>` |
| `filter(predicate)` | Returns Optional if predicate matches, else empty | `Optional<T>` |
| `or(supplier)` | Returns this Optional or alternative (Java 9+) | `Optional<T>` |
```java
Optional<String> opt = Optional.of("hello");

// map — transform value
Optional<String> upper = opt.map(String::toUpperCase);
// Optional["HELLO"]

Optional<Integer> length = opt.map(String::length);
// Optional[5]

Optional<String> emptyMap = Optional.<String>empty().map(String::toUpperCase);
// Optional.empty

// flatMap — avoid nested Optional
Optional<Optional<String>> nested = opt.map(s -> Optional.of(s.toUpperCase()));
// Optional[Optional["HELLO"]] — bad

Optional<String> flat = opt.flatMap(s -> Optional.of(s.toUpperCase()));
// Optional["HELLO"] — good

// flatMap with method returning Optional
class User {
    Optional<Address> getAddress() { return Optional.of(new Address()); }
}
class Address {
    Optional<String> getCity() { return Optional.of("New York"); }
}
Optional<User> userOpt = Optional.of(new User());
Optional<String> city = userOpt
    .flatMap(User::getAddress)
    .flatMap(Address::getCity);
// Optional["New York"]

// filter — keep value if predicate matches
Optional<String> filtered = opt.filter(s -> s.length() > 3);
// Optional["hello"] — length 5 > 3

Optional<String> filtered2 = opt.filter(s -> s.length() > 10);
// Optional.empty — length 5 not > 10

// or — return alternative Optional if empty (Java 9+)
Optional<String> result = Optional.<String>empty()
    .or(() -> Optional.of("Alternative"));
// Optional["Alternative"]

Optional<String> result2 = opt.or(() -> Optional.of("Alternative"));
// Optional["hello"] — original value returned
```

---

### 5. Consumption Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `ifPresent(consumer)` | Runs consumer if value is present | `void` |
| `ifPresentOrElse(consumer, runnable)` | Runs consumer if present, else runnable (Java 9+) | `void` |
```java
Optional<String> opt   = Optional.of("Hello");
Optional<String> empty = Optional.empty();

// ifPresent
opt.ifPresent(s -> System.out.println("Value: " + s));    // prints "Value: Hello"
empty.ifPresent(s -> System.out.println("Value: " + s));  // does nothing

// ifPresentOrElse (Java 9+)
opt.ifPresentOrElse(
    s -> System.out.println("Present: " + s),
    () -> System.out.println("Not present")
);
// prints "Present: Hello"

empty.ifPresentOrElse(
    s -> System.out.println("Present: " + s),
    () -> System.out.println("Not present")
);
// prints "Not present"
```

---

### 6. Stream Methods (Java 9+)

| Method | Description | Returns |
|--------|-------------|---------|
| `stream()` | Returns stream of 0 or 1 element | `Stream<T>` |
```java
Optional<String> opt   = Optional.of("Hello");
Optional<String> empty = Optional.empty();

// stream — convert to stream
opt.stream().forEach(System.out::println);    // prints "Hello"
empty.stream().forEach(System.out::println);  // does nothing

// Use with flatMap in streams to filter empty Optionals
List<Optional<String>> optionals = Arrays.asList(
    Optional.of("Alice"),
    Optional.empty(),
    Optional.of("Bob"),
    Optional.empty(),
    Optional.of("Charlie")
);

List<String> names = optionals.stream()
    .flatMap(Optional::stream)
    .collect(Collectors.toList());
// ["Alice", "Bob", "Charlie"]
```

---

### 7. orElse vs orElseGet

> One of the most important distinctions in `Optional`.
```java
// orElse — ALWAYS evaluates the default value, even if Optional has value
Optional<String> opt = Optional.of("Hello");

String result1 = opt.orElse(expensiveOperation());    // expensiveOperation() ALWAYS called
String result2 = opt.orElseGet(() -> expensiveOperation()); // expensiveOperation() only called if empty

// Use orElseGet when default value is expensive to compute
Optional<User> userOpt = findUserById(id);

// BAD — always queries DB even if user found
User user1 = userOpt.orElse(queryDefaultUserFromDB());

// GOOD — only queries DB if user not found
User user2 = userOpt.orElseGet(() -> queryDefaultUserFromDB());
```

---

### 8. Common Patterns

#### Replace Null Checks
```java
// Before Optional
String name = null;
if (user != null) {
    name = user.getName();
}
String result = name != null ? name : "Unknown";

// After Optional
String result = Optional.ofNullable(user)
    .map(User::getName)
    .orElse("Unknown");
```

#### Chained Null-Safe Navigation
```java
// Before Optional — deeply nested null checks
String city = null;
if (user != null) {
    Address address = user.getAddress();
    if (address != null) {
        city = address.getCity();
    }
}

// After Optional — clean and readable
String city = Optional.ofNullable(user)
    .map(User::getAddress)
    .map(Address::getCity)
    .orElse("Unknown City");
```

#### Filter and Transform
```java
// Find user by id, get their email if active, uppercase it
String email = Optional.ofNullable(findUserById(id))
    .filter(User::isActive)
    .map(User::getEmail)
    .map(String::toUpperCase)
    .orElse("NO EMAIL");
```

#### With Streams
```java
// Find first user with name starting with "A"
Optional<User> result = users.stream()
    .filter(u -> u.getName().startsWith("A"))
    .findFirst();

result.ifPresentOrElse(
    u -> System.out.println("Found: " + u.getName()),
    () -> System.out.println("Not found")
);
```

#### Return from Method
```java
// Repository method returning Optional
public Optional<User> findUserByEmail(String email) {
    return users.stream()
        .filter(u -> u.getEmail().equals(email))
        .findFirst();
}

// Service using Optional
public String getUserCity(String email) {
    return findUserByEmail(email)
        .map(User::getAddress)
        .map(Address::getCity)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
}
```

---

### 9. Anti-Patterns to Avoid
```java
// BAD — using get() without checking
Optional<String> opt = Optional.ofNullable(getValue());
String value = opt.get(); // may throw NoSuchElementException

// GOOD — use orElse / orElseGet / orElseThrow
String value = opt.orElse("Default");

// BAD — using isPresent() + get() (defeats purpose of Optional)
if (opt.isPresent()) {
    System.out.println(opt.get());
}

// GOOD — use ifPresent
opt.ifPresent(System.out::println);

// BAD — Optional as method parameter
public void process(Optional<String> name) { ... }

// GOOD — use overloading or nullable parameter
public void process(String name) { ... }

// BAD — Optional as field
class User {
    private Optional<String> email; // bad
}

// GOOD — use nullable field
class User {
    private String email; // may be null
}

// BAD — using orElse with expensive operation
String result = opt.orElse(callExpensiveService()); // always called

// GOOD — use orElseGet for lazy evaluation
String result = opt.orElseGet(() -> callExpensiveService()); // called only if empty
```

---

### 10. Optional with Primitive Types

> Use specialized Optional classes for primitives to avoid boxing overhead.

| Class | Description |
|-------|-------------|
| `OptionalInt` | Optional for `int` values |
| `OptionalLong` | Optional for `long` values |
| `OptionalDouble` | Optional for `double` values |
```java
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.OptionalDouble;

// OptionalInt
OptionalInt optInt = OptionalInt.of(42);
OptionalInt emptyInt = OptionalInt.empty();
optInt.getAsInt();                // 42
optInt.orElse(0);                 // 42
emptyInt.orElse(0);               // 0
optInt.isPresent();               // true
optInt.ifPresent(System.out::println); // prints 42

// OptionalDouble — commonly returned by stream operations
OptionalDouble avg = IntStream.of(1, 2, 3, 4, 5).average();
avg.getAsDouble();                // 3.0
avg.orElse(0.0);                  // 3.0

// OptionalLong
OptionalLong optLong = OptionalLong.of(100L);
optLong.getAsLong();              // 100
optLong.orElse(0L);               // 100
```

---

### Quick Reference — All Methods

| Method | Java Version | Description |
|--------|-------------|-------------|
| `Optional.of(value)` | 8 | Create with non-null value |
| `Optional.ofNullable(value)` | 8 | Create with nullable value |
| `Optional.empty()` | 8 | Create empty Optional |
| `isPresent()` | 8 | Check if value present |
| `isEmpty()` | 11 | Check if value absent |
| `get()` | 8 | Get value (unsafe) |
| `orElse(default)` | 8 | Get value or default |
| `orElseGet(supplier)` | 8 | Get value or lazy default |
| `orElseThrow()` | 10 | Get value or throw |
| `orElseThrow(supplier)` | 8 | Get value or throw custom |
| `map(function)` | 8 | Transform value |
| `flatMap(function)` | 8 | Transform to Optional |
| `filter(predicate)` | 8 | Filter value |
| `or(supplier)` | 9 | Alternative Optional |
| `ifPresent(consumer)` | 8 | Consume if present |
| `ifPresentOrElse(consumer, runnable)` | 9 | Consume or run |
| `stream()` | 9 | Convert to Stream |

> **Best Practices:**
> - Never use `Optional` as a method parameter or class field.
> - Always prefer `orElseGet()` over `orElse()` for expensive default values.
> - Avoid `get()` without `isPresent()` — prefer `orElse`, `orElseGet`, or `orElseThrow`.
> - Use `flatMap()` when mapping to a method that already returns `Optional`.
> - Use `ifPresentOrElse()` instead of `if (opt.isPresent()) ... else ...`.
> - Use `OptionalInt`, `OptionalLong`, `OptionalDouble` for primitive types.
> - Use `Optional` as return type to signal that a value may be absent.