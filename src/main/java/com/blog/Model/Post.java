package com.blog.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public final class Post {
    private final long id;
    private final long userId;
    private final String username;
    private final String title;
    private final String body;
    private final boolean draft;
    private final LocalDateTime createdAt;

    public static Post fromResultSet(ResultSet rs) throws SQLException {
        return new Post(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("username").trim(),
                rs.getString("title").trim(),
                rs.getString("body").trim(),
                rs.getBoolean("is_draft"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public static void validate(Post post) {
        if (post.title == null || post.title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (post.body == null || post.body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty");
        }
        if (post.username == null || post.username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}