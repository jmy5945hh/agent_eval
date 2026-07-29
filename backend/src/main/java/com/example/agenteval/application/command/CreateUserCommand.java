package com.example.agenteval.application.command;

import com.example.agenteval.application.dto.CreateUserRequest;
import com.example.agenteval.application.dto.UserResponse;
import com.example.agenteval.domain.model.User;
import com.example.agenteval.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserCommand {

    private final UserDomainService userDomainService;

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        User user = userDomainService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPhone()
        );

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreate_time())
                .updatedAt(user.getUpdate_time())
                .build();
    }
}
