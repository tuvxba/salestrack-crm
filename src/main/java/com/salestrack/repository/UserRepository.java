package com.salestrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salestrack.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
}