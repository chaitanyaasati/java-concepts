# Abstract Factory Design Pattern

## Overview

The **Abstract Factory** is a creational design pattern that provides an interface for creating **families of related or dependent objects** without specifying their concrete classes. It is often called a "factory of factories."

> *"Provide an interface for creating families of related or dependent objects without specifying their concrete classes."*
> — Gang of Four (GoF)

Where the **Factory Method** creates one product, the **Abstract Factory** creates an entire **suite of related products** that are designed to work together.

---

## Intent

- Create families of related objects without depending on their concrete classes.
- Enforce consistency — objects from the same factory are always compatible with each other.
- Swap entire product families by switching the factory, with zero changes to client code.

---

## The Problem It Solves

Imagine you're building a cross-platform UI toolkit. Your app needs to render `Button`, `Checkbox`, and `TextField` widgets. On Windows they look one way; on macOS they look another; on Linux yet another.

If you instantiate widgets directly (`new WindowsButton()`, `new MacCheckbox()`), your code becomes riddled with platform checks. Worse, mixing a `WindowsButton` with a `MacCheckbox` on the same screen would look broken.

The Abstract Factory solves this by providing one factory per platform. The client asks the factory for a `Button` — and always gets the right one for its platform, guaranteed to match every other widget the factory produces.

---

## Structure

```
          «interface»
         UIFactory
  ┌──────────────────────┐
  │ + createButton()     │
  │ + createCheckbox()   │
  │ + createTextField()  │
  └──────────┬───────────┘
             │
    ┌─────────┴──────────┐
    │                    │
WindowsFactory        MacFactory
┌─────────────┐    ┌─────────────┐
│createButton │    │createButton │──► MacButton
│  →WinButton │    │createCheckbox──► MacCheckbox
│createCheck. │    │createText.. │──► MacTextField
└─────────────┘    └─────────────┘

  «interface»        «interface»       «interface»
   Button             Checkbox          TextField
     ▲    ▲             ▲    ▲            ▲    ▲
WinButton MacButton  WinCheck MacCheck WinField MacField
```

### Participants

| Role | Responsibility |
|---|---|
| **AbstractFactory** | Declares creation methods for each distinct product type. |
| **ConcreteFactory** | Implements creation methods for one specific product family (e.g. Windows, Mac). |
| **AbstractProduct** | Declares the interface for a type of product object (e.g. `Button`). |
| **ConcreteProduct** | Implements the AbstractProduct for a specific family (e.g. `WindowsButton`). |
| **Client** | Uses only AbstractFactory and AbstractProduct interfaces — never concrete types. |

---

## Factory Method vs Abstract Factory

| | Factory Method | Abstract Factory |
|---|---|---|
| **Creates** | One product | A family of related products |
| **Mechanism** | Inheritance (subclass overrides a method) | Composition (client holds a factory object) |
| **Use when** | You want subclasses to choose the product | You need consistent families of products |
| **Extensibility** | Add a new subclass per new product | Add a new ConcreteFactory per new family |

---

## Code Example

### Java

The example models a **cross-platform UI toolkit** — buttons and checkboxes that must always match the host operating system.

