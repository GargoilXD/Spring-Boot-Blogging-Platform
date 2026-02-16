package com.blog.Service;

import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

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
    @DisplayName("Should find post by ID")
    void testFindPostById() {
        // Arrange
        int postId = 1;
        Post post = new Post(1, 1, "Test Post", "Test body", false, LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // Act
        Optional<Post> result = postService.findById(postId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Post", result.get().getTitle());
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("Should return empty optional when post not found")
    void testFindPostByIdNotFound() {
        // Arrange
        int postId = 999;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act
        Optional<Post> result = postService.findById(postId);

        // Assert
        assertTrue(result.isEmpty());
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("Should retrieve all posts with pagination")
    void testFindAllPostsWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = Arrays.asList(
            new Post(1, 1, "Post 1", "Body 1", false, LocalDateTime.now()),
            new Post(2, 1, "Post 2", "Body 2", true, LocalDateTime.now())
        );
        Page<Post> page = new PageImpl<>(posts, pageable, 2);

        when(postRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Post> result = postService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getNumber());
        verify(postRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should retrieve post count")
    void testCountPosts() {
        // Arrange
        when(postRepository.count()).thenReturn(5L);

        // Act
        long result = postService.count();

        // Assert
        assertEquals(5L, result);
        verify(postRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should successfully create and save a post")
    void testSavePostSuccess() {
        // Arrange
        CreatePostDTO dto = new CreatePostDTO(1, "New Post", "New body content", false);
        Post savedPost = new Post(1, 1, "New Post", "New body content", false, LocalDateTime.now());

        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        Post result = postService.save(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Post", result.getTitle());
        verify(userRepository, times(1)).findById(1);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should successfully update an existing post")
    void testUpdatePostSuccess() {
        // Arrange
        UpdatePostDTO dto = new UpdatePostDTO(1, 1, "Updated Post", "Updated body", false);
        Post existingPost = new Post(1, 1, "Old Post", "Old body", false, LocalDateTime.now());
        Post updatedPost = new Post(1, 1, "Updated Post", "Updated body", false, LocalDateTime.now());
        User user = new User(1, "user", "hash", "User", "user@test.com", "M", LocalDateTime.now());

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        // Act
        Post result = postService.update(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Post", result.getTitle());
        verify(postRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent post")
    void testUpdatePostNotFound() {
        // Arrange
        UpdatePostDTO dto = new UpdatePostDTO(999, 1, "Updated Post", "Updated body", false);

        when(postRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> postService.update(dto));

        verify(postRepository, times(1)).findById(999);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should successfully delete a post")
    void testDeletePostSuccess() {
        // Arrange
        int postId = 1;
        Post post = new Post(1, 1, "Post to delete", "Body", false, LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(tagService).deleteByPostId(postId);
        doNothing().when(commentRepository).deleteByPostId(postId);

        // Act
        assertDoesNotThrow(() -> postService.delete(postId));

        // Assert
        verify(postRepository, times(1)).findById(postId);
        verify(tagService, times(1)).deleteByPostId(postId);
        verify(commentRepository, times(1)).deleteByPostId(postId);
        verify(postRepository, times(1)).deleteById(postId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent post")
    void testDeletePostNotFound() {
        // Arrange
        int postId = 999;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> postService.delete(postId));

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Should return correct post count after operations")
    void testPostCountAfterOperations() {
        // Arrange
        when(postRepository.count()).thenReturn(10L);

        // Act
        long result = postService.count();

        // Assert
        assertEquals(10L, result);
        verify(postRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should handle empty page of posts")
    void testFindAllPostsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(postRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Post> result = postService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(postRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should retrieve posts with different pagination sizes")
    void testFindAllPostsMultiplePagination() {
        // Arrange
        Pageable pageable1 = PageRequest.of(0, 5);
        Pageable pageable2 = PageRequest.of(1, 5);

        List<Post> page1Posts = Arrays.asList(
            new Post(1, 1, "Post 1", "Body", false, LocalDateTime.now()),
            new Post(2, 1, "Post 2", "Body", false, LocalDateTime.now())
        );
        Page<Post> page1 = new PageImpl<>(page1Posts, pageable1, 10);

        List<Post> page2Posts = Arrays.asList(
            new Post(3, 1, "Post 3", "Body", false, LocalDateTime.now()),
            new Post(4, 1, "Post 4", "Body", false, LocalDateTime.now())
        );
        Page<Post> page2 = new PageImpl<>(page2Posts, pageable2, 10);

        when(postRepository.findAll(pageable1)).thenReturn(page1);
        when(postRepository.findAll(pageable2)).thenReturn(page2);

        // Act
        Page<Post> result1 = postService.findAll(pageable1);
        Page<Post> result2 = postService.findAll(pageable2);

        // Assert
        assertEquals(2, result1.getContent().size());
        assertEquals(2, result2.getContent().size());
        verify(postRepository, times(1)).findAll(pageable1);
        verify(postRepository, times(1)).findAll(pageable2);
    }
}
