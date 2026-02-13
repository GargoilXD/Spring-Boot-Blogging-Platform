package com.blog.API.Rest;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.ResponseCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;
import com.blog.Service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestCommentController.class)
@DisplayName("REST Comment Controller Tests")
class RestCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

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
    @DisplayName("GET /api/comments/post/{postId} - Should retrieve comments for a post successfully")
    void testGetCommentsForPost() throws Exception {
        // Arrange
        List<Comment> comments = Arrays.asList(
            new Comment(1, 1, 1, "Great post!", LocalDateTime.now()),
            new Comment(2, 2, 1, "Very informative", LocalDateTime.now())
        );
        when(commentService.findByPostId(1)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/api/comments/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].body").value("Great post!"))
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"));

        verify(commentService, times(1)).findByPostId(1);
    }

    @Test
    @DisplayName("GET /api/comments/post/{postId} - Should return empty list when no comments found")
    void testGetCommentsForPostEmpty() throws Exception {
        // Arrange
        when(commentService.findByPostId(999)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/comments/post/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"));

        verify(commentService, times(1)).findByPostId(999);
    }

    @Test
    @DisplayName("GET /api/comments/post/{postId} - Should return 400 for invalid post ID")
    void testGetCommentsForPostInvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/comments/post/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/comments - Should create comment successfully")
    void testCreateComment() throws Exception {
        // Arrange
        CreateCommentDTO createDTO = new CreateCommentDTO(1, 1, "Great post!");
        Comment savedComment = new Comment(1, 1, 1, "Great post!", LocalDateTime.now());

        when(commentService.save(any(CreateCommentDTO.class))).thenReturn(savedComment);

        // Act & Assert
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.body").value("Great post!"))
                .andExpect(jsonPath("$.message").value("Comment created successfully"));

        verify(commentService, times(1)).save(any(CreateCommentDTO.class));
    }

    @Test
    @DisplayName("POST /api/comments - Should create comment with long body")
    void testCreateCommentWithLongBody() throws Exception {
        // Arrange
        String longBody = "This is a very long comment that contains multiple sentences and provides detailed feedback about the post. It demonstrates that the system can handle longer comment bodies without any issues.";
        CreateCommentDTO createDTO = new CreateCommentDTO(1, 1, longBody);
        Comment savedComment = new Comment(1, 1, 1, longBody, LocalDateTime.now());

        when(commentService.save(any(CreateCommentDTO.class))).thenReturn(savedComment);

        // Act & Assert
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.body").value(longBody))
                .andExpect(jsonPath("$.message").value("Comment created successfully"));

        verify(commentService, times(1)).save(any(CreateCommentDTO.class));
    }

    @Test
    @DisplayName("PUT /api/comments - Should update comment successfully")
    void testUpdateComment() throws Exception {
        // Arrange
        UpdateCommentDTO updateDTO = new UpdateCommentDTO(1, 1, 1, "Updated comment");
        Comment updatedComment = new Comment(1, 1, 1, "Updated comment", LocalDateTime.now());

        when(commentService.update(any(UpdateCommentDTO.class))).thenReturn(updatedComment);

        // Act & Assert
        mockMvc.perform(put("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.body").value("Updated comment"))
                .andExpect(jsonPath("$.message").value("Comment updated successfully"));

        verify(commentService, times(1)).update(any(UpdateCommentDTO.class));
    }

    @Test
    @DisplayName("PUT /api/comments - Should return 404 when updating non-existent comment")
    void testUpdateCommentNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Comment not found"))
                .when(commentService).update(any(UpdateCommentDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCommentDTO(999, 1, 1, "Updated"))))
                .andExpect(status().is4xxClientError());

        verify(commentService, times(1)).update(any(UpdateCommentDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/comments/{id} - Should delete comment successfully")
    void testDeleteComment() throws Exception {
        // Arrange
        doNothing().when(commentService).delete(1);

        // Act & Assert
        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));

        verify(commentService, times(1)).delete(1);
    }

    @Test
    @DisplayName("DELETE /api/comments/{id} - Should return 404 when deleting non-existent comment")
    void testDeleteCommentNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Comment not found"))
                .when(commentService).delete(999);

        // Act & Assert
        mockMvc.perform(delete("/api/comments/999"))
                .andExpect(status().is4xxClientError());

        verify(commentService, times(1)).delete(999);
    }

    @Test
    @DisplayName("POST /api/comments - Should return 404 when post or user not found")
    void testCreateCommentPostOrUserNotFound() throws Exception {
        // Arrange
        CreateCommentDTO createDTO = new CreateCommentDTO(999, 999, "Test comment");
        doThrow(new EntityNotFoundException("Post or user not found"))
                .when(commentService).save(any(CreateCommentDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().is4xxClientError());

        verify(commentService, times(1)).save(any(CreateCommentDTO.class));
    }

    @Test
    @DisplayName("GET /api/comments/post/{postId} - Should retrieve multiple comments for popular post")
    void testGetMultipleCommentsForPost() throws Exception {
        // Arrange
        List<Comment> comments = Arrays.asList(
            new Comment(1, 1, 1, "First comment", LocalDateTime.now()),
            new Comment(2, 2, 1, "Second comment", LocalDateTime.now()),
            new Comment(3, 3, 1, "Third comment", LocalDateTime.now()),
            new Comment(4, 4, 1, "Fourth comment", LocalDateTime.now()),
            new Comment(5, 5, 1, "Fifth comment", LocalDateTime.now())
        );
        when(commentService.findByPostId(1)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/api/comments/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].body").value("First comment"))
                .andExpect(jsonPath("$.data[4].body").value("Fifth comment"))
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"));

        verify(commentService, times(1)).findByPostId(1);
    }
}
