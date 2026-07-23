package com.example.agenteval.domain.service;

import com.example.agenteval.domain.model.User;
import com.example.agenteval.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public User createUser(String username, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .status(1)
                .build();

        return userRepository.save(user);
    }
}
