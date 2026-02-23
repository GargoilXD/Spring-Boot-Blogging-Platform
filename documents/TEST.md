# Unit Test Documentation

## Overview

This document provides comprehensive documentation for all unit tests across the Spring Boot Blogging Platform. Tests cover the service layer, REST controller layer, GraphQL resolver layer, and security components. All tests use JUnit 5 and Mockito, with `@ExtendWith(MockitoExtension.class)` for lightweight, fast execution without starting a Spring context.

---

## Test Coverage Summary

### 1. PostServiceTest

**Location:** `src/test/java/com/blog/Service/PostServiceTest.java`

**Tests:** 13 test cases organized in nested classes

#### Security Context Mocking Pattern
Each test in `PostServiceTest` sets up a mocked `SecurityContext` with `"testuser"` as the authenticated principal. This is torn down via `SecurityContextHolder.clearContext()` in `@AfterEach`.

#### Test Cases

| Nested Class | Test Method | Description | Assertion |
|---|---|---|---|
| `FindById` | `findById_Success` | Post exists → Optional present | `assertTrue(result.isPresent())` |
| `FindById` | `findById_NotFound` | Post missing → Optional empty | `assertFalse(result.isPresent())` |
| `FindAll` | `findAll_Success` | Returns paginated result | Size equals 1 |
| `FindAll` | `findAll_EmptyPage` | Empty DB → empty Page | `assertTrue(result.isEmpty())` |
| `Count` | `count_Success` | Returns repository count | `assertEquals(5L, count)` |
| `Count` | `count_Zero` | Zero records | `assertEquals(0L, count)` |
| `Save` | `save_Success` | Creates post using SecurityContext user | `verify(user).addPost(...)` |
| `Save` | `save_Failure_UserNotFound` | Authenticated user not in DB → exception | Message contains `"Authenticated user not found"` |
| `Update` | `update_Success` | Owner updates post successfully | Post saved |
| `Update` | `update_Failure_UserNotFound` | Authenticated user not in DB | `EntityNotFoundException` |
| `Update` | `update_Failure_PostNotFound` | Post does not exist | `EntityNotFoundException` |
| `Update` | `update_Failure_NotOwner` | User is not the post owner | `SecurityException` |
| `Delete` | `delete_Success` | Cascade-deletes tags, comments, post | All three repository methods called |
| `Delete` | `delete_Failure_PostNotFound` | Post does not exist | `EntityNotFoundException`; nothing deleted |

---

### 2. CommentServiceTest

**Location:** `src/test/java/com/blog/Service/CommentServiceTest.java`

**Tests:** 11 test cases

#### Test Cases

| Nested Class | Test Method | Description | Assertion |
|---|---|---|---|
| `FindByPostId` | `findByPostId_Success` | Returns comment list | Size equals 1 |
| `FindByPostId` | `findByPostId_EmptyList` | No comments → empty list | `assertTrue(isEmpty)` |
| `Save` | `save_Success` | Creates comment from SecurityContext user | `verify(user).addComment(...)` |
| `Save` | `save_Failure_UserNotFound` | User missing → no DB save | `EntityNotFoundException` |
| `Save` | `save_Failure_PostNotFound` | Post missing → no DB save | `EntityNotFoundException` |
| `Update` | `update_Success` | Owner updates comment | Comment saved |
| `Update` | `update_Failure_UserNotFound` | User missing → exception | `EntityNotFoundException` |
| `Update` | `update_Failure_CommentNotFound` | Comment missing | `EntityNotFoundException` |
| `Update` | `update_Failure_NotOwner` | Non-owner update | `SecurityException` |
| `Delete` | `delete_Success` | Comment deleted | `verify(repository).deleteById(1)` |
| `Delete` | `delete_Failure_NotFound` | Comment missing | `EntityNotFoundException`; no delete called |

---

### 3. TagServiceTest

**Location:** `src/test/java/com/blog/Service/TagServiceTest.java`

**Tests:** 13 test cases

#### Test Cases

