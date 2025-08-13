package com.taskava.api.controller;

import com.taskava.data.entity.User;
import com.taskava.security.dto.JwtAuthenticationResponse;
import com.taskava.security.dto.LoginRequest;
import com.taskava.security.dto.RefreshTokenRequest;
import com.taskava.security.dto.SignUpRequest;
import com.taskava.security.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    @Operation(summary = "Authenticate user", description = "Login with username/email and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());
        
        try {
            JwtAuthenticationResponse response = authenticationService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsernameOrEmail(), e);
            throw e;
        }
    }
    
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Bad request or user already exists")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Registration attempt for email: {}", signUpRequest.getEmail());
        
        try {
            User user = authenticationService.registerUser(signUpRequest);
            
            ApiResponseDto response = ApiResponseDto.builder()
                    .success(true)
                    .message("User registered successfully. Please verify your email.")
                    .data(UserResponseDto.from(user))
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed for email: {}", signUpRequest.getEmail(), e);
            throw e;
        }
    }
    
    @Operation(summary = "Refresh access token", description = "Get a new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token successfully refreshed"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.debug("Token refresh request");
        
        try {
            JwtAuthenticationResponse response = authenticationService.refreshToken(refreshTokenRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }
    
    @Operation(summary = "Logout user", description = "Logout current user and invalidate tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully logged out")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto> logoutUser() {
        authenticationService.logout();
        
        ApiResponseDto response = ApiResponseDto.builder()
                .success(true)
                .message("User logged out successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Verify email", description = "Verify user email with token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email successfully verified"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponseDto> verifyEmail(@RequestParam String token) {
        // TODO: Implement email verification logic
        ApiResponseDto response = ApiResponseDto.builder()
                .success(true)
                .message("Email verified successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Request password reset", description = "Send password reset email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto> forgotPassword(@RequestParam String email) {
        // TODO: Implement password reset logic
        ApiResponseDto response = ApiResponseDto.builder()
                .success(true)
                .message("Password reset instructions sent to your email")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Reset password", description = "Reset password with token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password successfully reset"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        // TODO: Implement password reset logic
        ApiResponseDto response = ApiResponseDto.builder()
                .success(true)
                .message("Password reset successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Helper DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiResponseDto {
        private boolean success;
        private String message;
        private Object data;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UserResponseDto {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String status;
        
        public static UserResponseDto from(User user) {
            return UserResponseDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .status(user.getStatus().name())
                    .build();
        }
    }
}