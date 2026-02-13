package com.blog.Repository;

import com.blog.Model.PostTags;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TagRepository extends MongoRepository<PostTags, String> {
    Optional<PostTags> findByPostId(int postId);
    void deleteByPostId(int postId);
}
