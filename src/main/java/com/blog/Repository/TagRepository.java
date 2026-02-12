package com.blog.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashMap;
import java.util.List;

public interface TagRepository {// extends MongoRepository<String, String> {
    List<String> getAllTags();
    HashMap<Long, List<String>> getAllTagsByPosts();
    List<String> getTagsForPost(long PostID);
    void setPostTags(long postID, List<String> tags);
}
