# Internal working of ArrayList

- Unlike a regular array, which has a fixed size, an ArrayList can grow and shrink as elements are added or removed. This dynamic resizing is acheived by creating a new array when the current array is full and copying the elements to the new array.
- Internally, the arraylist is implemented as an array of object references. When you add elements to an arraylist, you are essentially storing these elements in this internal array.
- When you create an ArrayList, it has an initial capacity(defaults to 10). The capacity refers to the size of the internal array that can hold elements before needing to resize.

## The for-each loop works only with things that can be iterated.

# In Java, that means objects that are:

- 1️⃣ Arrays
- 2️⃣ Objects that implement Iterable interface (i.e., Collections)


# ArrayList Methods in Java

## Overview

`ArrayList<E>` is part of the `java.util` package and implements the `List<E>` interface. It is a **resizable-array** implementation that dynamically grows and shrinks as elements are added or removed. It maintains **insertion order**, allows **duplicate elements**, and permits `null` values. It is **not thread-safe**.

**Key characteristics:**
- Backed by a dynamic array
- Allows duplicates and `null` values
- Maintains insertion order
- Fast random access — O(1) for `get` and `set`
- Slow insertions/deletions in the middle — O(n)
- Not thread-safe (use `Collections.synchronizedList()` or `CopyOnWriteArrayList` for concurrency)

```java
import java.util.ArrayList;

ArrayList<String> list = new ArrayList<>();

// With initial capacity (avoids resizing overhead)
ArrayList<String> listWithCapacity = new ArrayList<>(20);

// From existing collection
ArrayList<String> fromCollection = new ArrayList<>(List.of("apple", "banana", "cherry"));
```

---

## Adding Elements

---

### 1. `add(E element)`
Appends the specified element to the **end** of the list. Always returns `true`.

```java
ArrayList<String> list = new ArrayList<>();
list.add("apple");
list.add("banana");
list.add("cherry");
// [apple, banana, cherry]
```

---

### 2. `add(int index, E element)`
Inserts the element at the specified index, shifting existing elements to the right. Throws `IndexOutOfBoundsException` if index is out of range.

```java
list.add(1, "avocado");
// [apple, avocado, banana, cherry]
```

---

### 3. `addAll(Collection<? extends E> c)`
Appends all elements of the specified collection to the end of the list, in the order returned by the collection's iterator.

```java
List<String> more = List.of("date", "elderberry");
list.addAll(more);
// [apple, avocado, banana, cherry, date, elderberry]
```

---

### 4. `addAll(int index, Collection<? extends E> c)`
Inserts all elements of the specified collection at the given index, shifting existing elements to the right.

```java
List<String> fruits = List.of("fig", "grape");
list.addAll(2, fruits);
// [apple, avocado, fig, grape, banana, cherry, date, elderberry]
```

---

## Accessing Elements

---

### 5. `get(int index)`
Returns the element at the specified index. O(1) operation. Throws `IndexOutOfBoundsException` for invalid index.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
String fruit = list.get(1); // "banana"
```

---

### 6. `indexOf(Object o)`
Returns the index of the **first occurrence** of the specified element, or `-1` if not found.

```java
list.add("apple");
int idx = list.indexOf("apple"); // 0
```

---

### 7. `lastIndexOf(Object o)`
Returns the index of the **last occurrence** of the specified element, or `-1` if not found.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "apple"));
int idx = list.lastIndexOf("apple"); // 2
```

---

### 8. `contains(Object o)`
Returns `true` if the list contains the specified element. Uses `.equals()` for comparison.

```java
list.contains("banana"); // true
list.contains("mango");  // false
```

---

### 9. `size()`
Returns the number of elements in the list.

```java
list.size(); // 3
```

---

### 10. `isEmpty()`
Returns `true` if the list contains no elements.

```java
list.isEmpty(); // false
```

---

## Updating Elements

---

### 11. `set(int index, E element)`
Replaces the element at the specified index with the given element. Returns the old element. Throws `IndexOutOfBoundsException` for invalid index.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
String old = list.set(1, "blueberry"); // old = "banana"
// [apple, blueberry, cherry]
```

---

### 12. `replaceAll(UnaryOperator<E> operator)`
Replaces each element with the result of applying the given operator. Modifies the list in-place.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
list.replaceAll(String::toUpperCase);
// [APPLE, BANANA, CHERRY]
```

