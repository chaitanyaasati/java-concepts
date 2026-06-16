# Semaphores in Java

## Table of Contents
- [Introduction](#introduction)
- [How a Semaphore Works](#how-a-semaphore-works)
    - [The Analogy](#the-analogy)
    - [Permits Explained](#permits-explained)
- [Java's `Semaphore` Class](#javas-semaphore-class)
    - [Package and Import](#package-and-import)
    - [Constructors](#constructors)
    - [Key Methods](#key-methods)
- [Types of Semaphores](#types-of-semaphores)
    - [Binary Semaphore](#binary-semaphore)
    - [Counting Semaphore](#counting-semaphore)
- [Fairness in Semaphores](#fairness-in-semaphores)
- [Examples](#examples)
    - [Example 1 — Binary Semaphore (Mutual Exclusion)](#example-1--binary-semaphore-mutual-exclusion)
    - [Example 2 — Counting Semaphore (Resource Pool)](#example-2--counting-semaphore-resource-pool)
    - [Example 3 — Database Connection Pool](#example-3--database-connection-pool)
    - [Example 4 — Rate Limiting](#example-4--rate-limiting)
    - [Example 5 — Producer-Consumer with Semaphores](#example-5--producerconsumer-with-semaphores)
    - [Example 6 — tryAcquire (Non-Blocking)](#example-6--tryacquire-non-blocking)
    - [Example 7 — Fair Semaphore](#example-7--fair-semaphore)
- [Semaphore vs synchronized](#semaphore-vs-synchronized)
- [Semaphore vs ReentrantLock](#semaphore-vs-reentrantlock)
- [Common Pitfalls](#common-pitfalls)
- [Summary](#summary)

---

## Introduction

A **Semaphore** is a synchronization primitive used in concurrent programming to **control access to shared resources** by multiple threads. It was introduced by Edsger Dijkstra in 1965 and remains one of the most fundamental tools in concurrent programming.

In Java, the `java.util.concurrent.Semaphore` class provides a full-featured, thread-safe implementation available since **Java 5**.

Semaphores are used when you need to:
- Limit the number of threads accessing a resource simultaneously
- Implement resource pools (database connections, threads, file handles)
- Coordinate thread execution order
- Implement producer-consumer patterns

---

## How a Semaphore Works

### The Analogy

Imagine a **parking lot** with a fixed number of spaces:

```
Parking Lot (5 spaces)
┌────────────────────────────────────────────┐
│  [Car1]  [Car2]  [Car3]  [ Free ]  [ Free ]│
│                                            │
│  Available permits: 2                      │
└────────────────────────────────────────────┘

 → New car arrives: acquires a permit, parks (permits: 1)
 → Car1 leaves: releases a permit (permits: 2)
 → Lot is full (0 permits): new cars WAIT at the gate
```

- **Entering** the lot = **acquiring** a permit
- **Leaving** the lot = **releasing** a permit
- **Lot is full** = threads block until a permit is released

### Permits Explained

A semaphore maintains a count of **permits**:

```
Initial permits: N

acquire() → permits - 1   (if permits == 0, thread BLOCKS)
release() → permits + 1   (unblocks a waiting thread if any)
```

```
Timeline with N=2 (2 permits):

Permit count:   2       1       0       0       1       2
                │       │       │       │       │       │
Thread A:       ├─acquire─────────────────release─────►
Thread B:           ├─acquire────────release─────────►
Thread C:               ├─BLOCKED───────────acquire──►
                                        ↑
                              Thread B released → Thread C unblocks
```

---

## Java's `Semaphore` Class

### Package and Import

```java
import java.util.concurrent.Semaphore;
```

### Constructors

```java
// Creates a semaphore with N permits (non-fair by default)
Semaphore semaphore = new Semaphore(int permits);

// Creates a semaphore with N permits and specified fairness
Semaphore semaphore = new Semaphore(int permits, boolean fair);
```

| Parameter | Description |
|---|---|
| `permits` | Initial number of permits (can be 0 or negative) |
| `fair` | `true` = FIFO ordering for waiting threads; `false` = no order guarantee |

### Key Methods

| Method | Description | Blocking? |
|---|---|---|
| `acquire()` | Acquires 1 permit; blocks if none available | ✅ Yes |
| `acquire(int n)` | Acquires `n` permits; blocks if not enough available | ✅ Yes |
| `acquireUninterruptibly()` | Like `acquire()` but ignores interrupts | ✅ Yes |
| `tryAcquire()` | Tries to acquire 1 permit immediately; returns `false` if unavailable | ❌ No |
| `tryAcquire(long timeout, TimeUnit unit)` | Tries to acquire within a timeout | ❌ No |
| `tryAcquire(int n, long timeout, TimeUnit unit)` | Tries to acquire `n` permits within a timeout | ❌ No |
| `release()` | Releases 1 permit | ❌ No |
| `release(int n)` | Releases `n` permits | ❌ No |
| `availablePermits()` | Returns the current number of available permits | ❌ No |
| `getQueueLength()` | Returns estimated number of threads waiting | ❌ No |
| `hasQueuedThreads()` | Returns `true` if threads are waiting | ❌ No |
| `drainPermits()` | Acquires all available permits, returns count | ❌ No |

---

## Types of Semaphores

### Binary Semaphore

A binary semaphore has only **1 permit**. It acts like a mutex — at most one thread can hold it at a time.

```java
Semaphore mutex = new Semaphore(1); // binary semaphore
```

```
Permits: 1

Thread A: acquire() → [permits: 0] → ... critical section ... → release() → [permits: 1]
Thread B:                              BLOCKED                  → acquire() → [permits: 0]
```

> **Key difference from `synchronized`:** A binary semaphore can be **released by a different thread** than the one that acquired it. `synchronized` and `ReentrantLock` must be released by the same thread.

### Counting Semaphore

A counting semaphore has **N permits** (N > 1), allowing up to N threads to access the resource concurrently.

```java
Semaphore pool = new Semaphore(5); // up to 5 threads at once
```

```
Permits: 5

T1: acquire() → [4]
T2: acquire() → [3]
T3: acquire() → [2]
T4: acquire() → [1]
T5: acquire() → [0]
T6: acquire() → BLOCKED (0 permits)
T1: release() → [1] → T6 unblocks → [0]
```

---

## Fairness in Semaphores

By default, Java's `Semaphore` is **non-fair**: when a permit becomes available, any waiting thread may acquire it — there's no guarantee of order. This can lead to **thread starvation**.

```java
// Non-fair (default) — faster but may starve threads
Semaphore nonFair = new Semaphore(3);

// Fair — FIFO order guaranteed, slightly slower
Semaphore fair = new Semaphore(3, true);
```

| Mode | Ordering | Throughput | Starvation Risk |
|---|---|---|---|
| Non-fair (`false`) | No guarantee | Higher | ⚠️ Yes |
| Fair (`true`) | FIFO | Slightly lower | ❌ No |

> 💡 Use `fair = true` when thread starvation is a real concern, such as in long-running server applications with many competing threads.

---

## Examples

### Example 1 — Binary Semaphore (Mutual Exclusion)

Demonstrates using a semaphore with 1 permit to protect a critical section, similar to `synchronized`:

```java
import java.util.concurrent.Semaphore;

public class BinarySemaphoreDemo {

    private static final Semaphore mutex = new Semaphore(1); // 1 permit = binary
    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 5; i++) {
                try {
                    mutex.acquire(); // acquire permit — enter critical section
                    counter++;
                    System.out.println(Thread.currentThread().getName()
                            + " | counter = " + counter);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    mutex.release(); // always release in finally!
                }
            }
        };

        Thread t1 = new Thread(task, "Thread-A");
        Thread t2 = new Thread(task, "Thread-B");
        Thread t3 = new Thread(task, "Thread-C");

        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();

        System.out.println("Final counter: " + counter); // always 15
    }
}

// Sample Output (no race condition):
// Thread-A | counter = 1
// Thread-A | counter = 2
// Thread-B | counter = 3
// Thread-C | counter = 4
// ...
// Final counter: 15
```

---

### Example 2 — Counting Semaphore (Resource Pool)

Limits concurrent access to a shared resource — here, only 3 threads can print at a time:

```java
import java.util.concurrent.Semaphore;

public class CountingSemaphoreDemo {

    // Only 3 threads allowed in the "printer room" at a time
    private static final Semaphore printerRoom = new Semaphore(3);

    public static void main(String[] args) {

        for (int i = 1; i <= 8; i++) {
            final int jobId = i;
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName()
                            + " waiting to print job #" + jobId);

                    printerRoom.acquire(); // wait for a slot
                    System.out.println(Thread.currentThread().getName()
                            + " PRINTING job #" + jobId
                            + " | Active printers: "
                            + (3 - printerRoom.availablePermits()));

                    Thread.sleep(2000); // simulate printing

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println(Thread.currentThread().getName()
                            + " DONE with job #" + jobId);
                    printerRoom.release(); // free the slot
                }
            }, "Printer-" + i).start();
        }
    }
}

// At most 3 "PRINTING" lines appear simultaneously
```

---

### Example 3 — Database Connection Pool

A realistic use case: limiting concurrent database connections:

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

    private static final int MAX_CONNECTIONS = 3;
    private final Semaphore semaphore = new Semaphore(MAX_CONNECTIONS, true); // fair

    // Simulated connection object
    public static class Connection {
        private final int id;
        Connection(int id) { this.id = id; }
        public String query(String sql) { return "Result of [" + sql + "] from conn-" + id; }
        public int getId() { return id; }
    }

    // Acquire a connection (blocks until one is available or timeout)
    public Connection getConnection(long timeoutMs) throws InterruptedException {
        boolean acquired = semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        if (!acquired) {
            throw new RuntimeException("Connection timeout — pool exhausted");
        }
        int connId = MAX_CONNECTIONS - semaphore.availablePermits();
        System.out.println(Thread.currentThread().getName()
                + " acquired connection-" + connId
                + " | Available: " + semaphore.availablePermits());
        return new Connection(connId);
    }

    // Release the connection back to the pool
    public void releaseConnection(Connection conn) {
        semaphore.release();
        System.out.println(Thread.currentThread().getName()
                + " released connection-" + conn.getId()
                + " | Available: " + semaphore.availablePermits());
    }

    public static void main(String[] args) {
        ConnectionPool pool = new ConnectionPool();

        for (int i = 1; i <= 6; i++) {
            final int threadId = i;
            new Thread(() -> {
                Connection conn = null;
                try {
                    conn = pool.getConnection(3000); // wait up to 3s
                    String result = conn.query("SELECT * FROM users WHERE id=" + threadId);
                    System.out.println(Thread.currentThread().getName() + " → " + result);
                    Thread.sleep(1500); // simulate query execution
                } catch (Exception e) {
                    System.err.println(Thread.currentThread().getName() + " failed: " + e.getMessage());
                } finally {
                    if (conn != null) pool.releaseConnection(conn);
                }
            }, "Thread-" + i).start();
        }
    }
}

// At most 3 threads hold connections at any time
// Threads 4, 5, 6 wait until one of the first 3 releases
```

---

### Example 4 — Rate Limiting

Control the rate of API calls — allow only N calls per time window:

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimiter {

    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Allow maxRequests per intervalMs milliseconds
    public RateLimiter(int maxRequests, long intervalMs) {
        this.semaphore = new Semaphore(maxRequests);

        // Refill permits periodically
        scheduler.scheduleAtFixedRate(() -> {
            int released = maxRequests - semaphore.availablePermits();
            if (released > 0) {
                semaphore.release(released);
                System.out.println("[RateLimiter] Refilled " + released + " permit(s)");
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public boolean tryRequest(String requestId) {
        if (semaphore.tryAcquire()) {
            System.out.println("[ALLOWED] " + requestId
                    + " | Remaining: " + semaphore.availablePermits());
            return true;
        } else {
            System.out.println("[BLOCKED] " + requestId + " — rate limit exceeded");
            return false;
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        // Allow 3 requests per second
        RateLimiter limiter = new RateLimiter(3, 1000);

        // Fire 7 requests rapidly
        for (int i = 1; i <= 7; i++) {
            limiter.tryRequest("Request-" + i);
        }

        Thread.sleep(1100); // wait for refill

        System.out.println("\n--- After 1 second refill ---");
        for (int i = 8; i <= 10; i++) {
            limiter.tryRequest("Request-" + i);
        }

        limiter.shutdown();
    }
}

// Output:
// [ALLOWED] Request-1 | Remaining: 2
// [ALLOWED] Request-2 | Remaining: 1
// [ALLOWED] Request-3 | Remaining: 0
// [BLOCKED] Request-4 — rate limit exceeded
// [BLOCKED] Request-5 — rate limit exceeded
// [BLOCKED] Request-6 — rate limit exceeded
// [BLOCKED] Request-7 — rate limit exceeded
// [RateLimiter] Refilled 3 permit(s)
// --- After 1 second refill ---
// [ALLOWED] Request-8 | Remaining: 2
// [ALLOWED] Request-9 | Remaining: 1
// [ALLOWED] Request-10 | Remaining: 0
```

---

### Example 5 — Producer-Consumer with Semaphores

Classic producer-consumer problem solved with two semaphores — one tracking empty slots, one tracking filled slots:

```java
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ProducerConsumer {

    private static final int BUFFER_SIZE = 3;
    private static final Queue<Integer> buffer = new LinkedList<>();

    // Tracks available empty slots (starts at BUFFER_SIZE)
    private static final Semaphore emptySlots = new Semaphore(BUFFER_SIZE);

    // Tracks available filled slots (starts at 0)
    private static final Semaphore filledSlots = new Semaphore(0);

    // Mutual exclusion for buffer access
    private static final Semaphore mutex = new Semaphore(1);

    static class Producer implements Runnable {
        @Override
        public void run() {
            for (int item = 1; item <= 7; item++) {
                try {
                    emptySlots.acquire(); // wait for an empty slot
                    mutex.acquire();      // lock the buffer

                    buffer.add(item);
                    System.out.println("Produced: " + item
                            + " | Buffer: " + buffer
                            + " | Empty slots: " + emptySlots.availablePermits());

                    mutex.release();      // unlock the buffer
                    filledSlots.release(); // signal a filled slot

                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 7; i++) {
                try {
                    filledSlots.acquire(); // wait for a filled slot
                    mutex.acquire();       // lock the buffer

                    int item = buffer.poll();
                    System.out.println("Consumed: " + item
                            + " | Buffer: " + buffer
                            + " | Filled slots: " + filledSlots.availablePermits());

                    mutex.release();       // unlock the buffer
                    emptySlots.release();  // signal an empty slot

                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread producer = new Thread(new Producer(), "Producer");
        Thread consumer = new Thread(new Consumer(), "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        System.out.println("Done. Final buffer: " + buffer);
    }
}

// Sample Output:
// Produced: 1 | Buffer: [1]       | Empty slots: 2
// Produced: 2 | Buffer: [1, 2]    | Empty slots: 1
// Consumed: 1 | Buffer: [2]       | Filled slots: 1
// Produced: 3 | Buffer: [2, 3]    | Empty slots: 1
// ...
```

---

### Example 6 — tryAcquire (Non-Blocking)

Use `tryAcquire()` when you want to attempt access without blocking:

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TryAcquireDemo {

    private static final Semaphore semaphore = new Semaphore(2);

    public static void main(String[] args) throws InterruptedException {

        // Immediate tryAcquire — does NOT block
        System.out.println("=== Immediate tryAcquire ===");
        for (int i = 1; i <= 4; i++) {
            boolean acquired = semaphore.tryAcquire();
            System.out.println("Thread-" + i + ": "
                    + (acquired ? "✅ Acquired" : "❌ Failed (no permits)"));
        }

        semaphore.release(2); // reset

        // tryAcquire with timeout — waits up to N ms before giving up
        System.out.println("\n=== tryAcquire with 1s timeout ===");
        Runnable slowRelease = () -> {
            try {
                Thread.sleep(500);
                semaphore.release();
                System.out.println("[Background] Released a permit");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        semaphore.acquire(2); // drain all permits
        new Thread(slowRelease).start();

        boolean result = semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
        System.out.println("After timeout attempt: " + (result ? "✅ Acquired" : "❌ Timed out"));
    }
}

// Output:
// === Immediate tryAcquire ===
// Thread-1: ✅ Acquired
// Thread-2: ✅ Acquired
// Thread-3: ❌ Failed (no permits)
// Thread-4: ❌ Failed (no permits)
//
// === tryAcquire with 1s timeout ===
// [Background] Released a permit
// After timeout attempt: ✅ Acquired
```

---

### Example 7 — Fair Semaphore

Demonstrates that fair mode preserves thread arrival order:

```java
import java.util.concurrent.Semaphore;

public class FairSemaphoreDemo {

    // fair = true: threads acquire in the order they requested
    private static final Semaphore fairSemaphore = new Semaphore(1, true);

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            try {
                System.out.println(Thread.currentThread().getName() + " is waiting...");
                fairSemaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " → ACQUIRED");
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                fairSemaphore.release();
                System.out.println(Thread.currentThread().getName() + " → RELEASED");
            }
        };

        // Occupy the semaphore so all threads queue up
        fairSemaphore.acquire();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(task, "Thread-" + (i + 1));
            threads[i].start();
            Thread.sleep(50); // small gap to ensure arrival order
        }

        fairSemaphore.release(); // release to let them proceed in order

        for (Thread t : threads) t.join();
    }
}

// Output (order preserved with fair = true):
// Thread-1 is waiting... → ACQUIRED → RELEASED
// Thread-2 is waiting... → ACQUIRED → RELEASED
// Thread-3 is waiting... → ACQUIRED → RELEASED
// Thread-4 is waiting... → ACQUIRED → RELEASED
// Thread-5 is waiting... → ACQUIRED → RELEASED
```

---

## Semaphore vs `synchronized`

| Feature | `Semaphore` | `synchronized` |
|---|---|---|
| **Permit count** | N (configurable) | 1 (binary only) |
| **Released by different thread** | ✅ Yes | ❌ No |
| **Timeout support** | ✅ `tryAcquire(timeout)` | ❌ No |
| **Interruptible wait** | ✅ `acquire()` | ❌ No (not directly) |
| **Fairness control** | ✅ Yes | ❌ No |
| **Reentrancy** | ❌ Not reentrant | ✅ Reentrant |
| **Visibility** | Queue length visible | Opaque |
| **Use case** | Resource pools, rate limiting | Simple mutual exclusion |

---

## Semaphore vs `ReentrantLock`

| Feature | `Semaphore` | `ReentrantLock` |
|---|---|---|
| **Permits** | N permits (counting) | 1 (binary, like mutex) |
| **Reentrancy** | ❌ Not reentrant | ✅ Reentrant |
| **Release by different thread** | ✅ Yes | ❌ No (same thread only) |
| **Condition variables** | ❌ No | ✅ `newCondition()` |
| **Timeout** | ✅ `tryAcquire(timeout)` | ✅ `tryLock(timeout)` |
| **Fairness** | ✅ Yes | ✅ Yes |
| **Best for** | Counting resource access | Exclusive critical sections |

---

## Common Pitfalls

### 1. Forgetting to Release — Deadlock

```java
// ❌ If an exception occurs before release(), the permit is lost forever
try {
    semaphore.acquire();
    riskyOperation(); // throws RuntimeException
    semaphore.release(); // NEVER REACHED → deadlock
} catch (Exception e) { }

// ✅ Always use finally to guarantee release
try {
    semaphore.acquire();
    riskyOperation();
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
} finally {
    semaphore.release(); // ALWAYS executed
}
```

### 2. Releasing More Than Acquired — Inflated Permits

```java
Semaphore semaphore = new Semaphore(2);

// ❌ Releasing without acquiring inflates permits beyond the intended limit
semaphore.release();
semaphore.release();
semaphore.release();

System.out.println(semaphore.availablePermits()); // 5 — exceeds intended max of 2!

// ✅ Only release what you acquired, and only after a successful acquire
```

### 3. Semaphore is NOT Reentrant

```java
Semaphore semaphore = new Semaphore(1);

// ❌ Same thread acquiring twice causes deadlock
semaphore.acquire(); // acquires the single permit
semaphore.acquire(); // DEADLOCK — waiting for a permit it already holds

// ✅ If reentrancy is needed, use ReentrantLock instead
```

### 4. Using `availablePermits()` for Control Flow

```java
// ❌ NOT thread-safe — count can change between check and action
if (semaphore.availablePermits() > 0) {
    // Another thread may have taken the last permit here
    semaphore.acquire(); // may block unexpectedly
}

// ✅ Use tryAcquire() — atomic check-and-acquire
if (semaphore.tryAcquire()) {
    try {
        // safe — you definitely hold the permit
    } finally {
        semaphore.release();
    }
}
```

### 5. Interrupt Handling

```java
// ❌ Swallowing the interrupt loses the signal
try {
    semaphore.acquire();
} catch (InterruptedException e) {
    // Silently ignoring the interrupt — bad practice
}

// ✅ Always restore the interrupt status
try {
    semaphore.acquire();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // restore interrupt flag
    return; // or handle gracefully
}

// ✅ Alternative: acquireUninterruptibly() if interrupts should be ignored by design
semaphore.acquireUninterruptibly(); // blocks until acquired, ignores interrupts
```

---

## Summary

| Concept | Key Point |
|---|---|
| **What it is** | A synchronization tool controlling concurrent access via permits |
| **Binary Semaphore** | 1 permit — acts as a mutex; can be released by a different thread |
| **Counting Semaphore** | N permits — limits concurrent access to N threads |
| **`acquire()`** | Blocks until a permit is available, then decrements count |
| **`release()`** | Increments permit count; unblocks a waiting thread if any |
| **`tryAcquire()`** | Non-blocking attempt; returns `false` immediately if no permits |
| **Fairness** | `new Semaphore(n, true)` enables FIFO ordering to prevent starvation |
| **Not reentrant** | Same thread acquiring twice deadlocks — use `ReentrantLock` if needed |
| **Always release in `finally`** | Guarantees permit is returned even on exceptions |
| **Key advantage over `synchronized`** | Counting permits, cross-thread release, timeout, interruptibility |

**Most Common Real-World Uses:**

| Scenario | Semaphore Configuration |
|---|---|
| Mutual exclusion (mutex) | `new Semaphore(1)` |
| Database connection pool (max 10) | `new Semaphore(10, true)` |
| Rate limiter (5 req/sec) | `new Semaphore(5)` + periodic `release()` |
| Producer-Consumer buffer | Two semaphores: `emptySlots` + `filledSlots` |
| Thread-safe resource pool | `new Semaphore(poolSize, true)` |

> 💡 **Best Practice:** Always release semaphores in a `finally` block. Prefer `tryAcquire()` over checking `availablePermits()`. Use fair semaphores (`true`) in long-running applications to prevent thread starvation.