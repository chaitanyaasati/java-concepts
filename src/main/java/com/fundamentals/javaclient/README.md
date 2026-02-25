# Java HttpClient — Essentials

## What is HttpClient?

`HttpClient` is a **built-in Java library** (introduced in **Java 11**) for making HTTP requests. No external dependency needed — just import and use.

- Supports `GET`, `POST`, `PUT`, `DELETE`, and other HTTP methods
- Supports both **synchronous** (blocking) and **asynchronous** (non-blocking) calls
- Supports **HTTPS**, headers, timeouts, and request bodies
- Pairs perfectly with **Jackson ObjectMapper** to parse JSON responses

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
```

---

## Core Building Blocks

Every HTTP call with `HttpClient` has three parts:

```
HttpClient  →  sends the request
HttpRequest →  defines what to send (URL, method, headers, body)
HttpResponse → holds what came back (status code, body)
```

### Create HttpClient
```java
// Simple client
HttpClient client = HttpClient.newHttpClient();

// Client with timeout and redirect policy
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build();
```

---

## 1. GET Request

### Basic GET
```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users/1"))
    .GET()  // default — can be omitted
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println("Status : " + response.statusCode()); // 200
System.out.println("Body   : " + response.body());       // JSON string
```

### GET with Headers
```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/users"))
    .header("Authorization", "Bearer your_token_here")
    .header("Accept", "application/json")
    .GET()
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

### GET + Parse JSON with ObjectMapper
```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {
    public int    id;
    public String name;
    public String email;
}

HttpClient     client  = HttpClient.newHttpClient();
ObjectMapper   mapper  = new ObjectMapper();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users/1"))
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

User user = mapper.readValue(response.body(), User.class);
System.out.println(user.name);  // Leanne Graham
System.out.println(user.email); // Sincere@april.biz
```

---

## 2. POST Request

### POST with JSON Body
```java
ObjectMapper mapper = new ObjectMapper();

// Java object to send
User newUser   = new User();
newUser.name  = "Alice";
newUser.email = "alice@example.com";

String requestBody = mapper.writeValueAsString(newUser);

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println("Status : " + response.statusCode()); // 201 Created
System.out.println("Body   : " + response.body());
```

---

## 3. PUT Request

```java
User updatedUser  = new User();
updatedUser.name  = "Alice Updated";
updatedUser.email = "alice_new@example.com";

String requestBody = mapper.writeValueAsString(updatedUser);

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users/1"))
    .header("Content-Type", "application/json")
    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println("Status: " + response.statusCode()); // 200 OK
```

---

## 4. DELETE Request

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users/1"))
    .DELETE()
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println("Status: " + response.statusCode()); // 200 OK
```

---

## 5. Async Request (Non-Blocking)

Use `sendAsync()` when you don't want to block the current thread while waiting for the response.

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users/1"))
    .build();

client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    .thenApply(HttpResponse::body)
    .thenAccept(body -> System.out.println("Response: " + body))
    .join(); // wait for completion (in main thread demo)
```

---

## 6. Timeout Handling

```java
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5)) // connection timeout
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .timeout(Duration.ofSeconds(10))       // request timeout
    .build();

try {
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println(response.body());
} catch (HttpTimeoutException e) {
    System.out.println("Request timed out: " + e.getMessage());
}
```

---

## 7. Handling the Response

```java
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

// Status code
int status = response.statusCode(); // 200, 201, 404, 500, etc.

// Response body as String
String body = response.body();

// Response headers
String contentType = response.headers()
                              .firstValue("Content-Type")
                              .orElse("unknown");

// Check success
if (status >= 200 && status < 300) {
    System.out.println("Success: " + body);
} else if (status == 404) {
    System.out.println("Not Found");
} else if (status >= 500) {
    System.out.println("Server Error");
}
```

---

## 8. BodyPublishers and BodyHandlers

### BodyPublishers — What to send in the request body

| Publisher | Usage |
|---|---|
| `BodyPublishers.ofString(str)` | Send a plain string or JSON |
| `BodyPublishers.ofFile(path)` | Send a file |
| `BodyPublishers.ofByteArray(bytes)` | Send raw bytes |
| `BodyPublishers.noBody()` | No body (GET, DELETE) |

### BodyHandlers — How to read the response body

| Handler | Usage |
|---|---|
| `BodyHandlers.ofString()` | Read response as String |
| `BodyHandlers.ofFile(path)` | Save response to a file |
| `BodyHandlers.ofByteArray()` | Read response as byte[] |
| `BodyHandlers.discarding()` | Ignore response body |

---

## 9. Complete Real-World Example

Fetching a list of posts and printing titles:

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class Post {
    public int    id;
    public int    userId;
    public String title;
    public String body;
}

public class ApiDemo {
    private static final HttpClient   client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            List<Post> posts = mapper.readValue(response.body(),
                new TypeReference<List<Post>>() {});

            posts.stream()
                 .limit(5)
                 .forEach(p -> System.out.println(p.id + " → " + p.title));
        } else {
            System.out.println("Failed: " + response.statusCode());
        }
    }
}
```

---

## Common Exceptions

| Exception | Cause | Fix |
|---|---|---|
| `IOException` | Network error or connection failure | Wrap in try-catch, check URL |
| `InterruptedException` | Thread interrupted during `send()` | Handle or re-interrupt thread |
| `HttpTimeoutException` | Request exceeded timeout | Increase timeout or check network |
| `ConnectException` | Cannot reach the server | Verify URL and server availability |

```java
try {
    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());
} catch (HttpTimeoutException e) {
    System.out.println("Timed out: " + e.getMessage());
} catch (IOException e) {
    System.out.println("Network error: " + e.getMessage());
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    System.out.println("Interrupted: " + e.getMessage());
}
```

---

## Quick Reference

```java
// 1. Create client
HttpClient client = HttpClient.newHttpClient();

// 2. Build request
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/endpoint"))
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer token")
    .timeout(Duration.ofSeconds(10))
    .GET()                                              // or
    .POST(HttpRequest.BodyPublishers.ofString(body))    // or
    .PUT(HttpRequest.BodyPublishers.ofString(body))     // or
    .DELETE()
    .build();

// 3. Send and get response
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

// 4. Use response
response.statusCode(); // 200, 201, 404, 500...
response.body();       // response JSON as String
response.headers();    // response headers

// 5. Parse JSON (with Jackson)
MyClass obj  = mapper.readValue(response.body(), MyClass.class);
List<T> list = mapper.readValue(response.body(), new TypeReference<List<T>>() {});
```

| HTTP Method | Builder Method |
|---|---|
| GET | `.GET()` |
| POST | `.POST(BodyPublishers.ofString(body))` |
| PUT | `.PUT(BodyPublishers.ofString(body))` |
| DELETE | `.DELETE()` |
| Custom | `.method("PATCH", BodyPublishers.ofString(body))` |