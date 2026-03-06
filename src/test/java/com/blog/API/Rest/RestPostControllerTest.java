package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.Model.User;
import com.blog.Service.PostService;
import com.blog.Model.Post;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestPostController Unit Tests")
class RestPostControllerTest {
    @Mock private PostService postService;
    @Mock private Post mockPost;
    @Mock private User mockUser;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private RestPostController postController;

    private CreatePostDTO createDTO;
    private UpdatePostDTO updateDTO;

    @BeforeEach
    void setUp() {
        createDTO = new CreatePostDTO("Title", "Content", false);
        updateDTO = new UpdatePostDTO("Updated Title", "Updated Content", false);
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/posts/{id}")
    class FindById {
        @Test
        @DisplayName("Returns 200 when post exists")
        void findById_Success() {
            when(mockPost.getId()).thenReturn(1);
            when(mockPost.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockPost.getTitle()).thenReturn("Title");
            when(mockPost.getBody()).thenReturn("Content");
            when(mockPost.isDraft()).thenReturn(false);
            when(mockPost.getCreatedAt()).thenReturn(null);
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
    @Nested
    @DisplayName("GET /api/posts")
    class FindAll {
        @Test
        @DisplayName("Returns 200 with paginated posts")
        void findAll_Success() {
            when(mockPost.getId()).thenReturn(1);
            when(mockPost.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockPost.getTitle()).thenReturn("Title");
            when(mockPost.getBody()).thenReturn("Content");
            when(mockPost.isDraft()).thenReturn(false);
            when(mockPost.getCreatedAt()).thenReturn(null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(mockPost));
            when(postService.findAll(pageable)).thenReturn(postPage);
            ResponseEntity<SuccessResponse<Page<ResponsePostDTO>>> response = postController.findAll(pageable);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }
    @Nested
    @DisplayName("POST /api/posts")
    class CreatePost {
        @Test
        @DisplayName("Returns 201 when post is created")
        void createPost_Success() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            
            when(mockPost.getId()).thenReturn(1);
            when(mockPost.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockPost.getTitle()).thenReturn("Title");
            when(mockPost.getBody()).thenReturn("Content");
            when(mockPost.isDraft()).thenReturn(false);
            when(mockPost.getCreatedAt()).thenReturn(null);
            
            when(postService.save(createDTO, "testuser")).thenReturn(CompletableFuture.completedFuture(mockPost));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponsePostDTO>>> responseFuture = postController.createPost(createDTO);
            ResponseEntity<SuccessResponse<ResponsePostDTO>> response = responseFuture.join();
            
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(postService).save(createDTO, "testuser");
        }

        @Test
        @DisplayName("Propagates EntityNotFoundException when authenticated user not found")
        void createPost_Failure_UserNotFound() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            
            when(postService.save(createDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new EntityNotFoundException("Authenticated user not found")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponsePostDTO>>> responseFuture = postController.createPost(createDTO);
            
            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof EntityNotFoundException);
            assertTrue(ex.getCause().getMessage().contains("Authenticated user not found"));
        }
    }
    @Nested
    @DisplayName("PUT /api/posts/{id}")
    class UpdatePost {
        @Test
        @DisplayName("Returns 200 when post is updated")
        void updatePost_Success() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(mockPost.getId()).thenReturn(1);
            when(mockPost.getUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(1);
            when(mockPost.getTitle()).thenReturn("Title");
            when(mockPost.getBody()).thenReturn("Content");
            when(mockPost.isDraft()).thenReturn(false);
            when(mockPost.getCreatedAt()).thenReturn(null);
            
            when(postService.update(1, updateDTO, "testuser")).thenReturn(CompletableFuture.completedFuture(mockPost));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponsePostDTO>>> responseFuture = postController.updatePost(1, updateDTO);
            ResponseEntity<SuccessResponse<ResponsePostDTO>> response = responseFuture.join();
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(postService).update(1, updateDTO, "testuser");
        }
        @Test
        @DisplayName("Propagates EntityNotFoundException from service")
        void updatePost_Failure_PostNotFound() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(postService.update(99, updateDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new EntityNotFoundException("Post not found")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponsePostDTO>>> responseFuture = postController.updatePost(99, updateDTO);
            
            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof EntityNotFoundException);
            assertEquals("Post not found", ex.getCause().getMessage());
        }
        @Test
        @DisplayName("Propagates SecurityException when user is not the owner")
        void updatePost_Failure_NotOwner() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(postService.update(1, updateDTO, "testuser")).thenReturn(CompletableFuture.failedFuture(new SecurityException("User does not own this post")));
            CompletableFuture<ResponseEntity<SuccessResponse<ResponsePostDTO>>> responseFuture = postController.updatePost(1, updateDTO);

            CompletionException ex = assertThrows(CompletionException.class, responseFuture::join);
            assertTrue(ex.getCause() instanceof SecurityException);
        }
    }
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
            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> postController.deletePost(99));
            assertEquals("Post not found", ex.getMessage());
        }
    }
}