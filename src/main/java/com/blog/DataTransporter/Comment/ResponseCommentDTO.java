package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResponseCommentDTO(@NotNull String id, @NotNull Long userId, @NotBlank String username, @NotNull Long postId, @NotBlank String body) {
    public ResponseCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getUsername(), comment.getPostId(), comment.getBody());
    }
    public Comment toEntity() {
        throw new UnsupportedOperationException();
    }
}

