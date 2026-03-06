package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Tags.ResponseTagsDTO;
import com.blog.DataTransporter.Tags.TagDTO;
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
        doNothing().when(tagService).setPostTags(1, List.of("tag1", "tag2"));
        ResponseEntity<SuccessResponse<Void>> response = tagController.setPostTags(1, new TagDTO(List.of("tag1", "tag2")));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).setPostTags(1, List.of("tag1", "tag2"));
    }
    @Test
    void setPostTags_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Post not found")).when(tagService).setPostTags(1, List.of("tag1", "tag2"));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> tagController.setPostTags(1, new TagDTO(List.of("tag1", "tag2"))));
        assertEquals("Post not found", exception.getMessage());
        verify(tagService).setPostTags(1, List.of("tag1", "tag2"));
    }
    @Test
    void addTagsToPost_Success() {
        doNothing().when(tagService).addTagsToPost(1, List.of("tag3"));
        ResponseEntity<SuccessResponse<Void>> response = tagController.addTagsToPost(1, new TagDTO(List.of("tag3")));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).addTagsToPost(1, List.of("tag3"));
    }
    @Test
    void addTagsToPost_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Post not found")).when(tagService).addTagsToPost(1, List.of("tag3"));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> tagController.addTagsToPost(1, new TagDTO(List.of("tag3"))));
        assertEquals("Post not found", exception.getMessage());
        verify(tagService).addTagsToPost(1, List.of("tag3"));
    }
    @Test
    void removeTagsFromPost_Success() {
        doNothing().when(tagService).removeTagsFromPost(1, List.of("tag3"));
        ResponseEntity<SuccessResponse<Void>> response = tagController.removeTagsFromPost(1, new TagDTO(List.of("tag3")));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tagService).removeTagsFromPost(1, List.of("tag3"));
    }
    @Test
    void removeTagsFromPost_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Post not found")).when(tagService).removeTagsFromPost(1, List.of("tag3"));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> tagController.removeTagsFromPost(1, new TagDTO(List.of("tag3"))));
        assertEquals("Post not found", exception.getMessage());
        verify(tagService).removeTagsFromPost(1, List.of("tag3"));
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
        doThrow(new EntityNotFoundException("Failed to Delete All Tags For Post")).when(tagService).deleteByPostId(1);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> tagController.deleteByPostId(1));
        assertTrue(exception.getMessage().contains("Failed to Delete All Tags For Post"));
        verify(tagService).deleteByPostId(1);
    }
}