# vertx-items-service
# Vert.x Items Service

A modern microservice built with Vert.x framework that provides REST API for user authentication and item management. The service demonstrates the implementation of clean architecture principles using Vert.x, MongoDB, and JWT authentication.

## Features

- 🔐 User authentication (register, login, logout)
- 🎫 JWT-based authorization
- 📦 Item management (create and list items)
- 🗄️ MongoDB persistence
- ✅ Unit tests
- 🔒 Secure password hashing
- 🌐 CORS support

## Tech Stack

- Java 21
- Vert.x 4.5.1
- MongoDB
- JWT Authentication
- BCrypt for password hashing
- JUnit 5 for testing

## Prerequisites

- JDK 21
- Maven
- MongoDB
- Your favorite IDE (IntelliJ IDEA recommended)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── example/
│   │           ├── config/      # Configuration classes
│   │           ├── controllers/ # Request handlers
│   │           ├── handlers/    # Business logic handlers
│   │           ├── models/      # Domain models
│   │           ├── repositories/# Data access layer
│   │           ├── services/    # Business logic
│   │           ├── exceptions/  # Custom exceptions
│   │           ├── Main.java    # Application entry point
│   │           └── MainVerticle.java
│   └── resources/
│       ├── config.json   # Application configuration
│       └── logback.xml   # Logging configuration
└── test/
    └── java/            # Test classes
```

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/yourusername/vertx-items-service.git
cd vertx-items-service
```

2. Start MongoDB:
```bash
# Windows
net start MongoDB

# macOS
brew services start mongodb-community

# Linux
sudo systemctl start mongodb
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
java -jar target/vertx-items-service-1.0-SNAPSHOT.jar
```

The service will start on port 3000 by default.

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /register
Content-Type: application/json

{
    "login": "user@example.com",
    "password": "SecurePassword123!"
}
```

**Response:** 204 No Content

#### Login
```http
POST /login
Content-Type: application/json

{
    "login": "user@example.com",
    "password": "SecurePassword123!"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5..."
}
```

#### Logout
```http
POST /logout
Authorization: Bearer <jwt_token>
```

**Response:** 204 No Content

### Item Endpoints

#### Create Item
```http
POST /items
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
    "title": "My Item"
}
```

**Response:** 204 No Content

#### Get Items
```http
GET /items
Authorization: Bearer <jwt_token>
```

**Response:**
```json
[
    {
        "id": "123e4567-e89b-12d3-a456-426614174000",
        "title": "My Item"
    }
]
```

## Running Tests

Execute all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=MainVerticleTest
```

## Security Features

- Password hashing using BCrypt
- JWT-based authentication
- Input validation
- CORS configuration
- Error handling with proper status codes

## Error Handling

The service provides detailed error responses:

- 400 Bad Request - Invalid input data
- 401 Unauthorized - Invalid or missing authentication
- 409 Conflict - Resource already exists
- 500 Internal Server Error - Server-side errors

Example error response:
```json
{
    "error": "Invalid request"
}
```

## Acknowledgments

- [Vert.x Documentation](https://vertx.io/docs/)
- [MongoDB Java Drivers](https://mongodb.github.io/mongo-java-driver/)
- [JWT Authentication](https://jwt.io/)

