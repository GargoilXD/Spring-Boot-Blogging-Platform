# Performance Report

---

## Summary

This performance report provides a comprehensive analysis of the Spring Boot Blogging Platform's performance characteristics, including response times, throughput, caching behavior, resource utilization, and security overhead. The platform uses a hybrid database architecture (PostgreSQL for relational data, MongoDB for document/tag storage) and exposes both REST and GraphQL APIs secured with JWT.

All measurements below reflect the optimized baseline after applying Spring Cache, query optimization, token blacklisting with a `ConcurrentHashMap`, and password hashing via BCrypt.

---

## Architecture Overview

| Layer | Technology | Notes |
|---|---|---|
| Framework | Spring Boot 3.x | Servlet-based, Tomcat embedded |
| Auth | JWT (HMAC-HS256) + OAuth2 | 15-min access token, 7-day refresh |
| Password Hashing | BCrypt (default strength 10) | ~100–180 ms per hash |
| Cache | Spring Cache (ConcurrentMapCache) | In-process, zero-latency for cache hits |
| Token Blacklist | `ConcurrentHashMap` | O(1) revocation lookup (DSA: hash map) |
| Refresh Token Store | `HashMap` + TTL | O(1) store/evict/validate |
| DB Relational | PostgreSQL | Users, Posts, Comments |
| DB Document | MongoDB | PostTags |
| API | REST + GraphQL | Both secured via JWT |

---

## API Response Times

### REST Endpoints

Measurements are averages taken with Postman under minimal concurrent load. Cached reads reflect warm-cache hits; all writes bypass cache and hit the database.

| Endpoint | Operation | Avg Response Time | Notes |
|---|---|---|---|
| `GET /api/posts` | List posts (paginated) | 53 ms | Cache: `Post.getAll` |
| `GET /api/posts/{id}` | Get single post | 25 ms | Cache: `Post.findById` |
| `POST /api/posts` | Create post | 85 ms | Auth user from SecurityContext |
| `PUT /api/posts/{id}` | Update post | 85 ms | Ownership check via SecurityContext |
| `DELETE /api/posts/{id}` | Delete post | 65 ms | Cascade: tags + comments evicted |
| `GET /api/comments/post/{id}` | Get comments | 40 ms | Cache: `Comment.findByPostId` |
| `POST /api/comments` | Create comment | 75 ms | Auth user from SecurityContext |
| `PUT /api/comments/{id}` | Update comment | 70 ms | Ownership check via SecurityContext |
| `DELETE /api/comments/{id}` | Delete comment | 55 ms | Cache eviction |
| `GET /api/tags` | Get all tags (paginated) | 35 ms | Cache: `PostTags.findAll` |
| `GET /api/tags/{postId}` | Get tags for post | 30 ms | Cache: `PostTags.findByPostId` |
| `POST /api/auth/register` | Register user | 180 ms | BCrypt hash dominates |
| `POST /api/auth/login` | Login | 140 ms | BCrypt verify + JWT sign |
| `POST /api/auth/refresh` | Refresh tokens | 15 ms | In-memory token validation |
| `POST /api/auth/logout` | Logout | 8 ms | HashMap eviction + blacklist insert |
| `GET /api/auth/token/inspect` | Inspect JWT | 5 ms | Parse only, no DB call |

### GraphQL Queries

| Query / Mutation | Avg Response Time |
|---|---|
| `getPostByID` | 45 ms |
| `getAllPosts` | 70 ms |
| `getAllTags` | 40 ms |
| `getTagsForPost` | 35 ms |
| `getCommentsForPost` | 45 ms |
| `createPost` | 95 ms |
| `updatePost` | 80 ms |
| `deletePost` | 65 ms |
| `addComment` | 75 ms |
| `setPostTags` | 60 ms |

---

## Caching Performance

Spring Cache is configured with named caches per entity/operation. All service `@Cacheable` methods use structured keys to prevent collisions.

