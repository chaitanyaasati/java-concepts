# SOLID Principles

## Overview

**SOLID** is an acronym for five foundational object-oriented design principles introduced by Robert C. Martin ("Uncle Bob"). These principles guide developers toward writing code that is **maintainable, scalable, and resilient to change**.

> *"The goal of the SOLID principles is the creation of mid-level software structures that tolerate change, are easy to understand, and are the basis of components that can be used in many software systems."*
> — Robert C. Martin, *Clean Architecture*

| Letter | Principle | Core Idea |
|---|---|---|
| **S** | Single Responsibility Principle | A class should have only one reason to change. |
| **O** | Open/Closed Principle | Open for extension, closed for modification. |
| **L** | Liskov Substitution Principle | Subtypes must be substitutable for their base types. |
| **I** | Interface Segregation Principle | No client should be forced to depend on methods it does not use. |
| **D** | Dependency Inversion Principle | Depend on abstractions, not on concretions. |

---

## Why SOLID Matters

Without these principles, codebases tend to develop these symptoms:

- **Rigidity** — A change in one place breaks things elsewhere.
- **Fragility** — The software breaks in unexpected ways after a change.
- **Immobility** — Code cannot be reused because it is too tightly coupled.
- **Viscosity** — Doing the right thing is harder than doing the wrong thing.

SOLID principles are the antidote to each of these problems.

---

## S — Single Responsibility Principle (SRP)

### Definition

> *"A class should have only one reason to change."*

A class should do **one thing** and do it well. Every responsibility is a potential axis of change — the more responsibilities a class has, the more reasons it has to be modified, and the more fragile it becomes.

### Violation Example

```java
// ✗ BAD — This class has THREE responsibilities:
//   1. Managing user data
//   2. Persisting to a database
//   3. Sending emails
public class UserManager {

    public void createUser(String name, String email) {
        // validate and store in memory...
        System.out.println("User created: " + name);

        // DB logic mixed in
        System.out.println("INSERT INTO users VALUES ('" + name + "', '" + email + "')");

        // Email logic mixed in
        System.out.println("Sending welcome email to: " + email);
    }
}
```

A change to email formatting forces you to touch `UserManager`. A change to the database schema also forces you to touch `UserManager`. These are completely unrelated reasons to change.

### Correct Example

```java
// ✓ GOOD — Each class has exactly one responsibility

// Responsibility 1: User domain logic
public class User {
    private String name;
    private String email;

    public User(String name, String email) {
        this.name  = name;
        this.email = email;
    }

    public String getName()  { return name; }
    public String getEmail() { return email; }
}

// Responsibility 2: Persistence
public class UserRepository {
    public void save(User user) {
        System.out.println("INSERT INTO users VALUES ('"
            + user.getName() + "', '" + user.getEmail() + "')");
    }
}

// Responsibility 3: Notifications
public class EmailService {
    public void sendWelcomeEmail(User user) {
        System.out.println("Sending welcome email to: " + user.getEmail());
    }
}

// Orchestrator — thin coordinator, delegates to specialists
public class UserRegistrationService {
    private final UserRepository repository;
    private final EmailService    emailService;

    public UserRegistrationService(UserRepository repository, EmailService emailService) {
        this.repository   = repository;
        this.emailService = emailService;
    }

    public void register(String name, String email) {
        User user = new User(name, email);
        repository.save(user);
        emailService.sendWelcomeEmail(user);
    }
}
```

**Key takeaway:** Each class now changes for exactly one reason. Email formatting changes touch only `EmailService`. Database changes touch only `UserRepository`.

---

## O — Open/Closed Principle (OCP)

### Definition

> *"Software entities should be open for extension, but closed for modification."*

You should be able to add new behaviour **without changing existing, tested code**. New functionality arrives via new classes, not by editing old ones.

### Violation Example

```java
// ✗ BAD — Adding a new shape requires modifying this class
public class AreaCalculator {
    public double calculate(Object shape) {
        if (shape instanceof Circle) {
            Circle c = (Circle) shape;
            return Math.PI * c.getRadius() * c.getRadius();
        } else if (shape instanceof Rectangle) {
            Rectangle r = (Rectangle) shape;
            return r.getWidth() * r.getHeight();
        }
        // Adding Triangle forces you to edit this method — violates OCP
        throw new IllegalArgumentException("Unknown shape");
    }
}
```

Every new shape forces a change to `AreaCalculator` — risking bugs in already-working code.

### Correct Example

```java
// ✓ GOOD — New shapes extend the system without touching existing code

// Abstraction — the extension point
public interface Shape {
    double area();
}

// Each shape encapsulates its own formula
public class Circle implements Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

public class Rectangle implements Shape {
    private final double width, height;
    public Rectangle(double width, double height) {
        this.width = width; this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

// Adding a Triangle requires ZERO changes to AreaCalculator
public class Triangle implements Shape {
    private final double base, height;
    public Triangle(double base, double height) {
        this.base = base; this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}

// Closed for modification — never needs to change
public class AreaCalculator {
    public double calculate(Shape shape) {
        return shape.area();
    }
}
```

