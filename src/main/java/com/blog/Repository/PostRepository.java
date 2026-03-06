package com.blog.Repository;

import com.blog.Model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("SELECT p FROM Post p WHERE p.draft = false ORDER BY p.createdAt DESC")
    List<Post> findAllPublished();
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByTitle(@Param("keyword") String keyword);
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(p.body)  LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Post> searchByTitleOrBody(@Param("kw") String keyword);
}
