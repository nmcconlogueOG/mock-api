# Testing Guide for Configuration-Driven Controllers

This guide explains how to write tests that simulate API calls to the configuration-driven controllers.

## Overview

The mock-api uses **WebTestClient** from Spring WebFlux to test reactive REST endpoints. There are two main approaches:

1. **@WebFluxTest** - Fast, focused tests for individual controllers
2. **@SpringBootTest** - Full integration tests with complete application context

## Test Approaches

### 1. Unit Testing with @WebFluxTest

Use `@WebFluxTest` when you want to:
- Test a single controller in isolation
- Run tests faster
- Focus on controller-specific behavior

**Example: UserControllerTest.java**

```java
@WebFluxTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testGetUserById() {
        webTestClient
            .get()
            .uri("/api/user/123")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo("1")
            .jsonPath("$.name").isEqualTo("John Doe")
            .jsonPath("$.email").isEqualTo("john@example.com");
    }
}
```

### 2. Integration Testing with @SpringBootTest

Use `@SpringBootTest` when you want to:
- Test the entire application context
- Test multiple controllers together
- Test with all beans and configurations loaded

**Example: IntegrationTest.java**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testUserAndProductEndpoints() {
        // Test user endpoint
        webTestClient.get().uri("/api/user/123")
            .exchange().expectStatus().isOk();

        // Test product endpoint
        webTestClient.get().uri("/api/product/101")
            .exchange().expectStatus().isOk();
    }
}
```

## Testing Different HTTP Methods

### GET Request
```java
@Test
public void testGetAllUsers() {
    webTestClient
        .get()
        .uri("/api/user")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isArray();
}
```

### GET with Path Parameters
```java
@Test
public void testGetUserById() {
    webTestClient
        .get()
        .uri("/api/user/{id}", "123")  // Path parameter
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk();
}
```

### GET with Query Parameters
```java
@Test
public void testGetActiveUsers() {
    // Method 1: Using URI string
    webTestClient
        .get()
        .uri("/api/user?status=active")
        .exchange()
        .expectStatus().isOk();

    // Method 2: Using URI builder (recommended)
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/user")
            .queryParam("status", "active")
            .build())
        .exchange()
        .expectStatus().isOk();
}
```

### POST Request
```java
@Test
public void testCreateUser() {
    String newUser = """
        {
            "name": "Test User",
            "email": "test@example.com",
            "role": "user"
        }
        """;

    webTestClient
        .post()
        .uri("/api/user")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(newUser)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.message").isEqualTo("User created successfully");
}
```

### PUT Request
```java
@Test
public void testUpdateUser() {
    String updatedUser = """
        {
            "name": "Updated Name",
            "email": "updated@example.com"
        }
        """;

    webTestClient
        .put()
        .uri("/api/user/{id}", "1")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updatedUser)
        .exchange()
        .expectStatus().isOk();
}
```

### DELETE Request
```java
@Test
public void testDeleteUser() {
    webTestClient
        .delete()
        .uri("/api/user/{id}", "1")
        .exchange()
        .expectStatus().isEqualTo(204);
}
```

## Response Assertions

### JSON Path Assertions
```java
webTestClient
    .get().uri("/api/user/123")
    .exchange()
    .expectBody()
    .jsonPath("$.id").isEqualTo("1")
    .jsonPath("$.name").isEqualTo("John Doe")
    .jsonPath("$.email").exists()
    .jsonPath("$.role").isNotEmpty();
```

### Array Assertions
```java
webTestClient
    .get().uri("/api/user")
    .exchange()
    .expectBody()
    .jsonPath("$").isArray()
    .jsonPath("$.length()").isEqualTo(3)
    .jsonPath("$[0].name").isEqualTo("Alice Johnson")
    .jsonPath("$[1].name").isEqualTo("Bob Smith");
```

### Using consumeWith for Complex Assertions
```java
@Test
public void testWithConsumeWith() {
    webTestClient
        .get().uri("/api/user")
        .exchange()
        .expectBodyList(Map.class)
        .consumeWith(response -> {
            List<Map> users = response.getResponseBody();
            assert users != null;
            assert users.size() == 3;
            assert users.get(0).get("name").equals("Alice Johnson");
        });
}
```

### Using returnResult to Get Response Body
```java
@Test
public void testWithReturnResult() {
    Map<String, Object> user = webTestClient
        .get().uri("/api/user/123")
        .exchange()
        .expectBody(Map.class)
        .returnResult()
        .getResponseBody();

    // Now you can use the response body for additional assertions
    assertNotNull(user);
    assertEquals("John Doe", user.get("name"));
}
```

## Testing Error Scenarios

### Test 404 Not Found
```java
@Test
public void testNonExistentEndpoint() {
    webTestClient
        .get()
        .uri("/api/user/search/advanced")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.error").exists()
        .jsonPath("$.path").isEqualTo("/api/user/search/advanced");
}
```

## Running Tests

### Run all tests
```bash
cd mock-api
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=UserControllerTest
```

### Run specific test method
```bash
mvn test -Dtest=UserControllerTest#testGetUserById
```

### Run with verbose output
```bash
mvn test -X
```

## Best Practices

1. **Use @WebFluxTest for controller tests** - Faster and more focused
2. **Use @SpringBootTest for integration tests** - Tests the full application
3. **Use URI builder for query parameters** - More reliable than string concatenation
4. **Test both success and error scenarios** - Ensure proper error handling
5. **Use meaningful test names** - Clearly describe what is being tested
6. **Verify status codes AND response body** - Don't just check status codes
7. **Test with different data** - Path parameters, query parameters, request bodies

## Configuration-Driven Testing

Since the controllers are configuration-driven, you can:

1. **Add new endpoints** by updating JSON configuration files
2. **No code changes needed** for new endpoints
3. **Tests remain the same** - just update expectations

**Example:** To add a new user endpoint:

1. Update `src/main/resources/mockdata/user.json`:
```json
{
  "path": "/search",
  "httpMethod": "GET",
  "returnId": "searchResults",
  "statusCode": 200
}
```

2. Add mock object:
```json
{
  "id": "searchResults",
  "class": "java.util.List",
  "genericClass": "java.util.Map",
  "objectValue": [...]
}
```

3. Write test:
```java
@Test
public void testSearchUsers() {
    webTestClient.get().uri("/api/user/search")
        .exchange().expectStatus().isOk();
}
```

## Additional Resources

- [Spring WebFlux Testing Documentation](https://docs.spring.io/spring-framework/reference/testing/webtestclient.html)
- [WebTestClient API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/reactive/server/WebTestClient.html)
- [JSONPath Syntax](https://github.com/json-path/JsonPath)
