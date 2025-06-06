package com.ericsson.security;

import com.ericsson.model.EngineerEntity;
import com.ericsson.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private JwtUserDetailsService jwtUserDetailsService;

    private final String adminBcryptPassword = "$2a$10$UuFEAl3WP8LGU6Tu7I0COuvyelyGVExd58J0yLA/cwkFv2m4bwaTu";

    @BeforeEach
    void setUp() {
        // Manually create service and inject the admin bcrypt password
        jwtUserDetailsService = new JwtUserDetailsService(adminBcryptPassword, userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnAdminUser_whenUsernameIsAdmin() {
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername("admin");

        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getPassword().startsWith("$2a$10$")); // bcrypt check
    }

    @Test
    void loadUserByUsername_shouldReturnEngineerUser_whenUserExists() {
        EngineerEntity engineer = new EngineerEntity();
        engineer.setUsername("engineer1");
        engineer.setPassword("hashedPass");
        engineer.setRole("ROLE_ENGINEER");

        when(userRepository.findByUsername("engineer1")).thenReturn(Optional.of(engineer));

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername("engineer1");

        assertEquals("engineer1", userDetails.getUsername());
        assertEquals("hashedPass", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_ENGINEER", userDetails.getAuthorities().iterator().next().getAuthority());

        verify(userRepository, times(1)).findByUsername("engineer1");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () ->
                jwtUserDetailsService.loadUserByUsername("unknown"));

        assertEquals("User not found with username: unknown", ex.getMessage());
        verify(userRepository).findByUsername("unknown");
    }
}
