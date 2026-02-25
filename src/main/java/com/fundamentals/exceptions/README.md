# Exceptions in Java — Important Concepts

## Overview

An **exception** is an event that disrupts the normal flow of a program during execution. In Java, exceptions are objects that represent errors or unexpected conditions. Java provides a robust, object-oriented mechanism to **detect, handle, and recover** from such conditions gracefully.

**Without Exception Handling:**
```java
int[] arr = new int[5];
arr[10] = 1; // ArrayIndexOutOfBoundsException — program crashes
```

**With Exception Handling:**
```java
try {
    int[] arr = new int[5];
    arr[10] = 1;
} catch (ArrayIndexOutOfBoundsException e) {
    System.out.println("Index out of range: " + e.getMessage());
} finally {
    System.out.println("This always runs.");
}
```

---

## 1. Exception Hierarchy

All exceptions and errors in Java are subclasses of `Throwable`.

```
Throwable
├── Error                          (unchecked — JVM-level, unrecoverable)
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── VirtualMachineError
│
└── Exception
    ├── IOException                (checked)
    │   ├── FileNotFoundException
    │   └── EOFException
    ├── SQLException               (checked)
    ├── ClassNotFoundException     (checked)
    ├── CloneNotSupportedException (checked)
    │
    └── RuntimeException           (unchecked)
        ├── NullPointerException
        ├── ArrayIndexOutOfBoundsException
        ├── ClassCastException
        ├── ArithmeticException
        ├── NumberFormatException
        ├── IllegalArgumentException
        ├── IllegalStateException
        ├── UnsupportedOperationException
        └── ConcurrentModificationException
```

---

## 2. Types of Exceptions

### 2.1 Checked Exceptions
- Subclasses of `Exception` (excluding `RuntimeException`)
- **Must** be handled or declared using `throws`
- Checked at **compile time**
- Represent recoverable conditions — e.g., file not found, network issue

```java
// Must be caught or declared
public void readFile(String path) throws IOException {
    FileReader fr = new FileReader(path); // FileNotFoundException (checked)
    BufferedReader br = new BufferedReader(fr);
    System.out.println(br.readLine());
    br.close();
}

// Calling method must handle it
try {
    readFile("data.txt");
} catch (IOException e) {
    System.out.println("File error: " + e.getMessage());
}
```

### 2.2 Unchecked Exceptions (Runtime Exceptions)
- Subclasses of `RuntimeException`
- **Not required** to be caught or declared
- Checked at **runtime**
- Usually indicate programming bugs — e.g., null pointer, array index out of bounds

```java
String str = null;
System.out.println(str.length()); // NullPointerException at runtime

int[] arr = {1, 2, 3};
System.out.println(arr[5]);       // ArrayIndexOutOfBoundsException at runtime

int result = 10 / 0;              // ArithmeticException at runtime
```

### 2.3 Errors
- Subclasses of `Error`
- Indicate serious, **unrecoverable** JVM-level problems
- Should **not** be caught or handled in application code

```java
// StackOverflowError — infinite recursion
public void recurse() {
    recurse(); // stack fills up and crashes
}

// OutOfMemoryError — heap exhausted
int[] massive = new int[Integer.MAX_VALUE];
```

### Comparison Table

| Feature | Checked Exception | Unchecked Exception | Error |
|---|---|---|---|
| Superclass | `Exception` | `RuntimeException` | `Error` |
| Compile-time check | Yes | No | No |
| Must handle/declare | Yes | No | No |
| Recoverable | Usually yes | Usually no | No |
| Cause | External conditions | Programming bugs | JVM/system failure |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `ArithmeticException` | `OutOfMemoryError`, `StackOverflowError` |

---

## 3. try-catch-finally Block

### Basic Syntax
```java
try {
    // Code that might throw an exception
} catch (ExceptionType1 e) {
    // Handle ExceptionType1
} catch (ExceptionType2 e) {
    // Handle ExceptionType2
} finally {
    // Always executes — cleanup code (optional)
}
```

### Example
```java
public static int divide(int a, int b) {
    try {
        int result = a / b;
        System.out.println("Result: " + result);
        return result;
    } catch (ArithmeticException e) {
        System.out.println("Cannot divide by zero: " + e.getMessage());
        return -1;
    } finally {
        System.out.println("divide() method finished.");
    }
}

divide(10, 2);  // Result: 5 → divide() method finished.
divide(10, 0);  // Cannot divide by zero: / by zero → divide() method finished.
```

### Multi-catch (Java 7+)
Catch multiple exception types in a single `catch` block using `|`.

