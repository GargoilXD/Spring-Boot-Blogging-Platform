package com.blog.Service;

import com.blog.Repository.CommentRepository;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
public class CommentService {
    CommentRepository repository;

    public CommentService(CommentRepository repository) {
        this.repository = repository;
    }
    @Cacheable(cacheNames = "Comment.findByPostId", key = "#ID")
    public List<Comment> findByPostId(int ID) {
        return repository.findByPostId(ID);
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#DTO.postId()")})
    public Comment save(CreateCommentDTO DTO) {
        return repository.save(DTO.toEntity());
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#DTO.postId()")})
    public Comment update(UpdateCommentDTO DTO) {
        repository.findById(DTO.id()).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + DTO.id()));
        return repository.save(DTO.toEntity());
    }
    @Caching(evict = {@CacheEvict(cacheNames = "Comment.findByPostId", key = "#ID")})
    public void delete(int ID) {
        repository.findById(ID).orElseThrow(() -> new EntityNotFoundException("Comment not found: " + ID));
        repository.deleteById(ID);
    }
}
