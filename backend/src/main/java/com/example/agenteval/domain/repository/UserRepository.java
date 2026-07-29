package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
