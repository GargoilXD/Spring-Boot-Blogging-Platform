package com.blog.Service;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;
import com.blog.Model.Post;
import com.blog.Model.User;
import com.blog.Repository.CommentRepository;
import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

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

    @Test
    @DisplayName("Should find comments by post ID")
    void testFindCommentsByPostId() {
        // Arrange
        int postId = 1;
        List<Comment> comments = Arrays.asList(
            new Comment(1, 1, 1, "Great post!", LocalDateTime.now()),
            new Comment(2, 2, 1, "Very informative", LocalDateTime.now())
        );

        when(commentRepository.findByPostId(postId)).thenReturn(comments);

        // Act
        List<Comment> result = commentService.findByPostId(postId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Great post!", result.get(0).getBody());
        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    @DisplayName("Should return empty list when no comments found for post")
    void testFindCommentsByPostIdEmpty() {
        // Arrange
        int postId = 999;
        when(commentRepository.findByPostId(postId)).thenReturn(Arrays.asList());

        // Act
        List<Comment> result = commentService.findByPostId(postId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    @DisplayName("Should successfully create and save a comment")
    void testSaveCommentSuccess() {
        // Arrange
        CreateCommentDTO dto = new CreateCommentDTO(1, 1, "Great post!");
        Comment savedComment = new Comment(1, 1, 1, "Great post!", LocalDateTime.now());

        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
        when(postRepository.findById(1)).thenReturn(Optional.of(new Post()));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        Comment result = commentService.save(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Great post!", result.getBody());
        verify(userRepository, times(1)).findById(1);
        verify(postRepository, times(1)).findById(1);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should successfully update an existing comment")
    void testUpdateCommentSuccess() {
        // Arrange
        UpdateCommentDTO dto = new UpdateCommentDTO(1, 1, 1, "Updated comment");
        Comment existingComment = new Comment(1, 1, 1, "Old comment", LocalDateTime.now());
        Comment updatedComment = new Comment(1, 1, 1, "Updated comment", LocalDateTime.now());
        User user = new User(1, "user", "hash", "User", "user@test.com", "M", LocalDateTime.now());
        Post post = new Post(1, 1, "Post", "Body", false, LocalDateTime.now());

        when(commentRepository.findById(1)).thenReturn(Optional.of(existingComment));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

        // Act
        Comment result = commentService.update(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated comment", result.getBody());
        verify(commentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);
        verify(postRepository, times(1)).findById(1);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent comment")
    void testUpdateCommentNotFound() {
        // Arrange
        UpdateCommentDTO dto = new UpdateCommentDTO(999, 1, 1, "Updated comment");

        when(commentRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> commentService.update(dto));

        verify(commentRepository, times(1)).findById(999);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should successfully delete a comment")
    void testDeleteCommentSuccess() {
        // Arrange
        int commentId = 1;
        Comment comment = new Comment(1, 1, 1, "Comment to delete", LocalDateTime.now());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act
        assertDoesNotThrow(() -> commentService.delete(commentId));

        // Assert
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent comment")
    void testDeleteCommentNotFound() {
        // Arrange
        int commentId = 999;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> commentService.delete(commentId));

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Should handle multiple comments for same post")
    void testFindMultipleCommentsForPost() {
        // Arrange
        int postId = 5;
        List<Comment> comments = Arrays.asList(
            new Comment(1, 1, 5, "First comment", LocalDateTime.now()),
            new Comment(2, 2, 5, "Second comment", LocalDateTime.now()),
            new Comment(3, 3, 5, "Third comment", LocalDateTime.now())
        );

        when(commentRepository.findByPostId(postId)).thenReturn(comments);

        // Act
        List<Comment> result = commentService.findByPostId(postId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(commentRepository, times(1)).findByPostId(postId);
    }
}
