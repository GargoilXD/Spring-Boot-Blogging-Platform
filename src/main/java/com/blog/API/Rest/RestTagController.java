package com.blog.API.Rest;

import com.blog.Service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tags")
@Tag(name = "Tags", description = "Tag management APIs for managing blog post tags and categories")
public class RestTagController {
    private final TagService tagService;
    
    public RestTagController(TagService tagService) {
        this.tagService = tagService;
    }
    @GetMapping
    @Operation(
        summary = "Get all tags",
        description = "Retrieves a list of all available tags in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        )
    })
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/post/{postId}")
    @Operation(
        summary = "Get tags for a post",
        description = "Retrieves all tags associated with a specific blog post"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<List<String>> getTagsForPost(
        @Parameter(description = "ID of the post to retrieve tags for", required = true, example = "1")
        @PathVariable Long postId
    ) {
        return ResponseEntity.ok(tagService.getTagsForPost(postId));
    }
    @PostMapping("/post/{postId}")
    @Operation(
        summary = "Set tags for a post",
        description = "Sets or updates the tags associated with a specific blog post"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags set successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid tag data",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<Void> setPostTags(
        @Parameter(description = "ID of the post to set tags for", required = true, example = "1")
        @PathVariable Long postId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "List of tag names to associate with the post",
            required = true
        )
        @RequestBody List<String> tags
    ) {
        tagService.setPostTags(postId, tags);
        return ResponseEntity.ok().build();
    }
}