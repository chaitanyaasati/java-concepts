## Lambda Expressions Important Methods & Concepts in Java

> Lambda expressions were introduced in **Java 8**.
> A lambda is an **anonymous function** that can be passed as an argument or stored in a variable.
> Lambdas implement **Functional Interfaces** — interfaces with exactly one abstract method.
> Syntax: `(parameters) -> expression` or `(parameters) -> { statements; }`
```java
// Basic syntax
() -> System.out.println("Hello")           // no parameter
x -> x * 2                                  // single parameter
(x, y) -> x + y                             // multiple parameters
(x, y) -> { return x + y; }                 // block body
(String s) -> s.toUpperCase()               // with type
```

---

### 1. Built-in Functional Interfaces (`java.util.function`)

---

#### Function\<T, R> — Takes input, returns output

| Method | Description |
|--------|-------------|
| `apply(T t)` | Applies function to given argument |
| `andThen(Function after)` | Chains: applies this, then after |
| `compose(Function before)` | Chains: applies before, then this |
| `Function.identity()` | Returns function that returns its input |
```java
import java.util.function.Function;

Function<String, Integer> length = s -> s.length();
length.apply("Hello");   // 5

Function<String, String> upper  = String::toUpperCase;
Function<String, String> trim   = String::trim;

// andThen — trim first, then uppercase
Function<String, String> trimThenUpper = trim.andThen(upper);
trimThenUpper.apply("  hello  "); // "HELLO"

// compose — uppercase first, then trim (reverse of andThen)
Function<String, String> upperThenTrim = trim.compose(upper);
upperThenTrim.apply("  hello  "); // "  HELLO  " trimmed = "HELLO"

// identity
Function<String, String> identity = Function.identity();
identity.apply("Hello"); // "Hello"
```

---

#### BiFunction\<T, U, R> — Takes two inputs, returns output

| Method | Description |
|--------|-------------|
| `apply(T t, U u)` | Applies function to two arguments |
| `andThen(Function after)` | Chains result with another function |
```java
import java.util.function.BiFunction;

BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
repeat.apply("Hello", 3); // "HelloHelloHello"

BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
add.andThen(result -> result * 2).apply(3, 4); // 14
```

---

#### Predicate\<T> — Takes input, returns boolean

| Method | Description |
|--------|-------------|
| `test(T t)` | Evaluates predicate on given argument |
| `and(Predicate other)` | Logical AND of two predicates |
| `or(Predicate other)` | Logical OR of two predicates |
| `negate()` | Logical NOT of predicate |
| `Predicate.not(predicate)` | Static NOT (Java 11+) |
| `Predicate.isEqual(target)` | Returns predicate testing equality |
```java
import java.util.function.Predicate;

Predicate<String> isLong    = s -> s.length() > 5;
Predicate<String> startsWithA = s -> s.startsWith("A");

isLong.test("Hello");        // false
isLong.test("Hello World");  // true

// and
Predicate<String> longAndStartsA = isLong.and(startsWithA);
longAndStartsA.test("Alexander"); // true
longAndStartsA.test("Alice");     // false

// or
Predicate<String> longOrStartsA = isLong.or(startsWithA);
longOrStartsA.test("Alice");      // true (starts with A)
longOrStartsA.test("Hello World"); // true (long)

// negate
Predicate<String> isShort = isLong.negate();
isShort.test("Hi"); // true

// Predicate.not (Java 11+)
Predicate<String> notBlank = Predicate.not(String::isBlank);
notBlank.test("Hello"); // true
notBlank.test("   ");   // false

// isEqual
Predicate<String> isHello = Predicate.isEqual("Hello");
isHello.test("Hello"); // true
isHello.test("World"); // false
```

---

#### BiPredicate\<T, U> — Takes two inputs, returns boolean

| Method | Description |
|--------|-------------|
| `test(T t, U u)` | Evaluates predicate on two arguments |
| `and(BiPredicate other)` | Logical AND |
| `or(BiPredicate other)` | Logical OR |
| `negate()` | Logical NOT |
```java
import java.util.function.BiPredicate;

BiPredicate<String, Integer> longerThan = (s, n) -> s.length() > n;
longerThan.test("Hello", 3);  // true
longerThan.test("Hi", 3);     // false

BiPredicate<String, String> startsWith = (s, prefix) -> s.startsWith(prefix);
startsWith.test("Hello", "He"); // true
```

