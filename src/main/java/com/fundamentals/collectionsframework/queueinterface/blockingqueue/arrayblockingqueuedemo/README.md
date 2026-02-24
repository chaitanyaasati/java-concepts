## ArrayBlockingQueue Important Methods in Java

> `ArrayBlockingQueue` is a class in Java (`java.util.concurrent.ArrayBlockingQueue`).
> It is a **bounded blocking queue** backed by an **array**.
> It follows **FIFO** (First In First Out) ordering.
> It blocks when the queue is **full** (on insert) or **empty** (on retrieve).
```java
import java.util.concurrent.ArrayBlockingQueue;

ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);          // capacity 10
ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10, true);    // fair ordering
```

> **Fair Ordering:** When `true`, threads waiting longest get priority access (lower throughput but prevents starvation).

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

// Blocking — process elements as they arrive (Producer-Consumer)
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

> `ArrayBlockingQueue` is most commonly used in **Producer-Consumer** problems.
```java
ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

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

### 8. Comparison with Other Blocking Queues

| Feature | `ArrayBlockingQueue` | `LinkedBlockingQueue` | `PriorityBlockingQueue` |
|---------|---------------------|-----------------------|-------------------------|
| Backed by | Array | LinkedList | Heap |
| Bounded | Yes (fixed) | Optional | No (unbounded) |
| Ordering | FIFO | FIFO | Priority |
| Fair Mode | Yes | Yes | No |
| Memory | Less (array) | More (nodes) | More (heap) |
| Performance | High (fixed size) | High (flexible) | Medium |

---

### Quick Comparison — Insertion & Removal Methods

| Operation | Throws Exception | Returns Value | Blocks (timeout) | Blocks (indefinitely) |
|-----------|-----------------|---------------|-------------------|-----------------------|
| Insert | `add(e)` | `offer(e)` | `offer(e, t, u)` | `put(e)` |
| Peek | `element()` | `peek()` | — | — |
| Remove | `remove()` | `poll()` | `poll(t, u)` | `take()` |

> **Best Practice:**
> - Use `put()` and `take()` for blocking producer-consumer patterns.
> - Use `offer()` and `poll()` for non-blocking with timeout.
> - Always handle `InterruptedException` properly in threaded environments.
> - Prefer `ArrayBlockingQueue` over `LinkedBlockingQueue` when memory usage and throughput are critical.