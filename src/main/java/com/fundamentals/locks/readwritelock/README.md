# ReadWriteLock 🚀

In Java, the ReadWriteLock interface (and its most common implementation, ReentrantReadWriteLock) is designed to improve performance in systems with high read frequency and low write frequency.

Standard locks (like synchronized or ReentrantLock) are "exclusive," meaning only one thread can access a resource at a time. A ReadWriteLock allows multiple readers to hold the lock simultaneously, as long as no one is writing.

## The Core Logic
- Read Lock: Multiple threads can hold it at once. If a writer wants the lock, it must wait for all readers to finish.
- Write Lock: Only one thread can hold it. No other readers or writers can access the resource while the write lock is held.

## Key Features to Know
- Reentrancy: Just like ReentrantLock, a thread can acquire the same lock multiple times without deadlocking itself.
- Lock Downgrading: You can acquire a write lock, then acquire the read lock, and then release the write lock. This "downgrades" your hold to a read lock safely.

  Note: You cannot "upgrade" from a read lock to a write lock directly; you must release the read lock first.
- Fairness: You can pass true into the constructor—new ReentrantReadWriteLock(true)—to grant the lock to the thread that has been waiting the longest (FIFO).

## When to Use it
While it sounds strictly better than a standard lock, ReadWriteLock has more overhead. You should only use it if:
1. Read operations are frequent and long-running.
2. Write operations are infrequent.

If your "read" is just a quick return value;, a standard ReentrantLock or even an AtomicInteger will likely be faster because the management overhead of the ReadWriteLock will outweigh the concurrency benefits.

| Feature        | Read Lock                              | Write Lock                                     |
|----------------|----------------------------------------|------------------------------------------------|
| Concurrency    | Shared(Multiple Threads)               | Exclusive(One thread)                          |
| Wait Condition | Waits if a thread holds the write lock | Waits if any thread holds a read or write lock |
| Best For       | Thread-Safe Data retrieval             | Data modification                              |
