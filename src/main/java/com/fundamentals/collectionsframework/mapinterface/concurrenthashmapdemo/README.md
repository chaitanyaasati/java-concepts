# ConcurrentHashMap Methods in Java

## Overview

`ConcurrentHashMap<K, V>` is part of the `java.util.concurrent` package. It is a **thread-safe**, high-performance alternative to `HashMap` and `Hashtable`. It achieves concurrency by using **segment-level locking** (Java 7) and **CAS (Compare-And-Swap) + synchronized blocks on individual buckets** (Java 8+), allowing multiple threads to read and write simultaneously without locking the entire map.

**Key characteristics:**
- Thread-safe without locking the whole map
- Does **not** allow `null` keys or `null` values
- Does **not** guarantee insertion order
- Ideal for high-concurrency read/write scenarios

```java
import java.util.concurrent.ConcurrentHashMap;

ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
```

---

## Core Map Methods

---

### 1. `put(K key, V value)`
Inserts or updates a key-value pair in a thread-safe manner. Neither key nor value can be `null`.

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("apple", 1);
map.put("banana", 2);
map.put("cherry", 3);
```

---

### 2. `get(Object key)`
Returns the value mapped to the specified key, or `null` if not found. Non-blocking read operation.

```java
int val = map.get("apple"); // 1
```

---

### 3. `getOrDefault(Object key, V defaultValue)`
Returns the value for the key, or the specified default if the key doesn't exist.

```java
int val = map.getOrDefault("mango", 0); // 0
```

---

### 4. `remove(Object key)`
Removes the mapping for the specified key in a thread-safe manner. Returns the removed value or `null`.

```java
map.remove("banana"); // removes "banana" -> 2
```

---

### 5. `remove(Object key, Object value)`
Removes the entry only if the key is currently mapped to the specified value. Returns `true` if removed. This is an **atomic** operation.

```java
map.put("apple", 1);
boolean removed = map.remove("apple", 1);  // true
boolean notRemoved = map.remove("apple", 99); // false — value doesn't match
```

---

### 6. `containsKey(Object key)`
Returns `true` if the map contains the specified key. Non-blocking.

```java
map.containsKey("apple"); // true
```

---

### 7. `containsValue(Object value)`
Returns `true` if the map contains one or more keys mapped to the specified value. May require full traversal.

```java
map.containsValue(1); // true
```

---

### 8. `size()`
Returns the number of key-value pairs. In highly concurrent scenarios, this is an **approximate count** — use `mappingCount()` for large maps.

```java
map.size(); // e.g., 2
```

---

### 9. `isEmpty()`
Returns `true` if the map has no entries.

```java
map.isEmpty(); // false
```

---

### 10. `clear()`
Removes all key-value pairs from the map in a thread-safe manner.

```java
map.clear();
```

---

### 11. `keySet()`
Returns a `Set` view of all keys. The set is backed by the map, so changes to the map are reflected in the set.

```java
Set<String> keys = map.keySet();
```

---

### 12. `values()`
Returns a `Collection` view of all values backed by the map.

```java
Collection<Integer> values = map.values();
```

---

### 13. `entrySet()`
Returns a `Set` of `Map.Entry<K, V>` pairs backed by the map. Safe for concurrent iteration.

```java
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
```

---

### 14. `putAll(Map<? extends K, ? extends V> m)`
Copies all mappings from the specified map into this map atomically per entry.

```java
Map<String, Integer> other = new HashMap<>();
other.put("date", 4);
other.put("elderberry", 5);
map.putAll(other);
```

---

## Atomic / Concurrent-Specific Methods

These methods perform **atomic read-modify-write** operations, making them safe and efficient for concurrent use without external synchronization.

---

### 15. `putIfAbsent(K key, V value)`
Inserts the key-value pair **only if the key is not already present**. Atomic operation. Returns the existing value if key is present, or `null` if inserted.

```java
map.put("apple", 1);
map.putIfAbsent("apple", 99); // Returns 1 — not updated
map.putIfAbsent("fig", 6);    // Returns null — inserted
```

---

### 16. `replace(K key, V value)`
Replaces the value for a key only if it currently has a mapping. Returns the old value or `null`. Atomic operation.

```java
map.replace("apple", 10); // Returns 1 (old value), now apple -> 10
```

---

### 17. `replace(K key, V oldValue, V newValue)`
Replaces the entry only if currently mapped to `oldValue`. Returns `true` if replaced. Fully **atomic compare-and-swap**.

```java
boolean replaced = map.replace("apple", 10, 20); // true
boolean notReplaced = map.replace("apple", 99, 50); // false — 99 doesn't match current value
```

---

### 18. `compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)`
Atomically computes a new value for the key. Removes the entry if the function returns `null`.

```java
// Increment count atomically
map.compute("apple", (k, v) -> v == null ? 1 : v + 1);
```

---

### 19. `computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)`
Atomically computes and inserts a value only if the key is not already present. Useful for initializing complex values.

```java
// Initialize a list if key is absent
ConcurrentHashMap<String, List<String>> multiMap = new ConcurrentHashMap<>();
multiMap.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
```

---

### 20. `computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)`
Atomically recomputes the value for a key only if it's currently present and non-null.

```java
map.computeIfPresent("apple", (k, v) -> v * 2); // apple -> 40
```

---

### 21. `merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)`
Atomically merges the given value with the existing value using the function, or inserts if absent. Removes entry if function returns `null`.

```java
// Word frequency counter
map.merge("apple", 1, Integer::sum); // apple -> 41
map.merge("newFruit", 1, Integer::sum); // newFruit -> 1 (inserted)
```

---

### 22. `forEach(BiConsumer<? super K, ? super V> action)`
Performs the given action for each entry. Safe for use during concurrent modifications.

```java
map.forEach((k, v) -> System.out.println(k + ": " + v));
```

---

### 23. `forEach(long parallelismThreshold, BiConsumer<? super K, ? super V> action)`
Parallel version of `forEach`. Uses the **Fork/Join pool** if the map size exceeds the threshold. Use `1` to always run in parallel.

```java
map.forEach(1, (k, v) -> System.out.println(Thread.currentThread().getName() + ": " + k));
```

---

## Bulk Parallel Operations (Java 8+)

`ConcurrentHashMap` supports powerful **parallel bulk operations** using the Fork/Join framework. Each takes a `parallelismThreshold` — if map size is below this, runs sequentially.

---

### 24. `search(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> searchFunction)`
Searches entries in parallel and returns the first non-null result from the function, or `null` if none found.

```java
// Find first key with value > 10
String result = map.search(1, (k, v) -> v > 10 ? k : null);
System.out.println(result); // e.g., "apple"
```

---

### 25. `searchKeys(long parallelismThreshold, Function<? super K, ? extends U> searchFunction)`
Searches only the keys in parallel. Returns the first non-null result.

```java
String key = map.searchKeys(1, k -> k.startsWith("a") ? k : null);
System.out.println(key); // "apple"
```

---

### 26. `searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction)`
Searches only the values in parallel. Returns the first non-null result.

```java
Integer found = map.searchValues(1, v -> v > 5 ? v : null);
```

---

### 27. `reduce(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer)`
Performs a parallel map-reduce over entries. Transforms each entry, then reduces results.

```java
// Sum all values in parallel
Integer total = map.reduce(1,
    (k, v) -> v,          // transformer: use value as-is
    Integer::sum           // reducer: sum them up
);
System.out.println(total);
```

---

### 28. `reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer)`
Reduces all values in parallel using the given function.

```java
Integer sum = map.reduceValues(1, Integer::sum);
```

---

### 29. `reduceKeys(long parallelismThreshold, BiFunction<? super K, ? super K, ? extends K> reducer)`
Reduces all keys in parallel using the given function.

```java
String allKeys = map.reduceKeys(1, (a, b) -> a + ", " + b);
System.out.println(allKeys); // e.g., "apple, cherry, date"
```

---

### 30. `mappingCount()`
Returns the number of mappings as a `long`. Preferred over `size()` for large maps in concurrent environments as it provides a more accurate estimate.

```java
long count = map.mappingCount();
```

---

### 31. `newKeySet()` *(static)*
Creates a new `Set` backed by a `ConcurrentHashMap` — useful when you need a concurrent `Set`.

```java
Set<String> concurrentSet = ConcurrentHashMap.newKeySet();
concurrentSet.add("apple");
concurrentSet.add("banana");
```

---

## Common Concurrent Patterns

### Word Frequency Counter
```java
ConcurrentHashMap<String, Integer> freq = new ConcurrentHashMap<>();
String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};

