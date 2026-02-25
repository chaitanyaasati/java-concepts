# LinkedHashMap Methods in Java

## Overview

`LinkedHashMap<K, V>` extends `HashMap<K, V>` and implements `Map<K, V>` in the `java.util` package. It maintains a **doubly-linked list** running through all of its entries, preserving **insertion order** (or optionally **access order**). It allows one `null` key and multiple `null` values. It is **not thread-safe**.

```java
import java.util.LinkedHashMap;

// Insertion-order (default)
LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

// Access-order (LRU cache behavior)
LinkedHashMap<String, Integer> lruMap = new LinkedHashMap<>(16, 0.75f, true);
```

---

## Core Map Methods (Inherited from HashMap)

---

### 1. `put(K key, V value)`
Inserts or updates a key-value pair. Maintains **insertion order**. Returns the previous value or `null`.

```java
LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
map.put("apple", 1);
map.put("banana", 2);
map.put("cherry", 3);
// Order preserved: apple -> banana -> cherry
```

---

### 2. `get(Object key)`
Returns the value mapped to the specified key, or `null` if not found. In **access-order** mode, moves the accessed entry to the end.

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
Removes the mapping for the specified key while maintaining the linked order of remaining entries. Returns the removed value or `null`.

```java
map.remove("banana");
// Order: apple -> cherry
```

---

### 5. `containsKey(Object key)`
Returns `true` if the map contains the specified key.

```java
map.containsKey("apple"); // true
```

---

### 6. `containsValue(Object value)`
Returns `true` if the map contains the specified value. Traversal follows insertion order.

```java
map.containsValue(1); // true
```

---

### 7. `size()`
Returns the number of key-value pairs in the map.

```java
map.size(); // 2
```

---

### 8. `isEmpty()`
Returns `true` if the map has no entries.

```java
map.isEmpty(); // false
```

---

### 9. `clear()`
Removes all key-value pairs and resets the linked list.

```java
map.clear();
```

---

### 10. `keySet()`
Returns a `Set` view of all keys in **insertion order** (or access order if configured).

```java
map.put("apple", 1);
map.put("banana", 2);
map.put("cherry", 3);

Set<String> keys = map.keySet();
// [apple, banana, cherry] — insertion order preserved
```

---

### 11. `values()`
Returns a `Collection` view of all values in insertion order.

```java
Collection<Integer> values = map.values();
// [1, 2, 3]
```

---

### 12. `entrySet()`
Returns a `Set` of `Map.Entry<K, V>` pairs in insertion order. Most commonly used for iteration.

```java
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
// apple = 1
// banana = 2
// cherry = 3
```

---

### 13. `putIfAbsent(K key, V value)`
Inserts the key-value pair only if the key is **not already present**. Preserves insertion order.

```java
map.putIfAbsent("apple", 99); // Won't update since "apple" exists
map.putIfAbsent("date", 4);   // Inserts at end: apple -> banana -> cherry -> date
```

---

### 14. `replace(K key, V value)`
Replaces the value for a key only if it currently has a mapping. Does **not** change the position in insertion order.

```java
map.replace("apple", 10); // Position in order unchanged
```

---

### 15. `replace(K key, V oldValue, V newValue)`
Replaces the entry only if currently mapped to the given old value. Returns `true` on success.

```java
map.replace("apple", 10, 20);
```

---

### 16. `putAll(Map<? extends K, ? extends V> m)`
Copies all mappings from the specified map. New keys are appended at the end in the order returned by the source map's iterator.

```java
LinkedHashMap<String, Integer> other = new LinkedHashMap<>();
other.put("elderberry", 5);
other.put("fig", 6);
map.putAll(other);
// Order: apple -> banana -> cherry -> date -> elderberry -> fig
```

---

### 17. `compute(K key, BiFunction remappingFunction)`
Computes a new value for the key using the provided function. Removes the entry if the function returns `null`.

```java
map.compute("apple", (k, v) -> v == null ? 1 : v + 1);
```

---

### 18. `computeIfAbsent(K key, Function mappingFunction)`
Computes and inserts a value only if the key is not already present. New entry appended at end.

```java
map.computeIfAbsent("grape", k -> k.length()); // grape -> 5
```

---

### 19. `computeIfPresent(K key, BiFunction remappingFunction)`
Recomputes the value for a key only if it's currently present and non-null.

