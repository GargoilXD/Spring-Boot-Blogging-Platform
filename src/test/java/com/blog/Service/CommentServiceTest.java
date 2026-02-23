package com.blog.Service;

import com.blog.Model.Post;
import com.blog.Model.User;
import com.blog.Model.Comment;
import com.blog.Repository.CommentRepository;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Unit Tests")
class CommentServiceTest {

    @Mock private UserRepository    userRepository;
    @Mock private PostRepository    postRepository;
    @Mock private CommentRepository repository;
    @Mock private User              user;
    @Mock private Post              post;
    @Mock private Comment           comment;
    @Mock private SecurityContext   securityContext;
    @Mock private Authentication    authentication;

    @InjectMocks
    private CommentService commentService;

    private CreateCommentDTO createDTO;
    private UpdateCommentDTO updateDTO;

    @BeforeEach
    void setUp() {
        createDTO = new CreateCommentDTO(1, "Content");
        updateDTO = new UpdateCommentDTO(1, "Updated Content");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── findByPostId ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByPostId")
    class FindByPostId {

        @Test
        @DisplayName("Returns list of comments for the given post")
        void findByPostId_Success() {
            when(repository.findByPostId(1)).thenReturn(List.of(comment));

            List<Comment> result = commentService.findByPostId(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(repository).findByPostId(1);
        }

        @Test
        @DisplayName("Returns empty list when no comments exist for the post")
        void findByPostId_EmptyList() {
            when(repository.findByPostId(99)).thenReturn(List.of());

            List<Comment> result = commentService.findByPostId(99);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Creates comment using authenticated user from SecurityContext")
        void save_Success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(postRepository.findById(1)).thenReturn(Optional.of(post));
            when(repository.save(any(Comment.class))).thenReturn(comment);

            Comment result = commentService.save(createDTO);

            assertNotNull(result);
            verify(userRepository).findByUsername("testuser");
            verify(postRepository).findById(1);
            verify(repository).save(any(Comment.class));
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when authenticated user not found in DB")
        void save_Failure_UserNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.save(createDTO)
            );

            assertTrue(ex.getMessage().contains("Authenticated user not found"));
            verify(postRepository, never()).findById(any());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when referenced post not found")
        void save_Failure_PostNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(postRepository.findById(1)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.save(createDTO)
            );

            assertTrue(ex.getMessage().contains("Post not found"));
            verify(repository, never()).save(any());
        }
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Updates comment when authenticated user is the owner")
        void update_Success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.findById(1)).thenReturn(Optional.of(comment));
            when(postRepository.findById(1)).thenReturn(Optional.of(post));
            when(user.getId()).thenReturn(10);
            when(post.getId()).thenReturn(1);
            when(comment.getUser()).thenReturn(user);
            when(comment.getPost()).thenReturn(post);
            when(repository.save(comment)).thenReturn(comment);

            Comment result = commentService.update(1, updateDTO);

            assertNotNull(result);
            verify(repository).save(comment);
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when authenticated user not found in DB")
        void update_Failure_UserNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.update(1, updateDTO)
            );

            assertTrue(ex.getMessage().contains("Authenticated user not found"));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when comment does not exist")
        void update_Failure_CommentNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.findById(99)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.update(99, updateDTO)
            );

            assertTrue(ex.getMessage().contains("Comment not found"));
        }

        @Test
        @DisplayName("Throws SecurityException when authenticated user does not own the comment")
        void update_Failure_NotOwner() {
            User differentUser = mock(User.class);
            when(differentUser.getId()).thenReturn(999);
            when(user.getId()).thenReturn(1);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.findById(1)).thenReturn(Optional.of(comment));
            when(postRepository.findById(1)).thenReturn(Optional.of(post));
            when(comment.getUser()).thenReturn(differentUser);

            assertThrows(SecurityException.class, () -> commentService.update(1, updateDTO));
            verify(repository, never()).save(any());
        }
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deletes comment successfully")
        void delete_Success() {
            when(repository.findById(1)).thenReturn(Optional.of(comment));
            doNothing().when(repository).deleteById(1);

            assertDoesNotThrow(() -> commentService.delete(1));

            verify(repository).deleteById(1);
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when comment does not exist")
        void delete_Failure_NotFound() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.delete(99)
            );

            assertTrue(ex.getMessage().contains("Comment not found"));
            verify(repository, never()).deleteById(any());
        }
    }
}