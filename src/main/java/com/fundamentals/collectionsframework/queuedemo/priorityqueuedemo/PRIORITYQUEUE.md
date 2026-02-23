## Priority Queue Important Methods

### 1. Insertion Methods

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `add(e)` | Inserts element | Yes, if capacity restricted |
| `offer(e)` | Inserts element | No (returns `false`) |
```java
stringLength.add("banana");
stringLength.offer("hi");      // preferred for PriorityQueue
```

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `peek()` | Returns head element | No (returns `null`) |
| `element()` | Returns head element | Yes (`NoSuchElementException`) |
```java
stringLength.peek();      // "hi" (shortest string) — returns null if empty
stringLength.element();   // "hi" — throws if empty
```

---

### 3. Removal Methods — *Removes the element*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `poll()` | Removes & returns head | No (returns `null`) |
| `remove()` | Removes & returns head | Yes (`NoSuchElementException`) |
| `remove(obj)` | Removes a specific element | No (returns `true/false`) |
```java
stringLength.poll();           // removes & returns "hi"
stringLength.remove();         // removes & returns head — throws if empty
stringLength.remove("banana"); // removes specific element
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns number of elements |
```java
stringLength.contains("banana"); // true
stringLength.isEmpty();          // false
stringLength.size();             // 1
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` |
| `toArray(T[])` | Converts to typed array |
| `iterator()` | Returns iterator (**no guaranteed order**) |
| `forEach(action)` | Iterates with lambda |
| `clear()` | Removes all elements |
```java
stringLength.toArray();
stringLength.toArray(new String[0]);
stringLength.forEach(s -> System.out.println(s));
stringLength.clear();
```

---

### 6. Drain / Stream Methods
```java
// Drain in priority order (correct way to iterate in order)
while (!stringLength.isEmpty()) {
    System.out.println(stringLength.poll());
}

// Stream (no guaranteed order)
stringLength.stream().forEach(System.out::println);
```

---

> **Note:** `iterator()` and `forEach()` do **NOT** guarantee priority order.  
> Always use **repeated `poll()`** to get elements in sorted order.

### 7. Other info

Java's built-in PriorityQueue does not have a built-in max size limit — the initial capacity parameter is just the initial internal array size, not a hard limit.
```java
// This is just internal array size hint, NOT a max size
PriorityQueue<Integer> pq = new PriorityQueue<>(10); // can still grow beyond 10
```