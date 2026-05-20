# Factory Method Design Pattern

## Overview

The **Factory Method** is a creational design pattern that defines an interface for creating an object, but lets subclasses decide which class to instantiate. It defers the instantiation logic to subclasses, promoting loose coupling and adherence to the Open/Closed Principle.

> *"Define an interface for creating an object, but let subclasses decide which class to instantiate. Factory Method lets a class defer instantiation to subclasses."*
> — Gang of Four (GoF)

---

## Intent

- Provide a way to create objects without specifying the exact class of the object to be created.
- Allow subclasses to alter the type of objects that will be created.
- Decouple the client code from concrete product implementations.

---

## The Problem It Solves

Imagine you're building a logistics application. Initially, it only supports truck deliveries. All your code is tightly coupled to the `Truck` class. Later, you need to add sea transport (`Ship`). Adding it would require changing the entire codebase.

The Factory Method solves this by providing a single interface (`createTransport()`) and letting each subclass decide what kind of transport to create.

---

## Structure

```
         Creator (Abstract)
        ┌─────────────────────┐
        │ + factoryMethod()   │◄──── defines the interface
        │ + someOperation()   │      for creating a product
        └────────┬────────────┘
                 │
       ┌─────────┴──────────┐
       │                    │
ConcreteCreatorA     ConcreteCreatorB
┌──────────────┐    ┌──────────────┐
│factoryMethod │    │factoryMethod │
│  → ProductA  │    │  → ProductB  │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ▼                   ▼
  «interface»         «interface»
    Product  ◄────────────────────
  ┌──────────┐
  │operation │
  └──────────┘
    ▲       ▲
ProductA   ProductB
```

### Participants

| Role | Responsibility |
|---|---|
| **Creator** | Declares the factory method. May provide a default implementation. |
| **ConcreteCreator** | Overrides the factory method to return a specific product. |
| **Product** | Defines the interface for objects the factory method creates. |
| **ConcreteProduct** | Implements the Product interface. |

---

## Code Example

### Java

The example models a **notification system** — a common real-world use case where the type of notification (Email, SMS, Push) is determined at runtime.

```java
// ── Product Interface ──────────────────────────────────────────────
public interface Notification {
    void send(String message);
}

// ── Concrete Products ──────────────────────────────────────────────
public class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("[Email] Sending: " + message);
    }
}

public class SMSNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("[SMS] Sending: " + message);
    }
}

public class PushNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("[Push] Sending: " + message);
    }
}

// ── Creator (Abstract Class) ───────────────────────────────────────
public abstract class NotificationService {

    // Factory Method — subclasses must override this
    public abstract Notification createNotification();

    // Uses the factory method internally; client never calls createNotification() directly
    public void notify(String message) {
        Notification notification = createNotification();
        notification.send(message);
    }
}

// ── Concrete Creators ──────────────────────────────────────────────
public class EmailService extends NotificationService {
    @Override
    public Notification createNotification() {
        return new EmailNotification();
    }
}

public class SMSService extends NotificationService {
    @Override
    public Notification createNotification() {
        return new SMSNotification();
    }
}

public class PushService extends NotificationService {
    @Override
    public Notification createNotification() {
        return new PushNotification();
    }
}

// ── Client Code ────────────────────────────────────────────────────
public class Main {
    // Client works with the abstract type — no knowledge of concrete classes
    static void sendAlert(NotificationService service, String message) {
        service.notify(message);
    }

    public static void main(String[] args) {
        sendAlert(new EmailService(), "Your order has shipped!");
        sendAlert(new SMSService(),   "Your OTP is 482910.");
        sendAlert(new PushService(),  "Flash sale starts now!");
    }
}
```

**Output:**
```
[Email] Sending: Your order has shipped!
[SMS]   Sending: Your OTP is 482910.
[Push]  Sending: Flash sale starts now!
```

**Key observations in the Java code:**

- `Notification` is a Java `interface` — the product contract.
- `NotificationService` is an `abstract class` — the creator. It owns the `notify()` business method and delegates object creation to `createNotification()`.
- Each `ConcreteCreator` (`EmailService`, `SMSService`, `PushService`) only overrides the factory method, keeping classes small and focused.
- `Main` (client) depends only on `NotificationService` and `Notification` — never on any concrete class.

---

## When to Use

Use the Factory Method pattern when:

- You **don't know ahead of time** what class you need to instantiate.
- You want to provide **library or framework users** a way to extend its internal components.
- You want to **reuse existing objects** instead of rebuilding them each time.
- You need to **decouple** the creation of objects from their usage.

---

## When NOT to Use

- When the object creation logic is simple and unlikely to change — it adds unnecessary complexity.
- When you only have **one concrete product** — a simple constructor call suffices.
- When you need to create objects from a **family of related products** — consider [Abstract Factory](https://refactoring.guru/design-patterns/abstract-factory) instead.

---

## Advantages

| Advantage | Description |
|---|---|
| **Open/Closed Principle** | Add new product types without modifying existing creator code. |
| **Single Responsibility** | Product creation code lives in one dedicated place. |
| **Loose Coupling** | Client code works with products via their interface, not concrete classes. |
| **Testability** | Easy to mock or stub the factory method in unit tests. |

---

## Disadvantages

- Introduces **additional classes and interfaces**, increasing code complexity.
- Can lead to **deep inheritance hierarchies** if overused.
- Each new product type requires a new `ConcreteCreator` subclass — can feel repetitive.

---

## Relationship to Other Patterns

| Pattern | Relationship |
|---|---|
| **Abstract Factory** | Often implemented using Factory Methods. Abstract Factory creates families of objects. |
| **Template Method** | Factory Method is a specialization of Template Method applied to object creation. |
| **Prototype** | Can replace Factory Method when you need to clone objects instead of constructing them. |
| **Singleton** | A Factory Method can return a singleton instance. |

---

## Real-World Analogies

- **Hiring Agency**: A company requests a worker from an agency (factory) without knowing how the agency selects and trains candidates (concrete product creation).
- **Restaurant Kitchen**: You order a dish (product) via the menu (interface). The kitchen (creator) decides which chef or station (concrete creator) prepares it.
- **Framework Plugins**: A UI framework defines a `createButton()` method. Each OS-specific subclass (Windows, macOS) returns its own native button.

---

## Real-World Usage in Libraries & Frameworks

- **Java**: `java.util.Calendar.getInstance()`, `java.nio.file.Files`, `javax.xml.parsers.DocumentBuilderFactory`
- **Python**: `logging.getLogger()` returns different logger types based on configuration.
- **JavaScript/Node.js**: `http.createServer()`, various driver adapters in ORMs like Sequelize.
- **Spring Framework**: `BeanFactory` and `ApplicationContext` use the Factory Method pattern extensively.
- **Django**: `Model.objects.create()` delegates to a configurable manager.

---

## Summary

```
Factory Method in one sentence:
"Let subclasses decide which object to create — the parent only defines the contract."
```

The Factory Method pattern is a foundational pattern in object-oriented design. It strikes an elegant balance between flexibility and structure: you get a stable creation interface while remaining open to extension. Master this pattern and you'll find it appearing naturally across almost every large codebase.

---

## Further Reading

- *Design Patterns: Elements of Reusable Object-Oriented Software* — GoF (Gamma, Helm, Johnson, Vlissides)
- [Refactoring Guru — Factory Method](https://refactoring.guru/design-patterns/factory-method)
- [SourceMaking — Factory Method](https://sourcemaking.com/design_patterns/factory_method)