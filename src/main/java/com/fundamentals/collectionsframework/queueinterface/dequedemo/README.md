## Deque (Double Ended Queue) Important Methods in Java

> `Deque` is an interface in Java (`java.util.Deque`), commonly implemented by `ArrayDeque` and `LinkedList`.
> It supports insertion and removal from **both ends**.
```java
Deque<String> deque = new ArrayDeque<>();
Deque<String> deque = new LinkedList<>();
```

---

### 1. Insertion Methods

| Method | Description | End | Throws Exception? |
|--------|-------------|-----|-------------------|
| `addFirst(e)` | Inserts at front | Head | Yes (`IllegalStateException`) |
| `addLast(e)` | Inserts at tail | Tail | Yes (`IllegalStateException`) |
| `offerFirst(e)` | Inserts at front | Head | No (returns `false`) |
| `offerLast(e)` | Inserts at tail | Tail | No (returns `false`) |
| `push(e)` | Inserts at front (stack) | Head | Yes (`IllegalStateException`) |
| `add(e)` | Inserts at tail (queue) | Tail | Yes (`IllegalStateException`) |
| `offer(e)` | Inserts at tail (queue) | Tail | No (returns `false`) |
```java
deque.addFirst("apple");      // [apple]
deque.addLast("banana");      // [apple, banana]
deque.offerFirst("mango");    // [mango, apple, banana]
deque.offerLast("grape");     // [mango, apple, banana, grape]
deque.push("cherry");         // [cherry, mango, apple, banana, grape]
```

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | End | Throws Exception? |
|--------|-------------|-----|-------------------|
| `peekFirst()` | Returns front element | Head | No (returns `null`) |
| `peekLast()` | Returns last element | Tail | No (returns `null`) |
| `getFirst()` | Returns front element | Head | Yes (`NoSuchElementException`) |
| `getLast()` | Returns last element | Tail | Yes (`NoSuchElementException`) |
| `peek()` | Returns front element (queue) | Head | No (returns `null`) |
| `element()` | Returns front element (queue) | Head | Yes (`NoSuchElementException`) |
```java
deque.peekFirst();   // "cherry" — null if empty
deque.peekLast();    // "grape"  — null if empty
deque.getFirst();    // "cherry" — throws if empty
deque.getLast();     // "grape"  — throws if empty
```

---

### 3. Removal Methods — *Removes the element*

| Method | Description | End | Throws Exception? |
|--------|-------------|-----|-------------------|
| `pollFirst()` | Removes & returns front | Head | No (returns `null`) |
| `pollLast()` | Removes & returns last | Tail | No (returns `null`) |
| `removeFirst()` | Removes & returns front | Head | Yes (`NoSuchElementException`) |
| `removeLast()` | Removes & returns last | Tail | Yes (`NoSuchElementException`) |
| `pop()` | Removes & returns front (stack) | Head | Yes (`NoSuchElementException`) |
| `poll()` | Removes & returns front (queue) | Head | No (returns `null`) |
| `remove()` | Removes & returns front (queue) | Head | Yes (`NoSuchElementException`) |
| `remove(obj)` | Removes specific element | Any | No (returns `true/false`) |
| `removeFirstOccurrence(obj)` | Removes first match | Head→Tail | No (returns `true/false`) |
| `removeLastOccurrence(obj)` | Removes last match | Tail→Head | No (returns `true/false`) |
```java
deque.pollFirst();                    // removes & returns "cherry"
deque.pollLast();                     // removes & returns "grape"
deque.removeFirst();                  // removes & returns front — throws if empty
deque.removeLast();                   // removes & returns last  — throws if empty
deque.pop();                          // removes & returns front — throws if empty
deque.removeFirstOccurrence("apple"); // removes first "apple"
deque.removeLastOccurrence("apple");  // removes last "apple"
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `isEmpty()` | Returns `true` if deque is empty |
| `size()` | Returns number of elements |
```java
deque.contains("banana"); // true
deque.isEmpty();          // false
deque.size();             // 3
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` |
| `toArray(T[])` | Converts to typed array |
| `iterator()` | Iterates front to back |
| `descendingIterator()` | Iterates back to front |
| `forEach(action)` | Iterates with lambda |
| `clear()` | Removes all elements |
```java
deque.toArray();
deque.toArray(new String[0]);
deque.forEach(s -> System.out.println(s));

// Reverse iteration
Iterator<String> it = deque.descendingIterator();
while (it.hasNext()) {
    System.out.println(it.next());
}

deque.clear();
```

---

### 6. Drain Methods
```java
// Front to back
while (!deque.isEmpty()) {
    System.out.println(deque.pollFirst());
}

// Back to front
while (!deque.isEmpty()) {
    System.out.println(deque.pollLast());
}
```

---

### 7. Deque as Stack vs Queue

> `Deque` can replace both `Stack` and `Queue` in Java.

| Role | Push / Enqueue | Pop / Dequeue | Peek |
|------|---------------|---------------|------|
| **Stack** (LIFO) | `push(e)` / `addFirst(e)` | `pop()` / `removeFirst()` | `peek()` / `peekFirst()` |
| **Queue** (FIFO) | `offer(e)` / `addLast(e)` | `poll()` / `removeFirst()` | `peek()` / `peekFirst()` |
```java
// Used as Stack
Deque<String> stack = new ArrayDeque<>();
stack.push("a");
stack.push("b");
stack.pop();   // "b" — LIFO

// Used as Queue
Deque<String> queue = new ArrayDeque<>();
queue.offer("a");
queue.offer("b");
queue.poll();  // "a" — FIFO
```

---

### Quick Comparison — Exception vs No Exception

| Operation | Throws Exception | No Exception (safe) |
|-----------|-----------------|----------------------|
| Insert Front | `addFirst(e)` | `offerFirst(e)` |
| Insert Tail | `addLast(e)` | `offerLast(e)` |
| Peek Front | `getFirst()` | `peekFirst()` |
| Peek Tail | `getLast()` | `peekLast()` |
| Remove Front | `removeFirst()` | `pollFirst()` |
| Remove Tail | `removeLast()` | `pollLast()` |

> **Best Practice:** Prefer `offerFirst()`, `offerLast()`, `peekFirst()`, `peekLast()`, `pollFirst()`, `pollLast()` to avoid unexpected exceptions.