# mock-api

A Spring Boot reactive web application demonstrating the use of the `mock-service` library for testing.

## Overview

This project is a sample reactive REST API built with Spring WebFlux that showcases:
- Reactive programming with Project Reactor (Mono/Flux)
- RESTful API endpoints
- Integration with the `mock-service` testing library
- Clean separation of concerns (Controller, Service, Model layers)

## Technologies

- Java 17
- Spring Boot 3.5.3
- Spring WebFlux
- Project Reactor
- Lombok
- mock-service (testing library)

## Prerequisites

Before running this application, you need to install the `mock-service` dependency to your local Maven repository:

```bash
cd ../mock-service
mvn clean install
```

## Running the Application

```bash
cd mock-api
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### User Management

- **GET** `/api/users` - Get all users
- **GET** `/api/users/{id}` - Get user by ID
- **POST** `/api/users` - Create a new user
- **PUT** `/api/users/{id}` - Update an existing user
- **DELETE** `/api/users/{id}` - Delete a user

### Example Requests

#### Get all users
```bash
curl http://localhost:8080/api/users
```

#### Get user by ID
```bash
curl http://localhost:8080/api/users/1
```

#### Create a new user
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'
```

#### Update a user
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com"}'
```

#### Delete a user
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## Testing with mock-service

This project demonstrates how to use the `mock-service` library for testing reactive services. The library allows you to:

1. Define mock behaviors in JSON configuration files
2. Avoid writing verbose Mockito code
3. Test complex reactive flows with Mono and Flux types

### Example Test

See `src/test/java/net/mcfarb/mockapi/service/UserServiceTest.java` for a complete example of:
- Setting up `MonoMockProvider`
- Configuring JSON-based mocks in `src/test/resources/mockdata/UserServiceTest.json`
- Testing reactive service methods with StepVerifier

### Running Tests

```bash
mvn test
```

## Project Structure

```
mock-api/
├── src/
│   ├── main/
│   │   ├── java/net/mcfarb/mockapi/
│   │   │   ├── controller/
│   │   │   │   └── UserController.java
│   │   │   ├── model/
│   │   │   │   └── User.java
│   │   │   ├── service/
│   │   │   │   └── UserService.java
│   │   │   └── MockApiApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/net/mcfarb/mockapi/
│       │   └── service/
│       │       └── UserServiceTest.java
│       └── resources/
│           └── mockdata/
│               └── UserServiceTest.json
└── pom.xml
```

## Building the Project

```bash
mvn clean package
```

This will create a JAR file in the `target/` directory.

## License

TBD
