package com.blog.Repository;

import com.blog.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
//    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.createdAt DESC")
    List<Comment> findByPostId(Integer postId);
//    @Query("DELETE FROM Comment c WHERE c.postId = :postId")
    void deleteByPostId(Integer postId);
}
