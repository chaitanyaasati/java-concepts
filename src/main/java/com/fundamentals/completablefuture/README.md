## CompletableFuture Important Methods in Java

> `CompletableFuture` is a class in Java (`java.util.concurrent.CompletableFuture`).
> It represents a **future result of an asynchronous computation**.
> It supports **chaining**, **combining**, **exception handling**, and **manual completion**.
> It implements both `Future` and `CompletionStage` interfaces.
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

CompletableFuture<String> future = new CompletableFuture<>();
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
```

---

### 1. Creation Methods

| Method | Description | Returns Value? |
|--------|-------------|----------------|
| `new CompletableFuture<>()` | Creates incomplete future (manual completion) | Yes |
| `CompletableFuture.runAsync(runnable)` | Runs task asynchronously | No (Void) |
| `CompletableFuture.runAsync(runnable, executor)` | Runs task on custom executor | No (Void) |
| `CompletableFuture.supplyAsync(supplier)` | Runs task asynchronously with result | Yes |
| `CompletableFuture.supplyAsync(supplier, executor)` | Runs task on custom executor with result | Yes |
| `CompletableFuture.completedFuture(value)` | Creates already completed future | Yes |
| `CompletableFuture.failedFuture(throwable)` | Creates already failed future (Java 9+) | Yes |
```java
// Manual future
CompletableFuture<String> manual = new CompletableFuture<>();

// Run without result (Runnable)
CompletableFuture<Void> run = CompletableFuture.runAsync(() -> {
    System.out.println("Running in: " + Thread.currentThread().getName());
});

// Run with result (Supplier)
CompletableFuture<String> supply = CompletableFuture.supplyAsync(() -> {
    return "Hello World";
});

// Custom executor
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletableFuture<String> custom = CompletableFuture.supplyAsync(() -> {
    return "Custom thread";
}, executor);

// Already completed
CompletableFuture<String> done = CompletableFuture.completedFuture("Done");

// Already failed (Java 9+)
CompletableFuture<String> failed = CompletableFuture.failedFuture(
    new RuntimeException("Error")
);
```

---

### 2. Manual Completion Methods

| Method | Description |
|--------|-------------|
| `complete(value)` | Completes future with given value |
| `completeExceptionally(throwable)` | Completes future with exception |
| `completeAsync(supplier)` | Completes future async with supplier (Java 9+) |
| `completeAsync(supplier, executor)` | Completes future async on executor (Java 9+) |
| `cancel(mayInterruptIfRunning)` | Cancels the future |
| `obtrudeValue(value)` | Forces completion with value (even if already done) |
| `obtrudeException(throwable)` | Forces completion with exception (even if already done) |
```java
CompletableFuture<String> future = new CompletableFuture<>();

// Complete with value
future.complete("Result");

// Complete with exception
future.completeExceptionally(new RuntimeException("Something went wrong"));

// Complete async (Java 9+)
future.completeAsync(() -> "Async Result");
future.completeAsync(() -> "Async Result", executor);

// Cancel
future.cancel(true);

// Force complete (overrides existing result)
future.obtrudeValue("Forced Result");
future.obtrudeException(new RuntimeException("Forced Error"));
```

---

### 3. Result Retrieval Methods

| Method | Description | Blocks? | Throws Exception? |
|--------|-------------|---------|-------------------|
| `get()` | Retrieves result | Yes (indefinitely) | Yes (checked) |
| `get(timeout, unit)` | Retrieves result with timeout | Yes (up to timeout) | Yes (checked) |
| `join()` | Retrieves result | Yes (indefinitely) | No (unchecked) |
| `getNow(defaultValue)` | Returns result if done, else default | No | No |
| `resultNow()` | Returns result if done (Java 19+) | No | Yes (if not done) |
| `exceptionNow()` | Returns exception if failed (Java 19+) | No | Yes (if not failed) |
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");

// get — throws checked exceptions
try {
    String result = future.get();                          // blocks indefinitely
    String result2 = future.get(2, TimeUnit.SECONDS);     // blocks up to 2s
} catch (InterruptedException | ExecutionException | TimeoutException e) {
    e.printStackTrace();
}

// join — throws unchecked exceptions (preferred in streams)
String result = future.join();

// getNow — returns default if not done
String result3 = future.getNow("Default");

// resultNow (Java 19+)
String result4 = future.resultNow();   // throws if not yet completed

// exceptionNow (Java 19+)
Throwable ex = future.exceptionNow();  // throws if not failed
```

---

### 4. Status Check Methods

| Method | Description |
|--------|-------------|
| `isDone()` | Returns `true` if completed (success, failure, or cancel) |
| `isCompletedExceptionally()` | Returns `true` if completed with exception |
| `isCancelled()` | Returns `true` if cancelled |
| `state()` | Returns state: RUNNING, SUCCESS, FAILED, CANCELLED (Java 19+) |
| `getNumberOfDependents()` | Returns number of futures waiting on this |
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");

