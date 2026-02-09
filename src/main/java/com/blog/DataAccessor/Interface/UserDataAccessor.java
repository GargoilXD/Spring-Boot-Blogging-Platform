package com.blog.DataAccessor.Interface;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;

import java.util.Optional;

public interface UserDataAccessor {
    Optional<User> getByID(Long id) throws DataAccessException;
    Optional<User> getByUsername(String username) throws DataAccessException;
    void register(RegisterUserDTO dto) throws DataAccessException;
}
