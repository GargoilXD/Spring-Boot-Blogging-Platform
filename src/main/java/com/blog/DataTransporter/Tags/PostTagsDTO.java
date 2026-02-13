package com.blog.DataTransporter.Tags;

import com.blog.Model.PostTags;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostTagsDTO(
        @Schema(description = "ID of the post to update", example = "1")
        @NotNull
        Integer postId,
        @Schema(description = "ID of the post to update", example = "1")
        @NotEmpty
        List<String> tags
) {
        public PostTags toEntity() {
                return new PostTags(null, postId, tags);
        }
}
