# LinkedList Methods in Java

## Overview

`LinkedList<E>` is part of the `java.util` package and implements both the `List<E>` and `Deque<E>` interfaces. It is a **doubly-linked list** where each element (node) holds a reference to both its previous and next node. It allows duplicate elements and `null` values and maintains **insertion order**. It is **not thread-safe**.

**Key characteristics:**
- Backed by a doubly-linked list
- Implements both `List` and `Deque` — can be used as a **list**, **stack**, **queue**, or **deque**
- Allows duplicates and `null` values
- Maintains insertion order
- Fast insertions/deletions at head or tail — O(1)
- Slow random access — O(n) (no index-based backing array)
- Higher memory overhead than `ArrayList` (each node stores two pointers)
- Not thread-safe (use `Collections.synchronizedList()` for concurrency)

```java
import java.util.LinkedList;

LinkedList<String> list = new LinkedList<>();

// From existing collection
LinkedList<String> fromCollection = new LinkedList<>(List.of("apple", "banana", "cherry"));
```

---

## Adding Elements

---

### 1. `add(E element)`
Appends the specified element to the **end** of the list. Equivalent to `addLast()`. Always returns `true`.

```java
LinkedList<String> list = new LinkedList<>();
list.add("apple");
list.add("banana");
list.add("cherry");
// [apple, banana, cherry]
```

---

### 2. `add(int index, E element)`
Inserts the element at the specified index, shifting existing elements to the right. Throws `IndexOutOfBoundsException` for invalid index.

```java
list.add(1, "avocado");
// [apple, avocado, banana, cherry]
```

---

### 3. `addFirst(E element)`
Inserts the element at the **beginning** of the list. O(1) operation.

```java
list.addFirst("apricot");
// [apricot, apple, avocado, banana, cherry]
```

---

### 4. `addLast(E element)`
Appends the element at the **end** of the list. O(1) operation. Equivalent to `add(e)`.

```java
list.addLast("date");
// [apricot, apple, avocado, banana, cherry, date]
```

---

### 5. `addAll(Collection<? extends E> c)`
Appends all elements of the specified collection to the end of the list, in iteration order.

```java
List<String> more = List.of("elderberry", "fig");
list.addAll(more);
// [..., elderberry, fig]
```

---

### 6. `addAll(int index, Collection<? extends E> c)`
Inserts all elements of the specified collection at the given index, shifting existing elements to the right.

```java
List<String> insert = List.of("grape", "honeydew");
list.addAll(2, insert);
```

---

### 7. `offer(E element)`
Adds the element to the **end** of the list (Queue operation). Returns `true`. Equivalent to `add(e)`.

```java
list.offer("kiwi");
```

---

### 8. `offerFirst(E element)`
Inserts the element at the **front** of the list (Deque operation). Returns `true`. Equivalent to `addFirst(e)`.

```java
list.offerFirst("lemon");
// [lemon, ...]
```

---

### 9. `offerLast(E element)`
Inserts the element at the **end** of the list (Deque operation). Returns `true`. Equivalent to `addLast(e)`.

```java
list.offerLast("mango");
```

---

### 10. `push(E element)`
Pushes the element onto the **stack** represented by this list (Stack operation). Inserts at the front. Equivalent to `addFirst(e)`.

```java
LinkedList<String> stack = new LinkedList<>();
stack.push("first");
stack.push("second");
stack.push("third");
// [third, second, first]
```

---

## Accessing Elements

---

### 11. `get(int index)`
Returns the element at the specified index. O(n) operation — traverses from head or tail depending on index. Throws `IndexOutOfBoundsException` for invalid index.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
String fruit = list.get(1); // "banana"
```

---

### 12. `getFirst()`
Returns the **first** element without removing it. Throws `NoSuchElementException` if list is empty.

```java
String first = list.getFirst(); // "apple"
```

---

### 13. `getLast()`
Returns the **last** element without removing it. Throws `NoSuchElementException` if list is empty.

```java
String last = list.getLast(); // "cherry"
```

---

### 14. `peek()`
Retrieves but does not remove the **first** element (Queue operation). Returns `null` if list is empty (unlike `getFirst()` which throws).

```java
String head = list.peek(); // "apple"
```

---

### 15. `peekFirst()`
Retrieves but does not remove the **first** element (Deque operation). Returns `null` if empty.

```java
String head = list.peekFirst(); // "apple"
```

---

### 16. `peekLast()`
Retrieves but does not remove the **last** element (Deque operation). Returns `null` if empty.

```java
String tail = list.peekLast(); // "cherry"
```

---

### 17. `element()`
Retrieves but does not remove the **first** element. Throws `NoSuchElementException` if empty. Equivalent to `getFirst()`.

```java
String head = list.element(); // "apple"
```

---

### 18. `indexOf(Object o)`
Returns the index of the **first occurrence** of the specified element, or `-1` if not found.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "apple"));
int idx = list.indexOf("apple"); // 0
```

