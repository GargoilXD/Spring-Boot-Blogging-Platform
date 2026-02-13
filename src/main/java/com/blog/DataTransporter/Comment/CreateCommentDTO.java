package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for creating a new comment")
public record CreateCommentDTO(
    @NotNull
    @Schema(description = "ID of the user creating the comment", example = "1")
    Integer userId,
    @NotNull
    @Schema(description = "ID of the post being commented on", example = "1")
    Integer postId,
    @NotBlank
    @Schema(description = "Content of the comment", example = "Great post! Very informative.")
    String body
) {
     public Comment toEntity() {
         return new Comment(null, userId, postId, body, null);
     }
}
