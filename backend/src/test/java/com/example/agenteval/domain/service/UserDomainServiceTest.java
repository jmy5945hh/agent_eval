package com.example.agenteval.domain.service;

import com.example.agenteval.domain.model.User;
import com.example.agenteval.domain.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDomainService userDomainService;

    private User user;

    @Before
    public void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("1234567890")
                .status(1)
                .build();
        user.setId(1);
    }

    @Test
    public void createUser_shouldReturnUser_whenValidData() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userDomainService.createUser("testuser", "test@example.com", "1234567890");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUser_shouldThrowException_whenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        userDomainService.createUser("testuser", "test@example.com", "1234567890");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUser_shouldThrowException_whenEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        userDomainService.createUser("testuser", "test@example.com", "1234567890");
    }
}
