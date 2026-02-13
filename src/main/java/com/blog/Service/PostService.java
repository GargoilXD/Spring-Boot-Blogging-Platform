package com.blog.Service;

import com.blog.Repository.PostRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
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

import java.util.Optional;

@Service
public class PostService {
    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }
    @Cacheable(cacheNames = "Post.findById", key = "#ID")
    public Optional<Post> findById(int ID) {
        return repository.findById(ID);
    }
    @Cacheable(cacheNames = "Post.getAll", key = "{#pageable.pageNumber, #pageable.pageSize}")
    public Page<Post> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    @Cacheable(cacheNames = "Post.count")
    public long count() {
        return repository.count();
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    }, put = {
        @CachePut(cacheNames = "Post.findById", key = "#result.id")
    })
    public Post save(@NotNull CreatePostDTO DTO) {
        return repository.save(DTO.toEntity());
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true),
        @CacheEvict(cacheNames = "Post.findById", key = "#DTO.postId()")
    }, put = {
        @CachePut(cacheNames = "Post.findById", key = "#DTO.postId()")
    })
    public Post update(@NotNull UpdatePostDTO DTO) {
        repository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        return repository.save(DTO.toEntity());
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "Post.findById", key = "#id"),
        @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    })
    public void delete(int ID) {
        repository.findById(ID).orElseThrow(() -> new EntityNotFoundException("Post not found: " + ID));
        repository.deleteById(ID);
    }
}