---

#### Consumer\<T> — Takes input, returns nothing

| Method | Description |
|--------|-------------|
| `accept(T t)` | Performs operation on given argument |
| `andThen(Consumer after)` | Chains: performs this, then after |
```java
import java.util.function.Consumer;

Consumer<String> print   = System.out::println;
Consumer<String> upper   = s -> System.out.println(s.toUpperCase());

print.accept("Hello");   // prints "Hello"

// andThen — chain consumers
Consumer<String> printAndUpper = print.andThen(upper);
printAndUpper.accept("Hello");
// prints "Hello"
// prints "HELLO"
```

---

#### BiConsumer\<T, U> — Takes two inputs, returns nothing

| Method | Description |
|--------|-------------|
| `accept(T t, U u)` | Performs operation on two arguments |
| `andThen(BiConsumer after)` | Chains two BiConsumers |
```java
import java.util.function.BiConsumer;

BiConsumer<String, Integer> repeat = (s, n) ->
    System.out.println(s.repeat(n));

repeat.accept("Hello", 3); // "HelloHelloHello"

BiConsumer<String, String> concat = (a, b) ->
    System.out.println(a + b);

concat.andThen((a, b) -> System.out.println(a.length() + b.length()))
      .accept("Hello", "World");
// "HelloWorld"
// 10
```

---

#### Supplier\<T> — Takes nothing, returns output

| Method | Description |
|--------|-------------|
| `get()` | Gets a result |
```java
import java.util.function.Supplier;

Supplier<String> greeting  = () -> "Hello World";
Supplier<Double> random    = Math::random;
Supplier<List<String>> list = ArrayList::new;

greeting.get();  // "Hello World"
random.get();    // random double
list.get();      // new ArrayList
```

---

#### UnaryOperator\<T> — Takes input, returns same type (extends Function)

| Method | Description |
|--------|-------------|
| `apply(T t)` | Applies operator to given argument |
| `andThen(Function after)` | Chains after this operator |
| `compose(Function before)` | Chains before this operator |
| `UnaryOperator.identity()` | Returns operator that returns its input |
```java
import java.util.function.UnaryOperator;

UnaryOperator<String> upper  = String::toUpperCase;
UnaryOperator<String> trim   = String::trim;
UnaryOperator<Integer> square = n -> n * n;

upper.apply("hello");    // "HELLO"
square.apply(5);         // 25

// andThen
UnaryOperator<String> trimAndUpper = trim.andThen(upper)::apply;
// or use Function for chaining
Function<String, String> chain = trim.andThen(upper);
chain.apply("  hello  "); // "HELLO"

// identity
UnaryOperator<String> identity = UnaryOperator.identity();
identity.apply("Hello"); // "Hello"
```

---

#### BinaryOperator\<T> — Takes two same-type inputs, returns same type (extends BiFunction)

| Method | Description |
|--------|-------------|
| `apply(T t1, T t2)` | Applies operator to two arguments |
| `BinaryOperator.minBy(comparator)` | Returns operator that returns minimum |
| `BinaryOperator.maxBy(comparator)` | Returns operator that returns maximum |
```java
import java.util.function.BinaryOperator;

BinaryOperator<Integer> add      = (a, b) -> a + b;
BinaryOperator<Integer> multiply = (a, b) -> a * b;
BinaryOperator<String>  concat   = (a, b) -> a + b;

add.apply(3, 4);         // 7
multiply.apply(3, 4);    // 12
concat.apply("Hi", "!"); // "Hi!"

// minBy / maxBy
BinaryOperator<String> shortest = BinaryOperator.minBy(
    Comparator.comparingInt(String::length)
);
BinaryOperator<String> longest = BinaryOperator.maxBy(
    Comparator.comparingInt(String::length)
);

shortest.apply("Hi", "Hello"); // "Hi"
longest.apply("Hi", "Hello");  // "Hello"
```

---

### 2. Primitive Functional Interfaces

