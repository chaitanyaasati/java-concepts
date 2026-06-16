## PriorityQueue Important Methods in Java

> `PriorityQueue` is a class in Java (`java.util.PriorityQueue`).
> It orders elements based on **natural ordering** or a **custom comparator**.
> The **head** of the queue is always the **smallest** (min-heap by default).
> Can use queue interface here
```java
PriorityQueue<Integer> pq = new PriorityQueue<>();                          // min-heap
PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder()); // max-heap
PriorityQueue<String>  pq = new PriorityQueue<>(Comparator.comparingInt(String::length)); // custom
```

---

### 1. Insertion Methods

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `add(e)` | Inserts element | Yes (`IllegalStateException`) |
| `offer(e)` | Inserts element | No (returns `false`) |
```java
pq.add(10);
pq.offer(5);    // preferred
pq.offer(20);
// internal order: [5, 10, 20]
```

---

### 2. Retrieval (Peek) Methods — *Does NOT remove*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `peek()` | Returns head (smallest) element | No (returns `null`) |
| `element()` | Returns head (smallest) element | Yes (`NoSuchElementException`) |
```java
pq.peek();      // 5 — null if empty
pq.element();   // 5 — throws if empty
```

---

### 3. Removal Methods — *Removes the element*

| Method | Description | Throws Exception? |
|--------|-------------|-------------------|
| `poll()` | Removes & returns head | No (returns `null`) |
| `remove()` | Removes & returns head | Yes (`NoSuchElementException`) |
| `remove(obj)` | Removes a specific element | No (returns `true/false`) |
```java
pq.poll();       // removes & returns 5  — null if empty
pq.remove();     // removes & returns 10 — throws if empty
pq.remove(20);   // removes specific element 20
```

---

### 4. Search / Check Methods

| Method | Description |
|--------|-------------|
| `contains(obj)` | Returns `true` if element exists |
| `isEmpty()` | Returns `true` if queue is empty |
| `size()` | Returns number of elements |
```java
pq.contains(10); // true
pq.isEmpty();    // false
pq.size();       // 2
```

---

### 5. Conversion / Iteration Methods

| Method | Description |
|--------|-------------|
| `toArray()` | Converts to `Object[]` |
| `toArray(T[])` | Converts to typed array |
| `iterator()` | Returns iterator (**no guaranteed order**) |
| `forEach(action)` | Iterates with lambda (**no guaranteed order**) |
| `clear()` | Removes all elements |
```java
pq.toArray();
pq.toArray(new Integer[0]);
pq.forEach(n -> System.out.println(n));  // no guaranteed order
pq.clear();
```

---

### 6. Drain Methods
```java
// Correct way to process all elements in priority order
while (!pq.isEmpty()) {
    System.out.println(pq.poll()); // always retrieves in sorted order
}

// Stream — no guaranteed order
pq.stream().forEach(System.out::println);

// Sorted stream
pq.stream().sorted().forEach(System.out::println);
```

---

### 7. Size Constraint Pattern (Top K Elements)

> Java's `PriorityQueue` has no built-in max size, but this pattern is commonly used:
```java
int K = 3;
PriorityQueue<Integer> pq = new PriorityQueue<>(); // min-heap keeps top K largest

for (int num : new int[]{5, 1, 8, 3, 9, 2}) {
    pq.offer(num);
    if (pq.size() > K) {
        pq.poll(); // removes smallest, keeps top K largest
    }
}
// pq contains top 3 largest: [5, 8, 9]
```

---

### 8. Min-Heap vs Max-Heap

| Type | Declaration | Head Element |
|------|-------------|--------------|
| Min-Heap (default) | `new PriorityQueue<>()` | Smallest |
| Max-Heap | `new PriorityQueue<>(Comparator.reverseOrder())` | Largest |
| Custom | `new PriorityQueue<>(Comparator.comparingInt(...))` | Custom order |
```java
// Min-Heap
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(5); minHeap.offer(1); minHeap.offer(3);
minHeap.peek(); // 1

// Max-Heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
maxHeap.offer(5); maxHeap.offer(1); maxHeap.offer(3);
maxHeap.peek(); // 5
```

---

### Quick Comparison — Exception vs No Exception

| Operation | Throws Exception | No Exception (safe) |
|-----------|-----------------|----------------------|
| Insert | `add(e)` | `offer(e)` |
| Peek | `element()` | `peek()` |
| Remove Head | `remove()` | `poll()` |

> **Best Practice:** Always prefer `offer()`, `peek()`, and `poll()` to avoid unexpected exceptions.

> **Important:** `iterator()` and `forEach()` do **NOT** guarantee priority order.
> Always use **repeated `poll()`** to retrieve elements in sorted order.