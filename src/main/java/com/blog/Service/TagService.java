package com.blog.Service;

import com.blog.Exception.DataAccessException;
import com.blog.Repository.TagRepository;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

//@Service
public class TagService {
    TagRepository repository;

    public TagService(TagRepository repository) {
        this.repository = repository;
    }
    @Cacheable(cacheNames = "tags")
    public List<String> getAllTags() throws DataAccessException {
        return repository.getAllTags();
    }
    @Cacheable(cacheNames = "tagsByPosts")
    public HashMap<Long, List<String>> getAllTagsByPosts() throws DataAccessException {
        return new HashMap<>();//repository.findAllById();
    }
    @Cacheable(cacheNames = "tagsForPost", key = "#ID")
    public List<String> getForPost(long ID) throws DataAccessException {
        throw new UnsupportedOperationException("getForPost");
//        return repository.getTagsForPost(ID);
    }
    @Cacheable(cacheNames = "tagsForPost", key = "#PostID")
    public List<String> getTagsForPost(long PostID) throws DataAccessException {
        throw new UnsupportedOperationException("getForPost");
//        return repository.getTagsForPost(PostID);
    }
    @Caching(evict = {
        @CacheEvict(cacheNames = "tagsForPost", key = "#PostID"),
        @CacheEvict(cacheNames = "tagsByPosts", allEntries = true),
        @CacheEvict(cacheNames = "tags", allEntries = true)
    })
    public void setPostTags(long PostID, List<String> tags) throws DataAccessException {
        throw new UnsupportedOperationException("getForPost");
//        repository.setPostTags(PostID, tags);
    }
}
