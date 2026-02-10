package com.blog.DataAccessor.Interface;

import com.blog.Exception.DataAccessException;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.Model.Post;

import java.util.List;
import java.util.Optional;

public interface PostDataAccessor {
    Optional<Post> findByID(Long id) throws DataAccessException;
    List<Post> findAll(GetPostDTO dto) throws DataAccessException;
    long count() throws DataAccessException;
    Post save(CreatePostDTO dto) throws DataAccessException;
    Post update(UpdatePostDTO dto) throws DataAccessException;
    void delete(Long ID) throws DataAccessException;
}
