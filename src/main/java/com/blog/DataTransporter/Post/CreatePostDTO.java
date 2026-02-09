package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreatePostDTO(
    @NotNull Long authorId,
    @NotBlank String title,
    @NotBlank String body,
    @NotNull boolean draft
) {
    public CreatePostDTO(Post post) {
        this(post.getUserId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(null, authorId, null, title, body, draft, null);
    }
    public Post toEntity(Long Id, String username, LocalDateTime createdAt) {
        return new Post(Id, authorId, username, title, body, draft, null);
    }
}