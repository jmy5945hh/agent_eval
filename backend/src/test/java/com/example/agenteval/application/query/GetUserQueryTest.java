package com.example.agenteval.application.query;

import com.example.agenteval.application.dto.UserResponse;
import com.example.agenteval.domain.model.User;
import com.example.agenteval.domain.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetUserQueryTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserQuery getUserQuery;

    private User user;

    @Before
    public void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("1234567890")
                .status(1)
                .build();
        user.setId(1L);
    }

    @Test
    public void execute_shouldReturnUserResponse_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserResponse> response = getUserQuery.execute(1L);

        assertTrue(response.isPresent());
        assertEquals(Long.valueOf(1L), response.get().getId());
        assertEquals("testuser", response.get().getUsername());
        assertEquals("test@example.com", response.get().getEmail());
    }

    @Test
    public void execute_shouldReturnEmpty_whenUserNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<UserResponse> response = getUserQuery.execute(999L);

        assertFalse(response.isPresent());
    }
}
