package com.blog.API.Rest;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.ResponseCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.Service.CommentService;
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
public class RestCommentController {
    private final CommentService commentService;

    public RestCommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    @GetMapping("/{postId}")
    public ResponseEntity<List<ResponseCommentDTO>> getCommentsForPost(@PathVariable @Min(1) Long postId) {
        return ResponseEntity.ok(commentService.getForPost(postId).stream().map(ResponseCommentDTO::new).toList());
    }
    @PostMapping
    public ResponseEntity<ResponseCommentDTO> createComment(@Valid @RequestBody CreateCommentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseCommentDTO(commentService.save(dto)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseCommentDTO> updateComment(@PathVariable String id, @Valid @RequestBody UpdateCommentDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseCommentDTO(commentService.update(new UpdateCommentDTO(id, dto.userId(), dto.username(), dto.postId(), dto.body()))));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}