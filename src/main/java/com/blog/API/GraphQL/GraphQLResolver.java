package com.blog.API.GraphQL;

import com.blog.DataTransporter.Comment.CreateCommentDTO;
import com.blog.DataTransporter.Comment.UpdateCommentDTO;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.Comment;
import com.blog.Model.Post;
import com.blog.Service.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
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
    public Post findPostByID(@Argument @NotNull Integer id) {
        return postService.findById(id).orElse(null);
    }
    @QueryMapping
    public List<Post> findAllPosts(@Argument Integer page, @Argument Integer size) {
        if (page == null) page = 0;
        if (size == null) size = 5;
        return postService.findAll(PageRequest.of(page, size)).toList();
    }
    @QueryMapping
    public List<String> findAllTags(@Argument Integer page, @Argument Integer size) {
        if (page == null) page = 0;
        if (size == null) size = 5;
        List<String> allTags = new ArrayList<>();
        tagService.findAll(PageRequest.of(page, size)).forEach(tags -> allTags.addAll(tags.getTags()));
        return allTags;
    }
    @QueryMapping
    public List<String> findTagsForPost(@Argument @NotNull Integer postID) {
        return tagService.findByPostId(postID);
    }
    @QueryMapping
    public List<Comment> findCommentsForPost(@Argument @NotNull Integer postID) {
        return commentService.findByPostId(postID);
    }
    // Mutation Mappings
    @MutationMapping
    public Boolean login(@Argument @NotNull String username, @Argument @NotNull String password) {
        authService.login(username.trim(), password);
        return true;
    }
    @MutationMapping
    public Boolean register(@Argument("input") @NotNull RegisterUserDTO input) {
        authService.register(input);
        return true;
    }
    @MutationMapping
    public Post createPost(@Argument("input") @NotNull CreatePostDTO input) {
        return postService.save(input);
    }
    @MutationMapping
    public Post updatePost(@Argument("input") @NotNull UpdatePostDTO input) {
        return postService.update(input);
    }
    @MutationMapping
    public Boolean deletePost(@Argument @NotNull Integer id) {
        postService.delete(id);
        return true;
    }

    @MutationMapping
    public Boolean addComment(@Argument("input") @NotNull CreateCommentDTO input) {
        commentService.save(input);
        return true;
    }
    @MutationMapping
    public Boolean updateComment(@Argument("input") @NotNull UpdateCommentDTO input) {
        commentService.update(input);
        return true;
    }
    @MutationMapping
    public Boolean deleteComment(@Argument @NotNull Integer id) {
        commentService.delete(id);
        return true;
    }
    @MutationMapping
    public Boolean setPostTags(@Argument @NotNull Integer postID, @Argument @NotNull List<String> tags) {
        tagService.setPostTags(new PostTagsDTO(postID, tags));
        return true;
    }
    @MutationMapping
    public Boolean addTagsToPost(@Argument @NotNull Integer postID, @Argument @NotNull List<String> tags) {
        tagService.addTagsToPost(new PostTagsDTO(postID, tags));
        return true;
    }
    @MutationMapping
    public Boolean removeTagsFromPost(@Argument @NotNull Integer postID, @Argument @NotNull List<String> tags) {
        tagService.removeTagsFromPost(new PostTagsDTO(postID, tags));
        return true;
    }
    @MutationMapping
    public Boolean deleteByPostId(@Argument @NotNull Integer postID) {
        tagService.deleteByPostId(postID);
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