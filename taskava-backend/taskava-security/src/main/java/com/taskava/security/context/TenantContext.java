package com.taskava.security.context;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * ThreadLocal context for managing tenant (workspace) isolation in multi-tenant architecture.
 * This ensures that all database queries are automatically filtered by the current workspace.
 */
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<UUID> currentWorkspace = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentOrganization = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentUser = new ThreadLocal<>();
    
    /**
     * Set the current workspace ID for this thread
     */
    public static void setCurrentWorkspace(UUID workspaceId) {
        log.debug("Setting workspace context: {}", workspaceId);
        currentWorkspace.set(workspaceId);
    }
    
    /**
     * Get the current workspace ID for this thread
     */
    public static UUID getCurrentWorkspace() {
        return currentWorkspace.get();
    }
    
    /**
     * Get the current workspace ID for this thread (alias for getCurrentWorkspace)
     */
    public static UUID getCurrentWorkspaceId() {
        return currentWorkspace.get();
    }
    
    /**
     * Set the current organization ID for this thread
     */
    public static void setCurrentOrganization(UUID organizationId) {
        log.debug("Setting organization context: {}", organizationId);
        currentOrganization.set(organizationId);
    }
    
    /**
     * Get the current organization ID for this thread
     */
    public static UUID getCurrentOrganization() {
        return currentOrganization.get();
    }
    
    /**
     * Set the current user ID for this thread
     */
    public static void setCurrentUser(UUID userId) {
        log.debug("Setting user context: {}", userId);
        currentUser.set(userId);
    }
    
    /**
     * Get the current user ID for this thread
     */
    public static UUID getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * Get the current user ID for this thread (alias for getCurrentUser)
     */
    public static UUID getCurrentUserId() {
        return currentUser.get();
    }
    
    /**
     * Clear all context for this thread
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        currentWorkspace.remove();
        currentOrganization.remove();
        currentUser.remove();
    }
    
    /**
     * Check if workspace context is set
     */
    public static boolean hasWorkspaceContext() {
        return currentWorkspace.get() != null;
    }
    
    /**
     * Check if organization context is set
     */
    public static boolean hasOrganizationContext() {
        return currentOrganization.get() != null;
    }
    
    /**
     * Check if user context is set
     */
    public static boolean hasUserContext() {
        return currentUser.get() != null;
    }
}