package com.blog.DataTransporter.Comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for creating a new comment. All fields are required. The comment author is derived from the authenticated JWT token.")
public record CreateCommentDTO(
    @Schema(description = "ID of the post being commented on. Must reference an existing post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Post ID is required") @Min(1)
    Integer postId,
    @Schema(description = "Content of the comment. Will be trimmed. Should be relevant to the post.", example = "Great post! Very informative.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment body is required")
    String body
) {
    public CreateCommentDTO {
        body = body.trim();
    }
}
