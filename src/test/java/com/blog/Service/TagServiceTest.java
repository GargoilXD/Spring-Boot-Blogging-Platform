package com.blog.Service;

import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.Model.Post;
import com.blog.Model.PostTags;
import com.blog.Repository.PostRepository;
import com.blog.Repository.TagRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Tests")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
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

    @Test
    @DisplayName("Should retrieve all tags with pagination")
    void testFindAllTagsWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PostTags> tags = Arrays.asList(
            new PostTags("1", 1, Arrays.asList("Java", "Spring")),
            new PostTags("2", 2, Arrays.asList("Python", "Django"))
        );
        Page<PostTags> page = new PageImpl<>(tags, pageable, 2);

        when(tagRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<PostTags> result = tagService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(tagRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no tags found")
    void testFindAllTagsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostTags> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(tagRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<PostTags> result = tagService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tagRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find tags by post ID")
    void testFindTagsByPostId() {
        // Arrange
        int postId = 1;
        PostTags postTags = new PostTags("1", 1, Arrays.asList("Java", "Spring", "Boot"));

        when(tagRepository.findByPostId(postId)).thenReturn(Optional.of(postTags));

        // Act
        List<String> result = tagService.findByPostId(postId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Java"));
        assertTrue(result.contains("Spring"));
        verify(tagRepository, times(1)).findByPostId(postId);
    }

    @Test
    @DisplayName("Should return empty list when post tags not found")
    void testFindTagsByPostIdNotFound() {
        // Arrange
        int postId = 999;

        when(tagRepository.findByPostId(postId)).thenReturn(Optional.empty());

        // Act
        List<String> result = tagService.findByPostId(postId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tagRepository, times(1)).findByPostId(postId);
    }

    @Test
    @DisplayName("Should successfully set tags for a post")
    void testSetPostTagsSuccess() {
        // Arrange
        PostTagsDTO dto = new PostTagsDTO(1, Arrays.asList("Java", "Spring", "Boot"));
        PostTags savedTags = new PostTags("1", 1, Arrays.asList("Java", "Spring", "Boot"));

        when(postRepository.findById(1)).thenReturn(Optional.of(new com.blog.Model.Post()));
        when(tagRepository.findByPostId(1)).thenReturn(Optional.empty());
        when(tagRepository.save(any(PostTags.class))).thenReturn(savedTags);

        // Act
        assertDoesNotThrow(() -> tagService.setPostTags(dto));

        // Assert
        verify(postRepository, times(1)).findById(1);
        verify(tagRepository, times(1)).save(any(PostTags.class));
    }

    @Test
    @DisplayName("Should successfully add tags to existing post")
    void testAddTagsToPostSuccess() {
        // Arrange
        int postId = 1;
        PostTags existingTags = new PostTags("1", 1, new ArrayList<>(Arrays.asList("Java", "Spring")));
        PostTagsDTO dto = new PostTagsDTO(postId, Arrays.asList("Boot", "Microservices"));

        when(postRepository.findById(postId)).thenReturn(Optional.of(new com.blog.Model.Post()));
        when(tagRepository.findByPostId(postId)).thenReturn(Optional.of(existingTags));
        when(tagRepository.save(any(PostTags.class))).thenReturn(existingTags);

        // Act
        assertDoesNotThrow(() -> tagService.addTagsToPost(dto));

        // Assert
        verify(postRepository, times(1)).findById(postId);
        verify(tagRepository, times(1)).findByPostId(postId);
        verify(tagRepository, times(1)).save(any(PostTags.class));
    }

    @Test
    @DisplayName("Should throw exception when adding tags to non-existent post")
    void testAddTagsToNonExistentPost() {
        // Arrange
        int postId = 999;
        PostTagsDTO dto = new PostTagsDTO(postId, Arrays.asList("Java", "Spring"));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tagService.addTagsToPost(dto));

        verify(postRepository, times(1)).findById(postId);
        verify(tagRepository, never()).save(any(PostTags.class));
    }

    @Test
    @DisplayName("Should successfully remove tags from post")
    void testRemoveTagsFromPostSuccess() {
        // Arrange
        int postId = 1;
        PostTags existingTags = new PostTags("1", 1, new ArrayList<>(Arrays.asList("Java", "Spring", "Boot")));
        PostTagsDTO dto = new PostTagsDTO(postId, Arrays.asList("Java"));

        when(postRepository.findById(postId)).thenReturn(Optional.of(new com.blog.Model.Post()));
        when(tagRepository.findByPostId(postId)).thenReturn(Optional.of(existingTags));
        when(tagRepository.save(any(PostTags.class))).thenReturn(existingTags);

        // Act
        assertDoesNotThrow(() -> tagService.removeTagsFromPost(dto));

        // Assert
        verify(postRepository, times(1)).findById(postId);
        verify(tagRepository, times(1)).findByPostId(postId);
        verify(tagRepository, times(1)).save(any(PostTags.class));
    }

    @Test
    @DisplayName("Should delete post tags when removing all tags")
    void testRemoveAllTagsDeletesPostTags() {
        // Arrange
        int postId = 1;
        PostTags existingTags = new PostTags("1", 1, new ArrayList<>(Arrays.asList("Java")));
        PostTagsDTO dto = new PostTagsDTO(postId, Arrays.asList("Java"));

        when(postRepository.findById(postId)).thenReturn(Optional.of(new com.blog.Model.Post()));
        when(tagRepository.findByPostId(postId)).thenReturn(Optional.of(existingTags));

        // Act
        assertDoesNotThrow(() -> tagService.removeTagsFromPost(dto));

        // Assert
        verify(postRepository, times(1)).findById(postId);
        verify(tagRepository, times(1)).findByPostId(postId);
        verify(tagRepository, times(1)).delete(existingTags);
    }

    @Test
    @DisplayName("Should throw exception when removing tags from non-existent post")
    void testRemoveTagsFromNonExistentPost() {
        // Arrange
        int postId = 999;
        PostTagsDTO dto = new PostTagsDTO(postId, Arrays.asList("Java"));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tagService.removeTagsFromPost(dto));

        verify(postRepository, times(1)).findById(postId);
        verify(tagRepository, never()).delete(any(PostTags.class));
    }

    @Test
    @DisplayName("Should successfully delete all tags for a post")
    void testDeleteByPostIdSuccess() {
        // Arrange
        int postId = 1;
        PostTags postTags = new PostTags("1", 1, Arrays.asList("Java", "Spring"));

        when(tagRepository.findByPostId(postId)).thenReturn(Optional.of(postTags));

        // Act
        assertDoesNotThrow(() -> tagService.deleteByPostId(postId));

        // Assert
        verify(tagRepository, times(1)).findByPostId(postId);
        verify(tagRepository, times(1)).deleteByPostId(postId);
    }

    @Test
    @DisplayName("Should throw exception when deleting tags for non-existent post")
    void testDeleteByPostIdNotFound() {
        // Arrange
        int postId = 999;

        when(tagRepository.findByPostId(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tagService.deleteByPostId(postId));

        verify(tagRepository, times(1)).findByPostId(postId);
        verify(tagRepository, never()).deleteByPostId(anyInt());
    }

    @Test
    @DisplayName("Should handle multiple tags for same post")
    void testMultipleTagsForPost() {
        // Arrange
        int postId = 5;
        PostTags postTags = new PostTags("5", 5, Arrays.asList("Java", "Spring", "Boot", "Microservices", "REST"));

        when(tagRepository.findByPostId(postId)).thenReturn(Optional.of(postTags));

        // Act
        List<String> result = tagService.findByPostId(postId);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(tagRepository, times(1)).findByPostId(postId);
    }

    @Test
    @DisplayName("Should find tags with different pagination sizes")
    void testFindAllTagsMultiplePagination() {
        // Arrange
        Pageable pageable1 = PageRequest.of(0, 5);
        Pageable pageable2 = PageRequest.of(1, 5);

        List<PostTags> page1Tags = Arrays.asList(
            new PostTags("1", 1, Arrays.asList("Java", "Spring"))
        );
        Page<PostTags> page1 = new PageImpl<>(page1Tags, pageable1, 10);

        List<PostTags> page2Tags = Arrays.asList(
            new PostTags("2", 2, Arrays.asList("Python", "Django"))
        );
        Page<PostTags> page2 = new PageImpl<>(page2Tags, pageable2, 10);

        when(tagRepository.findAll(pageable1)).thenReturn(page1);
        when(tagRepository.findAll(pageable2)).thenReturn(page2);

        // Act
        Page<PostTags> result1 = tagService.findAll(pageable1);
        Page<PostTags> result2 = tagService.findAll(pageable2);

        // Assert
        assertEquals(1, result1.getContent().size());
        assertEquals(1, result2.getContent().size());
        verify(tagRepository, times(1)).findAll(pageable1);
        verify(tagRepository, times(1)).findAll(pageable2);
    }
}
