package com.blog.API.Rest;

import com.blog.Service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tags")
public class RestTagController {
    private final TagService tagService;
    
    public RestTagController(TagService tagService) {
        this.tagService = tagService;
    }
    @GetMapping
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<String>> getTagsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(tagService.getTagsForPost(postId));
    }
    @PostMapping("/post/{postId}")
    public ResponseEntity<Void> setPostTags(@PathVariable Long postId, @RequestBody List<String> tags) {
        tagService.setPostTags(postId, tags);
        return ResponseEntity.ok().build();
    }
}