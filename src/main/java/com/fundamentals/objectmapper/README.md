# ObjectMapper — Essentials

## What is ObjectMapper?

`ObjectMapper` is the core class of the **Jackson** library used to **convert between Java objects and JSON**. It is the most widely used JSON processing tool in Java.

- **Serialization** → Java Object to JSON
- **Deserialization** → JSON to Java Object

### Add Jackson Dependency

**Maven:**
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
```

```java
import com.fasterxml.jackson.databind.ObjectMapper;
```

> **Best Practice:** Create **one shared instance** of `ObjectMapper` — it is thread-safe and expensive to create.

```java
ObjectMapper mapper = new ObjectMapper(); // reuse this
```

---

## The Java Class Used in Examples

```java
public class User {
    private String name;
    private int age;
    private String email;

    // Constructors
    public User() {}  // ← required for deserialization
    public User(String name, int age, String email) {
        this.name  = name;
        this.age   = age;
        this.email = email;
    }

    // Getters and Setters (required by Jackson)
    public String getName()        { return name; }
    public void   setName(String n) { this.name = n; }
    public int    getAge()          { return age; }
    public void   setAge(int a)     { this.age = a; }
    public String getEmail()        { return email; }
    public void   setEmail(String e){ this.email = e; }
}
```

> Jackson needs either **getters/setters** or **public fields** to work by default.

---

## 1. Serialization — Java Object → JSON

### Object to JSON String
```java
ObjectMapper mapper = new ObjectMapper();
User user = new User("Alice", 30, "alice@email.com");

String json = mapper.writeValueAsString(user);
System.out.println(json);
// {"name":"Alice","age":30,"email":"alice@email.com"}
```

### Object to Pretty-Printed JSON String
```java
String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                          .writeValueAsString(user);
System.out.println(prettyJson);
// {
//   "name" : "Alice",
//   "age" : 30,
//   "email" : "alice@email.com"
// }
```

### Object to JSON File
```java
mapper.writeValue(new File("user.json"), user);
```

### Object to byte array
```java
byte[] bytes = mapper.writeValueAsBytes(user);
```

---

## 2. Deserialization — JSON → Java Object

### JSON String to Object
```java
String json = "{\"name\":\"Alice\",\"age\":30,\"email\":\"alice@email.com\"}";

User user = mapper.readValue(json, User.class);
System.out.println(user.getName()); // Alice
System.out.println(user.getAge());  // 30
```

### JSON File to Object
```java
User user = mapper.readValue(new File("user.json"), User.class);
```

### JSON URL to Object
```java
User user = mapper.readValue(new URL("https://api.example.com/user/1"), User.class);
```

### JSON byte array to Object
```java
User user = mapper.readValue(bytes, User.class);
```

---

## 3. Working with Collections

### JSON Array → List
```java
String json = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";

List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});
users.forEach(u -> System.out.println(u.getName()));
// Alice
// Bob
```

### JSON Object → Map
```java
String json = "{\"city\":\"Mumbai\",\"country\":\"India\"}";

Map<String, String> map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
System.out.println(map.get("city")); // Mumbai
```

### List → JSON
```java
List<User> users = List.of(
    new User("Alice", 30, "alice@email.com"),
    new User("Bob",   25, "bob@email.com")
);

String json = mapper.writeValueAsString(users);
// [{"name":"Alice","age":30,...},{"name":"Bob","age":25,...}]
```

> **Why `TypeReference`?** Due to Java's type erasure, `List<User>.class` doesn't exist at runtime. `TypeReference` preserves the generic type information Jackson needs.

---

## 4. JsonNode — Working with Unknown JSON

When you don't have a matching Java class, use `JsonNode` to navigate JSON like a tree.

```java
String json = "{\"name\":\"Alice\",\"address\":{\"city\":\"Mumbai\",\"zip\":\"400001\"}}";

JsonNode root = mapper.readTree(json);

String name = root.get("name").asText();           // Alice
String city = root.get("address").get("city").asText(); // Mumbai
int    zip  = root.get("address").get("zip").asInt();   // 400001

// Check if field exists before accessing
if (root.has("email")) {
    System.out.println(root.get("email").asText());
}
```

### JsonNode Useful Methods

| Method | Description |
|---|---|
| `get("key")` | Get child node by field name |
| `get(index)` | Get element from array node |
| `asText()` | Value as String |
| `asInt()` | Value as int |
| `asDouble()` | Value as double |
| `asBoolean()` | Value as boolean |
| `has("key")` | Check if field exists |
| `isNull()` | Check if value is null |
| `isArray()` | Check if node is array |
| `isObject()` | Check if node is object |

---

## 5. Important Annotations

Annotations let you control how Jackson serializes and deserializes fields.

### `@JsonProperty` — Custom Field Name
```java
public class User {
    @JsonProperty("full_name")  // JSON key will be "full_name"
    private String name;

