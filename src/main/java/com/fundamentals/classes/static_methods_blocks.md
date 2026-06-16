# Static Methods and Static Blocks in Java

## Table of Contents
- [Introduction](#introduction)
- [Static Methods](#static-methods)
    - [What is a Static Method?](#what-is-a-static-method)
    - [Syntax](#syntax-static-method)
    - [Key Characteristics](#key-characteristics-of-static-methods)
    - [Examples](#examples-static-methods)
    - [When to Use Static Methods](#when-to-use-static-methods)
    - [Common Pitfalls](#common-pitfalls-static-methods)
- [Static Blocks](#static-blocks)
    - [What is a Static Block?](#what-is-a-static-block)
    - [Syntax](#syntax-static-block)
    - [Key Characteristics](#key-characteristics-of-static-blocks)
    - [Examples](#examples-static-blocks)
    - [Execution Order](#execution-order)
    - [When to Use Static Blocks](#when-to-use-static-blocks)
    - [Common Pitfalls](#common-pitfalls-static-blocks)
- [Static Methods vs Instance Methods](#static-methods-vs-instance-methods)
- [Summary](#summary)

---

## Introduction

In Java, the `static` keyword is used to define class-level members — things that belong to the **class itself** rather than to any particular instance (object) of the class. Two of the most important uses of `static` are **static methods** and **static blocks**.

Understanding these concepts is critical for writing clean, efficient, and well-structured Java code.

---

## Static Methods

### What is a Static Method?

A **static method** belongs to the class rather than to any object of the class. It can be called directly on the class without creating an instance.

### Syntax (Static Method)

```java
public class MyClass {

    public static returnType methodName(parameters) {
        // method body
    }
}
```

Calling a static method:

```java
MyClass.methodName(arguments);
```

### Key Characteristics of Static Methods

| Characteristic | Detail |
|---|---|
| **Belongs to** | The class, not an instance |
| **Called via** | `ClassName.methodName()` |
| **Can access** | Only static fields and other static methods directly |
| **Cannot use** | `this` or `super` keywords |
| **Overriding** | Cannot be overridden (but can be hidden) |
| **Memory** | Loaded once when the class is loaded |

### Examples (Static Methods)

#### Example 1 — Basic Utility Method

```java
public class MathUtils {

    // Static method: no instance needed
    public static int add(int a, int b) {
        return a + b;
    }

    public static double circleArea(double radius) {
        return Math.PI * radius * radius;
    }
}

// Usage — called directly on the class
public class Main {
    public static void main(String[] args) {
        int sum = MathUtils.add(5, 10);           // 15
        double area = MathUtils.circleArea(7.0);  // 153.94...

        System.out.println("Sum: " + sum);
        System.out.println("Area: " + area);
    }
}
```

#### Example 2 — Static Counter (Accessing Static Fields)

```java
public class Employee {

    private static int employeeCount = 0; // static field
    private String name;

    public Employee(String name) {
        this.name = name;
        employeeCount++; // static method/constructor can modify static fields
    }

    // Static method accessing a static field
    public static int getEmployeeCount() {
        return employeeCount;
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        new Employee("Alice");
        new Employee("Bob");
        new Employee("Charlie");

        System.out.println("Total Employees: " + Employee.getEmployeeCount()); // 3
    }
}
```

#### Example 3 — Factory Method Pattern (Common Use Case)

```java
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private String url;

    // Private constructor
    private DatabaseConnection(String url) {
        this.url = url;
    }

    // Static factory method — controls instance creation
    public static DatabaseConnection getInstance(String url) {
        if (instance == null) {
            instance = new DatabaseConnection(url);
        }
        return instance;
    }

    public String getUrl() {
        return url;
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        DatabaseConnection conn = DatabaseConnection.getInstance("jdbc:mysql://localhost:3306/mydb");
        System.out.println("Connected to: " + conn.getUrl());
    }
}
```

#### Example 4 — What Static Methods CANNOT Do

```java
public class Counter {

    private int count = 0;        // instance field
    private static int total = 0; // static field

    public static void invalidMethod() {
        // count++;   // ❌ ERROR: Cannot access instance field from static context
        // this.count = 1; // ❌ ERROR: Cannot use 'this' in static context
        total++;       // ✅ OK: Can access static field
    }
}
```

### When to Use Static Methods

- **Utility/helper methods** that don't depend on object state (e.g., `Math.abs()`, `Collections.sort()`)
- **Factory methods** that control object creation
- **Operations on static fields** (e.g., getting a counter or singleton)
- When the logic is **purely functional** and requires no instance data

### Common Pitfalls (Static Methods)

```java
public class Animal {
    public static void makeSound() {
        System.out.println("Generic animal sound");
    }
}

public class Dog extends Animal {
    // This HIDES the parent method, does NOT override it
    public static void makeSound() {
        System.out.println("Woof!");
    }
}

public class Main {
    public static void main(String[] args) {
        Animal a = new Dog();
        a.makeSound(); // Prints: "Generic animal sound" — NOT "Woof!"
                       // Static methods are resolved at compile time (no polymorphism)
    }
}
```

> ⚠️ **Pitfall:** Static methods do not support **runtime polymorphism**. They are resolved based on the reference type, not the actual object type.

---

## Static Blocks

### What is a Static Block?

A **static block** (also called a *static initializer*) is a block of code inside a class that runs **once** when the class is first loaded into the JVM. It is used to initialize static fields that require complex logic.

### Syntax (Static Block)

```java
public class MyClass {

    static {
        // Initialization code here
        // Runs once when the class is loaded
    }
}
```

### Key Characteristics of Static Blocks

| Characteristic | Detail |
|---|---|
| **Runs** | Once, when the class is first loaded by the JVM |
| **Purpose** | Initialize static fields with complex logic |
| **Can access** | Only static members of the class |
| **Cannot throw** | Checked exceptions (unless caught within the block) |
| **Multiple blocks** | A class can have multiple static blocks; they run in order |
| **Cannot use** | `this` or `super` |

### Examples (Static Blocks)

#### Example 1 — Basic Static Initialization

```java
public class Config {

    public static final String DB_URL;
    public static final int MAX_CONNECTIONS;

    // Static block runs once when Config class is loaded
    static {
        System.out.println("Config class is being loaded...");
        DB_URL = "jdbc:mysql://localhost:3306/appdb";
        MAX_CONNECTIONS = 10;
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("DB URL: " + Config.DB_URL);
        System.out.println("Max Connections: " + Config.MAX_CONNECTIONS);
    }
}

// Output:
// Config class is being loaded...
// DB URL: jdbc:mysql://localhost:3306/appdb
// Max Connections: 10
```

#### Example 2 — Loading a Driver or External Resource

```java
public class DatabaseManager {

    static {
        try {
            // Load the JDBC driver once when the class is initialized
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load JDBC driver.", e);
        }
    }

    public static void connect() {
        System.out.println("Connecting to database...");
    }
}
```

#### Example 3 — Populating a Static Collection

```java
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CountryCodes {

    public static final Map<String, String> CODES;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("IN", "India");
        map.put("US", "United States");
        map.put("GB", "United Kingdom");
        map.put("DE", "Germany");
        CODES = Collections.unmodifiableMap(map); // make it immutable
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(CountryCodes.CODES.get("IN")); // India
        System.out.println(CountryCodes.CODES.get("US")); // United States
    }
}
```

#### Example 4 — Multiple Static Blocks (Execution Order)

```java
public class MultiBlock {

    static int value;

    static {
        value = 10;
        System.out.println("First static block. value = " + value);
    }

    static {
        value = value * 2;
        System.out.println("Second static block. value = " + value);
    }

    static {
        value += 5;
        System.out.println("Third static block. value = " + value);
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Final value: " + MultiBlock.value);
    }
}

// Output:
// First static block. value = 10
// Second static block. value = 20
// Third static block. value = 25
// Final value: 25
```

### Execution Order

The full class loading sequence in Java follows this order:

```
1. Static fields (in declaration order)
2. Static blocks (in top-to-bottom order)
3. Instance fields (when an object is created)
4. Instance initializer blocks (when an object is created)
5. Constructor (when an object is created)
```

```java
public class InitOrder {

    static int staticField = initStatic(); // 1st

    static {
        System.out.println("2. Static block"); // 2nd
    }

    int instanceField = initInstance(); // 3rd (on object creation)

    {
        System.out.println("4. Instance initializer block"); // 4th
    }

    InitOrder() {
        System.out.println("5. Constructor"); // 5th
    }

    static int initStatic() {
        System.out.println("1. Static field initialized");
        return 1;
    }

    int initInstance() {
        System.out.println("3. Instance field initialized");
        return 2;
    }
}
```

### When to Use Static Blocks

- Initializing **complex static fields** that require multi-line logic
- **Loading external resources** (JDBC drivers, native libraries, config files)
- **Populating static collections** (maps, lists, sets)
- Running **one-time setup code** that must complete before the class is used

### Common Pitfalls (Static Blocks)

```java
public class BadExample {

    static {
        // ❌ Checked exceptions must be caught inside the static block
        // They cannot be propagated out
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted during static init: " + e.getMessage());
        }
    }
}
```

> ⚠️ **Pitfall:** If a static block throws an **uncaught exception**, the class will fail to load and a `ExceptionInInitializerError` is thrown — making the class permanently unusable in that JVM session.

---

## Static Methods vs Instance Methods

| Feature | Static Method | Instance Method |
|---|---|---|
| **Belongs to** | Class | Object (instance) |
| **Called via** | `ClassName.method()` | `object.method()` |
| **Accesses** | Only static members | Static + instance members |
| **`this` keyword** | ❌ Not available | ✅ Available |
| **Polymorphism** | ❌ No (compile-time) | ✅ Yes (runtime) |
| **Memory** | Shared across all instances | Per instance |
| **Use case** | Utility, factory, shared logic | Object-specific behavior |

---

## Summary

| Concept | Key Point |
|---|---|
| **Static Method** | Belongs to the class; called without an object; cannot access instance members |
| **Static Block** | Runs once on class load; used for complex static field initialization |
| **Both** | Cannot use `this` or `super`; can only access static members directly |
| **Static Block Order** | Multiple blocks execute top-to-bottom, before any object is created |
| **Static Method Caution** | No runtime polymorphism — method resolution is compile-time |

> 💡 **Best Practice:** Use static methods for stateless utility logic. Use static blocks sparingly — only when static field initialization genuinely requires complex setup that can't be done inline.