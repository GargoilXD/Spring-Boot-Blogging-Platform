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

import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class PostService {
    private final PostRepository repository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final TagService tagService;

    @Cacheable(cacheNames = "Post.findById", key = "#id")
    public Optional<Post> findById(int id) {
        return repository.findById(id);
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
    public Post save(@NotNull CreatePostDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Post post = dto.toEntity();
        user.addPost(post);
        return repository.save(post);
    }
    @Transactional
    @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    public Post update(@NotNull(message = "Post id is required") @Min(1) Integer postId, @NotNull UpdatePostDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Post post = repository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
        if (!post.getUser().getId().equals(user.getId())) throw new SecurityException("User does not own this post: " + postId);
        dto.update(post);
        return repository.save(post);
    }
    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "Post.findById", key = "#id"),
        @CacheEvict(cacheNames = {"Post.getAll", "Post.count"}, allEntries = true)
    })
    public void delete(int id) {
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));
        tagService.deleteByPostId(id);
        commentRepository.deleteByPostId(id);
        repository.deleteById(id);
    }
}