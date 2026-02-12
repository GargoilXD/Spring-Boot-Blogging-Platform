package com.blog.DataTransporter.Post;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class GetPostDTO {
    @NotNull @Min(0) final int page;
    @NotNull @Min(1) final int size;
    @NotBlank
    String sortBy = "posts.created_at";
    @NotBlank
    String direction = "DESC";


    public void validate() {
        if (sortBy == null || sortBy.trim().isEmpty()) throw new IllegalArgumentException("Sort By cannot be empty");
        switch (Objects.requireNonNull(sortBy).toLowerCase().trim()) {
            case "id":
            case "user_id":
            case "title":
            case "body":
            case "created_at":
            case "is_draft":
            case "username":
                break;
            default:
                throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Allowed fields: id, user_id, title, body, created_at, is_draft");
        }
        if(direction == null || !(direction.equalsIgnoreCase("ASC") || direction.equalsIgnoreCase("DESC"))) throw new IllegalArgumentException("Invalid sort direction: " + direction + ". Must be ASC or DESC");
    }
    public String getFullColumnName() {
        return switch (sortBy.toLowerCase()) {
            case "id" -> "posts.id";
            case "user_id" -> "posts.user_id";
            case "title" -> "posts.title";
            case "body" -> "posts.body";
            case "created_at" -> "posts.created_at";
            case "is_draft" -> "posts.is_draft";
            case "username" -> "users.username";
            default -> "posts." + sortBy;
        };
    }
}