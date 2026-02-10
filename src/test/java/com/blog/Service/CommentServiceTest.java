package com.blog.Service;

import com.blog.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.CommentDataAccessor;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentDataAccessor dataAccessor;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private CreateCommentDTO createCommentDTO;
    private UpdateCommentDTO updateCommentDTO;

    @BeforeEach
    void setUp() {
        testComment = new Comment(
                "507f1f77bcf86cd799439011",
                1L,
                1L,
                "johndoe",
                "This is a test comment",
                Instant.now()
        );

        createCommentDTO = new CreateCommentDTO(
                1L,
                1L,
                "johndoe",
                "New comment body"
        );

        updateCommentDTO = new UpdateCommentDTO(
                "507f1f77bcf86cd799439011",
                1L,
                "johndoe",
                1L,
                "Updated comment body"
        );
    }

    @Test
    void testGetForPost_Success() throws DataAccessException {
        // Arrange
        List<Comment> comments = Arrays.asList(testComment, testComment);
        when(dataAccessor.getForPost(1L)).thenReturn(comments);

        // Act
        List<Comment> result = commentService.getForPost(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testComment.getBody(), result.get(0).getBody());
        verify(dataAccessor, times(1)).getForPost(1L);
    }

    @Test
    void testGetForPost_EmptyList() throws DataAccessException {
        // Arrange
        when(dataAccessor.getForPost(999L)).thenReturn(List.of());

        // Act
        List<Comment> result = commentService.getForPost(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataAccessor, times(1)).getForPost(999L);
    }

    @Test
    void testGetForPost_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        when(dataAccessor.getForPost(anyLong())).thenThrow(new DataAccessException("Database error"));

        // Act & Assert
        assertThrows(DataAccessException.class, () -> commentService.getForPost(1L));
        verify(dataAccessor, times(1)).getForPost(1L);
    }

    @Test
    void testSave_Success() throws DataAccessException {
        // Arrange
        when(dataAccessor.save(any(CreateCommentDTO.class))).thenReturn(testComment);

        // Act
        Comment result = commentService.save(createCommentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getBody(), result.getBody());
        assertEquals(testComment.getUsername(), result.getUsername());
        verify(dataAccessor, times(1)).save(createCommentDTO);
    }

    @Test
    void testSave_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        when(dataAccessor.save(any(CreateCommentDTO.class)))
                .thenThrow(new DataAccessException("Failed to save comment"));

        // Act & Assert
        assertThrows(DataAccessException.class, () -> commentService.save(createCommentDTO));
        verify(dataAccessor, times(1)).save(createCommentDTO);
    }

    @Test
    void testUpdate_Success() throws DataAccessException {
        // Arrange
        Comment updatedComment = new Comment(
                testComment.getId(),
                testComment.getUserId(),
                testComment.getPostId(),
                testComment.getUsername(),
                "Updated comment body",
                testComment.getCreatedAt()
        );
        when(dataAccessor.update(any(UpdateCommentDTO.class))).thenReturn(updatedComment);

        // Act
        Comment result = commentService.update(updateCommentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updatedComment.getId(), result.getId());
        assertEquals("Updated comment body", result.getBody());
        verify(dataAccessor, times(1)).update(updateCommentDTO);
    }

    @Test
    void testUpdate_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        when(dataAccessor.update(any(UpdateCommentDTO.class)))
                .thenThrow(new DataAccessException("Failed to update comment"));

        // Act & Assert
        assertThrows(DataAccessException.class, () -> commentService.update(updateCommentDTO));
        verify(dataAccessor, times(1)).update(updateCommentDTO);
    }

    @Test
    void testDelete_Success() throws DataAccessException {
        // Arrange
        doNothing().when(dataAccessor).delete("507f1f77bcf86cd799439011");

        // Act
        commentService.delete("507f1f77bcf86cd799439011");

        // Assert
        verify(dataAccessor, times(1)).delete("507f1f77bcf86cd799439011");
    }

    @Test
    void testDelete_ThrowsDataAccessException() throws DataAccessException {
        // Arrange
        doThrow(new DataAccessException("Failed to delete comment"))
                .when(dataAccessor).delete(anyString());

        // Act & Assert
        assertThrows(DataAccessException.class, () -> commentService.delete("507f1f77bcf86cd799439011"));
        verify(dataAccessor, times(1)).delete("507f1f77bcf86cd799439011");
    }
}
