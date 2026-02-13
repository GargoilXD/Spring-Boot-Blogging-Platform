package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response data for a comment")
public record ResponseCommentDTO(
    @NotNull(message = "Comment ID is required") @Min(1)
    @Schema(description = "Unique identifier of the comment", example = "1")
    Integer id,
    @NotNull(message = "User ID is required") @Min(1)
    @Schema(description = "ID of the user who created the comment", example = "1")
    Integer userId,
    @NotNull(message = "Post ID is required") @Min(1)
    @Schema(description = "ID of the post being commented on", example = "1")
    Integer postId,
    @NotBlank(message = "Comment body is required")
    @Schema(description = "Content of the comment", example = "Great post! Very informative.")
    String body
) {
    public ResponseCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getPostId(), comment.getBody());
    }
}

