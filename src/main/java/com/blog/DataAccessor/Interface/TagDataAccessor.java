package com.blog.DataAccessor.Interface;

import com.blog.DataAccessor.Exception.DataAccessException;

import java.util.HashMap;
import java.util.List;

public interface TagDataAccessor {
    List<String> getAllTags() throws DataAccessException;
    HashMap<Long, List<String>> getAllTagsByPosts() throws DataAccessException;
    List<String> getTagsForPost(long PostID) throws DataAccessException;
    void setPostTags(long postID, List<String> tags) throws DataAccessException;
}
