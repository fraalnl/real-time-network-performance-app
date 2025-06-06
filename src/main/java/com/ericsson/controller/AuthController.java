package com.ericsson.controller;

import com.ericsson.dto.LoginRequestDto;
import com.ericsson.security.JwtUserDetailsService;
import com.ericsson.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder; //PasswordEncoder is part of Spring Security

    public AuthController(JwtUserDetailsService jwtUserDetailsService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(loginRequest.getUsername());
            if (passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
                // if the user has ROLE_ADMIN, assign admin; otherwise, engineer.
                String role;
                if (userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                    role = "ROLE_ADMIN";
                } else {
                    role = "ROLE_ENGINEER";
                }
                String token = jwtUtil.generateToken(userDetails.getUsername(), role);
                return ResponseEntity.ok(Map.of("token", token, "role", role));
            } else {//If the password does not match, it returns an HTTP 401 (Unauthorized) response
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials. Please try again."));
            }
        } catch (UsernameNotFoundException e) { //If the username does not exist
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials. Please try again."));
        }
    }
}
