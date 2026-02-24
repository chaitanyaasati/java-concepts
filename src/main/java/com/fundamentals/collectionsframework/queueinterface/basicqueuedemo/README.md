## Queue Important Methods in Java

> `Queue` is an interface in Java (`java.util.Queue`), commonly implemented by `LinkedList` and `ArrayDeque`.
```java
Queue<String> queue = new LinkedList<>();
Queue<String> queue = new ArrayDeque<>();
```

---

### 1. Insertion Methods

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `add(e)` | Inserts element at tail | Yes (`IllegalStateException`) |
| `offer(e)` | Inserts element at tail | No (returns `false`) |
```java
queue.add("apple");
queue.offer("banana");   // preferred
```

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `peek()` | Returns head element | No (returns `null`) |
| `element()` | Returns head element | Yes (`NoSuchElementException`) |
```java
queue.peek();      // returns "apple" — null if empty
queue.element();   // returns "apple" — throws if empty
```

---

### 3. Removal Methods — *Removes the element*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `poll()` | Removes & returns head | No (returns `null`) |
| `remove()` | Removes & returns head | Yes (`NoSuchElementException`) |
| `remove(obj)` | Removes a specific element | No (returns `true/false`) |
```java
queue.poll();            // removes & returns "apple" — null if empty
queue.remove();          // removes & returns head — throws if empty
queue.remove("banana");  // removes specific element
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns number of elements |
```java
queue.contains("banana"); // true
queue.isEmpty();          // false
queue.size();             // 1
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` |
| `toArray(T[])` | Converts to typed array |
| `iterator()` | Returns iterator (FIFO order) |
| `forEach(action)` | Iterates with lambda |
| `clear()` | Removes all elements |
```java
queue.toArray();
queue.toArray(new String[0]);
queue.forEach(s -> System.out.println(s));
queue.clear();
```

---

### 6. Drain Methods
```java
// Correct way to process all elements in FIFO order
while (!queue.isEmpty()) {
    System.out.println(queue.poll());
}

// Stream (maintains insertion order for LinkedList)
queue.stream().forEach(System.out::println);
```

---

### 7. Deque Methods (if using ArrayDeque)

> `ArrayDeque` implements `Deque` which extends `Queue`, giving extra methods:

| Method | Description |
|--------|-------------|
| `addFirst(e)` | Inserts at front |
| `addLast(e)` | Inserts at tail |
| `peekFirst()` | Returns front element |
| `peekLast()` | Returns last element |
| `pollFirst()` | Removes & returns front |
| `pollLast()` | Removes & returns last |
```java
Deque<String> deque = new ArrayDeque<>();
deque.addFirst("apple");
deque.addLast("banana");
deque.peekFirst();   // "apple"
deque.peekLast();    // "banana"
deque.pollFirst();   // removes "apple"
deque.pollLast();    // removes "banana"
```

---

### Quick Comparison — Exception vs No Exception

| Operation | Throws Exception | No Exception (safe) |
|-----------|-----------------|----------------------|
| Insert | `add(e)` | `offer(e)` |
| Peek | `element()` | `peek()` |
| Remove | `remove()` | `poll()` |

> **Best Practice:** Always prefer `offer()`, `peek()`, and `poll()` to avoid unexpected exceptions.