package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing blog post")
public record UpdatePostDTO(
    @Schema(description = "ID of the post to update", example = "1")
    Long postId,
    @NotBlank
    @Schema(description = "Updated title of the blog post", example = "My Updated Blog Post", required = true)
    String title,
    @NotBlank
    @Schema(description = "Updated content body of the blog post", example = "This is the updated content...", required = true)
    String body,
    @NotNull
    @Schema(description = "Whether the post is a draft", example = "false", required = true)
    boolean draft
) {
    public UpdatePostDTO(Post post) {
        this(post.getId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(postId, null, null, title, body, draft, null);
    }
}