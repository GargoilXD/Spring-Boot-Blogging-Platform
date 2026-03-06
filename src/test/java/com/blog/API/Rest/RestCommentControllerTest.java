package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Comment.ResponseCommentDTO;
import com.blog.Model.Post;
import com.blog.Model.User;
import com.blog.Service.CommentService;
import com.blog.Model.Comment;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestCommentController Unit Tests")
class RestCommentControllerTest {
    @Mock private CommentService commentService;
    @Mock private Comment mockComment;
    @Mock private User    mockUser;
    @Mock private Post    mockPost;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private RestCommentController commentController;

    private CreateCommentDTO createDTO;
    private UpdateCommentDTO updateDTO;

    @BeforeEach
    void setUp() {
        createDTO = new CreateCommentDTO(1, "Content");
        updateDTO = new UpdateCommentDTO(1, "Updated Content");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/comments/post/{postId}")
    class GetComments {
        @Test
        @DisplayName("Returns 200 with comments for the given post")
        void getCommentsForPost_Success() {
            when(mockComment.getId()).thenReturn(1);
            when(mockComment.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockComment.getPost()).thenReturn(mockPost);
            when(mockPost.getId()).thenReturn(1);
            when(mockComment.getBody()).thenReturn("Content");
            when(commentService.findByPostId(1)).thenReturn(List.of(mockComment));
            ResponseEntity<SuccessResponse<List<ResponseCommentDTO>>> response = commentController.getCommentsForPost(1);
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
    @Nested
    @DisplayName("POST /api/comments")
    class CreateComment {
        @Test
        @DisplayName("Returns 201 when comment is created")
        void createComment_Success() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(mockComment.getId()).thenReturn(1);
            when(mockComment.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockComment.getPost()).thenReturn(mockPost);
            when(mockPost.getId()).thenReturn(1);
            when(mockComment.getBody()).thenReturn("Content");
            
            when(commentService.save(createDTO, "testuser")).thenReturn(CompletableFuture.completedFuture(mockComment));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponseCommentDTO>>> responseFuture = commentController.createComment(createDTO);
            ResponseEntity<SuccessResponse<ResponseCommentDTO>> response = responseFuture.join();
            
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(commentService).save(createDTO, "testuser");
        }
        @Test
        @DisplayName("Propagates EntityNotFoundException when post not found")
        void createComment_Failure_PostNotFound() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(commentService.save(createDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new EntityNotFoundException("Post not found")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponseCommentDTO>>> responseFuture = commentController.createComment(createDTO);
            
            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof EntityNotFoundException);
            assertTrue(ex.getCause().getMessage().contains("Post not found"));
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException when authenticated user not found")
        void createComment_Failure_UserNotFound() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(commentService.save(createDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new EntityNotFoundException("Authenticated user not found")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponseCommentDTO>>> responseFuture = commentController.createComment(createDTO);
            
            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof EntityNotFoundException);
        }
    }
    @Nested
    @DisplayName("PUT /api/comments/{id}")
    class UpdateComment {
        @Test
        @DisplayName("Returns 200 when comment is updated")
        void updateComment_Success() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(mockComment.getId()).thenReturn(1);
            when(mockComment.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockComment.getPost()).thenReturn(mockPost);
            when(mockPost.getId()).thenReturn(1);
            when(mockComment.getBody()).thenReturn("Content");
            
            when(commentService.update(1, updateDTO, "testuser")).thenReturn(CompletableFuture.completedFuture(mockComment));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponseCommentDTO>>> responseFuture = commentController.updateComment(1, updateDTO);
            ResponseEntity<SuccessResponse<ResponseCommentDTO>> response = responseFuture.join();
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(commentService).update(1, updateDTO, "testuser");
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void updateComment_Failure_CommentNotFound() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(commentService.update(99, updateDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new EntityNotFoundException("Comment not found")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponseCommentDTO>>> responseFuture = commentController.updateComment(99, updateDTO);
            
            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof EntityNotFoundException);
            assertEquals("Comment not found", ex.getCause().getMessage());
        }

        @Test
        @DisplayName("Propagates SecurityException when user is not the owner")
        void updateComment_Failure_NotOwner() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(commentService.update(1, updateDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new SecurityException("User does not own this comment")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponseCommentDTO>>> responseFuture = commentController.updateComment(1, updateDTO);
            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof SecurityException);
        }
    }
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
            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> commentController.deleteComment(99));
            assertEquals("Comment not found", ex.getMessage());
        }
    }
}