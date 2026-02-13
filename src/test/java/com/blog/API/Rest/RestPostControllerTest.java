package com.blog.API.Rest;

import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;
import com.blog.Service.PostService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestPostController.class)
@DisplayName("REST Post Controller Tests")
class RestPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

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
    @DisplayName("GET /api/posts/{id} - Should retrieve post by ID successfully")
    void testFindPostById() throws Exception {
        // Arrange
        Post post = new Post(1, 1, "Test Post", "Test Body", false, LocalDateTime.now());
        when(postService.findById(1)).thenReturn(Optional.of(post));

        // Act & Assert
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Post"))
                .andExpect(jsonPath("$.message").value("Post found and returned successfully"));

        verify(postService, times(1)).findById(1);
    }

    @Test
    @DisplayName("GET /api/posts/{id} - Should return 404 when post not found")
    void testFindPostNotFound() throws Exception {
        // Arrange
        when(postService.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).findById(999);
    }

    @Test
    @DisplayName("GET /api/posts - Should retrieve all posts with pagination")
    void testFindAllPosts() throws Exception {
        // Arrange
        Post post1 = new Post(1, 1, "Post 1", "Body 1", false, LocalDateTime.now());
        Post post2 = new Post(2, 1, "Post 2", "Body 2", false, LocalDateTime.now());
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1, post2), PageRequest.of(0, 10), 2);

        when(postService.findAll(any())).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(postService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("GET /api/posts - Should retrieve all posts with sorting")
    void testFindAllPostsWithSorting() throws Exception {
        // Arrange
        Post post1 = new Post(1, 1, "Post 1", "Body 1", false, LocalDateTime.now());
        Post post2 = new Post(2, 1, "Post 2", "Body 2", false, LocalDateTime.now());
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1, post2), PageRequest.of(0, 10), 2);

        when(postService.findAll(any())).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts?page=0&size=10&sort=title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(postService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("POST /api/posts - Should create post successfully")
    void testCreatePost() throws Exception {
        // Arrange
        CreatePostDTO postDTO = new CreatePostDTO(1, "New Post", "New Body", false);
        Post savedPost = new Post(1, 1, "New Post", "New Body", false, LocalDateTime.now());

        when(postService.save(any(CreatePostDTO.class))).thenReturn(savedPost);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.title").value("New Post"))
                .andExpect(jsonPath("$.message").value("Post created successfully"));

        verify(postService, times(1)).save(any(CreatePostDTO.class));
    }

    @Test
    @DisplayName("POST /api/posts - Should create draft post successfully")
    void testCreateDraftPost() throws Exception {
        // Arrange
        CreatePostDTO postDTO = new CreatePostDTO(1, "Draft Post", "Draft Body", true);
        Post savedPost = new Post(1, 1, "Draft Post", "Draft Body", true, LocalDateTime.now());

        when(postService.save(any(CreatePostDTO.class))).thenReturn(savedPost);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.draft").value(true))
                .andExpect(jsonPath("$.message").value("Post created successfully"));

        verify(postService, times(1)).save(any(CreatePostDTO.class));
    }

    @Test
    @DisplayName("PUT /api/posts - Should update post successfully")
    void testUpdatePost() throws Exception {
        // Arrange
        UpdatePostDTO updateDTO = new UpdatePostDTO(1, 1, "Updated Post", "Updated Body", false);
        Post updatedPost = new Post(1, 1, "Updated Post", "Updated Body", false, LocalDateTime.now());

        when(postService.update(any(UpdatePostDTO.class))).thenReturn(updatedPost);

        // Act & Assert
        mockMvc.perform(put("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Post"))
                .andExpect(jsonPath("$.message").value("Post updated successfully"));

        verify(postService, times(1)).update(any(UpdatePostDTO.class));
    }

    @Test
    @DisplayName("PUT /api/posts - Should update post to draft")
    void testUpdatePostToDraft() throws Exception {
        // Arrange
        UpdatePostDTO updateDTO = new UpdatePostDTO(1, 1, "Updated Post", "Updated Body", true);
        Post updatedPost = new Post(1, 1, "Updated Post", "Updated Body", true, LocalDateTime.now());

        when(postService.update(any(UpdatePostDTO.class))).thenReturn(updatedPost);

        // Act & Assert
        mockMvc.perform(put("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.draft").value(true))
                .andExpect(jsonPath("$.message").value("Post updated successfully"));

        verify(postService, times(1)).update(any(UpdatePostDTO.class));
    }

    @Test
    @DisplayName("PUT /api/posts - Should return 404 when updating non-existent post")
    void testUpdatePostNotFound() throws Exception {
        // Arrange
        UpdatePostDTO updateDTO = new UpdatePostDTO(999, 1, "Updated Post", "Updated Body", false);
        doThrow(new EntityNotFoundException("Post not found"))
                .when(postService).update(any(UpdatePostDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().is4xxClientError());

        verify(postService, times(1)).update(any(UpdatePostDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - Should delete post successfully")
    void testDeletePost() throws Exception {
        // Arrange
        doNothing().when(postService).delete(1);

        // Act & Assert
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted successfully"));

        verify(postService, times(1)).delete(1);
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - Should return 404 when deleting non-existent post")
    void testDeletePostNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Post not found")).when(postService).delete(999);

        // Act & Assert
        mockMvc.perform(delete("/api/posts/999")).andExpect(status().is4xxClientError());
        verify(postService, times(1)).delete(999);
    }

    @Test
    @DisplayName("GET /api/posts/{id} - Should return 400 for invalid post ID")
    void testFindPostWithInvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/0"))
                .andExpect(status().isBadRequest());
    }
}