**Key takeaway:** `AreaCalculator` is written once and never touched again. Adding a `Pentagon` or `Hexagon` is purely additive.

---

## L — Liskov Substitution Principle (LSP)

### Definition

> *"Objects of a subclass should be replaceable with objects of the superclass without breaking the correctness of the program."*

Introduced by Barbara Liskov in 1987. If `S` is a subtype of `T`, then anywhere you use a `T`, you should be able to substitute an `S` and the program must still work correctly.

### Violation Example

```java
// ✗ BAD — Square "is-a" Rectangle sounds right, but breaks LSP

public class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int width)   { this.width  = width; }
    public void setHeight(int height) { this.height = height; }

    public int area() { return width * height; }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        // A square must keep sides equal — silently overrides parent contract
        this.width  = width;
        this.height = width;
    }

    @Override
    public void setHeight(int height) {
        this.width  = height;
        this.height = height;
    }
}

// Client code that breaks when Square is substituted for Rectangle
public class ResizeDemo {
    public static void resize(Rectangle r) {
        r.setWidth(5);
        r.setHeight(10);
        // Expected area: 50 — but with Square it's 100! Broken.
        System.out.println("Area: " + r.area());
    }

    public static void main(String[] args) {
        resize(new Rectangle()); // prints 50 ✓
        resize(new Square());    // prints 100 ✗ — contract violated
    }
}
```

### Correct Example

```java
// ✓ GOOD — Use a common abstraction; avoid forced inheritance

public interface Shape {
    int area();
}

public class Rectangle implements Shape {
    private final int width;
    private final int height;

    public Rectangle(int width, int height) {
        this.width = width; this.height = height;
    }

    @Override
    public int area() { return width * height; }
}

public class Square implements Shape {
    private final int side;

    public Square(int side) { this.side = side; }

    @Override
    public int area() { return side * side; }
}

// Client works correctly with any Shape — LSP satisfied
public class AreaPrinter {
    public void print(Shape shape) {
        System.out.println("Area: " + shape.area());
    }

    public static void main(String[] args) {
        AreaPrinter printer = new AreaPrinter();
        printer.print(new Rectangle(5, 10)); // Area: 50 ✓
        printer.print(new Square(7));        // Area: 49 ✓
    }
}
```

**Key takeaway:** LSP is violated when a subclass weakens a precondition, strengthens a postcondition, or changes behaviour the client depends on. When in doubt, prefer a shared interface over deep inheritance.

---

## I — Interface Segregation Principle (ISP)

### Definition

> *"No client should be forced to depend on methods it does not use."*

Fat interfaces create unnecessary coupling. Split large interfaces into smaller, focused ones so that implementing classes only need to know about methods relevant to them.

### Violation Example

```java
// ✗ BAD — One bloated interface forces all implementors to deal with all methods

public interface Worker {
    void work();
    void eat();
    void sleep();
    void attendMeeting();
}

// A Robot can work and attend meetings, but cannot eat or sleep
public class Robot implements Worker {
    @Override public void work()          { System.out.println("Robot working."); }
    @Override public void eat()           { throw new UnsupportedOperationException("Robots don't eat!"); }
    @Override public void sleep()         { throw new UnsupportedOperationException("Robots don't sleep!"); }
    @Override public void attendMeeting() { System.out.println("Robot attending meeting."); }
}
```

`Robot` is forced to "implement" behaviour that is meaningless for it, and throws exceptions at runtime — a clear ISP violation.

### Correct Example

```java
// ✓ GOOD — Split into focused, role-specific interfaces

public interface Workable {
    void work();
}

public interface Feedable {
    void eat();
}

public interface Restable {
    void sleep();
}

public interface MeetingAttendable {
    void attendMeeting();
}

// Human worker implements all relevant interfaces
public class HumanWorker implements Workable, Feedable, Restable, MeetingAttendable {
    @Override public void work()          { System.out.println("Human working."); }
    @Override public void eat()           { System.out.println("Human eating."); }
    @Override public void sleep()         { System.out.println("Human sleeping."); }
    @Override public void attendMeeting() { System.out.println("Human attending meeting."); }
}

// Robot only implements what applies to it — no dead methods
public class Robot implements Workable, MeetingAttendable {
    @Override public void work()          { System.out.println("Robot working."); }
    @Override public void attendMeeting() { System.out.println("Robot attending meeting."); }
}
```

**Key takeaway:** ISP keeps implementations clean and honest. No class should carry methods it cannot meaningfully fulfil. Smaller interfaces are also far easier to mock in unit tests.

---

## D — Dependency Inversion Principle (DIP)

### Definition

> *"High-level modules should not depend on low-level modules. Both should depend on abstractions. Abstractions should not depend on details. Details should depend on abstractions."*

