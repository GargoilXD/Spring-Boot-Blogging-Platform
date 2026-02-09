package com.blog.DataAccessor.JDBC;

import com.blog.DataAccessor.Interface.PostDataAccessor;
import com.blog.DataTransporter.PostDataTransporter;
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
        GET_BY_ID("SELECT posts.id, user_id, username, title, body, posts.created_at, is_draft FROM posts JOIN users ON posts.user_id = users.id WHERE id = ?"),
        GET_ALL("SELECT posts.id, user_id, username, title, body, posts.created_at, is_draft FROM posts JOIN users ON posts.user_id = users.id"),
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
    public List<Post> findAll() throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.GET_ALL.query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<Post> posts = new ArrayList<>();
                while (resultSet.next()) posts.add(Post.fromResultSet(resultSet));
                return posts;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error Getting All Posts", e);
        }
    }
    @Override
    public PostDataTransporter save(PostDataTransporter pdto) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.INSERT.query)) {
            statement.setLong(1, pdto.userId());
            statement.setString(2, pdto.title());
            statement.setString(3, pdto.body());
            statement.setBoolean(4, pdto.draft());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Post post = new Post(
                            resultSet.getLong("id"),
                            pdto.userId(),
                            pdto.username(),
                            pdto.title(),
                            pdto.body(),
                            pdto.draft(),
                            pdto.createdAt()
                    );
                    return PostDataTransporter.fromEntity(post);
                } else {
                    throw new DataAccessException(String.format("Error Inserting Post %s", pdto.title()));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Saving Post By User: %s", pdto.userId()), e);
        }
    }
    @Override
    public PostDataTransporter update(PostDataTransporter pdto) throws DataAccessException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(Queries.UPDATE.query)) {
            statement.setString(1, pdto.title());
            statement.setString(2, pdto.body());
            statement.setBoolean(3, pdto.draft());
            statement.setLong(4, pdto.id());
            statement.executeUpdate();
            return pdto;
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error Updating Post By ID: %s", pdto.id()), e);
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
