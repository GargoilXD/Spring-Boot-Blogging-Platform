package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response data for a blog post")
public record ResponsePostDTO(
    @NotNull(message = "Post ID is required")
    @Schema(description = "Unique identifier of the post", example = "1")
    Integer postId,
    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the post author", example = "1")
    Integer userId,
    @NotBlank(message = "Title is required")
    @Schema(description = "Title of the blog post", example = "My First Blog Post")
    String title,
    @NotBlank(message = "Body is required")
    @Schema(description = "Content body of the blog post", example = "This is the content of my first blog post...")
    String body,
    @NotNull(message = "Draft status is required")
    @Schema(description = "Whether the post is a draft", example = "false")
    boolean draft,
    @NotNull(message = "Created at is required")
    @Schema(description = "Date and time when the post was created", example = "2023-07-25T12:00:00Z")
    String createdAt
) {
    public ResponsePostDTO(Post post) {
        this(post.getId(), post.getUserId(), post.getTitle(), post.getBody(), post.isDraft(), post.getCreatedAt().toString());
    }
}