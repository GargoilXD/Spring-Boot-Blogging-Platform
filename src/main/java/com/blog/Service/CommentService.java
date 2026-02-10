package com.blog.Service;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.CommentDataAccessor;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CommentService {
    CommentDataAccessor dataAccessor;

    public CommentService(CommentDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }
    public List<Comment> getForPost(long ID) throws DataAccessException {
        return dataAccessor.getForPost(ID);
    }
    public Comment save(CreateCommentDTO dto) throws DataAccessException {
        return dataAccessor.save(dto);
    }
    public Comment update(UpdateCommentDTO dto) throws DataAccessException {
        return dataAccessor.update(dto);
    }
    public void delete(String ID) throws DataAccessException {
        dataAccessor.delete(ID);
    }
}
