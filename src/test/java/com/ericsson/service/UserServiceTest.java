package com.ericsson.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ericsson.dto.EngineerDto;
import com.ericsson.model.EngineerEntity;
import com.ericsson.repository.UserRepository;
import com.ericsson.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private EngineerDto engineerDto;

    @BeforeEach
    void setUp() {
        engineerDto = new EngineerDto();
        engineerDto.setUsername("testuser");
        engineerDto.setPassword("password123");
    }

    @Test
    void testCreateEngineer_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(jwtUtil.generateToken("testuser", "ROLE_ENGINEER")).thenReturn("dummyToken");

        String token = userService.createEngineer(engineerDto);

        ArgumentCaptor<EngineerEntity> captor = ArgumentCaptor.forClass(EngineerEntity.class);
        verify(userRepository, times(1)).save(captor.capture());

        EngineerEntity saved = captor.getValue();
        assertEquals("testuser", saved.getUsername());
        assertEquals("encodedPassword123", saved.getPassword());
        assertEquals("ROLE_ENGINEER", saved.getRole());

        assertEquals("dummyToken", token);
    }

    @Test
    void testCreateEngineer_UsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createEngineer(engineerDto));

        assertEquals("Username 'testuser' already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateEngineer_UsernameEmpty() {
        engineerDto.setUsername("  ");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createEngineer(engineerDto));

        assertEquals("Username cannot be empty", ex.getMessage());
    }

    @Test
    void testCreateEngineer_PasswordEmpty() {
        engineerDto.setPassword("");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createEngineer(engineerDto));

        assertEquals("Password cannot be empty", ex.getMessage());
    }

    @Test
    void testCreateEngineer_SaveFails() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        doThrow(new RuntimeException("DB failure")).when(userRepository).save(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createEngineer(engineerDto));

        assertEquals("Failed to create user: DB failure", ex.getMessage());
    }
}
