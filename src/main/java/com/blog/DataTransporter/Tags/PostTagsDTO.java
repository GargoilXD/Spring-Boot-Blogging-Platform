package com.blog.DataTransporter.Tags;

import com.blog.Model.PostTags;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Data for managing tags on a post. Used for setting, adding, or removing tags from a blog post.")
public record PostTagsDTO(
        @Schema(description = "ID of the post to update. Must reference an existing post.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Post ID is required") @Min(1)
        Integer postId,
        @Schema(description = "List of tags for the post. Tags will be trimmed. Empty list is not allowed.", example = "[\"technology\", \"spring-boot\", \"tutorial\"]", requiredMode = Schema.RequiredMode.REQUIRED)
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
