package com.blog.Service;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.CommentDataAccessor;
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
    CommentDataAccessor dataAccessor;

    public CommentService(CommentDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }
    @Cacheable(cacheNames = "commentsForPost", key = "#ID")
    public List<Comment> getForPost(long ID) throws DataAccessException {
        return dataAccessor.getForPost(ID);
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "commentsForPost", key = "#dto.postId()")
    })
    public Comment save(CreateCommentDTO dto) throws DataAccessException {
        return dataAccessor.save(dto);
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "commentsForPost", key = "#dto.postId()")
    })
    public Comment update(UpdateCommentDTO dto) throws DataAccessException {
        return dataAccessor.update(dto);
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "commentsForPost", allEntries = true)
    })
    public void delete(String ID) throws DataAccessException {
        dataAccessor.delete(ID);
    }
}
