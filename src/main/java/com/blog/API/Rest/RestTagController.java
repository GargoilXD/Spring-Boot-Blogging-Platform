package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.DataTransporter.Tags.ResponseTagsDTO;
import com.blog.Service.TagService;
import io.swagger.v3.oas.annotations.Operation;
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
    public ResponseEntity<SuccessResponse<Page<ResponseTagsDTO>>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags retrieved successfully", tagService.findAll(pageable).map(ResponseTagsDTO::new))) ;
    }
    @GetMapping("/{postId}")
    public ResponseEntity<SuccessResponse<List<String>>> findByPostId(@PathVariable @Min(1) Integer postId) {
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags retrieved successfully", tagService.findByPostId(postId))) ;
    }
    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> setPostTags(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.setPostTags(DTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(HttpStatus.CREATED, "Tags set successfully"));
    }
    @PutMapping("/add")
    public ResponseEntity<SuccessResponse<Void>> addTagsToPost(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.addTagsToPost(DTO);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags added successfully"));
    }
    @PutMapping("/remove")
    public ResponseEntity<SuccessResponse<Void>> removeTagsFromPost(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.removeTagsFromPost(DTO);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags removed successfully"));
    }
    @DeleteMapping("/{postId}")
    public ResponseEntity<SuccessResponse<Void>> deleteByPostId(@PathVariable @Min(1) int postId) {
        tagService.deleteByPostId(postId);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>(HttpStatus.OK, "Tags deleted successfully")) ;
    }
}