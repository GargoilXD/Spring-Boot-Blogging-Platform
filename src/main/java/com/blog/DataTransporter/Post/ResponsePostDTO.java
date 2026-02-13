package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response data for a blog post. Contains complete post information including metadata.")
public record ResponsePostDTO(
    @Schema(description = "Unique identifier of the post. Auto-generated upon creation.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Post ID is required")
    Integer postId,
    @Schema(description = "ID of the post author. References the user who created the post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    Integer userId,
    @Schema(description = "Title of the blog post.", example = "Getting Started with Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    String title,
    @Schema(description = "Content body of the blog post. May contain markdown or plain text.", example = "This is the content of my first blog post...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Body is required")
    String body,
    @Schema(description = "Whether the post is a draft. Draft posts are not publicly visible.", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Draft status is required")
    boolean draft,
    @Schema(description = "Date and time when the post was created. ISO 8601 format.", example = "2023-07-25T12:00:00Z", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Created at is required")
    String createdAt
) {
    public ResponsePostDTO(Post post) {
        this(post.getId(), post.getUserId(), post.getTitle(), post.getBody(), post.isDraft(), post.getCreatedAt().toString());
    }
}