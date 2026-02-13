package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing comment")
public record UpdateCommentDTO(
    @NotNull @Min(1)
    @Schema(description = "ID of the comment to update", example = "1")
    Integer id,
    @NotNull @Min(1)
    @Schema(description = "ID of the user who created the comment", example = "1")
    Integer userId,
    @NotNull
    @Schema(description = "ID of the post being commented on", example = "1")
    Integer postId,
    @NotBlank
    @Schema(description = "Updated content of the comment", example = "Updated comment text...")
    String body
) {
    public UpdateCommentDTO(Comment comment) {
        this(comment.getId(), comment.getUserId(), comment.getPostId(), comment.getBody());
    }
    public Comment toEntity() {
        return new Comment(id, userId, postId, body, null);
    }
}
