# mock-service

A Spring Boot reactive testing library that enables JSON-based service mocking for WebFlux applications.

## Overview

`mock-service` is a testing utility that allows you to define mock service behaviors using JSON configuration files instead of writing verbose Mockito code. It's particularly useful for:

- Integration testing with Spring WebFlux
- Mocking external service calls with predefined responses
- Creating complex mock objects (Lists, Maps, custom POJOs) from JSON
- Reducing boilerplate test code

## Features

- **JSON-driven mocking**: Define mock behaviors in JSON files
- **Reactive support**: Built-in support for `Mono` and `Flux` types
- **Complex object support**: Handle Lists, Maps, and nested objects
- **Spring integration**: Seamlessly integrates with Spring Boot test context
- **Flexible fallback**: Falls back to real Spring beans when mocks aren't defined

## Requirements

- Java 17+
- Spring Boot 3.5.3
- Spring WebFlux

## Usage

### 1. Basic Setup

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>net.mcfarb.code</groupId>
    <artifactId>mock-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 2. Create Mock Configuration

Create a JSON file in `src/test/resources/mockdata/YourTestClass.json`:

```json
{
  "mockServices": [
    {
      "serviceClass": "com.example.UserService",
      "methods": [
        {
          "methodName": "getUserById",
          "returnId": "user1",
          "methodArguments": ["java.lang.Long"]
        }
      ]
    }
  ],
  "mockObjects": [
    {
      "id": "user1",
      "class": "com.example.User",
      "objectValue": {
        "id": 123,
        "name": "John Doe",
        "email": "john@example.com"
      }
    }
  ]
}
```

### 3. Use in Tests

```java
@SpringBootTest
public class UserServiceTest {

    private MonoMockProvider<UserServiceTest, Object> mockProvider;

    @Autowired
    private SpringBeanMonoProvider springBeanMonoProvider;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        mockProvider = new MonoMockProvider<>();
        mockProvider.setJsonProcessor(new JsonProcessor());
        mockProvider.initialize("mockdata/UserServiceTest");
        mockProvider.setSpringBeanMonoProvider(springBeanMonoProvider);
    }

    @Test
    public void testUserService() {
        Mono<Object> userServiceMono = mockProvider.getBean("userService");

        StepVerifier
            .create(userServiceMono)
            .consumeNextWith(serviceObj -> {
                UserService service = (UserService) serviceObj;
                User user = service.getUserById(123L);

                assertEquals("John Doe", user.getName());
                assertEquals("john@example.com", user.getEmail());
            })
            .verifyComplete();
    }
}
```

## Examples

### Mocking a List Return Type

```json
{
  "mockObjects": [
    {
      "id": "userList",
      "class": "java.util.List",
      "genericClass": "com.example.User",
      "objectValue": [
        {"id": 1, "name": "Alice"},
        {"id": 2, "name": "Bob"},
        {"id": 3, "name": "Charlie"}
      ]
    }
  ],
  "mockServices": [
    {
      "serviceClass": "com.example.UserService",
      "methods": [
        {
          "methodName": "getAllUsers",
          "returnId": "userList",
          "methodArguments": null
        }
      ]
    }
  ]
}
```

### Mocking a Map Return Type

```json
{
  "mockObjects": [
    {
      "id": "userMap",
      "class": "java.util.Map",
      "version": "2.0",
      "keyClass": "java.lang.Long",
      "valueClass": "com.example.User",
      "objectValue": {
        "1": {"id": 1, "name": "Alice"},
        "2": {"id": 2, "name": "Bob"}
      }
    }
  ]
}
```

### Mocking Methods with Multiple Arguments

```json
{
  "mockServices": [
    {
      "serviceClass": "com.example.SearchService",
      "methods": [
        {
          "methodName": "search",
          "returnId": "searchResults",
          "methodArguments": [
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Boolean"
          ]
        }
      ]
    }
  ]
}
```

### Date Handling

Dates are automatically parsed using the configured `DateFormat`:

```java
DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");
objectMapper.setDateFormat(dateFormat);
```

```json
{
  "mockObjects": [
    {
      "id": "event",
      "class": "com.example.Event",
      "objectValue": {
        "name": "Conference",
        "date": "07/27/2020, 05:03:17"
      }
    }
  ]
}
```

## REST Endpoint Mocking

In addition to service mocking, `mock-service` also supports mocking REST API endpoints with flexible URL matching.

### Features

- **Path parameter matching**: Support for dynamic path segments like `/users/{userId}`
- **Wildcard matching**: Use `*` for wildcard segments like `/products/*`
- **Query parameter matching**: Match requests based on query parameters
- **HTTP method matching**: Configure different responses for GET, POST, etc.
- **Custom headers and status codes**: Define response headers and HTTP status codes

### REST Mock Configuration

Create a JSON file in `src/test/resources/mockdata/` with REST endpoint definitions:

```json
{
  "mockRestApis": [
    {
      "basePath": "/api/v1",
      "methods": [
        {
          "path": "/users/{userId}",
          "httpMethod": "GET",
          "returnId": "user1",
          "statusCode": 200
        },
        {
          "path": "/users",
          "httpMethod": "GET",
          "returnId": "userList",
          "statusCode": 200,
          "queryParameters": {
            "active": "true"
          }
        },
        {
          "path": "/products/*",
          "httpMethod": "GET",
          "returnId": "product1",
          "statusCode": 200
        }
      ]
    }
  ],
  "mockObjects": [
    {
      "id": "user1",
      "class": "com.example.User",
      "objectValue": {
        "id": 123,
        "name": "John Doe"
      }
    },
    {
      "id": "userList",
      "class": "java.util.List",
      "genericClass": "com.example.User",
      "objectValue": [
        {"id": 1, "name": "Alice"},
        {"id": 2, "name": "Bob"}
      ]
    },
    {
      "id": "product1",
      "class": "com.example.Product",
      "objectValue": {
        "name": "Widget",
        "price": 99.99
      }
    }
  ]
}
```

