package com.blog.Service;

import com.blog.DataAccessor.Interface.PostDataAccessor;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;

import jakarta.persistence.EntityNotFoundException;

import jakarta.validation.constraints.NotNull;
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
    private final PostDataAccessor dataAccessor;

    public PostService(PostDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }
    @Cacheable(cacheNames = "posts", key = "#id")
    public Optional<Post> getPost(long id) {
        return dataAccessor.findByID(id);
    }
    @Cacheable(cacheNames = "postsList")
    public List<Post> getAllPosts(GetPostDTO dto) {
        return dataAccessor.findAll(dto);
    }
    @Cacheable(cacheNames = "postCounts")
    public long countPosts() {
        return dataAccessor.count();
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = {"postsList", "postCounts"}, allEntries = true)
    }, put = {
        @CachePut(cacheNames = "posts", key = "#result.id")
    })
    public Post save(@NotNull CreatePostDTO dto) {
        return dataAccessor.save(dto);
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
        return dataAccessor.update(dto);
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "posts", key = "#id"),
        @CacheEvict(cacheNames = {"postsList", "postCounts"}, allEntries = true)
    })
    public void delete(long id) {
        if (dataAccessor.findByID(id).isEmpty()) throw new IllegalStateException("Post not found: " + id);
        dataAccessor.delete(id);
    }
}