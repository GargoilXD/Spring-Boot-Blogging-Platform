package com.blog.Service;

import com.blog.Model.Post;
import com.blog.Model.User;
import com.blog.Repository.CommentRepository;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;
import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
public class CommentService {
    final UserRepository userRepository;
    final PostRepository postRepository;
    final CommentRepository repository;

    @Cacheable(cacheNames = "Comment.findByPostId", key = "#id")
    public List<Comment> findByPostId(int id) {
        return repository.findByPostId(id);
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#dto.postId()")})
    public Comment save(CreateCommentDTO dto) {
        long start = System.currentTimeMillis();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        Comment comment = new Comment();
        comment.setBody(dto.body());
        user.addComment(comment);
        post.addComment(comment);
        Comment saved = repository.save(comment);
        metricsService.recordLatency("comment.save", System.currentTimeMillis() - start);
        metricsService.incrementCounter("comment.save");
        return saved;
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#dto.postId()")})
    public Comment update(@NotNull(message = "Comment id is required") @Min(1) Integer id, @NotNull UpdateCommentDTO dto) {
        long start = System.currentTimeMillis();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + username));
        Comment comment = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        if (!comment.getUser().getId().equals(user.getId())) throw new SecurityException("User does not own this comment: " + id);
        if (!comment.getPost().getId().equals(post.getId())) throw new EntityNotFoundException("Comment does not belong to this post: " + id);
        comment.setBody(dto.body());
        Comment updated = repository.save(comment);
        metricsService.recordLatency("comment.update", System.currentTimeMillis() - start);
        metricsService.incrementCounter("comment.update");
        return updated;
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", allEntries = true)})
    public void delete(@NotNull(message = "Comment id is required") @Min(1) Integer id) {
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        repository.deleteById(id);
    }
}