| Test Method | Description | Assertion |
|---|---|---|
| `findAll_Success` | Returns paginated PostTags | Size equals 1 |
| `findByPostId_Success` | Returns tags for post | Contains `"tag1"`, `"tag2"` |
| `findByPostId_NotFound_ReturnsEmptyList` | No tags → empty list | `assertTrue(isEmpty)` |
| `count_Success` | Returns tag count | `assertEquals(5L, count)` |
| `setPostTags_Success` | Replaces tags for post | `verify(repository).save(...)` |
| `setPostTags_Failure_PostNotFound` | Post missing | `EntityNotFoundException` |
| `addTagsToPost_Success` | Merges new tags | `verify(repository).save(...)` |
| `addTagsToPost_Failure_PostNotFound` | Post missing | `EntityNotFoundException` |
| `addTagsToPost_Failure_TagsNotFound` | No existing PostTags | `EntityNotFoundException` |
| `removeTagsFromPost_Success_DeleteWhenEmpty` | All tags removed → record deleted | `verify(repository).delete(...)` |
| `removeTagsFromPost_Failure_PostNotFound` | Post missing | `EntityNotFoundException` |
| `removeTagsFromPost_Failure_TagsNotFound` | No existing PostTags | `EntityNotFoundException` |
| `deleteByPostId_Success` | Deletes PostTags row | `verify(repository).deleteByPostId(1)` |
| `deleteByPostId_Failure_TagsNotFound` | PostTags missing | `EntityNotFoundException`; no delete |

---

### 4. AuthenticationServiceTest

**Location:** `src/test/java/com/blog/Service/AuthenticationServiceTest.java`

**Tests:** 11 test cases organized in nested classes

#### Test Cases

| Nested Class | Test Method | Description | Assertion |
|---|---|---|---|
| `LoginTests` | `login_Success_ReturnsTokenMap` | Valid credentials → access + refresh tokens returned | Map contains all three keys |
| `LoginTests` | `login_UserNotFound_ThrowsException` | Unknown username | `AuthenticationException("Invalid credentials")` |
| `LoginTests` | `login_WrongPassword_ThrowsException` | Wrong password | `AuthenticationException("Invalid credentials")` |
| `LoginTests` | `login_TrimsUsername` | Leading/trailing spaces stripped | `findByUsername("testuser")` called (not `" testuser "`) |
| `RegisterTests` | `register_Success` | Password hashed, user persisted | `verify(passwordHasher).hashPassword(...)` |
| `RegisterTests` | `register_DuplicateUsername_ThrowsException` | Existing username | `EntityExistsException` |
| `RefreshTests` | `refresh_ValidToken_ReturnsNewTokens` | Token rotation — old evicted, new stored | New access + refresh tokens returned |
| `RefreshTests` | `refresh_ExpiredToken_ThrowsException` | Expired refresh token | `AuthenticationException` |
| `RefreshTests` | `refresh_RevokedToken_ThrowsException` | Revoked/unknown refresh token | `AuthenticationException` |
| `LogoutTests` | `logout_BlacklistsAndEvicts` | Both tokens revoked | Both service methods called |
| `LogoutTests` | `logout_NullRefreshToken_OnlyBlacklists` | No refresh token provided | Only access token blacklisted |

---

### 5. JwtServiceTest

**Location:** `src/test/java/com/blog/Security/JwtServiceTest.java`

**Tests:** 20 test cases organized in nested classes

#### Test Cases

| Nested Class | Test Method | Description |
|---|---|---|
| `AccessTokenGeneration` | `generateAccessToken_NotNull` | Token is non-null and non-blank |
| `AccessTokenGeneration` | `generateAccessToken_ThreeParts` | Standard JWT `header.payload.signature` structure |
| `AccessTokenGeneration` | `generateAccessToken_SubjectIsUsername` | Subject claim matches username |
| `AccessTokenGeneration` | `generateAccessToken_ContainsRoles` | `roles` claim includes `ROLE_AUTHOR` |
| `AccessTokenGeneration` | `generateAccessToken_TypeIsAccess` | `type` claim equals `"access"` |
| `AccessTokenGeneration` | `generateAccessToken_NotExpiredImmediately` | Fresh token is not expired |
| `AccessTokenGeneration` | `generateAccessToken_AdminRoleClaim` | Admin token carries `ROLE_ADMIN` |
| `RefreshTokenGeneration` | `generateRefreshToken_NotNull` | Token is non-null and non-blank |
| `RefreshTokenGeneration` | `generateRefreshToken_TypeIsRefresh` | `type` claim equals `"refresh"` |
| `RefreshTokenGeneration` | `generateRefreshToken_SubjectIsUsername` | Subject matches username |
| `RefreshTokenGeneration` | `accessAndRefresh_AreDifferent` | Access and refresh tokens are distinct strings |
| `RefreshTokenGeneration` | `isRefreshTokenValid_True` | Valid refresh token passes validation |
| `RefreshTokenGeneration` | `isRefreshTokenValid_FalseForAccessToken` | Access token fails refresh validation |
| `TokenValidation` | `isTokenValid_True` | Fresh access token passes validation |
| `TokenValidation` | `isTokenValid_FalseForWrongUser` | Token from different user is rejected |
| `TokenValidation` | `isTokenValid_FalseForRefreshToken` | Refresh token fails access validation |
| `TokenValidation` | `tamperedToken_IsRejected` | Modified signature → exception |
| `TokenValidation` | `expiredToken_IsDetected` | `isTokenExpired` returns true |
| `TokenInspection` | `inspectToken_ReturnsAllKeys` | All 7 claim keys present |
| `TokenInspection` | `inspectToken_AlgorithmIsHS256` | Algorithm reported as `"HS256"` |
| `TokenInspection` | `inspectToken_IsExpiredFalse` | Fresh token reports `isExpired = false` |

