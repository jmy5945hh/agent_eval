package com.example.agenteval.application.query;

import com.example.agenteval.application.dto.UserResponse;
import com.example.agenteval.domain.model.User;
import com.example.agenteval.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserQuery {

    private final UserRepository userRepository;

    public Optional<UserResponse> execute(Integer userId) {
        return userRepository.findById(userId)
                .map(this::toResponse);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreateTime())
                .updatedAt(user.getUpdateTime())
                .build();
    }
}
