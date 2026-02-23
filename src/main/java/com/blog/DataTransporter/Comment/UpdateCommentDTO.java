package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing comment. All fields are required. The comment ID must exist in the system. Ownership is verified against the authenticated user's JWT token.")
public record UpdateCommentDTO(
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
    public void update(Comment comment) {
        comment.setBody(body);
    }
}
