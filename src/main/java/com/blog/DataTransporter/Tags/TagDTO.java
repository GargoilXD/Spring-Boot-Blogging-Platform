package com.blog.DataTransporter.Tags;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TagDTO(
        @NotEmpty(message = "Tags are required")
        List<String> tags
) {
    public TagDTO {
        tags = tags.stream().map(String::trim).toList();
    }
}
