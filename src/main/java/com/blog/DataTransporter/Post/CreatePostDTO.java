package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Data for creating a new blog post")
public record CreatePostDTO(
    @Schema(description = "ID of the post author", example = "1")
    @NotNull @Min(1)
    Integer userId,
    @Schema(description = "Title of the blog post", example = "My First Blog Post")
    @NotBlank
    String title,
    @Schema(description = "Content body of the blog post", example = "This is the content of my first blog post...")
    @NotBlank
    String body,
    @Schema(description = "Whether the post is a draft", example = "false")
    @NotNull
    Boolean draft
) {
    public CreatePostDTO(Post post) {
        this(post.getUserId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(null, userId, title, body, draft, null);
    }
}