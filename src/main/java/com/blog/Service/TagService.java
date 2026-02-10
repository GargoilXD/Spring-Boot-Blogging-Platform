package com.blog.Service;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.TagDataAccessor;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
public class TagService {
    TagDataAccessor dataAccessor;

    public TagService(TagDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }
    @Cacheable(cacheNames = "tags")
    public List<String> getAllTags() throws DataAccessException {
        return dataAccessor.getAllTags();
    }
    @Cacheable(cacheNames = "tagsByPosts")
    public HashMap<Long, List<String>> getAllTagsByPosts() throws DataAccessException {
        return dataAccessor.getAllTagsByPosts();
    }
    @Cacheable(cacheNames = "tagsForPost", key = "#ID")
    public List<String> getForPost(long ID) throws DataAccessException {
        return dataAccessor.getTagsForPost(ID);
    }
    @Cacheable(cacheNames = "tagsForPost", key = "#PostID")
    public List<String> getTagsForPost(long PostID) throws DataAccessException {
        return dataAccessor.getTagsForPost(PostID);
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "tagsForPost", key = "#PostID"),
        @CacheEvict(cacheNames = "tagsByPosts", allEntries = true),
        @CacheEvict(cacheNames = "tags", allEntries = true)
    })
    public void setPostTags(long PostID, List<String> tags) throws DataAccessException {
        dataAccessor.setPostTags(PostID, tags);
    }
}
