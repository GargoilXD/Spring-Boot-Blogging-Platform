package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentDTO(@NotNull Long userId, @NotBlank String username, @NotNull Long postId, @NotBlank String body) {
    // public CreateCommentDTO(Comment comment) {
    //     this(comment.getUserId(), comment.getUsername(), comment.getPostId(), comment.getBody());
    // }
    // public Comment toEntity() {
    //     return new Comment(userId, postId, username, body);
    // }
}
