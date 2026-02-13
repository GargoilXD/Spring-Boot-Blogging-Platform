package com.blog.Service;

import com.blog.Model.User;
import com.blog.Repository.CommentRepository;
import com.blog.Repository.PostRepository;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;

import com.blog.Repository.UserRepository;
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

import java.util.Objects;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository repository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final TagService tagService;

    public PostService(PostRepository repository, UserRepository userRepository, CommentRepository commentRepository, TagService tagService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.tagService = tagService;
    }
    @Cacheable(cacheNames = "Post.findById", key = "#ID")
    public Optional<Post> findById(int ID) {
        return repository.findById(ID);
    }
    @Cacheable(cacheNames = "Post.getAll", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<Post> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    @Cacheable(cacheNames = "Post.count")
    public long count() {
        return repository.count();
    }
    @Transactional
    @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    public Post save(@NotNull CreatePostDTO DTO) {
        userRepository.findById(DTO.userId()).orElseThrow(() -> new EntityNotFoundException("User not found: " + DTO.userId()));
        return repository.save(DTO.toEntity());
    }
    @Transactional
    @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    public Post update(@NotNull UpdatePostDTO DTO) {
        Post post = repository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        User user = userRepository.findById(DTO.userId()).orElseThrow(() -> new EntityNotFoundException("User not found: " + DTO.userId()));
        if (!Objects.equals(post.getUserId(), user.getId())) throw new EntityNotFoundException("User does not own this post: " + DTO.postId());
        return repository.save(DTO.toEntity());
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "Post.findById", key = "#ID"),
        @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    })
    public void delete(int ID) {
        repository.findById(ID).orElseThrow(() -> new EntityNotFoundException("Post not found: " + ID));
        tagService.deleteByPostId(ID);
        commentRepository.deleteByPostId(ID);
        repository.deleteById(ID);
    }
}