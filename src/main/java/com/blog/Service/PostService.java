package com.blog.Service;

import com.blog.DataAccessor.Interface.PostDataAccessor;
import com.blog.DataTransporter.PostDataTransporter;
import com.blog.Model.Post;

import jakarta.persistence.EntityNotFoundException;

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
    public Optional<Post> getPost(Long id) {
        return dataAccessor.findByID(id);
    }
    public List<Post> getAllPosts() {
        return dataAccessor.findAll();
    }
    @Transactional
    public PostDataTransporter save(PostDataTransporter postData) {
        if (postData == null) throw new IllegalArgumentException("Post data cannot be null");
        return dataAccessor.save(postData);
    }
    @Transactional
    public PostDataTransporter update(Long id, String title, String body, boolean draft) {
        Post existing = getPost(id).orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));
        PostDataTransporter dto = new PostDataTransporter(
                id,
                existing.getUserId(),
                existing.getUsername(),
                title,
                body,
                draft,
                existing.getCreatedAt()
        );
        return dataAccessor.update(dto);
    }
    @Transactional
    public void delete(long id) {
        if (dataAccessor.findByID(id).isEmpty()) throw new IllegalStateException("Post not found: " + id);
        dataAccessor.delete(id);
    }
}