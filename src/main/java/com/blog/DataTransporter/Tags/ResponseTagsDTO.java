package com.blog.DataTransporter.Tags;

import com.blog.Model.PostTags;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Response data for a tags")
public record ResponseTagsDTO(
        @Schema(description = "ID of the post to update", example = "1")
        @NotNull
        Integer PostID,
        @Schema(description = "Tags for the post", example = "['tag1', 'tag2']")
        @NotNull(message = "Tags are required")
        List<String> tags
) {
    public ResponseTagsDTO(PostTags postTags) {
        this(postTags.getPostId(), postTags.getTags());
    }
}

