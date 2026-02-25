# Generics in Java — Important Concepts

## Overview

**Generics** were introduced in Java 5 to provide **compile-time type safety** while writing reusable, type-independent code. They allow classes, interfaces, and methods to operate on **parameterized types** — meaning the actual type is specified when the class or method is used, not when it is defined.

**Without Generics (pre-Java 5):**
```java
List list = new ArrayList();
list.add("hello");
String s = (String) list.get(0); // explicit cast required
list.add(42);                     // no compile error — runtime ClassCastException risk
```

**With Generics:**
```java
List<String> list = new ArrayList<>();
list.add("hello");
String s = list.get(0); // no cast needed
list.add(42);           // compile-time error — type safety enforced
```

---

## 1. Why Use Generics?

| Benefit | Description |
|---|---|
| **Type Safety** | Errors caught at compile time, not runtime |
| **No Casting** | Eliminates explicit type casting |
| **Code Reusability** | Write once, use with any type |
| **Cleaner Code** | More readable and maintainable |
| **Generic Algorithms** | Algorithms that work on collections of different types |

---

## 2. Generic Classes

A **generic class** is defined with one or more type parameters inside angle brackets `<T>`.

### Syntax
```java
class ClassName<T> {
    // T is a placeholder for any type
}
```

### Example
```java
public class Box<T> {
    private T value;

    public Box(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

// Usage
Box<String>  stringBox  = new Box<>("Hello");
Box<Integer> intBox     = new Box<>(42);
Box<Double>  doubleBox  = new Box<>(3.14);

System.out.println(stringBox.getValue());  // Hello
System.out.println(intBox.getValue());     // 42
```

### Multiple Type Parameters
```java
public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key   = key;
        this.value = value;
    }

    public K getKey()   { return key; }
    public V getValue() { return value; }
}

// Usage
Pair<String, Integer> pair = new Pair<>("age", 30);
System.out.println(pair.getKey() + " = " + pair.getValue()); // age = 30
```

---

## 3. Generic Interfaces

Interfaces can also be parameterized with type parameters.

```java
public interface Printable<T> {
    void print(T item);
}

// Implementation
public class StringPrinter implements Printable<String> {
    @Override
    public void print(String item) {
        System.out.println("Printing: " + item);
    }
}

// Generic implementation
public class GenericPrinter<T> implements Printable<T> {
    @Override
    public void print(T item) {
        System.out.println("Item: " + item);
    }
}
```

---

## 4. Generic Methods

A **generic method** has its own type parameter, independent of the class it belongs to.

### Syntax
```java
public <T> returnType methodName(T param) { }
```

### Example
```java
public class Utils {

    // Generic method
    public static <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    // Generic method returning a value
    public static <T> T getFirst(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    // Generic method with multiple type parameters
    public static <K, V> Map<V, K> invertMap(Map<K, V> original) {
        Map<V, K> inverted = new HashMap<>();
        for (Map.Entry<K, V> entry : original.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return inverted;
    }
}

// Usage
Integer[] nums    = {1, 2, 3, 4, 5};
String[]  strings = {"a", "b", "c"};

Utils.printArray(nums);    // 1 2 3 4 5
Utils.printArray(strings); // a b c

List<String> names = List.of("Alice", "Bob", "Charlie");
System.out.println(Utils.getFirst(names)); // Alice
```

---

## 5. Type Parameter Naming Conventions

By convention, single uppercase letters are used for type parameters:

| Letter | Meaning | Common Usage |
|---|---|---|
| `T` | Type | General purpose type |
| `E` | Element | Collections (`List<E>`) |
| `K` | Key | Maps (`Map<K, V>`) |
| `V` | Value | Maps (`Map<K, V>`) |
| `N` | Number | Numeric types |
| `R` | Result | Return types in functions |
| `S`, `U`, `W` | 2nd, 3rd, 4th types | Multiple type parameters |

---

## 6. Bounded Type Parameters

**Bounded types** restrict the types that can be used as type arguments.

