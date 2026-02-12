package com.blog.DataTransporter.Comment;

import com.blog.Model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.relational.core.sql.In;

@Schema(description = "Data for creating a new comment")
public record CreateCommentDTO(
    @NotNull
    @Schema(description = "ID of the user creating the comment", example = "1", required = true)
    Integer userId,
    @NotNull
    @Schema(description = "ID of the post being commented on", example = "1", required = true)
    Integer postId,
    @NotBlank
    @Schema(description = "Content of the comment", example = "Great post! Very informative.", required = true)
    String body
) {
    // public CreateCommentDTO(Comment comment) {
    //     this(comment.getUserId(), comment.getUsername(), comment.getPostId(), comment.getBody());
    // }
     public Comment toEntity() {
         return new Comment(userId, postId, null, body);
     }
}
