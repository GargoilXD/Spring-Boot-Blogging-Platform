package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for updating an existing blog post")
public record UpdatePostDTO(
    @Schema(description = "ID of the post to update", example = "1")
    @NotNull @Min(1)
    Integer postId,
    @NotNull @Min(1)
    @Schema(description = "ID of the user to update", example = "1")
    Integer userId,
    @NotBlank
    @Schema(description = "Updated title of the blog post", example = "My Updated Blog Post")
    String title,
    @NotBlank
    @Schema(description = "Updated content body of the blog post", example = "This is the updated content...")
    String body,
    @NotNull
    @Schema(description = "Whether the post is a draft", example = "false")
    Boolean draft
) {
    public UpdatePostDTO(Post post) {
        this(post.getId(), post.getUserId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(postId, userId, title, body, draft, null);
    }
}