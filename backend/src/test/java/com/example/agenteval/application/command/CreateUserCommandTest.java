package com.example.agenteval.application.command;

import com.example.agenteval.application.dto.CreateUserRequest;
import com.example.agenteval.application.dto.UserResponse;
import com.example.agenteval.domain.model.User;
import com.example.agenteval.domain.service.UserDomainService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserCommandTest {

    @Mock
    private UserDomainService userDomainService;

    @InjectMocks
    private CreateUserCommand createUserCommand;

    private CreateUserRequest request;
    private User savedUser;

    @Before
    public void setUp() {
        request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPhone("1234567890");

        savedUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("1234567890")
                .status(1)
                .build();
        savedUser.setId(1);
    }

    @Test
    public void execute_shouldReturnUserResponse_whenValidRequest() {
        when(userDomainService.createUser(anyString(), anyString(), anyString())).thenReturn(savedUser);

        UserResponse response = createUserCommand.execute(request);

        assertNotNull(response);
        assertEquals(Integer.valueOf(1), response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("1234567890", response.getPhone());
        assertEquals(Integer.valueOf(1), response.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_shouldThrowException_whenUsernameExists() {
        when(userDomainService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Username already exists: testuser"));

        createUserCommand.execute(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_shouldThrowException_whenEmailExists() {
        when(userDomainService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Email already exists: test@example.com"));

        createUserCommand.execute(request);
    }
}
