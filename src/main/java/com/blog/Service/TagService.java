package com.blog.Service;

import com.blog.Model.PostTags;
import com.blog.Repository.PostRepository;
import com.blog.Repository.TagRepository;

import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class TagService {
    final TagRepository repository;
    final PostRepository postRepository;
    final PerformanceMetricsService metricsService;

    @Cacheable(cacheNames = "PostTags.findAll", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<PostTags> findAll(Pageable pageable) {
        long start = System.currentTimeMillis();
        Page<PostTags> result = repository.findAll(pageable);
        metricsService.recordLatency("tag.findAll", System.currentTimeMillis() - start);
        metricsService.incrementCounter("tag.findAll");
        return result;
    }
    @Cacheable(cacheNames = "PostTags.findByPostId", key = "#postId")
    public List<String> findByPostId(int postId) {
        long start = System.currentTimeMillis();
        List<String> result = repository.findByPostId(postId).map(PostTags::getTags).orElse(Collections.emptyList());
        metricsService.recordLatency("tag.findByPostId", System.currentTimeMillis() - start);
        metricsService.incrementCounter("tag.findByPostId");
        return result;
    }
    @Cacheable(cacheNames = "PostTags.count")
    public long count() {
        return repository.count();
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void setPostTags(@NotNull(message = "Post ID is required") @Min(1) Integer postId, @NotEmpty(message = "Tags are required") List<String> tags) {
        long start = System.currentTimeMillis();
        postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
        repository.findByPostId(postId).ifPresent(repository::delete);
        repository.save(new PostTags(null, postId, tags));
        metricsService.recordLatency("tag.setPostTags", System.currentTimeMillis() - start);
        metricsService.incrementCounter("tag.setPostTags");
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void addTagsToPost(@NotNull(message = "Post ID is required") @Min(1) Integer postId, @NotEmpty(message = "Tags are required") List<String> tags) {
        long start = System.currentTimeMillis();
        postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
        PostTags existing = repository.findByPostId(postId).orElseThrow(() -> new EntityNotFoundException("Failed to Add Tags For Post:" + postId));
        Set<String> currentTags = new HashSet<>(existing.getTags());
        currentTags.addAll(tags);
        existing.setTags(new ArrayList<>(currentTags));
        repository.save(existing);
        metricsService.recordLatency("tag.addTagsToPost", System.currentTimeMillis() - start);
        metricsService.incrementCounter("tag.addTagsToPost");
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void removeTagsFromPost(@NotNull(message = "Post ID is required") @Min(1) Integer postId, @NotEmpty(message = "Tags are required") List<String> tags) {
        long start = System.currentTimeMillis();
        postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
        PostTags existing = repository.findByPostId(postId).orElseThrow(() -> new EntityNotFoundException("Failed to Remove Tags For Post:" + postId));
        Set<String> currentTags = new HashSet<>(existing.getTags());
        tags.forEach(currentTags::remove);
        if (currentTags.isEmpty()) repository.delete(existing);
        else {
            existing.setTags(new ArrayList<>(currentTags));
            repository.save(existing);
        }
        metricsService.recordLatency("tag.removeTagsFromPost", System.currentTimeMillis() - start);
        metricsService.incrementCounter("tag.removeTagsFromPost");
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void deleteByPostId(int postId) {
        long start = System.currentTimeMillis();
        repository.findByPostId(postId).orElseThrow(() -> new EntityNotFoundException("Failed to Delete All Tags For Post:" + postId));
        repository.deleteByPostId(postId);
        metricsService.recordLatency("tag.deleteByPostId", System.currentTimeMillis() - start);
        metricsService.incrementCounter("tag.deleteByPostId");
    }
}
