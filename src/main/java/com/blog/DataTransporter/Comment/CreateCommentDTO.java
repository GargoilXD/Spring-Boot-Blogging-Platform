package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for creating a new comment")
public record CreateCommentDTO(
    @NotNull
    @Schema(description = "ID of the user creating the comment", example = "1", required = true)
    Long userId,
    @NotNull
    @Schema(description = "ID of the post being commented on", example = "1", required = true)
    Long postId,
    @NotBlank
    @Schema(description = "Username of the commenter", example = "johndoe", required = true)
    String username,
    @NotBlank
    @Schema(description = "Content of the comment", example = "Great post! Very informative.", required = true)
    String body
) {
    // public CreateCommentDTO(Comment comment) {
    //     this(comment.getUserId(), comment.getUsername(), comment.getPostId(), comment.getBody());
    // }
    // public Comment toEntity() {
    //     return new Comment(userId, postId, username, body);
    // }
}
