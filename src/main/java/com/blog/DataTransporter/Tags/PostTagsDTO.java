package com.blog.DataTransporter.Tags;

import com.blog.Model.PostTags;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostTagsDTO(
        @Schema(description = "ID of the post to update", example = "1")
        @NotNull(message = "Post ID is required") @Min(1)
        Integer postId,
        @Schema(description = "Tags for the post", example = "['tag1', 'tag2']")
        @NotEmpty(message = "Tags are required")
        List<String> tags
) {
        public PostTagsDTO {
                tags = tags.stream().map(String::trim).toList();
        }
        public PostTags toEntity() {
                return new PostTags(null, postId, tags);
        }
}
