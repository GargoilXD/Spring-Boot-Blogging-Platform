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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository repository;

    @Mock
    private User user;

    @Mock
    private Post post;

    @Mock
    private Comment comment;

    @InjectMocks
    private CommentService commentService;

    private CreateCommentDTO createDTO;
    private UpdateCommentDTO updateDTO;

    @BeforeEach
    void setUp() {
        createDTO = new CreateCommentDTO(1, 1, "Content");
        updateDTO = new UpdateCommentDTO(1, 1, "Updated Content");
    }

    @Test
    void findByPostId_Success() {
        int postId = 1;
        when(repository.findByPostId(postId)).thenReturn(List.of(comment));

        List<Comment> result = commentService.findByPostId(postId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findByPostId(postId);
    }

    @Test
    void save_Success() {
        when(userRepository.findById(createDTO.userId())).thenReturn(Optional.of(user));
        when(postRepository.findById(createDTO.postId())).thenReturn(Optional.of(post));
        when(repository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.save(createDTO);

        assertNotNull(result);
        verify(userRepository).findById(createDTO.userId());
        verify(postRepository).findById(createDTO.postId());
        verify(repository).save(any(Comment.class));
    }

    @Test
    void save_Failure_UserNotFound() {
        when(userRepository.findById(createDTO.userId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.save(createDTO)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(postRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void save_Failure_PostNotFound() {
        when(userRepository.findById(createDTO.userId())).thenReturn(Optional.of(user));
        when(postRepository.findById(createDTO.postId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.save(createDTO)
        );

        assertTrue(exception.getMessage().contains("Post not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void update_Failure_CommentNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.update(1, updateDTO)
        );

        assertTrue(exception.getMessage().contains("Comment not found"));
    }

    @Test
    void delete_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(comment));
        doNothing().when(repository).deleteById(1);

        assertDoesNotThrow(() -> commentService.delete(1));

        verify(repository).deleteById(1);
    }

    @Test
    void delete_Failure_NotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.delete(1)
        );

        assertTrue(exception.getMessage().contains("Comment not found"));
        verify(repository, never()).deleteById(any());
    }
}