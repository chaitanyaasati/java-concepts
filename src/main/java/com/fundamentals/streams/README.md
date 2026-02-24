## Streams Important Methods in Java

> `Stream` is an interface in Java (`java.util.stream.Stream`).
> It represents a **sequence of elements** supporting sequential and parallel operations.
> Streams are **lazy** — intermediate operations are not executed until a terminal operation is called.
> Streams are **not reusable** — once consumed, they cannot be reused.
```java
import java.util.stream.Stream;
import java.util.stream.Collectors;

// From collection
Stream<String> stream = list.stream();

// From array
Stream<String> stream = Arrays.stream(array);

// From values
Stream<String> stream = Stream.of("apple", "banana", "cherry");

// Parallel stream
Stream<String> stream = list.parallelStream();
```

---

### Stream Pipeline
```
Source → Intermediate Operations (lazy) → Terminal Operation (triggers execution)

list.stream()           // source
    .filter(...)        // intermediate
    .map(...)           // intermediate
    .collect(...)       // terminal — triggers execution
```

---

### 1. Stream Creation Methods

| Method | Description |
|--------|-------------|
| `Stream.of(values)` | Creates stream from values |
| `Stream.empty()` | Creates empty stream |
| `Stream.generate(supplier)` | Creates infinite stream from supplier |
| `Stream.iterate(seed, function)` | Creates infinite stream by iterating |
| `Stream.iterate(seed, predicate, function)` | Creates finite stream by iterating |
| `Stream.concat(stream1, stream2)` | Concatenates two streams |
| `Stream.builder()` | Creates stream using builder pattern |
| `collection.stream()` | Creates stream from collection |
| `collection.parallelStream()` | Creates parallel stream from collection |
| `Arrays.stream(array)` | Creates stream from array |
| `IntStream.range(start, end)` | Creates stream of int range (exclusive) |
| `IntStream.rangeClosed(start, end)` | Creates stream of int range (inclusive) |
```java
// From values
Stream<String> s1 = Stream.of("a", "b", "c");

// Empty stream
Stream<String> s2 = Stream.empty();

// Infinite stream — generate
Stream<Double> randoms = Stream.generate(Math::random).limit(5);

// Infinite stream — iterate
Stream<Integer> evens = Stream.iterate(0, n -> n + 2).limit(5);
// [0, 2, 4, 6, 8]

// Finite stream — iterate with predicate
Stream<Integer> finite = Stream.iterate(0, n -> n < 10, n -> n + 2);
// [0, 2, 4, 6, 8]

// Concatenate streams
Stream<String> concat = Stream.concat(Stream.of("a", "b"), Stream.of("c", "d"));

// Builder
Stream<String> built = Stream.<String>builder()
    .add("apple")
    .add("banana")
    .build();

// Int range
IntStream range = IntStream.range(1, 5);        // [1, 2, 3, 4]
IntStream rangeClosed = IntStream.rangeClosed(1, 5); // [1, 2, 3, 4, 5]
```

---

### 2. Intermediate Operations — *Returns a new Stream (lazy)*

#### Filtering

| Method | Description |
|--------|-------------|
| `filter(predicate)` | Keeps elements matching predicate |
| `distinct()` | Removes duplicate elements |
| `limit(n)` | Truncates stream to first n elements |
| `skip(n)` | Skips first n elements |
| `takeWhile(predicate)` | Takes elements while predicate is true (Java 9+) |
| `dropWhile(predicate)` | Drops elements while predicate is true (Java 9+) |
```java
List<Integer> nums = Arrays.asList(1, 2, 2, 3, 4, 4, 5, 6, 7, 8);

// filter
nums.stream().filter(n -> n % 2 == 0);
// [2, 2, 4, 4, 6, 8]

// distinct
nums.stream().distinct();
// [1, 2, 3, 4, 5, 6, 7, 8]

// limit
nums.stream().limit(4);
// [1, 2, 2, 3]

// skip
nums.stream().skip(4);
// [4, 4, 5, 6, 7, 8]

// takeWhile (Java 9+)
nums.stream().takeWhile(n -> n < 4);
// [1, 2, 2, 3]

// dropWhile (Java 9+)
nums.stream().dropWhile(n -> n < 4);
// [4, 4, 5, 6, 7, 8]
```

---

#### Mapping / Transformation