### Upper Bounded — `extends`
Restricts to a type **or its subclasses**.

```java
// T must be Number or a subclass of Number
public static <T extends Number> double sum(List<T> list) {
    double total = 0;
    for (T item : list) {
        total += item.doubleValue();
    }
    return total;
}

// Usage
List<Integer> ints    = List.of(1, 2, 3, 4, 5);
List<Double>  doubles = List.of(1.1, 2.2, 3.3);

System.out.println(sum(ints));    // 15.0
System.out.println(sum(doubles)); // 6.6
```

### Multiple Bounds
A type parameter can extend multiple interfaces (but only one class).

```java
// T must implement both Comparable and Serializable
public <T extends Comparable<T> & Serializable> T findMax(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

> **Note:** When combining a class and interfaces in bounds, the class must come **first**.
> ```java
> <T extends MyClass & InterfaceA & InterfaceB>  // valid
> <T extends InterfaceA & MyClass & InterfaceB>  // compile error
> ```

---

## 7. Wildcards

Wildcards (`?`) represent an **unknown type** and are used primarily in method parameters.

### 7.1 Unbounded Wildcard — `<?>`
Accepts a list of **any type**. Used when the type doesn't matter.

```java
public static void printList(List<?> list) {
    for (Object elem : list) {
        System.out.println(elem);
    }
}

// Works with any type
printList(List.of(1, 2, 3));
printList(List.of("a", "b", "c"));
printList(List.of(1.1, 2.2));
```

### 7.2 Upper Bounded Wildcard — `<? extends T>`
Accepts a type **T or any subtype of T**. Used for **reading** (producer).

```java
// Accepts List<Number>, List<Integer>, List<Double>, etc.
public static double sumList(List<? extends Number> list) {
    double total = 0;
    for (Number n : list) {
        total += n.doubleValue();
    }
    return total;
}

sumList(List.of(1, 2, 3));       // Integer extends Number
sumList(List.of(1.5, 2.5, 3.5)); // Double extends Number
```

### 7.3 Lower Bounded Wildcard — `<? super T>`
Accepts a type **T or any supertype of T**. Used for **writing** (consumer).

```java
// Accepts List<Integer>, List<Number>, List<Object>
public static void addNumbers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
    list.add(3);
}

List<Number> numbers = new ArrayList<>();
addNumbers(numbers); // valid — Number is supertype of Integer
```

### Wildcard Summary (PECS Principle)

> **PECS — Producer Extends, Consumer Super**

| Wildcard | Direction | Use when... |
|---|---|---|
| `<? extends T>` | **Producer** (read) | You only **get** items from the structure |
| `<? super T>` | **Consumer** (write) | You only **put** items into the structure |
| `<?>` | Neither | You only **read as Object**, no writes |

```java
// PECS Example
public static <T> void copy(List<? extends T> src, List<? super T> dest) {
    for (T item : src) {
        dest.add(item);
    }
}

List<Integer> source = List.of(1, 2, 3);
List<Number>  target = new ArrayList<>();
copy(source, target);
System.out.println(target); // [1, 2, 3]
```

---

## 8. Type Erasure

**Type erasure** is the process by which the Java compiler removes all generic type information at runtime. Generics exist **only at compile time** — the JVM sees raw types at runtime.

```java
List<String>  strList = new ArrayList<>();
List<Integer> intList = new ArrayList<>();

// Both have the same class at runtime
System.out.println(strList.getClass() == intList.getClass()); // true
System.out.println(strList.getClass()); // class java.util.ArrayList
```

### What happens during erasure:
- `List<T>` becomes `List`
- `<T extends Number>` becomes `Number`
- `<T>` (unbounded) becomes `Object`
- Bridge methods are inserted by the compiler when needed

### Consequences of Type Erasure

```java
// Cannot create instances of type parameters
public <T> T createInstance() {
    return new T(); // COMPILE ERROR — type erased at runtime
}

// Cannot use instanceof with parameterized types
if (list instanceof List<String>) { } // COMPILE ERROR

