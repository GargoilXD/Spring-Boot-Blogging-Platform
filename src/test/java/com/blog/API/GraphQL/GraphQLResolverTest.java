package com.blog.API.GraphQL;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.Comment;
import com.blog.Model.Post;
import com.blog.Model.PostTags;
import com.blog.Service.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphQLResolver Unit Tests")
class GraphQLResolverTest {

    @Mock private PostService           postService;
    @Mock private AuthenticationService authService;
    @Mock private CommentService        commentService;
    @Mock private TagService            tagService;
    @Mock private Post                  post;
    @Mock private Comment               comment;

    @InjectMocks
    private GraphQLResolver graphQLResolver;

    private PostTags postTags;

    @BeforeEach
    void setUp() {
        postTags = new PostTags("1", 1, List.of("tag1", "tag2"));
    }

    // ─── Query: findPostByID ────────────────────────────────────────────────

    @Nested
    @DisplayName("findPostByID")
    class FindPostByID {

        @Test
        @DisplayName("Returns post when found")
        void findPostByID_Found() {
            when(postService.findById(1)).thenReturn(Optional.of(post));
            Post result = graphQLResolver.findPostByID(1);
            assertNotNull(result);
            assertEquals(post, result);
        }

        @Test
        @DisplayName("Returns null when not found")
        void findPostByID_NotFound() {
            when(postService.findById(99)).thenReturn(Optional.empty());
            assertNull(graphQLResolver.findPostByID(99));
        }
    }

    // ─── Query: findAllPosts ────────────────────────────────────────────────

    @Nested
    @DisplayName("findAllPosts")
    class FindAllPosts {

        @Test
        @DisplayName("Uses default page/size when params are null")
        void findAllPosts_DefaultParams() {
            Page<Post> postPage = new PageImpl<>(List.of(post));
            when(postService.findAll(PageRequest.of(0, 5))).thenReturn(postPage);

            List<Post> result = graphQLResolver.findAllPosts(null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Uses custom page/size when provided")
        void findAllPosts_CustomParams() {
            Page<Post> postPage = new PageImpl<>(List.of(post));
            when(postService.findAll(PageRequest.of(2, 10))).thenReturn(postPage);

            List<Post> result = graphQLResolver.findAllPosts(2, 10);

            assertEquals(1, result.size());
        }
    }

    // ─── Query: findAllTags ─────────────────────────────────────────────────

    @Test
    @DisplayName("findAllTags: flattens tags across all PostTags pages")
    void findAllTags_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostTags> tagsPage = new PageImpl<>(List.of(postTags));
        when(tagService.findAll(pageable)).thenReturn(tagsPage);

        List<String> result = graphQLResolver.findAllTags(0, 10);

        assertEquals(2, result.size());
        assertTrue(result.contains("tag1"));
        assertTrue(result.contains("tag2"));
    }

    // ─── Query: findTagsForPost ─────────────────────────────────────────────

    @Test
    @DisplayName("findTagsForPost: returns tags for a specific post")
    void findTagsForPost_Success() {
        when(tagService.findByPostId(1)).thenReturn(List.of("tag1", "tag2"));
        List<String> result = graphQLResolver.findTagsForPost(1);
        assertEquals(2, result.size());
    }

    // ─── Query: findCommentsForPost ─────────────────────────────────────────

    @Test
    @DisplayName("findCommentsForPost: returns comments for a specific post")
    void findCommentsForPost_Success() {
        when(commentService.findByPostId(1)).thenReturn(List.of(comment));
        List<Comment> result = graphQLResolver.findCommentsForPost(1);
        assertEquals(1, result.size());
    }

    // ─── Mutation: login ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Mutation: login")
    class Login {

        @Test
        @DisplayName("Returns true on successful login")
        void login_Success() {
            when(authService.login("user", "pass"))
                .thenReturn(Map.of("accessToken", "acc", "refreshToken", "ref", "type", "Bearer"));

            Boolean result = graphQLResolver.login("user", "pass");

            assertTrue(result);
            verify(authService).login("user", "pass");
        }

