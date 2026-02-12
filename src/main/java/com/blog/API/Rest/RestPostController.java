package com.blog.API.Rest;

import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;
import com.blog.Service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/posts")
@Validated
@Tag(name = "Posts", description = "Blog post management APIs for creating, reading, updating, and deleting posts")
public class RestPostController {
    private final PostService postService;

    public RestPostController(PostService postService) {
        this.postService = postService;
    }
    @GetMapping("/{id}")
    @Operation(
        summary = "Get post by ID",
        description = "Retrieves a single blog post by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post found and returned successfully",
            content = @Content(schema = @Schema(implementation = ResponsePostDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post ID format",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<ResponsePostDTO> getPost(
        @Parameter(description = "ID of the post to retrieve", required = true, example = "1")
        @PathVariable @Min(1) Long id
    ) {
        return postService.getPost(id).map(ResponsePostDTO::new).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    @Operation(
        summary = "Get all posts",
        description = "Retrieves a paginated list of all blog posts with optional sorting"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination or sorting parameters",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<Page<ResponsePostDTO>> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(pageable).map(ResponsePostDTO::new));
    }
    @PostMapping
    @Operation(
        summary = "Create new post",
        description = "Creates a new blog post with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Post created successfully",
            content = @Content(schema = @Schema(implementation = ResponsePostDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post data",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<ResponsePostDTO> createPost(@Valid @RequestBody CreatePostDTO createDTO) {
        ResponsePostDTO response = new ResponsePostDTO(postService.save(createDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    @Operation(
        summary = "Update post",
        description = "Updates an existing blog post with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post updated successfully",
            content = @Content(schema = @Schema(implementation = ResponsePostDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post data or ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<ResponsePostDTO> updatePost(
        @Parameter(description = "ID of the post to update", required = true, example = "1")
        @PathVariable @Min(1) Integer id,
        @Valid @RequestBody UpdatePostDTO updateDTO
    ) {
        ResponsePostDTO response = new ResponsePostDTO(postService.update(new UpdatePostDTO(id, updateDTO.userId(), updateDTO.title(), updateDTO.body(), updateDTO.draft())));
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete post",
        description = "Deletes a blog post by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid post ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<Void> deletePost(
        @Parameter(description = "ID of the post to delete", required = true, example = "1")
        @PathVariable @Min(1) Long id
    ) {
        postService.delete(id);
        return ResponseEntity.ok().build();
    }
}