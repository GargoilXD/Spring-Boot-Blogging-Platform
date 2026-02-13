package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response data for a comment. Contains complete comment information including associations.")
public record ResponseCommentDTO(
    @Schema(description = "Unique identifier of the comment. Auto-generated upon creation.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Comment ID is required") @Min(1)
    Integer id,
    @Schema(description = "ID of the user who created the comment. References the user account.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required") @Min(1)
    Integer userId,
    @Schema(description = "ID of the post being commented on. References the blog post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Post ID is required") @Min(1)
    Integer postId,
    @Schema(description = "Content of the comment. The actual text of the comment.", example = "Great post! Very informative.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment body is required")
    String body
) {
    public ResponseCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getPostId(), comment.getBody());
    }
}

