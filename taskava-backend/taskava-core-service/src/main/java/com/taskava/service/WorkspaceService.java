package com.taskava.service;

import com.taskava.common.dto.*;
import com.taskava.data.entity.*;
import com.taskava.data.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    @PreAuthorize("@workspaceService.canCreateWorkspace(#request.organizationId, authentication.principal.id)")
    public WorkspaceDTO createWorkspace(CreateWorkspaceRequest request, UUID creatorUserId) {
        log.info("Creating workspace: {} in organization: {} by user: {}", 
                request.getName(), request.getOrganizationId(), creatorUserId);
        
        // Check if workspace name already exists in organization
        if (workspaceRepository.existsByOrganizationIdAndName(request.getOrganizationId(), request.getName())) {
            throw new IllegalArgumentException("Workspace with name already exists in this organization: " + request.getName());
        }
        
        Organization organization = organizationRepository.findActiveById(request.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + request.getOrganizationId()));
        
        // Create workspace
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .visibility(request.getVisibility() != null ? 
                    Workspace.Visibility.valueOf(request.getVisibility()) : 
                    Workspace.Visibility.PRIVATE)
                .organization(organization)
                .build();
        
        workspace = workspaceRepository.save(workspace);
        
        // Add creator as admin
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + creatorUserId));
        
        WorkspaceMember adminMembership = WorkspaceMember.builder()
                .workspace(workspace)
                .user(creator)
                .role(WorkspaceMember.WorkspaceRole.ADMIN)
                .active(true)
                .canCreateProjects(true)
                .canInviteMembers(true)
                .build();
        
        workspaceMemberRepository.save(adminMembership);
        
        return mapToDTO(workspace);
    }

    @PreAuthorize("@workspaceService.canViewWorkspace(#id, authentication.principal.id)")
    @Transactional(readOnly = true)
    public WorkspaceDTO getWorkspace(UUID id) {
        Workspace workspace = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        WorkspaceDTO dto = mapToDTO(workspace);
        dto.setTotalMembers(workspaceRepository.countMembers(id));
        dto.setTotalTeams(workspaceRepository.countActiveTeams(id));
        dto.setTotalProjects(workspaceRepository.countActiveProjects(id));
        
        return dto;
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#id, authentication.principal.id)")
    public WorkspaceDTO updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        log.info("Updating workspace: {}", id);
        
        Workspace workspace = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        // Update fields
        if (request.getName() != null) {
            if (!request.getName().equals(workspace.getName()) && 
                workspaceRepository.existsByOrganizationIdAndName(workspace.getOrganization().getId(), request.getName())) {
                throw new IllegalArgumentException("Workspace name already exists in this organization");
            }
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            workspace.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            workspace.setIcon(request.getIcon());
        }
        if (request.getVisibility() != null) {
            workspace.setVisibility(Workspace.Visibility.valueOf(request.getVisibility()));
        }
        
        workspace = workspaceRepository.save(workspace);
        return mapToDTO(workspace);
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#id, authentication.principal.id)")
    public void deleteWorkspace(UUID id, UUID deletedBy) {
        log.info("Soft deleting workspace: {} by user: {}", id, deletedBy);
        
        Workspace workspace = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        workspace.softDelete(deletedBy);
        workspaceRepository.save(workspace);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public Page<WorkspaceDTO> getUserWorkspaces(UUID userId, Pageable pageable) {
        Page<Workspace> workspaces = workspaceRepository.findByMemberId(userId, pageable);
        return workspaces.map(this::mapToDTO);
    }

    @PreAuthorize("@workspaceService.canViewWorkspace(#organizationId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Page<WorkspaceDTO> getOrganizationWorkspaces(UUID organizationId, Pageable pageable) {
        Page<Workspace> workspaces = workspaceRepository.findByOrganizationId(organizationId, pageable);
        return workspaces.map(this::mapToDTO);
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#workspaceId, authentication.principal.id)")
    public MembershipDTO addMember(UUID workspaceId, AddWorkspaceMemberRequest request, UUID invitedBy) {
        log.info("Adding member to workspace {} with role {}", workspaceId, request.getRole());
        
        // Determine user ID from request (either direct ID or email lookup)
        UUID userId = request.getUserId();
        if (userId == null && request.getEmail() != null) {
            // Try to find user by email
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                userId = user.getId();
            } else {
                // TODO: Send invitation email to non-registered user
                throw new IllegalArgumentException("User with email not found: " + request.getEmail());
            }
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("Either userId or email must be provided");
        }
        
        // Check if user is already a member
        if (workspaceMemberRepository.isActiveMember(workspaceId, userId)) {
            throw new IllegalArgumentException("User is already a member of this workspace");
        }
        
        Workspace workspace = workspaceRepository.findActiveById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));
        
        // Check if user is a member of the organization
        if (!organizationMemberRepository.isActiveMember(workspace.getOrganization().getId(), userId)) {
            throw new IllegalArgumentException("User must be a member of the organization first");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        WorkspaceMember membership = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceMember.WorkspaceRole.valueOf(request.getRole()))
                .invitedBy(invitedBy)
                .active(true)
                .canCreateProjects(request.getCanCreateProjects() != null ? request.getCanCreateProjects() : false)
                .canInviteMembers(request.getCanInviteMembers() != null ? request.getCanInviteMembers() : false)
                .build();
        
        membership = workspaceMemberRepository.save(membership);
        
        return mapMembershipToDTO(membership);
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#workspaceId, authentication.principal.id)")
    public void removeMember(UUID workspaceId, UUID userId) {
        log.info("Removing member {} from workspace {}", userId, workspaceId);
        
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));
        
        // Cannot remove the last admin
        if (membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN) {
            Long adminCount = workspaceMemberRepository.countByWorkspaceIdAndRole(
                    workspaceId, WorkspaceMember.WorkspaceRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot remove the last admin of the workspace");
            }
        }
        
        membership.setActive(false);
        membership.softDelete(UUID.randomUUID()); // Should use current user ID from security context
        workspaceMemberRepository.save(membership);
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#workspaceId, authentication.principal.id)")
    public MembershipDTO updateMemberRole(UUID workspaceId, UUID userId, UpdateWorkspaceMemberRoleRequest request) {
        log.info("Updating member {} role in workspace {} to {}", userId, workspaceId, request.getRole());
        
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));
        
        WorkspaceMember.WorkspaceRole role = WorkspaceMember.WorkspaceRole.valueOf(request.getRole());
        
        // Cannot remove the last admin
        if (membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN && 
            role != WorkspaceMember.WorkspaceRole.ADMIN) {
            Long adminCount = workspaceMemberRepository.countByWorkspaceIdAndRole(
                    workspaceId, WorkspaceMember.WorkspaceRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot remove the last admin of the workspace");
            }
        }
        
        membership.setRole(role);
        
        // Update permissions
        if (request.getCanCreateProjects() != null) {
            membership.setCanCreateProjects(request.getCanCreateProjects());
        } else {
            // Set defaults based on role if not explicitly provided
            if (role == WorkspaceMember.WorkspaceRole.ADMIN) {
                membership.setCanCreateProjects(true);
                membership.setCanInviteMembers(true);
            } else if (role == WorkspaceMember.WorkspaceRole.GUEST) {
                membership.setCanCreateProjects(false);
                membership.setCanInviteMembers(false);
            }
        }
        
        if (request.getCanInviteMembers() != null) {
            membership.setCanInviteMembers(request.getCanInviteMembers());
        }
        
        membership = workspaceMemberRepository.save(membership);
        
        return mapMembershipToDTO(membership);
    }

    @PreAuthorize("@workspaceService.canViewWorkspace(#workspaceId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Page<MembershipDTO> getWorkspaceMembers(UUID workspaceId, Pageable pageable) {
        Page<WorkspaceMember> members = workspaceMemberRepository.findActiveByWorkspaceId(workspaceId, pageable);
        return members.map(this::mapMembershipToDTO);
    }

    // Helper methods for security checks
    public boolean canCreateWorkspace(UUID organizationId, UUID userId) {
        // Check if user is an organization member with appropriate role
        return organizationMemberRepository.getUserRole(organizationId, userId)
                .map(role -> role != OrganizationMember.OrganizationRole.GUEST)
                .orElse(false);
    }

    public boolean canViewWorkspace(UUID workspaceId, UUID userId) {
        Workspace workspace = workspaceRepository.findActiveById(workspaceId).orElse(null);
        if (workspace == null) return false;
        
        // Public workspaces can be viewed by anyone
        if (workspace.getVisibility() == Workspace.Visibility.PUBLIC) {
            return true;
        }
        
        // Organization-visible workspaces can be viewed by org members
        if (workspace.getVisibility() == Workspace.Visibility.ORGANIZATION) {
            return organizationMemberRepository.isActiveMember(workspace.getOrganization().getId(), userId);
        }
        
        // Private workspaces require workspace membership
        return workspaceMemberRepository.isActiveMember(workspaceId, userId);
    }

    public boolean canManageWorkspace(UUID workspaceId, UUID userId) {
        return workspaceMemberRepository.getUserRole(workspaceId, userId)
                .map(role -> role == WorkspaceMember.WorkspaceRole.ADMIN)
                .orElse(false);
    }

    private WorkspaceDTO mapToDTO(Workspace workspace) {
        return WorkspaceDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .color(workspace.getColor())
                .icon(workspace.getIcon())
                .visibility(workspace.getVisibility().toString())
                .organizationId(workspace.getOrganization().getId())
                .organizationName(workspace.getOrganization().getName())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .createdBy(workspace.getCreatedBy())
                .updatedBy(workspace.getUpdatedBy())
                .build();
    }

    private MembershipDTO mapMembershipToDTO(WorkspaceMember membership) {
        return MembershipDTO.builder()
                .id(membership.getId())
                .userId(membership.getUser().getId())
                .userEmail(membership.getUser().getEmail())
                .userName(membership.getUser().getFullName())
                .userAvatar(membership.getUser().getAvatarUrl())
                .role(membership.getRole().toString())
                .entityId(membership.getWorkspace().getId())
                .entityType("WORKSPACE")
                .entityName(membership.getWorkspace().getName())
                .invitedBy(membership.getInvitedBy())
                .invitedAt(membership.getInvitedAt())
                .joinedAt(membership.getJoinedAt())
                .active(membership.isActive())
                .canCreateProjects(membership.isCanCreateProjects())
                .canInviteMembers(membership.isCanInviteMembers())
                .build();
    }
    
    // Additional methods for enhanced functionality
    
    @PreAuthorize("@workspaceService.canViewWorkspace(#id, authentication.principal.id)")
    @Transactional(readOnly = true)
    public WorkspaceStatisticsDTO getWorkspaceStatistics(UUID id) {
        log.info("Fetching statistics for workspace: {}", id);
        
        Workspace workspace = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        return WorkspaceStatisticsDTO.builder()
                .workspaceId(id)
                .workspaceName(workspace.getName())
                .totalMembers(workspaceRepository.countMembers(id))
                .totalTeams(workspaceRepository.countActiveTeams(id))
                .totalProjects(workspaceRepository.countActiveProjects(id))
                .activeProjects(workspaceRepository.countActiveProjects(id)) // TODO: Implement separate count for active vs archived
                .completedTasks(0L) // TODO: Implement task counting
                .pendingTasks(0L) // TODO: Implement task counting
                .overdueTasks(0L) // TODO: Implement task counting
                .build();
    }
    
    @PreAuthorize("@workspaceService.canViewWorkspace(#workspaceId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Page<ActivityDTO> getWorkspaceActivity(UUID workspaceId, Pageable pageable) {
        log.info("Fetching activity for workspace: {}", workspaceId);
        // TODO: Implement activity tracking and retrieval
        return Page.empty(pageable);
    }
    
    @PreAuthorize("@workspaceService.canManageWorkspace(#id, authentication.principal.id)")
    public void archiveWorkspace(UUID id, UUID archivedBy) {
        log.info("Archiving workspace: {} by user: {}", id, archivedBy);
        
        Workspace workspace = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        workspace.setArchived(true);
        workspace.setArchivedAt(java.time.LocalDateTime.now());
        workspace.setArchivedBy(archivedBy);
        workspaceRepository.save(workspace);
    }
    
    @PreAuthorize("@workspaceService.canManageWorkspace(#id, authentication.principal.id)")
    public WorkspaceDTO restoreWorkspace(UUID id, UUID restoredBy) {
        log.info("Restoring workspace: {} by user: {}", id, restoredBy);
        
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        if (!workspace.isArchived()) {
            throw new IllegalArgumentException("Workspace is not archived");
        }
        
        workspace.setArchived(false);
        workspace.setArchivedAt(null);
        workspace.setArchivedBy(null);
        workspace = workspaceRepository.save(workspace);
        
        return mapToDTO(workspace);
    }
    
    @PreAuthorize("@workspaceService.canManageWorkspace(#id, authentication.principal.id)")
    public WorkspaceDTO duplicateWorkspace(UUID id, String newName, UUID duplicatedBy) {
        log.info("Duplicating workspace: {} with name: {} by user: {}", id, newName, duplicatedBy);
        
        Workspace original = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        // Check if new name already exists
        if (workspaceRepository.existsByOrganizationIdAndName(original.getOrganization().getId(), newName)) {
            throw new IllegalArgumentException("Workspace with name already exists: " + newName);
        }
        
        // Create duplicate
        Workspace duplicate = Workspace.builder()
                .name(newName)
                .description(original.getDescription() + " (Copy)")
                .color(original.getColor())
                .icon(original.getIcon())
                .visibility(original.getVisibility())
                .organization(original.getOrganization())
                .build();
        
        duplicate = workspaceRepository.save(duplicate);
        
        // Add creator as admin
        User creator = userRepository.findById(duplicatedBy)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + duplicatedBy));
        
        WorkspaceMember adminMembership = WorkspaceMember.builder()
                .workspace(duplicate)
                .user(creator)
                .role(WorkspaceMember.WorkspaceRole.ADMIN)
                .active(true)
                .canCreateProjects(true)
                .canInviteMembers(true)
                .build();
        
        workspaceMemberRepository.save(adminMembership);
        
        // TODO: Optionally duplicate teams, projects, and custom fields
        
        return mapToDTO(duplicate);
    }
}