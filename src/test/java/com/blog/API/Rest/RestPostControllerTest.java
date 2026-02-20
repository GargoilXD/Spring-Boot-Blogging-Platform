package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.Service.PostService;
import com.blog.Model.Post;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestPostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private RestPostController postController;

    private CreatePostDTO createDTO;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        createDTO = new CreatePostDTO(1, "Title", "Content", false);
        mockPost = new Post();
    }

    @Test
    void findById_Success() {
        when(postService.findById(1)).thenReturn(Optional.of(mockPost));

        ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.findById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(postService).findById(1);
    }

    @Test
    void findById_NotFound() {
        when(postService.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.findById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(postService).findById(1);
    }

    @Test
    void findAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(mockPost));
        when(postService.findAll(pageable)).thenReturn(postPage);

        ResponseEntity<SuccessResponse<Page<ResponsePostDTO>>> response = postController.findAll(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(postService).findAll(pageable);
    }

    @Test
    void createPost_Success() {
        when(postService.save(createDTO)).thenReturn(mockPost);

        ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.createPost(createDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(postService).save(createDTO);
    }

    @Test
    void updatePost_Success() {
        Integer pathVariableId = 1;
        UpdatePostDTO dtoWithoutId = new UpdatePostDTO(null, 1, "Title", "Content", false);
        UpdatePostDTO dtoWithId = new UpdatePostDTO(pathVariableId, 1, "Title", "Content", false);

        when(postService.update(dtoWithId)).thenReturn(mockPost);

        ResponseEntity<SuccessResponse<ResponsePostDTO>> response =
                postController.updatePost(pathVariableId, dtoWithoutId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(postService).update(dtoWithId);
    }

    @Test
    void updatePost_Failure_PropagatesException() {
        Integer pathVariableId = 1;
        UpdatePostDTO dtoWithoutId = new UpdatePostDTO(null, 1, "Title", "Content", false);
        UpdatePostDTO dtoWithId = new UpdatePostDTO(pathVariableId, 1, "Title", "Content", false);

        doThrow(new EntityNotFoundException("Post not found"))
                .when(postService).update(dtoWithId);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postController.updatePost(pathVariableId, dtoWithoutId)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(postService).update(dtoWithId);
    }

    @Test
    void deletePost_Success() {
        doNothing().when(postService).delete(1);

        ResponseEntity<SuccessResponse<Void>> response = postController.deletePost(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(postService).delete(1);
    }

    @Test
    void deletePost_Failure_PropagatesException() {
        doThrow(new EntityNotFoundException("Post not found"))
                .when(postService).delete(1);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postController.deletePost(1)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(postService).delete(1);
    }
}