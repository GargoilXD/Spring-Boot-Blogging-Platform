package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response data for a blog post")
public record ResponsePostDTO(
    @NotNull
    @Schema(description = "Unique identifier of the post", example = "1")
    Integer postId,
    @NotNull
    @Schema(description = "ID of the post author", example = "1")
    Integer authorId,
    @NotBlank
    @Schema(description = "Title of the blog post", example = "My First Blog Post")
    String title,
    @NotNull
    @Schema(description = "Whether the post is a draft", example = "false")
    boolean draft
) {
    public ResponsePostDTO(Post post) {
        this(post.getId(), post.getUserId(), post.getTitle(), post.isDraft());
    }
}