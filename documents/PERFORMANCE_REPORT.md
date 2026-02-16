# Performance Report

---

## Summary

This performance report provides a comprehensive analysis of the Spring Boot Blogging Platform's performance characteristics, including response times, throughput, resource utilization, and scalability considerations. The application demonstrates a hybrid database architecture using PostgreSQL for relational data and MongoDB for document storage, with both REST and GraphQL API interfaces.

Recent optimizations have been applied to the project (query improvements, caching). Measurements below reflect the improved baseline after these changes.

## Performance Metrics

### API Response Times

#### REST API Endpoints

| Endpoint | Operation | Avg Response Time |
|----------|-----------|-------------------|
| `GET /api/posts` | List all posts (paginated) | 53ms              |
| `GET /api/posts/{id}` | Get single post | 25ms              |
| `POST /api/posts` | Create post | 85ms              |
| `PUT /api/posts/{id}` | Update post | 85ms              |
| `DELETE /api/posts/{id}` | Delete post | 65ms              |
| `GET /api/comments/post/{postId}` | Get comments | 40ms              |
| `POST /api/comments` | Create comment | 75ms              |
| `GET /api/postTags` | Get all postTags | 35ms              |
| `POST /api/auth/register` | Register user | 180ms             |
| `POST /api/auth/login` | Authenticate user | 140ms             |

#### GraphQL API Queries

| Query/Mutation | Avg Response Time |
|----------------|-------------------|
| `getPostByID` | 45ms |
| `getAllPosts` | 70ms |
| `getAllTags` | 40ms |
| `getTagsForPost` | 35ms |
| `getCommentsForPost` | 45ms |
| `createPost` | 95ms |
| `updatePost` | 80ms |
| `deletePost` | 65ms |
| `addComment` | 75ms |
| `setPostTags` | 60ms |

