package com.blog.API.Rest;

import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.Model.PostTags;
import com.blog.Service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestTagController.class)
@DisplayName("REST Tag Controller Tests")
class RestTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("GET /api/tags - Should retrieve all tags with pagination")
    void testFindAllTags() throws Exception {
        // Arrange
        PostTags tag1 = new PostTags("1", 1, Arrays.asList("Java", "Spring"));
        PostTags tag2 = new PostTags("2", 2, Arrays.asList("Python", "Django"));
        Page<PostTags> tagsPage = new PageImpl<>(Arrays.asList(tag1, tag2), PageRequest.of(0, 10), 2);

        when(tagService.findAll(any())).thenReturn(tagsPage);

        // Act & Assert
        mockMvc.perform(get("/api/tags?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(tagService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("GET /api/tags - Should retrieve all tags with different page size")
    void testFindAllTagsWithDifferentPageSize() throws Exception {
        // Arrange
        PostTags tag1 = new PostTags("1", 1, Arrays.asList("Java"));
        Page<PostTags> tagsPage = new PageImpl<>(Arrays.asList(tag1), PageRequest.of(0, 5), 1);

        when(tagService.findAll(any())).thenReturn(tagsPage);

        // Act & Assert
        mockMvc.perform(get("/api/tags?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(tagService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("GET /api/tags/{postId} - Should retrieve tags by post ID")
    void testFindTagsByPostId() throws Exception {
        // Arrange
        List<String> tags = Arrays.asList("Java", "Spring", "Boot");
        when(tagService.findByPostId(1)).thenReturn(tags);

        // Act & Assert
        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("Java"))
                .andExpect(jsonPath("$.message").value("Tags retrieved successfully"));

        verify(tagService, times(1)).findByPostId(1);
    }

    @Test
    @DisplayName("GET /api/tags/{postId} - Should return empty list when no tags found for post")
    void testFindTagsByPostIdEmpty() throws Exception {
        // Arrange
        when(tagService.findByPostId(999)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/tags/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.message").value("Tags retrieved successfully"));

        verify(tagService, times(1)).findByPostId(999);
    }

    @Test
    @DisplayName("GET /api/tags/{postId} - Should return 400 for invalid post ID")
    void testFindTagsByPostIdInvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tags/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tags - Should set tags for post successfully")
    void testSetPostTags() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(1, Arrays.asList("Java", "Spring"));
        doNothing().when(tagService).setPostTags(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tags set successfully"));

        verify(tagService, times(1)).setPostTags(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("POST /api/tags - Should set multiple tags for post")
    void testSetMultiplePostTags() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(1, Arrays.asList("Java", "Spring", "Boot", "Microservices", "REST"));
        doNothing().when(tagService).setPostTags(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tags set successfully"));

        verify(tagService, times(1)).setPostTags(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tags/add - Should add tags to post successfully")
    void testAddTagsToPost() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(1, Arrays.asList("Boot", "Microservices"));
        doNothing().when(tagService).addTagsToPost(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tags added successfully"));

        verify(tagService, times(1)).addTagsToPost(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tags/add - Should return 404 when adding tags to non-existent post")
    void testAddTagsToPostNotFound() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(999, Arrays.asList("Java"));
        doThrow(new EntityNotFoundException("Post not found"))
                .when(tagService).addTagsToPost(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().is4xxClientError());

        verify(tagService, times(1)).addTagsToPost(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tags/remove - Should remove tags from post successfully")
    void testRemoveTagsFromPost() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(1, Arrays.asList("Java"));
        doNothing().when(tagService).removeTagsFromPost(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tags removed successfully"));

        verify(tagService, times(1)).removeTagsFromPost(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tags/remove - Should remove multiple tags from post")
    void testRemoveMultipleTagsFromPost() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(1, Arrays.asList("Java", "Spring", "Boot"));
        doNothing().when(tagService).removeTagsFromPost(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tags removed successfully"));

        verify(tagService, times(1)).removeTagsFromPost(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tags/remove - Should return 404 when removing tags from non-existent post")
    void testRemoveTagsFromPostNotFound() throws Exception {
        // Arrange
        PostTagsDTO tagsDTO = new PostTagsDTO(999, Arrays.asList("Java"));
        doThrow(new EntityNotFoundException("Post not found"))
                .when(tagService).removeTagsFromPost(any(PostTagsDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsDTO)))
                .andExpect(status().is4xxClientError());

        verify(tagService, times(1)).removeTagsFromPost(any(PostTagsDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/tags/{postId} - Should delete tags for post successfully")
    void testDeleteByPostId() throws Exception {
        // Arrange
        doNothing().when(tagService).deleteByPostId(1);

        // Act & Assert
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tags deleted successfully"));

        verify(tagService, times(1)).deleteByPostId(1);
    }

    @Test
    @DisplayName("DELETE /api/tags/{postId} - Should return 404 when deleting tags for non-existent post")
    void testDeleteByPostIdNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Post not found"))
                .when(tagService).deleteByPostId(999);

        // Act & Assert
        mockMvc.perform(delete("/api/tags/999"))
                .andExpect(status().is4xxClientError());

        verify(tagService, times(1)).deleteByPostId(999);
    }

    @Test
    @DisplayName("DELETE /api/tags/{postId} - Should return 400 for invalid post ID")
    void testDeleteByPostIdInvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tags/0"))
                .andExpect(status().isBadRequest());
    }
}
