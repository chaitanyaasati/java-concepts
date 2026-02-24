## ConcurrentLinkedQueue Important Methods in Java

> `ConcurrentLinkedQueue` is a class in Java (`java.util.concurrent.ConcurrentLinkedQueue`).
> It is an **unbounded thread-safe queue** based on **linked nodes**.
> It follows **FIFO** (First In First Out) ordering.
> It uses a **lock-free** algorithm (CAS - Compare And Swap) for thread safety.
> It **never blocks** on insertion or retrieval.
```java
import java.util.concurrent.ConcurrentLinkedQueue;

ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>(existingCollection);
```

---

### 1. Insertion Methods

> `ConcurrentLinkedQueue` is **unbounded**, so insertion **never blocks or fails**.

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `add(e)` | Inserts element at tail | No (returns `true` always) |
| `offer(e)` | Inserts element at tail | No (returns `true` always) |
| `addAll(collection)` | Inserts all elements from collection | Yes (`NullPointerException` if null) |
```java
queue.add("apple");                                      // always succeeds
queue.offer("banana");                                   // preferred — always succeeds
queue.addAll(List.of("cherry", "mango", "grape"));       // adds all elements
```

> **Note:** Both `add()` and `offer()` behave identically since the queue is unbounded.
> `null` elements are **not allowed** and will throw `NullPointerException`.

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `peek()` | Returns head element | No (returns `null` if empty) |
| `element()` | Returns head element | Yes (`NoSuchElementException` if empty) |
```java
queue.peek();     // returns "apple" — null if empty
queue.element();  // returns "apple" — throws if empty
```

---

### 3. Removal Methods — *Removes the element*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `poll()` | Removes & returns head | No (returns `null` if empty) |
| `remove()` | Removes & returns head | Yes (`NoSuchElementException` if empty) |
| `remove(obj)` | Removes first occurrence of element | No (returns `true/false`) |
| `removeAll(collection)` | Removes all elements in collection | No (returns `true/false`) |
| `retainAll(collection)` | Retains only elements in collection | No (returns `true/false`) |
```java
queue.poll();                                    // removes & returns "apple" — null if empty
queue.remove();                                  // removes & returns head  — throws if empty
queue.remove("banana");                          // removes first "banana"
queue.removeAll(List.of("cherry", "mango"));     // removes all specified elements
queue.retainAll(List.of("grape"));               // keeps only "grape"
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `containsAll(collection)` | Returns `true` if all elements exist |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns number of elements |
```java
queue.contains("apple");                       // true
queue.containsAll(List.of("apple", "banana")); // true
queue.isEmpty();                               // false
queue.size();                                  // 3 — use with caution (O(n) operation)
```

> **Warning:** `size()` is an **O(n)** operation in `ConcurrentLinkedQueue`.
> Avoid calling it frequently in performance-critical code.
> Use `isEmpty()` instead to check if the queue has elements.

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` |
| `toArray(T[])` | Converts to typed array |
| `iterator()` | Returns weakly consistent iterator (FIFO order) |
| `forEach(action)` | Iterates with lambda |
| `spliterator()` | Returns spliterator for parallel processing |
| `clear()` | Removes all elements |
```java
queue.toArray();
queue.toArray(new String[0]);
queue.forEach(s -> System.out.println(s));

// Iterator — weakly consistent (won't throw ConcurrentModificationException)
Iterator<String> it = queue.iterator();
while (it.hasNext()) {
    System.out.println(it.next());
}

// Spliterator for parallel streams
queue.spliterator();

queue.clear();
```

> **Note:** The iterator is **weakly consistent** — it may or may not reflect
> modifications made after the iterator was created, but it will never throw
> `ConcurrentModificationException`.

---

### 6. Stream Methods
```java
// Sequential stream
queue.stream()
     .filter(s -> s.startsWith("a"))
     .forEach(System.out::println);

// Parallel stream — thread-safe
queue.parallelStream()
     .filter(s -> s.length() > 3)
     .forEach(System.out::println);
```

