## Comparator Important Methods in Java

> `Comparator` is a functional interface in Java (`java.util.Comparator`).
> It is used to define a **custom ordering** for objects.
> It can be used with sorting, priority queues, tree maps, and more.
> Returns **negative** if first < second, **zero** if equal, **positive** if first > second.
```java
import java.util.Comparator;

// Lambda
Comparator<String> comp = (a, b) -> a.compareTo(b);

// Method reference
Comparator<String> comp = Comparator.naturalOrder();

// Anonymous class
Comparator<String> comp = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
};
```

---

### 1. Core Method

| Method | Description |
|--------|-------------|
| `compare(T o1, T o2)` | Compares two objects, returns negative / zero / positive |
```java
Comparator<Integer> comp = (a, b) -> a - b;

comp.compare(3, 5);   // negative — 3 < 5
comp.compare(5, 5);   // zero    — equal
comp.compare(7, 5);   // positive — 7 > 5
```

---

### 2. Static Factory Methods

| Method | Description |
|--------|-------------|
| `Comparator.naturalOrder()` | Returns comparator using natural ordering (ascending) |
| `Comparator.reverseOrder()` | Returns comparator using reverse natural ordering (descending) |
| `Comparator.comparing(keyExtractor)` | Compares by extracted key |
| `Comparator.comparing(keyExtractor, keyComparator)` | Compares by key with custom comparator |
| `Comparator.comparingInt(keyExtractor)` | Compares by `int` key |
| `Comparator.comparingLong(keyExtractor)` | Compares by `long` key |
| `Comparator.comparingDouble(keyExtractor)` | Compares by `double` key |
| `Comparator.nullsFirst(comparator)` | Places `null` values before non-null |
| `Comparator.nullsLast(comparator)` | Places `null` values after non-null |
```java
// Natural order — ascending
Comparator<String> natural = Comparator.naturalOrder();

// Reverse order — descending
Comparator<String> reverse = Comparator.reverseOrder();

// Compare by string length
Comparator<String> byLength = Comparator.comparingInt(String::length);

// Compare by name field
Comparator<Person> byName = Comparator.comparing(Person::getName);

// Compare by age (int)
Comparator<Person> byAge = Comparator.comparingInt(Person::getAge);

// Compare by salary (double)
Comparator<Person> bySalary = Comparator.comparingDouble(Person::getSalary);

// Compare by id (long)
Comparator<Person> byId = Comparator.comparingLong(Person::getId);

// Nulls first
Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());

// Nulls last
Comparator<String> nullsLast = Comparator.nullsLast(Comparator.naturalOrder());
```

---

### 3. Chaining Methods

| Method | Description |
|--------|-------------|
| `thenComparing(comparator)` | Secondary comparator if first returns zero |
| `thenComparing(keyExtractor)` | Secondary compare by key if first returns zero |
| `thenComparing(keyExtractor, keyComparator)` | Secondary compare by key with custom comparator |
| `thenComparingInt(keyExtractor)` | Secondary compare by `int` key |
| `thenComparingLong(keyExtractor)` | Secondary compare by `long` key |
| `thenComparingDouble(keyExtractor)` | Secondary compare by `double` key |
```java
// Sort by last name, then first name
Comparator<Person> byLastThenFirst = Comparator
    .comparing(Person::getLastName)
    .thenComparing(Person::getFirstName);

// Sort by age, then name, then salary
Comparator<Person> multiSort = Comparator
    .comparingInt(Person::getAge)
    .thenComparing(Person::getName)
    .thenComparingDouble(Person::getSalary);

// Sort by department, then by age descending
Comparator<Person> deptThenAge = Comparator
    .comparing(Person::getDepartment)
    .thenComparingInt(Person::getAge).reversed();
```

---

### 4. Reversing Methods

| Method | Description |
|--------|-------------|
| `reversed()` | Returns a comparator with reversed ordering |
| `Comparator.reverseOrder()` | Returns a comparator reversing natural ordering |
```java
// Reverse natural order
Comparator<Integer> descending = Comparator.<Integer>naturalOrder().reversed();

// Reverse custom comparator
Comparator<String> byLengthDesc = Comparator.comparingInt(String::length).reversed();

// Reverse chained comparator
Comparator<Person> byAgeDesc = Comparator
    .comparingInt(Person::getAge)
    .reversed();

// Reverse only secondary comparator
Comparator<Person> byNameThenAgeDesc = Comparator
    .comparing(Person::getName)
    .thenComparingInt(Person::getAge)
    .reversed();
```

---

### 5. Null Handling Methods

| Method | Description |
|--------|-------------|
| `Comparator.nullsFirst(comparator)` | `null` values sort before non-null |
| `Comparator.nullsLast(comparator)` | `null` values sort after non-null |
```java
List<String> list = Arrays.asList("banana", null, "apple", null, "cherry");

// nulls first
list.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
// [null, null, apple, banana, cherry]

// nulls last
list.sort(Comparator.nullsLast(Comparator.naturalOrder()));
// [apple, banana, cherry, null, null]

// nulls first with custom comparator
Comparator<String> comp = Comparator.nullsFirst(
    Comparator.comparingInt(String::length)
);
```

