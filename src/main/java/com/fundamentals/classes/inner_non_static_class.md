# Inner Classes (Non-Static) in Java

## Table of Contents
- [Introduction](#introduction)
- [Types of Inner Classes in Java](#types-of-inner-classes-in-java)
- [Regular Inner Class](#regular-inner-class)
    - [What is a Regular Inner Class?](#what-is-a-regular-inner-class)
    - [Syntax](#syntax--regular-inner-class)
    - [Key Characteristics](#key-characteristics--regular-inner-class)
    - [Instantiating a Regular Inner Class](#instantiating-a-regular-inner-class)
    - [Access Rules](#access-rules)
    - [The `this` Keyword in Inner Classes](#the-this-keyword-in-inner-classes)
    - [Examples](#examples--regular-inner-class)
- [Local Inner Class](#local-inner-class)
    - [What is a Local Inner Class?](#what-is-a-local-inner-class)
    - [Syntax](#syntax--local-inner-class)
    - [Key Characteristics](#key-characteristics--local-inner-class)
    - [Examples](#examples--local-inner-class)
- [Anonymous Inner Class](#anonymous-inner-class)
    - [What is an Anonymous Inner Class?](#what-is-an-anonymous-inner-class)
    - [Syntax](#syntax--anonymous-inner-class)
    - [Key Characteristics](#key-characteristics--anonymous-inner-class)
    - [Examples](#examples--anonymous-inner-class)
- [Shadowing in Inner Classes](#shadowing-in-inner-classes)
- [Inner Class vs Static Nested Class](#inner-class-vs-static-nested-class)
- [Common Use Cases](#common-use-cases)
- [Common Pitfalls](#common-pitfalls)
- [Summary](#summary)

---

## Introduction

In Java, a class defined **inside another class** without the `static` keyword is called a **non-static inner class** — simply referred to as an **inner class**. Unlike static nested classes, inner classes are tightly coupled to the **instance** of their enclosing outer class.

This means an inner class object **always holds a hidden reference** to the outer class object that created it, giving it full access to all of the outer class's fields and methods — including `private` ones.

There are **three types** of non-static inner classes in Java:

| Type | Where it is defined |
|---|---|
| **Regular Inner Class** | Directly inside the outer class body |
| **Local Inner Class** | Inside a method or block |
| **Anonymous Inner Class** | Inline, without a name |

---

## Types of Inner Classes in Java

```
Nested Classes
├── Static Nested Class
└── Non-Static Inner Classes       ← focus of this document
    ├── Regular Inner Class
    ├── Local Inner Class
    └── Anonymous Inner Class
```

---

## Regular Inner Class

### What is a Regular Inner Class?

A **regular inner class** is a class declared directly inside an outer class body, **without** the `static` keyword. It has a special relationship with the outer class — every instance of the inner class is associated with exactly **one instance of the outer class**.

### Syntax — Regular Inner Class

```java
public class OuterClass {

    // Regular (non-static) inner class
    public class InnerClass {

        public void display() {
            System.out.println("Inside InnerClass");
        }
    }
}
```

### Key Characteristics — Regular Inner Class

| Characteristic | Detail |
|---|---|
| **`static` keyword** | ❌ Not used |
| **Requires outer instance** | ✅ Yes — always |
| **Access to outer instance members** | ✅ Yes — all, including `private` |
| **Access to outer static members** | ✅ Yes |
| **Can declare static members** | ❌ No (except `static final` constants) |
| **Can extend/implement** | ✅ Yes |
| **`this` keyword** | Refers to inner instance; `OuterClass.this` for outer |

### Instantiating a Regular Inner Class

A regular inner class **cannot** be instantiated without an outer class instance:

```java
// Step 1: Create the outer class instance
OuterClass outer = new OuterClass();

// Step 2: Use the outer instance to create the inner class instance
OuterClass.InnerClass inner = outer.new InnerClass();

// Shorthand (from inside the outer class)
InnerClass inner = new InnerClass();
```

Compare with a **static** nested class:

```java
// Static nested class — no outer instance needed
OuterClass.StaticNested obj = new OuterClass.StaticNested(); // ✅ directly
```

### Access Rules

A regular inner class has **unrestricted access** to all members of the outer class:

```java
public class Outer {

    private String privateField  = "private";
    protected String protectedField = "protected";
    public String publicField    = "public";
    static String staticField    = "static";

    public class Inner {

        public void showAll() {
            System.out.println(privateField);   // ✅ private outer field
            System.out.println(protectedField); // ✅ protected outer field
            System.out.println(publicField);    // ✅ public outer field
            System.out.println(staticField);    // ✅ static outer field
        }
    }
}
```

### The `this` Keyword in Inner Classes

When both the inner and outer class share a field with the same name, `this` and `OuterClass.this` are used to disambiguate:

```java
public class Outer {

    String name = "Outer";

    public class Inner {

        String name = "Inner";

        public void printNames() {
            System.out.println(name);              // "Inner"  — inner class field
            System.out.println(this.name);         // "Inner"  — same as above
            System.out.println(Outer.this.name);   // "Outer"  — outer class field
        }
    }
}
```

### Examples — Regular Inner Class

#### Example 1 — Basic Inner Class

```java
public class Car {

    private String brand;
    private int year;

    public Car(String brand, int year) {
        this.brand = brand;
        this.year  = year;
    }

    // Regular inner class — accesses Car's private fields
    public class Engine {

        private int horsepower;

        public Engine(int horsepower) {
            this.horsepower = horsepower;
        }

        public void describe() {
            // ✅ Directly accesses outer class private fields
            System.out.println("Car   : " + brand + " (" + year + ")");
            System.out.println("Engine: " + horsepower + " HP");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Car car = new Car("Tesla", 2024);
        Car.Engine engine = car.new Engine(450);
        engine.describe();
    }
}

// Output:
// Car   : Tesla (2024)
// Engine: 450 HP
```

---

#### Example 2 — Iterator Pattern

Java's own `ArrayList` uses an inner class to implement `Iterator`. Here's the same concept built from scratch:

```java
import java.util.NoSuchElementException;

public class NumberRange {

    private int start;
    private int end;

    public NumberRange(int start, int end) {
        this.start = start;
        this.end   = end;
    }

    // Inner class implementing Iterator behaviour
    public class RangeIterator {

        private int current;

        public RangeIterator() {
            this.current = start; // ✅ accesses outer field directly
        }

        public boolean hasNext() {
            return current <= end; // ✅ accesses outer field directly
        }

        public int next() {
            if (!hasNext()) throw new NoSuchElementException();
            return current++;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        NumberRange range = new NumberRange(1, 5);
        NumberRange.RangeIterator iterator = range.new RangeIterator();

        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        // Output: 1 2 3 4 5
    }
}
```

---

#### Example 3 — Event Listener Pattern

Regular inner classes are ideal for event listeners because they need direct access to the outer object's state:

```java
public class Button {

    private String label;
    private ClickListener listener;

    public Button(String label) {
        this.label = label;
    }

    public interface ClickListener {
        void onClick();
    }

    public void setClickListener(ClickListener listener) {
        this.listener = listener;
    }

    public void click() {
        System.out.println("[" + label + "] was clicked!");
        if (listener != null) listener.onClick();
    }
}

public class Form {

    private String formTitle = "Login Form";
    private int submitCount = 0;

    // Regular inner class implementing an interface
    public class SubmitHandler implements Button.ClickListener {

        @Override
        public void onClick() {
            submitCount++; // ✅ directly modifies outer class field
            System.out.println(formTitle + " submitted. Count: " + submitCount);
        }
    }

    public void setup() {
        Button submitBtn = new Button("Submit");
        submitBtn.setClickListener(new SubmitHandler());
        submitBtn.click();
        submitBtn.click();
    }
}

public class Main {
    public static void main(String[] args) {
        new Form().setup();
    }
}

// Output:
// [Submit] was clicked!
// Login Form submitted. Count: 1
// [Submit] was clicked!
// Login Form submitted. Count: 2
```

---

#### Example 4 — Outer Class Creating Multiple Inner Instances

Each inner class instance is bound to its own outer class instance:

```java
public class House {

    private String address;

    public House(String address) {
        this.address = address;
    }

    public class Room {

        private String roomName;

        public Room(String roomName) {
            this.roomName = roomName;
        }

        public void describe() {
            // address comes from the enclosing House instance
            System.out.println(roomName + " in house at: " + address);
        }
    }

    public void printRooms() {
        new Room("Living Room").describe();
        new Room("Kitchen").describe();
        new Room("Bedroom").describe();
    }
}

public class Main {
    public static void main(String[] args) {
        House h1 = new House("221B Baker Street");
        House h2 = new House("4 Privet Drive");

        h1.printRooms();
        System.out.println("---");
        h2.printRooms();
    }
}

// Output:
// Living Room in house at: 221B Baker Street
// Kitchen in house at: 221B Baker Street
// Bedroom in house at: 221B Baker Street
// ---
// Living Room in house at: 4 Privet Drive
// Kitchen in house at: 4 Privet Drive
// Bedroom in house at: 4 Privet Drive
```

---

## Local Inner Class

### What is a Local Inner Class?

A **local inner class** is a class defined **inside a method, constructor, or block**. Its scope is limited to that block — it cannot be used anywhere outside of it.

### Syntax — Local Inner Class

```java
public class OuterClass {

    public void myMethod() {

        // Local inner class — defined inside a method
        class LocalHelper {
            public void help() {
                System.out.println("Helping from inside the method!");
            }
        }

        // Must be instantiated within the same method
        LocalHelper helper = new LocalHelper();
        helper.help();
    }
}
```

### Key Characteristics — Local Inner Class

| Characteristic | Detail |
|---|---|
| **Scope** | Only within the method/block where it is defined |
| **Access to outer instance members** | ✅ Yes |
| **Access to local variables** | ✅ Only if `effectively final` |
| **Can have access modifiers** | ❌ No (`public`, `private`, etc. not allowed) |
| **Can extend/implement** | ✅ Yes |
| **Can declare static members** | ❌ No |

> 💡 **Effectively Final:** A local variable used inside a local inner class must not be reassigned after it is first set. The compiler enforces this even without the `final` keyword (since Java 8).

### Examples — Local Inner Class

#### Example 1 — Basic Local Inner Class

```java
public class Printer {

    private String printerName = "HP LaserJet";

    public void printDocument(String docName) {

        // Local inner class — scoped to this method only
        class DocumentFormatter {

            public String format() {
                // ✅ Can access outer instance field (printerName)
                // ✅ Can access effectively final local variable (docName)
                return "[" + printerName + "] Printing: " + docName;
            }
        }

        DocumentFormatter formatter = new DocumentFormatter();
        System.out.println(formatter.format());
    }
}

public class Main {
    public static void main(String[] args) {
        Printer printer = new Printer();
        printer.printDocument("Annual Report.pdf");
        printer.printDocument("Invoice_2024.pdf");
    }
}

// Output:
// [HP LaserJet] Printing: Annual Report.pdf
// [HP LaserJet] Printing: Invoice_2024.pdf
```

---

#### Example 2 — Local Inner Class Implementing an Interface

```java
public class SortDemo {

    public void sortAndPrint(int[] numbers) {

        // Local inner class implementing Runnable for demonstration
        class ArrayPrinter {
            void print(int[] arr) {
                for (int n : arr) System.out.print(n + " ");
                System.out.println();
            }
        }

        // Sort using bubble sort
        for (int i = 0; i < numbers.length - 1; i++) {
            for (int j = 0; j < numbers.length - 1 - i; j++) {
                if (numbers[j] > numbers[j + 1]) {
                    int temp = numbers[j];
                    numbers[j] = numbers[j + 1];
                    numbers[j + 1] = temp;
                }
            }
        }

        ArrayPrinter printer = new ArrayPrinter();
        printer.print(numbers);
    }
}

public class Main {
    public static void main(String[] args) {
        SortDemo demo = new SortDemo();
        demo.sortAndPrint(new int[]{5, 2, 8, 1, 9, 3});
    }
}

// Output:
// 1 2 3 5 8 9
```

---

#### Example 3 — Effectively Final Rule

```java
public class LocalVarDemo {

    public void process() {

        String message = "Hello"; // effectively final — never reassigned

        class Greeter {
            void greet() {
                System.out.println(message); // ✅ OK — effectively final
            }
        }

        new Greeter().greet();

        // message = "Changed"; // ❌ If you uncomment this, compiler error above
    }
}
```

---

## Anonymous Inner Class

### What is an Anonymous Inner Class?

An **anonymous inner class** is a local inner class **without a name**. It is declared and instantiated in a single expression, typically used to provide a one-off implementation of an interface or abstract class **inline**.

### Syntax — Anonymous Inner Class

```java
// Implementing an interface anonymously
InterfaceName obj = new InterfaceName() {
    @Override
    public void method() {
        // implementation
    }
};

// Extending an abstract class anonymously
AbstractClass obj = new AbstractClass() {
    @Override
    public void abstractMethod() {
        // implementation
    }
};
```

### Key Characteristics — Anonymous Inner Class

| Characteristic | Detail |
|---|---|
| **Has a name** | ❌ No |
| **Declared and instantiated** | In one single expression |
| **Can implement** | Exactly **one** interface, or extend **one** class |
| **Can have constructors** | ❌ No (uses instance initializer `{}` instead) |
| **Access to outer members** | ✅ Yes |
| **Access to local variables** | ✅ Only if effectively final |
| **Can define new methods** | ✅ Yes, but only callable via polymorphism within the block |
| **Best replaced by** | Lambda expressions (for single-method interfaces, Java 8+) |

### Examples — Anonymous Inner Class

#### Example 1 — Implementing an Interface

```java
public interface Greeting {
    void greet(String name);
}

public class Main {
    public static void main(String[] args) {

        // Anonymous inner class implementing Greeting
        Greeting formal = new Greeting() {
            @Override
            public void greet(String name) {
                System.out.println("Good evening, " + name + ".");
            }
        };

        Greeting casual = new Greeting() {
            @Override
            public void greet(String name) {
                System.out.println("Hey, " + name + "!");
            }
        };

        formal.greet("Mr. Smith");  // Good evening, Mr. Smith.
        casual.greet("Alex");       // Hey, Alex!
    }
}
```

---

#### Example 2 — Extending an Abstract Class

```java
public abstract class Shape {
    abstract double area();

    public void printArea() {
        System.out.println("Area: " + area());
    }
}

public class Main {
    public static void main(String[] args) {

        // Anonymous class extending Shape — no need to create a subclass file
        Shape circle = new Shape() {
            private double radius = 7.0;

            @Override
            double area() {
                return Math.PI * radius * radius;
            }
        };

        Shape rectangle = new Shape() {
            private double width  = 5.0;
            private double height = 8.0;

            @Override
            double area() {
                return width * height;
            }
        };

        circle.printArea();     // Area: 153.93804002589985
        rectangle.printArea();  // Area: 40.0
    }
}
```

---

#### Example 3 — Thread with Anonymous Runnable

```java
public class Main {
    public static void main(String[] args) {

        // Anonymous inner class implementing Runnable
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 3; i++) {
                    System.out.println("Thread running: step " + i);
                }
            }
        });

        thread.start();
    }
}

// Output (order may vary):
// Thread running: step 1
// Thread running: step 2
// Thread running: step 3
```

> 💡 **Modern Java:** Since Java 8, `Runnable` is a **functional interface**, so this can be replaced by a lambda: `new Thread(() -> { ... }).start();`
> Anonymous inner classes are still preferred when implementing interfaces with **multiple methods** or when you need instance fields.

---

#### Example 4 — Comparator with Anonymous Inner Class

```java
import java.util.Arrays;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {

        String[] fruits = {"Banana", "Apple", "Mango", "Cherry", "Date"};

        // Sort by string length using an anonymous Comparator
        Arrays.sort(fruits, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        });

        System.out.println(Arrays.toString(fruits));
        // [Date, Apple, Mango, Banana, Cherry]

        // Java 8+ lambda equivalent:
        // Arrays.sort(fruits, (a, b) -> Integer.compare(a.length(), b.length()));
    }
}
```

---

#### Example 5 — Anonymous Class with Instance Initializer

Since anonymous classes can't have constructors, use an **instance initializer block** `{}` for setup:

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        // Double-brace initialization (anonymous class + instance initializer)
        Map<String, Integer> scores = new HashMap<String, Integer>() {{
            put("Alice", 95);
            put("Bob",   87);
            put("Carol", 92);
        }};

        scores.forEach((name, score) ->
                System.out.println(name + ": " + score));
    }
}

// Output:
// Alice: 95
// Bob: 87
// Carol: 92
```

> ⚠️ **Note:** Double-brace initialization is a known anti-pattern for performance-sensitive code because it creates a new anonymous subclass on every use. Prefer `Map.of()` or explicit `put()` calls in production code.

---

## Shadowing in Inner Classes

When an inner class declares a field or variable with the **same name** as an outer class member, the inner name **shadows** the outer one:

```java
public class Outer {

    int value = 10;

    public class Inner {

        int value = 20; // shadows Outer.value

        public void display() {
            int value = 30; // shadows Inner.value

            System.out.println(value);              // 30 — local variable
            System.out.println(this.value);         // 20 — inner class field
            System.out.println(Outer.this.value);   // 10 — outer class field
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
        inner.display();
    }
}

// Output:
// 30
// 20
// 10
```

---

## Inner Class vs Static Nested Class

| Feature | Regular Inner Class | Static Nested Class |
|---|---|---|
| **`static` keyword** | ❌ No | ✅ Yes |
| **Outer instance required** | ✅ Yes | ❌ No |
| **Instantiation** | `outer.new Inner()` | `new Outer.Nested()` |
| **Access to outer instance members** | ✅ Yes (directly) | ❌ No (needs ref) |
| **Access to outer static members** | ✅ Yes | ✅ Yes |
| **Hidden outer reference** | ✅ Yes (can cause leaks) | ❌ No |
| **Can declare static members** | ❌ No | ✅ Yes |
| **Best for** | Iterators, listeners, tightly coupled helpers | Builders, nodes, grouped utilities |

---

## Common Use Cases

| Use Case | Type of Inner Class | Example |
|---|---|---|
| **Iterator / cursor** | Regular Inner Class | Custom collection iterator |
| **Event listener / callback** | Regular or Anonymous | GUI click handlers |
| **One-off interface implementation** | Anonymous | `Runnable`, `Comparator`, `ActionListener` |
| **Method-scoped helper** | Local Inner Class | Formatter used only inside one method |
| **Sorting logic** | Anonymous | `Comparator` passed to `Collections.sort()` |
| **Thread tasks** | Anonymous | `new Thread(new Runnable() { ... })` |

---

## Common Pitfalls

### 1. Memory Leak — Hidden Outer Reference

```java
public class Activity {

    private byte[] largeData = new byte[1024 * 1024]; // 1MB

    public class InnerTask {
        // Implicitly holds a reference to Activity (including largeData)
        // If InnerTask outlives Activity, largeData cannot be garbage collected
        public void run() {
            System.out.println("Running task...");
        }
    }
}

// ✅ Fix: Make it static if it doesn't need outer instance access
public class Activity {
    private byte[] largeData = new byte[1024 * 1024];

    public static class SafeTask { // no hidden reference
        public void run() {
            System.out.println("Running task...");
        }
    }
}
```

### 2. Forgetting Outer Instance for Instantiation

```java
public class Outer {
    public class Inner { }
}

public class Main {
    public static void main(String[] args) {
        // ❌ Compile error — Inner needs an Outer instance
        // Outer.Inner inner = new Outer.Inner();

        // ✅ Correct
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
    }
}
```

### 3. Reassigning Effectively Final Variables in Local/Anonymous Classes

```java
public class Demo {
    public void run() {
        int count = 0;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                // ❌ ERROR: count must be effectively final
                // count++;
                System.out.println("count = " + count);
            }
        };

        // count = 1; // ❌ This would also break the effectively final rule above

        r.run();
    }
}
```

### 4. Anonymous Class Cannot Implement Multiple Interfaces

```java
interface A { void doA(); }
interface B { void doB(); }

// ❌ An anonymous class can only implement ONE interface at a time
// Object obj = new A(), B() { ... }; // Not possible

// ✅ Use a named inner class instead
public class MyClass implements A, B {
    public void doA() { ... }
    public void doB() { ... }
}
```

### 5. Static Members Not Allowed in Inner Classes

```java
public class Outer {
    public class Inner {
        // ❌ Static fields not allowed in non-static inner classes
        // static int count = 0;

        // ✅ Static final constants are allowed (compile-time constants)
        static final int MAX = 100;
    }
}
```

---

## Summary

| Type | Declared Where | Needs Outer Instance | Has Name | Key Use Case |
|---|---|---|---|---|
| **Regular Inner Class** | Inside outer class body | ✅ Yes | ✅ Yes | Iterator, listener, tightly coupled helper |
| **Local Inner Class** | Inside a method/block | ✅ Yes | ✅ Yes | Method-scoped helper logic |
| **Anonymous Inner Class** | Inline as an expression | ✅ Yes | ❌ No | One-off interface/abstract class implementation |

**Key Rules to Remember:**

| Rule | Detail |
|---|---|
| All non-static inner classes hold a **hidden reference** to their outer instance | Can cause memory leaks if the inner object outlives the outer |
| All inner classes have full access to **all** outer class members (including `private`) | The primary advantage over static nested classes |
| Local and anonymous inner classes can only use **effectively final** local variables | The variable must not be reassigned after first use |
| Anonymous inner classes implement exactly **one** interface or extend one class | Use a named class if you need more |
| Prefer **static nested classes** when outer instance access isn't needed | Avoids memory leaks and hidden references |

> 💡 **Best Practice:** Use non-static inner classes **only** when the inner class genuinely needs to operate on the enclosing instance's state. For event listeners and callbacks in modern Java, prefer **lambda expressions** for single-method interfaces. For everything else, consider whether a static nested class or top-level class is a cleaner option.