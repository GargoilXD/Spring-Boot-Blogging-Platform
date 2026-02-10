package com.blog.DataAccessor.MongoDB;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.blog.DataAccessor.Interface.TagDataAccessor;
import com.blog.Exception.DataAccessException;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;

@Primary
@Repository
public class MongoDBTagDataAccessor implements TagDataAccessor {
    private final MongoCollection<Document> collection;

    public MongoDBTagDataAccessor(
            MongoClient mongoClient,
            @Value("${spring.data.mongodb.database:blogdb_dev}") String databaseName,
            @Value("${mongodb.collection.post_tags:post_tags}") String collectionName) {

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
        collection.createIndex(Indexes.ascending("postId"), new IndexOptions().name("idx_postId").unique(true).background(true));
    }

    @Override
    public List<String> getAllTags() throws DataAccessException {
        try {
            Set<String> allTags = new HashSet<>();
            try (MongoCursor<Document> cursor = collection.find().cursor()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    List<String> tags = doc.getList("tags", String.class);
                    if (tags != null) {
                        allTags.addAll(tags);
                    }
                }
            }
            return new ArrayList<>(allTags);
        } catch (MongoException e) {
            throw new DataAccessException("Error getting all tags", e);
        }
    }

    @Override
    public HashMap<Long, List<String>> getAllTagsByPosts() throws DataAccessException {
        try {
            HashMap<Long, List<String>> tagsByPost = new HashMap<>();
            try (MongoCursor<Document> cursor = collection.find().cursor()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Long postId = doc.getLong("postId");
                    List<String> tags = doc.getList("tags", String.class);
                    if (postId != null && tags != null) {
                        tagsByPost.put(postId, new ArrayList<>(tags));
                    }
                }
            }
            return tagsByPost;
        } catch (MongoException e) {
            throw new DataAccessException("Error getting tags by post", e);
        }
    }

    @Override
    public List<String> getTagsForPost(long postId) throws DataAccessException {
        try {
            Document postTags = collection.find(Filters.eq("postId", postId)).first();
            if (postTags != null) {
                List<String> tags = postTags.getList("tags", String.class);
                if (tags != null) {
                    return new ArrayList<>(tags);
                }
            }
            return List.of();
        } catch (MongoException e) {
            throw new DataAccessException(
                    String.format("Error getting tags for post: %s", postId), e);
        }
    }

    @Override
    public void setPostTags(long postId, List<String> tags) throws DataAccessException {
        if (postId <= 0) {
            throw new IllegalArgumentException("postId must be positive");
        }
        if (tags == null) {
            throw new IllegalArgumentException("tags cannot be null");
        }
        List<String> normalizedTags = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();

        try {
            collection.updateOne(
                    Filters.eq("postId", postId),
                    Updates.set("tags", normalizedTags),
                    new UpdateOptions().upsert(true)
            );
        } catch (MongoException e) {
            throw new DataAccessException(
                    String.format("Error setting tags for post: %s", postId), e);
        }
    }
}