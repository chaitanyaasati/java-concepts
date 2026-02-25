# HashMap Methods in Java

## Overview

`HashMap<K, V>` is part of `java.util` package and stores key-value pairs. It allows one `null` key and multiple `null` values. It is **not thread-safe** and does not maintain insertion order.

---

## 1. `put(K key, V value)`
Inserts or updates a key-value pair. Returns the previous value associated with the key, or `null`.

```java
HashMap<String, Integer> map = new HashMap<>();
map.put("apple", 1);
map.put("banana", 2);
```

---

## 2. `get(Object key)`
Returns the value mapped to the specified key, or `null` if not found.

```java
int val = map.get("apple"); // 1
```

---

## 3. `getOrDefault(Object key, V defaultValue)`
Returns the value for the key, or the specified default if the key doesn't exist.

```java
int val = map.getOrDefault("mango", 0); // 0
```

---

## 4. `remove(Object key)`
Removes the mapping for the specified key. Returns the removed value or `null`.

```java
map.remove("banana"); // removes "banana" -> 2
```

---

## 5. `containsKey(Object key)`
Returns `true` if the map contains the specified key.

```java
map.containsKey("apple"); // true
```

---

## 6. `containsValue(Object value)`
Returns `true` if the map contains the specified value.

```java
map.containsValue(1); // true
```

---

## 7. `size()`
Returns the number of key-value pairs in the map.

```java
map.size(); // e.g., 1
```

---

## 8. `isEmpty()`
Returns `true` if the map contains no key-value pairs.

```java
map.isEmpty(); // false
```

---

## 9. `clear()`
Removes all key-value pairs from the map.

```java
map.clear();
```

---

## 10. `keySet()`
Returns a `Set` view of all keys in the map.

```java
Set<String> keys = map.keySet();
```

---

## 11. `values()`
Returns a `Collection` view of all values in the map.

```java
Collection<Integer> values = map.values();
```

---

## 12. `entrySet()`
Returns a `Set` of `Map.Entry<K, V>` objects — useful for iterating over key-value pairs.

```java
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
```

---

## 13. `putIfAbsent(K key, V value)`
Inserts the key-value pair only if the key is **not already present**.

```java
map.putIfAbsent("apple", 99); // Won't update since "apple" exists
```

---

## 14. `replace(K key, V value)`
Replaces the value for a key only if it currently has a mapping. Returns the old value or `null`.

```java
map.replace("apple", 10);
```

---

## 15. `replace(K key, V oldValue, V newValue)`
Replaces the entry for a key only if currently mapped to the given old value. Returns `true` on success.

```java
map.replace("apple", 10, 20);
```

---

## 16. `putAll(Map<? extends K, ? extends V> m)`
Copies all mappings from the specified map into this map.

```java
HashMap<String, Integer> other = new HashMap<>();
other.put("cherry", 3);
map.putAll(other);
```

---

## 17. `compute(K key, BiFunction remappingFunction)`
Computes a new value for the key using the provided function. Removes entry if function returns `null`.

```java
map.compute("apple", (k, v) -> v == null ? 1 : v + 1);
```

---

## 18. `computeIfAbsent(K key, Function mappingFunction)`
Computes the value for a key only if it's not already present.

```java
map.computeIfAbsent("grape", k -> k.length());
```

---

## 19. `computeIfPresent(K key, BiFunction remappingFunction)`
Recomputes value for a key only if it's currently present and non-null.

```java
map.computeIfPresent("apple", (k, v) -> v * 2);
```

---

## 20. `merge(K key, V value, BiFunction remappingFunction)`
Merges the given value with the existing value using the function, or inserts if absent.

```java
map.merge("apple", 5, Integer::sum);
```

---

## 21. `forEach(BiConsumer action)`
Performs the given action for each entry in the map.

```java
map.forEach((k, v) -> System.out.println(k + ": " + v));
```

---

## Quick Reference Table

| Method | Description |
|---|---|
| `put(k, v)` | Insert / update |
| `get(k)` | Retrieve value |
| `getOrDefault(k, def)` | Get with fallback |
| `remove(k)` | Delete entry |
| `containsKey(k)` | Check key existence |
| `containsValue(v)` | Check value existence |
| `size()` | Number of entries |
| `isEmpty()` | Check if empty |
| `clear()` | Remove all entries |
| `keySet()` | All keys |
| `values()` | All values |
| `entrySet()` | All key-value pairs |
| `putIfAbsent(k, v)` | Insert if not present |
| `replace(k, v)` | Replace value |
| `putAll(map)` | Bulk insert |
| `compute(k, fn)` | Compute new value |
| `computeIfAbsent(k, fn)` | Compute if missing |
| `computeIfPresent(k, fn)` | Compute if present |
| `merge(k, v, fn)` | Merge values |
| `forEach(fn)` | Iterate all entries |