package com.blog.DataTransporter.Post;

import com.blog.Model.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResponsePostDTO(@NotNull Long postId, @NotNull Long authorId, @NotBlank String title, @NotNull boolean draft) {
    public ResponsePostDTO(Post post) {
        this(post.getId(), post.getUserId(), post.getTitle(), post.isDraft());
    }
    public Post toEntity() {
        throw new UnsupportedOperationException();
    }
}