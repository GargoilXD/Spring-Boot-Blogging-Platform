package com.blog.Service;

import com.blog.Model.Post;
import com.blog.Model.User;
import com.blog.Repository.CommentRepository;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    final UserRepository userRepository;
    final PostRepository postRepository;
    final CommentRepository repository;
    final PerformanceMetricsService metricsService;

    @Cacheable(cacheNames = "Comment.findByPostId", key = "#id")
    public List<Comment> findByPostId(int id) {
        long start = System.currentTimeMillis();
        List<Comment> result = repository.findByPostId(id);
        metricsService.recordLatency("comment.findByPostId", System.currentTimeMillis() - start);
        metricsService.incrementCounter("comment.findByPostId");
        return result;
    }
    @Transactional
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#dto.postId()")})
    @Async("blogExecutor")
    public CompletableFuture<Comment> save(CreateCommentDTO dto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        Comment comment = new Comment();
        comment.setBody(dto.body());
        user.addComment(comment);
        post.addComment(comment);
        return CompletableFuture.completedFuture(repository.save(comment));
    }
    @Transactional
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#dto.postId()")})
    @Async("blogExecutor")
    public CompletableFuture<Comment> update(@NotNull(message = "Comment id is required") @Min(1) Integer id, @NotNull UpdateCommentDTO dto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Comment comment = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        if (!comment.getUser().getId().equals(user.getId())) throw new SecurityException("User does not own this comment: " + id);
        if (!comment.getPost().getId().equals(post.getId())) throw new EntityNotFoundException("Comment does not belong to this post: " + id);
        comment.setBody(dto.body());
        return CompletableFuture.completedFuture(repository.save(comment));
    }
    @Transactional
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", allEntries = true)})
    public void delete(@NotNull(message = "Comment id is required") @Min(1) Integer id) {
        Comment comment = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        if (!comment.getUser().getId().equals(user.getId())) throw new SecurityException("User does not own this comment: " + id);
        repository.deleteById(id);
        metricsService.incrementCounter("comment.delete");
    }
}
