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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQLResolverTest {

    @Mock
    private PostService postService;

    @Mock
    private AuthenticationService authService;

    @Mock
    private CommentService commentService;

    @Mock
    private TagService tagService;

    @Mock
    private Post post;

    @Mock
    private Comment comment;

    @Mock
    private PostTags postTags;

    @InjectMocks
    private GraphQLResolver graphQLResolver;

    @BeforeEach
    void setUp() {
        postTags = new PostTags("1", 1, List.of("tag1", "tag2"));
    }

    // ==================== Query Mappings ====================

    @Test
    void findPostByID_Found() {
        when(postService.findById(1)).thenReturn(Optional.of(post));

        Post result = graphQLResolver.findPostByID(1);

        assertNotNull(result);
        assertEquals(post, result);
        verify(postService).findById(1);
    }

    @Test
    void findPostByID_NotFound() {
        when(postService.findById(1)).thenReturn(Optional.empty());

        Post result = graphQLResolver.findPostByID(1);

        assertNull(result);
        verify(postService).findById(1);
    }

    @Test
    void findAllPosts_WithDefaultParams() {
        Page<Post> postPage = new PageImpl<>(List.of(post));
        when(postService.findAll(PageRequest.of(0, 5))).thenReturn(postPage);

        List<Post> result = graphQLResolver.findAllPosts(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postService).findAll(PageRequest.of(0, 5));
    }

    @Test
    void findAllPosts_WithCustomParams() {
        Page<Post> postPage = new PageImpl<>(List.of(post));
        when(postService.findAll(PageRequest.of(2, 10))).thenReturn(postPage);

        List<Post> result = graphQLResolver.findAllPosts(2, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postService).findAll(PageRequest.of(2, 10));
    }

    @Test
    void findAllTags_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostTags> tagsPage = new PageImpl<>(List.of(postTags));
        when(tagService.findAll(pageable)).thenReturn(tagsPage);

        List<String> result = graphQLResolver.findAllTags(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("tag1"));
        assertTrue(result.contains("tag2"));
        verify(tagService).findAll(pageable);
    }

    @Test
    void findTagsForPost_Success() {
        when(tagService.findByPostId(1)).thenReturn(List.of("tag1", "tag2"));

        List<String> result = graphQLResolver.findTagsForPost(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tagService).findByPostId(1);
    }

    @Test
    void findCommentsForPost_Success() {
        when(commentService.findByPostId(1)).thenReturn(List.of(comment));

        List<Comment> result = graphQLResolver.findCommentsForPost(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(commentService).findByPostId(1);
    }

    // ==================== Mutation Mappings - Auth ====================

    @Test
    void login_Success() {
        doNothing().when(authService).login("user", "pass");

        Boolean result = graphQLResolver.login("user", "pass");

        assertTrue(result);
        verify(authService).login("user", "pass");
    }

    @Test
    void login_Failure_PropagatesException() {
        doThrow(new RuntimeException("Invalid credentials"))
                .when(authService).login("user", "wrong");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> graphQLResolver.login("user", "wrong")
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authService).login("user", "wrong");
    }

    @Test
    void register_Success() {
        RegisterUserDTO input = new RegisterUserDTO("user", "password123", "User Name", "adsdad@email.com", "Other");
        doNothing().when(authService).register(input);

        Boolean result = graphQLResolver.register(input);

        assertTrue(result);
        verify(authService).register(input);
    }

    // ==================== Mutation Mappings - Post ====================

    @Test
    void createPost_Success() {
        CreatePostDTO input = new CreatePostDTO(1, "Title", "Content", false);
        when(postService.save(input)).thenReturn(post);

        Post result = graphQLResolver.createPost(input);

        assertNotNull(result);
        assertEquals(post, result);
        verify(postService).save(input);
    }

    @Test
    void updatePost_Success() {
        UpdatePostDTO input = new UpdatePostDTO(1, "Title", "Content", false);
        when(postService.update(1, input)).thenReturn(post);

        Post result = graphQLResolver.updatePost(1, input);

        assertNotNull(result);
        assertEquals(post, result);
        verify(postService).update(1, input);
    }

    @Test
    void deletePost_Success() {
        doNothing().when(postService).delete(1);

        Boolean result = graphQLResolver.deletePost(1);

        assertTrue(result);
        verify(postService).delete(1);
    }

    @Test
    void deletePost_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Post not found"))
                .when(postService).delete(1);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> graphQLResolver.deletePost(1)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(postService).delete(1);
    }

    // ==================== Mutation Mappings - Comment ====================

    @Test
    void addComment_Success() {
        CreateCommentDTO input = new CreateCommentDTO(1, 1, "Content");
        when(commentService.save(input)).thenReturn(comment);

        Boolean result = graphQLResolver.addComment(input);

        assertTrue(result);
        verify(commentService).save(input);
    }

    @Test
    void updateComment_Success() {
        UpdateCommentDTO input = new UpdateCommentDTO(1, 1, "Updated");
        when(commentService.update(1, input)).thenReturn(comment);
        Boolean result = graphQLResolver.updateComment(1, input);
        assertTrue(result);
        verify(commentService).update(1, input);
    }

    @Test
    void deleteComment_Success() {
        doNothing().when(commentService).delete(1);

        Boolean result = graphQLResolver.deleteComment(1);

        assertTrue(result);
        verify(commentService).delete(1);
    }

    // ==================== Mutation Mappings - Tags ====================

    @Test
    void setPostTags_Success() {
        doNothing().when(tagService).setPostTags(1, List.of("tag1", "tag2"));

        Boolean result = graphQLResolver.setPostTags(1, List.of("tag1", "tag2"));

        assertTrue(result);
        verify(tagService).setPostTags(1, List.of("tag1", "tag2"));
    }

    @Test
    void addTagsToPost_Success() {
        doNothing().when(tagService).addTagsToPost(1, List.of("tag3"));

        Boolean result = graphQLResolver.addTagsToPost(1, List.of("tag3"));

        assertTrue(result);
        verify(tagService).addTagsToPost(1, List.of("tag3"));
    }

    @Test
    void removeTagsFromPost_Success() {
        doNothing().when(tagService).removeTagsFromPost(1, List.of("tag1"));

        Boolean result = graphQLResolver.removeTagsFromPost(1, List.of("tag1"));

        assertTrue(result);
        verify(tagService).removeTagsFromPost(1, List.of("tag1"));
    }

    @Test
    void deleteByPostId_Success() {
        doNothing().when(tagService).deleteByPostId(1);

        Boolean result = graphQLResolver.deleteByPostId(1);

        assertTrue(result);
        verify(tagService).deleteByPostId(1);
    }
}