package com.blog.Service;

import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.Model.PostTags;
import com.blog.Repository.PostRepository;
import com.blog.Repository.TagRepository;

import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

@Service
public class TagService {
    TagRepository repository;
    PostRepository postRepository;

    public TagService(TagRepository repository, PostRepository postRepository) {
        this.repository = repository;
        this.postRepository = postRepository;
    }
    @Cacheable(cacheNames = "PostTags.findAll", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<PostTags> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    @Cacheable(cacheNames = "PostTags.findByPostId", key = "#postId")
    public List<String> findByPostId(int postId) {
        return repository.findByPostId(postId).map(PostTags::getTags).orElse(Collections.emptyList());
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void setPostTags(PostTagsDTO DTO) {
        postRepository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        repository.save(DTO.toEntity());
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void addTagsToPost(PostTagsDTO DTO) {
        postRepository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        PostTags existing = repository.findByPostId(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Failed to Add Tags For Post:" + DTO.postId()));
        Set<String> currentTags = new HashSet<>(existing.getTags());
        currentTags.addAll(DTO.tags());
        existing.setTags(new ArrayList<>(currentTags));
        repository.save(existing);
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void removeTagsFromPost(PostTagsDTO DTO) {
        postRepository.findById(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Post not found: " + DTO.postId()));
        PostTags existing = repository.findByPostId(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Failed to Remove Tags For Post:" + DTO.postId()));
        Set<String> currentTags = new HashSet<>(existing.getTags());
        DTO.tags().forEach(currentTags::remove);
        if (currentTags.isEmpty()) repository.delete(existing);
        else {
            existing.setTags(new ArrayList<>(currentTags));
            repository.save(existing);
        }
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#postId"),
            @CacheEvict(cacheNames = {"PostTags.findAll", "PostTags.count"}, allEntries = true)
    })
    public void deleteByPostId(int postId) {
        repository.findByPostId(postId).orElseThrow(() -> new EntityNotFoundException("Failed to Delete All Tags For Post:" + postId));
        repository.deleteByPostId(postId);
    }
}
