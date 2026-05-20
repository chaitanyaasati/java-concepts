# ReentrantLock in Java

## Table of Contents
- [Introduction](#introduction)
- [What is Reentrancy?](#what-is-reentrancy)
- [How ReentrantLock Works](#how-reentrantlock-works)
    - [The Analogy](#the-analogy)
    - [Lock Hold Count](#lock-hold-count)
- [Java's `ReentrantLock` Class](#javas-reentrantlock-class)
    - [Package and Import](#package-and-import)
    - [Constructors](#constructors)
    - [Key Methods](#key-methods)
- [Basic Usage Pattern](#basic-usage-pattern)
- [Fairness in ReentrantLock](#fairness-in-reentrantlock)
- [Condition Variables](#condition-variables)
    - [What is a Condition?](#what-is-a-condition)
    - [Condition Methods](#condition-methods)
- [Examples](#examples)
    - [Example 1 — Basic Lock and Unlock](#example-1--basic-lock-and-unlock)
    - [Example 2 — Reentrancy in Action](#example-2--reentrancy-in-action)
    - [Example 3 — tryLock (Non-Blocking)](#example-3--trylock-non-blocking)
    - [Example 4 — tryLock with Timeout](#example-4--trylock-with-timeout)
    - [Example 5 — Interruptible Lock](#example-5--interruptible-lock)
    - [Example 6 — Fair ReentrantLock](#example-6--fair-reentrantlock)
    - [Example 7 — Condition Variables (Producer-Consumer)](#example-7--condition-variables-producerconsumer)
    - [Example 8 — Multiple Conditions](#example-8--multiple-conditions)
    - [Example 9 — Read-Write Locking with ReentrantReadWriteLock](#example-9--readwrite-locking-with-reentrantreadwritelock)
    - [Example 10 — Deadlock Detection with tryLock](#example-10--deadlock-detection-with-trylock)
- [ReentrantLock vs `synchronized`](#reentrantlock-vs-synchronized)
- [ReentrantLock vs `Semaphore`](#reentrantlock-vs-semaphore)
- [Common Pitfalls](#common-pitfalls)
- [Summary](#summary)

---

## Introduction

`ReentrantLock` is a **mutual exclusion lock** provided by the `java.util.concurrent.locks` package, available since **Java 5**. It offers the same fundamental behaviour as the `synchronized` keyword — only one thread can hold the lock at a time — but with significantly more flexibility and control.

The name **Reentrant** means that a thread that already holds the lock can acquire it again without blocking itself. This is the same behaviour as `synchronized` (which is also reentrant), but `ReentrantLock` exposes this as an explicit, inspectable counter.

Key advantages over `synchronized`:
- **Timed lock attempts** — try to acquire with a timeout
- **Interruptible locking** — waiting thread can be interrupted
- **Non-blocking `tryLock()`** — attempt without blocking at all
- **Fairness policy** — guarantee FIFO thread ordering
- **Condition variables** — multiple wait/signal queues per lock
- **Lock introspection** — query hold count, queue length, etc.

---

## What is Reentrancy?

**Reentrancy** means a thread can acquire a lock it **already holds** without deadlocking itself.

```
Without reentrancy (non-reentrant lock):

Thread-A holds lock → calls method that tries to acquire same lock → DEADLOCK ❌

With reentrancy (ReentrantLock / synchronized):

Thread-A holds lock (hold count: 1)
  → acquires same lock again (hold count: 2) ✅
    → acquires again (hold count: 3) ✅
      → unlock (hold count: 2)
    → unlock (hold count: 1)
  → unlock (hold count: 0) → lock is FREE for other threads
```

The lock is only released to other threads when the **hold count reaches 0**. Every `lock()` must be matched with exactly one `unlock()`.

---

## How ReentrantLock Works

### The Analogy

Think of `ReentrantLock` as a **hotel room key card** system with a **personal counter**:

```
Hotel Room (the locked resource)
┌──────────────────────────────────────────────┐
│                                              │
│  Key Card Holder: Thread-A                   │
│  Entry Count    : 2  (acquired twice)        │
│                                              │
│  Waiting in lobby: [Thread-B, Thread-C]      │
└──────────────────────────────────────────────┘

 → Thread-A swipes the card again: count 2 → 3 (no issue, same holder)
 → Thread-A checks out once:       count 3 → 2
 → Thread-A checks out again:      count 2 → 1
 → Thread-A checks out finally:    count 1 → 0  → Room available!
 → Thread-B (next in queue) gets the key card
```

### Lock Hold Count

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();   // hold count: 1
lock.lock();   // hold count: 2
lock.lock();   // hold count: 3

System.out.println(lock.getHoldCount()); // 3

lock.unlock(); // hold count: 2
lock.unlock(); // hold count: 1
lock.unlock(); // hold count: 0  → lock released to other threads
```

---

## Java's `ReentrantLock` Class

### Package and Import

```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;         // for condition variables
import java.util.concurrent.locks.ReentrantReadWriteLock; // for read-write variant
```

### Constructors

```java
// Non-fair lock (default) — higher throughput, no ordering guarantee
ReentrantLock lock = new ReentrantLock();

// Fair lock — threads acquire in FIFO arrival order
ReentrantLock lock = new ReentrantLock(boolean fair);
```

### Key Methods

| Method | Description | Blocking? |
|---|---|---|
| `lock()` | Acquires the lock; blocks until available | ✅ Yes |
| `lockInterruptibly()` | Acquires the lock; can be interrupted while waiting | ✅ Yes |
| `tryLock()` | Tries to acquire immediately; returns `false` if unavailable | ❌ No |
| `tryLock(long time, TimeUnit unit)` | Tries to acquire within a timeout | ⏱ Timed |
| `unlock()` | Releases the lock (must be called by lock holder) | ❌ No |
| `newCondition()` | Returns a new `Condition` instance bound to this lock | ❌ No |
| `isLocked()` | Returns `true` if any thread holds this lock | ❌ No |
| `isHeldByCurrentThread()` | Returns `true` if the calling thread holds this lock | ❌ No |
| `getHoldCount()` | Returns the reentrant hold count of the current thread | ❌ No |
| `isFair()` | Returns `true` if the lock uses fair ordering | ❌ No |
| `hasQueuedThreads()` | Returns `true` if threads are waiting to acquire | ❌ No |
| `getQueueLength()` | Returns estimated number of waiting threads | ❌ No |

---

## Basic Usage Pattern

Always follow this pattern — **lock before the try, unlock in the finally**:

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();               // acquire BEFORE try block
try {
    // --- critical section ---
    // Only one thread executes here at a time
} finally {
    lock.unlock();         // ALWAYS unlock in finally
}
```

> ⚠️ **Critical Rule:** Never put `lock()` inside the `try` block. If `lock()` itself throws (rare but possible with `lockInterruptibly()`), the `finally` block would call `unlock()` without a corresponding `lock()`, causing an `IllegalMonitorStateException`.

```java
// ❌ Wrong pattern — lock inside try
try {
    lock.lock(); // if this throws, finally still calls unlock()
    // ...
} finally {
    lock.unlock(); // IllegalMonitorStateException!
}

// ✅ Correct pattern — lock before try
lock.lock();
try {
    // ...
} finally {
    lock.unlock();
}
```

---

## Fairness in ReentrantLock

```java
// Non-fair (default) — any waiting thread may acquire next
ReentrantLock nonFair = new ReentrantLock(false);

// Fair — threads acquire in the order they requested (FIFO)
ReentrantLock fair = new ReentrantLock(true);
```

| Mode | Ordering | Throughput | Starvation Risk |
|---|---|---|---|
| Non-fair (`false`) | No guarantee | Higher | ⚠️ Possible |
| Fair (`true`) | FIFO arrival order | Slightly lower | ❌ Eliminated |

```
Non-fair scenario (Thread-B may "barge" ahead):

T-A releases lock → T-C (new arrival) grabs it before T-B (already waiting)
Result: T-B keeps waiting → potential starvation

Fair scenario:

T-A releases lock → T-B (waited first) gets it → T-C waits
Result: strict ordering guaranteed
```

---

## Condition Variables

### What is a Condition?

A `Condition` is a thread coordination mechanism bound to a `ReentrantLock`. It allows threads to **wait** for a specific condition to become true and be **signalled** when it does. It is the flexible equivalent of `Object.wait()` / `Object.notify()` in `synchronized` blocks.

A single `ReentrantLock` can have **multiple Conditions** — each with its own wait queue. This is a key advantage over `synchronized`, which has only one wait queue per object.

```
Lock with two Conditions:

┌─────────────────────────────────────────┐
│  ReentrantLock                          │
│                                         │
│  notFull  Condition  ← producer waits   │
│  notEmpty Condition  ← consumer waits   │
└─────────────────────────────────────────┘

Producer: if buffer full  → notFull.await()
Consumer: if buffer empty → notEmpty.await()

Producer fills buffer → notEmpty.signal()   (wakes a consumer)
Consumer drains buffer → notFull.signal()   (wakes a producer)
```

### Condition Methods

| Method | Description |
|---|---|
| `await()` | Releases the lock and waits; re-acquires on signal |
| `await(long time, TimeUnit unit)` | Waits up to the given time, then returns |
| `awaitUninterruptibly()` | Like `await()` but ignores thread interrupts |
| `awaitNanos(long nanosTimeout)` | Waits up to the given nanoseconds |
| `awaitUntil(Date deadline)` | Waits until the given absolute time |
| `signal()` | Wakes one waiting thread (like `notify()`) |
| `signalAll()` | Wakes all waiting threads (like `notifyAll()`) |

> 💡 `await()` must be called while holding the lock — just like `Object.wait()`. It atomically releases the lock and suspends the thread.

---

## Examples

### Example 1 — Basic Lock and Unlock

Protecting a shared counter with `ReentrantLock`:

```java
import java.util.concurrent.locks.ReentrantLock;

public class BasicLockDemo {

    private static final ReentrantLock lock = new ReentrantLock();
    private static int counter = 0;

    public static void increment() {
        lock.lock();
        try {
            counter++;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                increment();
            }
        };

        Thread t1 = new Thread(task, "Thread-A");
        Thread t2 = new Thread(task, "Thread-B");
        Thread t3 = new Thread(task, "Thread-C");

        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();

        // Always 3000 — no race condition
        System.out.println("Final counter: " + counter);
    }
}

// Output:
// Final counter: 3000
```

---

### Example 2 — Reentrancy in Action

A thread calling a method that internally calls another locked method — both protected by the same lock:

```java
import java.util.concurrent.locks.ReentrantLock;

public class ReentrancyDemo {

    private final ReentrantLock lock = new ReentrantLock();

    public void outerMethod() {
        lock.lock();
        try {
            System.out.println("outerMethod — hold count: " + lock.getHoldCount()); // 1
            innerMethod(); // calls another method that also acquires the same lock
            System.out.println("back in outerMethod — hold count: " + lock.getHoldCount()); // 1
        } finally {
            lock.unlock();
        }
    }

    public void innerMethod() {
        lock.lock(); // same thread re-acquires — NO deadlock
        try {
            System.out.println("innerMethod — hold count: " + lock.getHoldCount()); // 2
            deeperMethod();
        } finally {
            lock.unlock(); // count drops from 2 → 1
        }
    }

    public void deeperMethod() {
        lock.lock();
        try {
            System.out.println("deeperMethod — hold count: " + lock.getHoldCount()); // 3
        } finally {
            lock.unlock(); // count drops from 3 → 2
        }
    }

    public static void main(String[] args) {
        new ReentrancyDemo().outerMethod();
    }
}

// Output:
// outerMethod  — hold count: 1
// innerMethod  — hold count: 2
// deeperMethod — hold count: 3
// back in outerMethod — hold count: 1
```

---

### Example 3 — tryLock (Non-Blocking)

Attempt to acquire the lock without blocking — useful when you have alternative work to do:

```java
import java.util.concurrent.locks.ReentrantLock;

public class TryLockDemo {

    private static final ReentrantLock lock = new ReentrantLock();
    private static int sharedData = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 5; i++) {
                if (lock.tryLock()) {   // non-blocking — returns immediately
                    try {
                        sharedData++;
                        System.out.println(Thread.currentThread().getName()
                                + " → acquired lock | sharedData = " + sharedData);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                } else {
                    // Lock was busy — do something else instead of blocking
                    System.out.println(Thread.currentThread().getName()
                            + " → lock busy, doing other work...");
                    try { Thread.sleep(50); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };

        Thread t1 = new Thread(task, "Thread-A");
        Thread t2 = new Thread(task, "Thread-B");

        t1.start(); t2.start();
        t1.join();  t2.join();
    }
}

// Sample Output:
// Thread-A → acquired lock | sharedData = 1
// Thread-B → lock busy, doing other work...
// Thread-B → acquired lock | sharedData = 2
// Thread-A → acquired lock | sharedData = 3
// ...
```

---

### Example 4 — tryLock with Timeout

Attempt to acquire the lock within a maximum wait time:

```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class TryLockTimeoutDemo {

    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {

        // Thread-A holds the lock for 2 seconds
        Thread threadA = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Thread-A: acquired lock, holding for 2s...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Thread-A: releasing lock");
                lock.unlock();
            }
        }, "Thread-A");

        // Thread-B tries with 3s timeout — will succeed (lock released in 2s)
        Thread threadB = new Thread(() -> {
            try {
                System.out.println("Thread-B: trying to acquire lock (3s timeout)...");
                boolean acquired = lock.tryLock(3, TimeUnit.SECONDS);
                if (acquired) {
                    try {
                        System.out.println("Thread-B: ✅ acquired lock!");
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("Thread-B: ❌ timed out, giving up");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Thread-B");

        // Thread-C tries with 1s timeout — will fail (lock held for 2s)
        Thread threadC = new Thread(() -> {
            try {
                Thread.sleep(100); // slight delay so Thread-A goes first
                System.out.println("Thread-C: trying to acquire lock (1s timeout)...");
                boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);
                if (acquired) {
                    try {
                        System.out.println("Thread-C: ✅ acquired lock!");
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("Thread-C: ❌ timed out after 1s, doing fallback");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Thread-C");

        threadA.start();
        Thread.sleep(50);
        threadB.start();
        threadC.start();

        threadA.join(); threadB.join(); threadC.join();
    }
}

// Output:
// Thread-A: acquired lock, holding for 2s...
// Thread-B: trying to acquire lock (3s timeout)...
// Thread-C: trying to acquire lock (1s timeout)...
// Thread-C: ❌ timed out after 1s, doing fallback
// Thread-A: releasing lock
// Thread-B: ✅ acquired lock!
```

---

### Example 5 — Interruptible Lock

`lockInterruptibly()` allows a waiting thread to be interrupted, useful for cancellable operations:

```java
import java.util.concurrent.locks.ReentrantLock;

public class InterruptibleLockDemo {

    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {

        // Thread-A holds the lock indefinitely
        Thread threadA = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Thread-A: holding the lock...");
                Thread.sleep(Long.MAX_VALUE); // holds forever
            } catch (InterruptedException e) {
                System.out.println("Thread-A: interrupted while sleeping");
            } finally {
                lock.unlock();
            }
        }, "Thread-A");

        // Thread-B tries to acquire — can be interrupted while waiting
        Thread threadB = new Thread(() -> {
            try {
                System.out.println("Thread-B: waiting for lock (interruptibly)...");
                lock.lockInterruptibly(); // can be cancelled!
                try {
                    System.out.println("Thread-B: acquired lock");
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                // Cleanly handles the cancellation
                System.out.println("Thread-B: ✅ interrupted while waiting — cancelled gracefully");
            }
        }, "Thread-B");

        threadA.start();
        Thread.sleep(200);
        threadB.start();
        Thread.sleep(500);

        System.out.println("Main: interrupting Thread-B...");
        threadB.interrupt(); // cancel Thread-B's wait

        threadB.join();
        threadA.interrupt();
        threadA.join();
    }
}

// Output:
// Thread-A: holding the lock...
// Thread-B: waiting for lock (interruptibly)...
// Main: interrupting Thread-B...
// Thread-B: ✅ interrupted while waiting — cancelled gracefully
// Thread-A: interrupted while sleeping
```

---

### Example 6 — Fair ReentrantLock

Demonstrates that threads acquire the lock in the order they requested it:

```java
import java.util.concurrent.locks.ReentrantLock;

public class FairLockDemo {

    // fair = true: FIFO ordering for waiting threads
    private static final ReentrantLock fairLock = new ReentrantLock(true);

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Lock is fair: " + fairLock.isFair());

        Runnable task = () -> {
            System.out.println(Thread.currentThread().getName() + ": waiting for lock...");
            fairLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ": ✅ acquired lock");
                Thread.sleep(200); // hold briefly
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println(Thread.currentThread().getName() + ": releasing lock");
                fairLock.unlock();
            }
        };

        // Pre-occupy the lock so all threads queue up
        fairLock.lock();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(task, "Thread-" + (i + 1));
            threads[i].start();
            Thread.sleep(30); // stagger start so arrival order is clear
        }

        System.out.println("Main: releasing initial lock — threads proceed in order");
        fairLock.unlock();

        for (Thread t : threads) t.join();
    }
}

// Output (strictly ordered):
// Lock is fair: true
// Thread-1: waiting...  → acquired → released
// Thread-2: waiting...  → acquired → released
// Thread-3: waiting...  → acquired → released
// Thread-4: waiting...  → acquired → released
// Thread-5: waiting...  → acquired → released
```

---

### Example 7 — Condition Variables (Producer-Consumer)

Using `Condition` objects for fine-grained thread coordination:

```java
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer {

    private static final int BUFFER_SIZE  = 3;
    private static final Queue<Integer> buffer = new LinkedList<>();
    private static final ReentrantLock lock    = new ReentrantLock();

    // Two separate condition queues — key advantage over synchronized
    private static final Condition notFull  = lock.newCondition(); // producer waits here
    private static final Condition notEmpty = lock.newCondition(); // consumer waits here

    static class Producer implements Runnable {
        @Override
        public void run() {
            for (int item = 1; item <= 8; item++) {
                lock.lock();
                try {
                    // Wait while buffer is full
                    while (buffer.size() == BUFFER_SIZE) {
                        System.out.println("Producer: buffer FULL, waiting...");
                        notFull.await(); // releases lock and waits
                    }

                    buffer.offer(item);
                    System.out.println("Produced: " + item + " | Buffer: " + buffer);

                    notEmpty.signal(); // wake one waiting consumer
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }

                try { Thread.sleep(200); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 8; i++) {
                lock.lock();
                try {
                    // Wait while buffer is empty
                    while (buffer.isEmpty()) {
                        System.out.println("Consumer: buffer EMPTY, waiting...");
                        notEmpty.await(); // releases lock and waits
                    }

                    int item = buffer.poll();
                    System.out.println("Consumed: " + item + " | Buffer: " + buffer);

                    notFull.signal(); // wake one waiting producer
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }

                try { Thread.sleep(500); } catch (InterruptedException e) {
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
        System.out.println("Done. Buffer: " + buffer);
    }
}

// Sample Output:
// Produced: 1 | Buffer: [1]
// Produced: 2 | Buffer: [1, 2]
// Consumed: 1 | Buffer: [2]
// Produced: 3 | Buffer: [2, 3]
// Producer: buffer FULL, waiting...
// Consumed: 2 | Buffer: [3]
// Produced: 4 | Buffer: [3, 4]
// ...
```

---

### Example 8 — Multiple Conditions

A traffic light system with three separate condition queues — one per signal colour:

```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TrafficLight {

    private enum Signal { RED, GREEN, YELLOW }
    private Signal current = Signal.RED;

    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition redCondition    = lock.newCondition();
    private final Condition greenCondition  = lock.newCondition();
    private final Condition yellowCondition = lock.newCondition();

    public void red() throws InterruptedException {
        lock.lock();
        try {
            while (current != Signal.RED) redCondition.await();
            System.out.println(Thread.currentThread().getName() + ": 🔴 RED   — Stop!");
            Thread.sleep(1000);
            current = Signal.GREEN;
            greenCondition.signal();
        } finally { lock.unlock(); }
    }

    public void green() throws InterruptedException {
        lock.lock();
        try {
            while (current != Signal.GREEN) greenCondition.await();
            System.out.println(Thread.currentThread().getName() + ": 🟢 GREEN  — Go!");
            Thread.sleep(1000);
            current = Signal.YELLOW;
            yellowCondition.signal();
        } finally { lock.unlock(); }
    }

    public void yellow() throws InterruptedException {
        lock.lock();
        try {
            while (current != Signal.YELLOW) yellowCondition.await();
            System.out.println(Thread.currentThread().getName() + ": 🟡 YELLOW — Slow down!");
            Thread.sleep(500);
            current = Signal.RED;
            redCondition.signal();
        } finally { lock.unlock(); }
    }

    public static void main(String[] args) {
        TrafficLight light = new TrafficLight();
        int cycles = 3;

        Thread redThread    = new Thread(() -> {
            try { for (int i = 0; i < cycles; i++) light.red(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "RedThread");

        Thread greenThread  = new Thread(() -> {
            try { for (int i = 0; i < cycles; i++) light.green(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "GreenThread");

        Thread yellowThread = new Thread(() -> {
            try { for (int i = 0; i < cycles; i++) light.yellow(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "YellowThread");

        redThread.start(); greenThread.start(); yellowThread.start();
    }
}

// Output (3 cycles):
// RedThread   : 🔴 RED    — Stop!
// GreenThread : 🟢 GREEN  — Go!
// YellowThread: 🟡 YELLOW — Slow down!
// RedThread   : 🔴 RED    — Stop!
// ...
```

---

### Example 9 — Read-Write Locking with ReentrantReadWriteLock

`ReentrantReadWriteLock` is a specialised variant that allows **multiple readers** or **one writer** at a time — dramatically improving throughput for read-heavy workloads:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteCache {

    private final Map<String, String> cache = new HashMap<>();
    private final ReadWriteLock rwLock      = new ReentrantReadWriteLock();

    // Multiple threads can read simultaneously
    public String get(String key) {
        rwLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()
                    + ": reading key=" + key);
            Thread.sleep(300); // simulate read latency
            return cache.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Only one thread can write at a time; readers are blocked during write
    public void put(String key, String value) {
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()
                    + ": writing key=" + key + " value=" + value);
            Thread.sleep(500); // simulate write latency
            cache.put(key, value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReadWriteCache cache = new ReadWriteCache();

        // Pre-populate
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");

        // Launch multiple readers concurrently
        for (int i = 1; i <= 4; i++) {
            final String key = "user:" + ((i % 2) + 1);
            new Thread(() -> {
                String val = cache.get(key);
                System.out.println(Thread.currentThread().getName()
                        + ": got " + key + " = " + val);
            }, "Reader-" + i).start();
        }

        // Writer runs concurrently — blocks readers while writing
        new Thread(() -> cache.put("user:3", "Charlie"), "Writer-1").start();
    }
}

// Multiple Reader threads run at the SAME time (concurrent reads)
// Writer-1 gets exclusive access — all readers wait while it writes
```

---

### Example 10 — Deadlock Detection with tryLock

Use `tryLock()` to avoid deadlock when acquiring multiple locks:

```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class DeadlockAvoidance {

    private static final ReentrantLock lockA = new ReentrantLock();
    private static final ReentrantLock lockB = new ReentrantLock();

    // ❌ This pattern can DEADLOCK
    static void deadlockProneTask(String name, ReentrantLock first, ReentrantLock second) {
        first.lock();
        try {
            System.out.println(name + ": acquired first lock");
            Thread.sleep(100);
            second.lock(); // may deadlock if other thread holds second
            try {
                System.out.println(name + ": acquired both locks");
            } finally { second.unlock(); }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally { first.unlock(); }
    }

    // ✅ This pattern AVOIDS deadlock using tryLock with timeout
    static void safeTask(String name, ReentrantLock first, ReentrantLock second)
            throws InterruptedException {
        while (true) {
            boolean gotFirst  = false;
            boolean gotSecond = false;
            try {
                gotFirst  = first.tryLock(100, TimeUnit.MILLISECONDS);
                gotSecond = second.tryLock(100, TimeUnit.MILLISECONDS);

                if (gotFirst && gotSecond) {
                    System.out.println(name + ": ✅ acquired both locks — doing work");
                    Thread.sleep(200); // do work
                    return; // success
                } else {
                    System.out.println(name + ": ⚠️ couldn't acquire both — backing off");
                    Thread.sleep(50); // back off before retrying
                }
            } finally {
                if (gotFirst)  first.unlock();
                if (gotSecond) second.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try { safeTask("Thread-A", lockA, lockB); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Thread-A");

        Thread t2 = new Thread(() -> {
            try { safeTask("Thread-B", lockB, lockA); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Thread-B");

        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println("Both threads completed without deadlock!");
    }
}

// Output (may vary):
// Thread-A: ✅ acquired both locks — doing work
// Thread-B: ⚠️ couldn't acquire both — backing off
// Thread-B: ✅ acquired both locks — doing work
// Both threads completed without deadlock!
```

---

## ReentrantLock vs `synchronized`

| Feature | `ReentrantLock` | `synchronized` |
|---|---|---|
| **Reentrancy** | ✅ Yes | ✅ Yes |
| **Fairness control** | ✅ `new ReentrantLock(true)` | ❌ No |
| **`tryLock()` (non-blocking)** | ✅ Yes | ❌ No |
| **Timed lock attempt** | ✅ `tryLock(timeout)` | ❌ No |
| **Interruptible wait** | ✅ `lockInterruptibly()` | ❌ No |
| **Multiple condition queues** | ✅ `newCondition()` (many) | ❌ One per object |
| **Lock introspection** | ✅ `getHoldCount()`, `isLocked()` | ❌ No |
| **Must manually unlock** | ✅ Yes (in `finally`) | ❌ Auto-released |
| **Scope** | Explicit — spans methods/classes | Bound to block/method |
| **Performance** | Comparable (JVM optimises both) | Comparable |
| **Code verbosity** | More verbose | Concise |
| **Risk of forgetting unlock** | ⚠️ Yes | ❌ No |
| **Best for** | Complex synchronization, cancellation | Simple critical sections |

---

## ReentrantLock vs `Semaphore`

| Feature | `ReentrantLock` | `Semaphore` |
|---|---|---|
| **Permits / holders** | 1 (exclusive) | N (counting) |
| **Reentrancy** | ✅ Yes | ❌ No |
| **Release by different thread** | ❌ No (same thread only) | ✅ Yes |
| **Condition variables** | ✅ `newCondition()` | ❌ No |
| **`tryLock` / `tryAcquire`** | ✅ Yes | ✅ Yes |
| **Fairness** | ✅ Yes | ✅ Yes |
| **Introspection** | ✅ Rich | ✅ Moderate |
| **Best for** | Exclusive critical sections | Resource pools, rate limiting |

---

## Common Pitfalls

### 1. Forgetting to Unlock — Deadlock

```java
// ❌ If an exception occurs, unlock is never called — all other threads wait forever
lock.lock();
riskyOperation(); // throws RuntimeException
lock.unlock();    // NEVER REACHED

// ✅ Always unlock in finally
lock.lock();
try {
    riskyOperation();
} finally {
    lock.unlock(); // GUARANTEED to execute
}
```

### 2. Unlocking Without Holding — Exception

```java
// ❌ Unlocking a lock you don't hold
ReentrantLock lock = new ReentrantLock();
lock.unlock(); // throws IllegalMonitorStateException

// ❌ Unlocking more times than acquired
lock.lock();
lock.unlock();
lock.unlock(); // throws IllegalMonitorStateException

// ✅ Match every lock() with exactly one unlock()
lock.lock();
try { /* work */ } finally { lock.unlock(); }
```

### 3. Acquiring in try — Unlock Without Acquire

```java
// ❌ If lock() throws (e.g. lockInterruptibly()), finally still runs
try {
    lock.lock(); // should be BEFORE try
    // ...
} finally {
    lock.unlock(); // called even if lock() never succeeded
}

// ✅ lock() before the try block
lock.lock();
try {
    // ...
} finally {
    lock.unlock();
}
```

### 4. Forgetting `while` Loop with Conditions

```java
Condition condition = lock.newCondition();

// ❌ Using if — susceptible to spurious wakeups
lock.lock();
try {
    if (!conditionMet()) {      // wrong: spurious wakeup bypasses check
        condition.await();
    }
    // condition may NOT actually be met here
} finally { lock.unlock(); }

// ✅ Always use while — re-checks condition after every wakeup
lock.lock();
try {
    while (!conditionMet()) {   // correct: loops until actually satisfied
        condition.await();
    }
    // condition IS guaranteed to be met here
} finally { lock.unlock(); }
```

### 5. Calling await() Without Holding the Lock

```java
ReentrantLock lock = new ReentrantLock();
Condition condition = lock.newCondition();

// ❌ await() called without holding the lock
condition.await(); // throws IllegalMonitorStateException

// ✅ Must hold the lock before calling await()
lock.lock();
try {
    condition.await(); // correct
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
} finally {
    lock.unlock();
}
```

---

## Summary

| Concept | Key Point |
|---|---|
| **What it is** | A reentrant mutual exclusion lock with advanced features over `synchronized` |
| **Reentrancy** | Same thread can acquire the lock multiple times; hold count tracks depth |
| **Must unlock in `finally`** | Every `lock()` must be paired with `unlock()` in a `finally` block |
| **`tryLock()`** | Non-blocking acquisition; returns `false` instead of blocking |
| **`tryLock(timeout)`** | Waits up to a time limit before giving up |
| **`lockInterruptibly()`** | Waiting thread can be cancelled via `Thread.interrupt()` |
| **Fairness** | `new ReentrantLock(true)` ensures FIFO ordering, prevents starvation |
| **Conditions** | `newCondition()` creates per-condition wait queues; use `while` not `if` |
| **Multiple conditions** | Key advantage over `synchronized` — separate queues per state |
| **Read-Write variant** | `ReentrantReadWriteLock` — concurrent reads, exclusive writes |
| **Deadlock avoidance** | Use `tryLock(timeout)` when acquiring multiple locks |

**Choosing the right tool:**

| Scenario | Recommended |
|---|---|
| Simple mutual exclusion | `synchronized` (simpler, less error-prone) |
| Need timeout / cancellation | `ReentrantLock` with `tryLock()` / `lockInterruptibly()` |
| Multiple wait conditions | `ReentrantLock` with multiple `Condition` objects |
| Read-heavy shared data | `ReentrantReadWriteLock` |
| Prevent thread starvation | `ReentrantLock(true)` (fair mode) |
| Limiting concurrent access | `Semaphore` |

> 💡 **Best Practice:** Prefer `synchronized` for simple cases — it is less error-prone because the JVM auto-releases it. Reach for `ReentrantLock` only when you need its extra capabilities: timed waits, interruptibility, multiple conditions, or fairness control.