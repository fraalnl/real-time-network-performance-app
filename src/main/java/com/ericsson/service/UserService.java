package com.ericsson.service;

import com.ericsson.repository.UserRepository;
import com.ericsson.dto.EngineerDto;
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

	//Creates an account and returns a JWT token
    public String createEngineer(final EngineerDto dto) {
        EngineerEntity user = new EngineerEntity();
        user.setUsername(dto.getUsername());
        // Hash Passwords Before Storing in the Database
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_ENGINEER");

        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}