```java
try {
    String s = null;
    int[] arr = new int[3];

    if (Math.random() > 0.5) {
        System.out.println(s.length());   // NullPointerException
    } else {
        System.out.println(arr[10]);      // ArrayIndexOutOfBoundsException
    }
} catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
    System.out.println("Caught: " + e.getClass().getSimpleName());
}
```

### Nested try-catch
```java
try {
    try {
        int result = 10 / 0;
    } catch (ArithmeticException e) {
        System.out.println("Inner catch: " + e.getMessage());
        throw new RuntimeException("Wrapped exception", e); // re-throw
    }
} catch (RuntimeException e) {
    System.out.println("Outer catch: " + e.getMessage());
}
```

### finally Block Rules
- `finally` always executes — even if a `return` statement is in `try` or `catch`
- `finally` does **not** execute only when `System.exit()` is called or the JVM crashes
- If both `try`/`catch` and `finally` have `return`, the `finally` return wins

```java
public static int test() {
    try {
        return 1;
    } finally {
        return 2; // this return wins — method returns 2
    }
}
System.out.println(test()); // 2
```

---

## 4. try-with-resources (Java 7+)

Automatically closes resources that implement `AutoCloseable` or `Closeable`. Eliminates the need for explicit `close()` in `finally`.

```java
// Old way — verbose and error-prone
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("file.txt"));
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (br != null) {
        try { br.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}

// New way — try-with-resources (Java 7+)
try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
} // br.close() called automatically
```

### Multiple Resources
```java
try (
    FileInputStream  fis = new FileInputStream("input.txt");
    FileOutputStream fos = new FileOutputStream("output.txt")
) {
    int data;
    while ((data = fis.read()) != -1) {
        fos.write(data);
    }
} catch (IOException e) {
    e.printStackTrace();
}
// Both fis and fos are closed automatically (in reverse order: fos first, then fis)
```

### Custom AutoCloseable Resource
```java
public class DatabaseConnection implements AutoCloseable {
    public DatabaseConnection() {
        System.out.println("Connection opened.");
    }

    public void query(String sql) {
        System.out.println("Executing: " + sql);
    }

    @Override
    public void close() {
        System.out.println("Connection closed.");
    }
}

// Usage
try (DatabaseConnection conn = new DatabaseConnection()) {
    conn.query("SELECT * FROM users");
} // close() called automatically
// Output:
// Connection opened.
// Executing: SELECT * FROM users
// Connection closed.
```

---

## 5. throw and throws

### `throw` — Explicitly Throwing an Exception
Used to **manually throw** an exception from within a method.

```java
public static void validateAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative: " + age);
    }
    if (age > 150) {
        throw new IllegalArgumentException("Age seems unrealistic: " + age);
    }
    System.out.println("Valid age: " + age);
}

validateAge(25);   // Valid age: 25
validateAge(-5);   // IllegalArgumentException: Age cannot be negative: -5
```

### `throws` — Declaring Checked Exceptions
Used in a **method signature** to declare that the method may throw a checked exception. Callers must handle or re-declare it.

```java
public void loadConfig(String path) throws IOException, ClassNotFoundException {
    // may throw IOException or ClassNotFoundException
    FileInputStream fis = new FileInputStream(path);
    ObjectInputStream ois = new ObjectInputStream(fis);
    ois.readObject();
    ois.close();
}

// Caller must handle or propagate
try {
    loadConfig("config.ser");
} catch (IOException | ClassNotFoundException e) {
    System.out.println("Failed to load: " + e.getMessage());
}
```

### `throw` vs `throws`

| | `throw` | `throws` |
|---|---|---|
| Purpose | Actually throws an exception | Declares possible exceptions |
| Location | Inside method body | In method signature |
| Followed by | An exception instance | Exception class name(s) |
| Used with | Checked + Unchecked | Mostly checked exceptions |
| Example | `throw new IOException()` | `void m() throws IOException` |

---

## 6. Custom Exceptions

You can create your own exceptions by extending `Exception` (checked) or `RuntimeException` (unchecked).

### Custom Checked Exception
```java
public class InsufficientFundsException extends Exception {
    private double amount;

    public InsufficientFundsException(double amount) {
        super("Insufficient funds. Required: " + amount);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
```

### Custom Unchecked Exception
```java
public class InvalidUserException extends RuntimeException {
    private String userId;

    public InvalidUserException(String userId) {
        super("Invalid user ID: " + userId);
        this.userId = userId;
    }

    public InvalidUserException(String userId, Throwable cause) {
        super("Invalid user ID: " + userId, cause);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
```

