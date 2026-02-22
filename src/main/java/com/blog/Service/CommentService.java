package com.blog.Service;

import com.blog.Model.Post;
import com.blog.Model.User;
import com.blog.Repository.CommentRepository;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

import java.util.List;
import java.util.Objects;

import com.blog.Repository.PostRepository;
import com.blog.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
        User user = userRepository.findById(dto.userId()).orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.userId()));
        Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        Comment comment = dto.toEntity();
        user.addComment(comment);
        post.addComment(comment);
        return repository.save(comment);
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#dto.postId()")})
    public Comment update(@NotNull(message = "Comment id is required") @Min(1) Integer id, @NotNull UpdateCommentDTO dto) {
        Comment comment = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        User user = userRepository.findById(dto.userId()).orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.userId()));
        Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        if (!Objects.equals(comment.getUser().getId(), user.getId())) throw new EntityNotFoundException("User does not own this comment: " + id);
        if (!Objects.equals(comment.getPost().getId(), post.getId())) throw new EntityNotFoundException("Comment does not belong to this post: " + id);
        dto.update(comment);
        return repository.save(comment);
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", allEntries = true)})
    public void delete(@NotNull(message = "Comment id is required") @Min(1) Integer id) {
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        repository.deleteById(id);
    }
}