// Cannot create generic arrays
T[] array = new T[10]; // COMPILE ERROR

// Workaround using Class<T>
public <T> T createInstance(Class<T> clazz) throws Exception {
    return clazz.getDeclaredConstructor().newInstance();
}
```

---

## 9. Raw Types

A **raw type** is using a generic class without specifying its type parameter. It exists for backward compatibility but should be avoided.

```java
// Raw type — avoid in modern code
List rawList = new ArrayList();
rawList.add("hello");
rawList.add(42);          // no warning about mixing types
String s = (String) rawList.get(1); // ClassCastException at runtime!

// Preferred — use generic type
List<String> typedList = new ArrayList<>();
```

> **Warning:** Using raw types disables all generic type checks and can lead to `ClassCastException` at runtime.

---

## 10. Generic Restrictions (What You Cannot Do)

### Cannot instantiate type parameters
```java
public <T> void method() {
    T obj = new T(); // ERROR
}
```

### Cannot create generic arrays
```java
List<String>[] arr = new ArrayList<String>[10]; // ERROR
// Workaround:
List<String>[] arr = new ArrayList[10];          // unchecked warning but compiles
```

### Cannot use primitives as type arguments
```java
List<int>    list = new ArrayList<>(); // ERROR
List<Integer> list = new ArrayList<>(); // OK — use wrapper types
```

### Cannot use instanceof with parameterized types
```java
if (obj instanceof List<String>) { } // ERROR
if (obj instanceof List<?>)      { } // OK — unbounded wildcard is allowed
```

### Cannot declare static fields of type parameter
```java
public class MyClass<T> {
    private static T instance; // ERROR — static context doesn't know T
}
```

### Cannot catch or throw generic types
```java
public class GenericException<T> extends Exception { } // ERROR — cannot subclass Throwable generically

public <T extends Exception> void method() throws T { } // OK in method signature
```

---

## 11. Generic Classes and Inheritance

### A Generic Class Can Extend Another Generic Class
```java
public class NumberBox<T extends Number> extends Box<T> {
    public double doubleValue() {
        return getValue().doubleValue();
    }
}
```

### Subtype Relationship with Generics
```java
// Integer IS-A Number — but...
List<Integer> intList = new ArrayList<>();
List<Number>  numList = intList; // COMPILE ERROR — List<Integer> is NOT a List<Number>

// Use wildcards instead:
List<? extends Number> numList2 = intList; // OK
```

> **Important:** `List<Integer>` is **not** a subtype of `List<Number>`, even though `Integer` is a subtype of `Number`. This is because generics are **invariant** by default.

---

## 12. Comparable with Generics

Generics are heavily used with `Comparable` for writing type-safe sorting logic.

```java
public static <T extends Comparable<T>> T findMax(List<T> list) {
    if (list == null || list.isEmpty()) throw new IllegalArgumentException("Empty list");

    T max = list.get(0);
    for (T item : list) {
        if (item.compareTo(max) > 0) {
            max = item;
        }
    }
    return max;
}

// Usage
System.out.println(findMax(List.of(3, 1, 4, 1, 5, 9)));         // 9
System.out.println(findMax(List.of("banana", "apple", "cherry"))); // cherry
```

---

## 13. Generic Stack — Full Example

A complete example combining generic class, bounded types, and methods:

```java
public class GenericStack<T> {
    private LinkedList<T> stack = new LinkedList<>();

    public void push(T item) {
        stack.addFirst(item);
    }

    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return stack.removeFirst();
    }

    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return stack.getFirst();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    // Generic method within generic class
    public <R extends Comparable<R>> R findMax(Function<T, R> mapper) {
        return stack.stream()
                    .map(mapper)
                    .max(Comparator.naturalOrder())
                    .orElseThrow();
    }
}

// Usage
GenericStack<String> strStack = new GenericStack<>();
strStack.push("apple");
strStack.push("banana");
strStack.push("cherry");

System.out.println(strStack.peek()); // cherry
System.out.println(strStack.pop());  // cherry
System.out.println(strStack.size()); // 2