### Using Custom Exceptions
```java
public class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException(amount);
        }
        balance -= amount;
        System.out.println("Withdrawn: " + amount + " | Balance: " + balance);
    }
}

// Usage
BankAccount account = new BankAccount(500.0);
try {
    account.withdraw(200.0); // Withdrawn: 200.0 | Balance: 300.0
    account.withdraw(400.0); // throws InsufficientFundsException
} catch (InsufficientFundsException e) {
    System.out.println(e.getMessage());        // Insufficient funds. Required: 400.0
    System.out.println("Amount: " + e.getAmount()); // Amount: 400.0
}
```

---

## 7. Exception Chaining

Exception chaining (also called wrapping) preserves the original cause when re-throwing.

```java
public void readData(String path) throws DataProcessingException {
    try {
        FileReader fr = new FileReader(path);
        // process...
    } catch (IOException e) {
        // Wrap low-level exception into a higher-level one
        throw new DataProcessingException("Failed to read data from: " + path, e);
    }
}

// Accessing the cause
try {
    readData("data.txt");
} catch (DataProcessingException e) {
    System.out.println("Error: " + e.getMessage());
    System.out.println("Cause: " + e.getCause().getMessage()); // original IOException
    e.printStackTrace(); // full chain printed
}
```

---

## 8. Important Methods of Throwable

All exceptions inherit these methods from `Throwable`:

| Method | Description |
|---|---|
| `getMessage()` | Returns the detail message string |
| `getLocalizedMessage()` | Returns localized detail message |
| `getCause()` | Returns the cause of this exception |
| `toString()` | Returns class name + message |
| `printStackTrace()` | Prints stack trace to stderr |
| `printStackTrace(PrintStream)` | Prints stack trace to given stream |
| `getStackTrace()` | Returns array of `StackTraceElement` |
| `initCause(Throwable)` | Sets the cause |
| `getSuppressed()` | Returns suppressed exceptions (try-with-resources) |
| `addSuppressed(Throwable)` | Adds a suppressed exception |

```java
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println(e.getMessage());         // / by zero
    System.out.println(e.toString());           // java.lang.ArithmeticException: / by zero
    System.out.println(e.getClass().getName()); // java.lang.ArithmeticException

    StackTraceElement[] trace = e.getStackTrace();
    System.out.println(trace[0]);               // file and line where error occurred
}
```

---

## 9. Common Built-in Exceptions

### RuntimeExceptions (Unchecked)

| Exception | Cause |
|---|---|
| `NullPointerException` | Accessing method/field on a `null` reference |
| `ArrayIndexOutOfBoundsException` | Accessing array with invalid index |
| `StringIndexOutOfBoundsException` | Accessing String with invalid index |
| `ClassCastException` | Invalid type cast |
| `ArithmeticException` | Math error e.g., division by zero |
| `NumberFormatException` | Invalid string-to-number conversion |
| `IllegalArgumentException` | Method receives an illegal argument |
| `IllegalStateException` | Method called at wrong time |
| `UnsupportedOperationException` | Operation not supported |
| `StackOverflowError` | Infinite or deep recursion |
| `ConcurrentModificationException` | Collection modified during iteration |
| `NegativeArraySizeException` | Array created with negative size |

### Checked Exceptions

| Exception | Cause |
|---|---|
| `IOException` | General I/O failure |
| `FileNotFoundException` | File does not exist |
| `EOFException` | End of file reached unexpectedly |
| `SQLException` | Database access error |
| `ClassNotFoundException` | Class not found at runtime |
| `InterruptedException` | Thread interrupted while waiting |
| `ParseException` | Error while parsing a string |
| `MalformedURLException` | Malformed URL string |

---

## 10. Best Practices

### 1. Catch Specific Exceptions First
```java
// Correct — specific before general
try {
    // risky code
} catch (FileNotFoundException e) {
    System.out.println("File not found.");
} catch (IOException e) {
    System.out.println("I/O error.");
} catch (Exception e) {
    System.out.println("General error.");
}

// Wrong — unreachable catch blocks
try {
    // risky code
} catch (Exception e) {         // catches everything
} catch (FileNotFoundException e) { // COMPILE ERROR — unreachable
}
```

### 2. Never Swallow Exceptions
```java
// Bad — silently ignoring exception
try {
    loadFile("config.txt");
} catch (IOException e) {
    // do nothing — dangerous!
}

// Good — at minimum log it
try {
    loadFile("config.txt");
} catch (IOException e) {
    logger.error("Failed to load config: ", e);
    throw new RuntimeException("Configuration error", e);
}
```

### 3. Use finally or try-with-resources for Cleanup
```java
// Preferred — try-with-resources
try (Connection conn = getConnection()) {
    // use conn
} catch (SQLException e) {
    e.printStackTrace();
}
```

