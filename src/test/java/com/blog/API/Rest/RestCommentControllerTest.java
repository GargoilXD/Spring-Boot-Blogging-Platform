package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Comment.ResponseCommentDTO;
import com.blog.Service.CommentService;
import com.blog.Model.Comment;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
class RestCommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private RestCommentController commentController;

    private CreateCommentDTO createDTO;
    private Comment mockComment;

    @BeforeEach
    void setUp() {
        createDTO = new CreateCommentDTO(1, 1, "Content");
        mockComment = new Comment();
    }

    @Test
    void getCommentsForPost_Success() {
        int postId = 1;
        when(commentService.findByPostId(postId)).thenReturn(List.of(mockComment));

        ResponseEntity<SuccessResponse<List<ResponseCommentDTO>>> response =
                commentController.getCommentsForPost(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(commentService).findByPostId(postId);
    }

    @Test
    void createComment_Success() {
        when(commentService.save(createDTO)).thenReturn(mockComment);

        ResponseEntity<SuccessResponse<ResponseCommentDTO>> response = commentController.createComment(createDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(commentService).save(createDTO);
    }

    @Test
    void updateComment_Success() {
        Integer pathVariableId = 1;
        UpdateCommentDTO dtoWithoutId = new UpdateCommentDTO(null, 1, 1, "Updated Content");
        UpdateCommentDTO dtoWithId = new UpdateCommentDTO(pathVariableId, 1, 1, "Updated Content");

        when(commentService.update(dtoWithId)).thenReturn(mockComment);

        ResponseEntity<SuccessResponse<ResponseCommentDTO>> response = commentController.updateComment(pathVariableId, dtoWithoutId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify the service was called with the DTO that has the ID from path variable
        verify(commentService).update(dtoWithId);
    }

    @Test
    void updateComment_Failure_PropagatesException() {
        Integer pathVariableId = 1;
        UpdateCommentDTO dtoWithoutId = new UpdateCommentDTO(null, 1, 1, "Content");
        UpdateCommentDTO dtoWithId = new UpdateCommentDTO(pathVariableId, 1, 1, "Content");

        doThrow(new EntityNotFoundException("Comment not found"))
                .when(commentService).update(dtoWithId);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentController.updateComment(pathVariableId, dtoWithoutId)
        );

        assertEquals("Comment not found", exception.getMessage());
        verify(commentService).update(dtoWithId);
    }

    @Test
    void deleteComment_Success() {
        doNothing().when(commentService).delete(1);

        ResponseEntity<SuccessResponse<Void>> response =
                commentController.deleteComment(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(commentService).delete(1);
    }

    @Test
    void deleteComment_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Comment not found"))
                .when(commentService).delete(1);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentController.deleteComment(1)
        );

        assertEquals("Comment not found", exception.getMessage());
        verify(commentService).delete(1);
    }
}