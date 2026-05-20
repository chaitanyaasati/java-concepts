# ReadWriteLock in Java

## Table of Contents
- [Introduction](#introduction)
- [The Problem ReadWriteLock Solves](#the-problem-readwritelock-solves)
- [How ReadWriteLock Works](#how-readwritelock-works)
  - [The Analogy](#the-analogy)
  - [Lock Compatibility Matrix](#lock-compatibility-matrix)
  - [Concurrency Timeline](#concurrency-timeline)
- [Java's `ReadWriteLock` Interface](#javas-readwritelock-interface)
  - [Package and Import](#package-and-import)
  - [Interface Definition](#interface-definition)
- [Java's `ReentrantReadWriteLock` Class](#javas-reentrantreadwritelock-class)
  - [Constructors](#constructors)
  - [Key Methods — Outer Lock](#key-methods--outer-lock)
  - [Key Methods — Read Lock](#key-methods--read-lock)
  - [Key Methods — Write Lock](#key-methods--write-lock)
- [Basic Usage Pattern](#basic-usage-pattern)
- [Fairness in ReentrantReadWriteLock](#fairness-in-reentrantreadwritelock)
- [Lock Downgrading](#lock-downgrading)
- [Lock Upgrading](#lock-upgrading)
- [Examples](#examples)
  - [Example 1 — Basic Read and Write Lock](#example-1--basic-read-and-write-lock)
  - [Example 2 — Thread-Safe Cache](#example-2--thread-safe-cache)
  - [Example 3 — Concurrent Config Store](#example-3--concurrent-config-store)
  - [Example 4 — Read-Heavy Leaderboard](#example-4--read-heavy-leaderboard)
  - [Example 5 — Lock Downgrading](#example-5--lock-downgrading)
  - [Example 6 — Fair ReadWriteLock](#example-6--fair-readwritelock)
  - [Example 7 — tryLock on Read and Write](#example-7--trylock-on-read-and-write)
  - [Example 8 — Inventory Management System](#example-8--inventory-management-system)
- [Performance Comparison](#performance-comparison)
- [ReadWriteLock vs `synchronized`](#readwritelock-vs-synchronized)
- [ReadWriteLock vs `ReentrantLock`](#readwritelock-vs-reentrantlock)
- [StampedLock — The Modern Alternative](#stampedlock--the-modern-alternative)
- [Common Pitfalls](#common-pitfalls)
- [Summary](#summary)

---

## Introduction

`ReadWriteLock` is a synchronization mechanism in Java that allows **multiple threads to read shared data concurrently** while ensuring **exclusive access for write operations**. It is part of the `java.util.concurrent.locks` package introduced in **Java 5**.

The core insight is simple: **reads don't modify data, so they can safely happen in parallel**. A standard `synchronized` block or `ReentrantLock` treats reads and writes identically — only one thread at a time — which wastes throughput in read-heavy applications.

`ReadWriteLock` solves this by maintaining **two separate locks**:
- **Read Lock** — shared, many threads can hold it simultaneously
- **Write Lock** — exclusive, only one thread can hold it, and no readers allowed

This makes `ReadWriteLock` ideal for scenarios where:
- Data is read **much more frequently** than it is written
- Read operations are **long-running** (database queries, heavy computation)
- Maximum read throughput is critical (caches, configuration stores, leaderboards)

---

## The Problem ReadWriteLock Solves

Consider a shared data store accessed by 10 threads — 9 readers and 1 writer:

```
With synchronized / ReentrantLock (mutex):

Time →  [R1]  [R2]  [R3]  [R4]  [R5]  [R6]  [R7]  [R8]  [R9]  [W1]
         ───   ───   ───   ───   ───   ───   ───   ───   ───   ───
         Each thread waits for the previous to finish — all serialized
         Total time = sum of all individual times  ← SLOW

With ReadWriteLock:

Time →  [R1][R2][R3][R4][R5][R6][R7][R8][R9]   [W1]
         ─────────────────────────────────────   ────
         All 9 readers run SIMULTANEOUSLY        Writer gets
         Total time = one reader's time          exclusive access
                                                 ← FAST
```

---

## How ReadWriteLock Works

### The Analogy

Think of a **public library reading room** with a whiteboard policy:

```
Library Reading Room
┌───────────────────────────────────────────────────────┐
│                                                       │
│  📖 Readers: Alice, Bob, Carol, Dave (all reading)    │
│                                                       │
│  Rule 1: Multiple readers can be in the room at once  │
│  Rule 2: When a writer enters, ALL readers must leave │
│  Rule 3: Only ONE writer at a time — door locked      │
│                                                       │
│  ✏️  Writer (waiting outside): Eve                    │
└───────────────────────────────────────────────────────┘

State A — Readers active:
  Alice enters   → readers: 1 ✅
  Bob enters     → readers: 2 ✅
  Carol enters   → readers: 3 ✅
  Eve (writer)   → BLOCKED — must wait for all readers to leave

State B — Writer active:
  All readers leave → readers: 0
  Eve enters     → writers: 1, door LOCKED ✅
  Frank (reader) → BLOCKED — must wait for Eve to finish
  Grace (writer) → BLOCKED — must wait for Eve to finish
```

### Lock Compatibility Matrix

|  | Read Lock (held) | Write Lock (held) |
|---|---|---|
| **Read Lock (request)** | ✅ Allowed | ❌ Blocked |
| **Write Lock (request)** | ❌ Blocked | ❌ Blocked |

- **Read + Read** → Compatible — concurrent reads allowed
- **Read + Write** → Incompatible — writer waits for all readers
- **Write + Read** → Incompatible — readers wait for writer
- **Write + Write** → Incompatible — one writer at a time

### Concurrency Timeline

```
Thread:   R1    R2    W1    R3    R4
          │     │     │     │     │
t=0:      ├─READ─┤     │     │     │
t=1:      │     ├─READ─┤     │     │
          │     │     ├─WAIT─┤     │   ← W1 waits for R1, R2 to finish
t=2:      ┤     ┤     │     │     │   ← R1, R2 done
          │     │     ├─WRITE┤     │   ← W1 gets exclusive access
          │     │     │     ├─WAIT─┤   ← R3, R4 wait for W1
t=3:      │     │     ┤     │     │   ← W1 done
          │     │     │     ├─READ─┤   ← R3 and R4 read concurrently
          │     │     │     ├─READ─┘
t=4:      │     │     │     ┤
```

---

## Java's `ReadWriteLock` Interface

### Package and Import

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
```

### Interface Definition

`ReadWriteLock` is an interface with just two methods:

```java
public interface ReadWriteLock {
    Lock readLock();   // returns the shared read lock
    Lock writeLock();  // returns the exclusive write lock
}
```

Both `readLock()` and `writeLock()` return a `Lock` object — the same interface used by `ReentrantLock`. This means the read and write locks support all `Lock` operations: `lock()`, `tryLock()`, `lockInterruptibly()`, and `unlock()`.

---

## Java's `ReentrantReadWriteLock` Class

`ReentrantReadWriteLock` is the standard implementation of `ReadWriteLock`. It is **reentrant** — a thread holding the write lock can re-acquire it, and a thread holding the write lock can also acquire the read lock (downgrading).

### Constructors

```java
// Non-fair (default) — higher throughput, no ordering guarantee
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

// Fair — threads acquire in FIFO arrival order
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(boolean fair);
```

### Key Methods — Outer Lock

| Method | Description |
|---|---|
| `readLock()` | Returns the read `Lock` object |
| `writeLock()` | Returns the write `Lock` object |
| `isFair()` | Returns `true` if fair ordering is enabled |
| `getReadLockCount()` | Returns total number of read locks currently held |
| `isWriteLocked()` | Returns `true` if write lock is held by any thread |
| `isWriteLockedByCurrentThread()` | Returns `true` if current thread holds the write lock |
| `getWriteHoldCount()` | Returns write lock hold count of current thread |
| `getReadHoldCount()` | Returns read lock hold count of current thread |
| `hasQueuedThreads()` | Returns `true` if any threads are waiting |
| `getQueueLength()` | Returns estimated number of waiting threads |

### Key Methods — Read Lock

| Method | Description | Blocking? |
|---|---|---|
| `readLock().lock()` | Acquires read lock; blocks if write lock is held | ✅ Yes |
| `readLock().lockInterruptibly()` | Acquires read lock; interruptible | ✅ Yes |
| `readLock().tryLock()` | Non-blocking read lock attempt | ❌ No |
| `readLock().tryLock(time, unit)` | Timed read lock attempt | ⏱ Timed |
| `readLock().unlock()` | Releases the read lock | ❌ No |

### Key Methods — Write Lock

| Method | Description | Blocking? |
|---|---|---|
| `writeLock().lock()` | Acquires write lock; blocks if any lock is held | ✅ Yes |
| `writeLock().lockInterruptibly()` | Acquires write lock; interruptible | ✅ Yes |
| `writeLock().tryLock()` | Non-blocking write lock attempt | ❌ No |
| `writeLock().tryLock(time, unit)` | Timed write lock attempt | ⏱ Timed |
| `writeLock().unlock()` | Releases the write lock | ❌ No |
| `writeLock().newCondition()` | Returns a `Condition` for the write lock | ❌ No |

> 💡 **Note:** Only the **write lock** supports `newCondition()`. Calling `newCondition()` on the read lock throws `UnsupportedOperationException`.

---

## Basic Usage Pattern

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

// ─── READ operation ──────────────────────────────────────────────
readLock.lock();
try {
    // Multiple threads can execute here simultaneously
    // Read shared data safely
} finally {
    readLock.unlock(); // ALWAYS unlock in finally
}

// ─── WRITE operation ─────────────────────────────────────────────
writeLock.lock();
try {
    // Only ONE thread executes here at a time
    // No readers are active while this runs
    // Modify shared data safely
} finally {
    writeLock.unlock(); // ALWAYS unlock in finally
}
```

---

## Fairness in ReentrantReadWriteLock

```java
// Non-fair (default)
ReentrantReadWriteLock nonFair = new ReentrantReadWriteLock(false);

// Fair
ReentrantReadWriteLock fair = new ReentrantReadWriteLock(true);
```

| Mode | Read Behaviour | Write Behaviour | Throughput | Starvation |
|---|---|---|---|---|
| **Non-fair** | New readers may jump ahead of waiting writers | New writers may barge ahead | Higher | ⚠️ Writers can starve |
| **Fair** | Readers wait if a writer is queued | FIFO ordering for all | Slightly lower | ❌ Eliminated |

```
Non-fair write starvation scenario:

Writer W1 is waiting for readers R1, R2 to finish.
Before they finish, R3, R4, R5 keep arriving and getting the read lock.
W1 keeps waiting... forever (starvation).

Fair mode fix:
Once W1 is queued, new readers R3, R4, R5 also wait — W1 gets its turn.
```

> ⚠️ In non-fair mode, **writers can starve** in very read-heavy systems. Use `fair = true` if writes must complete in bounded time.

---

## Lock Downgrading

**Lock downgrading** means converting a write lock to a read lock **without releasing the lock in between**. This is supported by `ReentrantReadWriteLock`.

```
Write Lock held → acquire Read Lock → release Write Lock → still holding Read Lock
                                                           (downgraded successfully)
```

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

// ✅ Correct downgrade sequence
rwLock.writeLock().lock();         // Step 1: Acquire write lock
try {
    // modify data ...
    rwLock.readLock().lock();      // Step 2: Acquire read lock WHILE holding write
} finally {
    rwLock.writeLock().unlock();   // Step 3: Release write lock (now holding only read)
}
try {
    // Read the data you just wrote — no other writer can interrupt
} finally {
    rwLock.readLock().unlock();    // Step 4: Release read lock
}
```

**Why downgrade?** After writing data, you often need to read it back (e.g., to return a result or validate). Downgrading lets you do this atomically — you keep visibility of the just-written value without letting another writer change it between your write and read.

---

## Lock Upgrading

`ReentrantReadWriteLock` does **NOT** support lock upgrading (read lock → write lock). Attempting it causes a **deadlock**:

```java
// ❌ DEADLOCK — do not attempt
rwLock.readLock().lock();
// ... deciding to write ...
rwLock.writeLock().lock(); // BLOCKS — waiting for all readers (including itself) to finish
                           // DEADLOCK: this thread holds the read lock and waits for itself
```

```
Why upgrading deadlocks:

Thread-A holds read lock.
Thread-A requests write lock → waits for all readers to release.
Thread-A IS one of the readers → waits for itself → DEADLOCK.
```

**Workaround — release and re-acquire:**

```java
// ✅ Correct way to "upgrade" (release read, acquire write)
rwLock.readLock().unlock();  // release read lock first
rwLock.writeLock().lock();   // then acquire write lock
try {
    // Note: data may have changed between unlock and lock
    // Always re-validate state after acquiring write lock
} finally {
    rwLock.writeLock().unlock();
}
```

> 💡 If you need true atomic read-then-write, consider `StampedLock` (Java 8+), which supports optimistic locking and proper upgrade semantics.

---

## Examples

### Example 1 — Basic Read and Write Lock

The simplest demonstration — concurrent reads, exclusive writes:

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BasicReadWriteDemo {

    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static int sharedValue = 0;

    public static int read() {
        rwLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()
                    + " → READ  value = " + sharedValue
                    + " | Active readers: " + rwLock.getReadLockCount());
            Thread.sleep(300); // simulate read time
            return sharedValue;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public static void write(int value) {
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()
                    + " → WRITE value = " + value
                    + " | Write locked: " + rwLock.isWriteLocked());
            Thread.sleep(500); // simulate write time
            sharedValue = value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // Launch 4 concurrent readers
        for (int i = 1; i <= 4; i++) {
            new Thread(BasicReadWriteDemo::read, "Reader-" + i).start();
        }

        Thread.sleep(100);

        // Launch 1 writer — blocks while readers are active
        new Thread(() -> write(42), "Writer-1").start();

        Thread.sleep(1000);

        // After writer completes, new readers see updated value
        for (int i = 5; i <= 7; i++) {
            new Thread(BasicReadWriteDemo::read, "Reader-" + i).start();
        }
    }
}

// Output (readers R1–R4 run concurrently, Writer-1 waits, then R5–R7 run concurrently):
// Reader-1 → READ  value = 0 | Active readers: 4
// Reader-2 → READ  value = 0 | Active readers: 4
// Reader-3 → READ  value = 0 | Active readers: 4
// Reader-4 → READ  value = 0 | Active readers: 4
// Writer-1 → WRITE value = 42 | Write locked: true
// Reader-5 → READ  value = 42 | Active readers: 3
// Reader-6 → READ  value = 42 | Active readers: 3
// Reader-7 → READ  value = 42 | Active readers: 3
```

---

### Example 2 — Thread-Safe Cache

A production-style in-memory cache where many threads read simultaneously and writes are rare:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteCache<K, V> {

    private final Map<K, V> cache                 = new HashMap<>();
    private final ReentrantReadWriteLock rwLock   = new ReentrantReadWriteLock();

    // Many threads can call get() simultaneously
    public V get(K key) {
        rwLock.readLock().lock();
        try {
            V value = cache.get(key);
            System.out.println(Thread.currentThread().getName()
                    + " GET " + key + " = " + value
                    + " [readers active: " + rwLock.getReadLockCount() + "]");
            return value;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Only one thread can call put() at a time; all readers are blocked
    public void put(K key, V value) {
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()
                    + " PUT " + key + " = " + value);
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Read lock — multiple threads can check size concurrently
    public int size() {
        rwLock.readLock().lock();
        try {
            return cache.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Write lock — exclusive clear operation
    public void clear() {
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " CLEAR cache");
            cache.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReadWriteCache<String, String> cache = new ReadWriteCache<>();

        // Pre-populate
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        cache.put("user:3", "Carol");

        // 6 concurrent readers
        for (int i = 1; i <= 6; i++) {
            final String key = "user:" + ((i % 3) + 1);
            new Thread(() -> cache.get(key), "Reader-" + i).start();
        }

        Thread.sleep(100);

        // Writer updates while readers might still be active
        new Thread(() -> cache.put("user:4", "Dave"), "Writer-1").start();

        Thread.sleep(200);

        // More readers after write
        new Thread(() -> cache.get("user:4"), "Reader-7").start();
        new Thread(() -> System.out.println("Cache size: " + cache.size()), "Reader-8").start();
    }
}
```

---

### Example 3 — Concurrent Config Store

Configuration values read thousands of times per second, updated occasionally:

```java
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigStore {

    private Map<String, String> config                = new HashMap<>();
    private final ReentrantReadWriteLock rwLock       = new ReentrantReadWriteLock(true); // fair

    public ConfigStore() {
        // Default configuration
        config.put("db.host",       "localhost");
        config.put("db.port",       "5432");
        config.put("db.poolSize",   "10");
        config.put("cache.ttl",     "3600");
        config.put("app.logLevel",  "INFO");
    }

    public String getProperty(String key) {
        rwLock.readLock().lock();
        try {
            return config.getOrDefault(key, "NOT_FOUND");
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Map<String, String> getAllProperties() {
        rwLock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new HashMap<>(config)); // safe copy
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void setProperty(String key, String value) {
        rwLock.writeLock().lock();
        try {
            System.out.println("[CONFIG UPDATE] " + key + " : "
                    + config.get(key) + " → " + value);
            config.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Atomically replace entire config (e.g., on hot-reload)
    public void reloadConfig(Map<String, String> newConfig) {
        rwLock.writeLock().lock();
        try {
            System.out.println("[CONFIG RELOAD] Replacing " + config.size()
                    + " entries with " + newConfig.size() + " entries");
            config = new HashMap<>(newConfig);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConfigStore store = new ConfigStore();

        // Simulate many threads reading config simultaneously
        for (int i = 0; i < 8; i++) {
            new Thread(() -> {
                String host    = store.getProperty("db.host");
                String port    = store.getProperty("db.port");
                String logLevel = store.getProperty("app.logLevel");
                System.out.println(Thread.currentThread().getName()
                        + " → db=" + host + ":" + port + " log=" + logLevel);
            }, "AppThread-" + i).start();
        }

        Thread.sleep(100);

        // Admin thread updating config — rare event
        new Thread(() -> {
            store.setProperty("app.logLevel", "DEBUG");
            store.setProperty("db.poolSize",  "20");
        }, "AdminThread").start();

        Thread.sleep(200);

        // Hot reload scenario
        Map<String, String> newConfig = new HashMap<>();
        newConfig.put("db.host",     "prod-db.example.com");
        newConfig.put("db.port",     "5432");
        newConfig.put("db.poolSize", "50");
        newConfig.put("app.logLevel","WARN");

        new Thread(() -> store.reloadConfig(newConfig), "ReloadThread").start();
    }
}
```

---

### Example 4 — Read-Heavy Leaderboard

A game leaderboard — read by every player every second, updated rarely:

```java
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Leaderboard {

    private final List<String[]> entries              = new ArrayList<>(); // [rank, player, score]
    private final ReentrantReadWriteLock rwLock       = new ReentrantReadWriteLock();

    public void addOrUpdateScore(String player, int score) {
        rwLock.writeLock().lock();
        try {
            // Remove existing entry if present
            entries.removeIf(e -> e[1].equals(player));
            entries.add(new String[]{"-", player, String.valueOf(score)});

            // Sort descending by score
            entries.sort((a, b) -> Integer.compare(
                    Integer.parseInt(b[2]), Integer.parseInt(a[2])));

            // Recalculate ranks
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i)[0] = String.valueOf(i + 1);
            }

            System.out.println("[WRITE] Updated score: " + player + " = " + score);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Multiple threads read leaderboard concurrently
    public List<String> getTopN(int n) {
        rwLock.readLock().lock();
        try {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < Math.min(n, entries.size()); i++) {
                String[] e = entries.get(i);
                result.add("#" + e[0] + " " + e[1] + " (" + e[2] + " pts)");
            }
            return result;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int getPlayerRank(String player) {
        rwLock.readLock().lock();
        try {
            for (String[] entry : entries) {
                if (entry[1].equals(player)) return Integer.parseInt(entry[0]);
            }
            return -1;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Leaderboard board = new Leaderboard();

        // Initial data
        board.addOrUpdateScore("Alice",   8500);
        board.addOrUpdateScore("Bob",     9200);
        board.addOrUpdateScore("Carol",   7800);
        board.addOrUpdateScore("Dave",    9100);

        // Many concurrent readers
        for (int i = 0; i < 6; i++) {
            new Thread(() -> {
                List<String> top3 = board.getTopN(3);
                System.out.println(Thread.currentThread().getName()
                        + " reads top 3: " + top3);
            }, "Player-" + i).start();
        }

        Thread.sleep(50);

        // Occasional score updates
        new Thread(() -> board.addOrUpdateScore("Eve", 9500), "ScoreUpdater-1").start();
        new Thread(() -> board.addOrUpdateScore("Bob", 9800), "ScoreUpdater-2").start();

        Thread.sleep(200);

        System.out.println("\nFinal leaderboard:");
        board.getTopN(5).forEach(System.out::println);
    }
}
```

---

### Example 5 — Lock Downgrading

Write data and immediately downgrade to a read lock to read the result atomically:

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockDowngradeDemo {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile double cachedResult = 0.0;
    private volatile boolean cacheValid  = false;

    public double getOrCompute() {
        // Step 1: Try to read with read lock first (fast path)
        rwLock.readLock().lock();
        if (cacheValid) {
            try {
                double value = cachedResult;
                System.out.println(Thread.currentThread().getName()
                        + ": cache hit → " + value);
                return value;
            } finally {
                rwLock.readLock().unlock();
            }
        }
        // Cache miss — must compute and write
        rwLock.readLock().unlock(); // release read lock before acquiring write lock

        // Step 2: Acquire write lock to compute and store result
        rwLock.writeLock().lock();
        try {
            // Double-check: another thread may have computed while we waited
            if (!cacheValid) {
                System.out.println(Thread.currentThread().getName()
                        + ": cache miss — computing...");
                Thread.sleep(500); // simulate expensive computation
                cachedResult = Math.random() * 1000;
                cacheValid   = true;
                System.out.println(Thread.currentThread().getName()
                        + ": computed result = " + cachedResult);
            }

            // Step 3: DOWNGRADE — acquire read lock while still holding write lock
            rwLock.readLock().lock();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Step 4: Release write lock — now holding only read lock
            rwLock.writeLock().unlock();
            System.out.println(Thread.currentThread().getName()
                    + ": downgraded write → read");
        }

        // Step 5: Read the result under read lock
        try {
            return cachedResult;
        } finally {
            // Step 6: Release read lock
            rwLock.readLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockDowngradeDemo demo = new LockDowngradeDemo();

        // First call computes and caches; subsequent calls use cache
        for (int i = 1; i <= 5; i++) {
            new Thread(demo::getOrCompute, "Thread-" + i).start();
            Thread.sleep(50);
        }
    }
}

// Output:
// Thread-1: cache miss — computing...
// Thread-1: computed result = 743.21...
// Thread-1: downgraded write → read
// Thread-2: cache hit → 743.21...
// Thread-3: cache hit → 743.21...
// Thread-4: cache hit → 743.21...
// Thread-5: cache hit → 743.21...
```

---

### Example 6 — Fair ReadWriteLock

Prevents writer starvation in read-heavy systems:

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FairReadWriteDemo {

    private static final ReentrantReadWriteLock fairLock =
            new ReentrantReadWriteLock(true); // fair = true

    private static int data = 0;

    static void doRead(String name) {
        System.out.println(name + ": waiting to read...");
        fairLock.readLock().lock();
        try {
            System.out.println(name + ": ✅ reading data = " + data
                    + " [active readers: " + fairLock.getReadLockCount() + "]");
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            fairLock.readLock().unlock();
            System.out.println(name + ": read done");
        }
    }

    static void doWrite(String name, int value) {
        System.out.println(name + ": waiting to write...");
        fairLock.writeLock().lock();
        try {
            System.out.println(name + ": ✅ WRITING data = " + value);
            Thread.sleep(300);
            data = value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            fairLock.writeLock().unlock();
            System.out.println(name + ": write done");
        }
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Fair lock: " + fairLock.isFair());

        // Start readers
        new Thread(() -> doRead("Reader-1"), "Reader-1").start();
        new Thread(() -> doRead("Reader-2"), "Reader-2").start();
        Thread.sleep(50);

        // Writer queues up while readers are active
        // Fair mode: new readers after this writer will wait behind the writer
        new Thread(() -> doWrite("Writer-1", 100), "Writer-1").start();
        Thread.sleep(50);

        // These readers arrive AFTER writer — in fair mode they wait
        new Thread(() -> doRead("Reader-3"), "Reader-3").start();
        new Thread(() -> doRead("Reader-4"), "Reader-4").start();
    }
}

// With fair=true: Reader-1, Reader-2 run → Writer-1 runs → Reader-3, Reader-4 run
// With fair=false: Reader-3, Reader-4 might jump ahead of Writer-1 (writer starvation)
```

---

### Example 7 — tryLock on Read and Write

Non-blocking and timed lock attempts for both read and write:

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;

public class TryLockReadWriteDemo {

    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static String resource = "initial-data";

    public static void main(String[] args) throws InterruptedException {

        // ── Non-blocking tryLock ──────────────────────────────────────────
        System.out.println("=== Non-blocking tryLock ===");

        rwLock.writeLock().lock(); // occupy write lock
        System.out.println("Write lock occupied");

        // Reader tries to acquire — fails immediately (non-blocking)
        boolean readAcquired = rwLock.readLock().tryLock();
        System.out.println("Read tryLock while write held: "
                + (readAcquired ? "✅ acquired" : "❌ failed"));

        // Another writer tries — fails immediately
        boolean writeAcquired = rwLock.writeLock().tryLock();
        System.out.println("Write tryLock while write held: "
                + (writeAcquired ? "✅ acquired" : "❌ failed"));

        rwLock.writeLock().unlock();
        System.out.println("Write lock released\n");

        // ── Timed tryLock ────────────────────────────────────────────────
        System.out.println("=== Timed tryLock ===");

        Thread longReader = new Thread(() -> {
            rwLock.readLock().lock();
            try {
                System.out.println("Long reader: holding read lock for 2s...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.readLock().unlock();
                System.out.println("Long reader: done");
            }
        }, "LongReader");

        longReader.start();
        Thread.sleep(100); // let long reader acquire

        // Writer tries with 3s timeout — succeeds after long reader finishes
        Thread timedWriter = new Thread(() -> {
            try {
                System.out.println("Timed writer: trying (3s timeout)...");
                boolean acquired = rwLock.writeLock().tryLock(3, TimeUnit.SECONDS);
                if (acquired) {
                    try {
                        resource = "updated-data";
                        System.out.println("Timed writer: ✅ acquired! resource = " + resource);
                    } finally {
                        rwLock.writeLock().unlock();
                    }
                } else {
                    System.out.println("Timed writer: ❌ timed out");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "TimedWriter");

        timedWriter.start();

        // Fast writer tries with 0.5s timeout — fails (reader holds lock for 2s)
        Thread fastWriter = new Thread(() -> {
            try {
                Thread.sleep(50);
                System.out.println("Fast writer: trying (0.5s timeout)...");
                boolean acquired = rwLock.writeLock().tryLock(500, TimeUnit.MILLISECONDS);
                System.out.println("Fast writer: "
                        + (acquired ? "✅ acquired" : "❌ timed out — doing fallback"));
                if (acquired) rwLock.writeLock().unlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "FastWriter");

        fastWriter.start();

        longReader.join(); timedWriter.join(); fastWriter.join();
    }
}

// Output:
// === Non-blocking tryLock ===
// Write lock occupied
// Read tryLock while write held:  ❌ failed
// Write tryLock while write held: ❌ failed
// Write lock released
//
// === Timed tryLock ===
// Long reader: holding read lock for 2s...
// Timed writer: trying (3s timeout)...
// Fast writer:  trying (0.5s timeout)...
// Fast writer:  ❌ timed out — doing fallback
// Long reader:  done
// Timed writer: ✅ acquired! resource = updated-data
```

---

### Example 8 — Inventory Management System

A realistic system combining read-heavy queries with occasional write updates:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InventorySystem {

    private final Map<String, Integer> stock       = new HashMap<>();
    private final ReentrantReadWriteLock rwLock    = new ReentrantReadWriteLock(true);

    public InventorySystem() {
        stock.put("Apple",  100);
        stock.put("Banana", 50);
        stock.put("Cherry", 200);
        stock.put("Mango",  75);
    }

    // Read — concurrent across all threads
    public int checkStock(String item) {
        rwLock.readLock().lock();
        try {
            int qty = stock.getOrDefault(item, 0);
            System.out.printf("[%-10s] CHECK  %-8s qty=%-4d [readers: %d]%n",
                    Thread.currentThread().getName(), item, qty,
                    rwLock.getReadLockCount());
            return qty;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Read — returns total inventory value (complex read, still concurrent)
    public int totalStock() {
        rwLock.readLock().lock();
        try {
            return stock.values().stream().mapToInt(Integer::intValue).sum();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Write — exclusive: restock an item
    public void restock(String item, int quantity) {
        rwLock.writeLock().lock();
        try {
            int before = stock.getOrDefault(item, 0);
            stock.put(item, before + quantity);
            System.out.printf("[%-10s] RESTOCK %-8s %d → %d%n",
                    Thread.currentThread().getName(), item, before, before + quantity);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Write — exclusive: process a sale (reduce stock)
    public boolean sell(String item, int quantity) {
        rwLock.writeLock().lock();
        try {
            int current = stock.getOrDefault(item, 0);
            if (current < quantity) {
                System.out.printf("[%-10s] SELL    %-8s FAILED (only %d in stock)%n",
                        Thread.currentThread().getName(), item, current);
                return false;
            }
            stock.put(item, current - quantity);
            System.out.printf("[%-10s] SELL    %-8s %d → %d%n",
                    Thread.currentThread().getName(), item, current, current - quantity);
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        InventorySystem inventory = new InventorySystem();

        // Multiple customer threads checking stock simultaneously
        for (int i = 1; i <= 5; i++) {
            final String item = new String[]{"Apple", "Banana", "Cherry", "Mango", "Apple"}[i - 1];
            new Thread(() -> inventory.checkStock(item), "Customer-" + i).start();
        }

        Thread.sleep(50);

        // Concurrent sales and restocks
        new Thread(() -> inventory.sell("Apple",  20), "Sale-1").start();
        new Thread(() -> inventory.sell("Banana", 60), "Sale-2").start(); // will fail
        new Thread(() -> inventory.restock("Mango", 50), "Restock-1").start();

        Thread.sleep(200);

        System.out.println("\nFinal inventory:");
        inventory.stock.forEach((item, qty) ->
                System.out.println("  " + item + ": " + qty));
        System.out.println("  Total: " + inventory.totalStock() + " units");
    }
}
```

---

## Performance Comparison

| Scenario | `synchronized` | `ReentrantLock` | `ReadWriteLock` |
|---|---|---|---|
| **10 readers, 0 writers** | Sequential | Sequential | ✅ All concurrent |
| **10 readers, 1 writer** | Sequential | Sequential | ✅ Readers concurrent; writer exclusive |
| **1 reader, 1 writer** | Sequential | Sequential | Sequential (write blocks read) |
| **0 readers, 10 writers** | Sequential | Sequential | Sequential (writes exclusive) |

> `ReadWriteLock` shines when **reads greatly outnumber writes**. For write-heavy workloads, it offers no throughput advantage over a plain lock.

---

## ReadWriteLock vs `synchronized`

| Feature | `ReadWriteLock` | `synchronized` |
|---|---|---|
| **Concurrent reads** | ✅ Yes — multiple threads | ❌ No — one at a time |
| **Exclusive writes** | ✅ Yes | ✅ Yes |
| **Fairness control** | ✅ Yes | ❌ No |
| **tryLock support** | ✅ Yes | ❌ No |
| **Interruptible wait** | ✅ Yes | ❌ No |
| **Lock downgrading** | ✅ Yes | ❌ No |
| **Condition variables** | ✅ (write lock only) | ✅ `wait/notify` |
| **Auto-release** | ❌ Manual (`finally`) | ✅ Automatic |
| **Code complexity** | Higher | Lower |
| **Best for** | Read-heavy shared data | Simple mutual exclusion |

---

## ReadWriteLock vs `ReentrantLock`

| Feature | `ReadWriteLock` | `ReentrantLock` |
|---|---|---|
| **Concurrent reads** | ✅ Yes | ❌ No |
| **Exclusive writes** | ✅ Yes | ✅ Yes |
| **Reentrancy** | ✅ Yes (separate for read/write) | ✅ Yes |
| **Lock downgrading** | ✅ Yes (write → read) | ❌ N/A |
| **Lock upgrading** | ❌ No (causes deadlock) | ❌ N/A |
| **Multiple conditions** | ✅ Write lock only | ✅ Unlimited |
| **Complexity** | Higher | Medium |
| **Best for** | Read-heavy workloads | General exclusive locking |

---

## StampedLock — The Modern Alternative

Java 8 introduced `StampedLock`, which improves on `ReadWriteLock` with **optimistic reading** — a lock-free read that assumes no writer is active, and validates afterwards:

```java
import java.util.concurrent.locks.StampedLock;

public class StampedLockDemo {

    private final StampedLock lock = new StampedLock();
    private double x = 0.0, y = 0.0;

    public void move(double deltaX, double deltaY) {
        long stamp = lock.writeLock(); // exclusive write
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public double distanceFromOrigin() {

        // 1. Try optimistic read first (no locking — extremely fast)
        long stamp = lock.tryOptimisticRead();
        double currentX = x;
        double currentY = y;

        // 2. Validate — did a writer modify data during our read?
        if (!lock.validate(stamp)) {
            // 3. Optimistic read failed — fall back to real read lock
            stamp = lock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
}
```

| Feature | `ReadWriteLock` | `StampedLock` |
|---|---|---|
| **Optimistic reads** | ❌ No | ✅ Yes — lock-free reads |
| **Lock upgrading** | ❌ Deadlocks | ✅ Supported |
| **Reentrancy** | ✅ Yes | ❌ No |
| **Condition variables** | ✅ Yes | ❌ No |
| **API complexity** | Medium | High |
| **Best for** | General read-heavy use | Maximum read throughput |

---

## Common Pitfalls

### 1. Forgetting to Unlock — Deadlock

```java
// ❌ Exception before unlock — lock held forever, all other threads blocked
rwLock.readLock().lock();
processData(); // throws RuntimeException
rwLock.readLock().unlock(); // NEVER REACHED

// ✅ Always unlock in finally
rwLock.readLock().lock();
try {
    processData();
} finally {
    rwLock.readLock().unlock(); // GUARANTEED
}
```

### 2. Attempting Lock Upgrade — Deadlock

```java
// ❌ DEADLOCK — read → write upgrade not supported
rwLock.readLock().lock();
try {
    if (needsUpdate()) {
        rwLock.writeLock().lock(); // DEADLOCK — waits for itself to release read lock
    }
} finally {
    rwLock.readLock().unlock();
}

// ✅ Correct pattern — release read, then acquire write
rwLock.readLock().unlock();      // Step 1: release read
rwLock.writeLock().lock();       // Step 2: acquire write
try {
    // re-check condition — state may have changed
} finally {
    rwLock.writeLock().unlock();
}
```

### 3. Using Read Lock for Writes

```java
private int counter = 0;

// ❌ Modifying shared state under read lock — NOT thread-safe
// Multiple threads can hold the read lock simultaneously
public void increment() {
    rwLock.readLock().lock();
    try {
        counter++; // race condition — multiple threads modify simultaneously
    } finally {
        rwLock.readLock().unlock();
    }
}

// ✅ Write operations must use write lock
public void increment() {
    rwLock.writeLock().lock();
    try {
        counter++;
    } finally {
        rwLock.writeLock().unlock();
    }
}
```

### 4. Read Lock Starvation of Writers (Non-Fair)

```java
// ❌ In non-fair mode, continuous stream of readers starves writers
ReentrantReadWriteLock nonFairLock = new ReentrantReadWriteLock(); // default

// If readers arrive continuously, a writer can wait indefinitely

// ✅ Use fair mode when write latency matters
ReentrantReadWriteLock fairLock = new ReentrantReadWriteLock(true);
// Once a writer queues, new readers wait behind it
```

### 5. Calling newCondition() on Read Lock

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

// ❌ Read lock does NOT support conditions
Condition c = rwLock.readLock().newCondition(); // throws UnsupportedOperationException

// ✅ Only the write lock supports newCondition()
Condition c = rwLock.writeLock().newCondition(); // OK
```

---

## Summary

| Concept | Key Point |
|---|---|
| **What it is** | A lock that allows concurrent reads but exclusive writes |
| **Read Lock** | Shared — multiple threads can hold it simultaneously |
| **Write Lock** | Exclusive — one thread only; no readers allowed |
| **Compatibility** | Read+Read ✅, Read+Write ❌, Write+Write ❌ |
| **Always unlock in `finally`** | Both read and write locks must be released in a `finally` block |
| **Lock Downgrading** | Write → Read is supported (atomically) |
| **Lock Upgrading** | Read → Write is NOT supported (causes deadlock) |
| **Fairness** | `new ReentrantReadWriteLock(true)` prevents writer starvation |
| **newCondition()** | Only supported on the write lock |
| **Best use case** | Read-heavy workloads — caches, config stores, leaderboards |
| **No benefit when** | Write-heavy — use `ReentrantLock` or `synchronized` instead |

**When to use `ReadWriteLock`:**

| Scenario | Recommendation |
|---|---|
| Reads >> Writes (e.g. 95% reads) | ✅ `ReentrantReadWriteLock` |
| Equal reads and writes | ⚠️ Marginal benefit — test first |
| Writes >> Reads | ❌ Use `ReentrantLock` or `synchronized` |
| Need optimistic reads (max throughput) | ✅ `StampedLock` (Java 8+) |
| Need lock upgrade (read → write) | ✅ `StampedLock` (Java 8+) |
| Simple mutual exclusion | ✅ `synchronized` or `ReentrantLock` |

> 💡 **Best Practice:** Measure before optimising. `ReadWriteLock` adds overhead compared to a plain lock. It only wins when reads are significantly more frequent than writes **and** read operations are long enough to benefit from concurrency. Always unlock in `finally`, never attempt lock upgrading, and prefer `fair = true` if writer latency is bounded.