package com.blog.DataAccessor.JDBC;

import com.blog.DataAccessor.Interface.PostDataAccessor;
import com.blog.DataTransporter.Post.CreatePostDTO;
import com.blog.DataTransporter.Post.GetPostDTO;
import com.blog.DataTransporter.Post.UpdatePostDTO;
import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.Model.Post;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JDBCPostDataAccessor implements PostDataAccessor {
    enum Queries {
        GET_BY_ID("SELECT posts.id, user_id, username, title, body, posts.created_at, is_draft FROM posts JOIN users ON posts.user_id = users.id WHERE posts.id = ?"),
        GET_ALL("SELECT posts.id, user_id, username, title, body, posts.created_at, is_draft FROM posts JOIN users ON posts.user_id = users.id"),
        PAGINATED_GET_ALL("SELECT posts.id, user_id, username, title, body, posts.created_at, is_draft FROM posts JOIN users ON posts.user_id = users.id ORDER BY %s %s LIMIT ? OFFSET ?"),
        COUNT("SELECT COUNT(*) FROM posts"),
        INSERT("INSERT INTO posts (user_id, title, body, is_draft) VALUES (?, ?, ?, ?) RETURNING id"),
        UPDATE("UPDATE posts SET title = ?, body = ?, is_draft = ? WHERE id = ?"),
        DELETE("DELETE FROM posts WHERE id = ?");

        final String query;
        Queries(String query) { this.query = query; }
    }
    private final DataSource dataSource;

    public JDBCPostDataAccessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public Optional<Post> findByID(Long id) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.GET_BY_ID.query)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()? Optional.of(Post.fromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Getting Post By ID: %s", id), e);
        }
    }
    @Override
    public List<Post> findAll(GetPostDTO dto) throws DataAccessException {
        dto.validate();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(String.format(Queries.PAGINATED_GET_ALL.query, dto.getFullColumnName(), dto.getDirection()))) {
            statement.setInt(1, dto.getSize());
            statement.setInt(2, dto.getPage() * dto.getSize());
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<Post> posts = new ArrayList<>();
                while (resultSet.next()) {
                    posts.add(Post.fromResultSet(resultSet));
                }
                return posts;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Getting Posts with pagination: page=%d, size=%d, sortBy=%s, direction=%s", dto.getPage(), dto.getSize(), dto.getSortBy(), dto.getDirection()), e);
        }
    }
    @Override
    public long count() throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(Queries.COUNT.query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                } else {
                    throw new DataAccessException("Error getting post count: no result returned");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting posts", e);
        }
    }
    @Override
    public Post save(CreatePostDTO dto) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.INSERT.query)) {
            statement.setLong(1, dto.authorId());
            statement.setString(2, dto.title());
            statement.setString(3, dto.body());
            statement.setBoolean(4, dto.draft());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return dto.toEntity(resultSet.getLong("id"), null, null);
                } else {
                    throw new DataAccessException(String.format("Error Inserting Post %s", dto.title()));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Saving Post By User: %s", dto.authorId()), e);
        }
    }

    @Override
    public Post update(UpdatePostDTO dto) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.UPDATE.query)) {
            statement.setString(1, dto.title());
            statement.setString(2, dto.body());
            statement.setBoolean(3, dto.draft());
            statement.setLong(4, dto.postId());
            statement.executeUpdate();
            return dto.toEntity();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Updating Post By ID: %s", dto.postId()), e);
        }
    }

    @Override
    public void delete(Long ID) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.DELETE.query)) {
            statement.setLong(1, ID);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Deleting Post By ID: %s", ID), e);
        }
    }
}