---

### 6. RefreshTokenServiceTest

**Location:** `src/test/java/com/blog/Security/RefreshTokenServiceTest.java`

**Tests:** 8 test cases

#### Test Cases

| Test Method | Description | Assertion |
|---|---|---|
| `store_ThenIsValid_ReturnsTrue` | Stored token is valid for correct user | `assertTrue` |
| `isValid_WrongUser_ReturnsFalse` | Token is invalid for different user | `assertFalse` |
| `evict_TokenBecomesInvalid` | Evicted token no longer valid | `assertFalse` |
| `isValid_UnknownToken_ReturnsFalse` | Unknown token → false | `assertFalse` |
| `getUsernameFor_ReturnsUsername` | Returns correct username | `assertEquals("alice", ...)` |
| `getUsernameFor_UnknownToken_ReturnsNull` | Unknown token → null | `assertNull` |
| `store_NewToken_EjectsPreviousToken` | New token replaces old for same user (rotation) | Old invalid, new valid |
| `expiredToken_IsNotValid` | TTL = 0ms → token immediately expired | `assertFalse` |

---

### 7. RestPostControllerTest

**Location:** `src/test/java/com/blog/API/Rest/RestPostControllerTest.java`

**Tests:** 9 test cases organized in nested classes

#### Test Cases

| Nested Class | Test Method | HTTP Status | Description |
|---|---|---|---|
| `FindById` | `findById_Success` | 200 | Post exists |
| `FindById` | `findById_NotFound` | 404 | Post missing |
| `FindAll` | `findAll_Success` | 200 | Paginated results returned |
| `CreatePost` | `createPost_Success` | 201 | Post created |
| `CreatePost` | `createPost_Failure_UserNotFound` | — | `EntityNotFoundException` propagated |
| `UpdatePost` | `updatePost_Success` | 200 | Post updated |
| `UpdatePost` | `updatePost_Failure_PostNotFound` | — | `EntityNotFoundException` propagated |
| `UpdatePost` | `updatePost_Failure_NotOwner` | — | `SecurityException` propagated |
| `DeletePost` | `deletePost_Success` | 200 | Post deleted |
| `DeletePost` | `deletePost_Failure_PostNotFound` | — | `EntityNotFoundException` propagated |

---

### 8. RestCommentControllerTest

**Location:** `src/test/java/com/blog/API/Rest/RestCommentControllerTest.java`

**Tests:** 9 test cases organized in nested classes

#### Test Cases

| Nested Class | Test Method | HTTP Status | Description |
|---|---|---|---|
| `GetComments` | `getCommentsForPost_Success` | 200 | Comments returned |
| `GetComments` | `getCommentsForPost_EmptyList` | 200 | Empty list returned |
| `CreateComment` | `createComment_Success` | 201 | Comment created |
| `CreateComment` | `createComment_Failure_PostNotFound` | — | `EntityNotFoundException` propagated |
| `CreateComment` | `createComment_Failure_UserNotFound` | — | `EntityNotFoundException` propagated |
| `UpdateComment` | `updateComment_Success` | 200 | Comment updated |
| `UpdateComment` | `updateComment_Failure_CommentNotFound` | — | `EntityNotFoundException` propagated |
| `UpdateComment` | `updateComment_Failure_NotOwner` | — | `SecurityException` propagated |
| `DeleteComment` | `deleteComment_Success` | 200 | Comment deleted |
| `DeleteComment` | `deleteComment_Failure_NotFound` | — | `EntityNotFoundException` propagated |

---

### 9. RestAuthenticationControllerTest

