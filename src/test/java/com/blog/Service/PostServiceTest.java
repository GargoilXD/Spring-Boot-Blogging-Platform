package com.blog.Service;

import com.blog.Model.User;
import com.blog.Model.Post;
import com.blog.Repository.CommentRepository;
import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TagService tagService;

    @Mock
    private User user;

    @Mock
    private Post post;

    @InjectMocks
    private PostService postService;

    private CreatePostDTO createDTO;
    private UpdatePostDTO updateDTO;

    @BeforeEach
    void setUp() {
        createDTO = new CreatePostDTO(1, "Title", "Content", false);
        updateDTO = new UpdatePostDTO(1, "Updated Title", "Updated Content", false);
    }

    @Test
    void findById_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(post));

        Optional<Post> result = postService.findById(1);

        assertTrue(result.isPresent());
        verify(repository).findById(1);
    }

    @Test
    void findById_NotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        Optional<Post> result = postService.findById(1);

        assertFalse(result.isPresent());
        verify(repository).findById(1);
    }

    @Test
    void findAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post));
        when(repository.findAll(pageable)).thenReturn(postPage);

        Page<Post> result = postService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository).findAll(pageable);
    }

    @Test
    void count_Success() {
        when(repository.count()).thenReturn(5L);

        long result = postService.count();

        assertEquals(5L, result);
        verify(repository).count();
    }

    @Test
    void save_Success() {
        when(userRepository.findById(createDTO.userId())).thenReturn(Optional.of(user));
        when(repository.save(any(Post.class))).thenReturn(post);

        Post result = postService.save(createDTO);

        assertNotNull(result);
        verify(userRepository).findById(createDTO.userId());
        verify(repository).save(any(Post.class));
    }

    @Test
    void save_Failure_UserNotFound() {
        when(userRepository.findById(createDTO.userId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.save(createDTO)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void update_Failure_PostNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.update(1, updateDTO)
        );

        assertTrue(exception.getMessage().contains("Post not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void update_Failure_UserNotFound() {
        when(repository.findById(1)).thenReturn(Optional.of(post));
        when(userRepository.findById(updateDTO.userId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.update(1, updateDTO)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void delete_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(post));
        doNothing().when(tagService).deleteByPostId(1);
        doNothing().when(commentRepository).deleteByPostId(1);
        doNothing().when(repository).deleteById(1);

        assertDoesNotThrow(() -> postService.delete(1));

        verify(tagService).deleteByPostId(1);
        verify(commentRepository).deleteByPostId(1);
        verify(repository).deleteById(1);
    }

    @Test
    void delete_Failure_PostNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.delete(1)
        );

        assertTrue(exception.getMessage().contains("Post not found"));
        verify(tagService, never()).deleteByPostId(anyInt());
        verify(commentRepository, never()).deleteByPostId(anyInt());
        verify(repository, never()).deleteById(anyInt());
    }
}