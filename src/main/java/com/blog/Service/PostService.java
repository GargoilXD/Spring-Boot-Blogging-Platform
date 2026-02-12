package com.blog.Service;

import com.blog.Repository.PostRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;

import jakarta.persistence.EntityNotFoundException;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }
    @Cacheable(cacheNames = "posts", key = "#id")
    public Optional<Post> getPost(long id) {
        return repository.findById(id);
    }
    @Cacheable(cacheNames = "postsList")
    public Page<Post> getAllPosts(Pageable pageable) {
        return repository.findAll(pageable);
    }
    @Cacheable(cacheNames = "postCounts")
    public long countPosts() {
        return repository.count();
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = {"postsList", "postCounts"}, allEntries = true)
    }, put = {
        @CachePut(cacheNames = "posts", key = "#result.id")
    })
    public Post save(@NotNull CreatePostDTO dto) {
        return repository.save(dto.toEntity());
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = {"postsList", "postCounts"}, allEntries = true),
        @CacheEvict(cacheNames = "posts", key = "#dto.postId()")
    }, put = {
        @CachePut(cacheNames = "posts", key = "#dto.postId()")
    })
    public Post update(@NotNull UpdatePostDTO dto) {
        getPost(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        return repository.save(dto.toEntity());
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "posts", key = "#id"),
        @CacheEvict(cacheNames = {"postsList", "postCounts"}, allEntries = true)
    })
    public void delete(long id) {
        if (repository.findById(id).isEmpty()) throw new IllegalStateException("Post not found: " + id);
        repository.deleteById(id);
    }
}