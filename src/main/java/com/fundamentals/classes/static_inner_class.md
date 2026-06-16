# Static Inner Classes in Java

## Table of Contents
- [Introduction](#introduction)
- [Types of Nested Classes in Java](#types-of-nested-classes-in-java)
- [What is a Static Inner Class?](#what-is-a-static-inner-class)
- [Syntax](#syntax)
- [Key Characteristics](#key-characteristics)
- [Instantiating a Static Inner Class](#instantiating-a-static-inner-class)
- [Access Rules](#access-rules)
    - [What a Static Inner Class Can Access](#what-a-static-inner-class-can-access)
    - [What a Static Inner Class Cannot Access](#what-a-static-inner-class-cannot-access)
- [Examples](#examples)
    - [Example 1 — Basic Static Inner Class](#example-1--basic-static-inner-class)
    - [Example 2 — Accessing Outer Class Static Members](#example-2--accessing-outer-class-static-members)
    - [Example 3 — Builder Pattern](#example-3--builder-pattern)
    - [Example 4 — Node in a Linked List](#example-4--node-in-a-linked-list)
    - [Example 5 — Static Inner Class as a Key/Value Pair](#example-5--static-inner-class-as-a-keyvalue-pair)
    - [Example 6 — Accessing Private Members of the Outer Class](#example-6--accessing-private-members-of-the-outer-class)
- [Static Inner Class vs Non-Static Inner Class](#static-inner-class-vs-non-static-inner-class)
- [Static Inner Class vs Top-Level Class](#static-inner-class-vs-top-level-class)
- [Common Use Cases](#common-use-cases)
- [Common Pitfalls](#common-pitfalls)
- [Summary](#summary)

---

## Introduction

Java allows you to define a class **inside another class**. These are called **nested classes**. When a nested class is declared with the `static` keyword, it is called a **static nested class** — commonly referred to as a **static inner class**.

A static inner class is logically grouped with its outer class but does **not** require an instance of the outer class to be instantiated. This makes it a powerful tool for clean code organization, encapsulation, and well-known design patterns like the **Builder Pattern**.

---

## Types of Nested Classes in Java

Before diving deep, it helps to understand where static inner classes fit in Java's nested class hierarchy:

```
Nested Classes
├── Static Nested Class      ← focus of this document
└── Inner Classes (non-static)
    ├── Regular Inner Class
    ├── Local Inner Class    (defined inside a method)
    └── Anonymous Inner Class
```

| Type | Declared with `static`? | Needs outer class instance? |
|---|---|---|
| Static Nested Class | ✅ Yes | ❌ No |
| Regular Inner Class | ❌ No | ✅ Yes |
| Local Inner Class | ❌ No | ✅ Yes (implicitly) |
| Anonymous Inner Class | ❌ No | ✅ Yes (implicitly) |

---

## What is a Static Inner Class?

A **static inner class** is a nested class declared with the `static` modifier. It is associated with the **outer class** at the class level — not with any specific instance of the outer class.

Think of it as a **regular top-level class** that happens to be scoped inside another class for organizational clarity.

---

## Syntax

```java
public class OuterClass {

    // Static inner class
    public static class StaticInnerClass {

        // Fields, methods, constructors — just like any regular class
        public void display() {
            System.out.println("Inside StaticInnerClass");
        }
    }
}
```

---

## Key Characteristics

| Characteristic | Detail |
|---|---|
| **Declared with** | `static` keyword inside an outer class |
| **Instance of outer class** | ❌ Not required |
| **Access to outer static members** | ✅ Yes (including `private` static members) |
| **Access to outer instance members** | ❌ No (without an explicit outer instance reference) |
| **Can have** | Its own fields, methods, constructors, and even its own nested classes |
| **Can be** | `public`, `protected`, `private`, or package-private |
| **Implements/extends** | ✅ Can implement interfaces or extend classes |
| **`this` keyword** | Refers to the static inner class instance, not the outer class |

---

## Instantiating a Static Inner Class

Since a static inner class does **not** need an instance of the outer class, you can create it directly:

```java
// From outside the outer class
OuterClass.StaticInnerClass obj = new OuterClass.StaticInnerClass();

// From inside the outer class
StaticInnerClass obj = new StaticInnerClass();
```

Compare this to a **non-static** inner class, which requires an outer class instance first:

```java
// Non-static inner class — requires outer instance
OuterClass outer = new OuterClass();
OuterClass.InnerClass inner = outer.new InnerClass(); // ← outer instance required
```

---

## Access Rules

### What a Static Inner Class CAN Access

```java
public class Outer {

    private static String staticPrivateField = "I am private static";
    public static int staticPublicField = 42;

    public static class Inner {

        public void show() {
            // ✅ Can access private static fields of the outer class
            System.out.println(staticPrivateField);

            // ✅ Can access public static fields of the outer class
            System.out.println(staticPublicField);
        }
    }
}
```

### What a Static Inner Class CANNOT Access

```java
public class Outer {

    private String instanceField = "I am an instance field";

    public static class Inner {

        public void show() {
            // ❌ ERROR: Cannot access instance field without an outer instance
            // System.out.println(instanceField);

            // ✅ Workaround: pass an outer instance explicitly
            Outer outerRef = new Outer();
            System.out.println(outerRef.instanceField); // This works
        }
    }
}
```

> 💡 **Rule of Thumb:** A static inner class can access **anything static** in the outer class (even `private`), but it needs an **explicit outer object reference** to access instance members.

---

## Examples

### Example 1 — Basic Static Inner Class

```java
public class Vehicle {

    private static String type = "Motorized";

    public static class Engine {

        private int horsepower;

        public Engine(int horsepower) {
            this.horsepower = horsepower;
        }

        public void describe() {
            // Accessing outer class's private static field
            System.out.println("Type: " + type);
            System.out.println("Horsepower: " + horsepower);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        // No Vehicle instance needed
        Vehicle.Engine engine = new Vehicle.Engine(250);
        engine.describe();
    }
}

// Output:
// Type: Motorized
// Horsepower: 250
```

---

### Example 2 — Accessing Outer Class Static Members

```java
public class University {

    private static String universityName = "State University";
    private static int totalStudents = 5000;

    public static class Department {

        private String deptName;

        public Department(String deptName) {
            this.deptName = deptName;
        }

        public void printInfo() {
            // Accessing private static members of the outer class
            System.out.println("University : " + universityName);
            System.out.println("Department : " + deptName);
            System.out.println("Total Students: " + totalStudents);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        University.Department cs = new University.Department("Computer Science");
        University.Department math = new University.Department("Mathematics");

        cs.printInfo();
        System.out.println("---");
        math.printInfo();
    }
}

// Output:
// University : State University
// Department : Computer Science
// Total Students: 5000
// ---
// University : State University
// Department : Mathematics
// Total Students: 5000
```

---

### Example 3 — Builder Pattern

The **Builder Pattern** is the most prevalent real-world use of static inner classes. It allows step-by-step construction of complex objects while keeping the class clean and immutable.

```java
public class Person {

    // All fields are final — immutable object
    private final String firstName;
    private final String lastName;
    private final int age;
    private final String email;
    private final String phone;

    // Private constructor — only the Builder can call it
    private Person(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName  = builder.lastName;
        this.age       = builder.age;
        this.email     = builder.email;
        this.phone     = builder.phone;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public int    getAge()       { return age; }
    public String getEmail()     { return email; }
    public String getPhone()     { return phone; }

    @Override
    public String toString() {
        return String.format("Person{name='%s %s', age=%d, email='%s', phone='%s'}",
                firstName, lastName, age, email, phone);
    }

    // ─── Static Inner Builder Class ───────────────────────────────────────────
    public static class Builder {

        // Required fields
        private final String firstName;
        private final String lastName;

        // Optional fields — defaults provided
        private int    age   = 0;
        private String email = "";
        private String phone = "";

        public Builder(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName  = lastName;
        }

        public Builder age(int age) {
            this.age = age;
            return this; // enables method chaining
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }
}

// Usage — clean, readable, no telescoping constructors
public class Main {
    public static void main(String[] args) {
        Person person = new Person.Builder("John", "Doe")
                .age(30)
                .email("john.doe@example.com")
                .phone("+91-9876543210")
                .build();

        System.out.println(person);
    }
}

// Output:
// Person{name='John Doe', age=30, email='john.doe@example.com', phone='+91-9876543210'}
```

---

### Example 4 — Node in a Linked List

Data structures often use static inner classes to represent their internal components. This is how Java's own `LinkedList` and `HashMap` are implemented internally.

```java
public class LinkedList<T> {

    // Static inner class — Node does not need a LinkedList instance to exist
    public static class Node<T> {
        T data;
        Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private int size;

    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    public void print() {
        Node<T> current = head;
        while (current != null) {
            System.out.print(current.data);
            if (current.next != null) System.out.print(" -> ");
            current = current.next;
        }
        System.out.println();
    }
}

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<>();
        list.add(10);
        list.add(20);
        list.add(30);
        list.print(); // 10 -> 20 -> 30

        // Node can also be used independently
        LinkedList.Node<String> node = new LinkedList.Node<>("standalone");
        System.out.println(node.data); // standalone
    }
}
```

---

### Example 5 — Static Inner Class as a Key/Value Pair

```java
import java.util.ArrayList;
import java.util.List;

public class Cache {

    // Static inner class acts as a self-contained data holder
    public static class Entry<K, V> {

        private final K key;
        private V value;

        public Entry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        public K getKey()   { return key; }
        public V getValue() { return value; }
        public void setValue(V value) { this.value = value; }

        @Override
        public String toString() {
            return key + " -> " + value;
        }
    }

    private final List<Entry<String, String>> entries = new ArrayList<>();

    public void put(String key, String value) {
        entries.add(new Entry<>(key, value));
    }

    public void printAll() {
        entries.forEach(System.out::println);
    }
}

public class Main {
    public static void main(String[] args) {
        Cache cache = new Cache();
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        cache.printAll();

        // Entry can also be used directly without a Cache instance
        Cache.Entry<String, Integer> entry = new Cache.Entry<>("score", 99);
        System.out.println(entry);
    }
}

// Output:
// user:1 -> Alice
// user:2 -> Bob
// score -> 99
```

---

### Example 6 — Accessing Private Members of the Outer Class

Static inner classes have special access to **private** static members of their enclosing outer class — something no other external class can do.

```java
public class BankAccount {

    private static double interestRate = 3.5; // private — hidden from the outside

    private String accountHolder;
    private double balance;

    public BankAccount(String accountHolder, double balance) {
        this.accountHolder = accountHolder;
        this.balance       = balance;
    }

    // Static inner class accesses private static field
    public static class InterestCalculator {

        public double calculate(double balance) {
            // ✅ Accessing private static field of outer class
            return balance * (interestRate / 100);
        }

        public static void printRate() {
            System.out.println("Current Interest Rate: " + interestRate + "%");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount.InterestCalculator calc = new BankAccount.InterestCalculator();

        double interest = calc.calculate(10000);
        System.out.println("Interest earned: ₹" + interest); // ₹350.0

        BankAccount.InterestCalculator.printRate(); // Current Interest Rate: 3.5%
    }
}
```

---

## Static Inner Class vs Non-Static Inner Class

| Feature | Static Inner Class | Non-Static Inner Class |
|---|---|---|
| **`static` keyword** | ✅ Required | ❌ Not used |
| **Outer instance required** | ❌ No | ✅ Yes |
| **Instantiation** | `new Outer.Inner()` | `outer.new Inner()` |
| **Access to outer static members** | ✅ Yes (even `private`) | ✅ Yes |
| **Access to outer instance members** | ❌ No (without ref) | ✅ Yes (directly) |
| **`this` keyword** | Refers to inner class | Can use `Outer.this` |
| **Memory** | No hidden outer reference | Holds hidden reference to outer instance |
| **Risk of memory leaks** | ❌ Lower | ⚠️ Higher (outer reference kept alive) |
| **Best for** | Logically related helpers, builders | Callbacks, event listeners, iterators |

---

## Static Inner Class vs Top-Level Class

| Feature | Static Inner Class | Top-Level Class |
|---|---|---|
| **Location** | Inside another class | Standalone `.java` file |
| **Visibility** | Can be `private` or `protected` | Only `public` or package-private |
| **Access to outer private statics** | ✅ Yes | ❌ No |
| **Encapsulation** | Scoped within the outer class | Globally accessible |
| **Use case** | Tightly coupled helper to outer class | General-purpose, reusable class |

> 💡 **Guideline:** If the inner class is **only meaningful in the context of the outer class** (e.g., a `Builder`, `Node`, or `Entry`), make it a static inner class. If it can stand on its own, make it a top-level class.

---

## Common Use Cases

| Pattern / Use Case | Example in Java Standard Library |
|---|---|
| **Builder Pattern** | `Lombok @Builder`, `AlertDialog.Builder` (Android) |
| **Data Structure Nodes** | `LinkedList.Node`, `TreeMap.Entry` |
| **Map Entries** | `Map.Entry<K, V>` (interface in `java.util`) |
| **Event/Callback Helpers** | GUI event helper classes |
| **Grouping related classes** | `Math` utilities grouped inside a parent class |
| **Singleton within scope** | Scoped singleton managed by the outer class |

---

## Common Pitfalls

### 1. Confusing Static Inner Class with Non-Static Inner Class

```java
public class Outer {

    public class NonStaticInner { }         // requires Outer instance
    public static class StaticInner { }    // does NOT require Outer instance
}

// ❌ This will NOT compile for NonStaticInner without an outer instance
// Outer.NonStaticInner obj = new Outer.NonStaticInner(); // ERROR

// ✅ This is correct for StaticInner
Outer.StaticInner obj = new Outer.StaticInner(); // OK
```

### 2. Trying to Access Instance Members Directly

```java
public class Outer {

    private String name = "Outer";

    public static class Inner {
        public void show() {
            // ❌ ERROR: name is an instance field, not a static field
            // System.out.println(name);

            // ✅ Correct — pass or create an Outer instance
            Outer o = new Outer();
            System.out.println(o.name);
        }
    }
}
```

### 3. Memory Leak Risk with Non-Static vs Static

```java
// ⚠️ Non-static inner class holds a hidden reference to the outer instance
// This can prevent garbage collection of the outer object
public class Outer {
    public class LeakyInner {
        // implicitly holds: Outer.this
    }
}

// ✅ Static inner class has NO hidden reference — safer for long-lived objects
public class Outer {
    public static class SafeInner {
        // No hidden reference to Outer
    }
}
```

> ⚠️ **Always prefer static inner classes** unless the inner class explicitly needs access to instance members of the outer class. Non-static inner classes can cause **memory leaks** if the inner class object outlives the outer class object.

---

## Summary

| Concept | Key Point |
|---|---|
| **Definition** | A nested class declared with `static` inside an outer class |
| **No outer instance needed** | Instantiated directly: `new Outer.Inner()` |
| **Accesses outer statics** | Yes — including `private` static fields and methods |
| **Cannot access outer instance members** | Not without an explicit outer object reference |
| **Most common use** | Builder Pattern, data structure nodes, grouped helpers |
| **Prefer over non-static** | When the inner class doesn't need `Outer.this` — avoids memory leaks |
| **Prefer over top-level** | When the class is tightly coupled to and only meaningful within the outer class |

> 💡 **Best Practice:** Default to making nested classes `static`. Only drop the `static` modifier when the inner class genuinely needs to operate on the enclosing instance's state.