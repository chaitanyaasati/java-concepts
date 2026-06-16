# Atomic Classes in Java

## Table of Contents
- [Introduction](#introduction)
- [The Problem Atomic Classes Solve](#the-problem-atomic-classes-solve)
- [How Atomic Classes Work — Compare-And-Swap (CAS)](#how-atomic-classes-work--compare-and-swap-cas)
    - [The Analogy](#the-analogy)
    - [CAS Operation Explained](#cas-operation-explained)
    - [CAS vs Locking](#cas-vs-locking)
- [The Atomic Class Family](#the-atomic-class-family)
- [Package and Import](#package-and-import)
- [`AtomicInteger`](#atomicinteger)
    - [Key Methods — AtomicInteger](#key-methods--atomicinteger)
    - [Examples — AtomicInteger](#examples--atomicinteger)
- [`AtomicLong`](#atomiclong)
    - [Key Methods — AtomicLong](#key-methods--atomiclong)
    - [Examples — AtomicLong](#examples--atomiclong)
- [`AtomicBoolean`](#atomicboolean)
    - [Key Methods — AtomicBoolean](#key-methods--atomicboolean)
    - [Examples — AtomicBoolean](#examples--atomicboolean)
- [`AtomicReference`](#atomicreference)
    - [Key Methods — AtomicReference](#key-methods--atomicreference)
    - [Examples — AtomicReference](#examples--atomicreference)
- [`AtomicIntegerArray`, `AtomicLongArray`, `AtomicReferenceArray`](#atomicintegerarray-atomiclongarray-atomicreferencearray)
    - [Key Methods — Atomic Arrays](#key-methods--atomic-arrays)
    - [Examples — Atomic Arrays](#examples--atomic-arrays)
- [`AtomicIntegerFieldUpdater`, `AtomicLongFieldUpdater`, `AtomicReferenceFieldUpdater`](#atomicintegerfieldupdater-atomiclongfieldupdater-atomicreferencefieldupdater)
    - [Examples — Field Updaters](#examples--field-updaters)
- [High-Performance Accumulators (Java 8+)](#high-performance-accumulators-java-8)
    - [`LongAdder` and `DoubleAdder`](#longadder-and-doubleadder)
    - [`LongAccumulator` and `DoubleAccumulator`](#longaccumulator-and-doubleaccumulator)
    - [Examples — Adders and Accumulators](#examples--adders-and-accumulators)
- [The ABA Problem](#the-aba-problem)
    - [What is the ABA Problem?](#what-is-the-aba-problem)
    - [`AtomicStampedReference` — Solving ABA](#atomicstampedreference--solving-aba)
    - [`AtomicMarkableReference`](#atomicmarkablereference)
- [Practical Examples](#practical-examples)
    - [Example 1 — Thread-Safe Counter](#example-1--thread-safe-counter)
    - [Example 2 — Non-Blocking Stack (Lock-Free)](#example-2--non-blocking-stack-lock-free)
    - [Example 3 — compareAndSet for One-Time State Transition](#example-3--compareandset-for-one-time-state-transition)
    - [Example 4 — Atomic Maximum Tracker](#example-4--atomic-maximum-tracker)
    - [Example 5 — Request Rate Limiter with AtomicInteger](#example-5--request-rate-limiter-with-atomicinteger)
    - [Example 6 — Thread-Safe Singleton with AtomicReference](#example-6--thread-safe-singleton-with-atomicreference)
    - [Example 7 — Lock-Free Linked List Node Append](#example-7--lock-free-linked-list-node-append)
    - [Example 8 — LongAdder vs AtomicLong (Performance)](#example-8--longadder-vs-atomiclong-performance)
    - [Example 9 — AtomicStampedReference (ABA Fix)](#example-9--atomicstampedreference-aba-fix)
    - [Example 10 — Statistics Tracker with Multiple Atomics](#example-10--statistics-tracker-with-multiple-atomics)
- [Atomic Classes vs `synchronized`](#atomic-classes-vs-synchronized)
- [Atomic Classes vs `volatile`](#atomic-classes-vs-volatile)
- [Atomic Classes vs `ReentrantLock`](#atomic-classes-vs-reentrantlock)
- [Common Pitfalls](#common-pitfalls)
- [When to Use Each Atomic Class](#when-to-use-each-atomic-class)
- [Summary](#summary)

---

## Introduction

**Atomic classes** in Java are thread-safe wrappers around primitive values and object references that support **lock-free, thread-safe compound operations** using **hardware-level Compare-And-Swap (CAS)** instructions. They live in the `java.util.concurrent.atomic` package, introduced in **Java 5**.

Unlike `synchronized` blocks or `ReentrantLock`, atomic classes achieve thread safety **without blocking**. A thread performing a CAS operation never waits for another thread — it either succeeds immediately or retries. This makes them faster than lock-based approaches under moderate contention and eliminates risks like deadlock and priority inversion.

Key properties of all atomic classes:
- **Lock-free** — no mutex, no blocking, no deadlock risk
- **Thread-safe** — all operations are atomic from other threads' perspective
- **Visible** — all writes are immediately visible to other threads (volatile semantics)
- **Non-reentrant concerns** — no locks to manage or forget to release

---

## The Problem Atomic Classes Solve

Even with `volatile`, compound read-modify-write operations are not atomic:

```
Race condition with volatile int counter = 0:

Thread-A: reads counter  → 0
Thread-B: reads counter  → 0     ← both read before either writes
Thread-A: counter = 0+1 = 1
Thread-B: counter = 0+1 = 1     ← overwrites Thread-A — lost update!

Expected: counter = 2
Actual:   counter = 1  ❌

With synchronized:
  Thread-B blocks while Thread-A runs → safe but involves lock overhead

With AtomicInteger:
  Thread-A: CAS(expected=0, new=1) → succeeds → counter = 1
  Thread-B: CAS(expected=0, new=1) → FAILS (counter is now 1, not 0)
  Thread-B: retry: CAS(expected=1, new=2) → succeeds → counter = 2 ✅
  No lock — no blocking — correct result!
```

---

## How Atomic Classes Work — Compare-And-Swap (CAS)

### The Analogy

Think of CAS as a **compare-before-commit** transaction at a bank ATM:

```
ATM (CAS operation):
  "I will update the balance ONLY IF it is still what I saw when I started"

Thread sees balance = $500
Thread computes new balance = $500 - $100 = $400

CAS attempt: "Set balance to $400 ONLY IF it is still $500"
  → Balance is $500  → ✅ Update succeeds → balance = $400
  → Balance is $450  → ❌ Someone else changed it → retry with new value

This is atomic at the hardware level — the check and update are ONE instruction.
No other thread can interpose between the check and the update.
```

### CAS Operation Explained

```
CAS(memoryLocation, expectedValue, newValue):
  if (*memoryLocation == expectedValue) {   ← check (atomic)
      *memoryLocation = newValue;           ← update (atomic)
      return true;                          ← success
  } else {
      return false;                         ← failure — retry loop
  }

The entire if-then-set is ONE indivisible hardware instruction (CMPXCHG on x86).
No thread can interrupt between the check and the set.
```

```
CAS Retry Loop (the "spin loop" inside AtomicInteger):

int current;
do {
    current = get();                             // read current value
} while (!compareAndSet(current, current + 1)); // retry until CAS succeeds

Thread-A: read=0, CAS(0→1) → ✅ success
Thread-B: read=0, CAS(0→1) → ❌ fail (A already changed it to 1)
Thread-B: read=1, CAS(1→2) → ✅ success  (retry with fresh value)
```

### CAS vs Locking

```
Locking approach:
  Thread-A: acquire lock → read → modify → write → release lock
  Thread-B: BLOCKED  ←────────────────────────────────────────────
  (Thread-B sleeps — OS context switch — expensive)

CAS approach:
  Thread-A: read=0, CAS(0→1) → success
  Thread-B: read=0, CAS(0→1) → fail → read=1, CAS(1→2) → success
  (Thread-B never sleeps — spins briefly — no OS involvement)

Winner: CAS is faster under low-to-moderate contention
        Locking may win under very high contention (many retries waste CPU)
```

---

## The Atomic Class Family

```
java.util.concurrent.atomic
│
├── Scalar Atomics
│   ├── AtomicInteger          — int with atomic operations
│   ├── AtomicLong             — long with atomic operations
│   ├── AtomicBoolean          — boolean with atomic operations
│   └── AtomicReference<V>     — object reference with atomic operations
│
├── Array Atomics
│   ├── AtomicIntegerArray     — int[] where each element is atomic
│   ├── AtomicLongArray        — long[] where each element is atomic
│   └── AtomicReferenceArray<E>— E[] where each element is atomic
│
├── Field Updaters (Java 5+)
│   ├── AtomicIntegerFieldUpdater<T>   — atomically update volatile int field
│   ├── AtomicLongFieldUpdater<T>      — atomically update volatile long field
│   └── AtomicReferenceFieldUpdater<T,V>— atomically update volatile ref field
│
├── Stamped / Markable References
│   ├── AtomicStampedReference<V>  — reference + int stamp (solves ABA)
│   └── AtomicMarkableReference<V> — reference + boolean mark
│
└── High-Performance Accumulators (Java 8+)
    ├── LongAdder              — high-throughput long summation
    ├── DoubleAdder            — high-throughput double summation
    ├── LongAccumulator        — long with custom accumulator function
    └── DoubleAccumulator      — double with custom accumulator function
```

---

## Package and Import

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.DoubleAccumulator;
```

---

## `AtomicInteger`

The most commonly used atomic class — wraps an `int` with atomic operations.

### Key Methods — AtomicInteger

| Method | Description | Returns |
|---|---|---|
| `get()` | Returns the current value | `int` |
| `set(int val)` | Sets to the given value (volatile write) | `void` |
| `getAndSet(int val)` | Sets to val, returns old value | `int` |
| `compareAndSet(int expected, int update)` | Sets to update if current == expected | `boolean` |
| `compareAndExchange(int expected, int update)` | CAS — returns witness value (Java 9+) | `int` |
| `getAndIncrement()` | Atomically increments by 1, returns old value | `int` |
| `getAndDecrement()` | Atomically decrements by 1, returns old value | `int` |
| `getAndAdd(int delta)` | Atomically adds delta, returns old value | `int` |
| `incrementAndGet()` | Atomically increments by 1, returns new value | `int` |
| `decrementAndGet()` | Atomically decrements by 1, returns new value | `int` |
| `addAndGet(int delta)` | Atomically adds delta, returns new value | `int` |
| `getAndUpdate(IntUnaryOperator fn)` | Atomically applies fn, returns old value | `int` |
| `updateAndGet(IntUnaryOperator fn)` | Atomically applies fn, returns new value | `int` |
| `getAndAccumulate(int x, IntBinaryOperator fn)` | Applies fn(current, x), returns old | `int` |
| `accumulateAndGet(int x, IntBinaryOperator fn)` | Applies fn(current, x), returns new | `int` |
| `intValue()` / `longValue()` | Returns value as int/long | `int`/`long` |
| `lazySet(int val)` | Eventually sets; no full memory barrier (Java 6+) | `void` |

### Examples — AtomicInteger

#### Basic Operations

```java
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerBasics {
    public static void main(String[] args) {
        AtomicInteger ai = new AtomicInteger(10);

        System.out.println("Initial    : " + ai.get());            // 10

        System.out.println("getAndIncr : " + ai.getAndIncrement()); // 10 (then 11)
        System.out.println("After incr : " + ai.get());             // 11

        System.out.println("incrAndGet : " + ai.incrementAndGet()); // 12
        System.out.println("getAndAdd  : " + ai.getAndAdd(5));      // 12 (then 17)
        System.out.println("addAndGet  : " + ai.addAndGet(-3));     // 14

        // compareAndSet — sets to 99 ONLY IF current value is 14
        boolean success = ai.compareAndSet(14, 99);
        System.out.println("CAS 14→99  : " + success + " | value=" + ai.get()); // true | 99

        // compareAndSet — fails because current value is 99, not 14
        boolean fail = ai.compareAndSet(14, 0);
        System.out.println("CAS 14→0   : " + fail + " | value=" + ai.get());    // false | 99

        // updateAndGet with lambda (Java 8+)
        int newVal = ai.updateAndGet(v -> v * 2);
        System.out.println("x2         : " + newVal);  // 198

        // accumulateAndGet with custom function
        int max = ai.accumulateAndGet(250, Math::max);
        System.out.println("max(198,250): " + max);   // 250
    }
}
```

---

## `AtomicLong`

Identical API to `AtomicInteger` but for `long` values. Essential for counters, IDs, and timestamps that exceed `Integer.MAX_VALUE`.

### Key Methods — AtomicLong

Same as `AtomicInteger` but for `long`. Key additions:

| Method | Description |
|---|---|
| `getAndIncrement()` | Atomically increments by 1L, returns old value |
| `incrementAndGet()` | Atomically increments by 1L, returns new value |
| `compareAndSet(long expected, long update)` | CAS for long |

### Examples — AtomicLong

```java
import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongDemo {

    // Shared sequence generator — safe for concurrent access
    private static final AtomicLong sequence  = new AtomicLong(0);
    private static final AtomicLong totalBytes = new AtomicLong(0);

    public static long nextSequenceId() {
        return sequence.incrementAndGet(); // atomic — always unique, always increasing
    }

    public static void recordBytes(long bytes) {
        totalBytes.addAndGet(bytes);
    }

    public static void main(String[] args) throws InterruptedException {
        int THREADS = 8;
        long[] ids  = new long[THREADS];

        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                ids[idx] = nextSequenceId();
                recordBytes(idx * 1024L);
                System.out.printf("[%s] ID=%d bytes=%dKB total=%dKB%n",
                        Thread.currentThread().getName(),
                        ids[idx], idx,
                        totalBytes.get() / 1024);
            }, "Thread-" + (i + 1));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("\nAll IDs (sorted):");
        java.util.Arrays.sort(ids);
        System.out.println(java.util.Arrays.toString(ids)); // [1, 2, 3, 4, 5, 6, 7, 8]
        System.out.println("Total bytes tracked: " + totalBytes.get());
    }
}

// IDs are always unique and in range [1..8] — guaranteed by AtomicLong
```

---

## `AtomicBoolean`

A boolean value that can be atomically set and checked. Most powerful for **one-time state transitions** — e.g. ensuring something runs exactly once.

### Key Methods — AtomicBoolean

| Method | Description | Returns |
|---|---|---|
| `get()` | Returns current boolean value | `boolean` |
| `set(boolean val)` | Sets to val | `void` |
| `getAndSet(boolean val)` | Sets to val, returns old value | `boolean` |
| `compareAndSet(boolean expected, boolean update)` | Sets if current == expected | `boolean` |
| `lazySet(boolean val)` | Eventually sets (no full barrier) | `void` |

### Examples — AtomicBoolean

```java
import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanDemo {

    // ── Pattern 1: One-time initialization guard ────────────────────────────
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    public static void initialize() {
        // compareAndSet: only the FIRST thread to call this executes the body
        if (initialized.compareAndSet(false, true)) {
            System.out.println("[" + Thread.currentThread().getName()
                    + "] Initializing system... (runs exactly once)");
            // expensive initialization here
        } else {
            System.out.println("[" + Thread.currentThread().getName()
                    + "] Already initialized — skipping");
        }
    }

    // ── Pattern 2: Graceful stop flag ───────────────────────────────────────
    private final AtomicBoolean running = new AtomicBoolean(true);

    public void startWorker() {
        new Thread(() -> {
            System.out.println("Worker started");
            while (running.get()) {
                // do work
                try { Thread.sleep(100); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("Worker stopped gracefully");
        }, "Worker").start();
    }

    public void stopWorker() {
        running.set(false);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== One-Time Init Pattern ===");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(AtomicBooleanDemo::initialize, "Thread-" + (i + 1));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("\n=== Stop Flag Pattern ===");
        AtomicBooleanDemo demo = new AtomicBooleanDemo();
        demo.startWorker();
        Thread.sleep(350);
        demo.stopWorker();
        Thread.sleep(200);
    }
}

// Output:
// === One-Time Init Pattern ===
// [Thread-1] Initializing system... (runs exactly once)
// [Thread-2] Already initialized — skipping
// [Thread-3] Already initialized — skipping
// [Thread-4] Already initialized — skipping
// [Thread-5] Already initialized — skipping
//
// === Stop Flag Pattern ===
// Worker started
// Worker stopped gracefully
```

---

## `AtomicReference`

An object reference that can be atomically read and updated. Enables lock-free replacement of entire objects.

### Key Methods — AtomicReference

| Method | Description | Returns |
|---|---|---|
| `get()` | Returns current reference | `V` |
| `set(V val)` | Sets to val | `void` |
| `getAndSet(V val)` | Sets to val, returns old reference | `V` |
| `compareAndSet(V expected, V update)` | Sets if current == expected (reference equality) | `boolean` |
| `compareAndExchange(V expected, V update)` | CAS — returns witness value (Java 9+) | `V` |
| `getAndUpdate(UnaryOperator<V> fn)` | Atomically applies fn, returns old value | `V` |
| `updateAndGet(UnaryOperator<V> fn)` | Atomically applies fn, returns new value | `V` |
| `getAndAccumulate(V x, BinaryOperator<V> fn)` | Applies fn(current, x), returns old | `V` |
| `accumulateAndGet(V x, BinaryOperator<V> fn)` | Applies fn(current, x), returns new | `V` |

> ⚠️ `compareAndSet` uses **reference equality** (`==`), not `.equals()`. Two different objects with the same content are NOT considered equal by CAS.

### Examples — AtomicReference

```java
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceDemo {

    // Immutable snapshot — replaced atomically as a whole
    static final class AppConfig {
        final String host;
        final int    port;
        final int    timeout;

        AppConfig(String host, int port, int timeout) {
            this.host    = host;
            this.port    = port;
            this.timeout = timeout;
        }

        @Override public String toString() {
            return host + ":" + port + " (timeout=" + timeout + "ms)";
        }
    }

    // Atomic reference to an immutable config — lock-free hot-reload
    private static final AtomicReference<AppConfig> config =
            new AtomicReference<>(new AppConfig("localhost", 8080, 3000));

    // Reader — always gets a consistent snapshot
    public static AppConfig getConfig() {
        return config.get(); // single volatile read — always consistent
    }

    // Writer — atomically replaces the entire config
    public static void updateConfig(AppConfig newConfig) {
        AppConfig old = config.getAndSet(newConfig);
        System.out.printf("[%s] Config updated: %s → %s%n",
                Thread.currentThread().getName(), old, newConfig);
    }

    // CAS-based conditional update — only update if the expected version matches
    public static boolean updateIfCurrent(AppConfig expected, AppConfig newConfig) {
        boolean updated = config.compareAndSet(expected, newConfig);
        if (updated) {
            System.out.printf("[%s] CAS update succeeded: %s%n",
                    Thread.currentThread().getName(), newConfig);
        } else {
            System.out.printf("[%s] CAS update FAILED — config was already changed%n",
                    Thread.currentThread().getName());
        }
        return updated;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Initial config: " + getConfig());

        // Multiple readers — no locking needed
        for (int i = 0; i < 4; i++) {
            new Thread(() ->
                System.out.println(Thread.currentThread().getName()
                        + " reads: " + getConfig()),
                "Reader-" + i
            ).start();
        }

        Thread.sleep(50);

        // Writer updates atomically
        new Thread(() ->
            updateConfig(new AppConfig("prod-server", 9090, 5000)),
            "Writer-1"
        ).start();

        Thread.sleep(50);

        // CAS-based conditional update
        AppConfig current  = getConfig();
        AppConfig proposed = new AppConfig("prod-server", 9090, 10000);
        new Thread(() -> updateIfCurrent(current, proposed), "CASWriter").start();
    }
}
```

---

## `AtomicIntegerArray`, `AtomicLongArray`, `AtomicReferenceArray`

Array variants where **each individual element** is atomically updatable — no need to synchronize the entire array.

### Key Methods — Atomic Arrays

| Method | Description |
|---|---|
| `get(int index)` | Returns element at index |
| `set(int index, int val)` | Sets element at index |
| `getAndSet(int index, int val)` | Sets element, returns old value |
| `compareAndSet(int index, int expected, int update)` | CAS on element at index |
| `getAndIncrement(int index)` | Atomically increments element, returns old |
| `incrementAndGet(int index)` | Atomically increments element, returns new |
| `getAndAdd(int index, int delta)` | Atomically adds delta, returns old |
| `addAndGet(int index, int delta)` | Atomically adds delta, returns new |
| `length()` | Returns array length |

### Examples — Atomic Arrays

```java
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicArrayDemo {

    // ── Example 1: Bucketed hit counter (per-second traffic tracking) ─────────
    static final int BUCKETS        = 60; // one bucket per second
    static final AtomicIntegerArray hitCounts = new AtomicIntegerArray(BUCKETS);

    public static void recordHit(int secondOfMinute) {
        hitCounts.incrementAndGet(secondOfMinute % BUCKETS);
    }

    // ── Example 2: Parallel sum using atomic long array for partial sums ──────
    static final int DATA_SIZE = 1_000_000;
    static final int SEGMENTS  = 4;
    static final AtomicLongArray partialSums = new AtomicLongArray(SEGMENTS);

    public static long parallelSum(int[] data) throws InterruptedException {
        int segmentSize = data.length / SEGMENTS;
        Thread[] workers = new Thread[SEGMENTS];

        for (int s = 0; s < SEGMENTS; s++) {
            final int segIdx = s;
            final int from   = s * segmentSize;
            final int to     = (s == SEGMENTS - 1) ? data.length : from + segmentSize;

            workers[s] = new Thread(() -> {
                long sum = 0;
                for (int i = from; i < to; i++) sum += data[i];
                partialSums.addAndGet(segIdx, sum); // atomic add to this segment's cell
            }, "Worker-" + s);
        }

        for (Thread w : workers) w.start();
        for (Thread w : workers) w.join();

        long total = 0;
        for (int s = 0; s < SEGMENTS; s++) total += partialSums.get(s);
        return total;
    }

    public static void main(String[] args) throws InterruptedException {
        // Hit counter demo
        System.out.println("=== Bucketed Hit Counter ===");
        for (int i = 0; i < 20; i++) {
            final int second = i % 5; // distribute across 5 buckets
            new Thread(() -> recordHit(second), "HitThread-" + i).start();
        }
        Thread.sleep(200);
        System.out.print("Hits per second: ");
        for (int s = 0; s < 5; s++) System.out.print("s" + s + "=" + hitCounts.get(s) + " ");
        System.out.println();

        // Parallel sum demo
        System.out.println("\n=== Parallel Sum ===");
        int[] data = new int[DATA_SIZE];
        long expectedSum = 0;
        for (int i = 0; i < DATA_SIZE; i++) { data[i] = i + 1; expectedSum += data[i]; }

        long result = parallelSum(data);
        System.out.println("Expected sum : " + expectedSum);
        System.out.println("Parallel sum : " + result);
        System.out.println("Match        : " + (expectedSum == result)); // always true ✅
    }
}
```

---

## `AtomicIntegerFieldUpdater`, `AtomicLongFieldUpdater`, `AtomicReferenceFieldUpdater`

Field updaters allow you to **atomically update a `volatile` field in an existing class** without changing the class to use `AtomicInteger`. They are useful for reducing memory overhead when you have many instances — each instance stores a raw `volatile` field, not a full `AtomicInteger` object.

### Examples — Field Updaters

```java
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class FieldUpdaterDemo {

    // The class with volatile fields — NOT AtomicInteger
    static class Node {
        volatile int    refCount  = 0;
        volatile String state     = "INIT";
        final    String id;

        Node(String id) { this.id = id; }
    }

    // Field updaters — created ONCE per class (expensive), used many times (cheap)
    private static final AtomicIntegerFieldUpdater<Node> REF_COUNT_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(Node.class, "refCount");

    private static final AtomicReferenceFieldUpdater<Node, String> STATE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, String.class, "state");

    public static void main(String[] args) throws InterruptedException {
        Node node = new Node("NODE-1");

        // Atomically increment refCount on existing Node — no AtomicInteger needed
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                int count = REF_COUNT_UPDATER.incrementAndGet(node);
                System.out.println(Thread.currentThread().getName()
                        + ": refCount=" + count);
            }, "Thread-" + (i + 1));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("Final refCount: " + node.refCount); // always 5

        // Atomically transition state
        boolean transitioned = STATE_UPDATER.compareAndSet(node, "INIT", "ACTIVE");
        System.out.println("INIT→ACTIVE : " + transitioned + " | state=" + node.state);

        transitioned = STATE_UPDATER.compareAndSet(node, "INIT", "ACTIVE"); // fails
        System.out.println("INIT→ACTIVE : " + transitioned + " | state=" + node.state);

        transitioned = STATE_UPDATER.compareAndSet(node, "ACTIVE", "CLOSED");
        System.out.println("ACTIVE→CLOSED: " + transitioned + " | state=" + node.state);
    }
}

// Output:
// Thread-1: refCount=1
// Thread-3: refCount=2
// Thread-2: refCount=3
// Thread-5: refCount=4
// Thread-4: refCount=5
// Final refCount: 5
// INIT→ACTIVE  : true  | state=ACTIVE
// INIT→ACTIVE  : false | state=ACTIVE
// ACTIVE→CLOSED: true  | state=CLOSED
```

---

## High-Performance Accumulators (Java 8+)

### `LongAdder` and `DoubleAdder`

`LongAdder` is optimized for high-contention counters. Instead of a single value with CAS retries, it maintains a **set of cells** — each thread updates its own cell, and the total is summed on demand.

```
AtomicLong (one shared cell):
  Thread-1 ──► CAS ──► retry ──► retry ──► success
  Thread-2 ──► CAS ──► retry ──► success
  Thread-3 ──► CAS ──► success
  (threads compete for ONE cell — high contention)

LongAdder (distributed cells):
  Thread-1 ──► Cell-0 (no contention!)
  Thread-2 ──► Cell-1 (no contention!)
  Thread-3 ──► Cell-2 (no contention!)
  sum() = Cell-0 + Cell-1 + Cell-2 (done on read)
  (threads mostly use separate cells — minimal contention)
```

| Method | Description |
|---|---|
| `add(long x)` | Adds x to the sum |
| `increment()` | Adds 1 to the sum |
| `decrement()` | Subtracts 1 from the sum |
| `sum()` | Returns the current sum (approximate under concurrent updates) |
| `sumThenReset()` | Returns and resets the sum atomically |
| `reset()` | Resets the sum to zero |
| `longValue()` | Same as `sum()` |

> ⚠️ `LongAdder.sum()` is **not a snapshot** — it may miss concurrent updates. Use `AtomicLong` when you need exact point-in-time values.

### `LongAccumulator` and `DoubleAccumulator`

Generalized version of `LongAdder` that supports **any associative, commutative** binary operation (not just addition):

```java
// Maximum tracker — accumulates the maximum seen value
LongAccumulator maxTracker = new LongAccumulator(Math::max, Long.MIN_VALUE);
maxTracker.accumulate(42);
maxTracker.accumulate(17);
maxTracker.accumulate(99);
System.out.println(maxTracker.get()); // 99

// Product accumulator — accumulates the product of all values
LongAccumulator product = new LongAccumulator((a, b) -> a * b, 1L);
product.accumulate(2);
product.accumulate(3);
product.accumulate(5);
System.out.println(product.get()); // 30
```

### Examples — Adders and Accumulators

```java
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.LongAccumulator;

public class AdderAccumulatorDemo {

    public static void main(String[] args) throws InterruptedException {
        int THREADS    = 8;
        int OPS_EACH   = 100_000;

        // ── LongAdder vs AtomicLong ───────────────────────────────────────────
        AtomicLong atomicCounter = new AtomicLong(0);
        LongAdder  adderCounter  = new LongAdder();

        Runnable atomicTask = () -> {
            for (int i = 0; i < OPS_EACH; i++) atomicCounter.incrementAndGet();
        };
        Runnable adderTask = () -> {
            for (int i = 0; i < OPS_EACH; i++) adderCounter.increment();
        };

        // Time AtomicLong
        Thread[] aThreads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) aThreads[i] = new Thread(atomicTask);
        long aStart = System.nanoTime();
        for (Thread t : aThreads) t.start();
        for (Thread t : aThreads) t.join();
        long aTime = System.nanoTime() - aStart;

        // Time LongAdder
        Thread[] dThreads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) dThreads[i] = new Thread(adderTask);
        long dStart = System.nanoTime();
        for (Thread t : dThreads) t.start();
        for (Thread t : dThreads) t.join();
        long dTime = System.nanoTime() - dStart;

        long expected = (long) THREADS * OPS_EACH;
        System.out.println("Expected        : " + expected);
        System.out.println("AtomicLong sum  : " + atomicCounter.get()
                + " | time=" + aTime / 1_000_000 + "ms");
        System.out.println("LongAdder sum   : " + adderCounter.sum()
                + " | time=" + dTime / 1_000_000 + "ms");
        System.out.println("LongAdder speedup: ~" + (aTime / Math.max(dTime, 1)) + "x faster");

        // ── LongAccumulator — custom operations ───────────────────────────────
        System.out.println("\n=== LongAccumulator Examples ===");

        LongAccumulator maxValue = new LongAccumulator(Math::max, Long.MIN_VALUE);
        LongAccumulator minValue = new LongAccumulator(Math::min, Long.MAX_VALUE);

        int[] values = {42, 17, 99, 5, 73, 28, 56, 11};
        Thread[] workers = new Thread[values.length];
        for (int i = 0; i < values.length; i++) {
            final int v = values[i];
            workers[i] = new Thread(() -> {
                maxValue.accumulate(v);
                minValue.accumulate(v);
            }, "Worker-" + i);
        }
        for (Thread w : workers) w.start();
        for (Thread w : workers) w.join();

        System.out.println("Max value: " + maxValue.get()); // 99
        System.out.println("Min value: " + minValue.get()); // 5

        // sumThenReset — for periodic metric reporting
        LongAdder requestCount = new LongAdder();
        for (int i = 0; i < 1000; i++) requestCount.increment();
        long periodCount = requestCount.sumThenReset(); // report and reset atomically
        System.out.println("\nPeriod requests: " + periodCount); // 1000
        System.out.println("After reset    : " + requestCount.sum()); // 0
    }
}

// Sample Output (LongAdder is typically 2–5x faster under high contention):
// Expected        : 800000
// AtomicLong sum  : 800000 | time=45ms
// LongAdder sum   : 800000 | time=12ms
// LongAdder speedup: ~3x faster
```

---

## The ABA Problem

### What is the ABA Problem?

CAS checks if a value **equals** the expected value. But what if another thread changed it from A to B and back to A? The CAS sees A and thinks nothing happened — but the value was modified.

```
ABA scenario with a lock-free stack (top of stack):

Initial state: stack top → Node-A → Node-B → Node-C

Thread-1: reads top = Node-A (about to pop)
   ← Thread-1 is paused here →

Thread-2: pops Node-A, pops Node-B, pushes Node-A back
          stack top → Node-A → Node-C

Thread-1: resumes → CAS(expected=Node-A, new=Node-B)
          top is still Node-A → CAS SUCCEEDS ✅
          BUT Node-B was already popped and possibly freed — DANGLING POINTER ❌

The problem: CAS saw A again but the world had changed significantly
```

### `AtomicStampedReference` — Solving ABA

`AtomicStampedReference` pairs a reference with an integer **stamp** (version counter). Both the reference AND the stamp must match for CAS to succeed:

```java
import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicStampedReferenceDemo {

    public static void main(String[] args) {
        String initial = "StateA";
        AtomicStampedReference<String> ref =
                new AtomicStampedReference<>(initial, 0); // initial stamp = 0

        System.out.println("Initial: " + ref.getReference() + " stamp=" + ref.getStamp());

        // Read current reference and stamp together
        int[] stampHolder = new int[1];
        String current    = ref.get(stampHolder); // fills stampHolder[0] with stamp
        int    currentStamp = stampHolder[0];
        System.out.println("Read: " + current + " stamp=" + currentStamp);

        // Simulate ABA: value changes A → B → A with stamps
        ref.compareAndSet("StateA", "StateB", 0, 1); // stamp: 0 → 1
        System.out.println("After A→B: " + ref.getReference() + " stamp=" + ref.getStamp());

        ref.compareAndSet("StateB", "StateA", 1, 2); // stamp: 1 → 2
        System.out.println("After B→A: " + ref.getReference() + " stamp=" + ref.getStamp());

        // Thread-1's stale CAS — expected stamp=0 but current stamp=2 → FAILS ✅
        boolean result = ref.compareAndSet("StateA", "StateC", currentStamp, currentStamp + 1);
        System.out.println("Stale CAS (stamp=0): " + result
                + " | " + ref.getReference() + " stamp=" + ref.getStamp());
        // false — ABA detected! Even though value is "StateA", stamp mismatch prevents CAS

        // Fresh CAS with correct stamp — succeeds ✅
        result = ref.compareAndSet("StateA", "StateC", 2, 3);
        System.out.println("Fresh CAS (stamp=2): " + result
                + " | " + ref.getReference() + " stamp=" + ref.getStamp());
    }
}

// Output:
// Initial: StateA stamp=0
// Read: StateA stamp=0
// After A→B: StateB stamp=1
// After B→A: StateA stamp=2
// Stale CAS (stamp=0): false | StateA stamp=2  ← ABA detected ✅
// Fresh CAS (stamp=2): true  | StateC stamp=3
```

### `AtomicMarkableReference`

Like `AtomicStampedReference` but with a single `boolean` mark instead of an integer stamp — useful for **soft deletion** (marking a node as logically deleted without removing it):

```java
import java.util.concurrent.atomic.AtomicMarkableReference;

public class AtomicMarkableReferenceDemo {

    static class Node {
        final int value;
        Node(int value) { this.value = value; }
        @Override public String toString() { return "Node(" + value + ")"; }
    }

    public static void main(String[] args) {
        Node node = new Node(42);

        // Pair: reference + boolean mark (false = active, true = deleted)
        AtomicMarkableReference<Node> ref =
                new AtomicMarkableReference<>(node, false);

        System.out.println("Node: " + ref.getReference() + " | deleted=" + ref.isMarked());

        // Soft-delete: mark as deleted without removing
        boolean[] markHolder = new boolean[1];
        Node current = ref.get(markHolder);
        System.out.println("Before delete: " + current + " marked=" + markHolder[0]);

        // Mark as deleted (false → true) — only succeeds if currently unmarked
        boolean marked = ref.compareAndSet(node, node, false, true);
        System.out.println("Mark as deleted: " + marked + " | marked=" + ref.isMarked());

        // Attempt to re-process an already-deleted node — fails ✅
        marked = ref.compareAndSet(node, node, false, true);
        System.out.println("Re-delete (fails): " + marked); // false — already marked
    }
}
```

---

## Practical Examples

### Example 1 — Thread-Safe Counter

```java
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeCounter {

    private final AtomicInteger count = new AtomicInteger(0);

    public int increment()        { return count.incrementAndGet(); }
    public int decrement()        { return count.decrementAndGet(); }
    public int add(int delta)     { return count.addAndGet(delta); }
    public int get()              { return count.get(); }
    public void reset()           { count.set(0); }

    // Conditional increment — only increment if below a max
    public boolean incrementIfBelow(int max) {
        int current;
        do {
            current = count.get();
            if (current >= max) return false;
        } while (!count.compareAndSet(current, current + 1));
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        int THREADS = 10, OPS = 1000;

        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPS; j++) counter.increment();
            }, "Thread-" + i);
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("Expected : " + (THREADS * OPS));
        System.out.println("Actual   : " + counter.get()); // always 10000 ✅

        // Conditional increment demo
        counter.reset();
        int MAX = 5;
        for (int i = 0; i < 8; i++) {
            boolean success = counter.incrementIfBelow(MAX);
            System.out.println("incrementIfBelow(" + MAX + "): " + success
                    + " | count=" + counter.get());
        }
    }
}

// Output:
// Expected : 10000
// Actual   : 10000
// incrementIfBelow(5): true  | count=1
// incrementIfBelow(5): true  | count=2
// incrementIfBelow(5): true  | count=3
// incrementIfBelow(5): true  | count=4
// incrementIfBelow(5): true  | count=5
// incrementIfBelow(5): false | count=5
// incrementIfBelow(5): false | count=5
// incrementIfBelow(5): false | count=5
```

---

### Example 2 — Non-Blocking Stack (Lock-Free)

```java
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<T> {

    private static class Node<T> {
        final T       value;
        final Node<T> next;
        Node(T value, Node<T> next) { this.value = value; this.next = next; }
    }

    private final AtomicReference<Node<T>> top = new AtomicReference<>(null);

    // Lock-free push
    public void push(T value) {
        Node<T> newNode;
        do {
            Node<T> currentTop = top.get();
            newNode = new Node<>(value, currentTop);
        } while (!top.compareAndSet(newNode.next, newNode)); // retry if top changed
    }

    // Lock-free pop
    public T pop() {
        Node<T> currentTop;
        do {
            currentTop = top.get();
            if (currentTop == null) return null; // empty stack
        } while (!top.compareAndSet(currentTop, currentTop.next));
        return currentTop.value;
    }

    public boolean isEmpty() { return top.get() == null; }

    public static void main(String[] args) throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();

        // Multiple threads push concurrently
        Thread[] pushers = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final int base = i * 10;
            pushers[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    stack.push(base + j);
                    System.out.println(Thread.currentThread().getName()
                            + " pushed: " + (base + j));
                }
            }, "Pusher-" + i);
        }
        for (Thread p : pushers) p.start();
        for (Thread p : pushers) p.join();

        // Pop all elements
        System.out.println("\nPopping all:");
        Integer val;
        int count = 0;
        while ((val = stack.pop()) != null) {
            System.out.print(val + " ");
            count++;
        }
        System.out.println("\nTotal elements: " + count); // always 20
    }
}
```

---

### Example 3 — compareAndSet for One-Time State Transition

```java
import java.util.concurrent.atomic.AtomicInteger;

public class StateMachine {

    // States
    static final int IDLE       = 0;
    static final int PROCESSING = 1;
    static final int DONE       = 2;
    static final int FAILED     = 3;

    private final AtomicInteger state = new AtomicInteger(IDLE);
    private final String        name;

    StateMachine(String name) { this.name = name; }

    public boolean start() {
        // Only transition IDLE → PROCESSING (exactly once, even with concurrent calls)
        if (state.compareAndSet(IDLE, PROCESSING)) {
            System.out.println(name + ": IDLE → PROCESSING ✅");
            return true;
        }
        System.out.println(name + ": already started (state=" + state.get() + ")");
        return false;
    }

    public boolean complete() {
        if (state.compareAndSet(PROCESSING, DONE)) {
            System.out.println(name + ": PROCESSING → DONE ✅");
            return true;
        }
        System.out.println(name + ": cannot complete (state=" + state.get() + ")");
        return false;
    }

    public boolean fail() {
        // Can fail from either IDLE or PROCESSING
        int current = state.get();
        if ((current == IDLE || current == PROCESSING)
                && state.compareAndSet(current, FAILED)) {
            System.out.println(name + ": → FAILED ✅");
            return true;
        }
        System.out.println(name + ": cannot fail (state=" + state.get() + ")");
        return false;
    }

    public String getStateName() {
        return switch (state.get()) {
            case IDLE       -> "IDLE";
            case PROCESSING -> "PROCESSING";
            case DONE       -> "DONE";
            case FAILED     -> "FAILED";
            default         -> "UNKNOWN";
        };
    }

    public static void main(String[] args) throws InterruptedException {
        StateMachine job = new StateMachine("Job-1");

        // 5 threads all try to start the job — only one should succeed
        Thread[] starters = new Thread[5];
        for (int i = 0; i < 5; i++) {
            starters[i] = new Thread(job::start, "Starter-" + (i + 1));
        }
        for (Thread s : starters) s.start();
        for (Thread s : starters) s.join();

        System.out.println("State: " + job.getStateName());

        job.complete();
        System.out.println("State: " + job.getStateName());

        job.complete(); // fails — already DONE
        job.fail();     // fails — already DONE
    }
}

// Output:
// Job-1: IDLE → PROCESSING ✅
// Job-1: already started (state=1)  × 4
// State: PROCESSING
// Job-1: PROCESSING → DONE ✅
// State: DONE
// Job-1: cannot complete (state=2)
// Job-1: cannot fail (state=2)
```

---

### Example 4 — Atomic Maximum Tracker

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicMaxTracker {

    private final AtomicInteger maxValue  = new AtomicInteger(Integer.MIN_VALUE);
    private final AtomicInteger minValue  = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicLong    totalSum  = new AtomicLong(0);
    private final AtomicInteger count     = new AtomicInteger(0);

    public void record(int value) {
        // Atomic max update — CAS retry loop
        int current;
        do {
            current = maxValue.get();
        } while (value > current && !maxValue.compareAndSet(current, value));

        // Atomic min update — CAS retry loop
        do {
            current = minValue.get();
        } while (value < current && !minValue.compareAndSet(current, value));

        totalSum.addAndGet(value);
        count.incrementAndGet();
    }

    public int    getMax()     { return maxValue.get(); }
    public int    getMin()     { return minValue.get(); }
    public long   getSum()     { return totalSum.get(); }
    public int    getCount()   { return count.get(); }
    public double getAverage() {
        int c = count.get();
        return c == 0 ? 0.0 : (double) totalSum.get() / c;
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicMaxTracker tracker = new AtomicMaxTracker();
        int THREADS = 6;

        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int base = i * 10;
            threads[i] = new Thread(() -> {
                java.util.Random rnd = new java.util.Random();
                for (int j = 0; j < 100; j++) {
                    tracker.record(base + rnd.nextInt(10));
                }
            }, "Thread-" + i);
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("Count  : " + tracker.getCount());    // 600
        System.out.println("Max    : " + tracker.getMax());
        System.out.println("Min    : " + tracker.getMin());
        System.out.println("Sum    : " + tracker.getSum());
        System.out.printf ("Average: %.2f%n", tracker.getAverage());
    }
}
```

---

### Example 5 — Request Rate Limiter with AtomicInteger

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicRateLimiter {

    private final int         maxRequests;   // max allowed per window
    private final long        windowMs;      // window size in ms
    private final AtomicInteger reqCount    = new AtomicInteger(0);
    private final AtomicLong    windowStart = new AtomicLong(System.currentTimeMillis());

    public AtomicRateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs    = windowMs;
    }

    public boolean tryAcquire() {
        long now    = System.currentTimeMillis();
        long winStart = windowStart.get();

        // Reset window if expired — CAS ensures only one thread resets
        if (now - winStart >= windowMs) {
            if (windowStart.compareAndSet(winStart, now)) {
                reqCount.set(0); // reset counter for new window
            }
        }

        // Atomically increment and check
        int current = reqCount.incrementAndGet();
        if (current <= maxRequests) {
            return true; // within limit
        } else {
            reqCount.decrementAndGet(); // over limit — undo increment
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicRateLimiter limiter = new AtomicRateLimiter(5, 1000); // 5 req/sec

        System.out.println("=== Rate Limiter (5 req/sec) ===");
        for (int i = 1; i <= 8; i++) {
            final int reqId = i;
            new Thread(() -> {
                boolean allowed = limiter.tryAcquire();
                System.out.printf("Request-%-2d : %s%n", reqId,
                        allowed ? "✅ ALLOWED" : "❌ BLOCKED");
            }, "Req-" + i).start();
        }

        Thread.sleep(1100); // wait for window to reset

        System.out.println("\n--- After 1s window reset ---");
        for (int i = 9; i <= 12; i++) {
            final int reqId = i;
            new Thread(() -> {
                boolean allowed = limiter.tryAcquire();
                System.out.printf("Request-%-2d : %s%n", reqId,
                        allowed ? "✅ ALLOWED" : "❌ BLOCKED");
            }, "Req-" + i).start();
        }
        Thread.sleep(200);
    }
}
```

---

### Example 6 — Thread-Safe Singleton with AtomicReference

```java
import java.util.concurrent.atomic.AtomicReference;

public class AtomicSingleton {

    private static class Config {
        final String env;
        final int    workers;
        Config(String env, int workers) { this.env = env; this.workers = workers; }
        @Override public String toString() { return env + "/" + workers + " workers"; }
    }

    // null = not yet initialized
    private static final AtomicReference<Config> INSTANCE = new AtomicReference<>(null);

    public static Config getInstance() {
        Config existing = INSTANCE.get();
        if (existing != null) return existing;

        // First call — create a new instance
        Config candidate = new Config("production", Runtime.getRuntime().availableProcessors());

        // Only one thread wins — losers discard their candidate
        if (INSTANCE.compareAndSet(null, candidate)) {
            System.out.println("[" + Thread.currentThread().getName()
                    + "] Created singleton: " + candidate);
            return candidate;
        }

        // Another thread won the race — return their instance
        return INSTANCE.get();
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[8];
        for (int i = 0; i < 8; i++) {
            threads[i] = new Thread(() -> {
                Config c = getInstance();
                System.out.println(Thread.currentThread().getName()
                        + " → " + c + " [same=" + (c == getInstance()) + "]");
            }, "Thread-" + (i + 1));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}
// Output: "Created singleton" appears EXACTLY ONCE, all threads use the same instance
```

---

### Example 7 — Lock-Free Linked List Node Append

```java
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> {

    private static class Node<T> {
        final T                   value;
        final AtomicReference<Node<T>> next = new AtomicReference<>(null);
        Node(T value) { this.value = value; }
    }

    private final Node<T>             sentinel = new Node<>(null);      // dummy head
    private final AtomicReference<Node<T>> head = new AtomicReference<>(sentinel);
    private final AtomicReference<Node<T>> tail = new AtomicReference<>(sentinel);

    // Lock-free enqueue (Michael-Scott algorithm)
    public void enqueue(T value) {
        Node<T> newNode = new Node<>(value);
        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();
            if (last == tail.get()) { // consistent snapshot check
                if (next == null) {
                    // tail is truly last — try to append
                    if (last.next.compareAndSet(null, newNode)) {
                        tail.compareAndSet(last, newNode); // advance tail
                        return;
                    }
                } else {
                    tail.compareAndSet(last, next); // tail is behind — advance it
                }
            }
        }
    }

    // Lock-free dequeue
    public T dequeue() {
        while (true) {
            Node<T> first = head.get();
            Node<T> last  = tail.get();
            Node<T> next  = first.next.get();
            if (first == head.get()) {
                if (first == last) {
                    if (next == null) return null; // empty
                    tail.compareAndSet(last, next);
                } else {
                    T value = next.value;
                    if (head.compareAndSet(first, next)) return value;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockFreeQueue<String> queue = new LockFreeQueue<>();

        // Concurrent producers
        Thread p1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) queue.enqueue("P1-msg-" + i);
        }, "Producer-1");
        Thread p2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) queue.enqueue("P2-msg-" + i);
        }, "Producer-2");

        p1.start(); p2.start(); p1.join(); p2.join();

        // Drain the queue
        String msg;
        int count = 0;
        while ((msg = queue.dequeue()) != null) { count++; }
        System.out.println("Total dequeued: " + count); // always 10
    }
}
```

---

### Example 8 — LongAdder vs AtomicLong (Performance)

```java
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class PerformanceComparison {

    static long benchmark(Runnable task, int threads) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) ts[i] = new Thread(task);
        long start = System.nanoTime();
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        return System.nanoTime() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        int THREADS = 16, OPS = 500_000;

        AtomicLong atomicLong = new AtomicLong(0);
        LongAdder  longAdder  = new LongAdder();

        long atomicTime = benchmark(
                () -> { for (int i = 0; i < OPS; i++) atomicLong.incrementAndGet(); },
                THREADS);

        long adderTime = benchmark(
                () -> { for (int i = 0; i < OPS; i++) longAdder.increment(); },
                THREADS);

        System.out.printf("%-15s sum=%d  time=%dms%n",
                "AtomicLong:", atomicLong.get(), atomicTime / 1_000_000);
        System.out.printf("%-15s sum=%d  time=%dms%n",
                "LongAdder:", longAdder.sum(), adderTime / 1_000_000);
        System.out.printf("LongAdder is ~%.1fx faster under %d-thread contention%n",
                (double) atomicTime / Math.max(adderTime, 1), THREADS);

        // When to prefer each:
        System.out.println("\nAtomicLong  : best when you need exact current value often");
        System.out.println("LongAdder   : best for pure increment/decrement counters");
    }
}

// Sample Output (16 threads, high contention):
// AtomicLong:  sum=8000000  time=312ms
// LongAdder:   sum=8000000  time=67ms
// LongAdder is ~4.7x faster under 16-thread contention
```

---

### Example 9 — AtomicStampedReference (ABA Fix)

```java
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABAFixDemo {

    // Simple node for a linked list
    static class Node {
        int value; Node next;
        Node(int v) { this.value = v; }
        @Override public String toString() { return "N(" + value + ")"; }
    }

    // Stack top — stamped to detect ABA
    private final AtomicStampedReference<Node> top =
            new AtomicStampedReference<>(null, 0);

    public void push(int value) {
        Node newNode = new Node(value);
        int[] stamp  = new int[1];
        do {
            Node current = top.get(stamp);
            newNode.next = current;
        } while (!top.compareAndSet(newNode.next, newNode, stamp[0], stamp[0] + 1));
        System.out.println(Thread.currentThread().getName()
                + " pushed: " + value + " | stamp=" + (top.getStamp()));
    }

    public Integer pop() {
        int[] stamp = new int[1];
        Node  current;
        do {
            current = top.get(stamp);
            if (current == null) return null;
        } while (!top.compareAndSet(current, current.next, stamp[0], stamp[0] + 1));
        System.out.println(Thread.currentThread().getName()
                + " popped: " + current.value + " | stamp=" + top.getStamp());
        return current.value;
    }

    public static void main(String[] args) throws InterruptedException {
        ABAFixDemo stack = new ABAFixDemo();

        stack.push(1); stack.push(2); stack.push(3);
        System.out.println("Stack top stamp: " + stack.top.getStamp());

        Thread[] threads = new Thread[4];
        threads[0] = new Thread(() -> stack.push(10), "Pusher-1");
        threads[1] = new Thread(() -> stack.push(20), "Pusher-2");
        threads[2] = new Thread(() -> stack.pop(),    "Popper-1");
        threads[3] = new Thread(() -> stack.pop(),    "Popper-2");

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("Final stamp: " + stack.top.getStamp());
        // Each operation increments stamp — ABA impossible ✅
    }
}
```

---

### Example 10 — Statistics Tracker with Multiple Atomics

```java
import java.util.concurrent.atomic.*;

public class RequestStats {

    private final AtomicLong  totalRequests   = new AtomicLong(0);
    private final AtomicLong  successCount    = new AtomicLong(0);
    private final AtomicLong  failureCount    = new AtomicLong(0);
    private final AtomicLong  totalLatencyMs  = new AtomicLong(0);
    private final LongAdder   activeRequests  = new LongAdder();
    private final LongAccumulator maxLatency  =
            new LongAccumulator(Math::max, 0L);
    private final LongAccumulator minLatency  =
            new LongAccumulator(Math::min, Long.MAX_VALUE);

    public void recordRequest(boolean success, long latencyMs) {
        totalRequests.incrementAndGet();
        totalLatencyMs.addAndGet(latencyMs);
        maxLatency.accumulate(latencyMs);
        minLatency.accumulate(latencyMs);
        if (success) successCount.incrementAndGet();
        else         failureCount.incrementAndGet();
    }

    public void requestStarted()  { activeRequests.increment(); }
    public void requestFinished() { activeRequests.decrement(); }

    public void printStats() {
        long total   = totalRequests.get();
        long success = successCount.get();
        long failure = failureCount.get();
        long latency = totalLatencyMs.get();

        System.out.println("=== Request Statistics ===");
        System.out.println("Total Requests  : " + total);
        System.out.println("Success         : " + success
                + " (" + (total > 0 ? success * 100 / total : 0) + "%)");
        System.out.println("Failures        : " + failure);
        System.out.println("Active Now      : " + activeRequests.sum());
        System.out.printf ("Avg Latency     : %.1f ms%n",
                total > 0 ? (double) latency / total : 0.0);
        System.out.println("Max Latency     : " + maxLatency.get() + " ms");
        System.out.println("Min Latency     : " + minLatency.get() + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        RequestStats stats = new RequestStats();
        java.util.Random rnd = new java.util.Random();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    stats.requestStarted();
                    long latency = 10 + rnd.nextInt(490); // 10–500ms
                    boolean ok   = rnd.nextDouble() > 0.1; // 90% success
                    try { Thread.sleep(latency / 50); } // compressed simulation
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    stats.recordRequest(ok, latency);
                    stats.requestFinished();
                }
            }, "Worker-" + i);
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        stats.printStats();
    }
}

// Sample Output:
// === Request Statistics ===
// Total Requests  : 1000
// Success         : 905 (90%)
// Failures        : 95
// Active Now      : 0
// Avg Latency     : 254.3 ms
// Max Latency     : 499 ms
// Min Latency     : 10 ms
```

---

## Atomic Classes vs `synchronized`

| Feature | Atomic Classes | `synchronized` |
|---|---|---|
| **Mutual exclusion** | ❌ No (single-variable operations only) | ✅ Yes — any block of code |
| **Atomicity** | ✅ Single-variable CAS | ✅ Any multi-statement block |
| **Blocking** | ❌ Never blocks (spin retry) | ✅ Can block threads |
| **Deadlock risk** | ❌ None | ⚠️ Possible |
| **Performance (low contention)** | ✅ Faster | Comparable |
| **Performance (high contention)** | ⚠️ CAS spins waste CPU | May be faster (threads sleep) |
| **Visibility** | ✅ Volatile semantics | ✅ Full flush/refresh |
| **Ordering** | ✅ Sequential consistency | ✅ Happens-before on lock |
| **Compound multi-variable ops** | ❌ No — use synchronized | ✅ Yes |
| **wait/notify** | ❌ No | ✅ Yes |
| **Best for** | Single variable, high-read counters | Multi-variable critical sections |

---

## Atomic Classes vs `volatile`

| Feature | Atomic Classes | `volatile` |
|---|---|---|
| **Visibility** | ✅ Yes | ✅ Yes |
| **Atomicity of `++`** | ✅ Yes (`incrementAndGet`) | ❌ No (race condition) |
| **CAS operations** | ✅ Yes | ❌ No |
| **Memory overhead** | Higher (object wrapper) | Lower (field only) |
| **Best for** | Counters, CAS patterns | Simple flags, references |

---

## Atomic Classes vs `ReentrantLock`

| Feature | Atomic Classes | `ReentrantLock` |
|---|---|---|
| **Single variable ops** | ✅ Lock-free, fast | Overkill |
| **Multi-variable ops** | ❌ No | ✅ Yes |
| **Blocking** | ❌ Never | ✅ Can block |
| **Deadlock risk** | ❌ None | ⚠️ Possible |
| **Condition variables** | ❌ No | ✅ Yes |
| **Best for** | Counters, IDs, CAS patterns | Complex critical sections |

---

## Common Pitfalls

### 1. Using Atomic Classes for Multi-Variable Consistency

```java
AtomicInteger x = new AtomicInteger(0);
AtomicInteger y = new AtomicInteger(0);

// ❌ NOT atomic together — another thread may see x updated but not y
x.set(10);
y.set(20); // not an atomic pair operation

// ✅ Use synchronized to update multiple atomics consistently
synchronized (lock) {
    x.set(10);
    y.set(20);
}
// Or use AtomicReference to a single immutable object holding both x and y
```

### 2. Ignoring CAS Return Value

```java
AtomicInteger ai = new AtomicInteger(5);

// ❌ Ignoring the return value — CAS may have failed!
ai.compareAndSet(5, 10); // what if it returned false?

// ✅ Check the return value or use a retry loop
boolean success = ai.compareAndSet(5, 10);
if (!success) {
    System.out.println("CAS failed — value was not 5, it was: " + ai.get());
}

// ✅ Or use the retry pattern
int current;
do {
    current = ai.get();
} while (!ai.compareAndSet(current, current * 2)); // retries until success
```

### 3. Reading Atomic Value Multiple Times (Non-Atomic Snapshot)

```java
AtomicInteger counter = new AtomicInteger(100);

// ❌ Two reads — value may change between them
if (counter.get() > 0) {
    counter.decrementAndGet(); // counter may be 0 now! underflow possible
}

// ✅ Use CAS retry to do check-then-act atomically
int current;
do {
    current = counter.get();
    if (current <= 0) break; // exit if already zero
} while (!counter.compareAndSet(current, current - 1));
```

### 4. Treating `LongAdder.sum()` as a Snapshot

```java
LongAdder adder = new LongAdder();

// ❌ sum() is approximate under concurrent updates
long snapshot = adder.sum(); // may miss concurrent increments

// ✅ Use AtomicLong when you need an exact point-in-time value
AtomicLong exact = new AtomicLong(0);
long snapshot2 = exact.get(); // always the exact current value
```

### 5. ABA Problem — Using AtomicReference Without Version

```java
// ❌ CAS may succeed even though the value was changed and restored
AtomicReference<String> ref = new AtomicReference<>("A");
// ... another thread changes A → B → A ...
ref.compareAndSet("A", "C"); // succeeds — doesn't detect A→B→A cycle

// ✅ Use AtomicStampedReference to detect ABA
AtomicStampedReference<String> stampedRef = new AtomicStampedReference<>("A", 0);
int[] stamp = new int[1];
String current = stampedRef.get(stamp);
// Even if value is "A" again, stamp mismatch prevents stale CAS
stampedRef.compareAndSet(current, "C", stamp[0], stamp[0] + 1);
```

---

## When to Use Each Atomic Class

| Class | When to Use |
|---|---|
| `AtomicInteger` | Thread-safe int counter, ID generator, CAS patterns on int |
| `AtomicLong` | Thread-safe long counter, timestamp, large sequence numbers |
| `AtomicBoolean` | One-time init guard, stop flag with CAS protection |
| `AtomicReference<V>` | Lock-free swap of immutable objects, config hot-swap |
| `AtomicIntegerArray` | Per-index counters (hit buckets, per-slot stats) |
| `AtomicLongArray` | Per-index long counters (large-scale per-bucket metrics) |
| `AtomicReferenceArray<E>` | Per-index object references that need atomic update |
| `AtomicIntegerFieldUpdater` | Atomically update `volatile int` field without boxing overhead |
| `AtomicStampedReference<V>` | CAS on reference + version counter; ABA-safe lock-free structures |
| `AtomicMarkableReference<V>` | Soft-deletion flag; logically delete a node without removal |
| `LongAdder` | High-throughput increment-only counter (prefer over `AtomicLong` under high contention) |
| `DoubleAdder` | High-throughput double summation |
| `LongAccumulator` | High-throughput custom accumulation (max, min, product) |
| `DoubleAccumulator` | High-throughput double custom accumulation |

---

## Summary

| Concept | Key Point |
|---|---|
| **What they are** | Lock-free, thread-safe wrappers using hardware CAS instructions |
| **CAS** | Atomically check-and-update: succeeds only if value matches expected |
| **Never block** | Threads retry instead of sleeping — no deadlock, no context switch |
| **Volatile semantics** | All reads/writes have full memory visibility |
| **Single-variable only** | For multi-variable consistency, still need `synchronized` |
| **`AtomicInteger/Long`** | Core scalar atomics — counters, IDs, flags |
| **`AtomicBoolean`** | One-time transitions, stop flags |
| **`AtomicReference`** | Lock-free object swap; use immutable objects for best results |
| **Atomic arrays** | Per-element atomic operations on arrays |
| **`LongAdder`** | Better than `AtomicLong` for pure counting under high contention |
| **`LongAccumulator`** | Custom reduction functions (max, min, product) at high throughput |
| **ABA problem** | CAS may miss A→B→A cycles — use `AtomicStampedReference` |
| **Don't ignore CAS return** | Always check `compareAndSet()` result or use a retry loop |
| **Not for compound ops** | `x++; y++;` must still be in `synchronized` for atomicity |

**Quick Selection Guide:**

```
Need thread-safe shared state?
         │
         ├─ Simple int counter / ID?
         │         └─ AtomicInteger / AtomicLong ✅
         │
         ├─ Pure increment, very high contention?
         │         └─ LongAdder ✅  (fastest)
         │
         ├─ One-time boolean guard?
         │         └─ AtomicBoolean.compareAndSet(false, true) ✅
         │
         ├─ Swap an entire object atomically?
         │         └─ AtomicReference<ImmutableObject> ✅
         │
         ├─ Per-slot counters in an array?
         │         └─ AtomicIntegerArray / AtomicLongArray ✅
         │
         ├─ High-throughput max/min/custom reduction?
         │         └─ LongAccumulator / DoubleAccumulator ✅
         │
         ├─ Lock-free data structure (ABA-safe)?
         │         └─ AtomicStampedReference ✅
         │
         └─ Multiple variables must be consistent together?
                   └─ synchronized / ReentrantLock ✅
```

> 💡 **Best Practice:** Use `AtomicInteger`/`AtomicLong` for simple counters and IDs. Use `LongAdder` when many threads are incrementing under high contention. Use `AtomicReference` with **immutable objects** for lock-free config/state swapping. Always check the return value of `compareAndSet()`. Reach for `synchronized` when you need to update multiple variables as a single unit.