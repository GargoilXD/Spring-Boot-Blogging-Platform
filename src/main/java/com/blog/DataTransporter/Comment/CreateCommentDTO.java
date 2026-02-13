package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for creating a new comment. All fields are required. The comment will be associated with the specified user and post.")
public record CreateCommentDTO(
    @Schema(description = "ID of the user creating the comment. Must reference an existing user.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required") @Min(1)
    Integer userId,
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
    public Comment toEntity() {
        return new Comment(null, userId, postId, body, null);
    }
}
