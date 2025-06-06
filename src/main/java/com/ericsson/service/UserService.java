package com.ericsson.service;

import com.ericsson.repository.UserRepository;
import com.ericsson.dto.EngineerDto;
import com.ericsson.exception.InvalidUserInputException;
import com.ericsson.exception.UserAlreadyExistsException;
import com.ericsson.exception.UserCreationException;
import com.ericsson.model.EngineerEntity;
import com.ericsson.util.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(final UserRepository userRepository,
                       final PasswordEncoder passwordEncoder,
                       final JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    public String createEngineer(final EngineerDto dto) {
    	if (userRepository.existsByUsername(dto.getUsername())) {
    	    throw new UserAlreadyExistsException("Username '" + dto.getUsername() + "' already exists");
    	}

    	// Validate input
    	if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
    	    throw new InvalidUserInputException("Username cannot be empty");
    	}

    	if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
    	    throw new InvalidUserInputException("Password cannot be empty");
    	}

        // Create new user
        EngineerEntity user = new EngineerEntity();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_ENGINEER");

        try {
            userRepository.save(user);
            return jwtUtil.generateToken(user.getUsername(), user.getRole());
        } catch (Exception e) {
            throw new UserCreationException("Failed to create user: " + e.getMessage(), e);
        }
    }
}
