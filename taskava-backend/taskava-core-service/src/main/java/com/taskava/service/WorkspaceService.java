package com.taskava.service;

import com.taskava.common.dto.WorkspaceDTO;
import com.taskava.common.dto.MembershipDTO;
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

    @PreAuthorize("@workspaceService.canCreateWorkspace(#dto.organizationId, authentication.principal.id)")
    public WorkspaceDTO createWorkspace(WorkspaceDTO dto, UUID creatorUserId) {
        log.info("Creating workspace: {} in organization: {} by user: {}", 
                dto.getName(), dto.getOrganizationId(), creatorUserId);
        
        // Check if workspace name already exists in organization
        if (workspaceRepository.existsByOrganizationIdAndName(dto.getOrganizationId(), dto.getName())) {
            throw new IllegalArgumentException("Workspace with name already exists in this organization: " + dto.getName());
        }
        
        Organization organization = organizationRepository.findActiveById(dto.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + dto.getOrganizationId()));
        
        // Create workspace
        Workspace workspace = Workspace.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .visibility(dto.getVisibility() != null ? 
                    Workspace.Visibility.valueOf(dto.getVisibility()) : 
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
    public WorkspaceDTO updateWorkspace(UUID id, WorkspaceDTO dto) {
        log.info("Updating workspace: {}", id);
        
        Workspace workspace = workspaceRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + id));
        
        // Update fields
        if (dto.getName() != null) {
            if (!dto.getName().equals(workspace.getName()) && 
                workspaceRepository.existsByOrganizationIdAndName(workspace.getOrganization().getId(), dto.getName())) {
                throw new IllegalArgumentException("Workspace name already exists in this organization");
            }
            workspace.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            workspace.setDescription(dto.getDescription());
        }
        if (dto.getColor() != null) {
            workspace.setColor(dto.getColor());
        }
        if (dto.getIcon() != null) {
            workspace.setIcon(dto.getIcon());
        }
        if (dto.getVisibility() != null) {
            workspace.setVisibility(Workspace.Visibility.valueOf(dto.getVisibility()));
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
    public MembershipDTO addMember(UUID workspaceId, UUID userId, String role, UUID invitedBy) {
        log.info("Adding member {} to workspace {} with role {}", userId, workspaceId, role);
        
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
                .role(WorkspaceMember.WorkspaceRole.valueOf(role))
                .invitedBy(invitedBy)
                .active(true)
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
    public MembershipDTO updateMemberRole(UUID workspaceId, UUID userId, String newRole) {
        log.info("Updating member {} role in workspace {} to {}", userId, workspaceId, newRole);
        
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));
        
        WorkspaceMember.WorkspaceRole role = WorkspaceMember.WorkspaceRole.valueOf(newRole);
        
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
        
        // Update permissions based on new role
        if (role == WorkspaceMember.WorkspaceRole.ADMIN) {
            membership.setCanCreateProjects(true);
            membership.setCanInviteMembers(true);
        } else if (role == WorkspaceMember.WorkspaceRole.GUEST) {
            membership.setCanCreateProjects(false);
            membership.setCanInviteMembers(false);
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
}