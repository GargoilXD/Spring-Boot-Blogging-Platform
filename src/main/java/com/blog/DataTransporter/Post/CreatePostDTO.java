package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Data for creating a new blog post")
public record CreatePostDTO(
    @NotNull
    @Schema(description = "ID of the post author", example = "1", required = true)
    Integer authorId,
    @NotBlank
    @Schema(description = "Title of the blog post", example = "My First Blog Post", required = true)
    String title,
    @NotBlank
    @Schema(description = "Content body of the blog post", example = "This is the content of my first blog post...", required = true)
    String body,
    @NotNull
    @Schema(description = "Whether the post is a draft", example = "false", required = true)
    boolean draft
) {
    public CreatePostDTO(Post post) {
        this(post.getUserId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(null, authorId, title, body, draft);
    }
    public Post toEntity(Integer Id, LocalDateTime createdAt) {
        return new Post(Id, authorId, title, body, draft);
    }
}