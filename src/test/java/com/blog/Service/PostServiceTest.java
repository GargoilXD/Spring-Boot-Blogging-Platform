package com.blog.Service;

import com.blog.Model.User;
import com.blog.Model.Post;
import com.blog.Repository.CommentRepository;
import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceTest {

    @Mock private PostRepository    repository;
    @Mock private UserRepository    userRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private TagService        tagService;
    @Mock private User              user;
    @Mock private Post              post;
    @Mock private SecurityContext   securityContext;
    @Mock private Authentication    authentication;

    @InjectMocks
    private PostService postService;

    private CreatePostDTO createDTO;
    private UpdatePostDTO updateDTO;

    @BeforeEach
    void setUp() {
        createDTO = new CreatePostDTO("Title", "Content", false);
        updateDTO = new UpdatePostDTO("Updated Title", "Updated Content", false);

//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Returns post when it exists")
        void findById_Success() {
            when(repository.findById(1)).thenReturn(Optional.of(post));
            Optional<Post> result = postService.findById(1);
            assertTrue(result.isPresent());
            verify(repository).findById(1);
        }

        @Test
        @DisplayName("Returns empty Optional when post is not found")
        void findById_NotFound() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            Optional<Post> result = postService.findById(99);
            assertFalse(result.isPresent());
        }
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Returns paginated posts")
        void findAll_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(post));
            when(repository.findAll(pageable)).thenReturn(postPage);

            Page<Post> result = postService.findAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("Returns empty page when no posts exist")
        void findAll_EmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(repository.findAll(pageable)).thenReturn(Page.empty());
            assertTrue(postService.findAll(pageable).isEmpty());
        }
    }

    // ─── count ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("count")
    class Count {

        @Test
        @DisplayName("Returns total post count")
        void count_Success() {
            when(repository.count()).thenReturn(5L);
            assertEquals(5L, postService.count());
        }

        @Test
        @DisplayName("Returns zero when no posts exist")
        void count_Zero() {
            when(repository.count()).thenReturn(0L);
            assertEquals(0L, postService.count());
        }
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Creates post using authenticated user from SecurityContext")
        void save_Success() {
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.save(any(Post.class))).thenReturn(post);

            Post result = postService.save(createDTO);

            assertNotNull(result);
            verify(userRepository).findByUsername("testuser");
            verify(user).addPost(any(Post.class));
            verify(repository).save(any(Post.class));
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when authenticated user not found in DB")
        void save_Failure_UserNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postService.save(createDTO)
            );

            assertTrue(ex.getMessage().contains("Authenticated user not found"));
            verify(repository, never()).save(any());
        }
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Updates post when authenticated user is the owner")
        void update_Success() {
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.findById(1)).thenReturn(Optional.of(post));
            when(user.getId()).thenReturn(42);
            when(post.getUser()).thenReturn(user);
            when(repository.save(post)).thenReturn(post);

            Post result = postService.update(1, updateDTO);

            assertNotNull(result);
            verify(repository).save(post);
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when authenticated user not found in DB")
        void update_Failure_UserNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postService.update(1, updateDTO)
            );

            assertTrue(ex.getMessage().contains("Authenticated user not found"));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when post does not exist")
        void update_Failure_PostNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.findById(99)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postService.update(99, updateDTO)
            );

            assertTrue(ex.getMessage().contains("Post not found"));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws SecurityException when authenticated user does not own the post")
        void update_Failure_NotOwner() {
            User postOwner = mock(User.class);
            when(postOwner.getId()).thenReturn(999);
            when(user.getId()).thenReturn(1);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(repository.findById(1)).thenReturn(Optional.of(post));
            when(post.getUser()).thenReturn(postOwner);

            assertThrows(SecurityException.class, () -> postService.update(1, updateDTO));
            verify(repository, never()).save(any());
        }
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deletes post and all associated data")
        void delete_Success() {
            when(repository.findById(1)).thenReturn(Optional.of(post));
            doNothing().when(tagService).deleteByPostId(1);
            doNothing().when(commentRepository).deleteByPostId(1);
            doNothing().when(repository).deleteById(1);

            assertDoesNotThrow(() -> postService.delete(1));

            verify(tagService).deleteByPostId(1);
            verify(commentRepository).deleteByPostId(1);
            verify(repository).deleteById(1);
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when post does not exist")
        void delete_Failure_PostNotFound() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> postService.delete(99)
            );

            assertTrue(ex.getMessage().contains("Post not found"));
            verify(tagService, never()).deleteByPostId(anyInt());
            verify(commentRepository, never()).deleteByPostId(anyInt());
            verify(repository, never()).deleteById(anyInt());
        }
    }
}