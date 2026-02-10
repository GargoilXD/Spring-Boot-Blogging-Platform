package com.blog.DataAccessor.MongoDB;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataAccessor.Interface.CommentDataAccessor;
import com.blog.Exception.DataAccessException;
import com.blog.Model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Primary
@Repository
public class MongoDBCommentDataAccessor implements CommentDataAccessor {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoDBCommentDataAccessor(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Comment> getForPost(long postId) throws DataAccessException {
        try {
            // Try querying with both Long object and primitive long to handle type mismatches
            Query query = new Query();
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("postId").is(postId),
                    Criteria.where("postId").is(Long.valueOf(postId))
            ));
            query.limit(500);

            List<Comment> results = mongoTemplate.find(query, Comment.class, "comments");

            // Debug logging
            System.out.println("Query for postId: " + postId + ", found: " + results.size() + " comments");

            return results;
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error getting comments for post: %s", postId), e);
        }
    }

    @Override
    public Comment save(CreateCommentDTO dto) throws DataAccessException {
        try {
            Comment comment = new Comment();
            // Let MongoDB generate the ID (ObjectId) automatically
            // No need to set it manually — Spring Data will handle it if field is String + @Id
            comment.setUserId(dto.userId());
            comment.setPostId(dto.postId());
            comment.setUsername(dto.username());
            comment.setBody(dto.body());
            // createdAt is set in Comment constructor when using 4-arg constructor,
            // but here we're using setters → so set it explicitly or adjust constructor
            comment.setCreatedAt(java.time.Instant.now());

            return mongoTemplate.save(comment); // Saves and returns saved entity (with ID populated)
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error saving comment by user: %s", dto.username()), e);
        }
    }

    @Override
    public Comment update(UpdateCommentDTO dto) throws DataAccessException {
        try {
            Query query = query(where("_id").is(dto.id()));
            Update update = new Update().set("body", dto.body());

            Comment updatedComment = mongoTemplate.findAndModify(
                    query,
                    update,
                    new org.springframework.data.mongodb.core.FindAndModifyOptions().returnNew(true),
                    Comment.class
            );

            if (updatedComment == null) {
                throw new DataAccessException("Comment not found or no changes made");
            }

            return updatedComment;
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error updating comment by ID: %s", dto.id()), e);
        }
    }

    @Override
    public void delete(String id) throws DataAccessException {
        try {
            Query query = query(where("_id").is(id));
            mongoTemplate.remove(query, Comment.class);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error deleting comment by ID: %s", id), e);
        }
    }

    @Override
    public void deleteForPost(long postId) throws DataAccessException {
        try {
            Query query = query(where("postId").is(postId));
            mongoTemplate.remove(query, Comment.class);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error deleting comments for post: %s", postId), e);
        }
    }
}