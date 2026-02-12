package com.blog.Service;

import com.blog.Exception.DataAccessException;
import com.blog.Repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository dataAccessor;

    @InjectMocks
    private TagService tagService;

    private List<String> testTags;
    private HashMap<Long, List<String>> testTagsByPosts;

    @BeforeEach
    void setUp() {
        testTags = Arrays.asList("java", "spring-boot", "technology", "programming");

        testTagsByPosts = new HashMap<>();
        testTagsByPosts.put(1L, Arrays.asList("java", "spring-boot"));
        testTagsByPosts.put(2L, Arrays.asList("technology", "programming"));
    }

    @Test
    void testGetAllTags_Success() throws DataAccessException {
        // Arrange
        when(dataAccessor.getAllTags()).thenReturn(testTags);

        // Act
        List<String> result = tagService.getAllTags();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("java"));
        assertTrue(result.contains("spring-boot"));
        verify(dataAccessor, times(1)).getAllTags();
    }

    @Test
    void testGetAllTags_EmptyList() throws DataAccessException {
        // Arrange
        when(dataAccessor.getAllTags()).thenReturn(List.of());

        // Act
        List<String> result = tagService.getAllTags();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataAccessor, times(1)).getAllTags();
    }

    @Test
    void testGetAllTags_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        when(dataAccessor.getAllTags()).thenThrow(new DataAccessException("Database error"));

        // Act & Assert
        assertThrows(DataAccessException.class, () -> tagService.getAllTags());
        verify(dataAccessor, times(1)).getAllTags();
    }

    @Test
    void testGetAllTagsByPosts_Success() throws DataAccessException {
        // Arrange
        when(dataAccessor.getAllTagsByPosts()).thenReturn(testTagsByPosts);

        // Act
        HashMap<Long, List<String>> result = tagService.getAllTagsByPosts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
        assertEquals(2, result.get(1L).size());
        verify(dataAccessor, times(1)).getAllTagsByPosts();
    }

    @Test
    void testGetAllTagsByPosts_EmptyMap() throws DataAccessException {
        // Arrange
        when(dataAccessor.getAllTagsByPosts()).thenReturn(new HashMap<>());

        // Act
        HashMap<Long, List<String>> result = tagService.getAllTagsByPosts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataAccessor, times(1)).getAllTagsByPosts();
    }

    @Test
    void testGetForPost_Success() throws DataAccessException {
        // Arrange
        List<String> postTags = Arrays.asList("java", "spring-boot");
        when(dataAccessor.getTagsForPost(1L)).thenReturn(postTags);

        // Act
        List<String> result = tagService.getForPost(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("java"));
        assertTrue(result.contains("spring-boot"));
        verify(dataAccessor, times(1)).getTagsForPost(1L);
    }

    @Test
    void testGetForPost_EmptyList() throws DataAccessException {
        // Arrange
        when(dataAccessor.getTagsForPost(999L)).thenReturn(List.of());

        // Act
        List<String> result = tagService.getForPost(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataAccessor, times(1)).getTagsForPost(999L);
    }

    @Test
    void testGetTagsForPost_Success() throws DataAccessException {
        // Arrange
        List<String> postTags = Arrays.asList("java", "spring-boot", "tutorial");
        when(dataAccessor.getTagsForPost(1L)).thenReturn(postTags);

        // Act
        List<String> result = tagService.getTagsForPost(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("tutorial"));
        verify(dataAccessor, times(1)).getTagsForPost(1L);
    }

    @Test
    void testGetTagsForPost_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        when(dataAccessor.getTagsForPost(anyLong()))
                .thenThrow(new DataAccessException("Failed to retrieve tags"));

        // Act & Assert
        assertThrows(DataAccessException.class, () -> tagService.getTagsForPost(1L));
        verify(dataAccessor, times(1)).getTagsForPost(1L);
    }

    @Test
    void testSetPostTags_Success() throws DataAccessException {
        // Arrange
        List<String> newTags = Arrays.asList("java", "microservices", "cloud");
        doNothing().when(dataAccessor).setPostTags(1L, newTags);

        // Act
        tagService.setPostTags(1L, newTags);

        // Assert
        verify(dataAccessor, times(1)).setPostTags(1L, newTags);
    }

    @Test
    void testSetPostTags_EmptyList() throws DataAccessException {
        // Arrange
        List<String> emptyTags = List.of();
        doNothing().when(dataAccessor).setPostTags(1L, emptyTags);

        // Act
        tagService.setPostTags(1L, emptyTags);

        // Assert
        verify(dataAccessor, times(1)).setPostTags(1L, emptyTags);
    }

    @Test
    void testSetPostTags_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        List<String> tags = Arrays.asList("java", "spring-boot");
        doThrow(new DataAccessException("Failed to set tags"))
                .when(dataAccessor).setPostTags(anyLong(), anyList());

        // Act & Assert
        assertThrows(DataAccessException.class, () -> tagService.setPostTags(1L, tags));
        verify(dataAccessor, times(1)).setPostTags(1L, tags);
    }
}