This is the principle that enables **dependency injection** and is the foundation of most modern application frameworks (Spring, Guice, etc.).

### Violation Example

```java
// ✗ BAD — High-level class directly instantiates a low-level class

public class MySQLDatabase {
    public void save(String data) {
        System.out.println("Saving '" + data + "' to MySQL database.");
    }
}

// OrderService is tightly coupled to MySQL — switching to PostgreSQL
// requires changing OrderService itself
public class OrderService {
    private MySQLDatabase database = new MySQLDatabase(); // hard dependency

    public void placeOrder(String orderData) {
        System.out.println("Processing order...");
        database.save(orderData); // cannot swap without editing this class
    }
}
```

### Correct Example

```java
// ✓ GOOD — Both high-level and low-level depend on an abstraction

// Abstraction (the inversion point)
public interface Database {
    void save(String data);
}

// Low-level module A — depends on the abstraction
public class MySQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving '" + data + "' to MySQL.");
    }
}

// Low-level module B — also depends on the abstraction
public class PostgreSQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving '" + data + "' to PostgreSQL.");
    }
}

// Low-level module C — in-memory, useful for testing
public class InMemoryDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving '" + data + "' in memory.");
    }
}

// High-level module — depends only on the abstraction, never on a concrete DB
public class OrderService {
    private final Database database; // injected from outside

    // Constructor injection — the preferred form of DI
    public OrderService(Database database) {
        this.database = database;
    }

    public void placeOrder(String orderData) {
        System.out.println("Processing order...");
        database.save(orderData);
    }
}

// Composition root — the only place that knows about concrete classes
public class Main {
    public static void main(String[] args) {
        // Swap databases without touching OrderService
        Database db = new PostgreSQLDatabase();
        OrderService service = new OrderService(db);
        service.placeOrder("Order #1042");

        // In tests, inject InMemoryDatabase — zero infrastructure needed
        OrderService testService = new OrderService(new InMemoryDatabase());
        testService.placeOrder("Test Order");
    }
}
```

**Output:**
```
Processing order...
Saving 'Order #1042' to PostgreSQL.
Processing order...
Saving 'Test Order' in memory.
```

**Key takeaway:** DIP makes high-level business logic immune to changes in low-level infrastructure. Swapping databases, message brokers, or file systems becomes a single-line change at the composition root.

---

## Principles Working Together

The five principles are not independent — they reinforce each other:

```
SRP  ──► keeps classes focused, making them easier to extend (OCP)
OCP  ──► relies on abstractions, which are defined by ISP
LSP  ──► ensures substitutability so OCP extensions actually work
ISP  ──► produces lean interfaces that are easier to invert (DIP)
DIP  ──► enforces the use of abstractions that make SRP and OCP possible
```

A class that violates SRP is usually also hard to satisfy LSP and DIP. Fix one and the others often improve naturally.

---

## Quick Reference

| Principle | Symptom of Violation | Fix |
|---|---|---|
| **SRP** | Class changes for multiple unrelated reasons | Split into focused, single-purpose classes |
| **OCP** | Adding features requires editing existing classes | Extract an interface; add via new subclass |
| **LSP** | Subclass throws exceptions or silently changes behaviour | Prefer interface over deep inheritance |
| **ISP** | Class has `throw new UnsupportedOperationException()` | Split fat interfaces into role interfaces |
| **DIP** | Class uses `new ConcreteClass()` internally | Inject dependency via constructor/interface |

---

## Real-World Usage

| Principle | Where you see it |
|---|---|
| **SRP** | Layered architecture (Controller / Service / Repository) |
| **OCP** | Plugin systems, strategy pattern, decorator pattern |
| **LSP** | Java Collections (`List`, `ArrayList`, `LinkedList` interchangeability) |
| **ISP** | `java.util.Comparator`, `java.lang.Runnable`, `java.io.Closeable` |
| **DIP** | Spring `@Autowired`, JDBC `DataSource`, JPA `EntityManager` |

---

## Summary

```
S — One class, one job.
O — Add features; never edit finished code.
L — Subtypes must honour the contract of their parent.
I — Prefer many small interfaces over one large one.
D — Depend on abstractions; inject your dependencies.
```

SOLID principles do not guarantee perfect software — they are design heuristics, not laws. Apply them with judgment. Over-engineering a simple script with SOLID abstractions is its own form of bad design. But for any system expected to grow, change, and be maintained by a team, SOLID provides an invaluable shared vocabulary and a tested set of guidelines for keeping complexity under control.

---

## Further Reading

- *Clean Code* — Robert C. Martin
- *Clean Architecture* — Robert C. Martin
- *Agile Software Development, Principles, Patterns, and Practices* — Robert C. Martin
- [SOLID Principles on Baeldung (Java)](https://www.baeldung.com/solid-principles)
- [Refactoring Guru — Design Principles](https://refactoring.guru/refactoring/principles)