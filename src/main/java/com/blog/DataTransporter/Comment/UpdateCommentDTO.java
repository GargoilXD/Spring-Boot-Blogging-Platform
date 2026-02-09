package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCommentDTO(@NotNull String id, @NotNull Long userId, @NotBlank String username, @NotNull Long postId, @NotBlank String body) {
    public UpdateCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getUsername(), comment.getPostId(), comment.getBody());
    }
    public Comment toEntity() {
        return new Comment(id, userId, postId, username, body, null);
    }
}
