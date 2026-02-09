package com.blog.Service;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.TagDataAccessor;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TagService {
    TagDataAccessor dataAccessor;

    public TagService(TagDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }
    public List<String> getAllTags() throws DataAccessException {
        return dataAccessor.getAllTags();
    }
    public HashMap<Long, List<String>> getAllTagsByPosts() throws DataAccessException {
        return dataAccessor.getAllTagsByPosts();
    }
    public List<String> getForPost(long ID) throws DataAccessException {
        return dataAccessor.getTagsForPost(ID);
    }
    public List<String> getTagsForPost(long PostID) throws DataAccessException {
        return dataAccessor.getTagsForPost(PostID);
    }
    public void setPostTags(long PostID, List<String> tags) throws DataAccessException {
        dataAccessor.setPostTags(PostID, tags);
    }
}
