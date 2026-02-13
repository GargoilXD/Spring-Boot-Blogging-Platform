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
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
public class CommentService {
    UserRepository userRepository;
    PostRepository postRepository;
    CommentRepository repository;

    public CommentService(UserRepository userRepository, PostRepository postRepository, CommentRepository repository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.repository = repository;
    }
    @Cacheable(cacheNames = "Comment.findByPostId", key = "#ID")
    public List<Comment> findByPostId(int ID) {
        return repository.findByPostId(ID);
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#DTO.postId()")})
    public Comment save(CreateCommentDTO DTO) {
        userRepository.findById(DTO.userId()).orElseThrow(() -> new EntityNotFoundException("User not found: " + DTO.userId()));
        postRepository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        return repository.save(DTO.toEntity());
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#DTO.postId()")})
    public Comment update(UpdateCommentDTO DTO) {
        Comment comment = repository.findById(DTO.id()).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + DTO.id()));
        User user = userRepository.findById(DTO.userId()).orElseThrow(() -> new EntityNotFoundException("User not found: " + DTO.userId()));
        Post post = postRepository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        if (!Objects.equals(comment.getUserId(), user.getId())) throw new EntityNotFoundException("User does not own this comment: " + DTO.id());
        if (!Objects.equals(comment.getPostId(), post.getId())) throw new EntityNotFoundException("Comment does not belong to this post: " + DTO.id());
        return repository.save(DTO.toEntity());
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", allEntries = true)})
    public void delete(int ID) {
        repository.findById(ID).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + ID));
        repository.deleteById(ID);
    }
}
