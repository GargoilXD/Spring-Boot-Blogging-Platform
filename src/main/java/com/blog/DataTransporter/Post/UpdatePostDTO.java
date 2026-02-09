package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePostDTO(Long postId, @NotBlank String title, @NotBlank String body, @NotNull boolean draft) {
    public UpdatePostDTO(Post post) {
        this(post.getId(), post.getTitle(), post.getBody(), post.isDraft());
    }
    public Post toEntity() {
        return new Post(postId, null, null, title, body, draft, null);
    }
}