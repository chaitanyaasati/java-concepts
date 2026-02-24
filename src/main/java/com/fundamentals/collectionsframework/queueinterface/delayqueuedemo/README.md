## DelayQueue Important Methods in Java

> `DelayQueue` is a class in Java (`java.util.concurrent.DelayQueue`).
> It is an **unbounded blocking queue** where elements can only be retrieved
> when their **delay has expired**.
> Elements must implement the **`Delayed`** interface.
```java
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

DelayQueue<DelayedElement> delayQueue = new DelayQueue<>();
```

---

### Setting Up — Delayed Interface

> Every element added to `DelayQueue` must implement `Delayed` interface.
```java
class DelayedElement implements Delayed {
    private final String name;
    private final long expiryTime; // in milliseconds

    public DelayedElement(String name, long delayMs) {
        this.name = name;
        this.expiryTime = System.currentTimeMillis() + delayMs;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = expiryTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(
            this.getDelay(TimeUnit.MILLISECONDS),
            o.getDelay(TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public String toString() { return name; }
}
```

---

### 1. Insertion Methods

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `add(e)` | Inserts element | Yes (`IllegalStateException`) |
| `offer(e)` | Inserts element | No (returns `true` always) |
| `put(e)` | Inserts element (blocking) | No (never blocks, unbounded) |
```java
delayQueue.add(new DelayedElement("Task A", 3000));    // expires after 3s
delayQueue.offer(new DelayedElement("Task B", 1000));  // expires after 1s — preferred
delayQueue.put(new DelayedElement("Task C", 5000));    // expires after 5s
```

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `peek()` | Returns head element **even if not expired** | No (returns `null`) |
```java
delayQueue.peek(); // returns head — even if delay not expired, null if empty
```

> **Note:** `peek()` returns the element with the **shortest remaining delay**,
> regardless of whether it has expired or not.

---

### 3. Removal Methods — *Removes the element*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `poll()` | Removes & returns head **only if expired** | No (returns `null`) |
| `poll(timeout, unit)` | Waits up to timeout for an expired element | Yes (`InterruptedException`) |
| `take()` | Blocks until an expired element is available | Yes (`InterruptedException`) |
| `remove()` | Removes & returns head **only if expired** | Yes (`NoSuchElementException`) |
| `remove(obj)` | Removes a specific element regardless of expiry | No (returns `true/false`) |
```java
delayQueue.poll();                                    // null if no expired element
delayQueue.poll(2, TimeUnit.SECONDS);                 // waits up to 2s for expired element
delayQueue.take();                                    // blocks until element expires
delayQueue.remove();                                  // throws if no expired element
delayQueue.remove(new DelayedElement("Task A", 0));   // removes specific element
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists (expired or not) |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns total number of elements (expired or not) |
| `remainingCapacity()` | Always returns `Integer.MAX_VALUE` (unbounded) |
```java
delayQueue.contains(element); // true — regardless of expiry
delayQueue.isEmpty();         // false
delayQueue.size();            // 3 — includes non-expired elements
delayQueue.remainingCapacity(); // Integer.MAX_VALUE
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` (includes non-expired) |
| `toArray(T[])` | Converts to typed array (includes non-expired) |
| `iterator()` | Returns iterator (**no guaranteed order**, includes non-expired) |
| `forEach(action)` | Iterates with lambda (includes non-expired) |
| `drainTo(collection)` | Moves all **expired** elements to another collection |
| `drainTo(collection, maxElements)` | Moves up to N **expired** elements |
| `clear()` | Removes all elements regardless of expiry |
```java
delayQueue.toArray();
delayQueue.toArray(new DelayedElement[0]);
delayQueue.forEach(e -> System.out.println(e));

// Drain expired elements into a list
List<DelayedElement> expired = new ArrayList<>();
delayQueue.drainTo(expired);           // all expired
delayQueue.drainTo(expired, 5);        // up to 5 expired

delayQueue.clear();
```

---

### 6. Drain / Processing Methods
```java
// Non-blocking — process only expired elements
DelayedElement element;
while ((element = delayQueue.poll()) != null) {
    System.out.println("Processed: " + element);
}

// Blocking — process elements as they expire
while (true) {
    try {
        DelayedElement e = delayQueue.take(); // blocks until expired
        System.out.println("Processed: " + e);
    } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        break;
    }
}
```

---

### 7. Common Use Cases

| Use Case | Description |
|----------|-------------|
| Task Scheduling | Execute tasks after a specific delay |
| Cache Expiry | Remove cached items after TTL expires |
| Session Timeout | Invalidate sessions after inactivity |
| Retry Mechanism | Retry failed tasks after a delay |
| Rate Limiting | Control request processing rate |
```java
// Example: Simple Task Scheduler
DelayQueue<DelayedElement> scheduler = new DelayQueue<>();

scheduler.offer(new DelayedElement("Send Email", 2000));   // after 2s
scheduler.offer(new DelayedElement("Clear Cache", 5000));  // after 5s
scheduler.offer(new DelayedElement("Backup DB", 10000));   // after 10s

// Worker thread
new Thread(() -> {
    while (!scheduler.isEmpty()) {
        try {
            DelayedElement task = scheduler.take();
            System.out.println("Executing: " + task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}).start();
```

---

### Quick Comparison — Retrieval Methods

| Method | Blocks? | Returns if Not Expired | Returns if Empty |
|--------|---------|------------------------|-----------------|
| `peek()` | No | Returns element | `null` |
| `poll()` | No | `null` | `null` |
| `poll(timeout, unit)` | Yes (up to timeout) | Waits | `null` |
| `take()` | Yes (indefinitely) | Waits | Waits |

> **Best Practice:** Use `take()` in a dedicated worker thread for continuous processing.
> Use `poll()` for non-blocking checks.
> Never use `peek()` to decide if an element is ready — always use `poll()` or `take()`.