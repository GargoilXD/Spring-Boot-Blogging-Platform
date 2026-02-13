package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@Validated
@Tag(name = "Posts", description = "Blog post management APIs for creating, reading, updating, and deleting blog posts with pagination and sorting support")
public class RestPostController {
    private final PostService postService;

    public RestPostController(PostService postService) {
        this.postService = postService;
    }
    @GetMapping("/{id}")
    @Operation(
        summary = "Get post by ID",
        description = "Retrieves a single blog post by its unique identifier. Returns complete post details including author information and creation timestamp."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post found and returned successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post ID format - ID must be a positive integer",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<ResponsePostDTO>> findById(
        @Parameter(description = "ID of the post to retrieve", required = true, example = "1")
        @PathVariable @Min(1) Integer id
    ) {
        return postService.findById(id).map(ResponsePostDTO::new).map(dto -> ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Post found and returned successfully", dto))).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SuccessResponse<>(HttpStatus.NOT_FOUND, "Post not found")));
    }
    @GetMapping
    @Operation(
        summary = "Get all posts",
        description = "Retrieves a paginated list of all blog posts with optional sorting. Supports pagination parameters: page (default 0), size (default 10), and sort (e.g., createdAt,desc)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination or sorting parameters",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Page<ResponsePostDTO>>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Posts retrieved successfully", postService.findAll(pageable).map(ResponsePostDTO::new))) ;
    }
    @PostMapping
    @Operation(
        summary = "Create new post",
        description = "Creates a new blog post with the provided details. The post will be assigned a unique ID and creation timestamp automatically.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Post creation data",
            required = true,
            content = @Content(schema = @Schema(implementation = CreatePostDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Post created successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<ResponsePostDTO>> createPost(@Valid @RequestBody CreatePostDTO createDTO) {
        ResponsePostDTO response = new ResponsePostDTO(postService.save(createDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(HttpStatus.CREATED, "Post created successfully", response));
    }
    @PutMapping
    @Operation(
        summary = "Update post",
        description = "Updates an existing blog post with the provided details. Only the specified fields will be updated. The post ID must exist.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Post update data",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdatePostDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post updated successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post data or ID - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<ResponsePostDTO>> updatePost(@Valid @RequestBody UpdatePostDTO updateDTO) {
        ResponsePostDTO response = new ResponsePostDTO(postService.update(updateDTO));
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Post updated successfully", response))   ;
    }
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete post",
        description = "Deletes a blog post by its unique identifier. This action is irreversible and will also delete all associated comments and tags."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found - no post exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post ID - ID must be a positive integer",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> deletePost(
        @Parameter(description = "ID of the post to delete", required = true, example = "1")
        @PathVariable @Min(1) Integer id
    ) {
        postService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Post deleted successfully")) ;
    }
}