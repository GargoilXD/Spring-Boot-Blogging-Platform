package com.blog.DataAccessor.JDBC;

import com.blog.DataAccessor.Interface.UserDataAccessor;
import com.blog.Exception.DataAccessException;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class JDBCUserDataAccessor implements UserDataAccessor {
    enum Queries {
        GET_BY_ID("SELECT * FROM users WHERE id = ?"),
        GET_BY_USERNAME("SELECT * FROM users WHERE username = ?"),
        INSERT("INSERT INTO users (username, password, full_name, email, gender) VALUES (?, ?, ?, ?, ?)");

        final String query;
        Queries(String query) { this.query = query; }
    }
    private final DataSource dataSource;

    public JDBCUserDataAccessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public Optional<User> getByID(Long id) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.GET_BY_ID.query)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()?  Optional.of(User.fromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Getting User By ID: %s", id), e);
        }
    }
    @Override
    public Optional<User> getByUsername(String username) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.GET_BY_USERNAME.query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()? Optional.of(User.fromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Getting User By Name: %s", username), e);
        }
    }
    @Override
    public void register(RegisterUserDTO dto) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.INSERT.query)) {
            statement.setString(1, dto.username());
            statement.setString(2, dto.password());
            statement.setString(3, dto.fullName());
            statement.setString(4, dto.email());
            statement.setString(5, dto.gender());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Saving User: %s", dto.username()), e);
        }
    }
}
