# Unit Tests

## Overview

This document provides comprehensive information about the unit tests created for the service layer of the Spring Boot Blogging Platform.

## Test Coverage Summary

### 1. PostServiceTest

**Location:** `src/test/java/com/blog/Service/PostServiceTest.java`

**Tests:** 12 test cases

#### Test Cases:

| Test Method | Description | Assertions |
|-------------|-------------|------------|
| `testGetPost_Success()` | Verifies successful retrieval of a post by ID | Post is present, ID and title match |
| `testGetPost_NotFound()` | Verifies behavior when post doesn't exist | Returns empty Optional |
| `testGetAllPosts_Success()` | Verifies retrieval of paginated posts | List size is correct |
| `testGetAllPosts_EmptyList()` | Verifies behavior with no posts | Returns empty list |
| `testCountPosts_Success()` | Verifies post count retrieval | Count matches expected value |
| `testCountPosts_Zero()` | Verifies count when no posts exist | Returns 0 |
| `testSave_Success()` | Verifies successful post creation | Post is created with correct data |
| `testUpdate_Success()` | Verifies successful post update | Post is updated correctly |
| `testUpdate_PostNotFound()` | Verifies update fails for non-existent post | Throws EntityNotFoundException |
| `testDelete_Success()` | Verifies successful post deletion | Delete method is called |
| `testDelete_PostNotFound()` | Verifies delete fails for non-existent post | Throws IllegalStateException |

---

### 2. CommentServiceTest

**Location:** `src/test/java/com/blog/Service/CommentServiceTest.java`

**Tests:** 10 test cases

#### Test Cases:

| Test Method | Description | Assertions |
|-------------|-------------|------------|
| `testGetForPost_Success()` | Verifies retrieval of comments for a post | List size and content match |
| `testGetForPost_EmptyList()` | Verifies behavior with no comments | Returns empty list |
| `testGetForPost_ThrowsDataAccessException()` | Verifies exception handling | Throws DataAccessException |
| `testSave_Success()` | Verifies successful comment creation | Comment is created correctly |
| `testSave_ThrowsDataAccessException()` | Verifies save error handling | Throws DataAccessException |
| `testUpdate_Success()` | Verifies successful comment update | Comment body is updated |
| `testUpdate_ThrowsDataAccessException()` | Verifies update error handling | Throws DataAccessException |
| `testDelete_Success()` | Verifies successful comment deletion | Delete is called |
| `testDelete_ThrowsDataAccessException()` | Verifies delete error handling | Throws DataAccessException |

---

### 3. TagServiceTest

**Location:** `src/test/java/com/blog/Service/TagServiceTest.java`

**Tests:** 11 test cases

#### Test Cases:

| Test Method | Description | Assertions |
|-------------|-------------|------------|
| `testGetAllTags_Success()` | Verifies retrieval of all tags | List contains expected tags |
| `testGetAllTags_EmptyList()` | Verifies behavior with no tags | Returns empty list |
| `testGetAllTags_ThrowsDataAccessException()` | Verifies exception handling | Throws DataAccessException |
| `testGetAllTagsByPosts_Success()` | Verifies retrieval of tags grouped by posts | Map contains correct data |
| `testGetAllTagsByPosts_EmptyMap()` | Verifies behavior with no tag mappings | Returns empty map |
| `testGetForPost_Success()` | Verifies retrieval of tags for a post | List contains post tags |
| `testGetForPost_EmptyList()` | Verifies behavior with no tags for post | Returns empty list |
| `testGetTagsForPost_Success()` | Verifies tag retrieval (alternate method) | List contains expected tags |
| `testGetTagsForPost_ThrowsDataAccessException()` | Verifies exception handling | Throws DataAccessException |
| `testSetPostTags_Success()` | Verifies setting tags for a post | Method is called correctly |
| `testSetPostTags_EmptyList()` | Verifies setting empty tag list | Handles empty list |
| `testSetPostTags_ThrowsDataAccessException()` | Verifies error handling | Throws DataAccessException |
---

### 4. AuthenticationServiceTest

**Location:** `src/test/java/com/blog/Service/AuthenticationServiceTest.java`

**Tests:** 11 test cases

#### Test Cases:

| Test Method | Description | Assertions |
|-------------|-------------|------------|
| `testAuthenticate_Success()` | Verifies successful authentication | No exception thrown |
| `testAuthenticate_UserNotFound()` | Verifies authentication fails for non-existent user | Throws AuthenticationException |
| `testAuthenticate_InvalidPassword()` | Verifies authentication fails with wrong password | Throws AuthenticationException |
| `testAuthenticate_WithWhitespace()` | Verifies username trimming works | Authentication succeeds |
| `testRegister_Success()` | Verifies successful user registration | User is registered |
| `testRegister_UserAlreadyExists()` | Verifies registration fails for existing user | Throws UserAlreadyExistsException |
| `testRegister_WithWhitespace()` | Verifies username trimming during registration | Registration succeeds |
| `testRegister_InvalidUsername()` | Verifies validation for short username | Throws IllegalArgumentException |
| `testRegister_InvalidPassword()` | Verifies validation for short password | Throws IllegalArgumentException |
| `testRegister_InvalidEmail()` | Verifies validation for invalid email | Throws IllegalArgumentException |
---

## Running the Tests

### Run All Tests

```bash
# Using Maven
mvn test

# Using Maven Wrapper (Windows)
.\mvnw.cmd test

# Using Maven Wrapper (Linux/Mac)
./mvnw test
```

### Run Specific Test Class

```bash
# Run PostServiceTest only
mvn test -Dtest=PostServiceTest

# Run multiple test classes
mvn test -Dtest=PostServiceTest,CommentServiceTest
```

### Run Specific Test Method

```bash
# Run a specific test method
mvn test -Dtest=PostServiceTest#testGetPost_Success
```