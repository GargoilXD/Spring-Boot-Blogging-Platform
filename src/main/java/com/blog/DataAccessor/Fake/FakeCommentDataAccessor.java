package com.blog.DataAccessor.Fake;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.blog.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.CommentDataAccessor;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Model.Comment;

@Repository
public class FakeCommentDataAccessor implements CommentDataAccessor {

    @Override
    public List<Comment> getForPost(long PostID) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'getForPost'");
    }

    @Override
    public Comment save(CreateCommentDTO dto) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'save'");
    }

    @Override
    public Comment update(UpdateCommentDTO dto) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'update'");
    }

    @Override
    public void delete(String ID) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'delete'");
    }

    @Override
    public void deleteForPost(long PostID) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'deleteForPost'");
    }
    
}
