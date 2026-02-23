package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Comment.ResponseCommentDTO;
import com.blog.Service.CommentService;
import com.blog.Model.Comment;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestCommentController Unit Tests")
class RestCommentControllerTest {

    @Mock private CommentService commentService;

    @InjectMocks
    private RestCommentController commentController;

    private CreateCommentDTO createDTO;
    private UpdateCommentDTO updateDTO;
    private Comment mockComment;

    @BeforeEach
    void setUp() {
        createDTO   = new CreateCommentDTO(1, "Content");
        updateDTO   = new UpdateCommentDTO(1, "Updated Content");
        mockComment = new Comment();
    }

    // ─── GET /api/comments/post/{postId} ─────────────────────────────────────

    @Nested
    @DisplayName("GET /api/comments/post/{postId}")
    class GetComments {

        @Test
        @DisplayName("Returns 200 with comments for the given post")
        void getCommentsForPost_Success() {
            when(commentService.findByPostId(1)).thenReturn(List.of(mockComment));

            ResponseEntity<SuccessResponse<List<ResponseCommentDTO>>> response =
                commentController.getCommentsForPost(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(commentService).findByPostId(1);
        }

        @Test
        @DisplayName("Returns 200 with empty list when no comments exist")
        void getCommentsForPost_EmptyList() {
            when(commentService.findByPostId(99)).thenReturn(List.of());

            ResponseEntity<SuccessResponse<List<ResponseCommentDTO>>> response =
                commentController.getCommentsForPost(99);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    // ─── POST /api/comments ──────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/comments")
    class CreateComment {

        @Test
        @DisplayName("Returns 201 when comment is created")
        void createComment_Success() {
            when(commentService.save(createDTO)).thenReturn(mockComment);

            ResponseEntity<SuccessResponse<ResponseCommentDTO>> response =
                commentController.createComment(createDTO);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(commentService).save(createDTO);
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException when post not found")
        void createComment_Failure_PostNotFound() {
            when(commentService.save(createDTO)).thenThrow(new EntityNotFoundException("Post not found"));

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentController.createComment(createDTO)
            );

            assertTrue(ex.getMessage().contains("Post not found"));
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException when user not found")
        void createComment_Failure_UserNotFound() {
            when(commentService.save(createDTO)).thenThrow(new EntityNotFoundException("Authenticated user not found"));

            assertThrows(EntityNotFoundException.class, () -> commentController.createComment(createDTO));
        }
    }

    // ─── PUT /api/comments/{id} ──────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/comments/{id}")
    class UpdateComment {

        @Test
        @DisplayName("Returns 200 when comment is updated")
        void updateComment_Success() {
            when(commentService.update(1, updateDTO)).thenReturn(mockComment);

            ResponseEntity<SuccessResponse<ResponseCommentDTO>> response =
                commentController.updateComment(1, updateDTO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(commentService).update(1, updateDTO);
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void updateComment_Failure_CommentNotFound() {
            doThrow(new EntityNotFoundException("Comment not found")).when(commentService).update(99, updateDTO);

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentController.updateComment(99, updateDTO)
            );

            assertEquals("Comment not found", ex.getMessage());
        }

        @Test
        @DisplayName("Propagates SecurityException when user is not the owner")
        void updateComment_Failure_NotOwner() {
            doThrow(new SecurityException("User does not own this comment")).when(commentService).update(1, updateDTO);

            assertThrows(SecurityException.class, () -> commentController.updateComment(1, updateDTO));
        }
    }

    // ─── DELETE /api/comments/{id} ───────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/comments/{id}")
    class DeleteComment {

        @Test
        @DisplayName("Returns 200 when comment is deleted")
        void deleteComment_Success() {
            doNothing().when(commentService).delete(1);

            ResponseEntity<SuccessResponse<Void>> response = commentController.deleteComment(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(commentService).delete(1);
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void deleteComment_Failure_NotFound() {
            doThrow(new EntityNotFoundException("Comment not found")).when(commentService).delete(99);

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> commentController.deleteComment(99)
            );

            assertEquals("Comment not found", ex.getMessage());
        }
    }
}