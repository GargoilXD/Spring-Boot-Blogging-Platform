package com.blog.DataAccessor.Fake;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.blog.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.TagDataAccessor;

@Repository
public class FakeTagDataAccessor implements TagDataAccessor {

    @Override
    public List<String> getAllTags() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'getAllTags'");
    }

    @Override
    public HashMap<Long, List<String>> getAllTagsByPosts() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'getAllTagsByPosts'");
    }

    @Override
    public List<String> getTagsForPost(long PostID) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'getTagsForPost'");
    }

    @Override
    public void setPostTags(long postID, List<String> tags) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new DataAccessException("Unimplemented method 'setPostTags'");
    }
    
}
