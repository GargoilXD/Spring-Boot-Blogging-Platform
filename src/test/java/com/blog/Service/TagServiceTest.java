package com.blog.Service;

import com.blog.Model.Post;
import com.blog.Model.PostTags;
import com.blog.Repository.PostRepository;
import com.blog.Repository.TagRepository;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository repository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostTags postTags;

    @Mock
    private Post post;

    @InjectMocks
    private TagService tagService;

    private List<String> tags;

    @BeforeEach
    void setUp() {
        tags = List.of("tag1", "tag2");
//        post = new Post(1, 1, "Title", "Content", false, LocalDateTime.now());
    }

    @Test
    void findAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostTags> postTagsPage = new PageImpl<>(List.of(postTags));
        when(repository.findAll(pageable)).thenReturn(postTagsPage);

        Page<PostTags> result = tagService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository).findAll(pageable);
    }

    @Test
    void findByPostId_Success() {
        when(postTags.getTags()).thenReturn(new ArrayList<>(List.of("tag1", "tag2")));
        when(repository.findByPostId(1)).thenReturn(Optional.of(postTags));

        List<String> result = tagService.findByPostId(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("tag1"));
        assertTrue(result.contains("tag2"));
        verify(repository).findByPostId(1);
    }

    @Test
    void findByPostId_NotFound_ReturnsEmptyList() {
        when(repository.findByPostId(1)).thenReturn(Optional.empty());

        List<String> result = tagService.findByPostId(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findByPostId(1);
    }

    @Test
    void count_Success() {
        when(repository.count()).thenReturn(5L);

        long result = tagService.count();

        assertEquals(5L, result);
        verify(repository).count();
    }

    @Test
    void setPostTags_Success() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(repository.findByPostId(1)).thenReturn(Optional.of(postTags));
        when(repository.save(any(PostTags.class))).thenReturn(postTags);

        assertDoesNotThrow(() -> tagService.setPostTags(1, tags));

        verify(postRepository).findById(1);
        verify(repository).findByPostId(1);
        verify(repository).save(any(PostTags.class));
    }

    @Test
    void setPostTags_Failure_PostNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.setPostTags(1, tags)
        );

        assertTrue(exception.getMessage().contains("Post not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void addTagsToPost_Success() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(repository.findByPostId(1)).thenReturn(Optional.of(postTags));
        when(repository.save(any(PostTags.class))).thenReturn(postTags);

        assertDoesNotThrow(() -> tagService.addTagsToPost(1, tags));

        verify(repository).save(any(PostTags.class));
    }

    @Test
    void addTagsToPost_Failure_PostNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.addTagsToPost(1, tags)
        );

        assertTrue(exception.getMessage().contains("Post not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void addTagsToPost_Failure_TagsNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(repository.findByPostId(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.addTagsToPost(1, tags)
        );

        assertTrue(exception.getMessage().contains("Failed to Add Tags For Post"));
        verify(repository, never()).save(any());
    }

    @Test
    void removeTagsFromPost_Success_DeleteWhenEmpty() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(repository.findByPostId(1)).thenReturn(Optional.of(postTags));
        doNothing().when(repository).delete(any(PostTags.class));

        assertDoesNotThrow(() -> tagService.removeTagsFromPost(1, tags));

        verify(repository).delete(any(PostTags.class));
        verify(repository, never()).save(any());
    }

    @Test
    void removeTagsFromPost_Failure_PostNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.removeTagsFromPost(1, tags)
        );

        assertTrue(exception.getMessage().contains("Post not found"));
    }

    @Test
    void removeTagsFromPost_Failure_TagsNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(repository.findByPostId(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.removeTagsFromPost(1, tags)
        );

        assertTrue(exception.getMessage().contains("Failed to Remove Tags For Post"));
    }

    @Test
    void deleteByPostId_Success() {
        when(repository.findByPostId(1)).thenReturn(Optional.of(postTags));
        doNothing().when(repository).deleteByPostId(1);

        assertDoesNotThrow(() -> tagService.deleteByPostId(1));

        verify(repository).deleteByPostId(1);
    }

    @Test
    void deleteByPostId_Failure_TagsNotFound() {
        when(repository.findByPostId(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.deleteByPostId(1)
        );

        assertTrue(exception.getMessage().contains("Failed to Delete All Tags For Post"));
        verify(repository, never()).deleteByPostId(anyInt());
    }
}