---

### 19. `lastIndexOf(Object o)`
Returns the index of the **last occurrence** of the specified element, or `-1` if not found.

```java
int idx = list.lastIndexOf("apple"); // 2
```

---

### 20. `contains(Object o)`
Returns `true` if the list contains the specified element. Uses `.equals()` for comparison.

```java
list.contains("banana"); // true
list.contains("mango");  // false
```

---

### 21. `size()`
Returns the number of elements in the list.

```java
list.size(); // 3
```

---

### 22. `isEmpty()`
Returns `true` if the list contains no elements.

```java
list.isEmpty(); // false
```

---

## Updating Elements

---

### 23. `set(int index, E element)`
Replaces the element at the specified index with the given element. Returns the old element. O(n) traversal.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
String old = list.set(1, "blueberry"); // old = "banana"
// [apple, blueberry, cherry]
```

---

### 24. `replaceAll(UnaryOperator<E> operator)`
Replaces each element with the result of applying the given operator. Modifies the list in-place.

```java
list.replaceAll(String::toUpperCase);
// [APPLE, BLUEBERRY, CHERRY]
```

---

## Removing Elements

---

### 25. `remove()`
Removes and returns the **first** element (Queue operation). Throws `NoSuchElementException` if empty.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
String removed = list.remove(); // "apple"
// [banana, cherry]
```

---

### 26. `remove(int index)`
Removes the element at the specified index, shifting subsequent elements left. Returns the removed element. O(n) traversal.

```java
String removed = list.remove(1); // "banana"
// [cherry]
```

---

### 27. `remove(Object o)`
Removes the **first occurrence** of the specified element. Returns `true` if found and removed.

```java
list.remove("cherry"); // true
```

---

### 28. `removeFirst()`
Removes and returns the **first** element. O(1) operation. Throws `NoSuchElementException` if empty.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
String first = list.removeFirst(); // "apple"
// [banana, cherry]
```

---

### 29. `removeLast()`
Removes and returns the **last** element. O(1) operation. Throws `NoSuchElementException` if empty.

```java
String last = list.removeLast(); // "cherry"
// [banana]
```

---

### 30. `removeFirstOccurrence(Object o)`
Removes the **first occurrence** of the specified element (Deque operation). Returns `true` if found. Equivalent to `remove(Object o)`.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "apple"));
list.removeFirstOccurrence("apple"); // true
// [banana, apple]
```

---

### 31. `removeLastOccurrence(Object o)`
Removes the **last occurrence** of the specified element (Deque operation). Returns `true` if found.

```java
list.removeLastOccurrence("apple"); // true
// [banana]
```

---

### 32. `poll()`
Retrieves and removes the **first** element (Queue operation). Returns `null` if empty (safer than `remove()`).

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
String head = list.poll(); // "apple"
// [banana, cherry]
```

---

### 33. `pollFirst()`
Retrieves and removes the **first** element (Deque operation). Returns `null` if empty.

```java
String first = list.pollFirst(); // "banana"
```

---

### 34. `pollLast()`
Retrieves and removes the **last** element (Deque operation). Returns `null` if empty.

```java
String last = list.pollLast(); // "cherry"
```

---

### 35. `pop()`
Pops and returns the element from the **stack** represented by this list (Stack operation). Removes from the front. Throws `NoSuchElementException` if empty. Equivalent to `removeFirst()`.

```java
LinkedList<String> stack = new LinkedList<>(List.of("third", "second", "first"));
String top = stack.pop(); // "third"
```

---

### 36. `removeAll(Collection<?> c)`
Removes all elements in the list that are also contained in the specified collection.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry", "date"));
list.removeAll(List.of("banana", "date"));
// [apple, cherry]
```