> Specialized interfaces to avoid boxing/unboxing overhead.

| Interface | Method | Description |
|-----------|--------|-------------|
| `IntFunction<R>` | `apply(int value)` | Takes `int`, returns R |
| `IntPredicate` | `test(int value)` | Takes `int`, returns boolean |
| `IntConsumer` | `accept(int value)` | Takes `int`, returns void |
| `IntSupplier` | `getAsInt()` | Returns `int` |
| `IntUnaryOperator` | `applyAsInt(int value)` | Takes `int`, returns `int` |
| `IntBinaryOperator` | `applyAsInt(int l, int r)` | Takes two `int`, returns `int` |
| `ToIntFunction<T>` | `applyAsInt(T value)` | Takes T, returns `int` |
| `LongFunction<R>` | `apply(long value)` | Takes `long`, returns R |
| `DoubleFunction<R>` | `apply(double value)` | Takes `double`, returns R |
```java
import java.util.function.*;

IntPredicate isEven = n -> n % 2 == 0;
isEven.test(4);   // true
isEven.test(3);   // false

IntUnaryOperator square = n -> n * n;
square.applyAsInt(5); // 25

IntBinaryOperator add = (a, b) -> a + b;
add.applyAsInt(3, 4); // 7

ToIntFunction<String> lengthFn = String::length;
lengthFn.applyAsInt("Hello"); // 5

IntSupplier randomInt = () -> (int)(Math.random() * 100);
randomInt.getAsInt(); // random int
```

---

### 3. Method References

> Method references are shorthand for lambdas calling a single method.

| Type | Syntax | Lambda Equivalent |
|------|--------|-------------------|
| Static method | `ClassName::staticMethod` | `(args) -> ClassName.staticMethod(args)` |
| Instance method (specific) | `instance::method` | `(args) -> instance.method(args)` |
| Instance method (arbitrary) | `ClassName::instanceMethod` | `(obj, args) -> obj.method(args)` |
| Constructor | `ClassName::new` | `(args) -> new ClassName(args)` |
```java
// Static method reference
Function<String, Integer> parseInt = Integer::parseInt;
parseInt.apply("42"); // 42

// Instance method reference — specific instance
String prefix = "Hello";
Predicate<String> startsWith = prefix::startsWith;
startsWith.test("He"); // true

// Instance method reference — arbitrary instance
Function<String, String> upper = String::toUpperCase;
upper.apply("hello"); // "HELLO"

Predicate<String> isEmpty = String::isEmpty;
isEmpty.test(""); // true

// Constructor reference
Supplier<ArrayList<String>> listFactory = ArrayList::new;
listFactory.get(); // new ArrayList

Function<String, StringBuilder> sbFactory = StringBuilder::new;
sbFactory.apply("Hello"); // new StringBuilder("Hello")
```

---

### 4. Lambda with Collections
```java
List<String> names = Arrays.asList("Charlie", "Alice", "Bob", "David");

// sort
names.sort((a, b) -> a.compareTo(b));
names.sort(Comparator.naturalOrder());

// forEach
names.forEach(name -> System.out.println(name));
names.forEach(System.out::println);

// removeIf
names.removeIf(name -> name.length() > 4);
// removes names longer than 4 chars

// replaceAll
names.replaceAll(String::toUpperCase);
// replaces all with uppercase

// stream with lambda
names.stream()
     .filter(s -> s.startsWith("A"))
     .map(String::toUpperCase)
     .collect(Collectors.toList());
```

---

### 5. Lambda with Maps
```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Alice", 90);
scores.put("Bob",   85);
scores.put("Charlie", 92);

// forEach
scores.forEach((name, score) ->
    System.out.println(name + ": " + score));

// replaceAll
scores.replaceAll((name, score) -> score + 10);
// adds 10 to all scores

// computeIfAbsent
scores.computeIfAbsent("David", name -> 75);
// adds David:75 only if not present

// computeIfPresent
scores.computeIfPresent("Alice", (name, score) -> score + 5);
// updates Alice's score only if present

// compute
scores.compute("Bob", (name, score) ->
    score == null ? 0 : score + 5
);

// merge
scores.merge("Alice", 5, Integer::sum);
// adds 5 to Alice's score, or sets 5 if not present
```

