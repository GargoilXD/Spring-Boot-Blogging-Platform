package com.blog.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
// @Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    @Field("user_id")
    private long userId;

    @Field("post_id")
    private long postId;

    @Field("username")
    private String username;

    @Field("body")
    private String body;

    @Field("created_at")
    private Instant createdAt;

    public Comment(long userId, long postId, String username, String body) {
        this.userId = userId;
        this.postId = postId;
        this.username = username;
        this.body = body;
        this.createdAt = Instant.now();
        validate();
    }

    public void validate() {
        if (userId <= 0 || postId <= 0 || username == null || username.isEmpty() || body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Invalid comment data");
        }
    }
}