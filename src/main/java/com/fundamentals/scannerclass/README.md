# Java Scanner Class — Essentials

## What is Scanner?

`Scanner` is a class in `java.util` package used to **read input** in Java — from the keyboard, a file, or a string. It is the most beginner-friendly way to take user input.

```java
import java.util.Scanner; // mandatory import
```

---

## Creating a Scanner

```java
// Read from keyboard
Scanner sc = new Scanner(System.in);

// Read from a String
Scanner sc = new Scanner("Hello 42 3.14");

// Read from a File
Scanner sc = new Scanner(new File("data.txt"));
```

---

## Reading Input — Core Methods

```java
Scanner sc = new Scanner(System.in);

String  word  = sc.next();        // reads one word (stops at space)
String  line  = sc.nextLine();    // reads full line (including spaces)
int     num   = sc.nextInt();     // reads integer
double  d     = sc.nextDouble();  // reads decimal number
long    l     = sc.nextLong();    // reads long
boolean b     = sc.nextBoolean(); // reads true or false
float   f     = sc.nextFloat();   // reads float
```

### Example
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = sc.nextLine();

        System.out.print("Enter your age: ");
        int age = sc.nextInt();

        System.out.println("Hello " + name + ", you are " + age + " years old.");
        sc.close();
    }
}
```

---

## next() vs nextLine()

| Method | Reads | Includes Spaces |
|---|---|---|
| `next()` | One word | No |
| `nextLine()` | Entire line | Yes |

```java
Scanner sc = new Scanner(System.in);
// Input: Hello World

String word = sc.next();     // "Hello" — stops at space
String line = sc.nextLine(); // "Hello World" — reads full line
```

---

## ⚠️ The Most Common Bug — nextInt() + nextLine()

When you use `nextInt()` (or any `nextXxx()`) followed by `nextLine()`, the `nextLine()` reads the leftover newline `\n` and returns an **empty string**.

```java
Scanner sc = new Scanner(System.in);

int age = sc.nextInt();      // reads 25, leaves "\n" behind
String name = sc.nextLine(); // picks up "\n" — reads EMPTY string!
```

**Fix — add an extra `sc.nextLine()` to consume the leftover newline:**
```java
Scanner sc = new Scanner(System.in);

int age = sc.nextInt();
sc.nextLine();           // ← flush the leftover newline

String name = sc.nextLine(); // now reads correctly
```

---

## Checking Before Reading — hasNext() Methods

Always check if input is available before reading to avoid `NoSuchElementException`.

```java
sc.hasNext()        // has any next token?
sc.hasNextLine()    // has another line?
sc.hasNextInt()     // next token is an int?
sc.hasNextDouble()  // next token is a double?
```

### Example — Read Until No More Input
```java
Scanner sc = new Scanner(System.in);

while (sc.hasNextInt()) {
    int n = sc.nextInt();
    System.out.println("Read: " + n);
}
```

### Example — Input Validation
```java
Scanner sc = new Scanner(System.in);

System.out.print("Enter a number: ");
while (!sc.hasNextInt()) {
    System.out.println("Not a number. Try again: ");
    sc.next(); // discard the invalid input
}
int num = sc.nextInt();
System.out.println("You entered: " + num);
```

---

## Reading from a String

Useful for parsing structured data without needing user input.

```java
Scanner sc = new Scanner("Alice 30 95.5");

String name  = sc.next();        // Alice
int    age   = sc.nextInt();     // 30
double score = sc.nextDouble();  // 95.5

System.out.println(name + " | " + age + " | " + score);
sc.close();
```

---

## Custom Delimiter

By default, Scanner splits on **whitespace**. You can change it using `useDelimiter()`.

```java
Scanner sc = new Scanner("apple,banana,cherry");
sc.useDelimiter(",");

while (sc.hasNext()) {
    System.out.println(sc.next());
}
// apple
// banana
// cherry
sc.close();
```

---

## Closing the Scanner

Always close the Scanner after use to free resources. The best approach is **try-with-resources**.

```java
// Manual close
sc.close();

// Preferred — auto-closes even if exception occurs
try (Scanner sc = new Scanner(new File("data.txt"))) {
    while (sc.hasNextLine()) {
        System.out.println(sc.nextLine());
    }
} catch (FileNotFoundException e) {
    e.printStackTrace();
}
```

> **Note:** Avoid calling `sc.close()` on `System.in` in small programs — it permanently closes standard input for the entire session.

---

## Common Exceptions

| Exception | Cause | Fix |
|---|---|---|
| `NoSuchElementException` | Reading when no input is left | Use `hasNext()` before reading |
| `InputMismatchException` | Type mismatch e.g. `nextInt()` on `"abc"` | Validate with `hasNextInt()` or use try-catch |
| `IllegalStateException` | Reading after `sc.close()` | Don't read from a closed scanner |

---

## Quick Reference

```java
Scanner sc = new Scanner(System.in);

sc.next()          // one word
sc.nextLine()      // full line
sc.nextInt()       // integer
sc.nextDouble()    // decimal
sc.nextBoolean()   // true / false
sc.nextLong()      // long number

sc.hasNext()       // check before next()
sc.hasNextLine()   // check before nextLine()
sc.hasNextInt()    // check before nextInt()

sc.useDelimiter(",") // change separator
sc.close()           // release resources
```

### Common Pitfalls at a Glance

| Pitfall | Fix |
|---|---|
| `nextLine()` reads empty after `nextInt()` | Add `sc.nextLine()` after `nextInt()` |
| Wrong type crashes program | Use `hasNextInt()` to validate first |
| No more input — program crashes | Use `hasNext()` before every read |
| Decimal parsing fails in some regions | Use `sc.useLocale(Locale.US)` |