```java
// ══════════════════════════════════════════════════════════════════
// ABSTRACT PRODUCTS
// ══════════════════════════════════════════════════════════════════

public interface Button {
    void render();
    void onClick();
}

public interface Checkbox {
    void render();
    void onToggle();
}

// ══════════════════════════════════════════════════════════════════
// CONCRETE PRODUCTS — Windows family
// ══════════════════════════════════════════════════════════════════

public class WindowsButton implements Button {
    @Override
    public void render() {
        System.out.println("[Windows] Rendering a flat, rectangular button.");
    }

    @Override
    public void onClick() {
        System.out.println("[Windows] Button clicked — ripple effect.");
    }
}

public class WindowsCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("[Windows] Rendering a square checkbox.");
    }

    @Override
    public void onToggle() {
        System.out.println("[Windows] Checkbox toggled — checkmark drawn.");
    }
}

// ══════════════════════════════════════════════════════════════════
// CONCRETE PRODUCTS — macOS family
// ══════════════════════════════════════════════════════════════════

public class MacButton implements Button {
    @Override
    public void render() {
        System.out.println("[Mac] Rendering a rounded, glossy button.");
    }

    @Override
    public void onClick() {
        System.out.println("[Mac] Button clicked — fade animation.");
    }
}

public class MacCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("[Mac] Rendering a rounded checkbox.");
    }

    @Override
    public void onToggle() {
        System.out.println("[Mac] Checkbox toggled — smooth slide animation.");
    }
}

// ══════════════════════════════════════════════════════════════════
// ABSTRACT FACTORY
// ══════════════════════════════════════════════════════════════════

public interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// ══════════════════════════════════════════════════════════════════
// CONCRETE FACTORIES
// ══════════════════════════════════════════════════════════════════

public class WindowsFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new WindowsButton();    // always returns Windows-family product
    }

    @Override
    public Checkbox createCheckbox() {
        return new WindowsCheckbox();  // always returns Windows-family product
    }
}

public class MacFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new MacButton();        // always returns Mac-family product
    }

    @Override
    public Checkbox createCheckbox() {
        return new MacCheckbox();      // always returns Mac-family product
    }
}

// ══════════════════════════════════════════════════════════════════
// CLIENT
// ══════════════════════════════════════════════════════════════════

// The Application class never references any concrete class.
// Swap the factory → entire UI family changes automatically.
public class Application {
    private final Button button;
    private final Checkbox checkbox;

    public Application(UIFactory factory) {
        // factory decides which concrete objects are created
        this.button   = factory.createButton();
        this.checkbox = factory.createCheckbox();
    }

    public void renderUI() {
        button.render();
        checkbox.render();
    }

    public void simulateInteraction() {
        button.onClick();
        checkbox.onToggle();
    }
}

// ══════════════════════════════════════════════════════════════════
// ENTRY POINT — factory selected based on OS at runtime
// ══════════════════════════════════════════════════════════════════

public class Main {
    public static void main(String[] args) {
        UIFactory factory = detectFactory();

        Application app = new Application(factory);
        app.renderUI();
        app.simulateInteraction();
    }

    private static UIFactory detectFactory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return new MacFactory();
        } else {
            return new WindowsFactory();   // default
        }
    }
}
```

**Output (on Windows):**
```
[Windows] Rendering a flat, rectangular button.
[Windows] Rendering a square checkbox.
[Windows] Button clicked — ripple effect.
[Windows] Checkbox toggled — checkmark drawn.
```

**Output (on macOS):**
```
[Mac] Rendering a rounded, glossy button.
[Mac] Rendering a rounded checkbox.
[Mac] Button clicked — fade animation.
[Mac] Checkbox toggled — smooth slide animation.
```

**Key observations in the Java code:**

- `UIFactory` is the **Abstract Factory** — it declares one creation method per product type.
- `WindowsFactory` and `MacFactory` are **Concrete Factories** — each guarantees all products it creates belong to the same family.
- `Application` (client) is injected with a factory and uses only `Button` and `Checkbox` interfaces — it has zero knowledge of `WindowsButton`, `MacButton`, etc.
- Switching the entire UI theme is a single-line change in `Main` — replace `WindowsFactory` with `MacFactory`.

---

## Adding a New Product Family

To add a **Linux** family, you only need to:

1. Create `LinuxButton implements Button`
2. Create `LinuxCheckbox implements Checkbox`
3. Create `LinuxFactory implements UIFactory`
4. Add `"linux"` detection in `detectFactory()`

The `Application` client and all existing code remain **completely unchanged** — a perfect demonstration of the Open/Closed Principle.

---

## When to Use

Use the Abstract Factory pattern when:

- Your code needs to work with **multiple families of related products**, but you don't want it to depend on the concrete classes of those products.
- You want to enforce that products from **one family are always used together** (preventing cross-family mismatches).
- You want to provide a **library of products** and only expose interfaces, not implementations.
- You need to **swap entire configurations** (e.g. themes, environments, platforms) at runtime or build time.

---

## When NOT to Use

- When you only have **one product family** — a simple Factory Method is sufficient.
- When adding new **product types** (not families) is frequent — Abstract Factory requires updating every factory interface and implementation.
- When the families are unlikely to change or grow — the abstraction adds complexity without benefit.

---

## Advantages

| Advantage | Description |
|---|---|
| **Consistency** | Guarantees that products from one factory are always compatible with each other. |
| **Open/Closed Principle** | Add new families by creating a new ConcreteFactory — existing code untouched. |
| **Single Responsibility** | Each ConcreteFactory owns the creation logic for its entire product family. |
| **Loose Coupling** | Client code depends only on abstract interfaces, never on concrete products. |
| **Easy Family Swap** | Replace one factory with another to change the entire product suite at once. |

---

## Disadvantages

- Adding a **new product type** (e.g. a `TextField`) requires changing the `AbstractFactory` interface and every `ConcreteFactory` — can be invasive.
- Can introduce a **large number of classes**: one interface and N concrete classes per product type, plus one factory per family.
- Overkill when only **one or two** product types exist.

---

## Relationship to Other Patterns

| Pattern | Relationship |
|---|---|
| **Factory Method** | Abstract Factory is often implemented using Factory Methods internally for each product. |
| **Builder** | Builder focuses on constructing complex objects step-by-step; Abstract Factory creates families in one shot. |
| **Prototype** | Abstract Factory can use Prototype to avoid subclassing — factories clone prototype instances instead. |
| **Singleton** | Concrete Factories are often Singletons since only one instance per family is needed. |
| **Facade** | Abstract Factory can act as a Facade that simplifies access to a subsystem of related objects. |

---

## Real-World Analogies

- **Furniture Store**: A store sells furniture sets (Modern, Victorian, Art Deco). When you order a Modern set, you get a matching sofa, chair, and table — all from the same design family.
- **Car Manufacturer**: An automobile factory produces engines, tyres, and interiors all matched for one car model. A sports car factory doesn't produce SUV parts.
- **Theme in an IDE**: Switching from "Light Theme" to "Dark Theme" swaps the entire family of colors, fonts, and icons at once — none picked individually.

---

## Real-World Usage in Libraries & Frameworks

- **Java AWT / Swing**: `Toolkit.getDefaultToolkit()` returns a platform-specific toolkit that creates native UI components.
- **JDBC**: `DriverManager.getConnection()` returns a database-specific `Connection`, which then produces matching `Statement` and `ResultSet` objects — all from the same DB family.
- **Spring Framework**: `ApplicationContext` acts as an Abstract Factory — it creates and wires beans that form a consistent, compatible application context.
- **Android**: `ViewGroup` and its subclasses act as factories for matched sets of UI views for a given layout type.
- **javax.xml.parsers**: `DocumentBuilderFactory` and `SAXParserFactory` create families of parser objects suited for their respective XML processing models.

---

## Summary

```
Abstract Factory in one sentence:
"One factory, a whole matched family — swap the factory, swap everything."
```

The Abstract Factory pattern shines when correctness depends on consistency across a group of related objects. It lifts the guarantee of compatibility from the programmer's discipline to the compiler — if it compiles and you injected the right factory, you're guaranteed a coherent product family. It is one of the most powerful tools in the object-oriented design arsenal, widely used in frameworks, cross-platform toolkits, and plugin architectures.

---

## Further Reading

- *Design Patterns: Elements of Reusable Object-Oriented Software* — GoF (Gamma, Helm, Johnson, Vlissides)
- [Refactoring Guru — Abstract Factory](https://refactoring.guru/design-patterns/abstract-factory)
- [SourceMaking — Abstract Factory](https://sourcemaking.com/design_patterns/abstract_factory)