---

### 6. Composing Lambdas
```java
// Function composition
Function<Integer, Integer> times2  = x -> x * 2;
Function<Integer, Integer> plus3   = x -> x + 3;

// andThen — times2 first, then plus3
Function<Integer, Integer> times2ThenPlus3 = times2.andThen(plus3);
times2ThenPlus3.apply(5); // (5*2)+3 = 13

// compose — plus3 first, then times2
Function<Integer, Integer> plus3ThenTimes2 = times2.compose(plus3);
plus3ThenTimes2.apply(5); // (5+3)*2 = 16

// Predicate composition
Predicate<Integer> isPositive = n -> n > 0;
Predicate<Integer> isEven     = n -> n % 2 == 0;

Predicate<Integer> isPositiveEven  = isPositive.and(isEven);
Predicate<Integer> isPositiveOrEven = isPositive.or(isEven);
Predicate<Integer> isNegative      = isPositive.negate();

isPositiveEven.test(4);   // true
isPositiveEven.test(-4);  // false
isNegative.test(-5);      // true
```

---

### 7. Custom Functional Interfaces
```java
// Define custom functional interface
@FunctionalInterface
interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}

@FunctionalInterface
interface Validator<T> {
    boolean validate(T t);

    default Validator<T> and(Validator<T> other) {
        return t -> this.validate(t) && other.validate(t);
    }

    default Validator<T> or(Validator<T> other) {
        return t -> this.validate(t) || other.validate(t);
    }

    default Validator<T> negate() {
        return t -> !this.validate(t);
    }
}

// Usage
TriFunction<Integer, Integer, Integer, Integer> sum = (a, b, c) -> a + b + c;
sum.apply(1, 2, 3); // 6

Validator<String> notEmpty  = s -> !s.isEmpty();
Validator<String> notTooLong = s -> s.length() <= 10;
Validator<String> valid = notEmpty.and(notTooLong);
valid.validate("Hello");        // true
valid.validate("");             // false
valid.validate("Hello World!"); // false
```

---

### 8. Functional Interface Quick Reference

| Interface | Parameters | Returns | Method |
|-----------|-----------|---------|--------|
| `Function<T,R>` | T | R | `apply(T)` |
| `BiFunction<T,U,R>` | T, U | R | `apply(T,U)` |
| `UnaryOperator<T>` | T | T | `apply(T)` |
| `BinaryOperator<T>` | T, T | T | `apply(T,T)` |
| `Predicate<T>` | T | boolean | `test(T)` |
| `BiPredicate<T,U>` | T, U | boolean | `test(T,U)` |
| `Consumer<T>` | T | void | `accept(T)` |
| `BiConsumer<T,U>` | T, U | void | `accept(T,U)` |
| `Supplier<T>` | none | T | `get()` |
| `Runnable` | none | void | `run()` |
| `Callable<T>` | none | T | `call()` |

---

### Quick Reference — Lambda Syntax
```java
// No parameter
Runnable r = () -> System.out.println("Hello");

// Single parameter (no parentheses needed)
Consumer<String> c = s -> System.out.println(s);

// Multiple parameters
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

// With type declaration
BiFunction<Integer, Integer, Integer> add = (Integer a, Integer b) -> a + b;

// Block body with return
Function<String, Integer> length = s -> {
    int len = s.length();
    return len;
};

// Method reference shorthand
Consumer<String> print = System.out::println;
Function<String, String> upper = String::toUpperCase;
Supplier<List<String>> list = ArrayList::new;
```

> **Best Practices:**
> - Always annotate custom functional interfaces with `@FunctionalInterface`.
> - Prefer **method references** over lambdas when they improve readability.
> - Use **primitive functional interfaces** (`IntPredicate`, `IntFunction`, etc.) to avoid boxing.
> - Keep lambda bodies **short** — extract complex logic into named methods.
> - Use `Predicate.not()` (Java 11+) instead of `p.negate()` for better readability.
> - Prefer `orElseGet(supplier)` over `orElse(value)` for expensive computations.
> - Avoid **side effects** in lambdas used with streams.
> - Variables used in lambdas must be **effectively final**.