| Method | Description |
|--------|-------------|
| `map(function)` | Transforms each element |
| `mapToInt(function)` | Transforms to `IntStream` |
| `mapToLong(function)` | Transforms to `LongStream` |
| `mapToDouble(function)` | Transforms to `DoubleStream` |
| `mapToObj(function)` | Transforms primitive stream to object stream |
| `flatMap(function)` | Flattens nested streams into one stream |
| `flatMapToInt(function)` | Flattens to `IntStream` |
| `flatMapToLong(function)` | Flattens to `LongStream` |
| `flatMapToDouble(function)` | Flattens to `DoubleStream` |
| `peek(consumer)` | Performs action on each element without modifying |
```java
List<String> names = Arrays.asList("alice", "bob", "charlie");

// map
names.stream().map(String::toUpperCase);
// ["ALICE", "BOB", "CHARLIE"]

// mapToInt
names.stream().mapToInt(String::length);
// IntStream [5, 3, 7]

// flatMap — flatten nested lists
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2, 3),
    Arrays.asList(4, 5, 6)
);
nested.stream().flatMap(Collection::stream);
// [1, 2, 3, 4, 5, 6]

// flatMap — split words into characters
Stream.of("hello", "world")
      .flatMap(s -> Arrays.stream(s.split("")));
// ["h","e","l","l","o","w","o","r","l","d"]

// peek — for debugging
names.stream()
     .peek(s -> System.out.println("Before: " + s))
     .map(String::toUpperCase)
     .peek(s -> System.out.println("After: " + s))
     .collect(Collectors.toList());
```

---

#### Sorting

| Method | Description |
|--------|-------------|
| `sorted()` | Sorts using natural ordering |
| `sorted(comparator)` | Sorts using custom comparator |
```java
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

// Natural order
names.stream().sorted();
// ["Alice", "Bob", "Charlie"]

// Reverse order
names.stream().sorted(Comparator.reverseOrder());
// ["Charlie", "Bob", "Alice"]

// By length
names.stream().sorted(Comparator.comparingInt(String::length));
// ["Bob", "Alice", "Charlie"]

// By length then alphabetically
names.stream().sorted(Comparator.comparingInt(String::length)
                                .thenComparing(Comparator.naturalOrder()));
```

---

### 3. Terminal Operations — *Triggers execution, returns result*

#### Collection

| Method | Description |
|--------|-------------|
| `collect(collector)` | Collects elements into a collection or result |
| `toList()` | Collects into unmodifiable list (Java 16+) |
| `toArray()` | Collects into `Object[]` |
| `toArray(generator)` | Collects into typed array |
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Alice");

// To List
List<String> list = names.stream().collect(Collectors.toList());

// To unmodifiable List (Java 16+)
List<String> immutable = names.stream().toList();

// To Set (removes duplicates)
Set<String> set = names.stream().collect(Collectors.toSet());

// To Array
String[] arr = names.stream().toArray(String[]::new);

// To Map
Map<String, Integer> map = names.stream()
    .distinct()
    .collect(Collectors.toMap(
        name -> name,
        String::length
    ));
// {Alice=5, Bob=3, Charlie=7}
```

---

#### Collectors

| Collector | Description |
|-----------|-------------|
| `Collectors.toList()` | Collects to `List` |
| `Collectors.toSet()` | Collects to `Set` |
| `Collectors.toMap(k, v)` | Collects to `Map` |
| `Collectors.toUnmodifiableList()` | Collects to unmodifiable `List` |
| `Collectors.joining()` | Joins strings |
| `Collectors.joining(delimiter)` | Joins with delimiter |
| `Collectors.joining(delimiter, prefix, suffix)` | Joins with delimiter, prefix, suffix |
| `Collectors.groupingBy(classifier)` | Groups elements by key |
| `Collectors.partitioningBy(predicate)` | Partitions into true/false groups |
| `Collectors.counting()` | Counts elements |
| `Collectors.summingInt(fn)` | Sums int values |
| `Collectors.averagingInt(fn)` | Averages int values |
| `Collectors.summarizingInt(fn)` | Returns statistics (count, sum, min, max, avg) |
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Anna", "Brian");

// joining
names.stream().collect(Collectors.joining(", "));
// "Alice, Bob, Charlie, Anna, Brian"

names.stream().collect(Collectors.joining(", ", "[", "]"));
// "[Alice, Bob, Charlie, Anna, Brian]"

// groupingBy
Map<Integer, List<String>> byLength = names.stream()
    .collect(Collectors.groupingBy(String::length));
// {5=[Alice, Brian], 3=[Bob, Anna], 7=[Charlie]}

// partitioningBy
Map<Boolean, List<String>> partitioned = names.stream()
    .collect(Collectors.partitioningBy(s -> s.startsWith("A")));
// {true=[Alice, Anna], false=[Bob, Charlie, Brian]}

// counting
long count = names.stream().collect(Collectors.counting()); // 5

// summingInt
int totalLength = names.stream().collect(Collectors.summingInt(String::length)); // 27

// averagingInt
double avgLength = names.stream().collect(Collectors.averagingInt(String::length)); // 5.4

// summarizingInt
IntSummaryStatistics stats = names.stream()
    .collect(Collectors.summarizingInt(String::length));
// count=5, sum=27, min=3, max=7, average=5.4
```

