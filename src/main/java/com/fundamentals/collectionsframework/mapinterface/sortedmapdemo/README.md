# SortedMap Methods in Java

## Overview

`SortedMap<K, V>` is a sub-interface of `Map<K, V>` in `java.util` package. It maintains its keys in **ascending sorted order** (natural ordering or via a `Comparator`). The most common implementation is **`TreeMap`**.

```java
import java.util.SortedMap;
import java.util.TreeMap;

SortedMap<String, Integer> map = new TreeMap<>();
```

---

## Methods Inherited from Map (Core Operations)

---

### 1. `put(K key, V value)`
Inserts or updates a key-value pair. Keys are stored in sorted order.

```java
map.put("banana", 2);
map.put("apple", 1);
map.put("cherry", 3);
// Internal order: apple -> banana -> cherry
```

---

### 2. `get(Object key)`
Returns the value mapped to the specified key, or `null` if not found.

```java
int val = map.get("apple"); // 1
```

---

### 3. `remove(Object key)`
Removes the mapping for the specified key. Returns the removed value or `null`.

```java
map.remove("banana");
```

---

### 4. `containsKey(Object key)`
Returns `true` if the map contains the specified key.

```java
map.containsKey("apple"); // true
```

---

### 5. `containsValue(Object value)`
Returns `true` if the map contains the specified value.

```java
map.containsValue(1); // true
```

---

### 6. `size()`
Returns the number of key-value pairs in the map.

```java
map.size(); // 2
```

---

### 7. `isEmpty()`
Returns `true` if the map has no entries.

```java
map.isEmpty(); // false
```

---

### 8. `clear()`
Removes all key-value pairs from the map.

```java
map.clear();
```

---

### 9. `keySet()`
Returns a `Set` view of all keys in **sorted order**.

```java
Set<String> keys = map.keySet(); // [apple, cherry]
```

---

### 10. `values()`
Returns a `Collection` view of all values, ordered by their corresponding sorted keys.

```java
Collection<Integer> values = map.values(); // [1, 3]
```

---

### 11. `entrySet()`
Returns a `Set` of `Map.Entry<K, V>` pairs in sorted key order.

```java
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
```

---

## SortedMap-Specific Methods

---

### 12. `firstKey()`
Returns the **lowest (first) key** in the map. Throws `NoSuchElementException` if map is empty.

```java
map.put("apple", 1);
map.put("banana", 2);
map.put("cherry", 3);

String first = map.firstKey(); // "apple"
```

---

### 13. `lastKey()`
Returns the **highest (last) key** in the map. Throws `NoSuchElementException` if map is empty.

```java
String last = map.lastKey(); // "cherry"
```

---

### 14. `headMap(K toKey)`
Returns a view of the portion of the map whose keys are **strictly less than** `toKey`. The returned map is backed by the original — changes reflect in both.

```java
SortedMap<String, Integer> head = map.headMap("cherry");
// {apple=1, banana=2}
```

---

### 15. `tailMap(K fromKey)`
Returns a view of the portion of the map whose keys are **greater than or equal to** `fromKey`. The returned map is backed by the original.

```java
SortedMap<String, Integer> tail = map.tailMap("banana");
// {banana=2, cherry=3}
```

---

### 16. `subMap(K fromKey, K toKey)`
Returns a view of the portion of the map whose keys range from `fromKey` (**inclusive**) to `toKey` (**exclusive**). Backed by the original map.

```java
SortedMap<String, Integer> sub = map.subMap("apple", "cherry");
// {apple=1, banana=2}
```

---

### 17. `comparator()`
Returns the `Comparator` used to order the keys, or `null` if the map uses the natural ordering of its keys.

```java
Comparator<? super String> comp = map.comparator();
// null for natural ordering

// With custom comparator:
SortedMap<String, Integer> reverseMap = new TreeMap<>(Comparator.reverseOrder());
reverseMap.comparator(); // returns the reverse comparator
```

---

## TreeMap-Specific Extended Methods (NavigableMap)

`TreeMap` also implements `NavigableMap`, providing additional navigation methods:

---

### 18. `floorKey(K key)`
Returns the greatest key **less than or equal to** the given key, or `null` if none.

```java
TreeMap<String, Integer> treeMap = new TreeMap<>(map);
treeMap.floorKey("avocado"); // "apple"
```

---

### 19. `ceilingKey(K key)`
Returns the smallest key **greater than or equal to** the given key, or `null` if none.

```java
treeMap.ceilingKey("avocado"); // "banana"
```

---

### 20. `lowerKey(K key)`
Returns the greatest key **strictly less than** the given key, or `null` if none.

```java
treeMap.lowerKey("banana"); // "apple"
```

---

### 21. `higherKey(K key)`
Returns the smallest key **strictly greater than** the given key, or `null` if none.

```java
treeMap.higherKey("banana"); // "cherry"
```

---

### 22. `pollFirstEntry()`
Removes and returns the entry with the **lowest key**, or `null` if map is empty.

```java
Map.Entry<String, Integer> first = treeMap.pollFirstEntry();
// apple=1 (removed from map)
```

---

### 23. `pollLastEntry()`
Removes and returns the entry with the **highest key**, or `null` if map is empty.

```java
Map.Entry<String, Integer> last = treeMap.pollLastEntry();
// cherry=3 (removed from map)
```

---

### 24. `descendingMap()`
Returns a reverse-order view of the map.

```java
NavigableMap<String, Integer> desc = treeMap.descendingMap();
// {cherry=3, banana=2, apple=1}
```

---

### 25. `descendingKeySet()`
Returns a reverse-order `NavigableSet` view of the keys.

```java
NavigableSet<String> descKeys = treeMap.descendingKeySet();
// [cherry, banana, apple]
```

---

## Key Differences: SortedMap vs HashMap

| Feature | `HashMap` | `SortedMap` (TreeMap) |
|---|---|---|
| Order | No order | Sorted by keys |
| `null` keys | One allowed | Not allowed |
| Performance | O(1) avg | O(log n) |
| Navigation methods | None | `firstKey`, `lastKey`, `headMap`, etc. |
| Use case | Fast lookup | Sorted/range queries |

---

## Quick Reference Table

| Method | Description |
|---|---|
| `put(k, v)` | Insert / update |
| `get(k)` | Retrieve value |
| `remove(k)` | Delete entry |
| `containsKey(k)` | Check key existence |
| `containsValue(v)` | Check value existence |
| `size()` | Number of entries |
| `isEmpty()` | Check if empty |
| `clear()` | Remove all entries |
| `keySet()` | All keys (sorted) |
| `values()` | All values (sorted by key) |
| `entrySet()` | All key-value pairs (sorted) |
| `firstKey()` | Lowest key |
| `lastKey()` | Highest key |
| `headMap(toKey)` | Keys < toKey |
| `tailMap(fromKey)` | Keys >= fromKey |
| `subMap(from, to)` | Keys in [from, to) |
| `comparator()` | Returns key comparator |
| `floorKey(k)` | Greatest key <= k |
| `ceilingKey(k)` | Smallest key >= k |
| `lowerKey(k)` | Greatest key < k |
| `higherKey(k)` | Smallest key > k |
| `pollFirstEntry()` | Remove & return first entry |
| `pollLastEntry()` | Remove & return last entry |
| `descendingMap()` | Reverse-order map view |
| `descendingKeySet()` | Reverse-order key set |