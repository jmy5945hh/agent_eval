package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.command.CreateUserCommand;
import com.example.agenteval.application.dto.CreateUserRequest;
import com.example.agenteval.application.dto.UserResponse;
import com.example.agenteval.application.query.GetUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserCommand createUserCommand;
    private final GetUserQuery getUserQuery;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserCommand.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return getUserQuery.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
