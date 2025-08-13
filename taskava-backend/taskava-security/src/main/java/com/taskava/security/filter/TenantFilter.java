package com.taskava.security.filter;

import com.taskava.security.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to extract and set tenant context from request headers or JWT claims.
 * This filter runs after authentication to ensure we have user context.
 */
@Slf4j
@Component
@Order(2) // Run after JWT authentication filter
public class TenantFilter extends OncePerRequestFilter {

    private static final String WORKSPACE_HEADER = "X-Workspace-Id";
    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract workspace ID from header
            String workspaceId = request.getHeader(WORKSPACE_HEADER);
            if (workspaceId != null) {
                try {
                    TenantContext.setCurrentWorkspace(UUID.fromString(workspaceId));
                    log.debug("Set workspace context from header: {}", workspaceId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid workspace ID in header: {}", workspaceId);
                }
            }
            
            // Extract organization ID from header
            String organizationId = request.getHeader(ORGANIZATION_HEADER);
            if (organizationId != null) {
                try {
                    TenantContext.setCurrentOrganization(UUID.fromString(organizationId));
                    log.debug("Set organization context from header: {}", organizationId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid organization ID in header: {}", organizationId);
                }
            }
            
            // Extract user ID from authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    String userId = authentication.getName();
                    if (userId != null && !userId.equals("anonymousUser")) {
                        TenantContext.setCurrentUser(UUID.fromString(userId));
                        log.debug("Set user context from authentication: {}", userId);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid user ID in authentication: {}", authentication.getName());
                }
            }
            
            // Process the request
            filterChain.doFilter(request, response);
            
        } finally {
            // Clear context after request processing
            TenantContext.clear();
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't filter public endpoints
        String path = request.getServletPath();
        return path.startsWith("/api/public/") || 
               path.startsWith("/api/auth/") ||
               path.startsWith("/health") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}