package com.blog.API.Rest;

import com.blog.DataTransporter.PostDataTransporter;
import com.blog.Service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/posts")
@Validated
public class RestPostController {
    public record PostUpdateRequest(
            @NotBlank String title,
            @NotBlank String body,
            boolean draft
    ) {}
    public record PostCreateRequest(
            @NotBlank String title,
            @NotBlank String body,
            boolean draft
    ) {}

    private final PostService postService;

    public RestPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDataTransporter> getPost(@PathVariable @Min(1) Long id) {
        return postService.getPost(id).map(PostDataTransporter::fromEntity).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    public ResponseEntity<List<PostDataTransporter>> getAllPosts() {
        List<PostDataTransporter> posts = postService.getAllPosts().stream().map(PostDataTransporter::fromEntity).toList();
        return ResponseEntity.ok(posts);
    }
    @PostMapping
    public ResponseEntity<PostDataTransporter> createPost(@Valid @RequestBody PostCreateRequest createRequest) {
        PostDataTransporter postDto = new PostDataTransporter(
                0L,
                0L,
                "",
                createRequest.title(),
                createRequest.body(),
                createRequest.draft(),
                null
        );
        PostDataTransporter saved = postService.save(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PostDataTransporter> updatePost(@PathVariable @Min(1) Long id, @Valid @RequestBody PostUpdateRequest updateRequest) {
        PostDataTransporter updated = postService.update(
                id,
                updateRequest.title(),
                updateRequest.body(),
                updateRequest.draft()
        );
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable @Min(1) Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}