---

#### Searching / Matching

| Method | Description | Short-circuits? |
|--------|-------------|-----------------|
| `findFirst()` | Returns first element as `Optional` | Yes |
| `findAny()` | Returns any element as `Optional` (better for parallel) | Yes |
| `anyMatch(predicate)` | Returns `true` if any element matches | Yes |
| `allMatch(predicate)` | Returns `true` if all elements match | Yes |
| `noneMatch(predicate)` | Returns `true` if no element matches | Yes |
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// findFirst
Optional<String> first = names.stream()
    .filter(s -> s.startsWith("A"))
    .findFirst();
// Optional["Alice"]

// findAny (preferred in parallel streams)
Optional<String> any = names.parallelStream()
    .filter(s -> s.length() > 3)
    .findAny();

// anyMatch
boolean hasA = names.stream().anyMatch(s -> s.startsWith("A")); // true

// allMatch
boolean allLong = names.stream().allMatch(s -> s.length() > 2); // true

// noneMatch
boolean noneShort = names.stream().noneMatch(s -> s.length() < 2); // true
```

---

#### Reduction

| Method | Description |
|--------|-------------|
| `reduce(identity, accumulator)` | Reduces to single value with initial value |
| `reduce(accumulator)` | Reduces to `Optional` without initial value |
| `reduce(identity, accumulator, combiner)` | Reduces in parallel streams |
| `count()` | Returns number of elements |
| `sum()` | Returns sum (IntStream / LongStream / DoubleStream) |
| `min(comparator)` | Returns minimum element as `Optional` |
| `max(comparator)` | Returns maximum element as `Optional` |
| `average()` | Returns average as `OptionalDouble` (primitive streams) |
| `summaryStatistics()` | Returns statistics (primitive streams) |
```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

// reduce with identity
int sum = nums.stream().reduce(0, Integer::sum);           // 15
int product = nums.stream().reduce(1, (a, b) -> a * b);    // 120

// reduce without identity
Optional<Integer> max = nums.stream().reduce(Integer::max); // 5

// count
long count = nums.stream().filter(n -> n > 2).count(); // 3

// min / max
Optional<Integer> min = nums.stream().min(Comparator.naturalOrder()); // 1
Optional<Integer> maximum = nums.stream().max(Comparator.naturalOrder()); // 5

// sum, average, statistics on IntStream
IntStream intStream = nums.stream().mapToInt(Integer::intValue);
int total = intStream.sum();                    // 15
OptionalDouble avg = intStream.average();       // 3.0
IntSummaryStatistics stats = intStream.summaryStatistics();
// count=5, sum=15, min=1, max=5, average=3.0
```

---

#### Iteration

| Method | Description |
|--------|-------------|
| `forEach(consumer)` | Performs action on each element |
| `forEachOrdered(consumer)` | Performs action in encounter order (important for parallel) |
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// forEach
names.stream().forEach(System.out::println);

// forEachOrdered — maintains order in parallel streams
names.parallelStream().forEachOrdered(System.out::println);
```

---

### 4. Primitive Streams

> Java provides specialized streams for primitives to avoid boxing overhead.

| Stream | Description |
|--------|-------------|
| `IntStream` | Stream of `int` values |
| `LongStream` | Stream of `long` values |
| `DoubleStream` | Stream of `double` values |
```java
// IntStream
IntStream.range(1, 6).sum();           // 15
IntStream.rangeClosed(1, 5).average(); // 3.0
IntStream.of(1, 2, 3, 4, 5).max();    // 5

// Convert to object stream
IntStream.range(1, 4)
         .mapToObj(i -> "Item " + i)
         .collect(Collectors.toList());
// ["Item 1", "Item 2", "Item 3"]

// Convert object stream to primitive stream
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
int totalLength = names.stream()
                       .mapToInt(String::length)
                       .sum(); // 15
```

---

### 5. Parallel Streams

