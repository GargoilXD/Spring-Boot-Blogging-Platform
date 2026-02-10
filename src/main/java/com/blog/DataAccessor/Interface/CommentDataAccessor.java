package com.blog.DataAccessor.Interface;

import com.blog.Exception.DataAccessException;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

import java.util.List;

public interface CommentDataAccessor {
    List<Comment> getForPost(long PostID) throws DataAccessException;
    Comment save(CreateCommentDTO dto) throws DataAccessException;
    Comment update(UpdateCommentDTO dto) throws DataAccessException;
    void delete(String ID) throws DataAccessException;
    void deleteForPost(long PostID) throws DataAccessException;
}
