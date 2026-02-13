# Spring Boot Blogging Platform

A full-featured RESTful and GraphQL blogging platform built with Spring Boot 3.5, featuring a hybrid data architecture with PostgreSQL and MongoDB.
## Overview

This blogging platform is a modern, scalable application that demonstrates best practices in Spring Boot development. It implements a hybrid database architecture using PostgreSQL for relational data (users and posts) and MongoDB for document-based data (comments and postTags), providing both REST and GraphQL APIs.

## Features

### Core Functionality
- **User Management**
  - User registration with secure password hashing (Argon2)
  - User authentication
  - User profiles with full name, email, and gender

- **Post Management**
  - Create, read, update, and delete blog posts
  - Draft and published post states
  - Pagination and sorting support
  - Author attribution with username display

- **Comment System**
  - Add comments to posts
  - Update and delete comments
  - Retrieve all comments for a specific post
  - MongoDB-based storage for scalability

- **Tagging System**
  - Add multiple postTags to posts
  - Retrieve all available postTags
  - Filter posts by postTags
  - MongoDB-based tag storage

### Technical Features
- **Dual API Support**
  - RESTful API with comprehensive endpoints
  - GraphQL API with queries and mutations
  - GraphiQL interface for GraphQL exploration

- **Data Validation**
  - Jakarta Bean Validation
  - Custom validation logic
  - Comprehensive error handling

- **Logging & Monitoring**
  - AOP-based logging aspect
  - Structured logging with file rotation
  - Configurable log levels per environment

- **Multi-Environment Support**
  - Development, testing, and production profiles
  - Environment-specific configurations
  - Externalized database credentials

## Technology Stack

### Backend Framework
- **Spring Boot 3.5.11** (SNAPSHOT)
- **Java 21**

### Spring Modules
- Spring Web (REST API)
- Spring Data JDBC (PostgreSQL integration)
- Spring Data MongoDB (MongoDB integration)
- Spring GraphQL (GraphQL API)
- Spring AOP (Aspect-Oriented Programming)
- Spring Validation (Bean Validation)

### Databases
- **PostgreSQL** - Relational data (Users, Posts)
- **MongoDB** - Document data (Comments, Tags)

### Security & Utilities
- **Argon2** - Password hashing (argon2-jvm 2.11)
- **Lombok** - Boilerplate code reduction
- **Jakarta Persistence API** - JPA annotations
- **Jakarta Transaction API** - Transaction management

### Build Tool
- **Maven** - Dependency management and build automation

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         API Layer (Controllers)         │
│  ┌──────────────┐    ┌──────────────┐  │
│  │  REST API    │    │  GraphQL API │  │
│  └──────────────┘    └──────────────┘  │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│         Service Layer (Business)        │
│  ┌──────┐ ┌──────┐ ┌─────────┐ ┌─────┐ │
│  │ Post │ │ User │ │ Comment │ │ Tag │ │
│  └──────┘ └──────┘ └─────────┘ └─────┘ │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│    Data Access Layer (Repositories)     │
│  ┌──────────┐         ┌──────────────┐ │
│  │   JDBC   │         │   MongoDB    │ │
│  │ (Posts,  │         │ (Comments,   │ │
│  │  Users)  │         │    Tags)     │ │
│  └──────────┘         └──────────────┘ │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│           Database Layer                │
│  ┌──────────┐         ┌──────────────┐ │
│  │PostgreSQL│         │   MongoDB    │ │
│  └──────────┘         └──────────────┘ │
└─────────────────────────────────────────┘
```

### Design Patterns
- **Data Transfer Objects (DTOs)** - Separate request/response models
- **Repository Pattern** - Data access abstraction
- **Dependency Injection** - Constructor-based injection
- **Aspect-Oriented Programming** - Cross-cutting concerns (logging)
- **Strategy Pattern** - Multiple data accessor implementations (JDBC, MongoDB, Fake)

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **PostgreSQL 12+** (running on port 5432)
- **MongoDB 4.4+** (running on port 27017)
- **Git** (for cloning the repository)

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd "Spring Boot Blogging Platform"
```

### 2. Set Up PostgreSQL Database

```sql
-- Create the development database
CREATE DATABASE blogdb_dev;

-- Create the test database (optional)
CREATE DATABASE blogdb_test;

-- Create the production database (optional)
CREATE DATABASE blogdb_prod;
```

### 3. Set Up MongoDB

MongoDB will automatically create the database and collections on first use. Ensure MongoDB is running:

```bash
# Start MongoDB service
mongod
```

### 4. Configure Environment Variables

