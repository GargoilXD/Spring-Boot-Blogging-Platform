package com.blog.API.GraphQL;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.ResponsePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.DataTransporter.Tags.ResponseTagsDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.Comment;
import com.blog.Model.Post;
import com.blog.Service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
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
    public ResponsePostDTO findPostByID(@Argument Integer id) {
        return postService.findById(id).map(ResponsePostDTO::new).orElse(null);
    }
    @QueryMapping
    public Page<ResponsePostDTO> getAllPosts(@Argument Integer page, @Argument Integer size) {
        return postService.findAll(PageRequest.of(page, size)).map(ResponsePostDTO::new);
    }
    @QueryMapping
    public Page<ResponseTagsDTO> findAllTags(@Argument Integer page, @Argument Integer size) {
        return tagService.findAll(PageRequest.of(page, size)).map(ResponseTagsDTO::new);
    }
    @QueryMapping
    public List<String> findTagsForPost(@Argument Integer postID) {
        return tagService.findByPostId(postID);
    }
    @QueryMapping
    public List<Comment> findCommentsForPost(@Argument Integer postID) {
        return commentService.findByPostId(postID);
    }
    // Mutation Mappings
    @MutationMapping
    public Boolean login(@Argument String username, @Argument String password) {
        authService.login(username.trim(), password);
        return true;
    }
    @MutationMapping
    public Boolean register(@Argument("input") RegisterUserDTO input) {
        authService.register(input);
        return true;
    }
    @MutationMapping
    public ResponsePostDTO createPost(@Argument("input") CreatePostDTO input) {
        return new ResponsePostDTO(postService.save(input));
    }
    @MutationMapping
    public ResponsePostDTO updatePost(@Argument("input") UpdatePostDTO input) {
        return new ResponsePostDTO(postService.update(input));
    }
    @MutationMapping
    public Boolean deletePost(@Argument Integer id) {
        postService.delete(id);
        return true;
    }

    @MutationMapping
    public Boolean addComment(@Argument("input") CreateCommentDTO input) {
        commentService.save(input);
        return true;
    }
    @MutationMapping
    public Boolean updateComment(@Argument("input") UpdateCommentDTO input) {
        commentService.update(input);
        return true;
    }
    @MutationMapping
    public Boolean deleteComment(@Argument Integer id) {
        commentService.delete(id);
        return true;
    }
    @MutationMapping
    public Boolean setPostTags(@Argument Integer postID, @Argument List<String> tags) {
        tagService.setPostTags(new PostTagsDTO(postID, tags));
        return true;
    }
    @SchemaMapping(typeName = "Post", field = "tags")
    public List<String> getTags(Post post) {
        return tagService.findByPostId(post.getId());
    }
    @SchemaMapping(typeName = "Post", field = "comments")
    public List<Comment> getComments(Post post) {
        return commentService.findByPostId(post.getId());
    }
}