---

### 7. Drain / Processing Methods
```java
// Non-blocking — process all available elements
String element;
while ((element = queue.poll()) != null) {
    System.out.println("Processed: " + element);
}

// Process with multiple consumer threads safely
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 4; i++) {
    executor.submit(() -> {
        String e;
        while ((e = queue.poll()) != null) {
            System.out.println(Thread.currentThread().getName() + " processed: " + e);
        }
    });
}
executor.shutdown();
```

---

### 8. Producer-Consumer Pattern
```java
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

// Multiple Producer Threads
Runnable producer = () -> {
    for (int i = 0; i < 5; i++) {
        String task = Thread.currentThread().getName() + "-Task" + i;
        queue.offer(task);
        System.out.println("Produced: " + task);
    }
};

// Multiple Consumer Threads
Runnable consumer = () -> {
    while (true) {
        String task = queue.poll();
        if (task != null) {
            System.out.println(Thread.currentThread().getName() + " consumed: " + task);
        }
    }
};

// Start producers and consumers
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(producer);
executor.submit(producer);
executor.submit(consumer);
executor.submit(consumer);
executor.shutdown();
```

---

### 9. Common Use Cases

| Use Case | Description |
|----------|-------------|
| Work Queue | Distribute tasks across multiple threads |
| Event Queue | Process events in order without blocking |
| Log Collection | Collect logs from multiple threads safely |
| Message Passing | Pass messages between threads |
| Concurrent Pipeline | Build lock-free processing pipelines |
```java
// Example: Multi-threaded log collection
ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();

// Multiple threads safely adding logs
Runnable logger = () -> {
    for (int i = 0; i < 3; i++) {
        logQueue.offer("[" + Thread.currentThread().getName() + "] Log entry " + i);
    }
};

ExecutorService executor = Executors.newFixedThreadPool(3);
executor.submit(logger);
executor.submit(logger);
executor.submit(logger);
executor.shutdown();
executor.awaitTermination(1, TimeUnit.SECONDS);

// Process all logs
logQueue.forEach(System.out::println);
```

---

### 10. Comparison with Other Concurrent Queues

| Feature | `ConcurrentLinkedQueue` | `LinkedBlockingQueue` | `ArrayBlockingQueue` | `PriorityBlockingQueue` |
|---------|------------------------|-----------------------|---------------------|------------------------|
| Backed by | Linked Nodes | Linked Nodes | Array | Heap |
| Bounded | No (unbounded) | Optional | Yes (fixed) | No (unbounded) |
| Blocking | Never | Yes | Yes | On retrieval only |
| Thread Safety | Lock-free (CAS) | Locks | Locks | Locks |
| Ordering | FIFO | FIFO | FIFO | Priority |
| `size()` cost | O(n) | O(1) | O(1) | O(1) |
| Null Elements | Not allowed | Not allowed | Not allowed | Not allowed |
| Best For | High throughput non-blocking | Producer-Consumer | Bounded tasks | Priority tasks |

---

### Quick Comparison — Exception vs No Exception

| Operation | Throws Exception | No Exception (safe) |
|-----------|-----------------|----------------------|
| Insert | `add(e)` | `offer(e)` |
| Peek | `element()` | `peek()` |
| Remove Head | `remove()` | `poll()` |

---

> **Important Notes:**
> - `null` elements are **strictly not allowed**.
> - `size()` is **O(n)** — avoid in performance-critical code, use `isEmpty()` instead.
> - Iterator is **weakly consistent** — safe for concurrent use, won't throw `ConcurrentModificationException`.
> - Uses **lock-free CAS** algorithm — better throughput than lock-based queues under high contention.
> - Does **not block** — use `LinkedBlockingQueue` if blocking behavior is needed.
> - Prefer `ConcurrentLinkedQueue` for **high-throughput, non-blocking, multi-threaded** scenarios.