Set the following environment variables for database credentials:

**Windows (PowerShell):**
```powershell
$env:DEV_DB_USERNAME="your_postgres_username"
$env:DEV_DB_PASSWORD="your_postgres_password"
```

**Linux/Mac:**
```bash
export DEV_DB_USERNAME="your_postgres_username"
export DEV_DB_PASSWORD="your_postgres_password"
```

### 5. Initialize Database Schema

The application will automatically initialize the PostgreSQL schema and data on startup when running in development mode (see `src/main/resources/schema.sql` and `data.sql`).

For MongoDB, you can optionally load sample data:

```bash
# Navigate to the mongo directory
cd src/main/resources/mongo

# Run the insert scripts (requires mongosh or mongo CLI)
bash insert_tags.sh
bash insert_comments.sh
```

## Configuration

### Application Profiles

The application supports three profiles:

1. **Development (`dev`)** - Default profile
   - Detailed logging
   - Auto-initialization of database schema
   - GraphiQL enabled
   - Pretty-printed JSON responses

2. **Test (`test`)** - For testing
   - Separate test database
   - Minimal logging

3. **Production (`prod`)** - For production deployment
   - Optimized logging
   - Production database
   - Security hardening

### Configuration Files

- `application.properties` - Common configuration
- `application-dev.properties` - Development settings
- `application-test.properties` - Test settings
- `application-prod.properties` - Production settings

## Running the Application

### Using Maven Wrapper (Recommended)

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

### Using Maven

```bash
mvn spring-boot:run
```
## API Documentation

### REST API Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate a user |

#### Posts

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts` | Get all posts (paginated) |
| GET | `/api/posts/{id}` | Get a specific post |
| POST | `/api/posts` | Create a new post |
| PUT | `/api/posts/{id}` | Update a post |
| DELETE | `/api/posts/{id}` | Delete a post |

**Query Parameters for GET /api/posts:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size
- `sortBy` (default: created_at) - Sort field
- `direction` (default: DESC) - Sort direction (ASC/DESC)

#### Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/comments/post/{postId}` | Get all comments for a post |
| POST | `/api/comments` | Create a new comment |
| PUT | `/api/comments/{id}` | Update a comment |
| DELETE | `/api/comments/{id}` | Delete a comment |

#### Tags

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/postTags` | Get all postTags |
| GET | `/api/postTags/post/{postId}` | Get postTags for a specific post |
| POST | `/api/postTags/post/{postId}` | Set postTags for a post |

### GraphQL API

Access the GraphiQL interface at `http://localhost:8080/graphiql` for interactive exploration.

#### Queries

```graphql
# Get a post by ID
query {
  getPostByID(id: "1") {
    id
    title
    body
    username
    draft
    createdAt
    postTags
    comments {
      id
      username
      body
      createdAt
    }
  }
}

# Get all posts
query {
  getAllPosts(page: 0, size: 10) {
    id
    title
    username
    draft
    createdAt
  }
}

# Get all postTags
query {
  getAllTags
}

# Get postTags for a post
query {
  getTagsForPost(postID: "1")
}

# Get comments for a post
query {
  getCommentsForPost(postID: "1") {
    id
    username
    body
    createdAt
  }
}
```

#### Mutations

```graphql
# Register a user
mutation {
  register(input: {
    username: "johndoe"
    password: "securePassword123"
    fullName: "John Doe"
    email: "john@example.com"
    gender: "Male"
  })
}

# Login
mutation {
  login(username: "johndoe", password: "securePassword123")
}

# Create a post
mutation {
  createPost(input: {
    authorId: "1"
    title: "My First Post"
    body: "This is the content of my first post."
    draft: false
  }) {
    id
    title
    createdAt
  }
}

# Update a post
mutation {
  updatePost(id: "1", input: {
    title: "Updated Title"
    body: "Updated content"
    draft: false
  }) {
    id
    title
    body
  }
}

# Delete a post
mutation {
  deletePost(id: "1")
}

# Add a comment
mutation {
  addComment(input: {
    userId: "1"
    postId: "1"
    username: "johndoe"
    body: "Great post!"
  })
}

# Update a comment
mutation {
  updateComment(id: "comment_id", body: "Updated comment text")
}

# Delete a comment
mutation {
  deleteComment(id: "comment_id")
}

# Set postTags for a post
mutation {
  setPostTags(postID: "1", postTags: ["technology", "spring-boot", "java"])
}
```

## Database Schema

### PostgreSQL Schema

#### Users Table
```sql
CREATE TABLE users (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    gender VARCHAR(1) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);
```

