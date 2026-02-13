package com.blog.API.Rest;

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
    public ResponseEntity<Page<ResponseTagsDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(tagService.findAll(pageable).map(ResponseTagsDTO::new));
    }
    @GetMapping("/{postId}")
    public ResponseEntity<List<String>> findByPostId(@PathVariable @Min(1) Integer postId) {
        return ResponseEntity.ok(tagService.findByPostId(postId));
    }
    @PostMapping
    public ResponseEntity<Void> setPostTags(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.setPostTags(DTO);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/add")
    public ResponseEntity<Void> addTagsToPost(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.addTagsToPost(DTO);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/remove")
    public ResponseEntity<Void> removeTagsFromPost(@Valid @RequestBody PostTagsDTO DTO) {
        tagService.removeTagsFromPost(DTO);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteByPostId(@PathVariable @Min(1) int postId) {
        tagService.deleteByPostId(postId);
        return ResponseEntity.ok().build();
    }
}