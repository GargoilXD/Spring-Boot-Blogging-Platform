package com.blog.Repository;

import com.blog.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    // Epic 4: OAuth2 login — look up users by Google email
    Optional<User> findByEmail(String email);
}
