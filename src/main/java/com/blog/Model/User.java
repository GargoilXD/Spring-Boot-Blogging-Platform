package com.blog.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@RequiredArgsConstructor
public final class User {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final String email;
    private final String gender;
    private final String createdAt;

    public static User fromResultSet(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("gender"),
                resultSet.getString("created_at")
        );
    }

    public void validate() {
        if (username == null || !username.matches("[a-zA-Z0-9]{3,20}")) {
            throw new IllegalArgumentException("Username must be alphanumeric, 3-20 characters long.");
        }
        if (email == null || !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
    }
}
