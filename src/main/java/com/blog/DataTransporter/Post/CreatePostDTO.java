package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for creating a new blog post. All fields are required. The post will be assigned a unique ID and creation timestamp automatically.")
public record CreatePostDTO(
    @Schema(description = "ID of the post author. Must reference an existing user in the system.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required") @Min(1)
    Integer userId,
    @Schema(description = "Title of the blog post. Will be trimmed. Should be concise and descriptive.", example = "Getting Started with Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    String title,
    @Schema(description = "Content body of the blog post. Can include markdown or plain text. Will be trimmed.", example = "This is the content of my first blog post...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Body is required")
    String body,
    @Schema(description = "Whether the post is a draft. Draft posts are not visible to the public.", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Draft status is required")
    Boolean draft
) {
    public CreatePostDTO {
        title = title.trim();
        body = body.trim();
    }
    public CreatePostDTO(Post post) {
        this(post.getUserId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(null, userId, title, body, draft, null);
    }
}