package com.blog.Service;

import com.blog.Exception.DataAccessException;
import com.blog.Repository.CommentRepository;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

import java.util.List;

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
    @Cacheable(cacheNames = "commentsForPost", key = "#ID")
    public List<Comment> getForPost(long ID) throws DataAccessException {
        return repository.findByPostId(ID);
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "commentsForPost", key = "#dto.postId()")
    })
    public Comment save(CreateCommentDTO dto) throws DataAccessException {
        return repository.save(dto.toEntity());
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "commentsForPost", key = "#dto.postId()")
    })
    public Comment update(UpdateCommentDTO dto) throws DataAccessException {
        // Verify ID
        return repository.save(dto.toEntity());
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "commentsForPost", allEntries = true)
    })
    public void delete(String ID) throws DataAccessException {
        repository.deleteById(ID);
    }
}
