## PriorityBlockingQueue Important Methods in Java

> `PriorityBlockingQueue` is a class in Java (`java.util.concurrent.PriorityBlockingQueue`).
> It is an **unbounded blocking queue** that orders elements based on
> **natural ordering** or a **custom comparator**.
> The **head** of the queue is always the **smallest** (min-heap by default).
> It **blocks only on retrieval** (when empty), never on insertion (unbounded).
```java
import java.util.concurrent.PriorityBlockingQueue;

PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>();                           // min-heap
PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>(10);                         // initial capacity hint
PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>(10, Comparator.reverseOrder()); // max-heap
PriorityBlockingQueue<String>  queue = new PriorityBlockingQueue<>(10, Comparator.comparingInt(String::length)); // custom
```

---

### Setting Up — Custom Object

> For custom objects, implement `Comparable` or provide a `Comparator`.
```java
class Task implements Comparable<Task> {
    private final String name;
    private final int priority;

    public Task(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority); // lower number = higher priority
    }

    @Override
    public String toString() { return name + "(" + priority + ")"; }
}

PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();
```

---

### 1. Insertion Methods

> `PriorityBlockingQueue` is **unbounded**, so insertion **never blocks**.

| Method | Description | Blocks? | Throws Exception? |
|--------|-------------|---------|-------------------|
| `add(e)` | Inserts element | No | Yes (`ClassCastException` if not comparable) |
| `offer(e)` | Inserts element | No | No (returns `true` always) |
| `offer(e, timeout, unit)` | Inserts element (timeout ignored) | No | No (returns `true` always) |
| `put(e)` | Inserts element (never blocks) | No | No |
```java
queue.add(new Task("Low Priority Task", 5));
queue.offer(new Task("High Priority Task", 1));    // preferred — always succeeds
queue.offer(new Task("Medium Task", 3), 2, TimeUnit.SECONDS); // timeout ignored
queue.put(new Task("Critical Task", 0));           // never blocks
```

> **Note:** Since the queue is unbounded, `offer()`, `put()`, and `offer(e, timeout, unit)`
> all behave identically — they never block or return `false`.

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Blocks? | Throws Exception? |
|--------|-------------|---------|-------------------|
| `peek()` | Returns head (highest priority) element | No | No (returns `null` if empty) |
| `element()` | Returns head element | No | Yes (`NoSuchElementException` if empty) |
```java
queue.peek();     // returns highest priority element — null if empty
queue.element();  // returns highest priority element — throws if empty
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
queue.remove(task);                          // removes specific element
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns number of elements |
| `remainingCapacity()` | Always returns `Integer.MAX_VALUE` (unbounded) |
```java
queue.contains(task);         // true
queue.isEmpty();              // false
queue.size();                 // 4
queue.remainingCapacity();    // Integer.MAX_VALUE — always unbounded
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` (**no guaranteed order**) |
| `toArray(T[])` | Converts to typed array (**no guaranteed order**) |
| `iterator()` | Returns iterator (**no guaranteed order**) |
| `forEach(action)` | Iterates with lambda (**no guaranteed order**) |
| `drainTo(collection)` | Moves all elements in **priority order** |
| `drainTo(collection, maxElements)` | Moves up to N elements in **priority order** |
| `clear()` | Removes all elements |
```java
queue.toArray();
queue.toArray(new Task[0]);
queue.forEach(t -> System.out.println(t));   // no guaranteed order

// Drain elements into a list (in priority order)
List<Task> list = new ArrayList<>();
queue.drainTo(list);           // moves all in priority order
queue.drainTo(list, 3);        // moves up to 3 in priority order

queue.clear();
```

---

### 6. Drain / Processing Methods
```java
// Non-blocking — process available elements
Task task;
while ((task = queue.poll()) != null) {
    System.out.println("Processed: " + task);
}

// Blocking — process elements as they arrive in priority order
while (true) {
    try {
        Task t = queue.take(); // blocks until element available
        System.out.println("Processed: " + t);
    } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        break;
    }
}

// Drain in priority order into list
List<Task> results = new ArrayList<>();
queue.drainTo(results);
results.forEach(System.out::println); // guaranteed priority order
```

---

### 7. Min-Heap vs Max-Heap vs Custom
```java
// Min-Heap (default) — smallest element at head
PriorityBlockingQueue<Integer> minHeap = new PriorityBlockingQueue<>();
minHeap.offer(5); minHeap.offer(1); minHeap.offer(3);
minHeap.peek();  // 1

// Max-Heap — largest element at head
PriorityBlockingQueue<Integer> maxHeap = new PriorityBlockingQueue<>(10, Comparator.reverseOrder());
maxHeap.offer(5); maxHeap.offer(1); maxHeap.offer(3);
maxHeap.peek();  // 5

// Custom — shortest string at head
PriorityBlockingQueue<String> custom = new PriorityBlockingQueue<>(10, Comparator.comparingInt(String::length));
custom.offer("banana"); custom.offer("hi"); custom.offer("apple");
custom.peek();  // "hi"
```

---

### 8. Producer-Consumer Pattern
```java
PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

// Producer Thread — adds tasks with different priorities
Thread producer = new Thread(() -> {
    taskQueue.offer(new Task("Low Priority",  5));
    taskQueue.offer(new Task("High Priority", 1));
    taskQueue.offer(new Task("Critical",      0));
    taskQueue.offer(new Task("Medium",        3));
    System.out.println("All tasks produced");
});

// Consumer Thread — processes in priority order
Thread consumer = new Thread(() -> {
    try {
        while (true) {
            Task task = taskQueue.take();  // blocks if empty, returns highest priority
            System.out.println("Processing: " + task);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

producer.start();
consumer.start();

// Output order: Critical(0) → High Priority(1) → Medium(3) → Low Priority(5)
```

---

### 9. Comparison with Other Blocking Queues

| Feature | `PriorityBlockingQueue` | `LinkedBlockingQueue` | `ArrayBlockingQueue` | `DelayQueue` |
|---------|------------------------|-----------------------|---------------------|--------------|
| Backed by | Heap | Linked Nodes | Array | Heap |
| Bounded | No (unbounded) | Optional | Yes (fixed) | No (unbounded) |
| Ordering | Priority | FIFO | FIFO | Delay expiry |
| Blocks on Insert | Never | When full | When full | Never |
| Blocks on Retrieve | When empty | When empty | When empty | Until expired |
| Fair Mode | No | No | Yes | No |
| Best For | Priority tasks | High throughput | Fixed size tasks | Scheduled tasks |

---

### Quick Comparison — Insertion & Removal Methods

| Operation | Throws Exception | Returns Value | Blocks (timeout) | Blocks (indefinitely) |
|-----------|-----------------|---------------|-------------------|-----------------------|
| Insert | `add(e)` | `offer(e)` | `offer(e, t, u)` *(ignored)* | `put(e)` *(never blocks)* |
| Peek | `element()` | `peek()` | — | — |
| Remove | `remove()` | `poll()` | `poll(t, u)` | `take()` |

---

> **Important Notes:**
> - `iterator()` and `forEach()` do **NOT** guarantee priority order.
> - Use **`drainTo()`** or **repeated `poll()`/`take()`** to get elements in priority order.
> - Since the queue is **unbounded**, always monitor memory usage in production.
> - `put()` and `offer(e, timeout, unit)` **never block** — timeout is meaningless here.
> - Always handle `InterruptedException` properly in threaded environments.