#### Posts Table
```sql
CREATE TABLE posts (
     id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
     user_id INTEGER NOT NULL REFERENCES users(id),
     title VARCHAR NOT NULL,
     body TEXT NOT NULL,
     is_draft BOOLEAN DEFAULT false,
     created_at TIMESTAMP DEFAULT now()
);
```

### MongoDB Collections

#### Comments Collection
```json
{
  "_id": "ObjectId",
  "userId": "Long",
  "postId": "Long",
  "username": "String",
  "body": "String",
  "createdAt": "ISODate"
}
```

#### Tags Collection
```json
{
  "_id": "ObjectId",
  "postId": "Long",
  "postTags": ["String"]
}
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=SpringBootBloggingPlatformApplicationTests
```
Testing details [TEST.md](documents/TEST.md)

## Project Structure

```
Spring Boot Blogging Platform/
├── src/
│   ├── main/
│   │   ├── java/com/blog/
│   │   │   ├── API/
│   │   │   │   ├── GraphQL/
│   │   │   │   │   └── GraphQLResolver.java
│   │   │   │   └── Rest/
│   │   │   │       ├── RestAuthenticationController.java
│   │   │   │       ├── RestCommentController.java
│   │   │   │       ├── RestPostController.java
│   │   │   │       └── RestTagController.java
│   │   │   ├── Aspect/
│   │   │   │   └── LoggingAspect.java
│   │   │   ├── Config/
│   │   │   │   └── AppConfig.java
│   │   │   ├── DataAccessor/
│   │   │   │   ├── Exception/
│   │   │   │   │   └── DataAccessException.java
│   │   │   │   ├── Fake/
│   │   │   │   │   ├── FakeCommentDataAccessor.java
│   │   │   │   │   └── FakeTagDataAccessor.java
│   │   │   │   ├── Interface/
│   │   │   │   │   ├── CommentDataAccessor.java
│   │   │   │   │   ├── PostDataAccessor.java
│   │   │   │   │   ├── TagDataAccessor.java
│   │   │   │   │   └── UserDataAccessor.java
│   │   │   │   ├── JDBC/
│   │   │   │   │   ├── JDBCPostDataAccessor.java
│   │   │   │   │   └── JDBCUserDataAccessor.java
│   │   │   │   └── MongoDB/
│   │   │   │       ├── MongoDBCommentDataAccessor.java
│   │   │   │       └── MongoDBTagDataAccessor.java
│   │   │   ├── DataTransporter/
│   │   │   │   ├── Comment/
│   │   │   │   │   ├── CreateCommentDTO.java
│   │   │   │   │   ├── ResponseCommentDTO.java
│   │   │   │   │   └── UpdateCommentDTO.java
│   │   │   │   ├── Post/
│   │   │   │   │   ├── CreatePostDTO.java
│   │   │   │   │   ├── GetPostDTO.java
│   │   │   │   │   ├── ResponsePostDTO.java
│   │   │   │   │   └── UpdatePostDTO.java
│   │   │   │   └── User/
│   │   │   │       ├── LoginUserDTO.java
│   │   │   │       └── RegisterUserDTO.java
│   │   │   ├── ExceptionHandler/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GraphQLExceptionHandler.java
│   │   │   │   └── RestExceptionHandler.java
│   │   │   ├── Model/
│   │   │   │   ├── Comment.java
│   │   │   │   ├── Post.java
│   │   │   │   └── User.java
│   │   │   ├── Service/
│   │   │   │   ├── Exception/
│   │   │   │   │   ├── AuthenticationException.java
│   │   │   │   │   └── UserAlreadyExistsException.java
│   │   │   │   ├── AuthenticationService.java
│   │   │   │   ├── CommentService.java
│   │   │   │   ├── PostService.java
│   │   │   │   └── TagService.java
│   │   │   ├── Utility/
│   │   │   │   └── PasswordHasher.java
│   │   │   └── SpringBootBloggingPlatformApplication.java
│   │   └── resources/
│   │       ├── graphql/
│   │       │   └── schema.graphql
│   │       ├── mongo/
│   │       │   ├── comment_data.json
│   │       │   ├── insert_comments.sh
│   │       │   ├── insert_tags.sh
│   │       │   └── tag_data.json
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── application-test.properties
│   │       ├── data.sql
│   │       └── schema.sql
│   └── test/
│       └── java/com/blog/
│           └── SpringBootBloggingPlatformApplicationTests.java
├── logs/
│   └── application.log
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
├── PERFORMANCE_REPORT.md
└── OPENAPI_DOCUMENTATION.yaml
```
