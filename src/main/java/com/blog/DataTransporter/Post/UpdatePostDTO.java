package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing blog post. All fields are required. The post ID must exist in the system.")
public record UpdatePostDTO(
    @Schema(description = "ID of the post to update. Must reference an existing post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Post ID is required") @Min(1)
    Integer postId,
    @Schema(description = "ID of the user who owns the post. Must match the original post author.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required") @Min(1)
    Integer userId,
    @Schema(description = "Updated title of the blog post. Will be trimmed.", example = "Getting Started with Spring Boot - Updated", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    String title,
    @Schema(description = "Updated content body of the blog post. Will be trimmed.", example = "This is the updated content of the blog post...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Body is required")
    String body,
    @Schema(description = "Whether the post is a draft. Can be changed to publish or unpublish the post.", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Draft status is required")
    Boolean draft
) {
    public UpdatePostDTO {
        title = title.trim();
        body = body.trim();
    }
    public UpdatePostDTO(Post post) {
        this(post.getId(), post.getUserId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(postId, userId, title, body, draft, null);
    }
}