---

## Removing Elements

---

### 13. `remove(int index)`
Removes the element at the specified index, shifting subsequent elements left. Returns the removed element. Throws `IndexOutOfBoundsException` for invalid index.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
String removed = list.remove(1); // "banana"
// [apple, cherry]
```

---

### 14. `remove(Object o)`
Removes the **first occurrence** of the specified element. Returns `true` if found and removed.

```java
list.remove("apple"); // true
// [cherry]
```

---

### 15. `removeAll(Collection<?> c)`
Removes all elements in the list that are also contained in the specified collection.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry", "date"));
list.removeAll(List.of("banana", "date"));
// [apple, cherry]
```

---

### 16. `removeIf(Predicate<? super E> filter)`
Removes all elements that satisfy the given predicate. Returns `true` if any elements were removed.

```java
ArrayList<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
numbers.removeIf(n -> n % 2 == 0); // remove even numbers
// [1, 3, 5]
```

---

### 17. `retainAll(Collection<?> c)`
Retains only the elements that are contained in the specified collection. Removes everything else. Returns `true` if the list changed.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry", "date"));
list.retainAll(List.of("banana", "cherry"));
// [banana, cherry]
```

---

### 18. `clear()`
Removes all elements from the list. The list will be empty after this call.

```java
list.clear();
list.isEmpty(); // true
```

---

## Searching and Sorting

---

### 19. `contains(Object o)`
*(See #8 above)* Returns `true` if the list contains the specified element.

---

### 20. `sort(Comparator<? super E> c)`
Sorts the list according to the given comparator. Pass `null` for natural ordering (elements must implement `Comparable`).

```java
ArrayList<String> list = new ArrayList<>(List.of("banana", "apple", "cherry"));

// Natural (alphabetical) order
list.sort(null);
// [apple, banana, cherry]

// Reverse order
list.sort(Comparator.reverseOrder());
// [cherry, banana, apple]

// By length
list.sort(Comparator.comparingInt(String::length));
// [apple, banana, cherry]
```

---

### 21. `Collections.binarySearch(List, key)`
Performs binary search on a **sorted** list. Returns index of the key, or a negative value if not found. List must be sorted before calling.

```java
ArrayList<Integer> nums = new ArrayList<>(List.of(1, 3, 5, 7, 9));
int idx = Collections.binarySearch(nums, 5); // 2
```

---

## Iterating

---

### 22. `forEach(Consumer<? super E> action)`
Performs the given action for each element in iteration order.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
list.forEach(fruit -> System.out.println(fruit));
```

---

### 23. `iterator()`
Returns an `Iterator` over the elements in proper sequence. Supports `hasNext()`, `next()`, and `remove()`.

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String fruit = it.next();
    if (fruit.equals("banana")) {
        it.remove(); // safe removal during iteration
    }
}
```

---

### 24. `listIterator()`
Returns a `ListIterator` which supports bidirectional traversal and modification.

```java
ListIterator<String> lit = list.listIterator(list.size()); // start from end
while (lit.hasPrevious()) {
    System.out.println(lit.previous()); // iterates in reverse
}
```

---

## Sub-List and Conversion

---

### 25. `subList(int fromIndex, int toIndex)`
Returns a **view** of the portion of the list from `fromIndex` (inclusive) to `toIndex` (exclusive). Changes to the sublist are reflected in the original list.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry", "date", "elderberry"));
List<String> sub = list.subList(1, 4);
// [banana, cherry, date]

// Useful pattern: remove a range
list.subList(1, 4).clear();
// [apple, elderberry]
```

---

### 26. `toArray()`
Returns an `Object[]` array containing all elements in proper sequence.

```java
Object[] arr = list.toArray();
```

---

### 27. `toArray(T[] a)`
Returns a typed array containing all elements. Preferred over `toArray()` for type safety.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
String[] arr = list.toArray(new String[0]);
// ["apple", "banana", "cherry"]
```

---

## Capacity Management

---

### 28. `ensureCapacity(int minCapacity)`
Increases the capacity of the list, if necessary, to ensure it can hold at least `minCapacity` elements. Useful to avoid repeated resizing during bulk inserts.

```java
ArrayList<Integer> list = new ArrayList<>();
list.ensureCapacity(1000); // pre-allocate for 1000 elements
for (int i = 0; i < 1000; i++) {
    list.add(i);
}
```

---

### 29. `trimToSize()`
Trims the internal array capacity to the current list size. Useful to minimize memory usage when the list won't grow further.

```java
ArrayList<String> list = new ArrayList<>(100);
list.add("apple");
list.add("banana");
list.trimToSize(); // capacity reduced to 2
```

---

## Stream and Functional Operations

---

### 30. `stream()`
Returns a sequential `Stream` over the elements. Enables powerful functional-style operations.

```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry", "avocado"));