| Method | Description |
|--------|-------------|
| `parallelStream()` | Creates parallel stream from collection |
| `parallel()` | Converts sequential stream to parallel |
| `sequential()` | Converts parallel stream to sequential |
| `isParallel()` | Returns `true` if stream is parallel |
```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Parallel stream
int sum = nums.parallelStream()
              .filter(n -> n % 2 == 0)
              .mapToInt(Integer::intValue)
              .sum(); // 30

// Convert to parallel
nums.stream()
    .parallel()
    .forEach(System.out::println); // order not guaranteed

// Use forEachOrdered to maintain order
nums.parallelStream()
    .forEachOrdered(System.out::println); // order guaranteed
```

---

### 6. Optional with Streams
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// findFirst returns Optional
Optional<String> result = names.stream()
    .filter(s -> s.startsWith("B"))
    .findFirst();

result.isPresent();           // true
result.get();                 // "Bob"
result.orElse("Unknown");     // "Bob"
result.orElseGet(() -> "N/A"); // "Bob"
result.ifPresent(System.out::println); // prints "Bob"

// flatMap with Optional (Java 9+)
names.stream()
     .map(s -> s.startsWith("A") ? Optional.of(s) : Optional.empty())
     .flatMap(Optional::stream)
     .collect(Collectors.toList());
// ["Alice"]
```

---

### 7. Common Stream Patterns
```java
List<Person> people = Arrays.asList(
    new Person("Alice",   30, "Engineering"),
    new Person("Bob",     25, "Marketing"),
    new Person("Charlie", 30, "Engineering"),
    new Person("David",   25, "HR")
);

// Filter and collect
List<Person> engineers = people.stream()
    .filter(p -> p.getDept().equals("Engineering"))
    .collect(Collectors.toList());

// Transform and collect
List<String> names = people.stream()
    .map(Person::getName)
    .sorted()
    .collect(Collectors.toList());

// Group by department
Map<String, List<Person>> byDept = people.stream()
    .collect(Collectors.groupingBy(Person::getDept));

// Count by department
Map<String, Long> countByDept = people.stream()
    .collect(Collectors.groupingBy(Person::getDept, Collectors.counting()));
// {Engineering=2, Marketing=1, HR=1}

// Average age by department
Map<String, Double> avgAgeByDept = people.stream()
    .collect(Collectors.groupingBy(
        Person::getDept,
        Collectors.averagingInt(Person::getAge)
    ));

// Get names of people over 25, sorted
List<String> result = people.stream()
    .filter(p -> p.getAge() > 25)
    .sorted(Comparator.comparing(Person::getName))
    .map(Person::getName)
    .collect(Collectors.toList());
```

---

### 8. Intermediate vs Terminal Operations

| Type | Operations | Returns |
|------|-----------|---------|
| **Intermediate** | `filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `skip`, `peek`, `takeWhile`, `dropWhile` | `Stream` (lazy) |
| **Terminal** | `collect`, `forEach`, `reduce`, `count`, `min`, `max`, `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch`, `toArray` | Result / void |

---

### Quick Reference — Most Used Methods
```java
stream.filter(predicate)               // keep matching elements
stream.map(function)                   // transform elements
stream.flatMap(function)               // flatten nested streams
stream.sorted(comparator)             // sort elements
stream.distinct()                     // remove duplicates
stream.limit(n)                       // take first n
stream.skip(n)                        // skip first n
stream.collect(Collectors.toList())   // collect to list
stream.collect(Collectors.groupingBy) // group elements
stream.forEach(consumer)              // iterate elements
stream.reduce(identity, accumulator)  // reduce to single value
stream.count()                        // count elements
stream.min(comparator)                // find minimum
stream.max(comparator)                // find maximum
stream.findFirst()                    // find first element
stream.anyMatch(predicate)            // check if any matches
stream.allMatch(predicate)            // check if all match
stream.noneMatch(predicate)           // check if none match
```

> **Best Practices:**
> - Streams are **not reusable** — create a new stream for each pipeline.
> - Prefer **method references** over lambdas for cleaner code.
> - Use **primitive streams** (`IntStream`, `LongStream`, `DoubleStream`) to avoid boxing overhead.
> - Use **parallel streams** only for large datasets with stateless, independent operations.
> - Always use `forEachOrdered()` instead of `forEach()` for parallel streams when order matters.
> - Avoid **side effects** in intermediate operations like `map()` and `filter()`.
> - Use `peek()` only for **debugging** — never for actual logic.