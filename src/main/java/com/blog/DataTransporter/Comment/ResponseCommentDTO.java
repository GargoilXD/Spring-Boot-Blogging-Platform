package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response data for a comment")
public record ResponseCommentDTO(
    @NotNull
    @Schema(description = "Unique identifier of the comment", example = "1")
    Integer id,
    @NotNull
    @Schema(description = "ID of the user who created the comment", example = "1")
    Integer userId,
    @NotNull
    @Schema(description = "ID of the post being commented on", example = "1")
    Integer postId,
    @NotBlank
    @Schema(description = "Content of the comment", example = "Great post! Very informative.")
    String body
) {
    public ResponseCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getPostId(), comment.getBody());
    }
}

