package com.blog.DataTransporter.Tags;

import com.blog.Model.PostTags;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Response data for tags associated with a post. Contains the post ID and its assigned tags.")
public record ResponseTagsDTO(
        @Schema(description = "ID of the post. References the blog post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Integer PostID,
        @Schema(description = "List of tags for the post. Tags are used for categorization and organization.", example = "[\"technology\", \"spring-boot\", \"tutorial\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Tags are required")
        List<String> tags
) {
    public ResponseTagsDTO(PostTags postTags) {
        this(postTags.getPostId(), postTags.getTags());
    }
}