---

### 37. `removeIf(Predicate<? super E> filter)`
Removes all elements that satisfy the given predicate. Returns `true` if any elements were removed.

```java
LinkedList<Integer> numbers = new LinkedList<>(List.of(1, 2, 3, 4, 5, 6));
numbers.removeIf(n -> n % 2 == 0);
// [1, 3, 5]
```

---

### 38. `retainAll(Collection<?> c)`
Retains only the elements contained in the specified collection, removing all others.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry", "date"));
list.retainAll(List.of("banana", "cherry"));
// [banana, cherry]
```

---

### 39. `clear()`
Removes all elements from the list.

```java
list.clear();
list.isEmpty(); // true
```

---

## Iterating

---

### 40. `forEach(Consumer<? super E> action)`
Performs the given action for each element in iteration order (head to tail).

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
list.forEach(fruit -> System.out.println(fruit));
```

---

### 41. `iterator()`
Returns a fail-fast `Iterator` over the elements from head to tail.

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

### 42. `listIterator(int index)`
Returns a `ListIterator` starting at the given index. Supports bidirectional traversal and in-place modification.

```java
ListIterator<String> lit = list.listIterator(list.size()); // start from end
while (lit.hasPrevious()) {
    System.out.println(lit.previous()); // reverse traversal
}
```

---

### 43. `descendingIterator()`
Returns an iterator over the elements in **reverse order** (tail to head). Unique to `Deque`.

```java
Iterator<String> descIt = list.descendingIterator();
while (descIt.hasNext()) {
    System.out.println(descIt.next()); // cherry, banana, apple
}
```

---

## Conversion

---

### 44. `toArray()`
Returns an `Object[]` array containing all elements in proper sequence (head to tail).

```java
Object[] arr = list.toArray();
```

---

### 45. `toArray(T[] a)`
Returns a typed array containing all elements. Preferred for type safety.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
String[] arr = list.toArray(new String[0]);
// ["apple", "banana", "cherry"]
```

---

### 46. `subList(int fromIndex, int toIndex)`
Returns a **view** of the portion of the list from `fromIndex` (inclusive) to `toIndex` (exclusive). Changes reflect in the original.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry", "date"));
List<String> sub = list.subList(1, 3);
// [banana, cherry]
```

---

## Stream and Functional Operations

---

### 47. `stream()`
Returns a sequential `Stream` over the elements for functional-style operations.

```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry", "avocado"));

List<String> aFruits = list.stream()
    .filter(s -> s.startsWith("a"))
    .collect(Collectors.toList());
// [apple, avocado]
```

---

### 48. `parallelStream()`
Returns a parallel `Stream` for concurrent processing using the Fork/Join pool.

```java
long count = list.parallelStream()
    .filter(s -> s.length() > 5)
    .count();
```

---

## LinkedList as Stack, Queue, and Deque

One of `LinkedList`'s most powerful traits is its ability to function as multiple data structures:

### As a Queue (FIFO)
```java
LinkedList<String> queue = new LinkedList<>();
queue.offer("first");   // enqueue
queue.offer("second");
queue.offer("third");

String head = queue.poll();  // dequeue -> "first"
String peek = queue.peek();  // peek    -> "second"
```

### As a Stack (LIFO)
```java
LinkedList<String> stack = new LinkedList<>();
stack.push("first");   // push
stack.push("second");
stack.push("third");

String top = stack.pop();   // pop  -> "third"
String peek = stack.peek(); // peek -> "second"
```

### As a Deque (Double-Ended Queue)
```java
LinkedList<String> deque = new LinkedList<>();
deque.addFirst("middle");
deque.addFirst("front");   // insert at front
deque.addLast("end");      // insert at back
// [front, middle, end]

deque.pollFirst(); // remove from front -> "front"
deque.pollLast();  // remove from back  -> "end"
```

---

## Common Patterns

### Reverse a LinkedList
```java
LinkedList<String> list = new LinkedList<>(List.of("apple", "banana", "cherry"));
Collections.reverse(list);
// [cherry, banana, apple]
```

### Sort a LinkedList
```java
list.sort(Comparator.naturalOrder());
// [apple, banana, cherry]

list.sort(Comparator.reverseOrder());
// [cherry, banana, apple]
```