GenericStack<Integer> intStack = new GenericStack<>();
intStack.push(10);
intStack.push(30);
intStack.push(20);

System.out.println(intStack.findMax(x -> x)); // 30
```

---

## 14. Generics with Functional Interfaces (Java 8+)

Generics integrate seamlessly with functional interfaces and the Stream API.

```java
// Generic functional interface
@FunctionalInterface
public interface Transformer<T, R> {
    R transform(T input);
}

// Usage
Transformer<String, Integer> strLen = str -> str.length();
System.out.println(strLen.transform("hello")); // 5

Transformer<Integer, String> intToStr = num -> "Number: " + num;
System.out.println(intToStr.transform(42)); // Number: 42

// With streams
List<String> words = List.of("apple", "banana", "cherry", "date");
List<Integer> lengths = words.stream()
                             .map(strLen::transform)
                             .collect(Collectors.toList());
System.out.println(lengths); // [5, 6, 6, 4]
```

---

## 15. Common Generic Patterns

### Generic Singleton Factory
```java
public class GenericSingleton<T> {
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz) throws Exception {
        return (T) instances.computeIfAbsent(clazz,
            c -> { try { return c.getDeclaredConstructor().newInstance(); }
                   catch (Exception e) { throw new RuntimeException(e); } });
    }
}
```

### Generic Repository Pattern
```java
public interface Repository<T, ID> {
    void   save(T entity);
    T      findById(ID id);
    List<T> findAll();
    void   delete(ID id);
}

public class UserRepository implements Repository<User, Long> {
    private Map<Long, User> store = new HashMap<>();

    @Override public void    save(User user)  { store.put(user.getId(), user); }
    @Override public User    findById(Long id) { return store.get(id); }
    @Override public List<User> findAll()      { return new ArrayList<>(store.values()); }
    @Override public void    delete(Long id)  { store.remove(id); }
}
```

### Generic Builder Pattern
```java
public abstract class Builder<T, B extends Builder<T, B>> {
    @SuppressWarnings("unchecked")
    protected B self() { return (B) this; }
    public abstract T build();
}

public class PersonBuilder extends Builder<Person, PersonBuilder> {
    private String name;
    private int    age;

    public PersonBuilder name(String name) { this.name = name; return self(); }
    public PersonBuilder age(int age)      { this.age  = age;  return self(); }

    @Override
    public Person build() { return new Person(name, age); }
}
```

---

## 16. Quick Reference Summary

### Generics Cheatsheet

| Concept | Syntax | Description |
|---|---|---|
| Generic class | `class Box<T>` | Class with type parameter |
| Generic interface | `interface Repo<T, ID>` | Interface with type parameters |
| Generic method | `<T> void print(T item)` | Method with its own type parameter |
| Upper bound | `<T extends Number>` | T must be Number or subclass |
| Lower bound | `<? super Integer>` | Unknown type that is Integer or supertype |
| Unbounded wildcard | `<?>` | Any type (read-only as Object) |
| Upper bounded wildcard | `<? extends T>` | Read from structure (Producer) |
| Lower bounded wildcard | `<? super T>` | Write to structure (Consumer) |
| Multiple bounds | `<T extends A & B>` | T must extend A and implement B |
| Type erasure | — | Generic info removed at runtime |
| Raw type | `List list` | Unparameterized — avoid in modern code |

### Key Rules to Remember

1. Generics are **invariant** — `List<Integer>` is NOT a `List<Number>`
2. Wildcards make generics **covariant** (`? extends`) or **contravariant** (`? super`)
3. All generic type info is **erased at runtime** — you cannot check `instanceof List<String>`
4. **Cannot use primitives** as type arguments — use wrapper types (`Integer`, `Double`, etc.)
5. **PECS**: Producer Extends, Consumer Super
6. **Cannot instantiate** type parameters directly — use `Class<T>` as a workaround
7. **Static members** cannot reference type parameters of the enclosing class
8. **Generic arrays** cannot be created — use collections or `Object[]` with casts