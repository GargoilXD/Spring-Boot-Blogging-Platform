package com.blog.API.GraphQL;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.Comment;
import com.blog.Model.Post;
import com.blog.Service.*;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

//@Controller
public class GraphQLResolver {
    private final PostService postService;
    private final AuthenticationService authService;
    private final CommentService commentService;
    private final TagService tagService;
    
    public GraphQLResolver(PostService postService, AuthenticationService authService, CommentService commentService, TagService tagService) {
        this.postService = postService;
        this.authService = authService;
        this.commentService = commentService;
        this.tagService = tagService;
    }

    @QueryMapping
    public ResponsePostDTO getPostByID(@Argument Long id) {
        return postService.getPost(id).map(ResponsePostDTO::new).orElse(null);
    }
    @QueryMapping
    public List<ResponsePostDTO> getAllPosts( Pageable pageable) {
        return postService.getAllPosts(pageable).stream().map(ResponsePostDTO::new).collect(Collectors.toList());
    }
    @QueryMapping
    public List<String> getAllTags() {
        return tagService.getAllTags();
    }
    @QueryMapping
    public List<String> getTagsForPost(@Argument Long postID) {
        return tagService.getTagsForPost(postID);
    }
    @QueryMapping
    public List<Comment> getCommentsForPost(@Argument Long postID) {
        return commentService.getForPost(postID);
    }

    @MutationMapping
    public ResponsePostDTO createPost(@Argument("input") CreatePostDTO input) {
        return new ResponsePostDTO(postService.save(input));
    }
    @MutationMapping
    public ResponsePostDTO updatePost(@Argument Integer id, @Argument("input") UpdatePostDTO input) {
        return new ResponsePostDTO(postService.update(new UpdatePostDTO(id, input.userId(), input.title(), input.body(), input.draft())));
    }
    @MutationMapping
    public Boolean deletePost(@Argument Integer id) {
        postService.delete(id);
        return true;
    }
    @MutationMapping
    public Boolean login(@Argument String username, @Argument String password) {
        authService.authenticate(username.trim(), password);
        return true;
    }
    @MutationMapping
    public Boolean register(@Argument("input") RegisterUserDTO input) {
        authService.register(input);
        return true;
        
    }
    @MutationMapping
    public Boolean addComment(@Argument("input") CreateCommentDTO input) {
        commentService.save(input);
        return true;
    }
    @MutationMapping
    public Boolean updateComment(@Argument String id, @Argument String body) {
        commentService.update(new UpdateCommentDTO(id, null, id, null, body));
        return true;
    }
    @MutationMapping
    public Boolean deleteComment(@Argument String id) {
        commentService.delete(id);
        return true;
    }
    @MutationMapping
    public Boolean setPostTags(@Argument Integer postID, @Argument List<String> tags) {
        tagService.setPostTags(postID, tags != null ? tags : List.of());
        return true;
    }
    
    @SchemaMapping(typeName = "Post", field = "tags")
    public List<String> getTags(Post post) {
        return tagService.getTagsForPost(post.getId());
    }
    
    @SchemaMapping(typeName = "Post", field = "comments")
    public List<Comment> getComments(Post post) {
        return commentService.getForPost(post.getId());
    }
}