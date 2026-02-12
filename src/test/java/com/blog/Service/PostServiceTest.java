package com.blog.Service;

import com.blog.Repository.PostRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository dataAccessor;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private CreatePostDTO createPostDTO;
    private UpdatePostDTO updatePostDTO;
    private GetPostDTO getPostDTO;

    @BeforeEach
    void setUp() {
        testPost = new Post(
                1,
                1,
                "Test Post Title",
                "Test post body content",
                false
        );

        createPostDTO = new CreatePostDTO(
                1L,
                "New Post Title",
                "New post body content",
                false
        );

        updatePostDTO = new UpdatePostDTO(
                1L,
                "Updated Post Title",
                "Updated post body content",
                false
        );

        getPostDTO = new GetPostDTO(0, 10, "created_at", "DESC");
    }

    @Test
    void testGetPost_Success() {
        // Arrange
        when(dataAccessor.findByID(1L)).thenReturn(Optional.of(testPost));

        // Act
        Optional<Post> result = postService.getPost(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPost.getId(), result.get().getId());
        assertEquals(testPost.getTitle(), result.get().getTitle());
        verify(dataAccessor, times(1)).findByID(1L);
    }

    @Test
    void testGetPost_NotFound() {
        // Arrange
        when(dataAccessor.findByID(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Post> result = postService.getPost(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(dataAccessor, times(1)).findByID(999L);
    }

    @Test
    void testGetAllPosts_Success() {
        // Arrange
        List<Post> posts = Arrays.asList(testPost, testPost);
        when(dataAccessor.findAll(any(GetPostDTO.class))).thenReturn(posts);

        // Act
        List<Post> result = postService.getAllPosts(getPostDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dataAccessor, times(1)).findAll(getPostDTO);
    }

    @Test
    void testGetAllPosts_EmptyList() {
        // Arrange
        when(dataAccessor.findAll(any(GetPostDTO.class))).thenReturn(List.of());

        // Act
        List<Post> result = postService.getAllPosts(getPostDTO);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataAccessor, times(1)).findAll(getPostDTO);
    }

    @Test
    void testCountPosts_Success() {
        // Arrange
        when(dataAccessor.count()).thenReturn(42L);

        // Act
        long count = postService.countPosts();

        // Assert
        assertEquals(42L, count);
        verify(dataAccessor, times(1)).count();
    }

    @Test
    void testCountPosts_Zero() {
        // Arrange
        when(dataAccessor.count()).thenReturn(0L);

        // Act
        long count = postService.countPosts();

        // Assert
        assertEquals(0L, count);
        verify(dataAccessor, times(1)).count();
    }

    @Test
    void testSave_Success() {
        // Arrange
        when(dataAccessor.save(any(CreatePostDTO.class))).thenReturn(testPost);

        // Act
        Post result = postService.save(createPostDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getTitle(), result.getTitle());
        verify(dataAccessor, times(1)).save(createPostDTO);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(dataAccessor.findByID(1L)).thenReturn(Optional.of(testPost));
        when(dataAccessor.update(any(UpdatePostDTO.class))).thenReturn(testPost);

        // Act
        Post result = postService.update(updatePostDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        verify(dataAccessor, times(1)).findByID(1L);
        verify(dataAccessor, times(1)).update(updatePostDTO);
    }

    @Test
    void testUpdate_PostNotFound() {
        // Arrange
        when(dataAccessor.findByID(999L)).thenReturn(Optional.empty());
        UpdatePostDTO invalidDTO = new UpdatePostDTO(999L, "Title", "Body", false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> postService.update(invalidDTO));
        verify(dataAccessor, times(1)).findByID(999L);
        verify(dataAccessor, never()).update(any(UpdatePostDTO.class));
    }

    @Test
    void testDelete_Success() {
        // Arrange
        when(dataAccessor.findByID(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(dataAccessor).delete(1L);

        // Act
        postService.delete(1L);

        // Assert
        verify(dataAccessor, times(1)).findByID(1L);
        verify(dataAccessor, times(1)).delete(1L);
    }

    @Test
    void testDelete_PostNotFound() {
        // Arrange
        when(dataAccessor.findByID(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> postService.delete(999L));
        verify(dataAccessor, times(1)).findByID(999L);
        verify(dataAccessor, never()).delete(anyLong());
    }
}
