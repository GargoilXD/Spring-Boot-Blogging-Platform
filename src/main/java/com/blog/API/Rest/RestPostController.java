package com.blog.API.Rest;

import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;
import com.blog.Service.PostService;
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
public class RestPostController {
    private final PostService postService;

    public RestPostController(PostService postService) {
        this.postService = postService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponsePostDTO> getPost(@PathVariable @Min(1) Long id) {
        return postService.getPost(id).map(ResponsePostDTO::new).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    public ResponseEntity<Page<ResponsePostDTO>> getAllPosts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "created_at") String sortBy, @RequestParam(defaultValue = "DESC") String direction) {
        GetPostDTO dto = new GetPostDTO(page, size, sortBy, direction);
        List<Post> posts = postService.getAllPosts(dto);
        long totalCount = postService.countPosts();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sort = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        List<ResponsePostDTO> responsePosts = posts.stream().map(ResponsePostDTO::new).toList();
        Page<ResponsePostDTO> pageResult = new PageImpl<>(responsePosts, pageable, totalCount);
        return ResponseEntity.ok(pageResult);
    }
    @PostMapping
    public ResponseEntity<ResponsePostDTO> createPost(@Valid @RequestBody CreatePostDTO createDTO) {
        ResponsePostDTO response = new ResponsePostDTO(postService.save(createDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponsePostDTO> updatePost(@PathVariable @Min(1) Long id, @Valid @RequestBody UpdatePostDTO updateDTO) {
        ResponsePostDTO response = new ResponsePostDTO(postService.update(new UpdatePostDTO(id, updateDTO.title(), updateDTO.body(), updateDTO.draft())));
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable @Min(1) Long id) {
        postService.delete(id);
        return ResponseEntity.ok().build();
    }
}