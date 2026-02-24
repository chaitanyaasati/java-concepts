## LinkedBlockingQueue Important Methods in Java

> `LinkedBlockingQueue` is a class in Java (`java.util.concurrent.LinkedBlockingQueue`).
> It is an **optionally bounded blocking queue** backed by **linked nodes**.
> It follows **FIFO** (First In First Out) ordering.
> It blocks when the queue is **full** (on insert) or **empty** (on retrieve).
> Default capacity is `Integer.MAX_VALUE` (effectively unbounded).
```java
import java.util.concurrent.LinkedBlockingQueue;

LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();         // unbounded
LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(10);      // bounded capacity 10
```

---

### 1. Insertion Methods

| Method | Description | Blocks? | Throws Exception? |
|--------|-------------|---------|-------------------|
| `add(e)` | Inserts element | No | Yes (`IllegalStateException` if full) |
| `offer(e)` | Inserts element | No | No (returns `false` if full) |
| `offer(e, timeout, unit)` | Inserts, waits up to timeout if full | Yes (up to timeout) | Yes (`InterruptedException`) |
| `put(e)` | Inserts, blocks until space available | Yes (indefinitely) | Yes (`InterruptedException`) |
```java
queue.add("apple");                                   // throws if full
queue.offer("banana");                                // returns false if full — preferred
queue.offer("cherry", 2, TimeUnit.SECONDS);           // waits up to 2s if full
queue.put("mango");                                   // blocks until space available
```

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Blocks? | Throws Exception? |
|--------|-------------|---------|-------------------|
| `peek()` | Returns head element | No | No (returns `null` if empty) |
| `element()` | Returns head element | No | Yes (`NoSuchElementException` if empty) |
```java
queue.peek();     // returns head — null if empty
queue.element();  // returns head — throws if empty
```

---

### 3. Removal Methods — *Removes the element*

| Method | Description | Blocks? | Throws Exception? |
|--------|-------------|---------|-------------------|
| `poll()` | Removes & returns head | No | No (returns `null` if empty) |
| `poll(timeout, unit)` | Removes & returns head, waits if empty | Yes (up to timeout) | Yes (`InterruptedException`) |
| `take()` | Removes & returns head, blocks until available | Yes (indefinitely) | Yes (`InterruptedException`) |
| `remove()` | Removes & returns head | No | Yes (`NoSuchElementException` if empty) |
| `remove(obj)` | Removes specific element | No | No (returns `true/false`) |
```java
queue.poll();                                // null if empty
queue.poll(2, TimeUnit.SECONDS);             // waits up to 2s if empty
queue.take();                                // blocks until element available
queue.remove();                              // throws if empty
queue.remove("banana");                      // removes specific element
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns number of elements |
| `remainingCapacity()` | Returns remaining space available |
```java
queue.contains("apple");      // true
queue.isEmpty();              // false
queue.size();                 // 3
queue.remainingCapacity();    // 7 (if capacity is 10 and size is 3)
                              // Integer.MAX_VALUE if unbounded
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` |
| `toArray(T[])` | Converts to typed array |
| `iterator()` | Returns iterator (FIFO order) |
| `forEach(action)` | Iterates with lambda |
| `drainTo(collection)` | Moves all elements to another collection |
| `drainTo(collection, maxElements)` | Moves up to N elements to another collection |
| `clear()` | Removes all elements |
```java
queue.toArray();
queue.toArray(new String[0]);
queue.forEach(s -> System.out.println(s));

// Drain elements into a list
List<String> list = new ArrayList<>();
queue.drainTo(list);           // moves all elements
queue.drainTo(list, 5);        // moves up to 5 elements

queue.clear();
```

---

### 6. Drain / Processing Methods
```java
// Non-blocking — process available elements
String element;
while ((element = queue.poll()) != null) {
    System.out.println("Processed: " + element);
}

// Blocking — process elements as they arrive
while (true) {
    try {
        String e = queue.take(); // blocks until element available
        System.out.println("Processed: " + e);
    } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        break;
    }
}
```

---

### 7. Producer-Consumer Pattern

> `LinkedBlockingQueue` is most commonly used in **Producer-Consumer** problems.
```java
LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(5);

// Producer Thread
Thread producer = new Thread(() -> {
    try {
        String[] tasks = {"Task1", "Task2", "Task3", "Task4", "Task5", "Task6"};
        for (String task : tasks) {
            queue.put(task);                          // blocks if queue is full
            System.out.println("Produced: " + task);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

// Consumer Thread
Thread consumer = new Thread(() -> {
    try {
        while (true) {
            String task = queue.take();               // blocks if queue is empty
            System.out.println("Consumed: " + task);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

producer.start();
consumer.start();
```

---

### 8. Thread Pool Usage

> `LinkedBlockingQueue` is used internally by `ThreadPoolExecutor` as a task queue.
```java
import java.util.concurrent.*;

// ThreadPoolExecutor with LinkedBlockingQueue
ExecutorService executor = new ThreadPoolExecutor(
    2,                                      // core pool size
    4,                                      // max pool size
    60, TimeUnit.SECONDS,                   // keep alive time
    new LinkedBlockingQueue<>(100)          // task queue with capacity 100
);

executor.submit(() -> System.out.println("Task executed by: " + Thread.currentThread().getName()));
executor.shutdown();
```

---

### 9. Comparison with Other Blocking Queues

| Feature | `LinkedBlockingQueue` | `ArrayBlockingQueue` | `PriorityBlockingQueue` | `DelayQueue` |
|---------|-----------------------|---------------------|-------------------------|--------------|
| Backed by | Linked Nodes | Array | Heap | Heap |
| Bounded | Optional | Yes (fixed) | No (unbounded) | No (unbounded) |
| Ordering | FIFO | FIFO | Priority | Delay expiry |
| Fair Mode | No | Yes | No | No |
| Memory | More (nodes) | Less (array) | More (heap) | More (heap) |
| Performance | High | High | Medium | Medium |
| Best For | Producer-Consumer | Fixed size tasks | Priority tasks | Scheduled tasks |

---

### Quick Comparison — Insertion & Removal Methods

| Operation | Throws Exception | Returns Value | Blocks (timeout) | Blocks (indefinitely) |
|-----------|-----------------|---------------|-------------------|-----------------------|
| Insert | `add(e)` | `offer(e)` | `offer(e, t, u)` | `put(e)` |
| Peek | `element()` | `peek()` | — | — |
| Remove | `remove()` | `poll()` | `poll(t, u)` | `take()` |

---

### Key Differences — LinkedBlockingQueue vs ArrayBlockingQueue

| Feature | `LinkedBlockingQueue` | `ArrayBlockingQueue` |
|---------|-----------------------|---------------------|
| Internal structure | Linked nodes (dynamic) | Fixed array (static) |
| Default capacity | `Integer.MAX_VALUE` | Must specify |
| Memory overhead | Higher (node objects) | Lower (primitive array) |
| Throughput | Higher (separate locks for head/tail) | Lower (single lock) |
| Fair scheduling | Not supported | Supported |
| Use case | High throughput pipelines | Bounded task queues |

> **Best Practice:**
> - Use `put()` and `take()` for blocking producer-consumer patterns.
> - Use `offer()` and `poll()` for non-blocking with timeout.
> - Prefer **bounded** `LinkedBlockingQueue` to avoid memory issues in production.
> - Use `LinkedBlockingQueue` over `ArrayBlockingQueue` when higher throughput is needed.
> - Always handle `InterruptedException` properly in threaded environments.