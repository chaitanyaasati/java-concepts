# StampedLock in Java

## Table of Contents
- [Introduction](#introduction)
- [The Problem StampedLock Solves](#the-problem-stampedlock-solves)
- [What is a Stamp?](#what-is-a-stamp)
- [Three Locking Modes](#three-locking-modes)
    - [Write Lock Mode](#write-lock-mode)
    - [Read Lock Mode (Pessimistic)](#read-lock-mode-pessimistic)
    - [Optimistic Read Mode](#optimistic-read-mode)
- [How Optimistic Reading Works](#how-optimistic-reading-works)
    - [The Analogy](#the-analogy)
    - [Optimistic Read Flow](#optimistic-read-flow)
- [Lock Compatibility Matrix](#lock-compatibility-matrix)
- [Java's `StampedLock` Class](#javas-stampedlock-class)
    - [Package and Import](#package-and-import)
    - [Constructor](#constructor)
    - [Key Methods — Write Lock](#key-methods--write-lock)
    - [Key Methods — Read Lock (Pessimistic)](#key-methods--read-lock-pessimistic)
    - [Key Methods — Optimistic Read](#key-methods--optimistic-read)
    - [Key Methods — Lock Conversion](#key-methods--lock-conversion)
    - [Key Methods — State Query](#key-methods--state-query)
- [Basic Usage Patterns](#basic-usage-patterns)
    - [Write Lock Pattern](#write-lock-pattern)
    - [Pessimistic Read Pattern](#pessimistic-read-pattern)
    - [Optimistic Read Pattern](#optimistic-read-pattern)
- [Lock Conversion](#lock-conversion)
    - [Read to Write (Upgrade)](#read-to-write-upgrade)
    - [Write to Read (Downgrade)](#write-to-read-downgrade)
- [Examples](#examples)
    - [Example 1 — Basic Write and Pessimistic Read](#example-1--basic-write-and-pessimistic-read)
    - [Example 2 — Optimistic Read (Core Pattern)](#example-2--optimistic-read-core-pattern)
    - [Example 3 — Point Class (Classic StampedLock Use Case)](#example-3--point-class-classic-stampedlock-use-case)
    - [Example 4 — Lock Upgrade (Read to Write)](#example-4--lock-upgrade-read-to-write)
    - [Example 5 — Lock Downgrade (Write to Read)](#example-5--lock-downgrade-write-to-read)
    - [Example 6 — High-Throughput Cache](#example-6--high-throughput-cache)
    - [Example 7 — tryOptimisticRead with Fallback Chain](#example-7--tryoptimisticread-with-fallback-chain)
    - [Example 8 — Flight Booking System](#example-8--flight-booking-system)
- [Performance Comparison](#performance-comparison)
- [StampedLock vs `ReentrantReadWriteLock`](#stampedlock-vs-reentrantreadwritelock)
- [StampedLock vs `ReentrantLock`](#stampedlock-vs-reentrantlock)
- [Common Pitfalls](#common-pitfalls)
- [When to Use StampedLock](#when-to-use-stampedlock)
- [Summary](#summary)

---

## Introduction

`StampedLock` is a **high-performance, capability-based lock** introduced in **Java 8** (`java.util.concurrent.locks.StampedLock`). It is designed to be a faster alternative to `ReentrantReadWriteLock` by introducing a revolutionary **optimistic reading** mode that allows reads to proceed **without acquiring any lock at all** — the fastest possible read path.

Unlike `ReentrantReadWriteLock`, `StampedLock` is:
- **Not reentrant** — the same thread cannot re-acquire a lock it already holds
- **Capability-based** — every lock operation returns a numeric **stamp** used to unlock
- **Three-mode** — write lock, pessimistic read lock, and optimistic read (lock-free)
- **Upgradeable** — a read lock can be converted to a write lock atomically
- **Not integrated with `Lock` interface** — has its own API

---

## The Problem StampedLock Solves

`ReentrantReadWriteLock` improved on plain locks for read-heavy workloads, but it still has a cost: **every read acquires a lock**, which involves memory barriers, queue inspection, and atomic operations. Under very high read concurrency, this overhead accumulates.

```
ReentrantReadWriteLock — even reads have overhead:

Thread R1: acquire readLock → [atomic CAS] → read → release readLock → [atomic CAS]
Thread R2: acquire readLock → [atomic CAS] → read → release readLock → [atomic CAS]
Thread R3: acquire readLock → [atomic CAS] → read → release readLock → [atomic CAS]
           ↑ Every reader pays the synchronization cost even when no writer is present

StampedLock — optimistic reads have near-zero overhead:

Thread R1: stamp = tryOptimisticRead() → [no lock] → read → validate(stamp) → done ✅
Thread R2: stamp = tryOptimisticRead() → [no lock] → read → validate(stamp) → done ✅
Thread R3: stamp = tryOptimisticRead() → [no lock] → read → validate(stamp) → done ✅
           ↑ No atomic operations, no queuing, no blocking — just a version check
```

---

## What is a Stamp?

A **stamp** is a `long` value returned by every lock/unlock operation in `StampedLock`. It serves as a **token** that:
- Proves the lock was acquired at that point in time
- Is required to release or convert the lock
- Changes every time a write operation occurs (version counter)
- Is `0` when a lock acquisition **fails** — always check for `0` before proceeding

```java
long stamp = lock.writeLock();         // stamp ≠ 0: write lock acquired
long stamp = lock.readLock();          // stamp ≠ 0: read lock acquired
long stamp = lock.tryOptimisticRead(); // stamp ≠ 0: current version snapshot
long stamp = lock.tryWriteLock();      // stamp = 0: FAILED to acquire

// All release calls require the stamp returned during acquisition
lock.unlockWrite(stamp);
lock.unlockRead(stamp);
lock.unlock(stamp);                    // works for both read and write
```

> ⚠️ **Critical Rule:** Never use a stamp from one lock call to unlock a different lock. Never discard a stamp — it is the only way to release the lock.

---

## Three Locking Modes

### Write Lock Mode

- **Exclusive** — no readers or other writers allowed
- Blocks until all existing readers and writers finish
- Returns a stamp; unlocked with `unlockWrite(stamp)` or `unlock(stamp)`

```
Write Lock:
Thread W1 holds write lock → ALL other threads (readers and writers) BLOCKED
```

### Read Lock Mode (Pessimistic)

- **Shared** — multiple pessimistic readers allowed simultaneously
- Blocks if a write lock is held
- Equivalent to `ReentrantReadWriteLock`'s read lock, but not reentrant
- Returns a stamp; unlocked with `unlockRead(stamp)` or `unlock(stamp)`

```
Pessimistic Read Lock:
R1, R2, R3 all hold read locks → allowed concurrently ✅
W1 tries to write              → BLOCKED until R1, R2, R3 release ❌
```

### Optimistic Read Mode

- **Lock-free** — acquires NO lock at all
- Always succeeds immediately (never blocks)
- Returns a version stamp; validated with `validate(stamp)` after reading
- If a writer modified data during the read, validation **fails** → retry with real lock
- The fastest read path — zero contention overhead when no writer is active

```
Optimistic Read:
stamp = tryOptimisticRead() → snapshot the current "version"
read data ...
validate(stamp)             → did any writer change data since stamp?
  → true:  no writer active — read was clean ✅
  → false: writer was active — data may be inconsistent, must retry ❌
```

---

## How Optimistic Reading Works

### The Analogy

Think of `StampedLock` as a **toll booth with a version board**:

```
Version Board: [v7]     ← incremented by every write operation

Optimistic reader:
  1. Note the version: "currently v7"
  2. Drive through WITHOUT stopping at the booth (no lock)
  3. After passing: check if the board still shows "v7"
     → Yes (v7): no writes happened — data is clean ✅ proceed
     → No (v8):  a write happened — data may be stale ❌ retry with a real lock

Writer:
  1. Stop at the booth (exclusive access)
  2. Change the board: v7 → v8
  3. Modify the data
  4. Leave the booth (release lock)
```

### Optimistic Read Flow

```
                    ┌─ tryOptimisticRead() → stamp (version snapshot)
                    │
               Read data fields into local variables
                    │
                    ├─ validate(stamp) ──→ true  → use local copies ✅ DONE
                    │                              (no writer modified data)
                    │
                    └─ validate(stamp) ──→ false → fallback to readLock()
                                                   re-read data under real lock ✅

Timeline with no writer:
t=0: stamp = tryOptimisticRead()    → version = 7
t=1: read x, y (local copies)
t=2: validate(stamp)                → still version 7 ✅
t=3: use x, y safely

Timeline with concurrent writer:
t=0: stamp = tryOptimisticRead()    → version = 7
t=1: read x (local copy)
t=1: [Writer modifies x, y — version becomes 8]
t=2: read y (local copy — possibly stale!)
t=3: validate(stamp)                → version is now 8 ❌
t=4: fallback to readLock() → re-read x, y safely
```

---

## Lock Compatibility Matrix

|  | Write (held) | Read (held) | Optimistic (held) |
|---|---|---|---|
| **Write (request)** | ❌ Blocked | ❌ Blocked | ✅ Allowed\* |
| **Read (request)** | ❌ Blocked | ✅ Allowed | ✅ Allowed\* |
| **Optimistic (request)** | ✅ Always returns | ✅ Always returns | ✅ Always returns |

\* Optimistic read always returns a stamp immediately — but `validate()` will return `false` if a write lock is or was held since the stamp was obtained.

---

## Java's `StampedLock` Class

### Package and Import

```java
import java.util.concurrent.locks.StampedLock;
```

### Constructor

```java
// Single constructor — no fairness parameter (StampedLock is always non-fair)
StampedLock lock = new StampedLock();
```

> 💡 `StampedLock` has no fairness option. It is designed for maximum throughput, not strict ordering. If fairness is required, use `ReentrantReadWriteLock(true)`.

### Key Methods — Write Lock

| Method | Description | Blocking? |
|---|---|---|
| `writeLock()` | Acquires write lock; blocks until available | ✅ Yes |
| `writeLockInterruptibly()` | Acquires write lock; interruptible | ✅ Yes |
| `tryWriteLock()` | Non-blocking write lock attempt; returns 0 on failure | ❌ No |
| `tryWriteLock(time, unit)` | Timed write lock attempt; returns 0 on timeout | ⏱ Timed |
| `unlockWrite(stamp)` | Releases write lock (stamp must match) | ❌ No |
| `isWriteLocked()` | Returns `true` if write lock is held | ❌ No |

### Key Methods — Read Lock (Pessimistic)

| Method | Description | Blocking? |
|---|---|---|
| `readLock()` | Acquires read lock; blocks if write lock held | ✅ Yes |
| `readLockInterruptibly()` | Acquires read lock; interruptible | ✅ Yes |
| `tryReadLock()` | Non-blocking read lock attempt; returns 0 on failure | ❌ No |
| `tryReadLock(time, unit)` | Timed read lock attempt; returns 0 on timeout | ⏱ Timed |
| `unlockRead(stamp)` | Releases read lock (stamp must match) | ❌ No |
| `getReadLockCount()` | Returns number of threads holding read locks | ❌ No |

### Key Methods — Optimistic Read

| Method | Description | Blocking? |
|---|---|---|
| `tryOptimisticRead()` | Returns current version stamp; **never blocks** | ❌ No |
| `validate(stamp)` | Returns `true` if no write has occurred since `stamp` | ❌ No |

### Key Methods — Lock Conversion

| Method | Description |
|---|---|
| `tryConvertToWriteLock(stamp)` | Atomically upgrade: read/optimistic → write; returns new stamp or 0 on failure |
| `tryConvertToReadLock(stamp)` | Atomically downgrade: write → read; returns new stamp or 0 on failure |
| `tryConvertToOptimisticRead(stamp)` | Convert write/read → optimistic; returns new stamp or 0 |
| `unlock(stamp)` | Releases any lock mode (read or write) |

### Key Methods — State Query

| Method | Description |
|---|---|
| `isReadLocked()` | Returns `true` if one or more read locks are held |
| `isWriteLocked()` | Returns `true` if write lock is held |
| `isLockStamp(stamp)` | Returns `true` if stamp represents a held lock |
| `isWriteLockStamp(stamp)` | Returns `true` if stamp represents a write lock |
| `isReadLockStamp(stamp)` | Returns `true` if stamp represents a read lock |
| `isOptimisticReadStamp(stamp)` | Returns `true` if stamp is an optimistic read stamp |

---

## Basic Usage Patterns

### Write Lock Pattern

```java
StampedLock lock = new StampedLock();

long stamp = lock.writeLock();    // acquire BEFORE try
try {
    // --- exclusive critical section ---
    // Modify shared data here
} finally {
    lock.unlockWrite(stamp);      // ALWAYS unlock in finally with the stamp
}
```

### Pessimistic Read Pattern

```java
long stamp = lock.readLock();     // blocks if writer is active
try {
    // --- shared read section ---
    // Multiple threads can be here simultaneously
} finally {
    lock.unlockRead(stamp);
}
```

### Optimistic Read Pattern

```java
// Step 1: get version snapshot — never blocks
long stamp = lock.tryOptimisticRead();

// Step 2: read data into LOCAL variables (not from shared fields directly)
double localX = this.x;
double localY = this.y;

// Step 3: validate — did any writer modify data since step 1?
if (!lock.validate(stamp)) {
    // Step 4: fallback to pessimistic read
    stamp = lock.readLock();
    try {
        localX = this.x;   // re-read safely under real lock
        localY = this.y;
    } finally {
        lock.unlockRead(stamp);
    }
}

// Step 5: use localX, localY — guaranteed consistent
```

> ⚠️ **Always copy shared fields into LOCAL variables before validating.** Reading directly from shared fields after `validate()` is unsafe — another write could occur between validate and your read.

---

## Lock Conversion

### Read to Write (Upgrade)

`StampedLock` **supports lock upgrading** — converting a held read/optimistic stamp to a write lock atomically. This is impossible with `ReentrantReadWriteLock`.

```java
// Upgrading optimistic read → write (most common pattern)
long stamp = lock.tryOptimisticRead();
// ... read fields ...
if (!lock.validate(stamp)) {
    // Optimistic failed — try to convert directly to write lock
    stamp = lock.tryConvertToWriteLock(stamp);
    if (stamp == 0L) {
        // Conversion failed — acquire write lock the hard way
        stamp = lock.writeLock();
    }
    try {
        // modify data under write lock
    } finally {
        lock.unlockWrite(stamp);
    }
}

// ──────────────────────────────────────────────────────────────────────
// Upgrading pessimistic read → write
long stamp = lock.readLock();
try {
    // reading...
    if (needsUpdate()) {
        long writeStamp = lock.tryConvertToWriteLock(stamp);
        if (writeStamp != 0L) {
            stamp = writeStamp;   // upgrade succeeded
            // modify data...
        } else {
            // upgrade failed (another reader or writer present)
            lock.unlockRead(stamp);
            stamp = lock.writeLock(); // acquire fresh write lock
        }
    }
} finally {
    lock.unlock(stamp);  // unlock(stamp) works for both read and write
}
```

### Write to Read (Downgrade)

```java
long stamp = lock.writeLock();
try {
    // modify data...
    long readStamp = lock.tryConvertToReadLock(stamp);
    if (readStamp != 0L) {
        stamp = readStamp; // downgraded to read lock
        // now holding only read lock — other readers can join
    }
    // read the just-written data...
} finally {
    lock.unlock(stamp);
}
```

---

## Examples

### Example 1 — Basic Write and Pessimistic Read

```java
import java.util.concurrent.locks.StampedLock;

public class BasicStampedLockDemo {

    private static final StampedLock lock = new StampedLock();
    private static int counter = 0;

    public static void increment() {
        long stamp = lock.writeLock();
        try {
            counter++;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public static int read() {
        long stamp = lock.readLock();
        try {
            return counter;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Runnable writer = () -> {
            for (int i = 0; i < 500; i++) increment();
        };

        Runnable reader = () -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName()
                        + " reads counter = " + read());
                try { Thread.sleep(50); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread w1 = new Thread(writer, "Writer-1");
        Thread w2 = new Thread(writer, "Writer-2");
        Thread r1 = new Thread(reader, "Reader-1");
        Thread r2 = new Thread(reader, "Reader-2");

        w1.start(); w2.start(); r1.start(); r2.start();
        w1.join();  w2.join();  r1.join();  r2.join();

        System.out.println("Final counter: " + counter); // always 1000
    }
}
```

---

### Example 2 — Optimistic Read (Core Pattern)

The most important `StampedLock` pattern — demonstrating the optimistic fast path with a safe fallback:

```java
import java.util.concurrent.locks.StampedLock;

public class OptimisticReadDemo {

    private final StampedLock lock = new StampedLock();
    private double price      = 100.0;
    private int    quantity   = 50;

    // ─── Writer ──────────────────────────────────────────────────────────────
    public void updateProduct(double newPrice, int newQty) {
        long stamp = lock.writeLock();
        try {
            System.out.printf("[WRITE] price %.1f→%.1f | qty %d→%d%n",
                    price, newPrice, quantity, newQty);
            price    = newPrice;
            quantity = newQty;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    // ─── Optimistic Reader ────────────────────────────────────────────────────
    public String getProductInfo() {

        // ── Fast path: optimistic read (no lock) ──────────────────────────
        long stamp = lock.tryOptimisticRead();  // get version snapshot

        // Read into LOCAL variables — never use shared fields directly
        double localPrice    = this.price;
        int    localQuantity = this.quantity;

        if (lock.validate(stamp)) {
            // No writer was active — data is clean
            return String.format("[OPTIMISTIC ✅] price=%.1f qty=%d thread=%s",
                    localPrice, localQuantity, Thread.currentThread().getName());
        }

        // ── Slow path: fallback to pessimistic read lock ───────────────────
        stamp = lock.readLock();
        try {
            localPrice    = this.price;
            localQuantity = this.quantity;
            return String.format("[PESSIMISTIC 🔒] price=%.1f qty=%d thread=%s",
                    localPrice, localQuantity, Thread.currentThread().getName());
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        OptimisticReadDemo demo = new OptimisticReadDemo();

        // Occasional writer
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                try { Thread.sleep(300); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                demo.updateProduct(100.0 + i * 10, 50 - i * 5);
            }
        }, "Writer");

        // Many concurrent readers — mostly hit optimistic fast path
        Thread[] readers = new Thread[6];
        for (int i = 0; i < 6; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    System.out.println(demo.getProductInfo());
                    try { Thread.sleep(100); } catch (InterruptedException e) {
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

// Output (most reads use optimistic fast path):
// [OPTIMISTIC ✅] price=100.0 qty=50 thread=Reader-1
// [OPTIMISTIC ✅] price=100.0 qty=50 thread=Reader-2
// [WRITE] price 100.0→110.0 | qty 50→45
// [PESSIMISTIC 🔒] price=110.0 qty=45 thread=Reader-3  ← fallback during write
// [OPTIMISTIC ✅] price=110.0 qty=45 thread=Reader-4
// ...
```

---

### Example 3 — Point Class (Classic StampedLock Use Case)

The canonical example from Java's own documentation — a 2D point with all three locking modes:

```java
import java.util.concurrent.locks.StampedLock;

public class Point {

    private double x;
    private double y;
    private final StampedLock lock = new StampedLock();

    // ─── Write: move the point ────────────────────────────────────────────────
    public void move(double deltaX, double deltaY) {
        long stamp = lock.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    // ─── Optimistic Read: distance from origin ────────────────────────────────
    public double distanceFromOrigin() {
        long stamp = lock.tryOptimisticRead();    // Step 1: version snapshot

        double localX = x;                        // Step 2: copy to locals
        double localY = y;

        if (!lock.validate(stamp)) {              // Step 3: validate
            stamp = lock.readLock();              // Step 4: fallback
            try {
                localX = x;
                localY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return Math.sqrt(localX * localX + localY * localY); // Step 5: compute
    }

    // ─── Pessimistic Read + conditional upgrade to Write ─────────────────────
    public void moveIfAtOrigin(double newX, double newY) {
        long stamp = lock.readLock();             // Start with read lock
        try {
            while (x == 0.0 && y == 0.0) {
                long writeStamp = lock.tryConvertToWriteLock(stamp); // try upgrade

                if (writeStamp != 0L) {
                    stamp = writeStamp;           // upgrade succeeded
                    x = newX;
                    y = newY;
                    break;
                } else {
                    // Upgrade failed — release read, acquire write
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                }
            }
        } finally {
            lock.unlock(stamp);                   // works for read or write stamp
        }
    }

    public String position() {
        long stamp = lock.readLock();
        try {
            return String.format("(%.2f, %.2f)", x, y);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Point point = new Point();

        // Writer thread moves the point
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                point.move(1.0, 1.0);
                System.out.println("Moved to " + point.position());
                try { Thread.sleep(100); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Writer");

        // Reader threads calculate distances (optimistic fast path)
        Thread[] readers = new Thread[4];
        for (int i = 0; i < 4; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 8; j++) {
                    double dist = point.distanceFromOrigin();
                    System.out.printf("%s → distance = %.4f%n",
                            Thread.currentThread().getName(), dist);
                    try { Thread.sleep(60); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Reader-" + (i + 1));
        }

        writer.start();
        for (Thread r : readers) r.start();
        writer.join();
        for (Thread r : readers) r.join();

        System.out.println("Final position: " + point.position());
    }
}
```

---

### Example 4 — Lock Upgrade (Read to Write)

Demonstrating atomic upgrade from pessimistic read to write lock — impossible with `ReentrantReadWriteLock`:

```java
import java.util.concurrent.locks.StampedLock;

public class LockUpgradeDemo {

    private final StampedLock lock = new StampedLock();
    private int inventory = 100;
    private int reserved  = 0;

    // Read inventory — if low, atomically upgrade to write to restock
    public void checkAndRestock(int threshold, int restockAmount) {
        long stamp = lock.readLock();                    // Start: pessimistic read
        System.out.println(Thread.currentThread().getName()
                + ": reading inventory = " + inventory + " (read lock)");
        try {
            while (inventory < threshold) {
                // Inventory is low — try to upgrade to write lock
                long writeStamp = lock.tryConvertToWriteLock(stamp);

                if (writeStamp != 0L) {
                    // ✅ Upgrade succeeded — now holding write lock
                    stamp = writeStamp;
                    System.out.println(Thread.currentThread().getName()
                            + ": ✅ upgraded to write lock — restocking "
                            + inventory + " → " + (inventory + restockAmount));
                    inventory += restockAmount;
                    break;
                } else {
                    // ❌ Upgrade failed — another reader/writer is active
                    System.out.println(Thread.currentThread().getName()
                            + ": ⚠️ upgrade failed — releasing read, acquiring write");
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();            // acquire fresh write lock
                }
            }
        } finally {
            lock.unlock(stamp);                          // works for read or write
        }
    }

    public void sell(int units) {
        long stamp = lock.writeLock();
        try {
            if (inventory >= units) {
                inventory -= units;
                System.out.println(Thread.currentThread().getName()
                        + ": sold " + units + " | remaining: " + inventory);
            } else {
                System.out.println(Thread.currentThread().getName()
                        + ": ❌ insufficient stock (" + inventory + " < " + units + ")");
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockUpgradeDemo demo = new LockUpgradeDemo();

        // Sell down the inventory
        Thread seller = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                demo.sell(12);
                try { Thread.sleep(50); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Seller");

        // Periodically check and restock if low
        Thread restockChecker = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                demo.checkAndRestock(30, 100); // restock if below 30
                try { Thread.sleep(120); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "RestockChecker");

        seller.start(); restockChecker.start();
        seller.join();  restockChecker.join();

        System.out.println("Final inventory: " + demo.inventory);
    }
}
```

---

### Example 5 — Lock Downgrade (Write to Read)

After writing data, downgrade to a read lock to safely read back the result:

```java
import java.util.concurrent.locks.StampedLock;

public class LockDowngradeDemo {

    private final StampedLock lock = new StampedLock();
    private double latestPrice = 0.0;
    private String lastUpdatedBy = "none";

    // Write new price, then downgrade to read to return the confirmed value
    public double updateAndConfirm(double newPrice, String updatedBy) {
        long stamp = lock.writeLock();
        double confirmedPrice;
        try {
            // Modify data under write lock
            latestPrice   = newPrice;
            lastUpdatedBy = updatedBy;
            System.out.println(Thread.currentThread().getName()
                    + ": [WRITE] price = " + latestPrice
                    + " by " + lastUpdatedBy);

            // Downgrade: convert write lock to read lock
            long readStamp = lock.tryConvertToReadLock(stamp);
            if (readStamp != 0L) {
                stamp = readStamp;     // now holding read lock
                System.out.println(Thread.currentThread().getName()
                        + ": ↓ downgraded to read lock");
                // Safe to read — no writer can interfere between write and read
                confirmedPrice = latestPrice;
            } else {
                // Downgrade failed (rare) — still holding write lock
                confirmedPrice = latestPrice;
            }
        } finally {
            lock.unlock(stamp);
        }
        return confirmedPrice;
    }

    public double readPrice() {
        long stamp = lock.tryOptimisticRead();
        double localPrice = latestPrice;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try { localPrice = latestPrice; }
            finally { lock.unlockRead(stamp); }
        }
        return localPrice;
    }

    public static void main(String[] args) throws InterruptedException {
        LockDowngradeDemo demo = new LockDowngradeDemo();

        Thread[] writers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final double price = 100.0 + i * 25.0;
            final String by    = "System-" + (i + 1);
            writers[i] = new Thread(() -> {
                double confirmed = demo.updateAndConfirm(price, by);
                System.out.println(Thread.currentThread().getName()
                        + ": confirmed price = " + confirmed);
            }, "Writer-" + (i + 1));
        }

        for (Thread w : writers) { w.start(); Thread.sleep(80); }
        for (Thread w : writers) w.join();

        System.out.println("Final price: " + demo.readPrice());
    }
}
```

---

### Example 6 — High-Throughput Cache

A cache using optimistic reads for maximum read concurrency:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public class StampedCache<K, V> {

    private final Map<K, V>   cache = new HashMap<>();
    private final StampedLock lock  = new StampedLock();

    public V get(K key) {
        // ── Fast path: optimistic read ────────────────────────────────────────
        long stamp = lock.tryOptimisticRead();
        V value = cache.get(key);

        if (lock.validate(stamp)) {
            return value; // clean read — no lock needed
        }

        // ── Slow path: pessimistic read lock ──────────────────────────────────
        stamp = lock.readLock();
        try {
            return cache.get(key);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public void put(K key, V value) {
        long stamp = lock.writeLock();
        try {
            cache.put(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public V computeIfAbsent(K key, java.util.function.Function<K, V> loader) {
        // Optimistic read first
        long stamp = lock.tryOptimisticRead();
        V value = cache.get(key);
        if (value != null && lock.validate(stamp)) {
            return value; // cache hit — fast path
        }

        // Upgrade to write lock to compute and store
        stamp = lock.writeLock();
        try {
            // Double-check: another thread may have computed while we waited
            value = cache.get(key);
            if (value == null) {
                value = loader.apply(key);
                cache.put(key, value);
                System.out.println(Thread.currentThread().getName()
                        + ": computed + cached key=" + key);
            }
            return value;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public int size() {
        long stamp = lock.tryOptimisticRead();
        int s = cache.size();
        return lock.validate(stamp) ? s : readSize();
    }

    private int readSize() {
        long stamp = lock.readLock();
        try { return cache.size(); }
        finally { lock.unlockRead(stamp); }
    }

    public static void main(String[] args) throws InterruptedException {
        StampedCache<String, String> cache = new StampedCache<>();

        // Writer populates cache
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                cache.put("key:" + i, "value-" + i);
                try { Thread.sleep(50); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Writer");

        // Readers — mostly hit optimistic fast path
        Thread[] readers = new Thread[8];
        for (int i = 0; i < 8; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 1; j <= 5; j++) {
                    V val = cache.get("key:" + j);
                    System.out.println(Thread.currentThread().getName()
                            + " key:" + j + " = " + val);
                    try { Thread.sleep(30); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Reader-" + (i + 1));
        }

        writer.start();
        for (Thread r : readers) r.start();
        writer.join();
        for (Thread r : readers) r.join();

        System.out.println("Cache size: " + cache.size());
    }
}
```

---

### Example 7 — tryOptimisticRead with Fallback Chain

The complete three-level fallback — optimistic → pessimistic read → write:

```java
import java.util.concurrent.locks.StampedLock;
import java.util.concurrent.TimeUnit;

public class FallbackChainDemo {

    private final StampedLock lock  = new StampedLock();
    private double rate             = 1.0;
    private String currency         = "USD";

    public String getExchangeInfo() {
        // ── Level 1: Optimistic read (fastest — no lock) ─────────────────────
        long stamp = lock.tryOptimisticRead();
        double localRate     = rate;
        String localCurrency = currency;

        if (lock.validate(stamp)) {
            return String.format("[OPTIMISTIC] %s @ %.4f", localCurrency, localRate);
        }

        // ── Level 2: Try timed pessimistic read (100ms) ───────────────────────
        try {
            stamp = lock.tryReadLock(100, TimeUnit.MILLISECONDS);
            if (stamp != 0L) {
                try {
                    localRate     = rate;
                    localCurrency = currency;
                    return String.format("[READ LOCK] %s @ %.4f", localCurrency, localRate);
                } finally {
                    lock.unlockRead(stamp);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ── Level 3: Blocking pessimistic read (guaranteed) ───────────────────
        stamp = lock.readLock();
        try {
            localRate     = rate;
            localCurrency = currency;
            return String.format("[BLOCKING READ] %s @ %.4f", localCurrency, localRate);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public void updateRate(String newCurrency, double newRate) {
        long stamp = lock.writeLock();
        try {
            this.currency = newCurrency;
            this.rate     = newRate;
            System.out.printf("[WRITE] %s = %.4f%n", newCurrency, newRate);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FallbackChainDemo demo = new FallbackChainDemo();

        Thread writer = new Thread(() -> {
            double[][] rates = {{1.2851, 0}, {0.8734, 1}, {1.1023, 2}};
            String[] currencies = {"GBP", "EUR", "CAD"};
            for (int i = 0; i < 3; i++) {
                demo.updateRate(currencies[i], rates[i][0]);
                try { Thread.sleep(200); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "RateUpdater");

        Thread[] readers = new Thread[5];
        for (int i = 0; i < 5; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 6; j++) {
                    System.out.println(Thread.currentThread().getName()
                            + ": " + demo.getExchangeInfo());
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

---

### Example 8 — Flight Booking System

A realistic system combining all three modes — optimistic reads for seat availability checks, pessimistic reads for seat maps, and exclusive writes for bookings:

```java
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public class FlightBookingSystem {

    private final StampedLock lock  = new StampedLock();
    private final boolean[]   seats;   // true = booked, false = available
    private int availableSeats;

    public FlightBookingSystem(int totalSeats) {
        this.seats          = new boolean[totalSeats];
        this.availableSeats = totalSeats;
    }

    // ─── Optimistic: quick availability check (no lock) ──────────────────────
    public int checkAvailableSeats() {
        long stamp = lock.tryOptimisticRead();
        int count = availableSeats;
        if (lock.validate(stamp)) {
            return count; // fast path
        }
        // fallback to pessimistic read
        stamp = lock.readLock();
        try { return availableSeats; }
        finally { lock.unlockRead(stamp); }
    }

    // ─── Pessimistic Read: display full seat map (longer read operation) ──────
    public String getSeatMap() {
        long stamp = lock.readLock();
        try {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < seats.length; i++) {
                sb.append(seats[i] ? "X" : "O");
                if (i < seats.length - 1) sb.append("|");
            }
            sb.append("] (").append(availableSeats).append(" free)");
            return sb.toString();
        } finally {
            lock.unlockRead(stamp);
        }
    }

    // ─── Write: book a specific seat ─────────────────────────────────────────
    public boolean bookSeat(int seatNumber, String passenger) {
        long stamp = lock.writeLock();
        try {
            if (seatNumber < 0 || seatNumber >= seats.length) {
                System.out.println(passenger + ": ❌ invalid seat " + seatNumber);
                return false;
            }
            if (seats[seatNumber]) {
                System.out.println(passenger + ": ❌ seat " + seatNumber + " already booked");
                return false;
            }
            seats[seatNumber] = true;
            availableSeats--;
            System.out.printf("%-12s: ✅ booked seat %d | available: %d%n",
                    passenger, seatNumber, availableSeats);
            return true;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    // ─── Write: cancel a booking ──────────────────────────────────────────────
    public boolean cancelBooking(int seatNumber, String passenger) {
        long stamp = lock.writeLock();
        try {
            if (!seats[seatNumber]) {
                System.out.println(passenger + ": seat " + seatNumber + " was not booked");
                return false;
            }
            seats[seatNumber] = false;
            availableSeats++;
            System.out.printf("%-12s: cancelled seat %d | available: %d%n",
                    passenger, seatNumber, availableSeats);
            return true;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FlightBookingSystem flight = new FlightBookingSystem(10); // 10-seat flight

        System.out.println("Initial: " + flight.getSeatMap());

        // Passengers checking availability (optimistic — no lock)
        for (int i = 0; i < 5; i++) {
            new Thread(() ->
                System.out.println(Thread.currentThread().getName()
                        + ": available = " + flight.checkAvailableSeats()),
                "Checker-" + i
            ).start();
        }

        Thread.sleep(50);

        // Concurrent booking attempts (exclusive write locks)
        String[] passengers = {"Alice", "Bob", "Carol", "Dave", "Eve",
                               "Frank", "Grace", "Henry", "Iris", "Jake"};
        Thread[] bookers = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final String name = passengers[i];
            final int seat    = i;
            bookers[i] = new Thread(() -> flight.bookSeat(seat, name), name);
        }
        for (Thread b : bookers) b.start();
        for (Thread b : bookers) b.join();

        System.out.println("\nAfter booking: " + flight.getSeatMap());

        // Some cancellations
        flight.cancelBooking(3, "Dave");
        flight.cancelBooking(7, "Henry");

        System.out.println("After cancels: " + flight.getSeatMap());

        // Seat map read (pessimistic — complete snapshot)
        for (int i = 0; i < 3; i++) {
            new Thread(() ->
                System.out.println(Thread.currentThread().getName()
                        + " sees: " + flight.getSeatMap()),
                "MapReader-" + i
            ).start();
        }
    }
}

// Sample Output:
// Initial: [O|O|O|O|O|O|O|O|O|O] (10 free)
// Checker-0: available = 10
// ...
// Alice  : ✅ booked seat 0 | available: 9
// Bob    : ✅ booked seat 1 | available: 8
// ...
// After booking: [X|X|X|X|X|X|X|X|X|X] (0 free)
// Dave   : cancelled seat 3 | available: 1
// Henry  : cancelled seat 7 | available: 2
// After cancels: [X|X|X|O|X|X|X|O|X|X] (2 free)
```

---

## Performance Comparison

| Workload | `synchronized` | `ReentrantReadWriteLock` | `StampedLock` |
|---|---|---|---|
| **Pure reads (no writers)** | Serialized | Concurrent | ✅ Lock-free (optimistic) |
| **Mostly reads, rare writes** | Serialized | Mostly concurrent | ✅ Near lock-free reads |
| **Equal reads and writes** | Serialized | Mixed | Marginal improvement |
| **Pure writes** | Serialized | Serialized | Serialized |
| **High contention** | Moderate | Better | ✅ Best |
| **Low contention** | Good | Overhead | May have overhead |

> `StampedLock` delivers the greatest throughput advantage in **high-read, low-write** workloads under **high thread contention**. For low-contention or write-heavy workloads, the simpler `synchronized` may be faster due to JVM optimisations (biased locking, lock elision).

---

## StampedLock vs `ReentrantReadWriteLock`

| Feature | `StampedLock` | `ReentrantReadWriteLock` |
|---|---|---|
| **Optimistic reads** | ✅ Lock-free | ❌ No |
| **Lock upgrading** | ✅ `tryConvertToWriteLock()` | ❌ Deadlocks |
| **Lock downgrading** | ✅ `tryConvertToReadLock()` | ✅ Yes |
| **Reentrancy** | ❌ Not reentrant | ✅ Reentrant |
| **Fairness** | ❌ Not supported | ✅ Optional |
| **Condition variables** | ❌ Not supported | ✅ Write lock only |
| **Stamp-based API** | ✅ Yes (must track stamps) | ❌ Standard Lock API |
| **Interruptible locking** | ✅ Yes | ✅ Yes |
| **Writer starvation** | ⚠️ Possible | ✅ Fair mode prevents it |
| **API complexity** | Higher | Medium |
| **Best for** | Maximum read throughput | General read-heavy + fairness |

---

## StampedLock vs `ReentrantLock`

| Feature | `StampedLock` | `ReentrantLock` |
|---|---|---|
| **Optimistic reads** | ✅ Yes | ❌ No |
| **Concurrent reads** | ✅ Yes (read + optimistic) | ❌ No |
| **Reentrancy** | ❌ No | ✅ Yes |
| **Fairness** | ❌ No | ✅ Optional |
| **Condition variables** | ❌ No | ✅ Multiple |
| **Lock upgrading** | ✅ Yes | ❌ N/A |
| **API complexity** | High | Medium |
| **Best for** | Read-heavy, max throughput | Exclusive critical sections |

---

## Common Pitfalls

### 1. Forgetting to Unlock — Deadlock

```java
// ❌ Exception before unlock — lock never released
long stamp = lock.writeLock();
riskyOperation();       // throws exception
lock.unlockWrite(stamp); // NEVER REACHED

// ✅ Always unlock in finally
long stamp = lock.writeLock();
try {
    riskyOperation();
} finally {
    lock.unlockWrite(stamp); // GUARANTEED
}
```

### 2. Using Wrong Unlock Method

```java
// ❌ Using wrong unlock — throws IllegalMonitorStateException or worse
long stamp = lock.writeLock();
lock.unlockRead(stamp);  // WRONG — this is a write stamp

long stamp = lock.readLock();
lock.unlockWrite(stamp); // WRONG — this is a read stamp

// ✅ Use lock.unlock(stamp) — works for any mode
long stamp = lock.writeLock();
try { /* ... */ } finally { lock.unlock(stamp); } // ✅ safe for both
```

### 3. Reading Shared Fields After validate() — Not Before

```java
// ❌ Reading shared fields AFTER validate() is unsafe
long stamp = lock.tryOptimisticRead();
if (lock.validate(stamp)) {
    return this.x + this.y; // ❌ another write may occur between validate() and this read
}

// ✅ Read into locals FIRST, then validate
long stamp = lock.tryOptimisticRead();
double localX = this.x;       // read to locals
double localY = this.y;       // read to locals
if (lock.validate(stamp)) {
    return localX + localY;  // ✅ safe — using local copies
}
```

### 4. StampedLock is NOT Reentrant

```java
StampedLock lock = new StampedLock();

long stamp1 = lock.writeLock();
long stamp2 = lock.writeLock(); // ❌ DEADLOCK — same thread blocks on itself

// ✅ If reentrancy is needed, use ReentrantLock or ReentrantReadWriteLock
```

### 5. Not Checking for Zero Stamp on tryLock

```java
// ❌ Not checking if acquisition failed (stamp == 0)
long stamp = lock.tryWriteLock();
try {
    modifyData(); // may run without lock if stamp == 0!
} finally {
    lock.unlockWrite(stamp); // throws if stamp == 0
}

// ✅ Always check for 0 on try methods
long stamp = lock.tryWriteLock();
if (stamp != 0L) {
    try {
        modifyData();
    } finally {
        lock.unlockWrite(stamp);
    }
} else {
    // handle failure — lock was not acquired
}
```

### 6. Discarding a Stamp After Conversion

```java
// ❌ Forgetting to use the new stamp after conversion
long stamp = lock.readLock();
long writeStamp = lock.tryConvertToWriteLock(stamp);
// writeStamp may be 0 (conversion failed) or a new write stamp
lock.unlock(stamp);      // ❌ Wrong! stamp is no longer valid if conversion succeeded

// ✅ Update stamp variable after conversion
long stamp = lock.readLock();
long writeStamp = lock.tryConvertToWriteLock(stamp);
if (writeStamp != 0L) {
    stamp = writeStamp;  // update to new stamp
}
// ... use lock ...
lock.unlock(stamp);      // ✅ correct — uses the current stamp
```

---

## When to Use StampedLock

| Scenario | Recommendation |
|---|---|
| Read-heavy, high concurrency, max throughput | ✅ `StampedLock` with optimistic reads |
| Need lock upgrading (read → write) | ✅ `StampedLock` with `tryConvertToWriteLock()` |
| Need reentrancy | ❌ Use `ReentrantLock` or `ReentrantReadWriteLock` |
| Need fairness / prevent starvation | ❌ Use `ReentrantReadWriteLock(true)` |
| Need condition variables | ❌ Use `ReentrantLock` with `newCondition()` |
| Simple mutual exclusion | ❌ Use `synchronized` (simpler, JVM-optimised) |
| Equal reads and writes | ⚠️ Benchmark first — `synchronized` may win |
| Cross-platform / legacy Java (< 8) | ❌ `StampedLock` requires Java 8+ |

---

## Summary

| Concept | Key Point |
|---|---|
| **What it is** | A high-performance lock with three modes: write, pessimistic read, optimistic read |
| **Stamp** | A `long` token returned by every lock operation; required to unlock or convert |
| **Stamp = 0** | Means lock acquisition failed (`tryLock` methods) — always check before using |
| **Write Lock** | Exclusive — blocks all readers and writers |
| **Pessimistic Read** | Shared — concurrent reads; blocked by write lock |
| **Optimistic Read** | Lock-free — always succeeds; validated with `validate()` |
| **Optimistic Read Pattern** | `tryOptimisticRead()` → copy to locals → `validate()` → use locals or fallback |
| **NOT reentrant** | Same thread acquiring twice deadlocks — use `ReentrantLock` if needed |
| **NOT fair** | No fairness option — writers can starve under heavy reads |
| **NO condition variables** | Use `ReentrantLock.newCondition()` if conditions are needed |
| **Lock upgrading** | `tryConvertToWriteLock(stamp)` — supported (unlike `ReentrantReadWriteLock`) |
| **Lock downgrading** | `tryConvertToReadLock(stamp)` — supported |
| **Always unlock in `finally`** | Use `lock.unlock(stamp)` — safe for any mode |
| **Best for** | High-read, low-write workloads under high thread concurrency |

**Three-mode decision flow:**

```
Need to access shared data?
         │
         ├─ Modifying data?
         │         └─ YES → writeLock() ─────────────────────────────► Exclusive write
         │
         ├─ Long read / multiple fields / must be consistent?
         │         └─ YES → readLock() ────────────────────────────────► Shared read
         │
         └─ Short read / single field / writers are rare?
                   └─ YES → tryOptimisticRead() + validate()
                            ├─ validate() == true  ──────────────────► No lock needed ✅
                            └─ validate() == false ──────────────────► Fall back to readLock()
```

> 💡 **Best Practice:** Always follow the optimistic read template exactly: get stamp → copy all fields to locals → validate → either use locals or fall back to a real lock. Never read shared fields after `validate()`. Never ignore a zero stamp. Always unlock in `finally`. Prefer `lock.unlock(stamp)` over mode-specific unlock methods to avoid mismatches.