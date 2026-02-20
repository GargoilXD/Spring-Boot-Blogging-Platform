package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.DataTransporter.Tags.ResponseTagsDTO;
import com.blog.Service.TagService;
import com.blog.Model.PostTags;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestTagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private RestTagController tagController;

    private PostTags mockPostTags;

    @BeforeEach
    void setUp() {
        mockPostTags = new PostTags();
    }

    @Test
    void findAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostTags> postTagsPage = new PageImpl<>(List.of(mockPostTags));
        when(tagService.findAll(pageable)).thenReturn(postTagsPage);

        ResponseEntity<SuccessResponse<Page<ResponseTagsDTO>>> response = tagController.findAll(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).findAll(pageable);
    }

    @Test
    void findByPostId_Success() {
        when(tagService.findByPostId(1)).thenReturn(List.of("tag1", "tag2"));

        ResponseEntity<SuccessResponse<List<String>>> response = tagController.findByPostId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).findByPostId(1);
    }

    @Test
    void setPostTags_Success() {
        PostTagsDTO validDTO = new PostTagsDTO(1, List.of("tag1"));
        doNothing().when(tagService).setPostTags(validDTO);

        ResponseEntity<SuccessResponse<Void>> response = tagController.setPostTags(validDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).setPostTags(validDTO);
    }

    @Test
    void setPostTags_Failure_InvalidPostId_Null() {
        PostTagsDTO invalidDTO = new PostTagsDTO(null, List.of("tag1"));

        ResponseEntity<SuccessResponse<Void>> response = tagController.setPostTags(invalidDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService, never()).setPostTags(any());
    }

    @Test
    void setPostTags_Failure_InvalidPostId_Zero() {
        PostTagsDTO invalidDTO = new PostTagsDTO(0, List.of("tag1"));

        ResponseEntity<SuccessResponse<Void>> response = tagController.setPostTags(invalidDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService, never()).setPostTags(any());
    }

    @Test
    void setPostTags_Failure_InvalidPostId_Negative() {
        PostTagsDTO invalidDTO = new PostTagsDTO(-1, List.of("tag1"));

        ResponseEntity<SuccessResponse<Void>> response = tagController.setPostTags(invalidDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService, never()).setPostTags(any());
    }

    @Test
    void setPostTags_Failure_PropagatesException() {
        PostTagsDTO validDTO = new PostTagsDTO(1, List.of("tag1"));
        doThrow(new EntityNotFoundException("Post not found"))
                .when(tagService).setPostTags(validDTO);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagController.setPostTags(validDTO)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(tagService).setPostTags(validDTO);
    }

    @Test
    void addTagsToPost_Success() {
        Integer pathVariableId = 1;
        PostTagsDTO dtoWithoutId = new PostTagsDTO(null, List.of("tag1"));
        PostTagsDTO dtoWithId = new PostTagsDTO(pathVariableId, List.of("tag1"));

        doNothing().when(tagService).addTagsToPost(dtoWithId);

        ResponseEntity<SuccessResponse<Void>> response =
                tagController.addTagsToPost(pathVariableId, dtoWithoutId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).addTagsToPost(dtoWithId);
    }

    @Test
    void addTagsToPost_Failure_PropagatesException() {
        Integer pathVariableId = 1;
        PostTagsDTO dtoWithoutId = new PostTagsDTO(null, List.of("tag1"));
        PostTagsDTO dtoWithId = new PostTagsDTO(pathVariableId, List.of("tag1"));

        doThrow(new EntityNotFoundException("Post not found"))
                .when(tagService).addTagsToPost(dtoWithId);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagController.addTagsToPost(pathVariableId, dtoWithoutId)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(tagService).addTagsToPost(dtoWithId);
    }

    @Test
    void removeTagsFromPost_Success() {
        Integer pathVariableId = 1;
        PostTagsDTO dtoWithoutId = new PostTagsDTO(null, List.of("tag1"));
        PostTagsDTO dtoWithId = new PostTagsDTO(pathVariableId, List.of("tag1"));

        doNothing().when(tagService).removeTagsFromPost(dtoWithId);

        ResponseEntity<SuccessResponse<Void>> response =
                tagController.removeTagsFromPost(pathVariableId, dtoWithoutId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).removeTagsFromPost(dtoWithId);
    }

    @Test
    void removeTagsFromPost_Failure_PropagatesException() {
        Integer pathVariableId = 1;
        PostTagsDTO dtoWithoutId = new PostTagsDTO(null, List.of("tag1"));
        PostTagsDTO dtoWithId = new PostTagsDTO(pathVariableId, List.of("tag1"));

        doThrow(new EntityNotFoundException("Post not found"))
                .when(tagService).removeTagsFromPost(dtoWithId);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagController.removeTagsFromPost(pathVariableId, dtoWithoutId)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(tagService).removeTagsFromPost(dtoWithId);
    }

    @Test
    void deleteByPostId_Success() {
        doNothing().when(tagService).deleteByPostId(1);

        ResponseEntity<SuccessResponse<Void>> response = tagController.deleteByPostId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).deleteByPostId(1);
    }

    @Test
    void deleteByPostId_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Failed to Delete All Tags For Post"))
                .when(tagService).deleteByPostId(1);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagController.deleteByPostId(1)
        );

        assertTrue(exception.getMessage().contains("Failed to Delete All Tags For Post"));
        verify(tagService).deleteByPostId(1);
    }
}