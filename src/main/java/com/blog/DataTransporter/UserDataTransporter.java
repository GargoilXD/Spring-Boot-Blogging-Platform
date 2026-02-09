package com.blog.DataTransporter;

import com.blog.Model.User;

public record UserDataTransporter(
        Long id,
        String username,
        String passwordHash,
        String fullName,
        String email,
        String gender,
        String createdAt
) {
    public static UserDataTransporter fromEntity(User user) {
        return new UserDataTransporter(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getEmail(),
                user.getGender(),
                user.getCreatedAt()
        );
    }

    public User toEntity() {
        return new User(
                id != null ? id : 0L,
                username,
                passwordHash,
                fullName,
                email,
                gender,
                createdAt
        );
    }
}