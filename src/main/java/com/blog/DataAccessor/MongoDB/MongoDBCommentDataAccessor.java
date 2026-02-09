package com.blog.DataAccessor.MongoDB;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.blog.DataAccessor.Interface.CommentDataAccessor;
import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.Model.Comment;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MongoDBCommentDataAccessor implements CommentDataAccessor {
    private final MongoCollection<Comment> collection;

    public MongoDBCommentDataAccessor(MongoClient mongoClient, @Value("${spring.data.mongodb.database:blogdb_dev}") String databaseName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection("comments", Comment.class);
        collection.createIndex(Indexes.ascending("postId"), new IndexOptions().name("idx_postId").background(true));
    }
    private ObjectId toObjectId(String id, String context) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Invalid " + context + " ID format: " + id, e);
        }
    }
    @Override
    public List<Comment> getForPost(long postId) throws DataAccessException {
        try {
            List<Comment> comments = new ArrayList<>();
            collection.find(Filters.eq("postId", postId)).limit(500).into(comments);
            return comments;
        } catch (MongoException e) {
            throw new DataAccessException(String.format("Error getting comments for post: %s", postId), e);
        }
    }
    @Override
    public Comment save(CreateCommentDTO dto) throws DataAccessException {
        try {
            Comment comment = new Comment();
            comment.setId(new ObjectId().toHexString());
            comment.setUserId(dto.userId());
            comment.setPostId(dto.postId());
            comment.setUsername(dto.username());
            comment.setBody(dto.body());
            comment.setCreatedAt(Instant.now());
            collection.insertOne(comment);
            return comment;
        } catch (MongoException e) {
            throw new DataAccessException(String.format("Error saving comment by user: %s", dto.username()), e);
        }
    }
    @Override
    public Comment update(UpdateCommentDTO dto) throws DataAccessException {
        try {
            ObjectId objectId = toObjectId(dto.id(), "comment");
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", objectId),
                    Updates.set("body", dto.body())
            );
            if (result.getModifiedCount() == 0) {
                throw new DataAccessException("Comment not found or no changes made");
            }
            return dto.toEntity();
        } catch (MongoException e) {
            throw new DataAccessException(String.format("Error updating comment by ID: %s", dto.id()), e);
        }
    }
    @Override
    public void delete(String id) throws DataAccessException {
        try {
            ObjectId objectId = toObjectId(id, "comment");
            collection.deleteOne(Filters.eq("_id", objectId));
        } catch (MongoException e) {
            throw new DataAccessException(String.format("Error deleting comment by ID: %s", id), e);
        }
    }
    @Override
    public void deleteForPost(long postId) throws DataAccessException {
        try {
            collection.deleteMany(Filters.eq("postId", postId));
        } catch (MongoException e) {
            throw new DataAccessException(String.format("Error deleting comments for post: %s", postId), e);
        }
    }
}