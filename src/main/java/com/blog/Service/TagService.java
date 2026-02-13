package com.blog.Service;

import com.blog.DataTransporter.Tags.PostTagsDTO;
import com.blog.Model.PostTags;
import com.blog.Repository.TagRepository;

import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

@Service
public class TagService {
    TagRepository repository;

    public TagService(TagRepository repository) {
        this.repository = repository;
    }
    @Cacheable(cacheNames = "PostTags.findAll", key = "{#pageable.pageNumber, #pageable.pageSize}")
    public Page<PostTags> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    @Cacheable(cacheNames = "PostTags.findByPostId", key = "#postId")
    public List<String> findByPostId(int postId) {
        return repository.findByPostId(postId).map(PostTags::getTags).orElse(Collections.emptyList());
    }
    @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#DTO.postId")
    public void setPostTags(PostTagsDTO DTO) {
        repository.save(DTO.toEntity());
    }
    @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#DTO.postId")
    public void addTagsToPost(PostTagsDTO DTO) {
        PostTags existing = repository.findByPostId(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Failed to Add Tags For Post:" + DTO.postId()));
        Set<String> currentTags = new HashSet<>(existing.getTags());
        currentTags.addAll(DTO.tags());
        existing.setTags(new ArrayList<>(currentTags));
        repository.save(existing);
    }
    @CacheEvict(cacheNames = "PostTags.findByPostId", key = "#DTO.postId")
    public void removeTagsFromPost(PostTagsDTO DTO) {
        PostTags existing = repository.findByPostId(DTO.postId()).orElseThrow(() -> new EntityNotFoundException("Failed to Remove Tags For Post:" + DTO.postId()));
        Set<String> currentTags = new HashSet<>(existing.getTags());
        DTO.tags().forEach(currentTags::remove);
        if (currentTags.isEmpty()) repository.delete(existing);
        else {
            existing.setTags(new ArrayList<>(currentTags));
            repository.save(existing);
        }
    }
    public void deleteByPostId(int postId) {
        repository.findByPostId(postId).orElseThrow(() -> new EntityNotFoundException("Failed to Delete All Tags For Post:" + postId));
        repository.deleteByPostId(postId);
    }
}