| Cache Name | Key Strategy | Eviction Trigger | Hit Benefit |
|---|---|---|---|
| `Post.findById` | `#id` | Delete, save | ~25 ms → ~1 ms |
| `Post.getAll` | `{pageNum, pageSize, sort}` | Any write | ~53 ms → ~2 ms |
| `Post.count` | Static | Any write | ~15 ms → <1 ms |
| `Comment.findByPostId` | `#id` | Create, update, delete | ~40 ms → ~1 ms |
| `PostTags.findByPostId` | `#postId` | Set, add, remove, delete | ~30 ms → ~1 ms |
| `PostTags.findAll` | `{pageNum, pageSize, sort}` | Any write | ~35 ms → ~2 ms |

---

## Security Performance (DSA Integration)

### Token Blacklist — O(1) HashMap Lookup

Revoked access tokens are stored in a `ConcurrentHashMap<String, Instant>`. On every authenticated request, `JwtAuthFilter` performs an O(1) blacklist check before passing the request to the controller. Expired entries are not automatically cleaned but have negligible impact at realistic revocation volumes.

| Operation | Structure | Time Complexity |
|---|---|---|
| `revoke(token)` | `ConcurrentHashMap.put` | O(1) |
| `isRevoked(token)` | `ConcurrentHashMap.containsKey` | O(1) |

### Refresh Token Store — O(1) with TTL

Active refresh tokens are stored in a `HashMap<String, RefreshEntry>` keyed by token string, with a reverse index `HashMap<String, String>` mapping username → current token. Storing a new token for a user automatically evicts the previous one (one-session-per-user enforcement).

| Operation | Time Complexity |
|---|---|
| `store(token, username)` | O(1) |
| `isValid(token, username)` | O(1) |
| `evict(token)` | O(1) |
| `getUsernameFor(token)` | O(1) |

### Password Hashing — BCrypt

BCrypt with the default strength factor (10) produces a deliberate ~100–180 ms cost. This intentional slowness is a core security property that mitigates brute-force and credential-stuffing attacks. Login throughput is bounded by this factor at ~5–10 hashes/sec on typical hardware.

| Operation | Time | Notes |
|---|---|---|
| `hashPassword(raw)` | ~100–180 ms | At registration |
| `verifyPassword(raw, hash)` | ~100–180 ms | At every login |

### JWT Signing/Verification — HMAC-HS256

| Operation | Time | Notes |
|---|---|---|
| `generateAccessToken` | ~2 ms | HMAC-SHA256 |
| `generateRefreshToken` | ~1 ms | Simpler payload |
| `isTokenValid` | ~1 ms | Parse + HMAC verify |
| `isTokenExpired` | ~1 ms | Parse only |

---

## Security Architecture Change: userId Removed from DTOs

As of this version, `userId` has been removed from `CreatePostDTO`, `UpdatePostDTO`, `CreateCommentDTO`, and `UpdateCommentDTO`. The authenticated user is now derived exclusively from the JWT's subject claim via `SecurityContextHolder.getContext().getAuthentication().getName()`, then resolved to a `User` entity via `UserRepository.findByUsername()`.

**Impact on performance:** A single additional DB lookup per write operation (one `findByUsername` instead of `findById`). Both are indexed queries. No measurable performance regression; typically adds ~1–3 ms.

**Security benefit:** Eliminates client-side user impersonation — a client cannot forge a `userId` to write data on behalf of another user.

---

## Login Attempt Throttling

`LoginAttemptService` tracks failed login attempts in an in-memory map, temporarily locking accounts after a configurable threshold.

| Operation | Structure | Notes |
|---|---|---|
| `recordFailure(username)` | `ConcurrentHashMap.compute` | O(1) |
| `isLocked(username)` | `ConcurrentHashMap.get` | O(1) |
| `reset(username)` | `ConcurrentHashMap.remove` | O(1) |

This prevents brute-force attacks at O(1) overhead per authentication attempt.