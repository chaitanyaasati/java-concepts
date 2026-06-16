# Executor Framework in Java

## Table of Contents
- [Introduction](#introduction)
- [The Problem the Executor Framework Solves](#the-problem-the-executor-framework-solves)
- [Executor Framework Architecture](#executor-framework-architecture)
    - [Interface Hierarchy](#interface-hierarchy)
    - [Key Interfaces Explained](#key-interfaces-explained)
- [Thread Pools — The Core Concept](#thread-pools--the-core-concept)
    - [How a Thread Pool Works](#how-a-thread-pool-works)
    - [Thread Pool Parameters](#thread-pool-parameters)
- [`Executors` Factory Methods](#executors-factory-methods)
    - [Fixed Thread Pool](#fixed-thread-pool)
    - [Cached Thread Pool](#cached-thread-pool)
    - [Single Thread Executor](#single-thread-executor)
    - [Scheduled Thread Pool](#scheduled-thread-pool)
    - [Work-Stealing Pool](#work-stealing-pool)
- [`ThreadPoolExecutor` — Full Control](#threadpoolexecutor--full-control)
    - [Constructor Parameters](#constructor-parameters)
    - [Rejection Policies](#rejection-policies)
    - [Thread Pool States](#thread-pool-states)
    - [Key Methods](#key-methods)
- [`Callable` and `Future`](#callable-and-future)
    - [Callable vs Runnable](#callable-vs-runnable)
    - [Future Methods](#future-methods)
- [`CompletableFuture` (Java 8+)](#completablefuture-java-8)
    - [Key CompletableFuture Methods](#key-completablefuture-methods)
- [`ScheduledExecutorService`](#scheduledexecutorservice)
    - [Schedule Methods](#schedule-methods)
- [Examples](#examples)
    - [Example 1 — Fixed Thread Pool with Runnable](#example-1--fixed-thread-pool-with-runnable)
    - [Example 2 — Callable and Future](#example-2--callable-and-future)
    - [Example 3 — invokeAll and invokeAny](#example-3--invokeall-and-invokeany)
    - [Example 4 — Custom ThreadPoolExecutor](#example-4--custom-threadpoolexecutor)
    - [Example 5 — Cached Thread Pool (Burst Handling)](#example-5--cached-thread-pool-burst-handling)
    - [Example 6 — ScheduledExecutorService](#example-6--scheduledexecutorservice)
    - [Example 7 — CompletableFuture Chaining](#example-7--completablefuture-chaining)
    - [Example 8 — CompletableFuture Combining](#example-8--completablefuture-combining)
    - [Example 9 — Custom Rejection Policy](#example-9--custom-rejection-policy)
    - [Example 10 — Web Crawler with Fixed Thread Pool](#example-10--web-crawler-with-fixed-thread-pool)
    - [Example 11 — Parallel Data Processing Pipeline](#example-11--parallel-data-processing-pipeline)
    - [Example 12 — Graceful Shutdown](#example-12--graceful-shutdown)
- [Choosing the Right Executor](#choosing-the-right-executor)
- [Executor Framework vs Manual Threads](#executor-framework-vs-manual-threads)
- [Common Pitfalls](#common-pitfalls)
- [Summary](#summary)

---

## Introduction

The **Executor Framework** is Java's high-level concurrency API for managing and controlling thread execution. Introduced in **Java 5** (`java.util.concurrent`), it decouples the **submission of tasks** from the **mechanics of task execution** — you hand off a task, and the framework decides how and when to run it.

Before the Executor Framework, developers created `new Thread(task).start()` for every concurrent task — leading to uncontrolled thread creation, wasted resources, and complex lifecycle management. The Executor Framework solves all of this with:

- **Thread Pools** — reuse threads instead of creating new ones per task
- **Task Queuing** — buffer tasks when all threads are busy
- **Lifecycle Management** — clean startup, shutdown, and drain
- **Result Handling** — `Future` and `CompletableFuture` for async results
- **Scheduling** — run tasks once, with a delay, or repeatedly

---

## The Problem the Executor Framework Solves

```
Without Executor Framework (manual threads):

for (Request req : requests) {
    new Thread(() -> handle(req)).start();  // ❌ one thread per task
}

Problems with this approach:
  ├─ 10,000 requests → 10,000 threads → OutOfMemoryError
  ├─ Thread creation is expensive (~1ms, ~512KB stack per thread)
  ├─ Context switching overhead with too many threads
  ├─ No backpressure — server crashes under load
  └─ No way to get return values or handle exceptions cleanly

With Executor Framework:

ExecutorService pool = Executors.newFixedThreadPool(10); // exactly 10 threads

for (Request req : requests) {
    pool.submit(() -> handle(req));  // ✅ queued, processed by 10 reusable threads
}

Benefits:
  ├─ 10,000 requests → 10 threads → tasks queued and processed in order
  ├─ Threads reused → zero creation overhead after warmup
  ├─ Backpressure via bounded queue → no OOM
  ├─ Return values via Future<T>
  └─ Clean shutdown with drain
```

---

## Executor Framework Architecture

### Interface Hierarchy

```
java.util.concurrent
│
├── Executor                          (submit Runnable)
│   └── ExecutorService               (submit + lifecycle)
│       ├── AbstractExecutorService   (base implementation)
│       │   └── ThreadPoolExecutor    (the core implementation)
│       │       └── ScheduledThreadPoolExecutor  (adds scheduling)
│       └── ForkJoinPool              (work-stealing for recursive tasks)
│
├── ScheduledExecutorService          (extends ExecutorService + scheduling)
│
├── Executors                         (factory class — creates executors)
│
├── Callable<V>                       (task that returns a value)
├── Future<V>                         (handle to async result)
│   └── ScheduledFuture<V>           (Future with scheduled delay)
│
└── CompletableFuture<T>              (Java 8+: composable async)
```

### Key Interfaces Explained

```java
// Executor — the simplest interface: just execute a Runnable
public interface Executor {
    void execute(Runnable command);
}

// ExecutorService — adds lifecycle and result-returning submit
public interface ExecutorService extends Executor {
    Future<?>    submit(Runnable task);
    <T> Future<T> submit(Callable<T> task);
    <T> Future<T> submit(Runnable task, T result);

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException;
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException;

    void     shutdown();
    List<Runnable> shutdownNow();
    boolean  isShutdown();
    boolean  isTerminated();
    boolean  awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}

// ScheduledExecutorService — adds delayed and periodic scheduling
public interface ScheduledExecutorService extends ExecutorService {
    ScheduledFuture<?> schedule(Runnable cmd, long delay, TimeUnit unit);
    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);
    ScheduledFuture<?> scheduleAtFixedRate(Runnable cmd, long initialDelay,
                                           long period, TimeUnit unit);
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable cmd, long initialDelay,
                                              long delay, TimeUnit unit);
}
```

---

## Thread Pools — The Core Concept

### How a Thread Pool Works

```
Thread Pool internals:

                 ┌─────────────────────────────────────────────────┐
                 │              ThreadPoolExecutor                  │
                 │                                                  │
  submit(task)   │  Task Queue (BlockingQueue)                      │
  ──────────────►│  [Task-1][Task-2][Task-3][Task-4][Task-5]...    │
                 │        │                                         │
                 │        ▼  (dequeue when thread is free)         │
                 │  ┌──────────────────────────────────────────┐   │
                 │  │          Worker Threads                  │   │
                 │  │  Thread-1: executing Task-1              │   │
                 │  │  Thread-2: executing Task-2              │   │
                 │  │  Thread-3: IDLE (waiting for task)       │   │
                 │  └──────────────────────────────────────────┘   │
                 │                                                  │
                 │  New task arrives → queue it → idle thread picks │
                 │  it up immediately → no thread creation cost!   │
                 └─────────────────────────────────────────────────┘

Thread Lifecycle in pool:
  Created → IDLE → picks up task → RUNNING → task done → IDLE → picks up next task
                                                         ↑
                                                  (thread not destroyed!)
```

### Thread Pool Parameters

| Parameter | Description |
|---|---|
| `corePoolSize` | Minimum threads always kept alive (even if idle) |
| `maximumPoolSize` | Maximum threads the pool can ever create |
| `keepAliveTime` | How long excess (beyond core) threads wait before being destroyed |
| `workQueue` | Queue holding tasks waiting for a free thread |
| `threadFactory` | Creates new threads (can set name, daemon, priority) |
| `rejectedExecutionHandler` | What to do when queue is full and max threads reached |

```
Task arrival logic:

new task arrives
  │
  ├─ active threads < corePoolSize?
  │         └─ YES → create new thread immediately (even if idle ones exist)
  │
  ├─ workQueue is not full?
  │         └─ YES → add task to queue (use idle thread when available)
  │
  ├─ active threads < maximumPoolSize?
  │         └─ YES → create new (non-core) thread to handle task
  │
  └─ all full → REJECTION POLICY fires
```

---

## `Executors` Factory Methods

### Fixed Thread Pool

```java
ExecutorService executor = Executors.newFixedThreadPool(int nThreads);

// Internally creates:
new ThreadPoolExecutor(
    nThreads,                    // corePoolSize
    nThreads,                    // maximumPoolSize (same — fixed size)
    0L, TimeUnit.MILLISECONDS,   // keepAliveTime (irrelevant — no extra threads)
    new LinkedBlockingQueue<>()  // unbounded queue ⚠️
);
```

```
Fixed Thread Pool behaviour:
  Pool size = 3, tasks submitted = 8

  Time 0: [T1 running][T2 running][T3 running]  [T4,T5,T6,T7,T8 queued]
  T1 done: [T4 running][T2 running][T3 running]  [T5,T6,T7,T8 queued]
  T2 done: [T4 running][T5 running][T3 running]  [T6,T7,T8 queued]
  ...and so on

✅ Use for: CPU-bound tasks, known workload, bounded resources
⚠️ Risk: Unbounded queue can grow to OOM if tasks come in faster than processed
```

### Cached Thread Pool

```java
ExecutorService executor = Executors.newCachedThreadPool();

// Internally creates:
new ThreadPoolExecutor(
    0,                           // corePoolSize (no permanent threads)
    Integer.MAX_VALUE,           // maximumPoolSize (unlimited threads!) ⚠️
    60L, TimeUnit.SECONDS,       // keepAliveTime (idle threads die after 60s)
    new SynchronousQueue<>()     // no buffering — direct handoff
);
```

```
Cached Thread Pool behaviour:
  task-1 → new Thread-1 created
  task-2 → new Thread-2 created (Thread-1 is busy)
  task-3 → Thread-1 is now idle → reused
  60s idle → Thread dies

✅ Use for: many short-lived tasks, unpredictable bursts
⚠️ Risk: No limit on thread count — 10,000 tasks → 10,000 threads → OOM
```

### Single Thread Executor

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

// One thread, unbounded queue
// Tasks execute SEQUENTIALLY in submission order
```

```
Single Thread Executor:
  [T1]→[T2]→[T3]→[T4]→[T5]  (strictly sequential, one at a time)

✅ Use for: tasks that must not run concurrently (logging, event sourcing)
✅ If the thread dies due to an exception, a NEW thread is created automatically
```

### Scheduled Thread Pool

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(int corePoolSize);

// Supports: delay once, fixed-rate repeat, fixed-delay repeat
```

### Work-Stealing Pool

```java
ExecutorService executor = Executors.newWorkStealingPool();        // Java 8+
ExecutorService executor = Executors.newWorkStealingPool(int parallelism); // Java 8+

// Internally uses ForkJoinPool
// Each thread has its own deque; idle threads "steal" from busy threads' deques
// Default parallelism = Runtime.getRuntime().availableProcessors()
```

```
Work-Stealing behaviour:
  Thread-1 deque: [T1][T2][T3]
  Thread-2 deque: [T4]
  Thread-3 deque: []  ← idle → steals T3 from Thread-1's deque (from the tail)

✅ Use for: recursive divide-and-conquer tasks, uneven workloads
```

---

## `ThreadPoolExecutor` — Full Control

### Constructor Parameters

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    int corePoolSize,              // min live threads
    int maximumPoolSize,           // max live threads
    long keepAliveTime,            // extra thread idle timeout
    TimeUnit unit,                 // time unit for keepAliveTime
    BlockingQueue<Runnable> workQueue,       // task buffer
    ThreadFactory threadFactory,             // (optional) how to name/configure threads
    RejectedExecutionHandler handler         // (optional) what to do when full
);
```

**Work Queue Options:**

| Queue Type | Behaviour | Use Case |
|---|---|---|
| `LinkedBlockingQueue` (unbounded) | No limit — tasks always queued | Fixed pool (safe if producers are slow) |
| `LinkedBlockingQueue(capacity)` | Bounded — blocks or rejects when full | Backpressure-aware systems |
| `ArrayBlockingQueue(capacity)` | Fixed-size bounded array | Predictable memory |
| `SynchronousQueue` | No buffering — direct handoff only | Cached pool (create thread immediately) |
| `PriorityBlockingQueue` | Tasks sorted by priority | Priority-based scheduling |
| `DelayQueue` | Tasks available after a delay | Delayed execution |

### Rejection Policies

When the queue is full AND all threads are at `maximumPoolSize`, the rejection handler fires:

| Policy | Class | Behaviour |
|---|---|---|
| **Abort** (default) | `AbortPolicy` | Throws `RejectedExecutionException` |
| **Caller Runs** | `CallerRunsPolicy` | The calling thread runs the task itself — backpressure |
| **Discard** | `DiscardPolicy` | Silently drops the task — no error |
| **Discard Oldest** | `DiscardOldestPolicy` | Drops the oldest queued task, retries submit |
| **Custom** | Implement `RejectedExecutionHandler` | Any custom logic |

### Thread Pool States

```
Thread Pool State Machine:

  RUNNING ──shutdown()──► SHUTDOWN ──all tasks done──► TIDYING ──► TERMINATED
      │                       │
      └──shutdownNow()────► STOP ─────────────────────► TIDYING ──► TERMINATED

  RUNNING   : accepting and processing tasks normally
  SHUTDOWN  : no new tasks accepted; existing tasks and queue are processed
  STOP      : no new tasks; queued tasks discarded; running tasks interrupted
  TIDYING   : all tasks terminated; running terminated() hook
  TERMINATED: fully shut down
```

### Key Methods

| Method | Description |
|---|---|
| `execute(Runnable)` | Submit fire-and-forget task |
| `submit(Callable/Runnable)` | Submit task, returns `Future` |
| `shutdown()` | Stop accepting new tasks; finish existing ones |
| `shutdownNow()` | Stop all tasks immediately; return queued tasks |
| `awaitTermination(timeout, unit)` | Block until all tasks complete or timeout |
| `getActiveCount()` | Number of threads currently executing tasks |
| `getCompletedTaskCount()` | Total tasks that completed |
| `getTaskCount()` | Total tasks ever submitted |
| `getQueue()` | Returns the work queue |
| `getPoolSize()` | Current number of threads in pool |
| `getCorePoolSize()` | Core thread count |
| `prestartAllCoreThreads()` | Warm up — start all core threads immediately |
| `allowCoreThreadTimeOut(true)` | Allow core threads to die when idle |
| `purge()` | Remove cancelled futures from work queue |

---

## `Callable` and `Future`

### Callable vs Runnable

```java
// Runnable — no return value, cannot declare checked exceptions
public interface Runnable {
    void run();
}

// Callable<V> — returns a value, can throw checked exceptions
public interface Callable<V> {
    V call() throws Exception;
}
```

| Feature | `Runnable` | `Callable<V>` |
|---|---|---|
| **Return value** | ❌ `void` | ✅ `V` |
| **Throws checked exceptions** | ❌ No | ✅ Yes |
| **Submit via** | `execute()` or `submit()` | `submit()` only |
| **Result** | `Future<?>` (no useful result) | `Future<V>` |

### Future Methods

| Method | Description | Blocking? |
|---|---|---|
| `get()` | Waits until result is available and returns it | ✅ Yes — indefinitely |
| `get(long timeout, TimeUnit unit)` | Waits up to timeout; throws `TimeoutException` | ⏱ Timed |
| `isDone()` | Returns `true` if task completed (any way) | ❌ No |
| `isCancelled()` | Returns `true` if task was cancelled | ❌ No |
| `cancel(boolean mayInterruptIfRunning)` | Attempts to cancel the task | ❌ No |

> ⚠️ `Future.get()` can throw: `ExecutionException` (task threw), `CancellationException` (task cancelled), `InterruptedException` (waiting thread interrupted). Always handle these.

---

## `CompletableFuture` (Java 8+)

`CompletableFuture` extends `Future` with **non-blocking composition** — you chain transformations and callbacks instead of blocking with `get()`.

### Key CompletableFuture Methods

| Method | Description |
|---|---|
| `supplyAsync(Supplier)` | Run supplier async, return `CompletableFuture<T>` |
| `runAsync(Runnable)` | Run runnable async, return `CompletableFuture<Void>` |
| `thenApply(Function)` | Transform result (sync, same thread) |
| `thenApplyAsync(Function)` | Transform result (async, another thread) |
| `thenAccept(Consumer)` | Consume result, return `CompletableFuture<Void>` |
| `thenRun(Runnable)` | Run after completion (no access to result) |
| `thenCompose(Function→CF)` | Flat-map — chain two async operations |
| `thenCombine(CF, BiFunction)` | Combine results of two independent CFs |
| `allOf(CF...)` | Complete when ALL given CFs complete |
| `anyOf(CF...)` | Complete when ANY given CF completes |
| `exceptionally(Function)` | Handle exception, provide fallback value |
| `handle(BiFunction)` | Handle both result and exception |
| `whenComplete(BiConsumer)` | Run callback on completion (result or exception) |
| `complete(T value)` | Manually complete with a value |
| `completeExceptionally(Throwable)` | Manually complete with an exception |
| `join()` | Like `get()` but throws unchecked exceptions |

---

## `ScheduledExecutorService`

### Schedule Methods

| Method | Description | Behaviour |
|---|---|---|
| `schedule(task, delay, unit)` | Run once after delay | One-shot |
| `scheduleAtFixedRate(task, initDelay, period, unit)` | Run every `period` ms (measured from task start) | Fixed rate — may overlap if task is slow |
| `scheduleWithFixedDelay(task, initDelay, delay, unit)` | Run with `delay` gap between task END and next START | Fixed delay — always waits for task to finish |

```
scheduleAtFixedRate(period=5s):
  ├─ Task starts at t=0, ends at t=3s → next starts at t=5s
  ├─ Task starts at t=5s, ends at t=9s → OVERRUN! next starts at t=10s (no overlap)
  └─ Fixed to the clock — period is from start to start

scheduleWithFixedDelay(delay=5s):
  ├─ Task starts at t=0, ends at t=3s → next starts at t=3+5=8s
  ├─ Task starts at t=8s, ends at t=12s → next starts at t=12+5=17s
  └─ Fixed gap between runs — always waits for task completion
```

---

## Examples

### Example 1 — Fixed Thread Pool with Runnable

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FixedThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        int NUM_THREADS = 3;
        int NUM_TASKS   = 9;

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        System.out.println("Submitting " + NUM_TASKS + " tasks to " + NUM_THREADS + "-thread pool");

        for (int i = 1; i <= NUM_TASKS; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.printf("[%s] Task-%d started%n",
                        Thread.currentThread().getName(), taskId);
                try {
                    Thread.sleep(500); // simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.printf("[%s] Task-%d finished%n",
                        Thread.currentThread().getName(), taskId);
            });
        }

        // Graceful shutdown — no new tasks, wait for all submitted tasks to finish
        executor.shutdown();
        boolean done = executor.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("All tasks completed: " + done);
    }
}

// Output (3 threads process 9 tasks in 3 batches of 3):
// [pool-1-thread-1] Task-1 started
// [pool-1-thread-2] Task-2 started
// [pool-1-thread-3] Task-3 started
// [pool-1-thread-1] Task-1 finished
// [pool-1-thread-1] Task-4 started    ← thread immediately picks up next task
// [pool-1-thread-2] Task-2 finished
// [pool-1-thread-2] Task-5 started
// ...
// All tasks completed: true
```

---

### Example 2 — Callable and Future

```java
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

public class CallableFutureDemo {

    // Callable returns a result and can throw checked exceptions
    static Callable<Integer> createTask(int taskId) {
        return () -> {
            System.out.printf("[%s] Task-%d computing...%n",
                    Thread.currentThread().getName(), taskId);
            Thread.sleep(300 + taskId * 100L);

            if (taskId == 4) throw new RuntimeException("Task-4 failed intentionally");

            int result = taskId * taskId; // compute square
            System.out.printf("[%s] Task-%d result=%d%n",
                    Thread.currentThread().getName(), taskId, result);
            return result;
        };
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        List<Future<Integer>> futures = new ArrayList<>();

        // Submit multiple Callable tasks
        for (int i = 1; i <= 6; i++) {
            Future<Integer> future = executor.submit(createTask(i));
            futures.add(future);
        }

        // Collect results
        int totalSum = 0;
        for (int i = 0; i < futures.size(); i++) {
            try {
                int result = futures.get(i).get(5, TimeUnit.SECONDS); // blocks until done
                System.out.println("Got result for Task-" + (i + 1) + ": " + result);
                totalSum += result;
            } catch (ExecutionException e) {
                System.out.println("Task-" + (i + 1) + " failed: " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                System.out.println("Task-" + (i + 1) + " timed out!");
                futures.get(i).cancel(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Total sum of results: " + totalSum);
        executor.shutdown();
    }
}

// Output:
// [pool-1-thread-1] Task-1 computing...
// [pool-1-thread-2] Task-2 computing...
// [pool-1-thread-3] Task-3 computing...
// [pool-1-thread-4] Task-4 computing...
// [pool-1-thread-1] Task-1 result=1
// Got result for Task-1: 1
// [pool-1-thread-2] Task-2 result=4
// Got result for Task-2: 4
// ...
// Task-4 failed: Task-4 failed intentionally
// ...
// Total sum of results: 91
```

---

### Example 3 — invokeAll and invokeAny

```java
import java.util.concurrent.*;
import java.util.*;

public class InvokeAllAnyDemo {

    // Simulates querying multiple databases — returns first success
    static Callable<String> queryDB(String dbName, long delayMs) {
        return () -> {
            Thread.sleep(delayMs);
            System.out.println(Thread.currentThread().getName()
                    + " queried " + dbName + " in " + delayMs + "ms");
            return "Result from " + dbName;
        };
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // ── invokeAll: wait for ALL tasks to complete ─────────────────────────
        System.out.println("=== invokeAll ===");
        List<Callable<String>> allTasks = List.of(
                queryDB("DB-Primary",   200),
                queryDB("DB-Replica-1", 400),
                queryDB("DB-Replica-2", 300)
        );

        long start = System.currentTimeMillis();
        List<Future<String>> allResults = executor.invokeAll(allTasks); // blocks until ALL done
        System.out.println("invokeAll time: " + (System.currentTimeMillis() - start) + "ms");

        for (Future<String> f : allResults) {
            System.out.println("  " + f.get()); // already done — no blocking
        }

        // ── invokeAny: return FIRST successful result, cancel others ──────────
        System.out.println("\n=== invokeAny ===");
        List<Callable<String>> raceTasks = List.of(
                queryDB("Fast-Server",  100),
                queryDB("Medium-Server",300),
                queryDB("Slow-Server",  500)
        );

        start = System.currentTimeMillis();
        String winner = executor.invokeAny(raceTasks); // blocks until FIRST succeeds
        System.out.println("invokeAny winner: " + winner);
        System.out.println("invokeAny time: " + (System.currentTimeMillis() - start) + "ms");

        executor.shutdown();
    }
}

// Output:
// === invokeAll ===
// pool-1-thread-1 queried DB-Primary   in 200ms
// pool-1-thread-3 queried DB-Replica-2 in 300ms
// pool-1-thread-2 queried DB-Replica-1 in 400ms
// invokeAll time: ~410ms
//   Result from DB-Primary
//   Result from DB-Replica-1
//   Result from DB-Replica-2
//
// === invokeAny ===
// pool-1-thread-1 queried Fast-Server in 100ms
// invokeAny winner: Result from Fast-Server
// invokeAny time: ~100ms  ← returns as soon as first finishes
```

---

### Example 4 — Custom ThreadPoolExecutor

```java
import java.util.concurrent.*;

public class CustomThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {

        // Custom thread factory — gives threads meaningful names
        ThreadFactory namedFactory = new ThreadFactory() {
            private int count = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "AppWorker-" + (++count));
                t.setDaemon(false);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                                    // corePoolSize
                5,                                    // maximumPoolSize
                30L, TimeUnit.SECONDS,                // keepAliveTime for extra threads
                new ArrayBlockingQueue<>(10),         // bounded queue — max 10 queued tasks
                namedFactory,                         // custom thread names
                new ThreadPoolExecutor.CallerRunsPolicy() // backpressure when full
        );

        // Allow core threads to terminate when idle
        executor.allowCoreThreadTimeOut(true);

        // Warm up — start all core threads immediately
        executor.prestartAllCoreThreads();

        System.out.println("Pool started | core=" + executor.getCorePoolSize()
                + " max=" + executor.getMaximumPoolSize());

        for (int i = 1; i <= 15; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.printf("[%s] Task-%02d | active=%d queued=%d%n",
                        Thread.currentThread().getName(), taskId,
                        executor.getActiveCount(),
                        executor.getQueue().size());
                try { Thread.sleep(200); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("Completed tasks: " + executor.getCompletedTaskCount());
        System.out.println("Total submitted: " + executor.getTaskCount());
    }
}
```

---

### Example 5 — Cached Thread Pool (Burst Handling)

```java
import java.util.concurrent.*;

public class CachedThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        System.out.println("=== Burst 1: 5 tasks ===");
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            executor.submit(() -> {
                System.out.printf("[%s] Task-%d starts%n",
                        Thread.currentThread().getName(), id);
                try { Thread.sleep(500); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.printf("[%s] Task-%d ends%n",
                        Thread.currentThread().getName(), id);
            });
        }

        Thread.sleep(700); // wait for burst 1 to finish, threads now idle

        System.out.println("\n=== Burst 2: 3 tasks (threads reused from burst 1) ===");
        for (int i = 6; i <= 8; i++) {
            final int id = i;
            executor.submit(() -> {
                System.out.printf("[%s] Task-%d starts (thread reused: %b)%n",
                        Thread.currentThread().getName(), id,
                        Thread.currentThread().getName().contains("pool-1"));
                try { Thread.sleep(200); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Done — notice threads are REUSED in burst 2");
    }
}
```

---

### Example 6 — ScheduledExecutorService

```java
import java.util.concurrent.*;
import java.time.LocalTime;

public class ScheduledExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

        // ── 1. One-shot delayed task ──────────────────────────────────────────
        System.out.println("Scheduling one-shot task (2s delay)...");
        ScheduledFuture<?> oneShot = scheduler.schedule(
                () -> System.out.println("[" + LocalTime.now() + "] One-shot task ran"),
                2, TimeUnit.SECONDS
        );

        // ── 2. Fixed-rate: run every 1s (period measured from START of each run) ─
        System.out.println("Scheduling fixed-rate task (every 1s)...");
        ScheduledFuture<?> fixedRate = scheduler.scheduleAtFixedRate(
                () -> System.out.println("[" + LocalTime.now() + "] Fixed-rate task"),
                0, 1, TimeUnit.SECONDS   // start immediately, repeat every 1s
        );

        // ── 3. Fixed-delay: run with 1s gap AFTER each completion ────────────
        System.out.println("Scheduling fixed-delay task (1s after each run)...");
        ScheduledFuture<?> fixedDelay = scheduler.scheduleWithFixedDelay(
                () -> {
                    System.out.println("[" + LocalTime.now() + "] Fixed-delay task START");
                    try { Thread.sleep(300); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("[" + LocalTime.now() + "] Fixed-delay task END");
                },
                500, 1000, TimeUnit.MILLISECONDS // 500ms initial delay, 1s between runs
        );

        // ── 4. Schedule a Callable (with return value) ────────────────────────
        ScheduledFuture<String> callableResult = scheduler.schedule(
                () -> "Scheduled callable completed at " + LocalTime.now(),
                3, TimeUnit.SECONDS
        );

        // Let tasks run for 5 seconds
        Thread.sleep(5000);

        // Cancel repeating tasks
        fixedRate.cancel(false);
        fixedDelay.cancel(false);

        // Get the callable result
        try {
            System.out.println("\nCallable result: " + callableResult.get());
        } catch (ExecutionException e) {
            System.err.println("Callable failed: " + e.getCause());
        }

        scheduler.shutdown();
        System.out.println("Scheduler stopped.");
    }
}
```

---

### Example 7 — CompletableFuture Chaining

```java
import java.util.concurrent.*;

public class CompletableFutureChaining {

    static ExecutorService executor = Executors.newFixedThreadPool(4);

    // Simulated async operations
    static CompletableFuture<String> fetchUser(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] Fetching user " + userId);
            sleep(200);
            return "User-" + userId;
        }, executor);
    }

    static CompletableFuture<String> fetchOrders(String user) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] Fetching orders for " + user);
            sleep(300);
            return user + " has 5 orders";
        }, executor);
    }

    static CompletableFuture<String> enrichWithRecommendations(String orderInfo) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] Adding recommendations");
            sleep(100);
            return orderInfo + " | Recommended: ProductX, ProductY";
        }, executor);
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        // Chain of async operations — each step starts when the previous finishes
        CompletableFuture<String> pipeline = fetchUser(42)
                .thenCompose(user -> fetchOrders(user))            // flat-map
                .thenCompose(orders -> enrichWithRecommendations(orders)) // flat-map
                .thenApply(result -> "FINAL: " + result.toUpperCase())    // transform
                .exceptionally(ex -> "ERROR: " + ex.getMessage());        // error fallback

        // Non-blocking — just registers a callback
        pipeline.thenAccept(result -> {
            System.out.println("\nPipeline result: " + result);
            System.out.println("Total time: " + (System.currentTimeMillis() - start) + "ms");
        });

        // Wait for the pipeline to complete (in real code, you'd return the CF)
        pipeline.join();
        executor.shutdown();
    }
}

// Output:
// [pool-1-thread-1] Fetching user 42
// [pool-1-thread-2] Fetching orders for User-42
// [pool-1-thread-3] Adding recommendations
//
// Pipeline result: FINAL: USER-42 HAS 5 ORDERS | RECOMMENDED: PRODUCTX, PRODUCTY
// Total time: ~620ms  (sequential chain: 200 + 300 + 100 = 600ms)
```

---

### Example 8 — CompletableFuture Combining

```java
import java.util.concurrent.*;
import java.util.List;
import java.util.stream.*;

public class CompletableFutureCombining {

    static ExecutorService executor = Executors.newFixedThreadPool(6);

    static CompletableFuture<Double> getStockPrice(String ticker) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100 + (int)(Math.random() * 200));
            double price = 100 + Math.random() * 900;
            System.out.printf("[%s] %s price: %.2f%n",
                    Thread.currentThread().getName(), ticker, price);
            return price;
        }, executor);
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        // ── thenCombine: combine two independent futures ───────────────────────
        CompletableFuture<Double> applePrice = getStockPrice("AAPL");
        CompletableFuture<Double> googlePrice = getStockPrice("GOOGL");

        CompletableFuture<Double> portfolio2 = applePrice
                .thenCombine(googlePrice, (a, g) -> a + g);

        System.out.println("\n2-stock portfolio: $" + String.format("%.2f", portfolio2.get()));
        System.out.println("Time (parallel): " + (System.currentTimeMillis() - start) + "ms\n");

        // ── allOf: wait for ALL futures then aggregate ─────────────────────────
        start = System.currentTimeMillis();
        String[] tickers = {"AAPL", "GOOGL", "MSFT", "AMZN", "META"};

        List<CompletableFuture<Double>> priceFutures = Arrays.stream(tickers)
                .map(CompletableFutureeCombining::getStockPrice)
                .collect(Collectors.toList());

        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                priceFutures.toArray(new CompletableFuture[0]));

        double portfolioTotal = allDone.thenApply(v ->
                priceFutures.stream()
                        .mapToDouble(f -> f.join())
                        .sum()
        ).get();

        System.out.printf("5-stock portfolio total: $%.2f%n", portfolioTotal);
        System.out.println("Time (all parallel): " + (System.currentTimeMillis() - start) + "ms\n");

        // ── anyOf: return as soon as ANY future completes ─────────────────────
        start = System.currentTimeMillis();
        CompletableFuture<Object> fastest = CompletableFuture.anyOf(
                getStockPrice("FAST1"), getStockPrice("FAST2"), getStockPrice("FAST3")
        );

        System.out.println("Fastest response: $" + String.format("%.2f", (Double)fastest.get()));
        System.out.println("anyOf time: " + (System.currentTimeMillis() - start) + "ms");

        executor.shutdown();
    }
}
```

---

### Example 9 — Custom Rejection Policy

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomRejectionDemo {

    static AtomicInteger rejectedCount = new AtomicInteger(0);

    // Custom rejection handler — logs and tracks rejected tasks
    static class LoggingRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            rejectedCount.incrementAndGet();
            System.out.printf("❌ REJECTED | queue=%d active=%d rejected_total=%d%n",
                    executor.getQueue().size(),
                    executor.getActiveCount(),
                    rejectedCount.get());
            // Could also: write to dead-letter queue, alert monitoring, save to DB
        }
    }

    public static void main(String[] args) throws InterruptedException {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                             // corePoolSize
                3,                             // maximumPoolSize
                10L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),   // max 3 queued tasks
                new LoggingRejectionHandler()  // custom policy
        );

        // Submit 10 tasks — pool can handle: 3 threads + 3 queued = 6 max
        // Tasks 7-10 will be rejected
        System.out.println("Submitting 10 tasks to pool (max capacity=6)...\n");
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            executor.execute(() -> {
                System.out.printf("✅ Running Task-%02d | [%s]%n",
                        id, Thread.currentThread().getName());
                try { Thread.sleep(500); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Thread.sleep(10); // slight delay between submissions
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("\nTotal rejected: " + rejectedCount.get());
        System.out.println("Completed: " + executor.getCompletedTaskCount());
    }
}

// Output:
// Submitting 10 tasks to pool (max capacity=6)...
// ✅ Running Task-01 | [pool-1-thread-1]
// ✅ Running Task-02 | [pool-1-thread-2]
// ...
// ❌ REJECTED | queue=3 active=3 rejected_total=1
// ❌ REJECTED | queue=3 active=3 rejected_total=2
// ...
// Total rejected: 4
// Completed: 6
```

---

### Example 10 — Web Crawler with Fixed Thread Pool

```java
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {

    private final ExecutorService      executor;
    private final Set<String>          visited = ConcurrentHashMap.newKeySet();
    private final List<Future<String>> results = new CopyOnWriteArrayList<>();
    private final AtomicInteger        crawled = new AtomicInteger(0);

    WebCrawler(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void crawl(String url) {
        if (!visited.add(url)) return; // already visited

        Future<String> future = executor.submit(() -> {
            // Simulate fetching and parsing the URL
            Thread.sleep(100 + (int)(Math.random() * 200));
            String content = "Content of [" + url + "] fetched by "
                    + Thread.currentThread().getName();
            crawled.incrementAndGet();
            System.out.println("Crawled: " + url);
            return content;
        });
        results.add(future);
    }

    public List<String> getAllResults() throws Exception {
        List<String> contents = new ArrayList<>();
        for (Future<String> f : results) {
            try {
                contents.add(f.get(5, TimeUnit.SECONDS));
            } catch (ExecutionException | TimeoutException e) {
                contents.add("ERROR: " + e.getMessage());
            }
        }
        return contents;
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        WebCrawler crawler = new WebCrawler(4); // 4 threads

        // Simulate URLs to crawl
        List<String> urls = List.of(
                "https://example.com",         "https://example.com/about",
                "https://example.com/products","https://example.com/blog",
                "https://example.com/contact", "https://example.com/faq",
                "https://example.com",         // duplicate — skipped!
                "https://example.com/pricing", "https://example.com/docs",
                "https://example.com/api"
        );

        long start = System.currentTimeMillis();
        urls.forEach(crawler::crawl);
        crawler.shutdown();

        System.out.println("\nTotal crawled: " + crawler.crawled.get());
        System.out.println("Total time  : " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("(4 threads crawled ~9 pages in parallel)");
    }
}
```

---

### Example 11 — Parallel Data Processing Pipeline

```java
import java.util.concurrent.*;
import java.util.*;
import java.util.stream.*;

public class ParallelPipeline {

    record Order(int id, String customer, double amount) {}

    static ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    // Stage 1: validate orders
    static CompletableFuture<Order> validate(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            if (order.amount() <= 0) throw new IllegalArgumentException("Invalid amount");
            System.out.printf("[VALIDATE] Order-%d for %s (%.2f)%n",
                    order.id(), order.customer(), order.amount());
            return order;
        }, executor);
    }

    // Stage 2: apply discount
    static CompletableFuture<Order> applyDiscount(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            double discounted = order.amount() * (order.amount() > 500 ? 0.9 : 1.0);
            System.out.printf("[DISCOUNT] Order-%d: %.2f → %.2f%n",
                    order.id(), order.amount(), discounted);
            return new Order(order.id(), order.customer(), discounted);
        }, executor);
    }

    // Stage 3: send confirmation
    static CompletableFuture<String> confirm(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.printf("[CONFIRM] Order-%d confirmed for %s: $%.2f%n",
                    order.id(), order.customer(), order.amount());
            return "Order-" + order.id() + " CONFIRMED";
        }, executor);
    }

    public static void main(String[] args) throws Exception {
        List<Order> orders = List.of(
                new Order(1, "Alice",  750.0),
                new Order(2, "Bob",    200.0),
                new Order(3, "Carol",  920.0),
                new Order(4, "Dave",  -50.0),   // invalid — will fail
                new Order(5, "Eve",   430.0)
        );

        // Process all orders in parallel through the pipeline
        List<CompletableFuture<String>> pipelines = orders.stream()
                .map(order -> validate(order)
                        .thenCompose(ParallelPipeline::applyDiscount)
                        .thenCompose(ParallelPipeline::confirm)
                        .exceptionally(ex -> "Order-" + order.id()
                                + " FAILED: " + ex.getCause().getMessage()))
                .collect(Collectors.toList());

        // Wait for all and collect results
        CompletableFuture.allOf(pipelines.toArray(new CompletableFuture[0])).join();

        System.out.println("\n=== Pipeline Results ===");
        pipelines.forEach(f -> System.out.println("  " + f.join()));

        executor.shutdown();
    }
}

// Output (all orders processed in parallel):
// [VALIDATE] Order-1 for Alice (750.00)
// [VALIDATE] Order-2 for Bob (200.00)
// ...
// [DISCOUNT] Order-1: 750.00 → 675.00   ← 10% discount applied
// [CONFIRM]  Order-1 confirmed for Alice: $675.00
// ...
// === Pipeline Results ===
//   Order-1 CONFIRMED
//   Order-2 CONFIRMED
//   Order-3 CONFIRMED
//   Order-4 FAILED: Invalid amount
//   Order-5 CONFIRMED
```

---

### Example 12 — Graceful Shutdown

```java
import java.util.concurrent.*;
import java.util.List;

public class GracefulShutdownDemo {

    static ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws InterruptedException {

        // Submit long-running tasks
        for (int i = 1; i <= 8; i++) {
            final int id = i;
            executor.submit(() -> {
                System.out.println("Task-" + id + " started");
                try {
                    Thread.sleep(1000);
                    System.out.println("Task-" + id + " completed ✅");
                } catch (InterruptedException e) {
                    System.out.println("Task-" + id + " interrupted ⚠️");
                    Thread.currentThread().interrupt();
                }
            });
        }

        // ── Graceful shutdown sequence ─────────────────────────────────────────
        System.out.println("\nInitiating graceful shutdown...");

        executor.shutdown(); // stop accepting new tasks; let existing tasks complete

        // Wait up to 5s for running tasks to finish
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Tasks still running after 5s — forcing shutdown...");

            // Force stop — interrupts running threads, returns queued tasks
            List<Runnable> cancelledTasks = executor.shutdownNow();
            System.out.println("Cancelled " + cancelledTasks.size() + " queued tasks");

            // Wait a bit more for running threads to respond to interrupt
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate — possible runaway thread");
            }
        }

        System.out.println("Shutdown complete. Terminated: " + executor.isTerminated());
    }
}

// Output:
// Task-1 started ... Task-4 started
// Initiating graceful shutdown...
// Task-1 completed ✅ ... Task-4 completed ✅  (first batch finishes within 5s)
// Task-5 started ... Task-8 started           (second batch starts)
// (5s timeout) Tasks still running after 5s — forcing shutdown...
// Task-5 interrupted ⚠️ ... Task-8 interrupted ⚠️
// Cancelled 0 queued tasks
// Shutdown complete. Terminated: true
```

---

## Choosing the Right Executor

| Executor | Core Threads | Max Threads | Queue | Best For |
|---|---|---|---|---|
| `newFixedThreadPool(n)` | n | n | Unbounded LinkedBlockingQueue | CPU-bound tasks, stable workload |
| `newCachedThreadPool()` | 0 | Integer.MAX_VALUE | SynchronousQueue | Many short-lived tasks, burst traffic |
| `newSingleThreadExecutor()` | 1 | 1 | Unbounded LinkedBlockingQueue | Sequential execution, event sourcing |
| `newScheduledThreadPool(n)` | n | Integer.MAX_VALUE | DelayedWorkQueue | Cron-like scheduling, retries |
| `newWorkStealingPool()` | — | CPU cores | Per-thread deque | Recursive tasks, fork-join style |
| Custom `ThreadPoolExecutor` | configurable | configurable | Bounded | Production systems needing full control |

```
Decision guide:

What type of tasks?
  │
  ├─ Short bursts, async I/O, unknown count?
  │         └─ CachedThreadPool  (dynamic threads, 60s idle die-off)
  │
  ├─ Known count, CPU-bound, stable load?
  │         └─ FixedThreadPool(n=CPU cores + 1)
  │
  ├─ Must run sequentially (event log, state machine)?
  │         └─ SingleThreadExecutor
  │
  ├─ Periodic or delayed tasks (heartbeat, cleanup)?
  │         └─ ScheduledThreadPool
  │
  ├─ Recursive divide-and-conquer (sort, tree traversal)?
  │         └─ WorkStealingPool / ForkJoinPool
  │
  └─ Production with backpressure, monitoring, custom naming?
            └─ Custom ThreadPoolExecutor (bounded queue + rejection policy)
```

---

## Executor Framework vs Manual Threads

| Concern | Manual `new Thread()` | Executor Framework |
|---|---|---|
| **Thread reuse** | ❌ New thread per task | ✅ Pooled and reused |
| **Thread count control** | ❌ Unbounded | ✅ Bounded (core + max) |
| **Task queuing** | ❌ Manual blocking | ✅ Built-in queue |
| **Backpressure** | ❌ None | ✅ Bounded queue + rejection |
| **Return values** | ❌ Shared state / callbacks | ✅ `Future` / `CompletableFuture` |
| **Exception handling** | ❌ Uncaught exception handler | ✅ `Future.get()` wraps exceptions |
| **Scheduling** | ❌ Manual `Timer` | ✅ `ScheduledExecutorService` |
| **Lifecycle** | ❌ Manual join() management | ✅ `shutdown()` / `awaitTermination()` |
| **Monitoring** | ❌ None | ✅ `getActiveCount()`, `getCompletedTaskCount()` |
| **Memory** | ❌ Stack per thread always | ✅ Configurable, predictable |

---

## Common Pitfalls

### 1. Never Calling `shutdown()` — Thread Leak

```java
// ❌ Executor threads are non-daemon — JVM won't exit if they keep running
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(task);
// program "finishes" but JVM stays alive — thread pool still running!

// ✅ Always shutdown the executor
ExecutorService executor = Executors.newFixedThreadPool(4);
try {
    executor.submit(task);
} finally {
    executor.shutdown(); // always call, even on exception
}
```

### 2. Ignoring `Future` Exceptions

```java
// ❌ Exception is swallowed — task silently fails
Future<?> future = executor.submit(() -> {
    throw new RuntimeException("Something went wrong");
});
// No get() call → exception is NEVER seen!

// ✅ Always call get() to observe exceptions
try {
    future.get(); // throws ExecutionException wrapping the original exception
} catch (ExecutionException e) {
    System.err.println("Task failed: " + e.getCause());
}
```

### 3. Unbounded Queue with Fixed Thread Pool — OOM

```java
// ❌ Default Executors.newFixedThreadPool uses UNBOUNDED LinkedBlockingQueue
ExecutorService executor = Executors.newFixedThreadPool(4);
// If tasks arrive faster than processed → queue grows without bound → OOM

// ✅ Use bounded queue with custom ThreadPoolExecutor
ThreadPoolExecutor executor = new ThreadPoolExecutor(
        4, 8, 30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(1000),   // bounded — at most 1000 queued
        new ThreadPoolExecutor.CallerRunsPolicy() // backpressure when full
);
```

### 4. Blocking Inside a Pool Thread — Thread Pool Deadlock

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

// ❌ DEADLOCK — outer tasks submit inner tasks and wait for them
// but all 2 threads are occupied by outer tasks waiting for inner tasks
executor.submit(() -> {
    Future<String> inner = executor.submit(() -> "inner result");
    return inner.get(); // DEADLOCK if pool is full
});

// ✅ Expand pool size, or use CompletableFuture (non-blocking composition)
// Or use separate executor for inner tasks
```

### 5. Using `newCachedThreadPool` for Long-Running Tasks

```java
// ❌ CachedThreadPool under heavy load → unbounded thread creation
ExecutorService executor = Executors.newCachedThreadPool();
for (int i = 0; i < 10_000; i++) {
    executor.submit(() -> {
        Thread.sleep(60_000); // 60s task
    }); // → 10,000 threads → OOM!
}

// ✅ Use FixedThreadPool for long-running or blocking tasks
ExecutorService executor = Executors.newFixedThreadPool(100);
```

### 6. Calling `shutdownNow()` Without Handling Interrupted Status

```java
List<Runnable> notRun = executor.shutdownNow();
// notRun contains tasks that were queued but never started

// ❌ Threads interrupted — but tasks must check and respond
// Threads that ignore InterruptedException will keep running after shutdownNow

// ✅ Worker tasks should check for interrupt
executor.submit(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        doWork();
    }
    System.out.println("Stopped cleanly after interrupt");
});
```

### 7. Using `CompletableFuture.get()` on the Main Thread — Blocks Everything

```java
// ❌ Defeats the purpose of async — blocks the calling thread
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> fetchData());
String result = cf.get(); // blocks main thread — might as well be synchronous

// ✅ Chain callbacks — never block
CompletableFuture.supplyAsync(() -> fetchData())
        .thenAccept(result -> processResult(result))  // non-blocking callback
        .exceptionally(ex -> { handleError(ex); return null; });
```

---

## Summary

| Concept | Key Point |
|---|---|
| **Executor** | Simplest interface — just `execute(Runnable)` |
| **ExecutorService** | Adds `submit()` (returns `Future`), `invokeAll()`, `invokeAny()`, lifecycle |
| **ThreadPoolExecutor** | Core implementation — configurable pool, queue, rejection policy |
| **Executors factory** | Quick creation: `newFixedThreadPool`, `newCachedThreadPool`, etc. |
| **FixedThreadPool** | Best for CPU-bound stable workloads; use bounded queue in production |
| **CachedThreadPool** | Best for short bursts; dangerous for long-running tasks |
| **SingleThreadExecutor** | Sequential execution; auto-replaces crashed thread |
| **ScheduledExecutorService** | Delay once, fixed-rate, or fixed-delay repeating tasks |
| **WorkStealingPool** | ForkJoinPool-based; idle threads steal from busy ones |
| **Callable vs Runnable** | Callable returns `V` and can throw; Runnable returns void |
| **Future** | Handle to async result; `get()` blocks; always handle `ExecutionException` |
| **CompletableFuture** | Non-blocking composition; chain transforms, combine results, handle errors |
| **invokeAll** | Submit list of Callables; returns when ALL complete |
| **invokeAny** | Submit list of Callables; returns when FIRST succeeds, cancels rest |
| **Rejection Policies** | Abort (default), CallerRuns (backpressure), Discard, DiscardOldest, Custom |
| **Always shutdown** | Call `shutdown()` or `shutdownNow()` — prevent thread leaks |
| **Bounded queues** | Use in production; unbounded queues can cause OOM |
| **Avoid pool deadlock** | Don't block pool threads waiting on tasks in the same pool |

**Quick Selection Guide:**

```
Need to run async tasks?
  │
  ├─ Fire-and-forget, no return value?
  │         └─ executor.execute(Runnable) ✅
  │
  ├─ Need return value or exception?
  │         └─ executor.submit(Callable) → Future<V> ✅
  │
  ├─ Wait for ALL tasks?
  │         └─ executor.invokeAll(tasks) ✅
  │
  ├─ Return as soon as ANY task succeeds?
  │         └─ executor.invokeAny(tasks) ✅
  │
  ├─ Chain async stages without blocking?
  │         └─ CompletableFuture.supplyAsync().thenCompose().thenApply() ✅
  │
  ├─ Combine independent async results?
  │         └─ CompletableFuture.allOf() / thenCombine() ✅
  │
  └─ Run on a schedule / periodically?
            └─ ScheduledExecutorService.scheduleAtFixedRate() ✅
```

> 💡 **Best Practice:** Prefer `CompletableFuture` over raw `Future` for non-trivial async workflows — it avoids blocking with `get()` and enables clean composition. Always use bounded queues in production `ThreadPoolExecutor` to avoid OOM. Always call `executor.shutdown()` — non-daemon threads will keep the JVM alive indefinitely otherwise. Name your threads via a custom `ThreadFactory` for easier debugging.