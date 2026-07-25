package com.eligibilitygpt.core.service;

import com.eligibilitygpt.core.dto.AuthResponse;
import com.eligibilitygpt.core.dto.LoginRequest;
import com.eligibilitygpt.core.dto.RegisterRequest;
import com.eligibilitygpt.core.exception.InvalidCredentialsException;
import com.eligibilitygpt.core.exception.UserAlreadyExistsException;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.repository.UserRepository;
import com.eligibilitygpt.core.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User newUser = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(savedUser.getEmail());
        Long expirationSeconds = 24 * 60 * 60L; // 24 hours

        return new AuthResponse(
            accessToken,
            null, // refresh token - implement later if needed
            expirationSeconds,
            savedUser.getEmail(),
            savedUser.getFullName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(user.getEmail());
        Long expirationSeconds = 24 * 60 * 60L; // 24 hours

        return new AuthResponse(
            accessToken,
            null, // refresh token - implement later if needed
            expirationSeconds,
            user.getEmail(),
            user.getFullName()
        );
    }
}