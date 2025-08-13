package com.taskava.security.service;

import com.taskava.data.entity.OrganizationMember;
import com.taskava.data.entity.WorkspaceMember;
import com.taskava.data.entity.TeamMember;
import com.taskava.data.entity.Project;
import com.taskava.data.entity.Task;
import com.taskava.data.repository.OrganizationMemberRepository;
import com.taskava.data.repository.WorkspaceMemberRepository;
import com.taskava.data.repository.TeamMemberRepository;
import com.taskava.data.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service providing custom security expressions for @PreAuthorize annotations.
 * These methods can be referenced in controllers using SpEL expressions like:
 * @PreAuthorize("@securityExpressionService.hasOrganizationAccess(#organizationId, authentication)")
 */
@Slf4j
@Service("securityExpressionService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityExpressionService {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;

    /**
     * Check if user has any access to an organization
     */
    public boolean hasOrganizationAccess(UUID organizationId, Authentication authentication) {
        if (organizationId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        return organizationMemberRepository.isActiveMember(organizationId, userId);
    }

    /**
     * Check if user is an admin of the organization
     */
    public boolean isOrganizationAdmin(UUID organizationId, Authentication authentication) {
        if (organizationId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .map(member -> member.getRole() == OrganizationMember.OrganizationRole.ADMIN || 
                               member.getRole() == OrganizationMember.OrganizationRole.OWNER)
                .orElse(false);
    }

    /**
     * Check if user is the owner of the organization
     */
    public boolean isOrganizationOwner(UUID organizationId, Authentication authentication) {
        if (organizationId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .map(member -> member.getRole() == OrganizationMember.OrganizationRole.OWNER)
                .orElse(false);
    }

    /**
     * Check if user has any access to a workspace
     */
    public boolean hasWorkspaceAccess(UUID workspaceId, Authentication authentication) {
        if (workspaceId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        return workspaceMemberRepository.isActiveMember(workspaceId, userId);
    }

    /**
     * Check if user is an admin of the workspace
     */
    public boolean isWorkspaceAdmin(UUID workspaceId, Authentication authentication) {
        if (workspaceId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> member.getRole() == WorkspaceMember.WorkspaceRole.ADMIN)
                .orElse(false);
    }

    /**
     * Check if user can modify workspace (admin or owner)
     */
    public boolean canModifyWorkspace(UUID workspaceId, Authentication authentication) {
        return isWorkspaceAdmin(workspaceId, authentication);
    }

    /**
     * Check if user has access to a team
     */
    public boolean hasTeamAccess(UUID teamId, Authentication authentication) {
        if (teamId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        // Check if user is a member of the team
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId).isPresent();
    }

    /**
     * Check if user is a team admin or lead
     */
    public boolean isTeamAdmin(UUID teamId, Authentication authentication) {
        if (teamId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .map(member -> member.getRole() == TeamMember.TeamRole.LEAD)
                .orElse(false);
    }

    /**
     * Check if user has access to a project
     * User has access if they are a member of the workspace or team that owns the project
     */
    public boolean hasProjectAccess(UUID projectId, Authentication authentication) {
        if (projectId == null || authentication == null) {
            return false;
        }
        
        UUID userId = getUserId(authentication);
        
        // Check if user has access through workspace or team membership
        return projectRepository.findById(projectId)
                .map(project -> {
                    // Check workspace access
                    if (project.getWorkspace() != null && 
                        hasWorkspaceAccess(project.getWorkspace().getId(), authentication)) {
                        return true;
                    }
                    // Check team access if project is associated with a team
                    if (project.getTeam() != null) {
                        return hasTeamAccess(project.getTeam().getId(), authentication);
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Check if user can modify a project
     * User can modify if they are workspace/team admin or project owner
     */
    public boolean canModifyProject(UUID projectId, Authentication authentication) {
        if (projectId == null || authentication == null) {
            return false;
        }
        
        return projectRepository.findById(projectId)
                .map(project -> {
                    // Check if user is workspace admin
                    if (project.getWorkspace() != null && 
                        isWorkspaceAdmin(project.getWorkspace().getId(), authentication)) {
                        return true;
                    }
                    // Check if user is team admin for team projects
                    if (project.getTeam() != null) {
                        return isTeamAdmin(project.getTeam().getId(), authentication);
                    }
                    // Check if user is the creator of the project
                    UUID userId = getUserId(authentication);
                    return project.getCreatedBy() != null && project.getCreatedBy().equals(userId);
                })
                .orElse(false);
    }

    /**
     * Check if user has access to a task through workspace membership
     * Note: Task-level security is typically handled through workspace access
     * since we don't have TaskRepository injected here
     */
    public boolean hasTaskAccess(UUID workspaceId, Authentication authentication) {
        if (workspaceId == null || authentication == null) {
            return false;
        }
        
        // For now, task access is determined by workspace membership
        return hasWorkspaceAccess(workspaceId, authentication);
    }

    /**
     * Check if user can modify a task in a workspace
     * User can modify if they have workspace access
     */
    public boolean canModifyTask(UUID workspaceId, Authentication authentication) {
        if (workspaceId == null || authentication == null) {
            return false;
        }
        
        // For now, users can modify tasks if they have workspace access
        return hasWorkspaceAccess(workspaceId, authentication);
    }

    /**
     * Check if user can invite members to organization
     */
    public boolean canInviteToOrganization(UUID organizationId, Authentication authentication) {
        return isOrganizationAdmin(organizationId, authentication);
    }

    /**
     * Check if user can invite members to workspace
     */
    public boolean canInviteToWorkspace(UUID workspaceId, Authentication authentication) {
        return isWorkspaceAdmin(workspaceId, authentication);
    }

    /**
     * Check if user can invite members to team
     */
    public boolean canInviteToTeam(UUID teamId, Authentication authentication) {
        return isTeamAdmin(teamId, authentication);
    }

    /**
     * Check if user can manage workspace custom fields
     */
    public boolean canManageCustomFields(UUID workspaceId, Authentication authentication) {
        return isWorkspaceAdmin(workspaceId, authentication);
    }

    /**
     * Extract user ID from authentication
     */
    private UUID getUserId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID in authentication principal: {}", authentication.getName());
            return null;
        }
    }
}