### Convert LinkedList to ArrayList
```java
ArrayList<String> arrayList = new ArrayList<>(list);
```

### Thread-Safe LinkedList
```java
List<String> syncList = Collections.synchronizedList(new LinkedList<>());
```

---

## Time Complexity Summary

| Operation | Time Complexity |
|---|---|
| `addFirst(e)` / `addLast(e)` | O(1) |
| `removeFirst()` / `removeLast()` | O(1) |
| `get(index)` | O(n) |
| `set(index, e)` | O(n) |
| `add(index, e)` | O(n) |
| `remove(index)` | O(n) |
| `remove(object)` | O(n) |
| `contains(o)` | O(n) |
| `indexOf(o)` | O(n) |
| `size()` | O(1) |
| `peek()` / `poll()` | O(1) |
| `push()` / `pop()` | O(1) |

---

## Comparison: ArrayList vs LinkedList

| Feature | `ArrayList` | `LinkedList` |
|---|---|---|
| Internal structure | Dynamic array | Doubly linked list |
| Random access `get(i)` | O(1) | O(n) |
| Insert / delete at head | O(n) | O(1) |
| Insert / delete at tail | O(1) amortized | O(1) |
| Insert / delete in middle | O(n) | O(n) (traversal) + O(1) (link) |
| Memory overhead | Low | High (prev + next pointers per node) |
| Implements `Deque` | No | Yes |
| Use case | Read-heavy / random access | Frequent insert/delete at ends, Queue/Stack/Deque |

---

## Quick Reference Table

| Method | Category | Description |
|---|---|---|
| `add(e)` | Add | Append at end |
| `add(index, e)` | Add | Insert at index |
| `addFirst(e)` | Add | Insert at front — O(1) |
| `addLast(e)` | Add | Append at end — O(1) |
| `addAll(c)` | Add | Append all from collection |
| `offer(e)` | Queue | Enqueue at end |
| `offerFirst(e)` | Deque | Insert at front |
| `offerLast(e)` | Deque | Insert at end |
| `push(e)` | Stack | Push onto stack (front) |
| `get(index)` | Access | Retrieve by index — O(n) |
| `getFirst()` | Access | First element (throws if empty) |
| `getLast()` | Access | Last element (throws if empty) |
| `peek()` | Queue | First element or null |
| `peekFirst()` | Deque | First element or null |
| `peekLast()` | Deque | Last element or null |
| `element()` | Queue | First element (throws if empty) |
| `indexOf(o)` | Search | First index of element |
| `lastIndexOf(o)` | Search | Last index of element |
| `contains(o)` | Search | Check existence |
| `size()` | Info | Number of elements |
| `isEmpty()` | Info | Check if empty |
| `set(index, e)` | Update | Replace at index |
| `replaceAll(fn)` | Update | Apply operator to all |
| `remove()` | Remove | Remove first (throws if empty) |
| `remove(index)` | Remove | Remove at index |
| `remove(o)` | Remove | Remove first occurrence |
| `removeFirst()` | Remove | Remove first — O(1) |
| `removeLast()` | Remove | Remove last — O(1) |
| `removeFirstOccurrence(o)` | Remove | Remove first occurrence |
| `removeLastOccurrence(o)` | Remove | Remove last occurrence |
| `poll()` | Queue | Dequeue first or null |
| `pollFirst()` | Deque | Remove first or null |
| `pollLast()` | Deque | Remove last or null |
| `pop()` | Stack | Pop from stack (front) |
| `removeAll(c)` | Remove | Remove all in collection |
| `removeIf(fn)` | Remove | Remove matching predicate |
| `retainAll(c)` | Remove | Keep only in collection |
| `clear()` | Remove | Remove all elements |
| `forEach(fn)` | Iterate | Iterate all elements |
| `iterator()` | Iterate | Forward iterator |
| `listIterator(index)` | Iterate | Bidirectional iterator |
| `descendingIterator()` | Iterate | Reverse iterator |
| `toArray()` | Convert | Convert to Object array |
| `toArray(T[])` | Convert | Convert to typed array |
| `subList(from, to)` | Convert | Sub-list view |
| `stream()` | Stream | Sequential stream |
| `parallelStream()` | Stream | Parallel stream |