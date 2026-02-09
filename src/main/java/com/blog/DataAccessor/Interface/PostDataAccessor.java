package com.blog.DataAccessor.Interface;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataTransporter.PostDataTransporter;
import com.blog.Model.Post;

import java.util.List;
import java.util.Optional;

public interface PostDataAccessor {
    Optional<Post> findByID(Long id) throws DataAccessException;
    List<Post> findAll() throws DataAccessException;
    PostDataTransporter save(PostDataTransporter pdto) throws DataAccessException;
    PostDataTransporter update(PostDataTransporter pdto) throws DataAccessException;
    void delete(Long ID) throws DataAccessException;
}
