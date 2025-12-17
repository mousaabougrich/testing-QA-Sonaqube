package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
}