**Location:** `src/test/java/com/blog/API/Rest/RestAuthenticationControllerTest.java`

**Tests:** 11 test cases organized in nested classes

#### Test Cases

| Nested Class | Test Method | HTTP Status | Description |
|---|---|---|---|
| `Login` | `login_Success` | 202 | Returns `accessToken`, `refreshToken`, `type` |
| `Login` | `login_Failure_InvalidCredentials` | — | `AuthenticationException` propagated |
| `Register` | `register_Success` | 201 | User created |
| `Register` | `register_Failure_DuplicateUsername` | — | `EntityExistsException` propagated |
| `Refresh` | `refresh_Success` | 200 | New token pair returned |
| `Refresh` | `refresh_Failure_ExpiredToken` | — | `AuthenticationException` propagated |
| `Logout` | `logout_Success` | 200 | Both tokens revoked |
| `Logout` | `logout_NoRefreshToken` | 200 | Only access token revoked |
| `InspectToken` | `inspectToken_Success` | 200 | Claims decoded and returned |
| `InspectToken` | `inspectToken_Failure_NoBearerPrefix` | 401 | Missing `Bearer ` prefix |
| `InspectToken` | `inspectToken_Failure_NullHeader` | 401 | Null Authorization header |

---

### 10. RestTagControllerTest

**Location:** `src/test/java/com/blog/API/Rest/RestTagControllerTest.java`

**Tests:** 10 test cases

| Test Method | HTTP Status | Description |
|---|---|---|
| `findAll_Success` | 200 | Paginated tags returned |
| `findByPostId_Success` | 200 | Tags for specific post |
| `setPostTags_Success` | 201 | Tags set for post |
| `setPostTags_Failure_PropagatesException` | — | `EntityNotFoundException` propagated |
| `addTagsToPost_Success` | 200 | Tags merged |
| `addTagsToPost_Failure_PropagatesException` | — | `EntityNotFoundException` propagated |
| `removeTagsFromPost_Success` | 200 | Tags removed |
| `removeTagsFromPost_Failure_PropagatesException` | — | `EntityNotFoundException` propagated |
| `deleteByPostId_Success` | 200 | All tags deleted |
| `deleteByPostId_Failure_PropagatesException` | — | `EntityNotFoundException` propagated |

---

### 11. GraphQLResolverTest

**Location:** `src/test/java/com/blog/API/GraphQL/GraphQLResolverTest.java`

**Tests:** 19 test cases

| Test Method | Description |
|---|---|
| `findPostByID_Found` | Returns `Post` when found |
| `findPostByID_NotFound` | Returns `null` when not found |
| `findAllPosts_DefaultParams` | Uses page=0, size=5 defaults |
| `findAllPosts_CustomParams` | Uses provided page/size |
| `findAllTags_Success` | Flattens tags across PostTags pages |
| `findTagsForPost_Success` | Returns tags for a post |
| `findCommentsForPost_Success` | Returns comments for a post |
| `login_Success` | Returns `true`, service returns token map |
| `login_Failure_PropagatesException` | Propagates exception |
| `register_Success` | Returns `true` |
| `createPost_Success` | Returns created `Post` |
| `updatePost_Success` | Returns updated `Post` |
| `deletePost_Success` | Returns `true` |
| `deletePost_Failure` | Propagates `EntityNotFoundException` |
| `addComment_Success` | Returns `true` |
| `updateComment_Success` | Returns `true` |
| `deleteComment_Success` | Returns `true` |
| `setPostTags_Success` | Returns `true` |
| `addTagsToPost_Success` | Returns `true` |
| `removeTagsFromPost_Success` | Returns `true` |
| `deleteByPostId_Success` | Returns `true` |

---

## Running the Tests

### Run All Tests

```bash
# Maven Wrapper (Linux/Mac)
./mvnw test

# Maven Wrapper (Windows)
.\mvnw.cmd test

# Standard Maven
mvn test
```

### Run a Specific Test Class

```bash
mvn test -Dtest=PostServiceTest
mvn test -Dtest=JwtServiceTest
mvn test -Dtest=RestAuthenticationControllerTest
```

### Run a Specific Test Method

```bash
mvn test -Dtest=PostServiceTest#save_Success
mvn test -Dtest=JwtServiceTest#tamperedToken_IsRejected
```

### Run Multiple Classes

```bash
mvn test -Dtest="PostServiceTest,CommentServiceTest,AuthenticationServiceTest"
```
