package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing comment. All fields are required. The comment ID must exist in the system.")
public record UpdateCommentDTO(
    @Schema(description = "ID of the comment to update. Must reference an existing comment.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Comment ID is required") @Min(1)
    Integer id,
    @Schema(description = "ID of the user who created the comment. Must match the original comment author.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required") @Min(1)
    Integer userId,
    @Schema(description = "ID of the post being commented on. Must match the original post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Post ID is required") @Min(1)
    Integer postId,
    @Schema(description = "Updated content of the comment. Will be trimmed.", example = "Updated comment text with corrections...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment body is required")
    String body
) {
    public UpdateCommentDTO {
        body = body.trim();
    }
    public UpdateCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getPostId(), comment.getBody());
    }
    public Comment toEntity() {
        return new Comment(id, userId, postId, body, null);
    }
}