future.isDone();                    // true if completed
future.isCompletedExceptionally(); // true if failed
future.isCancelled();              // true if cancelled
future.state();                    // RUNNING / SUCCESS / FAILED / CANCELLED (Java 19+)
future.getNumberOfDependents();    // number of dependent futures
```

---

### 5. Transformation Methods — *Chaining with result*

| Method | Description | Async? |
|--------|-------------|--------|
| `thenApply(function)` | Transforms result | No |
| `thenApplyAsync(function)` | Transforms result asynchronously | Yes |
| `thenApplyAsync(function, executor)` | Transforms on custom executor | Yes |
| `thenAccept(consumer)` | Consumes result, returns `Void` | No |
| `thenAcceptAsync(consumer)` | Consumes result asynchronously | Yes |
| `thenRun(runnable)` | Runs after completion, ignores result | No |
| `thenRunAsync(runnable)` | Runs asynchronously after completion | Yes |
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "hello");

// thenApply — transform result
CompletableFuture<String> upper = future.thenApply(String::toUpperCase);
// "HELLO"

// thenApply chaining
CompletableFuture<Integer> length = future
    .thenApply(String::toUpperCase)
    .thenApply(String::length);
// 5

// thenAccept — consume result
future.thenAccept(result -> System.out.println("Result: " + result));

// thenRun — run after completion
future.thenRun(() -> System.out.println("Future completed!"));

// Async versions
future.thenApplyAsync(String::toUpperCase);
future.thenApplyAsync(String::toUpperCase, executor);
future.thenAcceptAsync(System.out::println);
future.thenRunAsync(() -> System.out.println("Done"));
```

---

### 6. Combining Methods — *Two futures together*

| Method | Description |
|--------|-------------|
| `thenCompose(function)` | Chains dependent futures (flatMap equivalent) |
| `thenComposeAsync(function)` | Chains dependent futures asynchronously |
| `thenCombine(other, biFunction)` | Combines results of two independent futures |
| `thenCombineAsync(other, biFunction)` | Combines asynchronously |
| `thenAcceptBoth(other, biConsumer)` | Consumes results of two futures |
| `thenAcceptBothAsync(other, biConsumer)` | Consumes results asynchronously |
| `runAfterBoth(other, runnable)` | Runs after both futures complete |
| `runAfterBothAsync(other, runnable)` | Runs asynchronously after both complete |
| `applyToEither(other, function)` | Applies function to whichever completes first |
| `applyToEitherAsync(other, function)` | Applies function asynchronously to first completed |
| `acceptEither(other, consumer)` | Consumes whichever result completes first |
| `runAfterEither(other, runnable)` | Runs after either future completes |
```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "World");

// thenCompose — dependent futures (avoid nesting)
CompletableFuture<String> composed = f1.thenCompose(result ->
    CompletableFuture.supplyAsync(() -> result + " World")
);
// "Hello World"

// thenCombine — combine two independent results
CompletableFuture<String> combined = f1.thenCombine(f2,
    (r1, r2) -> r1 + " " + r2
);
// "Hello World"

// thenAcceptBoth — consume both results
f1.thenAcceptBoth(f2, (r1, r2) ->
    System.out.println(r1 + " " + r2)
);

// runAfterBoth — run after both complete
f1.runAfterBoth(f2, () -> System.out.println("Both done!"));

// applyToEither — use whichever completes first
CompletableFuture<String> fastest = f1.applyToEither(f2, String::toUpperCase);

// acceptEither — consume first completed
f1.acceptEither(f2, result -> System.out.println("First: " + result));

// runAfterEither
f1.runAfterEither(f2, () -> System.out.println("One done!"));
```

---

### 7. Combining Multiple Futures

| Method | Description |
|--------|-------------|
| `CompletableFuture.allOf(futures...)` | Waits for ALL futures to complete |
| `CompletableFuture.anyOf(futures...)` | Waits for ANY future to complete |
```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Result 1");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "Result 2");
CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> "Result 3");

// allOf — wait for all to complete
CompletableFuture<Void> allDone = CompletableFuture.allOf(f1, f2, f3);
allDone.thenRun(() -> {
    System.out.println(f1.join()); // "Result 1"
    System.out.println(f2.join()); // "Result 2"
    System.out.println(f3.join()); // "Result 3"
});

// allOf — collect all results into a list
List<CompletableFuture<String>> futures = List.of(f1, f2, f3);
CompletableFuture<List<String>> allResults = CompletableFuture
    .allOf(futures.toArray(new CompletableFuture[0]))
    .thenApply(v -> futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList())
    );
allResults.join(); // ["Result 1", "Result 2", "Result 3"]

// anyOf — get first completed result
CompletableFuture<Object> anyDone = CompletableFuture.anyOf(f1, f2, f3);
anyDone.thenAccept(result -> System.out.println("First: " + result));
```

---

### 8. Exception Handling Methods

| Method | Description |
|--------|-------------|
| `exceptionally(function)` | Handles exception, returns fallback value |
| `exceptionallyAsync(function)` | Handles exception asynchronously (Java 12+) |
| `handle(biFunction)` | Handles both result and exception |
| `handleAsync(biFunction)` | Handles both asynchronously |
| `whenComplete(biConsumer)` | Runs on both success and failure, does not change result |
| `whenCompleteAsync(biConsumer)` | Runs asynchronously on both success and failure |
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    if (true) throw new RuntimeException("Something went wrong!");
    return "Hello";
});

