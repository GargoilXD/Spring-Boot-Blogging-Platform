package com.blog.DataTransporter.Post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for creating a new blog post. All fields are required. The post will be assigned a unique ID and creation timestamp automatically. The author is derived from the authenticated JWT token.")
public record CreatePostDTO(
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
}