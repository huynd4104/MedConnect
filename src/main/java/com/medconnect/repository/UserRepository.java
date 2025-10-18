package com.medconnect.repository;

import com.medconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.verified = true")
    Optional<User> findVerifiedByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.blocked = false")
    Optional<User> findActiveByEmail(String email);
}