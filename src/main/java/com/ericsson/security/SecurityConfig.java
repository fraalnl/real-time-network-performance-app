package com.ericsson.security;

import com.ericsson.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration  // Spring can process it during startup
@EnableMethodSecurity // @PreAuthorize so you can control access on service methods
public class SecurityConfig {
	private final JwtUtil jwtUtil;
	private final JwtUserDetailsService jwtUserDetailsService;
	
	public SecurityConfig(JwtUtil jwtUtil, JwtUserDetailsService jwtUserDetailsService) {
		this.jwtUtil = jwtUtil;
		this.jwtUserDetailsService = jwtUserDetailsService;
	}

// provides a BCrypt-based encoder to hash passwords securely. When users log in, 
// password will be compared with the stored (hashed) password.
// Spring automatically injects this bean wherever a PasswordEncoder is required
    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		// Disable CSRF for simplicity in a REST API
		.csrf(AbstractHttpConfigurer::disable)
		// Configures the application to be stateless. Instead of using server-side sessions, 
		// each request is authenticated using the JWT token provided by the client.
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authorizeHttpRequests(auth -> auth
				// Permit access to static resources and authentication endpoints
				.requestMatchers(
						"/", 
						"/index.html", 
						"/app.js", 
						"/favicon.ico", 
						"/css/**", 
						"/js/**", 
						"/images/**", 
						"/api/auth/**"
						).permitAll()
				// All other endpoints require authentication with a token
				.anyRequest().authenticated()
				)
		.httpBasic(AbstractHttpConfigurer::disable) //Disabling Default Login Mechanisms
		.formLogin(AbstractHttpConfigurer::disable);

		// Add custom JWT authentication filter before the default username/password filter
		/*
		 * if a valid JWT is present, the request is authenticated without triggering the default mechanism 
		 * (which would otherwise look for username/password credentials). 
		 * This integration allows your application to support token-based (stateless) authentication 
		 * rather than relying solely on session-based or form-based methods
		 */
		http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, jwtUserDetailsService),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}