// exceptionally — handle exception with fallback
CompletableFuture<String> handled = future.exceptionally(ex -> {
    System.out.println("Error: " + ex.getMessage());
    return "Fallback Value";
});
handled.join(); // "Fallback Value"

// handle — handle both result and exception
CompletableFuture<String> result = future.handle((res, ex) -> {
    if (ex != null) {
        return "Error: " + ex.getMessage();
    }
    return res.toUpperCase();
});

// whenComplete — observe without changing result
future.whenComplete((res, ex) -> {
    if (ex != null) {
        System.out.println("Failed: " + ex.getMessage());
    } else {
        System.out.println("Success: " + res);
    }
});

// Async versions
future.exceptionallyAsync(ex -> "Async Fallback");
future.handleAsync((res, ex) -> ex != null ? "Error" : res);
future.whenCompleteAsync((res, ex) -> System.out.println("Done"));
```

---

### 9. Timeout Methods (Java 9+)

| Method | Description |
|--------|-------------|
| `orTimeout(timeout, unit)` | Completes exceptionally if timeout exceeded |
| `completeOnTimeout(value, timeout, unit)` | Completes with default value if timeout exceeded |
```java
// orTimeout — fail with TimeoutException if not done in time
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> {
        Thread.sleep(3000);
        return "Slow Result";
    })
    .orTimeout(1, TimeUnit.SECONDS); // throws TimeoutException after 1s

// completeOnTimeout — return default value if not done in time
CompletableFuture<String> future2 = CompletableFuture
    .supplyAsync(() -> {
        Thread.sleep(3000);
        return "Slow Result";
    })
    .completeOnTimeout("Default Value", 1, TimeUnit.SECONDS); // "Default Value"
```

---

### 10. Common Patterns

#### Sequential Async Tasks
```java
CompletableFuture<String> pipeline = CompletableFuture
    .supplyAsync(() -> "user123")                           // fetch user id
    .thenApplyAsync(id -> fetchUserFromDB(id))              // fetch user
    .thenApplyAsync(user -> fetchOrdersForUser(user))       // fetch orders
    .thenApplyAsync(orders -> generateReport(orders));      // generate report
```

#### Parallel Tasks with allOf
```java
CompletableFuture<String> userFuture   = CompletableFuture.supplyAsync(() -> fetchUser());
CompletableFuture<String> orderFuture  = CompletableFuture.supplyAsync(() -> fetchOrders());
CompletableFuture<String> configFuture = CompletableFuture.supplyAsync(() -> fetchConfig());

CompletableFuture.allOf(userFuture, orderFuture, configFuture)
    .thenRun(() -> {
        String user   = userFuture.join();
        String orders = orderFuture.join();
        String config = configFuture.join();
        System.out.println("All data fetched: " + user + orders + config);
    })
    .join();
```

#### With Exception Handling
```java
CompletableFuture<String> safe = CompletableFuture
    .supplyAsync(() -> riskyOperation())
    .exceptionally(ex -> "Fallback")
    .thenApply(String::toUpperCase)
    .orTimeout(5, TimeUnit.SECONDS);
```

---

### 11. CompletableFuture vs Future

| Feature | `Future` | `CompletableFuture` |
|---------|---------|---------------------|
| Manual completion | No | Yes |
| Chaining | No | Yes |
| Combining | No | Yes |
| Exception handling | No | Yes |
| Callbacks | No | Yes |
| Non-blocking | No | Yes |
| Cancellation | Yes | Yes |
| Async creation | No | Yes |

---

### Quick Reference — Method Groups

| Group | Methods |
|-------|---------|
| **Create** | `supplyAsync`, `runAsync`, `completedFuture`, `failedFuture` |
| **Transform** | `thenApply`, `thenAccept`, `thenRun` |
| **Compose** | `thenCompose`, `thenCombine`, `thenAcceptBoth` |
| **Either** | `applyToEither`, `acceptEither`, `runAfterEither` |
| **Multi** | `allOf`, `anyOf` |
| **Errors** | `exceptionally`, `handle`, `whenComplete` |
| **Timeout** | `orTimeout`, `completeOnTimeout` |
| **Retrieve** | `get`, `join`, `getNow` |
| **Complete** | `complete`, `completeExceptionally`, `cancel` |

> **Best Practices:**
> - Prefer `supplyAsync()` over `runAsync()` when a result is needed.
> - Prefer `join()` over `get()` inside stream pipelines to avoid checked exceptions.
> - Always handle exceptions with `exceptionally()` or `handle()` to avoid silent failures.
> - Use custom `ExecutorService` instead of the default `ForkJoinPool` for blocking tasks.
> - Use `orTimeout()` to prevent futures from hanging indefinitely.
> - Use `thenCompose()` instead of nested `thenApply()` to avoid `CompletableFuture<CompletableFuture<T>>`.
> - Always shut down custom `ExecutorService` after use.