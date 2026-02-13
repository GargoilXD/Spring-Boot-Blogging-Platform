package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.ResponseCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/comments")
@Validated
@Tag(name = "Comments", description = "Comment management APIs for creating, reading, updating, and deleting comments on blog posts. Comments are associated with specific posts and users.")
public class RestCommentController {
    private final CommentService commentService;

    public RestCommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    @GetMapping("/post/{postId}")
    @Operation(
        summary = "Get comments for a post",
        description = "Retrieves all comments associated with a specific blog post. Returns a list of comments with their IDs, author information, and content."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comments retrieved successfully",
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
    public ResponseEntity<SuccessResponse<List<ResponseCommentDTO>>> getCommentsForPost(
        @Parameter(description = "ID of the post to retrieve comments for", required = true, example = "1")
        @PathVariable @Min(1) Integer postId
    ) {
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Comments retrieved successfully", commentService.findByPostId(postId).stream().map(ResponseCommentDTO::new).toList()));
    }
    @PostMapping
    @Operation(
        summary = "Create new comment",
        description = "Creates a new comment on a blog post. The comment will be associated with the specified user and post. Both user and post must exist.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Comment creation data",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateCommentDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Comment created successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid comment data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post or user not found - referenced entities do not exist",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<ResponseCommentDTO>> createComment(@Valid @RequestBody CreateCommentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(HttpStatus.CREATED, "Comment created successfully", new ResponseCommentDTO(commentService.save(dto))))  ;
    }
    @PutMapping
    @Operation(
        summary = "Update comment",
        description = "Updates an existing comment with the provided details. Only the comment content can be modified. The comment ID must exist.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Comment update data",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateCommentDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comment updated successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Comment not found - no comment exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid comment data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<ResponseCommentDTO>> updateComment(@Valid @RequestBody UpdateCommentDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Comment updated successfully", new ResponseCommentDTO(commentService.update(dto))));
    }
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete comment",
        description = "Deletes a comment by its unique identifier. This action is irreversible. The comment will be permanently removed from the system."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comment deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Comment not found - no comment exists with the provided ID",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
        @Parameter(description = "ID of the comment to delete", required = true, example = "1")
        @PathVariable Integer id
    ) {
        commentService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Comment deleted successfully"));
    }
}