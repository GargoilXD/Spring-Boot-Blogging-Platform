package com.blog.DataTransporter;

import java.time.LocalDateTime;

import com.blog.Model.Post;

public record PostDataTransporter(
        Long id,
        Long userId,
        String username,
        String title,
        String body,
        Boolean draft,
        LocalDateTime createdAt
) {
    public static PostDataTransporter fromEntity(Post post) {
        return new PostDataTransporter(
                post.getId(),
                post.getUserId(),
                post.getUsername(),
                post.getTitle(),
                post.getBody(),
                post.isDraft(),
                post.getCreatedAt()
        );
    }

    public Post toEntity() {
        return new Post(
                id != null ? id : 0L,
                userId != null ? userId : 0L,
                username,
                title,
                body,
                draft != null ? draft : false,
                createdAt
        );
    }
}