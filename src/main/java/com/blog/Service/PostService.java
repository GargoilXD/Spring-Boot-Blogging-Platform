package com.blog.Service;

import com.blog.DataAccessor.Interface.PostDataAccessor;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;

import jakarta.persistence.EntityNotFoundException;

import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostDataAccessor dataAccessor;

    public PostService(PostDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }
    public Optional<Post> getPost(long id) {
        return dataAccessor.findByID(id);
    }
    public List<Post> getAllPosts(GetPostDTO dto) {
        return dataAccessor.findAll(dto);
    }
    public long countPosts() {
        return dataAccessor.count();
    }
    @Transactional
    public Post save(@NotNull CreatePostDTO dto) {
        return dataAccessor.save(dto);
    }
    @Transactional
    public Post update(@NotNull UpdatePostDTO dto) {
        getPost(dto.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + dto.postId()));
        return dataAccessor.update(dto);
    }
    @Transactional
    public void delete(long id) {
        if (dataAccessor.findByID(id).isEmpty()) throw new IllegalStateException("Post not found: " + id);
        dataAccessor.delete(id);
    }
}