### Using REST Mock Provider

```java
public class RestEndpointTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonProcessor jsonProcessor = new JsonProcessor();
    private MockRestProvider mockRestProvider = new MockRestProvider();

    @BeforeEach
    public void setup() throws Exception {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        jsonProcessor.setObjectMapper(objectMapper);
        mockRestProvider.setJsonProcessor(jsonProcessor);

        MockRestGeneratorInfo mockRestInfo = jsonProcessor
                .buildMockRestInfoObjectFromJson("mockdata/RestEndpointTest");

        mockRestProvider.initialize(mockRestInfo);
    }

    @Test
    public void testFindRestEndpoint() {
        // Find endpoint with path parameter
        MockRestMethodInfo method = mockRestProvider
                .findRestMethod("/api/v1/users/123", "GET", null);

        assertNotNull(method);
        assertEquals("/api/v1/users/{userId}", method.getPath());

        // Get the response object
        Object response = mockRestProvider.getResponseObject(method);
        User user = (User) response;
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void testQueryParameterMatching() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("active", "true");

        MockRestMethodInfo method = mockRestProvider
                .findRestMethod("/api/v1/users", "GET", queryParams);

        assertNotNull(method);
        List<User> users = (List<User>) mockRestProvider.getResponseObject(method);
        assertEquals(2, users.size());
    }

    @Test
    public void testExtractPathParameters() {
        Map<String, String> pathParams = mockRestProvider
                .extractPathParameters("/api/v1/users/{userId}", "/api/v1/users/123");

        assertEquals("123", pathParams.get("userId"));
    }
}
```

### REST Mock Examples

#### Path Parameters

Define endpoints with dynamic path segments using `{paramName}` syntax:

```json
{
  "path": "/api/users/{userId}/orders/{orderId}",
  "httpMethod": "GET",
  "returnId": "order1"
}
```

This will match:
- `/api/users/123/orders/456`
- `/api/users/abc/orders/xyz`

Extract parameter values:
```java
Map<String, String> params = mockRestProvider.extractPathParameters(
    "/api/users/{userId}/orders/{orderId}",
    "/api/users/123/orders/456"
);
// params: {"userId": "123", "orderId": "456"}
```

#### Wildcard Matching

Use `*` for wildcard segments:

```json
{
  "path": "/files/*",
  "httpMethod": "GET",
  "returnId": "file1"
}
```

This will match:
- `/files/document.pdf`
- `/files/image.png`
- `/files/123`

#### Query Parameter Matching

Match endpoints based on specific query parameters:

```json
{
  "path": "/search",
  "httpMethod": "GET",
  "returnId": "searchResults",
  "queryParameters": {
    "q": "test",
    "limit": "10"
  }
}
```

This will match:
- `/search?q=test&limit=10`
- `/search?q=test&limit=10&extra=value` (additional params are allowed)

But won't match:
- `/search?q=other&limit=10` (different value for `q`)
- `/search?limit=10` (missing required `q` parameter)

#### Custom Headers and Status Codes

```json
{
  "path": "/api/v1/resource",
  "httpMethod": "POST",
  "returnId": "createdResource",
  "statusCode": 201,
  "headers": {
    "Content-Type": "application/json",
    "X-Custom-Header": "value"
  }
}
```

### Testing User API with cURL

Once you have your mock server running, you can test the user API endpoints using these curl commands:

#### Get User by ID

```bash
curl -X GET http://localhost:8080/api/v1/users/123
```

Expected response:
```json
{
  "data1": "John Doe",
  "data2": 123,
  "data3": null
}
```

#### Get User List with Query Parameters

```bash
curl -X GET "http://localhost:8080/api/v1/users?active=true"
```

Expected response:
```json
[
  {
    "data1": "User 1",
    "data2": 1,
    "data3": null
  },
  {
    "data1": "User 2",
    "data2": 2,
    "data3": null
  }
]
```

#### Get User with Verbose Output

To see response headers and status codes:

```bash
curl -v -X GET http://localhost:8080/api/v1/users/123
```

#### Get User and Pretty Print JSON

Using `jq` for formatted output:

```bash
curl -X GET http://localhost:8080/api/v1/users/123 | jq
```

## Project Structure

```
src/main/java/net/mcfarb/testing/ddmock/
├── reactive/
│   └── MockGenerator.java          # Reactive mock bean generator
├── service/
│   ├── MonoMockProvider.java       # Main provider for Mono-based mocks
│   ├── MockRestProvider.java       # REST endpoint mock provider
│   ├── JsonProcessor.java          # JSON to object conversion
│   └── SpringBeanMonoProvider.java # Spring context bean provider
└── model/
    ├── MockGeneratorInfo.java      # Service mock configuration container
    ├── MockServiceInfo.java        # Service mock definition
    ├── MockMethodInfo.java         # Method mock definition
    ├── MockRestGeneratorInfo.java  # REST mock configuration container
    ├── MockRestInfo.java           # REST API definition
    ├── MockRestMethodInfo.java     # REST endpoint definition
    └── MockObject.java             # Object mock definition
```

## How It Works

1. **Configuration Loading**: JSON files are loaded and parsed into `MockGeneratorInfo` objects
2. **Object Building**: Mock objects are created from JSON definitions and stored in an object map
3. **Service Mocking**: Services are mocked using Mockito with configured return values
4. **Reactive Integration**: Mocks are wrapped in `Mono` for reactive compatibility
5. **Fallback**: If a bean isn't found in mocks, it falls back to the Spring context

## License

TBD

## Contributing

TBD