    @JsonProperty("user_age")
    private int age;
}
// {"full_name":"Alice","user_age":30}
```

### `@JsonIgnore` — Skip a Field
```java
public class User {
    private String name;

    @JsonIgnore  // password won't appear in JSON output
    private String password;
}
// {"name":"Alice"}
```

### `@JsonIgnoreProperties` — Ignore Unknown Fields
Prevents `UnrecognizedPropertyException` when JSON has extra fields your class doesn't have.
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String name;
    private int age;
    // JSON may have more fields — they will be silently ignored
}
```

### `@JsonInclude` — Exclude Null Values
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String name;
    private String email; // if null, won't appear in JSON
}
// email=null → {"name":"Alice"}  (email omitted)
```

### `@JsonAlias` — Accept Multiple JSON Key Names
```java
public class User {
    @JsonAlias({"full_name", "username", "name"})
    private String name; // accepts any of these keys during deserialization
}
```

### `@JsonFormat` — Format Date/Time
```java
public class Event {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date eventDate;
}
// {"eventDate":"25-12-2024"}
```

---

## 6. Configuring ObjectMapper

### Ignore Unknown Properties Globally
```java
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
// same as @JsonIgnoreProperties(ignoreUnknown=true) but applied globally
```

### Allow Serialization of Empty Objects
```java
mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
```

### Serialize Dates as Timestamps vs ISO String
```java
// As timestamp (default): 1735084800000
mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);

// As ISO string: "2024-12-25T00:00:00.000+00:00"
mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
mapper.registerModule(new JavaTimeModule()); // needed for java.time types
```

### Pretty Print Globally
```java
mapper.enable(SerializationFeature.INDENT_OUTPUT);
String json = mapper.writeValueAsString(user); // always pretty printed
```

---

## 7. Converting Between Object and Map

Useful to convert an object to a `Map` for dynamic field access, or vice versa.

```java
User user = new User("Alice", 30, "alice@email.com");

// Object → Map
Map<String, Object> map = mapper.convertValue(user, new TypeReference<Map<String, Object>>() {});
System.out.println(map.get("name")); // Alice
System.out.println(map.get("age"));  // 30

// Map → Object
Map<String, Object> data = new HashMap<>();
data.put("name", "Bob");
data.put("age", 25);
data.put("email", "bob@email.com");

User converted = mapper.convertValue(data, User.class);
System.out.println(converted.getName()); // Bob
```

---

## 8. Common Exceptions

| Exception | Cause | Fix |
|---|---|---|
| `UnrecognizedPropertyException` | JSON has a field your class doesn't | Add `@JsonIgnoreProperties(ignoreUnknown = true)` |
| `InvalidDefinitionException` | Missing no-arg constructor or getters/setters | Add `public NoArgConstructor()` and getters/setters |
| `JsonParseException` | Malformed JSON string | Validate JSON format |
| `MismatchedInputException` | Type mismatch e.g. string where int expected | Check JSON structure matches Java class |

---

## Quick Reference

```java
ObjectMapper mapper = new ObjectMapper();

// Serialize
mapper.writeValueAsString(obj)                          // Object → JSON string
mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) // pretty JSON
mapper.writeValue(new File("f.json"), obj)              // Object → File

// Deserialize
mapper.readValue(jsonString, MyClass.class)             // JSON string → Object
mapper.readValue(new File("f.json"), MyClass.class)     // File → Object
mapper.readValue(json, new TypeReference<List<T>>() {}) // JSON array → List

// Tree model
mapper.readTree(json)                                   // JSON → JsonNode

// Convert
mapper.convertValue(obj, new TypeReference<Map<String,Object>>() {}) // Object → Map
mapper.convertValue(map, MyClass.class)                 // Map → Object

// Config
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
mapper.enable(SerializationFeature.INDENT_OUTPUT)
```

### Essential Annotations

| Annotation | Purpose |
|---|---|
| `@JsonProperty("key")` | Custom JSON field name |
| `@JsonIgnore` | Exclude a field from JSON |
| `@JsonIgnoreProperties(ignoreUnknown = true)` | Ignore extra fields in JSON |
| `@JsonInclude(NON_NULL)` | Skip null fields in output |
| `@JsonAlias({"a","b"})` | Accept multiple key names |
| `@JsonFormat(pattern="...")` | Format dates |