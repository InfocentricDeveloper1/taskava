package com.taskava.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    
    private JwtTokenProvider tokenProvider;
    
    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        // Set test values using reflection
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", 
            "dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1qd3QtdG9rZW5z");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpirationMs", 86400000L);
    }
    
    @Test
    void testGenerateAndValidateToken() {
        // Create a test user principal
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .organizationId(orgId)
                .authorities(Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
                ))
                .build();
        
        // Generate token
        String token = tokenProvider.generateAccessToken(userPrincipal);
        assertNotNull(token);
        
        // Validate token
        assertTrue(tokenProvider.validateToken(token));
        
        // Extract claims
        assertEquals("testuser", tokenProvider.getUsernameFromToken(token));
        assertEquals(userId.toString(), tokenProvider.getUserIdFromToken(token));
        assertEquals(orgId.toString(), tokenProvider.getOrganizationIdFromToken(token));
        
        // Check authorities
        var authorities = tokenProvider.getAuthoritiesFromToken(token);
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }
    
    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(tokenProvider.validateToken(invalidToken));
    }
    
    @Test
    void testRefreshToken() {
        UUID userId = UUID.randomUUID();
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userId)
                .username("testuser")
                .build();
        
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);
        assertNotNull(refreshToken);
        assertTrue(tokenProvider.validateToken(refreshToken));
        
        // Verify the refresh token contains the user ID
        assertEquals(userId.toString(), tokenProvider.getUserIdFromToken(refreshToken));
    }
}