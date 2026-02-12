package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing comment")
public record UpdateCommentDTO(
    @NotNull
    @Schema(description = "ID of the comment to update", example = "507f1f77bcf86cd799439011", required = true)
    String id,
    @NotNull
    @Schema(description = "ID of the user who created the comment", example = "1", required = true)
    Integer userId,
    @NotBlank
    @Schema(description = "Username of the commenter", example = "johndoe", required = true)
    String username,
    @NotNull
    @Schema(description = "ID of the post being commented on", example = "1", required = true)
    Integer postId,
    @NotBlank
    @Schema(description = "Updated content of the comment", example = "Updated comment text...", required = true)
    String body
) {
    public UpdateCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getUsername(), comment.getPostId(), comment.getBody());
    }
    public Comment toEntity() {
        return new Comment(id, userId, postId, username, body, null);
    }
}