        @Test
        @DisplayName("Propagates exception on failed login")
        void login_Failure_PropagatesException() {
            when(authService.login("user", "wrong"))
                .thenThrow(new RuntimeException("Invalid credentials"));

            assertThrows(RuntimeException.class, () -> graphQLResolver.login("user", "wrong"));
        }
    }

    // ─── Mutation: register ─────────────────────────────────────────────────

    @Test
    @DisplayName("register: returns true on successful registration")
    void register_Success() {
        RegisterUserDTO input = new RegisterUserDTO("user", "password123", "User Name", "user@email.com", "Other");
        doNothing().when(authService).register(input);

        assertTrue(graphQLResolver.register(input));
        verify(authService).register(input);
    }

    // ─── Mutation: createPost ───────────────────────────────────────────────

    @Test
    @DisplayName("createPost: returns created Post")
    void createPost_Success() {
        CreatePostDTO input = new CreatePostDTO("Title", "Content", false);
        when(postService.save(input)).thenReturn(post);

        Post result = graphQLResolver.createPost(input);

        assertNotNull(result);
        assertEquals(post, result);
    }

    // ─── Mutation: updatePost ───────────────────────────────────────────────

    @Test
    @DisplayName("updatePost: returns updated Post")
    void updatePost_Success() {
        UpdatePostDTO input = new UpdatePostDTO("Title", "Content", false);
        when(postService.update(1, input)).thenReturn(post);

        Post result = graphQLResolver.updatePost(1, input);

        assertNotNull(result);
    }

    // ─── Mutation: deletePost ───────────────────────────────────────────────

    @Nested
    @DisplayName("Mutation: deletePost")
    class DeletePost {

        @Test
        @DisplayName("Returns true on success")
        void deletePost_Success() {
            doNothing().when(postService).delete(1);
            assertTrue(graphQLResolver.deletePost(1));
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException")
        void deletePost_Failure() {
            doThrow(new EntityNotFoundException("Post not found")).when(postService).delete(99);
            assertThrows(EntityNotFoundException.class, () -> graphQLResolver.deletePost(99));
        }
    }

    // ─── Mutation: addComment ───────────────────────────────────────────────

    @Test
    @DisplayName("addComment: returns true on success")
    void addComment_Success() {
        CreateCommentDTO input = new CreateCommentDTO(1, "Content");
        when(commentService.save(input)).thenReturn(comment);
        assertTrue(graphQLResolver.addComment(input));
    }

    // ─── Mutation: updateComment ────────────────────────────────────────────

    @Test
    @DisplayName("updateComment: returns true on success")
    void updateComment_Success() {
        UpdateCommentDTO input = new UpdateCommentDTO(1, "Updated");
        when(commentService.update(1, input)).thenReturn(comment);
        assertTrue(graphQLResolver.updateComment(1, input));
    }

    // ─── Mutation: deleteComment ────────────────────────────────────────────

    @Test
    @DisplayName("deleteComment: returns true on success")
    void deleteComment_Success() {
        doNothing().when(commentService).delete(1);
        assertTrue(graphQLResolver.deleteComment(1));
    }

    // ─── Mutation: tag operations ───────────────────────────────────────────

    @Test
    @DisplayName("setPostTags: returns true on success")
    void setPostTags_Success() {
        doNothing().when(tagService).setPostTags(1, List.of("tag1", "tag2"));
        assertTrue(graphQLResolver.setPostTags(1, List.of("tag1", "tag2")));
    }

    @Test
    @DisplayName("addTagsToPost: returns true on success")
    void addTagsToPost_Success() {
        doNothing().when(tagService).addTagsToPost(1, List.of("tag3"));
        assertTrue(graphQLResolver.addTagsToPost(1, List.of("tag3")));
    }

    @Test
    @DisplayName("removeTagsFromPost: returns true on success")
    void removeTagsFromPost_Success() {
        doNothing().when(tagService).removeTagsFromPost(1, List.of("tag1"));
        assertTrue(graphQLResolver.removeTagsFromPost(1, List.of("tag1")));
    }

    @Test
    @DisplayName("deleteByPostId: returns true on success")
    void deleteByPostId_Success() {
        doNothing().when(tagService).deleteByPostId(1);
        assertTrue(graphQLResolver.deleteByPostId(1));
    }
}