for (String word : words) {
    freq.merge(word, 1, Integer::sum);
}
System.out.println(freq); // {apple=3, banana=2, cherry=1}
```

### Thread-Safe Initialization with `computeIfAbsent`
```java
ConcurrentHashMap<String, List<String>> groupMap = new ConcurrentHashMap<>();

// Safe even when multiple threads access the same key simultaneously
groupMap.computeIfAbsent("fruits", k -> new CopyOnWriteArrayList<>()).add("apple");
groupMap.computeIfAbsent("fruits", k -> new CopyOnWriteArrayList<>()).add("banana");
```

### Atomic Conditional Update with `replace`
```java
ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();
stock.put("apple", 10);

// Safe decrement — only decrements if current value matches
boolean success = stock.replace("apple", 10, 9); // true
```

---

## Comparison: HashMap vs Hashtable vs ConcurrentHashMap

| Feature | `HashMap` | `Hashtable` | `ConcurrentHashMap` |
|---|---|---|---|
| Thread-safe | No | Yes (full lock) | Yes (partial lock / CAS) |
| `null` keys | 1 allowed | Not allowed | Not allowed |
| `null` values | Allowed | Not allowed | Not allowed |
| Performance | O(1) best | Low (full sync) | High (concurrent) |
| Iteration | Fail-fast | Fail-safe | Weakly consistent |
| Bulk operations | No | No | Yes (parallel) |
| Use case | Single-thread | Legacy code | Multi-thread |

---

## Quick Reference Table

| Method | Description |
|---|---|
| `put(k, v)` | Insert / update (thread-safe) |
| `get(k)` | Retrieve value (non-blocking) |
| `getOrDefault(k, def)` | Get with fallback |
| `remove(k)` | Delete entry |
| `remove(k, v)` | Atomic conditional remove |
| `containsKey(k)` | Check key existence |
| `containsValue(v)` | Check value existence |
| `size()` | Approximate count |
| `mappingCount()` | Accurate count (long) for large maps |
| `isEmpty()` | Check if empty |
| `clear()` | Remove all entries |
| `keySet()` | All keys |
| `values()` | All values |
| `entrySet()` | All key-value pairs |
| `putAll(map)` | Bulk insert |
| `putIfAbsent(k, v)` | Atomic insert if absent |
| `replace(k, v)` | Atomic replace |
| `replace(k, old, new)` | Atomic compare-and-swap replace |
| `compute(k, fn)` | Atomic compute |
| `computeIfAbsent(k, fn)` | Atomic compute if absent |
| `computeIfPresent(k, fn)` | Atomic compute if present |
| `merge(k, v, fn)` | Atomic merge |
| `forEach(fn)` | Iterate all entries |
| `forEach(threshold, fn)` | Parallel iterate |
| `search(threshold, fn)` | Parallel search entries |
| `searchKeys(threshold, fn)` | Parallel search keys |
| `searchValues(threshold, fn)` | Parallel search values |
| `reduce(threshold, fn, fn)` | Parallel map-reduce |
| `reduceValues(threshold, fn)` | Parallel reduce values |
| `reduceKeys(threshold, fn)` | Parallel reduce keys |
| `newKeySet()` | Create concurrent Set |