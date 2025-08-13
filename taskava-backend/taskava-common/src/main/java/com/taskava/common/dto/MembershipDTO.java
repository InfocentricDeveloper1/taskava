package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MembershipDTO {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private String userEmail;
    private String userName;
    private String userAvatar;
    
    @NotNull(message = "Role is required")
    private String role;
    
    private UUID entityId;  // Can be organizationId, workspaceId, or teamId
    private String entityType;  // ORGANIZATION, WORKSPACE, or TEAM
    private String entityName;
    
    private UUID invitedBy;
    private String invitedByName;
    private Instant invitedAt;
    private Instant joinedAt;
    private boolean active;
    
    // Workspace-specific permissions
    private Boolean canCreateProjects;
    private Boolean canInviteMembers;
}