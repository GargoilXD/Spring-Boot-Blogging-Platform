package com.blog.API.GraphQL;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.Comment;
import com.blog.Model.Post;
import com.blog.Service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@GraphQlTest(GraphQLResolver.class)
@DisplayName("GraphQL Resolver Tests")
class GraphQLResolverTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private PostService postService;

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private TagService tagService;

    private long testStartTime;
    private long testEndTime;

    @BeforeEach
    void setUp() {
        testStartTime = System.nanoTime();
    }

    @AfterEach
    void tearDown() {
        testEndTime = System.nanoTime();
        long executionTimeMs = (testEndTime - testStartTime) / 1_000_000;
        System.out.println("Execution Time: " + executionTimeMs + " ms");
    }

    // ==================== QUERY TESTS ====================

    @Test
    @DisplayName("Query: findPostByID - Should find post by ID")
    void testFindPostByID() {
        // Arrange
        Post post = new Post(1, 1, "Test Post", "Test Body", false, LocalDateTime.now());
        when(postService.findById(1)).thenReturn(Optional.of(post));

        // Act & Assert
        graphQlTester.document("{ findPostByID(id: 1) { id title } }")
                .execute()
                .path("findPostByID.id").entity(Integer.class).isEqualTo(1)
                .path("findPostByID.title").entity(String.class).isEqualTo("Test Post");

        verify(postService, times(1)).findById(1);
    }

    @Test
    @DisplayName("Query: findPostByID - Should return null for non-existent post")
    void testFindPostByIDNotFound() {
        // Arrange
        when(postService.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        graphQlTester.document("{ findPostByID(id: 999) { id title } }")
                .execute()
                .path("findPostByID").valueIsNull();

        verify(postService, times(1)).findById(999);
    }

    @Test
    @DisplayName("Query: getAllPosts - Should get all posts with pagination")
    void testGetAllPosts() {
        // Arrange
        Post post1 = new Post(1, 1, "Post 1", "Body 1", false, LocalDateTime.now());
        Post post2 = new Post(2, 1, "Post 2", "Body 2", false, LocalDateTime.now());
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1, post2), PageRequest.of(0, 10), 2);

        when(postService.findAll(any())).thenReturn(postPage);

        // Act & Assert
        graphQlTester.document("{ getAllPosts(page: 0, size: 10) { totalElements } }")
                .execute()
                .path("getAllPosts.totalElements").entity(Long.class).isEqualTo(2L);

        verify(postService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("Query: getAllPosts - Should get posts with different page size")
    void testGetAllPostsWithDifferentPageSize() {
        // Arrange
        Post post1 = new Post(1, 1, "Post 1", "Body 1", false, LocalDateTime.now());
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1), PageRequest.of(0, 5), 1);

        when(postService.findAll(any())).thenReturn(postPage);

        // Act & Assert
        graphQlTester.document("{ getAllPosts(page: 0, size: 5) { totalElements } }")
                .execute()
                .path("getAllPosts.totalElements").entity(Long.class).isEqualTo(1L);

        verify(postService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("Query: findAllTags - Should find all tags with pagination")
    void testFindAllTags() {
        // Arrange
        when(tagService.findAll(any())).thenReturn(Page.empty());

        // Act & Assert
        graphQlTester.document("{ findAllTags(page: 0, size: 10) { totalElements } }")
                .execute()
                .path("findAllTags.totalElements").entity(Long.class).isEqualTo(0L);

        verify(tagService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("Query: findTagsForPost - Should find tags for post")
    void testFindTagsForPost() {
        // Arrange
        List<String> tags = Arrays.asList("Java", "Spring", "Boot");
        when(tagService.findByPostId(1)).thenReturn(tags);

        // Act & Assert
        graphQlTester.document("{ findTagsForPost(postID: 1) }")
                .execute()
                .path("findTagsForPost[0]").entity(String.class).isEqualTo("Java");

        verify(tagService, times(1)).findByPostId(1);
    }

    @Test
    @DisplayName("Query: findTagsForPost - Should return empty list for post with no tags")
    void testFindTagsForPostEmpty() {
        // Arrange
        when(tagService.findByPostId(999)).thenReturn(Arrays.asList());

        // Act & Assert
        graphQlTester.document("{ findTagsForPost(postID: 999) }")
                .execute()
                .path("findTagsForPost").entityList(String.class).hasSize(0);

        verify(tagService, times(1)).findByPostId(999);
    }

    @Test
    @DisplayName("Query: findCommentsForPost - Should find comments for post")
    void testFindCommentsForPost() {
        // Arrange
        List<Comment> comments = Arrays.asList(
            new Comment(1, 1, 1, "Great post!", LocalDateTime.now()),
            new Comment(2, 2, 1, "Very informative", LocalDateTime.now())
        );
        when(commentService.findByPostId(1)).thenReturn(comments);

        // Act & Assert
        graphQlTester.document("{ findCommentsForPost(postID: 1) { body } }")
                .execute()
                .path("findCommentsForPost[0].body").entity(String.class).isEqualTo("Great post!");

        verify(commentService, times(1)).findByPostId(1);
    }

    @Test
    @DisplayName("Query: findCommentsForPost - Should return empty list for post with no comments")
    void testFindCommentsForPostEmpty() {
        // Arrange
        when(commentService.findByPostId(999)).thenReturn(Arrays.asList());

        // Act & Assert
        graphQlTester.document("{ findCommentsForPost(postID: 999) { body } }")
                .execute()
                .path("findCommentsForPost").entityList(Comment.class).hasSize(0);

        verify(commentService, times(1)).findByPostId(999);
    }

    // ==================== MUTATION TESTS ====================

    @Test
    @DisplayName("Mutation: login - Should login successfully")
    void testLoginMutation() {
        // Arrange
        doNothing().when(authService).login("johndoe", "SecurePass123!");

        // Act & Assert
        graphQlTester.document("mutation { login(username: \"johndoe\", password: \"SecurePass123!\") }")
                .execute()
                .path("login").entity(Boolean.class).isEqualTo(true);

        verify(authService, times(1)).login("johndoe", "SecurePass123!");
    }

    @Test
    @DisplayName("Mutation: login - Should handle username with spaces")
    void testLoginMutationWithSpaces() {
        // Arrange
        doNothing().when(authService).login("john doe", "SecurePass123!");

        // Act & Assert
        graphQlTester.document("mutation { login(username: \"john doe\", password: \"SecurePass123!\") }")
                .execute()
                .path("login").entity(Boolean.class).isEqualTo(true);

        verify(authService, times(1)).login("john doe", "SecurePass123!");
    }

    @Test
    @DisplayName("Mutation: register - Should register user successfully")
    void testRegisterMutation() {
        // Arrange
        doNothing().when(authService).register(any(RegisterUserDTO.class));

        // Act & Assert
        String mutation = "mutation { register(input: {username: \"newuser\", password: \"SecurePass123!\", fullName: \"New User\", email: \"new@example.com\", gender: \"Male\"}) }";
        graphQlTester.document(mutation)
                .execute()
                .path("register").entity(Boolean.class).isEqualTo(true);

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    @DisplayName("Mutation: createPost - Should create post successfully")
    void testCreatePostMutation() {
        // Arrange
        Post savedPost = new Post(1, 1, "New Post", "New Body", false, LocalDateTime.now());
        when(postService.save(any(CreatePostDTO.class))).thenReturn(savedPost);

        // Act & Assert
        String mutation = "mutation { createPost(input: {userId: 1, title: \"New Post\", body: \"New Body\", draft: false}) { id title } }";
        graphQlTester.document(mutation)
                .execute()
                .path("createPost.id").entity(Integer.class).isEqualTo(1)
                .path("createPost.title").entity(String.class).isEqualTo("New Post");

        verify(postService, times(1)).save(any(CreatePostDTO.class));
    }

    @Test
    @DisplayName("Mutation: createPost - Should create draft post")
    void testCreateDraftPostMutation() {
        // Arrange
        Post savedPost = new Post(1, 1, "Draft Post", "Draft Body", true, LocalDateTime.now());
        when(postService.save(any(CreatePostDTO.class))).thenReturn(savedPost);

        // Act & Assert
        String mutation = "mutation { createPost(input: {userId: 1, title: \"Draft Post\", body: \"Draft Body\", draft: true}) { draft } }";
        graphQlTester.document(mutation)
                .execute()
                .path("createPost.draft").entity(Boolean.class).isEqualTo(true);

        verify(postService, times(1)).save(any(CreatePostDTO.class));
    }

    @Test
    @DisplayName("Mutation: updatePost - Should update post successfully")
    void testUpdatePostMutation() {
        // Arrange
        Post updatedPost = new Post(1, 1, "Updated Post", "Updated Body", false, LocalDateTime.now());
        when(postService.update(any(UpdatePostDTO.class))).thenReturn(updatedPost);

        // Act & Assert
        String mutation = "mutation { updatePost(input: {postId: 1, userId: 1, title: \"Updated Post\", body: \"Updated Body\", draft: false}) { title } }";
        graphQlTester.document(mutation)
                .execute()
                .path("updatePost.title").entity(String.class).isEqualTo("Updated Post");

        verify(postService, times(1)).update(any(UpdatePostDTO.class));
    }

    @Test
    @DisplayName("Mutation: updatePost - Should update post to draft")
    void testUpdatePostToDraftMutation() {
        // Arrange
        Post updatedPost = new Post(1, 1, "Updated Post", "Updated Body", true, LocalDateTime.now());
        when(postService.update(any(UpdatePostDTO.class))).thenReturn(updatedPost);

        // Act & Assert
        String mutation = "mutation { updatePost(input: {postId: 1, userId: 1, title: \"Updated Post\", body: \"Updated Body\", draft: true}) { draft } }";
        graphQlTester.document(mutation)
                .execute()
                .path("updatePost.draft").entity(Boolean.class).isEqualTo(true);

        verify(postService, times(1)).update(any(UpdatePostDTO.class));
    }

    @Test
    @DisplayName("Mutation: deletePost - Should delete post successfully")
    void testDeletePostMutation() {
        // Arrange
        doNothing().when(postService).delete(1);

        // Act & Assert
        graphQlTester.document("mutation { deletePost(id: 1) }")
                .execute()
                .path("deletePost").entity(Boolean.class).isEqualTo(true);

        verify(postService, times(1)).delete(1);
    }

    @Test
    @DisplayName("Mutation: addComment - Should add comment successfully")
    void testAddCommentMutation() {
        // Arrange
        Comment savedComment = new Comment(1, 1, 1, "Great post!", LocalDateTime.now());
        when(commentService.save(any(CreateCommentDTO.class))).thenReturn(savedComment);

        // Act & Assert
        String mutation = "mutation { addComment(input: {userId: 1, postId: 1, body: \"Great post!\"}) }";
        graphQlTester.document(mutation)
                .execute()
                .path("addComment").entity(Boolean.class).isEqualTo(true);

        verify(commentService, times(1)).save(any(CreateCommentDTO.class));
    }

    @Test
    @DisplayName("Mutation: addComment - Should add comment with long body")
    void testAddLongCommentMutation() {
        // Arrange
        String longBody = "This is a very long comment that contains multiple sentences and provides detailed feedback about the post.";
        Comment savedComment = new Comment(1, 1, 1, longBody, LocalDateTime.now());
        when(commentService.save(any(CreateCommentDTO.class))).thenReturn(savedComment);

        // Act & Assert
        String mutation = "mutation { addComment(input: {userId: 1, postId: 1, body: \"" + longBody + "\"}) }";
        graphQlTester.document(mutation)
                .execute()
                .path("addComment").entity(Boolean.class).isEqualTo(true);

        verify(commentService, times(1)).save(any(CreateCommentDTO.class));
    }

    @Test
    @DisplayName("Mutation: updateComment - Should update comment successfully")
    void testUpdateCommentMutation() {
        // Arrange
        Comment updatedComment = new Comment(1, 1, 1, "Updated comment", LocalDateTime.now());
        when(commentService.update(any(UpdateCommentDTO.class))).thenReturn(updatedComment);

        // Act & Assert
        String mutation = "mutation { updateComment(input: {id: 1, userId: 1, postId: 1, body: \"Updated comment\"}) }";
        graphQlTester.document(mutation)
                .execute()
                .path("updateComment").entity(Boolean.class).isEqualTo(true);

        verify(commentService, times(1)).update(any(UpdateCommentDTO.class));
    }

    @Test
    @DisplayName("Mutation: deleteComment - Should delete comment successfully")
    void testDeleteCommentMutation() {
        // Arrange
        doNothing().when(commentService).delete(1);

        // Act & Assert
        graphQlTester.document("mutation { deleteComment(id: 1) }")
                .execute()
                .path("deleteComment").entity(Boolean.class).isEqualTo(true);

        verify(commentService, times(1)).delete(1);
    }

    @Test
    @DisplayName("Mutation: setPostTags - Should set post tags successfully")
    void testSetPostTagsMutation() {
        // Arrange
        doNothing().when(tagService).setPostTags(any(PostTagsDTO.class));

        // Act & Assert
        graphQlTester.document("mutation { setPostTags(postID: 1, tags: [\"Java\", \"Spring\"]) }")
                .execute()
                .path("setPostTags").entity(Boolean.class).isEqualTo(true);

        verify(tagService, times(1)).setPostTags(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("Mutation: setPostTags - Should set multiple tags")
    void testSetMultiplePostTagsMutation() {
        // Arrange
        doNothing().when(tagService).setPostTags(any(PostTagsDTO.class));

        // Act & Assert
        graphQlTester.document("mutation { setPostTags(postID: 1, tags: [\"Java\", \"Spring\", \"Boot\", \"Microservices\"]) }")
                .execute()
                .path("setPostTags").entity(Boolean.class).isEqualTo(true);

        verify(tagService, times(1)).setPostTags(any(PostTagsDTO.class));
    }

    // ==================== SCHEMA MAPPING TESTS ====================

    @Test
    @DisplayName("SchemaMapping: Post.tags - Should get tags for post")
    void testGetTagsSchemaMapping() {
        // Arrange
        Post post = new Post(1, 1, "Test Post", "Test Body", false, LocalDateTime.now());
        List<String> tags = Arrays.asList("Java", "Spring");
        when(tagService.findByPostId(1)).thenReturn(tags);

        // Act & Assert
        graphQlTester.document("{ findPostByID(id: 1) { tags } }")
                .execute()
                .path("findPostByID.tags[0]").entity(String.class).isEqualTo("Java");

        verify(tagService, times(1)).findByPostId(1);
    }

    @Test
    @DisplayName("SchemaMapping: Post.comments - Should get comments for post")
    void testGetCommentsSchemaMapping() {
        // Arrange
        Post post = new Post(1, 1, "Test Post", "Test Body", false, LocalDateTime.now());
        List<Comment> comments = Arrays.asList(
            new Comment(1, 1, 1, "Great post!", LocalDateTime.now())
        );
        when(postService.findById(1)).thenReturn(Optional.of(post));
        when(commentService.findByPostId(1)).thenReturn(comments);

        // Act & Assert
        graphQlTester.document("{ findPostByID(id: 1) { comments { body } } }")
                .execute()
                .path("findPostByID.comments[0].body").entity(String.class).isEqualTo("Great post!");

        verify(commentService, times(1)).findByPostId(1);
    }

    @Test
    @DisplayName("Query: findPostByID - Should get complete post with all fields")
    void testFindPostByIDWithAllFields() {
        // Arrange
        Post post = new Post(1, 1, "Test Post", "Test Body", false, LocalDateTime.now());
        when(postService.findById(1)).thenReturn(Optional.of(post));
        when(tagService.findByPostId(1)).thenReturn(Arrays.asList("Java"));
        when(commentService.findByPostId(1)).thenReturn(Arrays.asList(new Comment(1, 1, 1, "Comment", LocalDateTime.now())));

        // Act & Assert
        graphQlTester.document("{ findPostByID(id: 1) { id userId username title body draft createdAt tags comments { body } } }")
                .execute()
                .path("findPostByID.id").entity(Integer.class).isEqualTo(1)
                .path("findPostByID.title").entity(String.class).isEqualTo("Test Post")
                .path("findPostByID.tags[0]").entity(String.class).isEqualTo("Java");

        verify(postService, times(1)).findById(1);
    }
}
