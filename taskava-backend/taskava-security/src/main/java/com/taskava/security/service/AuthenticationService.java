package com.taskava.security.service;

import com.taskava.data.entity.User;
import com.taskava.data.repository.UserRepository;
import com.taskava.security.dto.JwtAuthenticationResponse;
import com.taskava.security.dto.LoginRequest;
import com.taskava.security.dto.RefreshTokenRequest;
import com.taskava.security.dto.SignUpRequest;
import com.taskava.security.jwt.JwtTokenProvider;
import com.taskava.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    @Transactional
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Update last login time
        userRepository.updateLastLoginTime(userPrincipal.getId(), Instant.now());
        
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);
        
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .organizationId(userPrincipal.getOrganizationId())
                .build();
    }
    
    @Transactional
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String userIdStr = tokenProvider.getUserIdFromToken(refreshToken);
        UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        
        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);
        String newRefreshToken = tokenProvider.generateRefreshToken(userPrincipal);
        
        return JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .organizationId(userPrincipal.getOrganizationId())
                .build();
    }
    
    @Transactional
    public User registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        
        if (userRepository.existsByUsernameAndOrganizationId(
                signUpRequest.getUsername(), signUpRequest.getOrganizationId())) {
            throw new RuntimeException("Username is already taken in this organization!");
        }
        
        // Create new user
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .status(User.UserStatus.PENDING_VERIFICATION)
                .roles(getDefaultRoles())
                .build();
        
        // Note: Organization should be set based on the signup flow
        // This is a simplified version
        
        return userRepository.save(user);
    }
    
    private Set<User.UserRole> getDefaultRoles() {
        Set<User.UserRole> roles = new HashSet<>();
        roles.add(User.UserRole.ORGANIZATION_MEMBER);
        return roles;
    }
    
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}