### 4. Provide Meaningful Messages
```java
// Bad
throw new IllegalArgumentException("invalid");

// Good
throw new IllegalArgumentException(
    "Username must be 3-20 characters. Received: '" + username + "' (length=" + username.length() + ")"
);
```

### 5. Don't Use Exceptions for Flow Control
```java
// Bad — using exception for normal flow
try {
    int value = Integer.parseInt(input);
} catch (NumberFormatException e) {
    // treating exceptions as if/else
}

// Good — validate first
if (input.matches("\\d+")) {
    int value = Integer.parseInt(input);
} else {
    System.out.println("Not a number.");
}
```

### 6. Prefer Unchecked Exceptions for Programming Errors
```java
// IllegalArgumentException for invalid arguments
public void setAge(int age) {
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("Invalid age: " + age);
    }
    this.age = age;
}

// IllegalStateException for invalid state
public void start() {
    if (isRunning) {
        throw new IllegalStateException("Service is already running.");
    }
    isRunning = true;
}
```

### 7. Always Log the Cause When Wrapping
```java
// Bad — cause is lost
catch (IOException e) {
    throw new ServiceException("Failed");
}

// Good — cause is preserved
catch (IOException e) {
    throw new ServiceException("Failed to process file", e); // pass cause
}
```

### 8. Avoid Catching Throwable or Error
```java
// Bad — catches JVM errors too
catch (Throwable t) { }

// Bad — catches OutOfMemoryError, StackOverflowError, etc.
catch (Error e) { }

// Good — catch specific exceptions
catch (Exception e) { }
```

---

## 11. Exception Propagation

When an exception is thrown and not caught in a method, it **propagates** up the call stack until it is caught or reaches the JVM (which terminates the thread).

```java
public static void methodC() {
    int result = 10 / 0; // throws ArithmeticException
}

public static void methodB() {
    methodC(); // not caught — propagates upward
}

public static void methodA() {
    methodB(); // not caught — propagates upward
}

public static void main(String[] args) {
    try {
        methodA();
    } catch (ArithmeticException e) {
        System.out.println("Caught in main: " + e.getMessage()); // / by zero
    }
}
```

---

## 12. Suppressed Exceptions (Java 7+)

In try-with-resources, if both the `try` block and `close()` throw exceptions, the `close()` exception is **suppressed** (not lost) and attached to the primary exception.

```java
public class FaultyResource implements AutoCloseable {
    public void use() throws Exception {
        throw new Exception("Exception from use()");
    }

    @Override
    public void close() throws Exception {
        throw new Exception("Exception from close()");
    }
}

try (FaultyResource r = new FaultyResource()) {
    r.use();
} catch (Exception e) {
    System.out.println("Primary: " + e.getMessage()); // Exception from use()
    for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("Suppressed: " + suppressed.getMessage()); // Exception from close()
    }
}
```

---

## 13. Re-throwing Exceptions

### Re-throw as-is
```java
try {
    riskyOperation();
} catch (IOException e) {
    logger.error("Error occurred", e);
    throw e; // re-throw the same exception
}
```

### Re-throw as a different type
```java
try {
    riskyOperation();
} catch (IOException e) {
    throw new ServiceException("Service unavailable", e);
}
```

### Re-throw in Java 7+ (precise rethrow)
```java
// Java 7+ can infer the specific checked exception type being rethrown
public void method() throws FileNotFoundException, EOFException {
    try {
        // code that throws FileNotFoundException or EOFException
    } catch (IOException e) {
        throw e; // compiler knows it can only be FileNotFoundException or EOFException
    }
}
```

---

## Quick Reference Summary

```
Throwable
├── Error        → JVM-level, don't catch
└── Exception
    ├── Checked  → must handle (IOException, SQLException, etc.)
    └── Unchecked (RuntimeException) → optional (NPE, ArrayIndexOutOfBounds, etc.)
```

| Keyword | Purpose |
|---|---|
| `try` | Wraps risky code |
| `catch` | Handles a specific exception |
| `finally` | Always runs — used for cleanup |
| `throw` | Explicitly throws an exception instance |
| `throws` | Declares exceptions a method may throw |

| Best Practice | Rule |
|---|---|
| Specific before general | Catch `FileNotFoundException` before `IOException` |
| Never swallow | Always log or handle exceptions |
| Use try-with-resources | For anything that implements `AutoCloseable` |
| Preserve cause | Pass original exception when wrapping |
| Meaningful messages | Include context in exception messages |
| Avoid flow control | Don't use exceptions as `if/else` substitutes |
| Prefer unchecked | For programming errors and invalid arguments |