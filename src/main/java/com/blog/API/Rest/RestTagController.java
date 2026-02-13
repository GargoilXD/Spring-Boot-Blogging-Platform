package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.DataTransporter.Tags.ResponseTagsDTO;
import com.blog.Service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/tags")
@Tag(name = "Tags", description = "Tag management APIs for managing blog post tags and categories. Tags can be used to categorize and organize posts for better discoverability.")
public class RestTagController {
    private final TagService tagService;
    
    public RestTagController(TagService tagService) {
        this.tagService = tagService;
    }
    @GetMapping
    @Operation(
        summary = "Get all tags",
        description = "Retrieves a paginated list of all available tags in the system. Supports pagination parameters: page (default 0), size (default 10), and sort."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        )
    })
    public ResponseEntity<SuccessResponse<Page<ResponseTagsDTO>>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags retrieved successfully", tagService.findAll(pageable).map(ResponseTagsDTO::new))) ;
    }
    @GetMapping("/{postId}")
    @Operation(
        summary = "Get tags for a post",
        description = "Retrieves all tags associated with a specific blog post. Returns a list of tag names."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post ID - ID must be a positive integer",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<List<String>>> findByPostId(
        @Parameter(description = "ID of the post to retrieve tags for", required = true, example = "1")
        @PathVariable @Min(1) Integer postId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags retrieved successfully", tagService.findByPostId(postId))) ;
    }
    @PostMapping
    @Operation(
        summary = "Set post tags",
        description = "Replaces all tags for a specific post with the provided list. Any existing tags will be removed and replaced with the new set.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Post tags data",
            required = true,
            content = @Content(schema = @Schema(implementation = PostTagsDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tags set successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid tag data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> setPostTags(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.setPostTags(DTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(HttpStatus.CREATED, "Tags set successfully"));
    }
    @PutMapping("/add")
    @Operation(
        summary = "Add tags to post",
        description = "Adds new tags to a specific post. Existing tags are preserved. Duplicate tags will be ignored.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Tags to add to the post",
            required = true,
            content = @Content(schema = @Schema(implementation = PostTagsDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags added successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid tag data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> addTagsToPost(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.addTagsToPost(DTO);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags added successfully"));
    }
    @PutMapping("/remove")
    @Operation(
        summary = "Remove tags from post",
        description = "Removes specified tags from a specific post. Only the tags in the request will be removed; other tags remain unchanged.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Tags to remove from the post",
            required = true,
            content = @Content(schema = @Schema(implementation = PostTagsDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags removed successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid tag data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> removeTagsFromPost(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.removeTagsFromPost(DTO);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags removed successfully"));
    }
    @DeleteMapping("/{postId}")
    @Operation(
        summary = "Delete all tags for a post",
        description = "Removes all tags associated with a specific post. This action is irreversible."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tags deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post ID - ID must be a positive integer",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> deleteByPostId(
        @Parameter(description = "ID of the post to delete all tags for", required = true, example = "1")
        @PathVariable @Min(1) int postId
    ) {
        tagService.deleteByPostId(postId);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags deleted successfully")) ;
    }
}