```java
map.computeIfPresent("apple", (k, v) -> v * 2);
```

---

### 20. `merge(K key, V value, BiFunction remappingFunction)`
Merges the given value with the existing value using the function, or inserts if absent.

```java
map.merge("apple", 5, Integer::sum);
```

---

### 21. `forEach(BiConsumer action)`
Performs the given action for each entry **in insertion order**.

```java
map.forEach((k, v) -> System.out.println(k + ": " + v));
// apple: 20
// banana: 2
// cherry: 3
```

---

## LinkedHashMap-Specific Feature

---

### 22. `removeEldestEntry(Map.Entry<K, V> eldest)` *(Override)*
A **protected** method called automatically after each `put` or `putAll`. Override it to implement **eviction policies** such as a fixed-size LRU cache. Returns `true` to remove the eldest entry.

```java
// LRU Cache — keeps only the 3 most recently used entries
LinkedHashMap<String, Integer> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
        return size() > 3;
    }
};

lruCache.put("a", 1);
lruCache.put("b", 2);
lruCache.put("c", 3);
lruCache.get("a");    // Access "a" — moves it to end
lruCache.put("d", 4); // Triggers removeEldestEntry — "b" is removed
// Remaining: {c=3, a=1, d=4}
```

---

## Constructors Summary

| Constructor | Description |
|---|---|
| `LinkedHashMap()` | Default capacity 16, load factor 0.75, insertion order |
| `LinkedHashMap(int initialCapacity)` | Custom initial capacity, insertion order |
| `LinkedHashMap(int initialCapacity, float loadFactor)` | Custom capacity and load factor |
| `LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder)` | `true` for access order, `false` for insertion order |
| `LinkedHashMap(Map<? extends K, ? extends V> m)` | Copies from existing map |

---

## Insertion Order vs Access Order

```java
// Insertion Order (default)
LinkedHashMap<String, Integer> insertionMap = new LinkedHashMap<>();
insertionMap.put("a", 1);
insertionMap.put("b", 2);
insertionMap.put("c", 3);
insertionMap.get("a");
System.out.println(insertionMap.keySet()); // [a, b, c]

// Access Order
LinkedHashMap<String, Integer> accessMap = new LinkedHashMap<>(16, 0.75f, true);
accessMap.put("a", 1);
accessMap.put("b", 2);
accessMap.put("c", 3);
accessMap.get("a"); // "a" moves to end
System.out.println(accessMap.keySet()); // [b, c, a]
```

---

## Comparison: HashMap vs LinkedHashMap vs TreeMap

| Feature | `HashMap` | `LinkedHashMap` | `TreeMap` |
|---|---|---|---|
| Order | None | Insertion / Access | Sorted (natural/comparator) |
| `null` keys | 1 allowed | 1 allowed | Not allowed |
| Performance | O(1) avg | O(1) avg | O(log n) |
| Memory overhead | Low | Medium (linked list) | High (tree nodes) |
| Use case | Fast lookup | Ordered iteration / LRU cache | Sorted/range queries |
| Thread-safe | No | No | No |

---

## Quick Reference Table

| Method | Description |
|---|---|
| `put(k, v)` | Insert / update (preserves insertion order) |
| `get(k)` | Retrieve value (moves to end in access-order) |
| `getOrDefault(k, def)` | Get with fallback |
| `remove(k)` | Delete entry, order of rest preserved |
| `containsKey(k)` | Check key existence |
| `containsValue(v)` | Check value existence |
| `size()` | Number of entries |
| `isEmpty()` | Check if empty |
| `clear()` | Remove all entries |
| `keySet()` | All keys in insertion/access order |
| `values()` | All values in insertion/access order |
| `entrySet()` | All key-value pairs in insertion/access order |
| `putIfAbsent(k, v)` | Insert if key not present |
| `replace(k, v)` | Replace value without changing position |
| `putAll(map)` | Bulk insert, new keys appended at end |
| `compute(k, fn)` | Compute new value |
| `computeIfAbsent(k, fn)` | Compute if key missing |
| `computeIfPresent(k, fn)` | Compute if key present |
| `merge(k, v, fn)` | Merge values |
| `forEach(fn)` | Iterate in insertion/access order |
| `removeEldestEntry(e)` | Override for eviction / LRU cache |