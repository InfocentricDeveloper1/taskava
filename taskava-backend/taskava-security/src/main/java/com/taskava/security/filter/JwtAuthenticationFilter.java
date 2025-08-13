package com.taskava.security.filter;

import com.taskava.security.jwt.JwtTokenProvider;
import com.taskava.security.jwt.UserPrincipal;
import com.taskava.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userIdStr = tokenProvider.getUserIdFromToken(jwt);
                String username = tokenProvider.getUsernameFromToken(jwt);
                String organizationIdStr = tokenProvider.getOrganizationIdFromToken(jwt);
                String workspaceIdStr = tokenProvider.getWorkspaceIdFromToken(jwt);
                List<String> authorities = tokenProvider.getAuthoritiesFromToken(jwt);
                
                UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;
                UUID organizationId = organizationIdStr != null ? UUID.fromString(organizationIdStr) : null;
                UUID workspaceId = workspaceIdStr != null ? UUID.fromString(workspaceIdStr) : null;
                
                // Create UserPrincipal from token claims
                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .id(userId)
                        .username(username)
                        .organizationId(organizationId)
                        .currentWorkspaceId(workspaceId)
                        .authorities(authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()))
                        .build();
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Set tenant context for multi-tenancy
                if (workspaceId != null) {
                    request.setAttribute("workspaceId", workspaceId);
                }
                request.setAttribute("organizationId", organizationId);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}