// Filter and collect
List<String> aFruits = list.stream()
    .filter(s -> s.startsWith("a"))
    .collect(Collectors.toList());
// [apple, avocado]

// Map and collect
List<Integer> lengths = list.stream()
    .map(String::length)
    .collect(Collectors.toList());
// [5, 6, 6, 7]
```

---

### 31. `parallelStream()`
Returns a parallel `Stream` for concurrent processing using the Fork/Join pool. Useful for large datasets.

```java
long count = list.parallelStream()
    .filter(s -> s.length() > 5)
    .count();
```

---

## Common Patterns

### Removing Duplicates
```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "apple", "cherry", "banana"));
ArrayList<String> unique = new ArrayList<>(new LinkedHashSet<>(list));
// [apple, banana, cherry]
```

### Reversing a List
```java
ArrayList<String> list = new ArrayList<>(List.of("apple", "banana", "cherry"));
Collections.reverse(list);
// [cherry, banana, apple]
```

### Shuffling
```java
Collections.shuffle(list);
```

### Finding Min / Max
```java
ArrayList<Integer> nums = new ArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2));
int min = Collections.min(nums); // 1
int max = Collections.max(nums); // 9
```

### Thread-Safe ArrayList
```java
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
```

---

## Time Complexity Summary

| Operation | Time Complexity |
|---|---|
| `get(index)` | O(1) |
| `set(index, e)` | O(1) |
| `add(e)` at end | O(1) amortized |
| `add(index, e)` | O(n) |
| `remove(index)` | O(n) |
| `remove(object)` | O(n) |
| `contains(o)` | O(n) |
| `indexOf(o)` | O(n) |
| `size()` | O(1) |
| `sort()` | O(n log n) |
| `toArray()` | O(n) |

---

## Comparison: ArrayList vs LinkedList vs Vector

| Feature | `ArrayList` | `LinkedList` | `Vector` |
|---|---|---|---|
| Internal structure | Dynamic array | Doubly linked list | Dynamic array |
| Random access | O(1) | O(n) | O(1) |
| Insert/delete (middle) | O(n) | O(1) | O(n) |
| Thread-safe | No | No | Yes (full sync) |
| `null` elements | Allowed | Allowed | Allowed |
| Memory overhead | Low | High (node pointers) | Low |
| Use case | Read-heavy | Insert/delete-heavy | Legacy threaded code |

---

## Quick Reference Table

| Method | Description |
|---|---|
| `add(e)` | Append element at end |
| `add(index, e)` | Insert element at index |
| `addAll(c)` | Append all from collection |
| `addAll(index, c)` | Insert all at index |
| `get(index)` | Retrieve element at index |
| `indexOf(o)` | First index of element |
| `lastIndexOf(o)` | Last index of element |
| `contains(o)` | Check element existence |
| `size()` | Number of elements |
| `isEmpty()` | Check if empty |
| `set(index, e)` | Replace element at index |
| `replaceAll(fn)` | Replace all with operator |
| `remove(index)` | Remove by index |
| `remove(o)` | Remove first occurrence |
| `removeAll(c)` | Remove all in collection |
| `removeIf(fn)` | Remove matching predicate |
| `retainAll(c)` | Keep only elements in collection |
| `clear()` | Remove all elements |
| `sort(comparator)` | Sort the list |
| `forEach(fn)` | Iterate all elements |
| `iterator()` | Get iterator |
| `listIterator()` | Get bidirectional iterator |
| `subList(from, to)` | Get sub-list view |
| `toArray()` | Convert to Object array |
| `toArray(T[])` | Convert to typed array |
| `ensureCapacity(n)` | Pre-allocate capacity |
| `trimToSize()` | Reduce capacity to size |
| `stream()` | Get sequential stream |
| `parallelStream()` | Get parallel stream |


