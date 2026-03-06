package com.blog.Service;

import com.blog.Model.User;
import com.blog.Repository.CommentRepository;
import com.blog.Repository.PostRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;
import com.blog.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class PostService {
    private final PostRepository repository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final TagService tagService;
    private final PostCacheService postCacheService;
    private final PerformanceMetricsService metricsService;

    @Cacheable(cacheNames = "Post.findById", key = "#id")
    public Optional<Post> findById(int id) {
        long start = System.currentTimeMillis();
        Optional<Post> result = repository.findById(id);
        result.ifPresent(post -> {
            postCacheService.put(post);
            postCacheService.incrementViews(id);
        });
        metricsService.recordLatency("post.findById", System.currentTimeMillis() - start);
        metricsService.incrementCounter("post.findById");
        return result;
    }

    @Cacheable(cacheNames = "Post.getAll", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<Post> findAll(Pageable pageable) {
        long start = System.currentTimeMillis();
        Page<Post> page = repository.findAll(pageable);
        page.getContent().forEach(postCacheService::put);
        metricsService.recordLatency("post.findAll", System.currentTimeMillis() - start);
        metricsService.incrementCounter("post.findAll");
        return page;
    }

    @Cacheable(cacheNames = "Post.count")
    public long count() {
        return repository.count();
    }
    public List<Post> getTrending(int top) {
        postCacheService.rebuildTrending(top);
        return postCacheService.getTrending();
    }
    public List<Post> search(String keyword) {
        if (postCacheService.size() > 0) {
            return postCacheService.search(keyword);
        }
        repository.findAll().forEach(postCacheService::put);
        return postCacheService.search(keyword);
    }
    @Transactional
    @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    public Post save(@NotNull CreatePostDTO dto) {
        long start = System.currentTimeMillis();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Post post = new Post();
        post.setTitle(dto.title());
        post.setBody(dto.body());
        post.setDraft(dto.draft());
        user.addPost(post);
        Post saved = repository.save(post);
        postCacheService.put(saved);
        metricsService.recordLatency("post.save", System.currentTimeMillis() - start);
        metricsService.incrementCounter("post.save");
        return saved;
    }
    @Transactional
    @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    public Post update(@NotNull(message = "Post id is required") @Min(1) Integer postId, @NotNull UpdatePostDTO dto) {
        long start = System.currentTimeMillis();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Post post = repository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
        if (!post.getUser().getId().equals(user.getId())) throw new SecurityException("User does not own this post: " + postId);
        post.setTitle(dto.title());
        post.setBody(dto.body());
        post.setDraft(dto.draft());
        Post updated = repository.save(post);
        postCacheService.put(updated);
        metricsService.recordLatency("post.update", System.currentTimeMillis() - start);
        metricsService.incrementCounter("post.update");
        return updated;
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "Post.findById", key = "#id"),
        @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    })
    public void delete(int id) {
        long start = System.currentTimeMillis();
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));
        tagService.deleteByPostId(id);
        commentRepository.deleteByPostId(id);
        repository.deleteById(id);
        postCacheService.evict(id);
        metricsService.recordLatency("post.delete", System.currentTimeMillis() - start);
        metricsService.incrementCounter("post.delete");
    }
}