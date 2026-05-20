# The `volatile` Keyword in Java

## Table of Contents
- [Introduction](#introduction)
- [The Problems `volatile` Solves](#the-problems-volatile-solves)
    - [Problem 1 — CPU Cache Visibility](#problem-1--cpu-cache-visibility)
    - [Problem 2 — Instruction Reordering](#problem-2--instruction-reordering)
- [How `volatile` Works](#how-volatile-works)
    - [The Analogy](#the-analogy)
    - [Memory Visibility Model](#memory-visibility-model)
    - [The Happens-Before Guarantee](#the-happens-before-guarantee)
- [Syntax](#syntax)
- [What `volatile` Guarantees](#what-volatile-guarantees)
- [What `volatile` Does NOT Guarantee](#what-volatile-does-not-guarantee)
- [Atomicity of `volatile` Operations](#atomicity-of-volatile-operations)
    - [Atomic Reads and Writes](#atomic-reads-and-writes)
    - [Non-Atomic Compound Operations](#non-atomic-compound-operations)
- [Examples](#examples)
    - [Example 1 — Visibility Without `volatile` (The Bug)](#example-1--visibility-without-volatile-the-bug)
    - [Example 2 — Visibility Fixed with `volatile`](#example-2--visibility-fixed-with-volatile)
    - [Example 3 — Stop Flag Pattern](#example-3--stop-flag-pattern)
    - [Example 4 — Status Flag Between Threads](#example-4--status-flag-between-threads)
    - [Example 5 — `volatile` long and double (64-bit Atomicity)](#example-5--volatile-long-and-double-64-bit-atomicity)
    - [Example 6 — Double-Checked Locking with `volatile`](#example-6--double-checked-locking-with-volatile)
    - [Example 7 — Instruction Reordering Demonstration](#example-7--instruction-reordering-demonstration)
    - [Example 8 — `volatile` vs `synchronized` for a Flag](#example-8--volatile-vs-synchronized-for-a-flag)
    - [Example 9 — When `volatile` is NOT Enough (Race Condition)](#example-9--when-volatile-is-not-enough-race-condition)
    - [Example 10 — `volatile` with Object References](#example-10--volatile-with-object-references)
- [Supported Primitive Types](#supported-primitive-types)
- [`volatile` vs `synchronized`](#volatile-vs-synchronized)
- [`volatile` vs `AtomicInteger` / Atomic Classes](#volatile-vs-atomicinteger--atomic-classes)
- [Java Memory Model and `volatile`](#java-memory-model-and-volatile)
    - [Memory Barriers](#memory-barriers)
    - [Happens-Before Rules for `volatile`](#happens-before-rules-for-volatile)
- [Common Pitfalls](#common-pitfalls)
- [When to Use `volatile`](#when-to-use-volatile)
- [Summary](#summary)

---

## Introduction

The `volatile` keyword in Java is a **field modifier** that guarantees two critical properties for shared variables in multithreaded programs:

1. **Visibility** — every write to a `volatile` variable is immediately visible to all other threads
2. **Ordering** — reads and writes to `volatile` variables cannot be reordered relative to each other

`volatile` was introduced with Java 1.0 but its full memory semantics — especially the **happens-before** guarantee — were precisely defined and strengthened in the **Java Memory Model (JMM)** introduced with **Java 5 (JSR-133)**.

It is the **lightest-weight synchronization primitive** in Java — cheaper than `synchronized` or any lock, but also the most limited. It solves exactly one class of problems: **sharing a simple flag or state variable between threads** where atomic compound operations are not needed.

---

## The Problems `volatile` Solves`

### Problem 1 — CPU Cache Visibility

Modern CPUs don't read/write directly to main memory for every operation. Each CPU core has its own **L1/L2 cache**. When a thread writes to a variable, the new value is written to the **core's local cache** first and may not be flushed to main memory immediately. Other threads running on different cores read from **their own caches** and may see the **old stale value** — even seconds later.

```
Without volatile:

  Main Memory:  [running = true]
                      │
          ┌───────────┴───────────┐
          │                       │
    Core 1 Cache            Core 2 Cache
    [running = true]        [running = true]
          │                       │
       Thread-A               Thread-B
    (sets running=false)     (reads running)
          │                       │
    Core 1 Cache            Core 2 Cache
    [running = false]  ✗    [running = true]  ← stale! never sees the change
    (not flushed yet)
          │
    Main Memory: [running = ???]  ← update not guaranteed
```

```
With volatile:

  Thread-A writes running=false
    → IMMEDIATELY flushed to Main Memory
    → Core 2 Cache is INVALIDATED

  Thread-B reads running
    → Cache miss → reads from Main Memory
    → sees running = false ✅
```

### Problem 2 — Instruction Reordering

The **JVM** and **CPU** are allowed to reorder instructions as long as the result appears correct **within a single thread**. This optimisation improves performance but can cause unexpected behaviour when multiple threads share data.

```java
// Original code:
initialized = false;
data = computeExpensiveData();
initialized = true;

// JVM/CPU may reorder to:
initialized = true;         // ← reordered to first!
data = computeExpensiveData();
// Another thread sees initialized=true before data is ready — BUG
```

`volatile` inserts **memory barriers** that prevent the JIT compiler and CPU from reordering reads/writes across the `volatile` access point.

---

## How `volatile` Works

### The Analogy

Think of a **shared whiteboard** in an office versus **private notebooks**:

```
Without volatile — private notebooks (CPU caches):

Thread-A writes "STOP" in its private notebook.
Thread-B reads from its own private notebook — still says "GO".
Thread-B never sees Thread-A's update ❌

With volatile — shared whiteboard (main memory):

Thread-A writes "STOP" directly on the shared whiteboard.
Thread-B always reads from the shared whiteboard.
Thread-B immediately sees "STOP" ✅

Rule: volatile = "always use the shared whiteboard, never your private notebook"
```

### Memory Visibility Model

```
              ┌──────────────────────────────────────┐
              │           MAIN MEMORY                │
              │                                      │
              │   volatile int flag = 0              │
              └──────────────┬───────────────────────┘
                             │ direct read/write
              ┌──────────────┴───────────────────────┐
              │                                      │
       ┌──────┴──────┐                      ┌────────┴──────┐
       │   Thread-A  │                      │   Thread-B    │
       │             │                      │               │
       │ flag = 1    │─── write to main ───►│ int x = flag  │
       │             │    memory instantly  │ x is always 1 │
       │ (no cache)  │                      │ (no cache)    │
       └─────────────┘                      └───────────────┘

Non-volatile variables go through CPU cache layers:
Thread-A → L1 Cache → L2 Cache → L3 Cache → Main Memory (eventual)
Thread-B reads from its own L1/L2 cache (may be stale)

Volatile variables bypass cache for synchronization:
Thread-A write → Main Memory (immediate)
Thread-B read  ← Main Memory (always fresh)
```

### The Happens-Before Guarantee

The Java Memory Model defines a **happens-before** relationship for `volatile`:

> **A write to a `volatile` variable V *happens-before* every subsequent read of V.**

This means: everything Thread-A did **before** writing to a `volatile` variable is guaranteed to be **visible** to Thread-B **after** Thread-B reads that same `volatile` variable.

```
Thread-A:                          Thread-B:
a = 10;           ← non-volatile
b = 20;           ← non-volatile
volatile_flag = true;  ────────────────► reads volatile_flag == true
                                        a is guaranteed to be 10 ✅
                                        b is guaranteed to be 20 ✅
```

The `volatile` write acts as a **publication fence** — everything written before it is visible to anyone who sees the `volatile` write.

---

## Syntax

```java
// Declare a volatile field
public class MyClass {

    private volatile boolean running   = true;
    private volatile int     status    = 0;
    private volatile long    timestamp = 0L;
    private volatile double  rate      = 0.0;
    private volatile String  message   = null;  // volatile reference

    // ❌ volatile cannot be applied to local variables
    // void method() {
    //     volatile int x = 0; // compile error
    // }

    // ❌ volatile cannot be applied to method parameters
    // void method(volatile int x) { } // compile error
}
```

> 💡 `volatile` is only valid on **instance fields** and **static fields**. It cannot be used on local variables, method parameters, or method return types.

---

## What `volatile` Guarantees

| Guarantee | Description |
|---|---|
| **Visibility** | A write by any thread is immediately visible to all other threads reading that variable |
| **No caching** | The variable is never read from or written to a CPU cache — always main memory |
| **Ordering (write)** | All writes that happened before a `volatile` write are visible after the `volatile` write is seen |
| **Ordering (read)** | All reads after a `volatile` read will see the latest value of all variables written before that `volatile` write |
| **64-bit atomicity** | `long` and `double` reads/writes are atomic when declared `volatile` (guaranteed single operation) |
| **No reordering** | The JVM and CPU cannot reorder instructions across a `volatile` access |

---

## What `volatile` Does NOT Guarantee

| Not Guaranteed | Why | Solution |
|---|---|---|
| **Atomicity of compound operations** | `i++` = read + increment + write — three separate steps | Use `AtomicInteger` or `synchronized` |
| **Mutual exclusion** | Multiple threads can read and write simultaneously | Use `synchronized` or `ReentrantLock` |
| **Atomic check-then-act** | `if (x == 0) x = 1;` is not atomic | Use `AtomicInteger.compareAndSet()` |
| **Object field visibility** | Makes the reference volatile, not the object's internals | Make internal fields volatile or use immutable objects |
| **Transactional consistency** | Cannot group multiple variables into one atomic operation | Use `synchronized` or `StampedLock` |

---

## Atomicity of `volatile` Operations

### Atomic Reads and Writes

The following operations on `volatile` primitives are **atomic** (performed as a single indivisible operation):

```java
volatile int    x;
volatile long   y;  // 64-bit — atomic only because of volatile
volatile double z;  // 64-bit — atomic only because of volatile
volatile boolean flag;

// These are ATOMIC:
x    = 42;        // ✅ single write
int a = x;        // ✅ single read
flag = true;      // ✅ single write
```

### Non-Atomic Compound Operations

The following operations on `volatile` variables are **NOT atomic**, even with the `volatile` keyword:

```java
volatile int counter = 0;

// These are NOT atomic — each is multiple steps:
counter++;           // ❌ read → increment → write (3 steps)
counter += 5;        // ❌ read → add → write (3 steps)
counter = counter + 1; // ❌ same as counter++

// What counter++ actually compiles to (three separate operations):
int temp = counter;  // step 1: read (another thread can interrupt here)
temp = temp + 1;     // step 2: increment
counter = temp;      // step 3: write (result may overwrite another thread's update)
```

```
Race condition with volatile counter++:

Initial: counter = 0

Thread-A: reads counter → 0
Thread-B: reads counter → 0       ← both read before either writes
Thread-A: writes counter = 1
Thread-B: writes counter = 1      ← overwrites Thread-A's result!

Expected: counter = 2
Actual:   counter = 1  ❌
```

---

## Examples

### Example 1 — Visibility Without `volatile` (The Bug)

Without `volatile`, Thread-B may never see Thread-A's write — possibly running forever:

```java
public class VisibilityBug {

    // ❌ No volatile — Thread-B may cache this value and never see the update
    private static boolean running = true;

    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            System.out.println("Worker: started");
            int count = 0;
            while (running) {      // may loop forever — reads stale cached value
                count++;
            }
            System.out.println("Worker: stopped. count = " + count);
        }, "Worker");

        worker.start();
        Thread.sleep(100); // let worker start

        System.out.println("Main: setting running = false");
        running = false;   // Thread-B may never see this update!

        worker.join(2000); // wait up to 2s
        if (worker.isAlive()) {
            System.out.println("Main: worker is STILL running — visibility bug!");
            worker.interrupt();
        }
    }
}

// Possible Output (JVM with optimisations enabled):
// Worker: started
// Main: setting running = false
// (worker keeps running forever — never sees the change)
```

---

### Example 2 — Visibility Fixed with `volatile`

```java
public class VisibilityFixed {

    // ✅ volatile — every thread always reads from main memory
    private static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            System.out.println("Worker: started");
            int count = 0;
            while (running) {      // always reads fresh value from main memory
                count++;
            }
            System.out.println("Worker: stopped. count = " + count);
        }, "Worker");

        worker.start();
        Thread.sleep(100);

        System.out.println("Main: setting running = false");
        running = false;   // immediately visible to all threads

        worker.join(1000);
        System.out.println("Main: worker finished = " + !worker.isAlive());
    }
}

// Output (guaranteed):
// Worker: started
// Main: setting running = false
// Worker: stopped. count = <some number>
// Main: worker finished = true
```

---

### Example 3 — Stop Flag Pattern

The most common and correct use of `volatile` — a single-writer stop signal:

```java
public class StopFlagPattern {

    private volatile boolean stopRequested = false;

    // Thread that does work and checks the flag
    class Worker implements Runnable {
        private final String name;

        Worker(String name) { this.name = name; }

        @Override
        public void run() {
            System.out.println(name + ": started");
            int workDone = 0;

            while (!stopRequested) {
                // Simulate work
                workDone++;
                if (workDone % 100_000 == 0) {
                    System.out.println(name + ": working... units=" + workDone);
                }
            }
            System.out.println(name + ": stopped gracefully. total work=" + workDone);
        }
    }

    public void stop() {
        System.out.println("Stop signal sent.");
        stopRequested = true; // single write — volatile is sufficient
    }

    public static void main(String[] args) throws InterruptedException {
        StopFlagPattern controller = new StopFlagPattern();

        Thread t1 = new Thread(controller.new Worker("Worker-1"));
        Thread t2 = new Thread(controller.new Worker("Worker-2"));

        t1.start();
        t2.start();

        Thread.sleep(300); // let workers run

        controller.stop(); // signal both workers to stop

        t1.join();
        t2.join();
        System.out.println("All workers stopped.");
    }
}

// Output:
// Worker-1: started
// Worker-2: started
// Worker-1: working... units=100000
// ...
// Stop signal sent.
// Worker-1: stopped gracefully.
// Worker-2: stopped gracefully.
// All workers stopped.
```

---

### Example 4 — Status Flag Between Threads

Using `volatile` to communicate initialization completion:

```java
public class StatusFlag {

    private volatile boolean dataReady = false;
    private int[]            data      = new int[1_000_000]; // non-volatile array

    // Producer thread — initializes data and sets the flag
    public void produce() {
        System.out.println("Producer: filling data...");
        for (int i = 0; i < data.length; i++) {
            data[i] = i * 2; // fill array (non-volatile operations)
        }
        // volatile write: happens-before guarantee ensures
        // all data[] writes above are visible after this point
        dataReady = true;
        System.out.println("Producer: data ready.");
    }

    // Consumer threads — wait for flag, then safely read data
    public void consume(String name) {
        System.out.println(name + ": waiting for data...");

        while (!dataReady) {
            // spin-wait — volatile read ensures fresh value each iteration
            Thread.onSpinWait(); // Java 9+ hint to CPU to use spin-wait instruction
        }

        // Happens-before: all data[] writes by producer are now visible
        System.out.println(name + ": data[0]=" + data[0]
                + " data[last]=" + data[data.length - 1]);
    }

    public static void main(String[] args) throws InterruptedException {
        StatusFlag sf = new StatusFlag();

        Thread consumer1 = new Thread(() -> sf.consume("Consumer-1"), "Consumer-1");
        Thread consumer2 = new Thread(() -> sf.consume("Consumer-2"), "Consumer-2");
        Thread producer  = new Thread(sf::produce, "Producer");

        consumer1.start();
        consumer2.start();
        Thread.sleep(100); // let consumers start waiting

        producer.start();

        producer.join();
        consumer1.join();
        consumer2.join();
    }
}

// Output:
// Consumer-1: waiting for data...
// Consumer-2: waiting for data...
// Producer: filling data...
// Producer: data ready.
// Consumer-1: data[0]=0 data[last]=1999998
// Consumer-2: data[0]=0 data[last]=1999998
```

> 💡 The `volatile` write to `dataReady` acts as a **publication barrier** — it guarantees that all writes to `data[]` that happened before `dataReady = true` are visible to any thread that reads `dataReady == true`.

---

### Example 5 — `volatile` long and double (64-bit Atomicity)

On 32-bit JVMs, `long` and `double` reads/writes are not guaranteed to be atomic without `volatile`:

```java
public class LongAtomicityDemo {

    // ❌ Without volatile — 32-bit JVMs may write high/low 32 bits separately
    //    Another thread may read a "torn" value (half old, half new)
    private long nonVolatileLong = 0L;

    // ✅ With volatile — write is atomic even on 32-bit JVMs
    private volatile long volatileLong = 0L;

    public void writeValues() {
        // Without volatile: another thread might see
        // high 32 bits of new value + low 32 bits of old value (torn read)
        nonVolatileLong = 0xDEADBEEFCAFEBABEL;

        // With volatile: atomic write — no tearing possible
        volatileLong    = 0xDEADBEEFCAFEBABEL;
    }

    public static void main(String[] args) throws InterruptedException {
        LongAtomicityDemo demo = new LongAtomicityDemo();

        // Writer thread
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) {
                demo.writeValues();
            }
        }, "Writer");

        // Reader thread — with non-volatile long, may see torn values
        Thread reader = new Thread(() -> {
            long expected = 0xDEADBEEFCAFEBABEL;
            int tornCount = 0;
            for (int i = 0; i < 100_000; i++) {
                long val = demo.nonVolatileLong;
                if (val != 0L && val != expected) {
                    tornCount++; // saw a "torn" value!
                }
            }
            System.out.println("Torn reads detected: " + tornCount);
            // On modern 64-bit JVMs this may be 0, but it's not guaranteed
            // On older/32-bit JVMs this would be non-zero
        }, "Reader");

        writer.start();
        reader.start();
        writer.join();
        reader.join();

        System.out.println("Volatile long is always atomic: " + demo.volatileLong);
    }
}
```

---

### Example 6 — Double-Checked Locking with `volatile`

The classic **Singleton pattern** using double-checked locking — `volatile` is essential here:

```java
public class Singleton {

    // ✅ volatile is REQUIRED here — without it, the JVM can reorder:
    //    1. allocate memory
    //    2. assign reference to instance  ← may happen BEFORE step 3
    //    3. call constructor
    // Another thread may see a non-null but incompletely constructed object
    private static volatile Singleton instance;

    private final String config;

    private Singleton() {
        // Simulate heavy initialization
        this.config = "production-config";
        System.out.println("Singleton created by: " + Thread.currentThread().getName());
    }

    public static Singleton getInstance() {
        if (instance == null) {                    // First check (no lock — fast path)
            synchronized (Singleton.class) {
                if (instance == null) {            // Second check (inside lock — safe)
                    instance = new Singleton();    // volatile write — no reordering
                }
            }
        }
        return instance;                           // volatile read — always current
    }

    public String getConfig() { return config; }

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            Singleton s = Singleton.getInstance();
            System.out.println(Thread.currentThread().getName()
                    + " → config = " + s.getConfig()
                    + " | same instance: " + (s == Singleton.getInstance()));
        };

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(task, "Thread-" + (i + 1));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}

// Output (Singleton created exactly once):
// Singleton created by: Thread-1
// Thread-1 → config = production-config | same instance: true
// Thread-2 → config = production-config | same instance: true
// Thread-3 → config = production-config | same instance: true
// Thread-4 → config = production-config | same instance: true
// Thread-5 → config = production-config | same instance: true
```

---

### Example 7 — Instruction Reordering Demonstration

Showing how `volatile` prevents reordering that can expose partially constructed state:

```java
public class ReorderingDemo {

    // Without volatile — the JVM can reorder these two writes:
    static int    value_nonVolatile   = 0;
    static boolean ready_nonVolatile  = false;

    // With volatile — no reordering across the volatile write:
    static int             value_safe = 0;
    static volatile boolean ready_safe = false;

    // ── Unsafe version (can exhibit reordering) ───────────────────────────────
    static void unsafeWriter() {
        value_nonVolatile = 42;       // write 1
        ready_nonVolatile = true;     // write 2 — JVM may reorder: write2 before write1
    }

    static void unsafeReader() {
        if (ready_nonVolatile) {
            // ready is true — but value may still be 0 due to reordering!
            System.out.println("Unsafe value: " + value_nonVolatile); // may print 0!
        }
    }

    // ── Safe version (volatile prevents reordering) ───────────────────────────
    static void safeWriter() {
        value_safe = 42;   // guaranteed to complete BEFORE volatile write
        ready_safe = true; // volatile write — acts as a memory barrier
    }

    static void safeReader() {
        if (ready_safe) {  // volatile read — sees latest value
            // JMM guarantees value_safe is 42 here — happens-before ensures it
            System.out.println("Safe value: " + value_safe); // always 42 ✅
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // Safe scenario
        Thread writer = new Thread(ReorderingDemo::safeWriter, "Writer");
        Thread reader = new Thread(() -> {
            while (!ready_safe) { /* wait */ }
            safeReader();
        }, "Reader");

        reader.start();
        Thread.sleep(50);
        writer.start();

        writer.join();
        reader.join();
    }
}
```

---

### Example 8 — `volatile` vs `synchronized` for a Flag

Comparing the two approaches for a simple stop flag — `volatile` is the right tool here:

```java
public class FlagComparison {

    // ── Approach 1: volatile (lightweight, correct for simple flags) ──────────
    private volatile boolean volatileFlag = true;

    public boolean isRunningVolatile() {
        return volatileFlag; // direct read from main memory
    }

    public void stopVolatile() {
        volatileFlag = false; // direct write to main memory
    }

    // ── Approach 2: synchronized (heavyweight but provides mutual exclusion) ──
    private boolean synchronizedFlag = true;

    public synchronized boolean isRunningSynchronized() {
        return synchronizedFlag; // acquires lock on every read
    }

    public synchronized void stopSynchronized() {
        synchronizedFlag = false; // acquires lock on every write
    }

    public static void main(String[] args) throws InterruptedException {
        FlagComparison fc = new FlagComparison();

        // volatile version — efficient, no lock overhead on every read
        Thread volatileWorker = new Thread(() -> {
            long count = 0;
            while (fc.isRunningVolatile()) { count++; }
            System.out.println("Volatile worker stopped. count=" + count);
        }, "VolatileWorker");

        // synchronized version — correct but acquires lock on each read
        Thread syncWorker = new Thread(() -> {
            long count = 0;
            while (fc.isRunningSynchronized()) { count++; }
            System.out.println("Sync worker stopped. count=" + count);
        }, "SyncWorker");

        volatileWorker.start();
        syncWorker.start();

        Thread.sleep(200);

        fc.stopVolatile();
        fc.stopSynchronized();

        volatileWorker.join();
        syncWorker.join();

        // volatile is faster here — no lock/unlock overhead on every loop iteration
        System.out.println("Both stopped. volatile is preferred for simple flags.");
    }
}
```

---

### Example 9 — When `volatile` is NOT Enough (Race Condition)

Demonstrating that `volatile` cannot prevent race conditions on compound operations:

```java
import java.util.concurrent.atomic.AtomicInteger;

public class VolatileNotEnough {

    // ❌ volatile counter — reads/writes are visible but ++ is NOT atomic
    private volatile int volatileCounter = 0;

    // ✅ AtomicInteger — all operations are atomic (CAS-based)
    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    // ✅ synchronized counter — correct but slowest
    private int synchronizedCounter = 0;

    public synchronized void incrementSynchronized() {
        synchronizedCounter++;
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileNotEnough demo = new VolatileNotEnough();
        int THREADS    = 10;
        int INCREMENTS = 10_000;

        Runnable volatileTask = () -> {
            for (int i = 0; i < INCREMENTS; i++) {
                demo.volatileCounter++; // ❌ NOT atomic — lost updates!
            }
        };

        Runnable atomicTask = () -> {
            for (int i = 0; i < INCREMENTS; i++) {
                demo.atomicCounter.incrementAndGet(); // ✅ atomic CAS
            }
        };

        Runnable syncTask = () -> {
            for (int i = 0; i < INCREMENTS; i++) {
                demo.incrementSynchronized(); // ✅ mutually exclusive
            }
        };

        // Run volatile version
        Thread[] volatileThreads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            volatileThreads[i] = new Thread(volatileTask, "VT-" + i);
        }
        for (Thread t : volatileThreads) t.start();
        for (Thread t : volatileThreads) t.join();

        // Run atomic version
        Thread[] atomicThreads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            atomicThreads[i] = new Thread(atomicTask, "AT-" + i);
        }
        for (Thread t : atomicThreads) t.start();
        for (Thread t : atomicThreads) t.join();

        // Run synchronized version
        Thread[] syncThreads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            syncThreads[i] = new Thread(syncTask, "ST-" + i);
        }
        for (Thread t : syncThreads) t.start();
        for (Thread t : syncThreads) t.join();

        System.out.println("Expected        : " + (THREADS * INCREMENTS));
        System.out.println("volatile counter: " + demo.volatileCounter
                + "  ❌ (lost updates due to race condition)");
        System.out.println("atomic counter  : " + demo.atomicCounter.get()
                + "  ✅ (always correct)");
        System.out.println("sync counter    : " + demo.synchronizedCounter
                + "  ✅ (always correct)");
    }
}

// Sample Output:
// Expected        : 100000
// volatile counter: 73841   ❌ (lost updates due to race condition)
// atomic counter  : 100000  ✅ (always correct)
// sync counter    : 100000  ✅ (always correct)
```

---

### Example 10 — `volatile` with Object References

`volatile` on a reference makes the **reference** visible — but does NOT make the object's fields volatile:

```java
public class VolatileReference {

    static class Config {
        final String host;    // final — safely published via volatile reference
        final int    port;    // final — safely published
        String       mutableField = "initial"; // NOT thread-safe without volatile

        Config(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    // volatile reference — the reference itself is visible across threads
    // The Config object uses final fields (immutable content = safe publication)
    private volatile Config config = new Config("localhost", 8080);

    // ✅ Safe: replacing the whole reference (immutable Config objects)
    public void updateConfig(String newHost, int newPort) {
        // Create a new immutable Config — assign atomically via volatile reference
        config = new Config(newHost, newPort);
        System.out.println(Thread.currentThread().getName()
                + ": config updated to " + newHost + ":" + newPort);
    }

    // ✅ Safe: reading via volatile reference — always sees latest Config object
    public void readConfig() {
        Config local = config; // read volatile reference once into local variable
        System.out.println(Thread.currentThread().getName()
                + ": host=" + local.host + " port=" + local.port);
    }

    // ❌ Unsafe: mutating the object's non-final field — volatile reference
    //    only guarantees reference visibility, not internal field visibility
    public void unsafeMutation() {
        config.mutableField = "changed"; // NOT volatile — may not be visible!
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileReference demo = new VolatileReference();

        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                demo.updateConfig("server-" + i, 8080 + i);
                try { Thread.sleep(100); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Writer");

        Thread[] readers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 8; j++) {
                    demo.readConfig();
                    try { Thread.sleep(70); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Reader-" + (i + 1));
        }

        writer.start();
        for (Thread r : readers) r.start();
        writer.join();
        for (Thread r : readers) r.join();
    }
}
```

> 💡 **Best Practice for volatile references:** Use **immutable objects** (all fields `final`) referenced by a `volatile` field. Replacing the whole reference is atomic and safe. Mutating the object's non-final internal fields is NOT made safe by the `volatile` reference.

---

## Supported Primitive Types

`volatile` can be applied to all Java primitive types and reference types:

| Type | Size | Notes |
|---|---|---|
| `boolean` | 1 bit | Most common volatile type — flags and signals |
| `byte` | 8 bits | Naturally atomic even without volatile |
| `short` | 16 bits | Naturally atomic even without volatile |
| `char` | 16 bits | Naturally atomic even without volatile |
| `int` | 32 bits | Naturally atomic even without volatile |
| `float` | 32 bits | Naturally atomic even without volatile |
| `long` | 64 bits | ⚠️ NOT atomic on 32-bit JVMs without `volatile` |
| `double` | 64 bits | ⚠️ NOT atomic on 32-bit JVMs without `volatile` |
| Object reference | 32/64 bits | Makes reference atomic — not object's fields |

> ⚠️ `long` and `double` are the only primitive types that **require** `volatile` for atomicity guarantees on 32-bit JVMs. On modern 64-bit JVMs, they are often atomic anyway, but the JVM spec does not guarantee it without `volatile`.

---

## `volatile` vs `synchronized`

| Feature | `volatile` | `synchronized` |
|---|---|---|
| **Visibility** | ✅ Yes | ✅ Yes |
| **Ordering / happens-before** | ✅ Yes | ✅ Yes |
| **Mutual exclusion** | ❌ No | ✅ Yes |
| **Atomicity of compound ops** | ❌ No (`i++` is unsafe) | ✅ Yes |
| **Reentrancy** | ❌ N/A | ✅ Yes |
| **Performance** | ✅ Faster (no lock overhead) | Slower (lock/unlock cost) |
| **Blocking** | ❌ Never blocks | ✅ Can block threads |
| **Use case** | Simple flags, status, references | Critical sections, compound ops |
| **64-bit atomicity** | ✅ Yes (long/double) | ✅ Yes |
| **Object scope** | Single field | Any block or method |

---

## `volatile` vs `AtomicInteger` / Atomic Classes

| Feature | `volatile int` | `AtomicInteger` |
|---|---|---|
| **Visibility** | ✅ Yes | ✅ Yes |
| **Read atomicity** | ✅ Yes | ✅ Yes |
| **Write atomicity** | ✅ Yes | ✅ Yes |
| **`i++` atomicity** | ❌ No | ✅ Yes (`incrementAndGet()`) |
| **Compare-and-set** | ❌ No | ✅ `compareAndSet(expected, update)` |
| **Performance** | ✅ Slightly faster for read/write | Slightly more overhead (CAS) |
| **Use case** | Simple flags, one writer | Counters, shared state, CAS patterns |

---

## Java Memory Model and `volatile`

### Memory Barriers

When the JVM encounters a `volatile` write or read, it inserts **memory barrier** instructions:

```
Volatile Write:
  1. [StoreStore Barrier] — all prior non-volatile writes complete first
  2. Write volatile variable to main memory
  3. [StoreLoad Barrier]  — ensures the write is flushed before any subsequent reads

Volatile Read:
  1. [LoadLoad Barrier]  — invalidate cached data, read fresh from main memory
  2. Read volatile variable from main memory
  3. [LoadStore Barrier] — all subsequent reads/writes happen after this read
```

### Happens-Before Rules for `volatile`

The Java Memory Model defines these rules:

```
Rule 1 — volatile write happens-before subsequent volatile read:
  Thread-A writes volatile x = 5
    → Thread-B reads volatile x
    → Thread-B is GUARANTEED to see x = 5

Rule 2 — Transitivity of happens-before:
  Thread-A: a = 1 (non-volatile)
  Thread-A: volatile_flag = true  ← volatile write
  Thread-B: reads volatile_flag == true  ← volatile read
  Thread-B: reads a  ← GUARANTEED to see a = 1
  (non-volatile write before volatile write is visible after volatile read)

Rule 3 — Volatile does NOT establish happens-before between two reads:
  Thread-A: reads volatile x
  Thread-B: reads volatile x
  No ordering guaranteed between Thread-A's read and Thread-B's read
```

---

## Common Pitfalls

### 1. Using `volatile` for Non-Atomic Compound Operations

```java
private volatile int count = 0;

// ❌ Looks safe but is NOT — multiple steps: read, increment, write
public void increment() {
    count++; // race condition — lost updates with multiple threads
}

// ✅ Use AtomicInteger for atomic compound operations
private final AtomicInteger count = new AtomicInteger(0);
public void increment() {
    count.incrementAndGet(); // atomic CAS — no race condition
}

// ✅ Or use synchronized
private int count = 0;
public synchronized void increment() {
    count++;
}
```

### 2. Assuming `volatile` Makes Object Fields Thread-Safe

```java
class Counter { int value = 0; } // non-volatile field

volatile Counter counter = new Counter(); // volatile REFERENCE only

// ❌ The reference is volatile but counter.value is NOT
counter.value++; // race condition — counter.value is not volatile

// ✅ Use an immutable object with a volatile reference, or synchronize writes
```

### 3. Using `volatile` for a Lock — It Cannot Provide Mutual Exclusion

```java
private volatile boolean locked = false;

// ❌ Not a lock — check-then-act is not atomic
public void criticalSection() {
    if (!locked) {          // read
        locked = true;      // write — another thread can interleave here
        // critical section — MULTIPLE threads can enter!
        locked = false;
    }
}

// ✅ Use synchronized for mutual exclusion
public synchronized void criticalSection() {
    // only one thread at a time
}
```

### 4. Reading a `volatile` Reference Multiple Times

```java
private volatile Config config;

// ❌ Two reads of volatile reference — may get different objects
//    if another thread updates config between the two reads
public void process() {
    if (config != null) {          // volatile read 1
        config.doSomething();      // volatile read 2 — config may be null now!
    }
}

// ✅ Read volatile reference once into a local variable
public void process() {
    Config local = config;         // single volatile read
    if (local != null) {
        local.doSomething();       // uses local variable — always the same object
    }
}
```

### 5. Forgetting `volatile` in Double-Checked Locking

```java
// ❌ BROKEN — without volatile, another thread may see partially constructed object
private static Singleton instance;
public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton(); // constructor may be reordered
                // thread may see: instance != null BUT object not fully initialized
            }
        }
    }
    return instance;
}

// ✅ CORRECT — volatile prevents reordering of constructor and reference assignment
private static volatile Singleton instance;
public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton(); // no reordering — safe publication
            }
        }
    }
    return instance;
}
```

### 6. Applying `volatile` to a Local Variable

```java
public void method() {
    volatile int x = 0; // ❌ COMPILE ERROR — volatile not allowed on local variables
}
```

---

## When to Use `volatile`

| Scenario | Use `volatile`? | Alternative if No |
|---|---|---|
| **Simple boolean stop/run flag** | ✅ Yes — perfect fit | `synchronized` getter/setter |
| **Single writer, multiple readers** | ✅ Yes — safe | `synchronized` for multiple writers |
| **64-bit long/double shared between threads** | ✅ Yes | `synchronized` or `AtomicLong` |
| **Double-checked locking singleton** | ✅ Yes — required | Initialization-on-demand holder |
| **Counter incremented by multiple threads** | ❌ No — use atomic | `AtomicInteger`, `synchronized` |
| **Check-then-act patterns** | ❌ No — not atomic | `AtomicInteger.compareAndSet()` |
| **Multiple related variables must update together** | ❌ No — not transactional | `synchronized` block |
| **Complex object with mutable fields** | ❌ No — only ref is volatile | `synchronized` or immutable objects |

---

## Summary

| Concept | Key Point |
|---|---|
| **What it is** | A field modifier guaranteeing visibility and ordering of reads/writes |
| **Visibility** | Every write is immediately flushed to main memory; every read fetches from main memory |
| **No caching** | The variable bypasses CPU caches — always uses main memory |
| **Ordering** | Writes before a volatile write are visible after the volatile read (happens-before) |
| **64-bit atomicity** | `volatile long` and `volatile double` are atomic on all JVMs |
| **NOT mutual exclusion** | Multiple threads can read and write simultaneously |
| **NOT atomic for `++`** | `volatile int i; i++` is a race condition — use `AtomicInteger` |
| **Volatile reference** | The reference is volatile — the referenced object's fields are NOT |
| **Memory barriers** | Inserts StoreStore, StoreLoad, LoadLoad, LoadStore barriers at access points |
| **No blocking** | `volatile` never blocks a thread — the lightest synchronization primitive |
| **Use for** | Stop flags, status signals, double-checked locking, 64-bit variable sharing |
| **Do not use for** | Counters, check-then-act, grouped variable updates, mutual exclusion |

**Quick Decision Guide:**

```
Sharing state between threads?
         │
         ├─ Simple flag / status (one writer, one value)?
         │         └─ volatile ✅
         │
         ├─ Counter / accumulator (multiple threads increment)?
         │         └─ AtomicInteger / AtomicLong ✅
         │
         ├─ Check-then-act (if x == 0, set x = 1)?
         │         └─ AtomicInteger.compareAndSet() ✅
         │
         ├─ Multiple related variables must be consistent?
         │         └─ synchronized block ✅
         │
         └─ Long/complex critical section?
                   └─ ReentrantLock ✅
```

> 💡 **Best Practice:** Use `volatile` only when you have a **single writer** and the operation is a **simple assignment** (not a compound read-modify-write). For anything more complex — counters, conditional updates, grouped updates — reach for `AtomicInteger`, `synchronized`, or `ReentrantLock` instead. `volatile` is not a replacement for locks; it is a complement to them for the simplest sharing scenarios.