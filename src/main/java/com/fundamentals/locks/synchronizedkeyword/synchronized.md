# The `synchronized` Keyword in Java

## Table of Contents
- [Introduction](#introduction)
- [The Problems `synchronized` Solves](#the-problems-synchronized-solves)
    - [Problem 1 — Race Conditions](#problem-1--race-conditions)
    - [Problem 2 — Memory Visibility](#problem-2--memory-visibility)
    - [Problem 3 — Instruction Reordering](#problem-3--instruction-reordering)
- [How `synchronized` Works](#how-synchronized-works)
    - [The Analogy](#the-analogy)
    - [The Monitor Lock (Intrinsic Lock)](#the-monitor-lock-intrinsic-lock)
    - [The Happens-Before Guarantee](#the-happens-before-guarantee)
- [Four Forms of `synchronized`](#four-forms-of-synchronized)
    - [1. Synchronized Instance Method](#1-synchronized-instance-method)
    - [2. Synchronized Static Method](#2-synchronized-static-method)
    - [3. Synchronized Block on `this`](#3-synchronized-block-on-this)
    - [4. Synchronized Block on an Explicit Object](#4-synchronized-block-on-an-explicit-object)
- [What `synchronized` Guarantees](#what-synchronized-guarantees)
- [What `synchronized` Does NOT Guarantee](#what-synchronized-does-not-guarantee)
- [Object Monitor and `wait` / `notify`](#object-monitor-and-wait--notify)
    - [Monitor Methods](#monitor-methods)
    - [The Wait-Notify Pattern](#the-wait-notify-pattern)
- [Reentrancy](#reentrancy)
- [Examples](#examples)
    - [Example 1 — Race Condition Without `synchronized`](#example-1--race-condition-without-synchronized)
    - [Example 2 — Race Condition Fixed with `synchronized` Method](#example-2--race-condition-fixed-with-synchronized-method)
    - [Example 3 — Synchronized Block (Finer Granularity)](#example-3--synchronized-block-finer-granularity)
    - [Example 4 — Synchronized Static Method (Class-Level Lock)](#example-4--synchronized-static-method-class-level-lock)
    - [Example 5 — Multiple Lock Objects (Independent Locks)](#example-5--multiple-lock-objects-independent-locks)
    - [Example 6 — Reentrancy in Action](#example-6--reentrancy-in-action)
    - [Example 7 — wait() and notify() (Producer-Consumer)](#example-7--wait-and-notify-producerconsumer)
    - [Example 8 — wait() and notifyAll()](#example-8--wait-and-notifyall)
    - [Example 9 — Deadlock Demonstration and Prevention](#example-9--deadlock-demonstration-and-prevention)
    - [Example 10 — Thread-Safe Singleton with `synchronized`](#example-10--thread-safe-singleton-with-synchronized)
    - [Example 11 — Synchronized Collection Wrapper](#example-11--synchronized-collection-wrapper)
    - [Example 12 — Bank Transfer with Ordered Locking](#example-12--bank-transfer-with-ordered-locking)
- [Lock Scope Comparison](#lock-scope-comparison)
- [`synchronized` vs `volatile`](#synchronized-vs-volatile)
- [`synchronized` vs `ReentrantLock`](#synchronized-vs-reentrantlock)
- [JVM Optimisations on `synchronized`](#jvm-optimisations-on-synchronized)
- [Common Pitfalls](#common-pitfalls)
- [When to Use `synchronized`](#when-to-use-synchronized)
- [Summary](#summary)

---

## Introduction

The `synchronized` keyword is Java's built-in mechanism for **mutual exclusion** and **memory visibility** in multithreaded programs. It has been part of Java since version 1.0 and remains the most fundamental concurrency tool in the language.

When a thread enters a `synchronized` block or method, it acquires the **intrinsic lock** (also called a **monitor lock**) of a specific object. While that lock is held, no other thread can enter any `synchronized` block or method guarded by the **same** lock object. When the thread exits the block, the lock is automatically released — even if an exception is thrown.

`synchronized` provides three critical guarantees at once:
- **Mutual Exclusion** — only one thread executes the critical section at a time
- **Visibility** — all writes made inside the block are visible to any thread that subsequently enters a `synchronized` block on the same lock
- **Ordering** — prevents instruction reordering across the lock boundary

---

## The Problems `synchronized` Solves

### Problem 1 — Race Conditions

A **race condition** occurs when two or more threads access shared mutable data concurrently and the correctness of the result depends on the exact timing of their execution.

```
Race condition on counter++:

Initial: counter = 0

Thread-A: reads counter  → 0
                              Thread-B: reads counter  → 0  (same stale value!)
Thread-A: counter = 0 + 1 = 1
                              Thread-B: counter = 0 + 1 = 1  (overwrites A's result!)

Expected: counter = 2
Actual:   counter = 1  ❌  (lost update)
```

`synchronized` prevents this by ensuring Thread-B cannot read `counter` until Thread-A has completed its full read-modify-write cycle.

### Problem 2 — Memory Visibility

Without synchronization, a thread running on one CPU core may cache a variable's value in its local cache. Other threads may read a **stale** value from their own caches, not seeing recent writes.

```
Without synchronization:
  Thread-A writes data on Core 1 → cached locally, NOT flushed to main memory
  Thread-B reads data on Core 2  → reads its own stale cache ❌

With synchronized:
  Thread-A exits synchronized block → ALL writes flushed to main memory ✅
  Thread-B enters synchronized block → reads FRESH values from main memory ✅
```

### Problem 3 — Instruction Reordering

The JVM and CPU can reorder instructions for performance. This is safe within a single thread but can break multithreaded correctness. `synchronized` inserts memory barriers that prevent reordering across the lock boundary.

---

## How `synchronized` Works

### The Analogy

Think of `synchronized` as a **single-key room** with a sign-in board:

```
The Synchronized Room (Monitor)
┌────────────────────────────────────────────────────────┐
│                                                        │
│  Key: currently held by Thread-A                       │
│  Task: modifying shared account balance                │
│                                                        │
│  Queue outside the door: [Thread-B, Thread-C, Thread-D]│
└────────────────────────────────────────────────────────┘

Rules:
  1. Only one thread holds the key at a time
  2. Thread-B, C, D wait in the BLOCKED state until Thread-A is done
  3. Thread-A exits → drops the key → Thread-B picks it up
  4. On entry: all cached data is refreshed from main memory
  5. On exit:  all written data is flushed to main memory
```

### The Monitor Lock (Intrinsic Lock)

In Java, **every object** has an associated intrinsic lock (monitor). The `synchronized` keyword uses this lock:

```
Every Java object:
┌─────────────────────────────────┐
│  Object Header                  │
│  ┌───────────────────────────┐  │
│  │  Mark Word                │  │
│  │  - hashCode               │  │
│  │  - GC info                │  │
│  │  - lock state ←────────── │  │  ← tracks who holds the monitor
│  └───────────────────────────┘  │
│  Object fields...               │
└─────────────────────────────────┘

Lock States (JVM internal):
  Unlocked       → any thread can acquire
  Biased         → JVM optimisation: lock is "biased" to one thread
  Thin Lock      → lightweight CAS-based lock for low contention
  Fat Lock       → full OS mutex — used when contention is detected
```

### The Happens-Before Guarantee

The Java Memory Model defines a strict happens-before relationship for `synchronized`:

> **An unlock of a monitor M *happens-before* every subsequent lock of M.**

This means: everything Thread-A did **inside** the synchronized block is guaranteed to be **visible** to Thread-B when Thread-B enters a synchronized block on the **same** lock object.

```
Thread-A:                             Thread-B:
  synchronized(lock) {
    a = 10;  (non-volatile write)
    b = 20;  (non-volatile write)
  }  ← UNLOCK ─────────────────────► LOCK (enters synchronized)
                                          reads a → guaranteed 10 ✅
                                          reads b → guaranteed 20 ✅
                                      }  ← UNLOCK
```

---

## Four Forms of `synchronized`

### 1. Synchronized Instance Method

Acquires the intrinsic lock on **`this`** — the instance the method is called on.

```java
public class Counter {
    private int count = 0;

    // Lock object: the Counter instance (this)
    public synchronized void increment() {
        count++; // only one thread at a time per Counter instance
    }

    public synchronized int getCount() {
        return count;
    }
}
```

```
Two Counter instances — independent locks:
  counterA.increment() and counterB.increment() can run SIMULTANEOUSLY ✅
  (different lock objects)

Same Counter instance — shared lock:
  Thread-A calls counterA.increment()
  Thread-B calls counterA.increment() → BLOCKED until Thread-A is done ✅
```

### 2. Synchronized Static Method

Acquires the intrinsic lock on the **`Class` object** — shared across ALL instances of the class.

```java
public class IdGenerator {
    private static int nextId = 0;

    // Lock object: IdGenerator.class (not 'this')
    public static synchronized int generateId() {
        return nextId++; // safe — class-level lock
    }
}
```

```
Class-level lock scope:
  Thread-A calls IdGenerator.generateId()
  Thread-B calls IdGenerator.generateId() → BLOCKED (same class lock) ✅

  ⚠️ Instance method lock and static method lock are DIFFERENT:
  Thread-A calls instance.instanceMethod()  (lock: this)
  Thread-B calls ClassName.staticMethod()   (lock: ClassName.class)
  → These do NOT block each other ❌
```

### 3. Synchronized Block on `this`

Acquires the lock on `this` but only for the duration of the block — not the whole method. Gives **finer granularity** than a synchronized method.

```java
public class DataProcessor {
    private int result = 0;

    public void process(int[] data) {
        // Non-synchronized work (no shared state — safe to run in parallel)
        int localSum = 0;
        for (int d : data) {
            localSum += d * 2; // purely local computation
        }

        // Synchronized block — only the write to shared state is protected
        synchronized (this) {
            result += localSum; // shared state — needs protection
        }
    }
}
```

### 4. Synchronized Block on an Explicit Object

Acquires the lock on any specified object — allows **multiple independent locks** in one class.

```java
public class TwoResourceManager {

    private final Object lockA = new Object(); // dedicated lock for resourceA
    private final Object lockB = new Object(); // dedicated lock for resourceB

    private int resourceA = 0;
    private int resourceB = 0;

    public void updateA(int value) {
        synchronized (lockA) { // only locks resourceA
            resourceA += value;
        }
    }

    public void updateB(int value) {
        synchronized (lockB) { // only locks resourceB
            resourceB += value;
        }
    }

    // Thread-A can update resourceA while Thread-B updates resourceB SIMULTANEOUSLY ✅
}
```

---

## What `synchronized` Guarantees

| Guarantee | Description |
|---|---|
| **Mutual Exclusion** | Only one thread executes the synchronized block at a time |
| **Visibility (entry)** | On entering a synchronized block, the thread reads fresh values from main memory |
| **Visibility (exit)** | On exiting, all writes are flushed to main memory |
| **Happens-Before** | Everything before an unlock is visible to any thread that acquires the same lock |
| **Atomicity of block** | The entire synchronized block executes as a single unit from other threads' perspective |
| **Reentrancy** | A thread that already holds the lock can re-enter synchronized blocks on the same object |
| **Ordering** | No reordering of instructions across the lock boundary |
| **Auto-release** | Lock is always released on exit — even if an exception is thrown |

---

## What `synchronized` Does NOT Guarantee

| Not Guaranteed | Why | Solution |
|---|---|---|
| **Starvation prevention** | Threads may wait indefinitely if others keep acquiring the lock | `ReentrantLock(true)` (fair) |
| **Deadlock prevention** | Two threads waiting for each other's lock | Consistent lock ordering |
| **Timed lock attempts** | Cannot try to acquire with a timeout | `ReentrantLock.tryLock(timeout)` |
| **Interruptible waiting** | Thread blocked on `synchronized` cannot be interrupted | `ReentrantLock.lockInterruptibly()` |
| **Multiple wait queues** | Only one wait set per object monitor | `ReentrantLock.newCondition()` |
| **Lock introspection** | Cannot query who holds the lock, queue length, etc. | `ReentrantLock.getHoldCount()`, etc. |

---

## Object Monitor and `wait` / `notify`

Every object's intrinsic lock comes with a **wait set** — a set of threads that have voluntarily released the lock and are waiting to be notified. This is Java's built-in condition variable mechanism.

### Monitor Methods

All three methods must be called from a `synchronized` block on the same object:

| Method | Description |
|---|---|
| `wait()` | Releases the lock and suspends the current thread; re-acquires on `notify` |
| `wait(long millis)` | Like `wait()` but wakes up after the timeout even if not notified |
| `wait(long millis, int nanos)` | Like `wait(millis)` with nanosecond precision |
| `notify()` | Wakes one arbitrary thread from the wait set |
| `notifyAll()` | Wakes all threads from the wait set |

> ⚠️ **Critical Rules:**
> - `wait()`, `notify()`, and `notifyAll()` **must** be called from a `synchronized` block on the same object. Calling them outside throws `IllegalMonitorStateException`.
> - Always call `wait()` inside a **`while` loop** — not an `if` — to guard against spurious wakeups.

### The Wait-Notify Pattern

```
Monitor Wait Set:

synchronized(lock) {
    while (!condition) {     ← ALWAYS use while, not if
        lock.wait();         ← atomically: release lock + suspend thread
    }                        ← re-check condition on wakeup (spurious wakeup guard)
    // condition is now true
}

synchronized(lock) {
    // change condition to true
    lock.notify();           ← wake one thread from wait set
    // or lock.notifyAll();  ← wake ALL threads
}

Thread state transitions:
  RUNNABLE → wait() → WAITING → notify() → BLOCKED → lock acquired → RUNNABLE
```

---

## Reentrancy

`synchronized` is **reentrant** — a thread that already holds an object's lock can re-enter `synchronized` blocks on the same object without deadlocking.

```java
public class ReentrantDemo {

    public synchronized void outer() {
        System.out.println("outer() — hold count increases");
        inner(); // calling another synchronized method on same object
    }

    public synchronized void inner() {
        // ✅ Same thread — re-enters without blocking
        System.out.println("inner() — re-entrant lock acquisition");
    }
}
```

```
Thread-A calls outer():
  Acquires lock on ReentrantDemo instance (count: 1)
  Calls inner() — same lock, same thread
  Acquires lock again (count: 2)  ← no deadlock!
  inner() exits → count: 1
  outer() exits → count: 0 → lock released
```

---

## Examples

### Example 1 — Race Condition Without `synchronized`

```java
public class RaceConditionDemo {

    private int counter = 0; // shared mutable state

    public void increment() {
        counter++; // ❌ NOT atomic: read → increment → write
    }

    public static void main(String[] args) throws InterruptedException {
        RaceConditionDemo demo = new RaceConditionDemo();
        int THREADS    = 10;
        int INCREMENTS = 10_000;

        Runnable task = () -> {
            for (int i = 0; i < INCREMENTS; i++) {
                demo.increment();
            }
        };

        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(task, "Thread-" + i);
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("Expected : " + (THREADS * INCREMENTS));
        System.out.println("Actual   : " + demo.counter + "  ← lost updates due to race!");
    }
}

// Sample Output:
// Expected : 100000
// Actual   : 73412  ❌  (varies every run — data race)
```

---

### Example 2 — Race Condition Fixed with `synchronized` Method

```java
public class SynchronizedCounter {

    private int count = 0;

    // ✅ synchronized — only one thread at a time
    public synchronized void increment() {
        count++;
    }

    public synchronized void decrement() {
        count--;
    }

    public synchronized int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        int THREADS    = 10;
        int OPERATIONS = 10_000;

        // Incrementers
        Thread[] incrementers = new Thread[THREADS / 2];
        for (int i = 0; i < THREADS / 2; i++) {
            incrementers[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS; j++) counter.increment();
            }, "Incrementer-" + i);
        }

        // Decrementers
        Thread[] decrementers = new Thread[THREADS / 2];
        for (int i = 0; i < THREADS / 2; i++) {
            decrementers[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS; j++) counter.decrement();
            }, "Decrementer-" + i);
        }

        for (Thread t : incrementers) t.start();
        for (Thread t : decrementers) t.start();
        for (Thread t : incrementers) t.join();
        for (Thread t : decrementers) t.join();

        // Equal increments and decrements → always 0
        System.out.println("Final count: " + counter.getCount()); // always 0 ✅
    }
}
```

---

### Example 3 — Synchronized Block (Finer Granularity)

Using a synchronized block instead of a method to minimize the time the lock is held:

```java
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {

    private final List<String> orders = new ArrayList<>();
    private int totalProcessed = 0;
    private final Object lock  = new Object(); // explicit lock object

    public void processOrder(String orderId) {
        // ── Non-synchronized work (expensive but thread-safe — local only) ──
        String processed = validate(orderId);   // pure local computation
        String enriched  = enrich(processed);   // pure local computation

        // ── Synchronized block — only protects shared state modification ──────
        synchronized (lock) {
            orders.add(enriched);
            totalProcessed++;
            System.out.printf("[%s] Order %s processed | total=%d%n",
                    Thread.currentThread().getName(), enriched, totalProcessed);
        }
        // Lock released here — other threads can now add their results
    }

    private String validate(String id) {
        try { Thread.sleep(50); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "VALID-" + id;
    }

    private String enrich(String id) {
        return id + "-ENRICHED";
    }

    public int getTotalProcessed() {
        synchronized (lock) { return totalProcessed; }
    }

    public static void main(String[] args) throws InterruptedException {
        OrderProcessor processor = new OrderProcessor();

        Thread[] workers = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final String orderId = "ORD-" + String.format("%03d", i + 1);
            workers[i] = new Thread(() -> processor.processOrder(orderId),
                    "Worker-" + (i + 1));
        }

        for (Thread w : workers) w.start();
        for (Thread w : workers) w.join();

        System.out.println("Total processed: " + processor.getTotalProcessed());
    }
}
```

---

### Example 4 — Synchronized Static Method (Class-Level Lock)

```java
public class SequenceGenerator {

    private static int nextId   = 1;
    private static int totalGenerated = 0;

    // Class-level lock — shared across ALL instances and threads
    public static synchronized int nextId() {
        int id = nextId++;
        totalGenerated++;
        System.out.printf("[%s] Generated ID: %d%n",
                Thread.currentThread().getName(), id);
        return id;
    }

    public static synchronized int getTotalGenerated() {
        return totalGenerated;
    }

    // ⚠️ Instance method — lock is 'this', NOT SequenceGenerator.class
    //    Does NOT conflict with the static synchronized method above
    public synchronized void doInstanceWork() {
        System.out.println("Instance work by " + Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        int[] ids = new int[5];

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                ids[idx] = SequenceGenerator.nextId();
            }, "Thread-" + (i + 1));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("Total generated: " + SequenceGenerator.getTotalGenerated());

        // All IDs are unique — guaranteed by class-level lock
        java.util.Arrays.sort(ids);
        System.out.println("IDs: " + java.util.Arrays.toString(ids));
    }
}

// Output:
// [Thread-1] Generated ID: 1
// [Thread-3] Generated ID: 2
// [Thread-2] Generated ID: 3
// [Thread-4] Generated ID: 4
// [Thread-5] Generated ID: 5
// Total generated: 5
// IDs: [1, 2, 3, 4, 5]  ← always unique, always consecutive
```

---

### Example 5 — Multiple Lock Objects (Independent Locks)

Two independent resources, each with its own lock — maximum concurrency:

```java
public class DualResourceManager {

    private int accountBalance = 1000;
    private int inventoryCount = 500;

    // Separate dedicated locks for each independent resource
    private final Object balanceLock   = new Object();
    private final Object inventoryLock = new Object();

    public void debitAccount(int amount) {
        synchronized (balanceLock) {   // only locks balance operations
            if (accountBalance >= amount) {
                accountBalance -= amount;
                System.out.printf("[%s] Debit %-4d | balance=%d%n",
                        Thread.currentThread().getName(), amount, accountBalance);
            }
        }
    }

    public void creditAccount(int amount) {
        synchronized (balanceLock) {
            accountBalance += amount;
            System.out.printf("[%s] Credit %-4d | balance=%d%n",
                    Thread.currentThread().getName(), amount, accountBalance);
        }
    }

    public void reduceInventory(int qty) {
        synchronized (inventoryLock) { // only locks inventory operations
            if (inventoryCount >= qty) {
                inventoryCount -= qty;
                System.out.printf("[%s] Reduce inv %-3d | inventory=%d%n",
                        Thread.currentThread().getName(), qty, inventoryCount);
            }
        }
    }

    public void restockInventory(int qty) {
        synchronized (inventoryLock) {
            inventoryCount += qty;
            System.out.printf("[%s] Restock %-4d | inventory=%d%n",
                    Thread.currentThread().getName(), qty, inventoryCount);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DualResourceManager mgr = new DualResourceManager();

        // These threads can run SIMULTANEOUSLY — different locks!
        Thread t1 = new Thread(() -> mgr.debitAccount(100),    "AccountThread-1");
        Thread t2 = new Thread(() -> mgr.creditAccount(50),    "AccountThread-2");
        Thread t3 = new Thread(() -> mgr.reduceInventory(30),  "InventoryThread-1");
        Thread t4 = new Thread(() -> mgr.restockInventory(20), "InventoryThread-2");

        t1.start(); t2.start(); t3.start(); t4.start();
        t1.join();  t2.join();  t3.join();  t4.join();

        System.out.println("Final balance: "   + mgr.accountBalance);
        System.out.println("Final inventory: " + mgr.inventoryCount);
    }
}
```

---

### Example 6 — Reentrancy in Action

A thread re-acquiring its own lock through chained synchronized calls:

```java
public class ReentrantSynchronized {

    private int balance;

    public ReentrantSynchronized(int balance) {
        this.balance = balance;
    }

    // Synchronized method that calls another synchronized method on the same object
    public synchronized boolean transfer(ReentrantSynchronized target, int amount) {
        System.out.printf("[%s] transfer() — acquired lock on %s%n",
                Thread.currentThread().getName(), this);

        if (hasSufficientFunds(amount)) { // calls another synchronized method — reentrant ✅
            this.balance -= amount;
            target.deposit(amount);        // acquires DIFFERENT object's lock — ok ✅
            System.out.printf("[%s] Transferred %d | from balance=%d%n",
                    Thread.currentThread().getName(), amount, this.balance);
            return true;
        }
        return false;
    }

    // Same lock (this) — re-entered by transfer() above
    public synchronized boolean hasSufficientFunds(int amount) {
        System.out.printf("[%s] hasSufficientFunds() — re-entrant lock on same object%n",
                Thread.currentThread().getName());
        return balance >= amount; // same thread re-acquires lock — no deadlock ✅
    }

    public synchronized void deposit(int amount) {
        balance += amount;
        System.out.printf("[%s] Deposited %d | to balance=%d%n",
                Thread.currentThread().getName(), amount, this.balance);
    }

    public synchronized int getBalance() { return balance; }

    public static void main(String[] args) throws InterruptedException {
        ReentrantSynchronized alice = new ReentrantSynchronized(1000);
        ReentrantSynchronized bob   = new ReentrantSynchronized(500);

        Thread t1 = new Thread(() -> alice.transfer(bob, 300), "Thread-A");
        Thread t2 = new Thread(() -> bob.transfer(alice, 100), "Thread-B");

        t1.start(); t2.start();
        t1.join();  t2.join();

        System.out.println("Alice balance: " + alice.getBalance());
        System.out.println("Bob   balance: " + bob.getBalance());
    }
}
```

---

### Example 7 — wait() and notify() (Producer-Consumer)

Classic producer-consumer implemented with `wait()` and `notify()`:

```java
import java.util.LinkedList;
import java.util.Queue;

public class WaitNotifyProducerConsumer {

    private final Queue<Integer> buffer   = new LinkedList<>();
    private final int            CAPACITY = 3;
    private final Object         lock     = new Object();

    class Producer implements Runnable {
        @Override
        public void run() {
            for (int item = 1; item <= 8; item++) {
                synchronized (lock) {
                    // ✅ ALWAYS use while — not if — to guard spurious wakeups
                    while (buffer.size() == CAPACITY) {
                        System.out.println("Producer: buffer FULL — waiting");
                        try { lock.wait(); } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    buffer.offer(item);
                    System.out.printf("Produced: %-2d | buffer: %s%n", item, buffer);
                    lock.notifyAll(); // wake waiting consumers
                }
                try { Thread.sleep(150); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    class Consumer implements Runnable {
        private final String name;
        Consumer(String name) { this.name = name; }

        @Override
        public void run() {
            for (int i = 0; i < 4; i++) {
                synchronized (lock) {
                    while (buffer.isEmpty()) {
                        System.out.println(name + ": buffer EMPTY — waiting");
                        try { lock.wait(); } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    int item = buffer.poll();
                    System.out.printf("%s consumed: %-2d | buffer: %s%n", name, item, buffer);
                    lock.notifyAll(); // wake waiting producer
                }
                try { Thread.sleep(400); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WaitNotifyProducerConsumer wn = new WaitNotifyProducerConsumer();

        Thread producer  = new Thread(wn.new Producer(),          "Producer");
        Thread consumer1 = new Thread(wn.new Consumer("Consumer-1"), "Consumer-1");
        Thread consumer2 = new Thread(wn.new Consumer("Consumer-2"), "Consumer-2");

        producer.start(); consumer1.start(); consumer2.start();
        producer.join();  consumer1.join();  consumer2.join();

        System.out.println("All done. Final buffer: " + wn.buffer);
    }
}

// Sample Output:
// Produced: 1  | buffer: [1]
// Consumer-1 consumed: 1  | buffer: []
// Produced: 2  | buffer: [2]
// Produced: 3  | buffer: [2, 3]
// Consumer-2 consumed: 2  | buffer: [3]
// ...
```

---

### Example 8 — wait() and notifyAll()

Demonstrating why `notifyAll()` is often safer than `notify()` — wakes all waiting threads so each can check its condition:

```java
public class NotifyAllDemo {

    private final Object lock   = new Object();
    private String       phase  = "INIT";

    // Multiple workers each waiting for a specific phase
    class PhaseWorker implements Runnable {
        private final String waitForPhase;
        private final String name;

        PhaseWorker(String name, String waitForPhase) {
            this.name         = name;
            this.waitForPhase = waitForPhase;
        }

        @Override
        public void run() {
            synchronized (lock) {
                // Wait until our specific phase is active
                while (!phase.equals(waitForPhase)) {
                    System.out.println(name + ": waiting for phase=" + waitForPhase
                            + " (current=" + phase + ")");
                    try { lock.wait(); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println(name + ": ✅ running in phase=" + phase);
            }
        }
    }

    public void advanceTo(String newPhase) throws InterruptedException {
        Thread.sleep(300);
        synchronized (lock) {
            System.out.println("\n>>> Advancing to phase: " + newPhase);
            phase = newPhase;
            lock.notifyAll(); // wake ALL workers — each checks its own condition
            // notify() would only wake ONE — others might never run!
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NotifyAllDemo demo = new NotifyAllDemo();

        Thread w1 = new Thread(demo.new PhaseWorker("Worker-A", "LOADING"),  "Worker-A");
        Thread w2 = new Thread(demo.new PhaseWorker("Worker-B", "PROCESSING"),"Worker-B");
        Thread w3 = new Thread(demo.new PhaseWorker("Worker-C", "LOADING"),  "Worker-C");
        Thread w4 = new Thread(demo.new PhaseWorker("Worker-D", "SAVING"),   "Worker-D");

        w1.start(); w2.start(); w3.start(); w4.start();
        Thread.sleep(100);

        demo.advanceTo("LOADING");    // wakes Worker-A and Worker-C
        Thread.sleep(200);
        demo.advanceTo("PROCESSING"); // wakes Worker-B
        Thread.sleep(200);
        demo.advanceTo("SAVING");     // wakes Worker-D

        w1.join(); w2.join(); w3.join(); w4.join();
    }
}

// Output:
// >>> Advancing to phase: LOADING
// Worker-A: ✅ running in phase=LOADING
// Worker-C: ✅ running in phase=LOADING
// >>> Advancing to phase: PROCESSING
// Worker-B: ✅ running in phase=PROCESSING
// >>> Advancing to phase: SAVING
// Worker-D: ✅ running in phase=SAVING
```

---

### Example 9 — Deadlock Demonstration and Prevention

```java
public class DeadlockDemo {

    private final Object lockA = new Object();
    private final Object lockB = new Object();

    // ❌ DEADLOCK — Thread-1 holds A, waits for B
    //               Thread-2 holds B, waits for A
    void deadlockProneTransfer_Thread1() {
        synchronized (lockA) {
            System.out.println("Thread-1: holding lockA, waiting for lockB...");
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (lockB) { // BLOCKS — Thread-2 holds lockB
                System.out.println("Thread-1: acquired both locks"); // never reached
            }
        }
    }

    void deadlockProneTransfer_Thread2() {
        synchronized (lockB) {
            System.out.println("Thread-2: holding lockB, waiting for lockA...");
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (lockA) { // BLOCKS — Thread-1 holds lockA
                System.out.println("Thread-2: acquired both locks"); // never reached
            }
        }
    }

    // ✅ PREVENTION — always acquire locks in the SAME ORDER (lockA before lockB)
    void safeOperation_Thread1() {
        synchronized (lockA) {          // consistent order: A → B
            synchronized (lockB) {
                System.out.println("SafeThread-1: ✅ acquired lockA then lockB");
            }
        }
    }

    void safeOperation_Thread2() {
        synchronized (lockA) {          // consistent order: A → B (same as Thread-1)
            synchronized (lockB) {
                System.out.println("SafeThread-2: ✅ acquired lockA then lockB");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DeadlockDemo demo = new DeadlockDemo();

        System.out.println("=== Safe version (consistent lock order) ===");
        Thread s1 = new Thread(demo::safeOperation_Thread1, "SafeThread-1");
        Thread s2 = new Thread(demo::safeOperation_Thread2, "SafeThread-2");
        s1.start(); s2.start();
        s1.join();  s2.join();
        System.out.println("Safe version completed successfully.\n");

        // Uncomment below to demonstrate the deadlock (will hang!):
        // System.out.println("=== Deadlock-prone version ===");
        // Thread d1 = new Thread(demo::deadlockProneTransfer_Thread1, "Thread-1");
        // Thread d2 = new Thread(demo::deadlockProneTransfer_Thread2, "Thread-2");
        // d1.start(); d2.start(); // DEADLOCK — will hang indefinitely
    }
}

// Output:
// === Safe version (consistent lock order) ===
// SafeThread-1: ✅ acquired lockA then lockB
// SafeThread-2: ✅ acquired lockA then lockB
// Safe version completed successfully.
```

---

### Example 10 — Thread-Safe Singleton with `synchronized`

```java
public class ThreadSafeSingleton {

    // ─── Approach 1: Synchronized method (simple but slowest) ────────────────
    private static ThreadSafeSingleton methodInstance;

    public static synchronized ThreadSafeSingleton getMethodInstance() {
        if (methodInstance == null) {
            methodInstance = new ThreadSafeSingleton("method-sync");
        }
        return methodInstance;
    }

    // ─── Approach 2: Double-checked locking with volatile (recommended) ───────
    private static volatile ThreadSafeSingleton dclInstance;

    public static ThreadSafeSingleton getDCLInstance() {
        if (dclInstance == null) {                  // first check (no lock — fast)
            synchronized (ThreadSafeSingleton.class) {
                if (dclInstance == null) {          // second check (inside lock)
                    dclInstance = new ThreadSafeSingleton("double-checked");
                }
            }
        }
        return dclInstance;
    }

    // ─── Approach 3: Initialization-on-demand holder (best — lazy + thread-safe) ─
    private static class Holder {
        // JVM guarantees class loading is thread-safe
        static final ThreadSafeSingleton INSTANCE =
                new ThreadSafeSingleton("holder");
    }

    public static ThreadSafeSingleton getHolderInstance() {
        return Holder.INSTANCE; // no synchronization needed — JVM handles it
    }

    private final String name;

    private ThreadSafeSingleton(String name) {
        this.name = name;
        System.out.println("Singleton[" + name + "] created by: "
                + Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Double-Checked Locking ===");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                ThreadSafeSingleton s = getDCLInstance();
                System.out.println(Thread.currentThread().getName()
                        + " → " + s.name);
            }, "Thread-" + (i + 1));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("\n=== Holder Pattern ===");
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                ThreadSafeSingleton s = getHolderInstance();
                System.out.println(Thread.currentThread().getName()
                        + " → " + s.name);
            }, "HThread-" + (i + 1)).start();
        }
    }
}
```

---

### Example 11 — Synchronized Collection Wrapper

Building a thread-safe list wrapper that uses `synchronized` blocks:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class SynchronizedList<T> {

    private final List<T>   items = new ArrayList<>();
    private final Object    lock  = new Object();

    public void add(T item) {
        synchronized (lock) {
            items.add(item);
            System.out.printf("[%s] Added: %-10s | size=%d%n",
                    Thread.currentThread().getName(), item, items.size());
        }
    }

    public boolean remove(T item) {
        synchronized (lock) {
            boolean removed = items.remove(item);
            System.out.printf("[%s] Removed: %-10s | found=%s | size=%d%n",
                    Thread.currentThread().getName(), item, removed, items.size());
            return removed;
        }
    }

    public T get(int index) {
        synchronized (lock) {
            return items.get(index);
        }
    }

    public int size() {
        synchronized (lock) {
            return items.size();
        }
    }

    // ⚠️ Iteration MUST be done under the lock — the caller owns the lock
    public void printAll() {
        synchronized (lock) {
            System.out.print("[" + Thread.currentThread().getName() + "] Items: [");
            Iterator<T> it = items.iterator();
            while (it.hasNext()) {
                System.out.print(it.next());
                if (it.hasNext()) System.out.print(", ");
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedList<String> list = new SynchronizedList<>();

        Thread writer1 = new Thread(() -> {
            list.add("Apple"); list.add("Banana"); list.add("Cherry");
        }, "Writer-1");

        Thread writer2 = new Thread(() -> {
            list.add("Date"); list.add("Elderberry"); list.add("Fig");
        }, "Writer-2");

        Thread remover = new Thread(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            list.remove("Banana");
            list.remove("Date");
        }, "Remover");

        Thread reader = new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            list.printAll();
        }, "Reader");

        writer1.start(); writer2.start(); remover.start(); reader.start();
        writer1.join();  writer2.join();  remover.join();  reader.join();

        System.out.println("Final size: " + list.size());
    }
}
```

---

### Example 12 — Bank Transfer with Ordered Locking

Safely transferring funds between two accounts without deadlock — uses consistent lock ordering by account ID:

```java
public class BankAccount {

    private final int    id;
    private       double balance;

    public BankAccount(int id, double initialBalance) {
        this.id      = id;
        this.balance = initialBalance;
    }

    public int getId() { return id; }

    // ✅ Consistent lock ordering by account ID — prevents deadlock
    public static void transfer(BankAccount from, BankAccount to, double amount) {

        // Always lock the lower-ID account first — consistent ordering
        BankAccount first  = from.id < to.id ? from : to;
        BankAccount second = from.id < to.id ? to   : from;

        synchronized (first) {
            synchronized (second) {
                if (from.balance < amount) {
                    System.out.printf("[%s] ❌ Insufficient funds: %.0f < %.0f%n",
                            Thread.currentThread().getName(), from.balance, amount);
                    return;
                }
                from.balance -= amount;
                to.balance   += amount;
                System.out.printf("[%s] Transferred %.0f | Account-%d=%.0f | Account-%d=%.0f%n",
                        Thread.currentThread().getName(), amount,
                        from.id, from.balance, to.id, to.balance);
            }
        }
    }

    public synchronized double getBalance() { return balance; }

    public static void main(String[] args) throws InterruptedException {
        BankAccount alice = new BankAccount(1, 1000.0);
        BankAccount bob   = new BankAccount(2, 800.0);
        BankAccount carol = new BankAccount(3, 600.0);

        // Concurrent transfers in both directions — no deadlock!
        Thread[] transfers = {
            new Thread(() -> transfer(alice, bob,   100), "T1: Alice→Bob"),
            new Thread(() -> transfer(bob,   alice, 50),  "T2: Bob→Alice"),
            new Thread(() -> transfer(bob,   carol, 200), "T3: Bob→Carol"),
            new Thread(() -> transfer(carol, alice, 75),  "T4: Carol→Alice"),
            new Thread(() -> transfer(alice, carol, 150), "T5: Alice→Carol"),
            new Thread(() -> transfer(carol, bob,   90),  "T6: Carol→Bob"),
        };

        for (Thread t : transfers) t.start();
        for (Thread t : transfers) t.join();

        System.out.printf("%nFinal balances:%n");
        System.out.printf("  Alice: %.0f%n", alice.getBalance());
        System.out.printf("  Bob  : %.0f%n", bob.getBalance());
        System.out.printf("  Carol: %.0f%n", carol.getBalance());

        double total = alice.getBalance() + bob.getBalance() + carol.getBalance();
        System.out.printf("  Total: %.0f (should be 2400)%n", total); // always 2400
    }
}
```

---

## Lock Scope Comparison

| Form | Lock Object | Scope | Concurrent Access |
|---|---|---|---|
| `synchronized` instance method | `this` | Whole method | One thread per instance |
| `synchronized` static method | `ClassName.class` | Whole method | One thread globally |
| `synchronized (this) { }` | `this` | Block only | One thread per instance |
| `synchronized (obj) { }` | `obj` | Block only | One thread per lock object |
| `synchronized (SomeClass.class) { }` | Class object | Block only | One thread globally |

```
Scope visualization:

synchronized method:
┌────────────────────────────────────┐
│ setup (non-critical)               │
│ ─── synchronized block ─────────── │ ← lock held the ENTIRE method
│  critical work                     │
│ ─── end of synchronized ─────────── │ ← lock released only on exit
│ teardown (non-critical)            │
└────────────────────────────────────┘

synchronized block (finer granularity):
┌────────────────────────────────────┐
│ setup (non-critical) ← no lock     │ ← parallel with other threads ✅
│ ─── synchronized block ─────────── │ ← lock acquired here
│  critical work                     │
│ ─── end of synchronized ─────────── │ ← lock released here (sooner!)
│ teardown (non-critical) ← no lock  │ ← parallel with other threads ✅
└────────────────────────────────────┘
```

---

## `synchronized` vs `volatile`

| Feature | `synchronized` | `volatile` |
|---|---|---|
| **Mutual exclusion** | ✅ Yes — one thread at a time | ❌ No |
| **Visibility** | ✅ Yes | ✅ Yes |
| **Ordering / happens-before** | ✅ Yes | ✅ Yes |
| **Atomicity of compound ops** | ✅ Yes (`i++` is safe) | ❌ No (`i++` is a race condition) |
| **`wait` / `notify`** | ✅ Yes | ❌ No |
| **Blocking** | ✅ Yes — can block threads | ❌ Never blocks |
| **Performance** | Higher overhead | ✅ Lighter |
| **Applies to** | Methods, blocks, any code | Fields only |
| **Best for** | Critical sections, compound ops | Simple flags, status variables |

---

## `synchronized` vs `ReentrantLock`

| Feature | `synchronized` | `ReentrantLock` |
|---|---|---|
| **Mutual exclusion** | ✅ Yes | ✅ Yes |
| **Reentrancy** | ✅ Yes | ✅ Yes |
| **Auto-release on exception** | ✅ Yes | ❌ No — must use `finally` |
| **Fairness control** | ❌ No | ✅ `new ReentrantLock(true)` |
| **tryLock (non-blocking)** | ❌ No | ✅ Yes |
| **Timed lock attempt** | ❌ No | ✅ `tryLock(timeout)` |
| **Interruptible wait** | ❌ No | ✅ `lockInterruptibly()` |
| **Multiple conditions** | ❌ One (`wait/notify`) | ✅ `newCondition()` |
| **Lock introspection** | ❌ No | ✅ `getHoldCount()`, etc. |
| **Code verbosity** | ✅ Concise | More verbose |
| **Performance** | JVM-optimised (often faster) | Comparable |
| **Best for** | Simple critical sections | Complex sync, timeouts, fairness |

---

## JVM Optimisations on `synchronized`

Modern JVMs apply several optimisations to `synchronized` blocks, making them much faster than a naive OS mutex:

| Optimisation | Description | When Applied |
|---|---|---|
| **Biased Locking** | Locks are "biased" to the first acquiring thread — subsequent acquisitions by the same thread are nearly free | Single-threaded access patterns |
| **Lock Elision** | JIT compiler removes locks that it proves are unreachable by other threads (e.g., thread-local objects) | Local objects that escape analysis proves are unshared |
| **Lock Coarsening** | Merges adjacent synchronized blocks on the same object into one | Repeated lock/unlock on same object |
| **Adaptive Spinning** | Thread spins briefly before blocking — avoids expensive OS context switch for short waits | Low contention, short hold times |
| **Thin Locks** | Lightweight CAS-based lock — no OS involvement until contention is detected | Low to moderate contention |
| **Fat Locks (Inflation)** | Escalates to OS mutex only when threads actually queue up | High contention |

> 💡 Because of these JVM optimisations, `synchronized` is often as fast as or faster than `ReentrantLock` for common patterns. Always **measure** before switching to a more complex locking primitive for "performance" reasons.

---

## Common Pitfalls

### 1. Synchronizing on a Non-Final or Changing Object

```java
private String lockObj = "lock"; // ❌ String literals are interned — shared globally!

public void method() {
    synchronized (lockObj) { // may accidentally lock on a shared interned string
        // ...
    }
}

// ❌ Synchronizing on boxed Integer — values -128 to 127 are cached!
private Integer count = 0;
synchronized (count) { // count may be a shared cached instance!
    count++;           // count = new Integer(1) → lock object CHANGED!
}

// ✅ Always use a dedicated final Object as the lock
private final Object lock = new Object();
synchronized (lock) { /* ... */ }
```

### 2. Synchronizing on `this` in a Public Class (Lock Leakage)

```java
// ❌ External code can lock on this — surprising interactions
public synchronized void method() { /* ... */ }

// External code:
SomeClass obj = new SomeClass();
synchronized (obj) {          // holds the same lock!
    // prevents SomeClass.method() from running — unintended interference
}

// ✅ Use a private final lock object — not visible outside the class
private final Object lock = new Object();
public void method() {
    synchronized (lock) { /* ... */ }
}
```

### 3. Using `if` Instead of `while` with `wait()`

```java
// ❌ Spurious wakeup — thread may wake without being notified
synchronized (lock) {
    if (buffer.isEmpty()) {
        lock.wait(); // wakes up spuriously — buffer may STILL be empty!
    }
    buffer.poll(); // NullPointerException if spurious wakeup!
}

// ✅ Always use while — re-checks condition on every wakeup
synchronized (lock) {
    while (buffer.isEmpty()) {
        lock.wait(); // wakes → re-checks → waits again if still empty
    }
    buffer.poll(); // safe — condition is guaranteed true
}
```

### 4. Calling `wait()` / `notify()` Without Holding the Lock

```java
// ❌ IllegalMonitorStateException — not inside synchronized block
lock.wait();    // throws java.lang.IllegalMonitorStateException
lock.notify();  // throws java.lang.IllegalMonitorStateException

// ✅ Must hold the lock
synchronized (lock) {
    lock.wait();    // ✅
    lock.notify();  // ✅
}
```

### 5. Nested Locks in Inconsistent Order — Deadlock

```java
// ❌ Thread-1: lock A then B | Thread-2: lock B then A → DEADLOCK
Thread-1: synchronized(A) { synchronized(B) { } }
Thread-2: synchronized(B) { synchronized(A) { } }

// ✅ Always acquire locks in the same consistent order
Thread-1: synchronized(A) { synchronized(B) { } }
Thread-2: synchronized(A) { synchronized(B) { } }  // same order
```

### 6. Holding a Lock During Blocking I/O or Sleep

```java
// ❌ Lock held while doing I/O — starves all other threads for a long time
public synchronized void processFile(String path) {
    readLargeFile(path);   // blocks for seconds — lock held the entire time!
    updateSharedState();
}

// ✅ Do the I/O outside the lock, synchronize only the shared state update
public void processFile(String path) {
    String data = readLargeFile(path); // no lock — I/O is safe outside
    synchronized (this) {
        updateSharedState(data);       // lock held briefly only for shared state
    }
}
```

### 7. Returning from Inside a Synchronized Block

```java
// ✅ This is actually SAFE — synchronized auto-releases on all exit paths
public synchronized int getAndReset() {
    int value = counter;
    counter = 0;
    return value; // lock IS released on return — no issue
}

// ⚠️ Only a concern with explicit locks (ReentrantLock):
lock.lock();
try {
    return value; // lock.unlock() in finally ensures release ✅
} finally {
    lock.unlock();
}
```

---

## When to Use `synchronized`

| Scenario | Use `synchronized`? | Alternative |
|---|---|---|
| **Simple counter / accumulator (multiple threads)** | ✅ Yes | `AtomicInteger` (faster) |
| **Protect a block of related statements** | ✅ Yes | `ReentrantLock` (more control) |
| **Wait-notify coordination** | ✅ Yes | `ReentrantLock` + `Condition` |
| **Simple mutual exclusion** | ✅ Yes — first choice | `ReentrantLock` |
| **Need timeout on lock acquisition** | ❌ No | `ReentrantLock.tryLock(timeout)` |
| **Need interruptible wait** | ❌ No | `ReentrantLock.lockInterruptibly()` |
| **Need fairness (FIFO ordering)** | ❌ No | `ReentrantLock(true)` |
| **Multiple condition queues** | ❌ No | `ReentrantLock.newCondition()` |
| **Simple boolean flag visibility** | ❌ No | `volatile` (lighter) |
| **Read-heavy shared data** | ❌ No | `ReadWriteLock` / `StampedLock` |
| **High-throughput atomic counter** | ❌ No | `AtomicInteger.incrementAndGet()` |

---

## Summary

| Concept | Key Point |
|---|---|
| **What it is** | A built-in keyword providing mutual exclusion, visibility, and ordering |
| **Intrinsic lock** | Every Java object has one; `synchronized` acquires it |
| **Mutual exclusion** | Only one thread executes a synchronized block on the same lock at a time |
| **Visibility** | Entry: fresh read from main memory. Exit: flush writes to main memory |
| **Happens-before** | Everything before an unlock is visible to the next thread that locks the same monitor |
| **Reentrancy** | A thread can re-acquire its own lock — no deadlock |
| **Auto-release** | Lock is released on exit — even if an exception is thrown |
| **Instance method** | Lock: `this` — one thread per instance |
| **Static method** | Lock: `ClassName.class` — one thread globally |
| **Block on `this`** | Lock: `this` — finer granularity than synchronized method |
| **Block on object** | Lock: specified object — independent locks per resource |
| **`wait()` / `notify()`** | Must be called inside `synchronized`; always use `while` not `if` |
| **Deadlock prevention** | Acquire multiple locks in a consistent order |
| **JVM optimised** | Biased locking, thin locks, lock elision — often very fast |
| **Limitation** | No timeout, no interrupt, no fairness, single condition per object |

**Quick Decision Guide:**

```
Need thread safety?
         │
         ├─ Simple flag / single-field visibility only?
         │         └─ volatile ✅  (lightest)
         │
         ├─ Atomic counter / single variable compound op?
         │         └─ AtomicInteger / AtomicLong ✅ (lock-free CAS)
         │
         ├─ Critical section — simple mutual exclusion?
         │         └─ synchronized ✅  (simplest, JVM-optimised)
         │
         ├─ Critical section — need timeout / interrupt / fairness / conditions?
         │         └─ ReentrantLock ✅ (explicit lock, full control)
         │
         ├─ Read-heavy, rare writes?
         │         └─ ReadWriteLock / StampedLock ✅ (concurrent reads)
         │
         └─ Limit concurrent access to N threads?
                   └─ Semaphore ✅ (counting access control)
```

> 💡 **Best Practice:** Start with `synchronized` for mutual exclusion — it is the simplest, most readable, and JVM-optimised. Only reach for `ReentrantLock` when you genuinely need features it adds: timed waits, interruptible locking, fairness, or multiple condition queues. Never hold a lock longer than necessary, always use `while` with `wait()`, and always acquire multiple locks in a consistent order to prevent deadlocks.