---

### 6. Usage with Collections

#### Sorting a List
```java
List<String> names = Arrays.asList("Charlie", "Alice", "Bob", "David");

// Natural order
Collections.sort(names, Comparator.naturalOrder());

// Reverse order
names.sort(Comparator.reverseOrder());

// By length
names.sort(Comparator.comparingInt(String::length));

// By length then alphabetically
names.sort(Comparator.comparingInt(String::length)
           .thenComparing(Comparator.naturalOrder()));
```

#### Sorting Custom Objects
```java
List<Person> people = Arrays.asList(
    new Person("Alice", 30, 50000.0),
    new Person("Bob",   25, 60000.0),
    new Person("Charlie", 30, 45000.0)
);

// Sort by age
people.sort(Comparator.comparingInt(Person::getAge));

// Sort by age descending then name
people.sort(Comparator
    .comparingInt(Person::getAge)
    .reversed()
    .thenComparing(Person::getName));
```

---

### 7. Usage with PriorityQueue
```java
// Min-heap by length
PriorityQueue<String> pq = new PriorityQueue<>(
    Comparator.comparingInt(String::length)
);

// Max-heap (reverse natural order)
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(
    Comparator.reverseOrder()
);

// Custom object priority queue
PriorityQueue<Person> personQueue = new PriorityQueue<>(
    Comparator.comparingInt(Person::getAge)
             .thenComparing(Person::getName)
);

personQueue.offer(new Person("Alice", 30, 50000.0));
personQueue.offer(new Person("Bob",   25, 60000.0));
personQueue.poll(); // Bob (youngest)
```

---

### 8. Usage with TreeMap / TreeSet
```java
// TreeMap sorted by key length
TreeMap<String, Integer> treeMap = new TreeMap<>(
    Comparator.comparingInt(String::length)
              .thenComparing(Comparator.naturalOrder())
);
treeMap.put("banana", 1);
treeMap.put("apple", 2);
treeMap.put("fig", 3);
// Order: fig, apple, banana

// TreeSet sorted by length descending
TreeSet<String> treeSet = new TreeSet<>(
    Comparator.comparingInt(String::length).reversed()
);
treeSet.add("banana");
treeSet.add("fig");
treeSet.add("apple");
// Order: banana, apple, fig
```

---

### 9. Usage with Streams
```java
List<Person> people = Arrays.asList(
    new Person("Alice",   30, 50000.0),
    new Person("Bob",     25, 60000.0),
    new Person("Charlie", 30, 45000.0)
);

// Sort stream by age
people.stream()
      .sorted(Comparator.comparingInt(Person::getAge))
      .forEach(System.out::println);

// Get youngest person
people.stream()
      .min(Comparator.comparingInt(Person::getAge))
      .ifPresent(System.out::println);   // Bob

// Get oldest person
people.stream()
      .max(Comparator.comparingInt(Person::getAge))
      .ifPresent(System.out::println);   // Alice or Charlie
```

---

### 10. Comparator vs Comparable

| Feature | `Comparator` | `Comparable` |
|---------|-------------|--------------|
| Package | `java.util` | `java.lang` |
| Method | `compare(T o1, T o2)` | `compareTo(T o)` |
| Defined in | Separate class / lambda | Inside the class itself |
| Multiple orderings | Yes (multiple comparators) | No (single natural order) |
| Modifies original class | No | Yes |
| Use case | Custom / external sorting | Natural ordering |
```java
// Comparable — natural ordering inside class
class Person implements Comparable<Person> {
    @Override
    public int compareTo(Person other) {
        return Integer.compare(this.age, other.age);
    }
}

// Comparator — external custom ordering
Comparator<Person> byName   = Comparator.comparing(Person::getName);
Comparator<Person> byAge    = Comparator.comparingInt(Person::getAge);
Comparator<Person> bySalary = Comparator.comparingDouble(Person::getSalary);
```

---

### Quick Reference — Common Patterns
```java
// Ascending by field
Comparator.comparing(Person::getName)
Comparator.comparingInt(Person::getAge)

// Descending by field
Comparator.comparing(Person::getName).reversed()
Comparator.comparingInt(Person::getAge).reversed()

// Multi-level sort
Comparator.comparing(Person::getDept)
          .thenComparingInt(Person::getAge)
          .thenComparing(Person::getName)

// Null safe
Comparator.nullsFirst(Comparator.comparing(Person::getName))
Comparator.nullsLast(Comparator.comparingInt(Person::getAge))

// Case insensitive string sort
Comparator.comparing(String::toLowerCase)
Comparator.comparing(Function.identity(), String.CASE_INSENSITIVE_ORDER)
```

> **Best Practices:**
> - Prefer `Comparator.comparingInt()` over `Comparator.comparing()` for primitive `int` to avoid boxing.
> - Use `thenComparing()` for stable multi-level sorting.
> - Use `nullsFirst()` or `nullsLast()` when data may contain `null` values.
> - Prefer method references (`Person::getName`) over lambdas for cleaner code.
> - Use `reversed()` instead of negating the result in `compare()`.