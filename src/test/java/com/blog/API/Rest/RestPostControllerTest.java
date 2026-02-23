package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.Service.PostService;
import com.blog.Model.Post;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("RestPostController Unit Tests")
class RestPostControllerTest {

    @Mock private PostService postService;

    @InjectMocks
    private RestPostController postController;

    private CreatePostDTO createDTO;
    private UpdatePostDTO updateDTO;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        createDTO  = new CreatePostDTO("Title", "Content", false);
        updateDTO  = new UpdatePostDTO("Updated Title", "Updated Content", false);
        mockPost   = new Post();
    }

    // ─── GET /api/posts/{id} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/posts/{id}")
    class FindById {

        @Test
        @DisplayName("Returns 200 when post exists")
        void findById_Success() {
            when(postService.findById(1)).thenReturn(Optional.of(mockPost));

            ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.findById(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(postService).findById(1);
        }

        @Test
        @DisplayName("Returns 404 when post does not exist")
        void findById_NotFound() {
            when(postService.findById(99)).thenReturn(Optional.empty());

            ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.findById(99);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ─── GET /api/posts ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/posts")
    class FindAll {

        @Test
        @DisplayName("Returns 200 with paginated posts")
        void findAll_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(mockPost));
            when(postService.findAll(pageable)).thenReturn(postPage);

            ResponseEntity<SuccessResponse<Page<ResponsePostDTO>>> response = postController.findAll(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    // ─── POST /api/posts ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/posts")
    class CreatePost {

        @Test
        @DisplayName("Returns 201 when post is created")
        void createPost_Success() {
            when(postService.save(createDTO)).thenReturn(mockPost);

            ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.createPost(createDTO);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(postService).save(createDTO);
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void createPost_Failure_UserNotFound() {
            when(postService.save(createDTO)).thenThrow(new EntityNotFoundException("Authenticated user not found"));

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postController.createPost(createDTO)
            );

            assertTrue(ex.getMessage().contains("Authenticated user not found"));
        }
    }

    // ─── PUT /api/posts/{id} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/posts/{id}")
    class UpdatePost {

        @Test
        @DisplayName("Returns 200 when post is updated")
        void updatePost_Success() {
            when(postService.update(1, updateDTO)).thenReturn(mockPost);

            ResponseEntity<SuccessResponse<ResponsePostDTO>> response = postController.updatePost(1, updateDTO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(postService).update(1, updateDTO);
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void updatePost_Failure_PostNotFound() {
            doThrow(new EntityNotFoundException("Post not found")).when(postService).update(99, updateDTO);

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postController.updatePost(99, updateDTO)
            );

            assertEquals("Post not found", ex.getMessage());
        }

        @Test
        @DisplayName("Propagates SecurityException when user is not the owner")
        void updatePost_Failure_NotOwner() {
            doThrow(new SecurityException("User does not own this post")).when(postService).update(1, updateDTO);

            assertThrows(SecurityException.class, () -> postController.updatePost(1, updateDTO));
        }
    }

    // ─── DELETE /api/posts/{id} ──────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/posts/{id}")
    class DeletePost {

        @Test
        @DisplayName("Returns 200 when post is deleted")
        void deletePost_Success() {
            doNothing().when(postService).delete(1);

            ResponseEntity<SuccessResponse<Void>> response = postController.deletePost(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(postService).delete(1);
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void deletePost_Failure_PostNotFound() {
            doThrow(new EntityNotFoundException("Post not found")).when(postService).delete(99);

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postController.deletePost(99)
            );

            assertEquals("Post not found", ex.getMessage());
        }
    }
}