package com.taskava.service;

import com.taskava.common.dto.*;
import com.taskava.data.entity.Team;
import com.taskava.data.entity.TeamMember;
import com.taskava.data.entity.User;
import com.taskava.data.entity.Workspace;
import com.taskava.data.repository.TeamMemberRepository;
import com.taskava.data.repository.TeamRepository;
import com.taskava.data.repository.UserRepository;
import com.taskava.data.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @PreAuthorize("@workspaceService.canViewWorkspace(#workspaceId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Long countWorkspaceTeams(UUID workspaceId) {
        return teamRepository.countByWorkspaceId(workspaceId);
    }
    
    /**
     * Create a new team within a workspace
     */
    @PreAuthorize("@teamService.canCreateTeamInWorkspace(#request.workspaceId, authentication.principal)")
    public TeamDTO createTeam(CreateTeamRequest request, UUID creatorUserId) {
        log.info("Creating team: {} in workspace: {} by user: {}", 
                request.getName(), request.getWorkspaceId(), creatorUserId);
        
        // Check if team name already exists in workspace
        if (teamRepository.existsByWorkspaceIdAndName(request.getWorkspaceId(), request.getName())) {
            throw new IllegalArgumentException("Team with name already exists in this workspace: " + request.getName());
        }
        
        // Get workspace
        Workspace workspace = workspaceRepository.findActiveById(request.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + request.getWorkspaceId()));
        
        // Get team lead if specified
        User teamLead = null;
        if (request.getTeamLeadId() != null) {
            teamLead = userRepository.findById(request.getTeamLeadId())
                    .orElseThrow(() -> new IllegalArgumentException("Team lead user not found: " + request.getTeamLeadId()));
        }
        
        // Create team
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .workspace(workspace)
                .teamLead(teamLead)
                .build();
        
        team = teamRepository.save(team);
        
        // Add creator as a team member (as LEAD if no team lead specified)
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found: " + creatorUserId));
        
        TeamMember.TeamRole creatorRole = (teamLead == null || teamLead.getId().equals(creatorUserId)) 
                ? TeamMember.TeamRole.LEAD 
                : TeamMember.TeamRole.MEMBER;
        
        TeamMember creatorMembership = TeamMember.builder()
                .team(team)
                .user(creator)
                .role(creatorRole)
                .invitedBy(creatorUserId)
                .invitedAt(Instant.now())
                .joinedAt(Instant.now())
                .active(true)
                .build();
        
        teamMemberRepository.save(creatorMembership);
        
        // If team lead is different from creator, add them as LEAD
        if (teamLead != null && !teamLead.getId().equals(creatorUserId)) {
            TeamMember leadMembership = TeamMember.builder()
                    .team(team)
                    .user(teamLead)
                    .role(TeamMember.TeamRole.LEAD)
                    .invitedBy(creatorUserId)
                    .invitedAt(Instant.now())
                    .joinedAt(Instant.now())
                    .active(true)
                    .build();
            
            teamMemberRepository.save(leadMembership);
        }
        
        return mapToDTO(team);
    }

    /**
     * Update team details
     */
    @PreAuthorize("@teamService.canManageTeam(#id, authentication.principal)")
    public TeamDTO updateTeam(UUID id, UpdateTeamRequest request) {
        log.info("Updating team: {}", id);
        
        Team team = teamRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        
        // Update fields if provided
        if (request.getName() != null) {
            // Check if new name conflicts with existing team in workspace
            if (!request.getName().equals(team.getName()) && 
                teamRepository.existsByWorkspaceIdAndName(team.getWorkspace().getId(), request.getName())) {
                throw new IllegalArgumentException("Team with name already exists in this workspace: " + request.getName());
            }
            team.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }
        
        if (request.getColor() != null) {
            team.setColor(request.getColor());
        }
        
        if (request.getIcon() != null) {
            team.setIcon(request.getIcon());
        }
        
        if (request.getTeamLeadId() != null) {
            User newTeamLead = userRepository.findById(request.getTeamLeadId())
                    .orElseThrow(() -> new IllegalArgumentException("Team lead user not found: " + request.getTeamLeadId()));
            
            // Ensure new team lead is a member of the team
            if (!teamMemberRepository.isActiveMember(id, request.getTeamLeadId())) {
                // Add as member with LEAD role
                TeamMember leadMembership = TeamMember.builder()
                        .team(team)
                        .user(newTeamLead)
                        .role(TeamMember.TeamRole.LEAD)
                        .invitedAt(Instant.now())
                        .joinedAt(Instant.now())
                        .active(true)
                        .build();
                teamMemberRepository.save(leadMembership);
            } else {
                // Update existing member's role to LEAD
                TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(id, request.getTeamLeadId())
                        .orElseThrow(() -> new IllegalArgumentException("Team membership not found"));
                membership.setRole(TeamMember.TeamRole.LEAD);
                teamMemberRepository.save(membership);
            }
            
            team.setTeamLead(newTeamLead);
        }
        
        team = teamRepository.save(team);
        return mapToDTO(team);
    }

    /**
     * Delete/archive a team (soft delete)
     */
    @PreAuthorize("@teamService.canManageTeam(#id, authentication.principal)")
    public void deleteTeam(UUID id, UUID deletedBy) {
        log.info("Soft deleting team: {} by user: {}", id, deletedBy);
        
        Team team = teamRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        
        // Soft delete the team
        team.softDelete(deletedBy);
        teamRepository.save(team);
        
        // Deactivate all team memberships
        List<TeamMember> members = teamMemberRepository.findActiveByTeamId(id);
        for (TeamMember member : members) {
            member.setActive(false);
            member.softDelete(deletedBy);
        }
        teamMemberRepository.saveAll(members);
    }

    /**
     * Get team by ID
     */
    @PreAuthorize("@teamService.canViewTeam(#id, authentication.principal)")
    @Transactional(readOnly = true)
    public TeamDTO getTeam(UUID id) {
        Team team = teamRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        
        TeamDTO dto = mapToDTO(team);
        dto.setTotalMembers(teamMemberRepository.countActiveMembers(id));
        dto.setTotalProjects(teamRepository.countActiveProjects(id));
        
        return dto;
    }

    /**
     * List teams in a workspace
     */
    @PreAuthorize("@teamService.canViewWorkspace(#workspaceId, authentication.principal)")
    @Transactional(readOnly = true)
    public Page<TeamDTO> getWorkspaceTeams(UUID workspaceId, Pageable pageable) {
        Page<Team> teams = teamRepository.findByWorkspaceId(workspaceId, pageable);
        return teams.map(team -> {
            TeamDTO dto = mapToDTO(team);
            dto.setTotalMembers(teamMemberRepository.countActiveMembers(team.getId()));
            dto.setTotalProjects(teamRepository.countActiveProjects(team.getId()));
            return dto;
        });
    }

    /**
     * Add a member to a team
     */
    @PreAuthorize("@teamService.canManageTeam(#teamId, authentication.principal)")
    public TeamMemberDTO addTeamMember(UUID teamId, AddTeamMemberRequest request, UUID invitedBy) {
        log.info("Adding member {} to team {} with role {}", request.getUserId(), teamId, request.getRole());
        
        // Check if user is already a member
        if (teamMemberRepository.isActiveMember(teamId, request.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this team");
        }
        
        Team team = teamRepository.findActiveById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));
        
        TeamMember membership = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamMember.TeamRole.valueOf(request.getRole()))
                .invitedBy(invitedBy)
                .invitedAt(Instant.now())
                .joinedAt(Instant.now())
                .active(true)
                .build();
        
        membership = teamMemberRepository.save(membership);
        
        // If adding as LEAD, update team's teamLead field
        if (membership.getRole() == TeamMember.TeamRole.LEAD) {
            team.setTeamLead(user);
            teamRepository.save(team);
        }
        
        return mapMemberToDTO(membership);
    }

    /**
     * Remove a member from a team
     */
    @PreAuthorize("@teamService.canManageTeam(#teamId, authentication.principal)")
    public void removeTeamMember(UUID teamId, UUID userId, UUID removedBy) {
        log.info("Removing member {} from team {}", userId, teamId);
        
        TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Team membership not found"));
        
        // Check if trying to remove the last LEAD
        if (membership.getRole() == TeamMember.TeamRole.LEAD) {
            Long leadCount = teamMemberRepository.countByTeamIdAndRole(teamId, TeamMember.TeamRole.LEAD);
            if (leadCount <= 1) {
                throw new IllegalArgumentException("Cannot remove the last team lead");
            }
        }
        
        membership.setActive(false);
        membership.softDelete(removedBy);
        teamMemberRepository.save(membership);
        
        // If removing team lead, clear the teamLead field
        Team team = membership.getTeam();
        if (team.getTeamLead() != null && team.getTeamLead().getId().equals(userId)) {
            team.setTeamLead(null);
            teamRepository.save(team);
        }
    }

    /**
     * Update a team member's role
     */
    @PreAuthorize("@teamService.canManageTeam(#teamId, authentication.principal)")
    public TeamMemberDTO updateTeamMemberRole(UUID teamId, UUID userId, UpdateTeamMemberRoleRequest request) {
        log.info("Updating member {} role in team {} to {}", userId, teamId, request.getRole());
        
        TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Team membership not found"));
        
        TeamMember.TeamRole newRole = TeamMember.TeamRole.valueOf(request.getRole());
        TeamMember.TeamRole oldRole = membership.getRole();
        
        // Check if demoting the last LEAD
        if (oldRole == TeamMember.TeamRole.LEAD && newRole != TeamMember.TeamRole.LEAD) {
            Long leadCount = teamMemberRepository.countByTeamIdAndRole(teamId, TeamMember.TeamRole.LEAD);
            if (leadCount <= 1) {
                throw new IllegalArgumentException("Cannot demote the last team lead");
            }
        }
        
        membership.setRole(newRole);
        membership = teamMemberRepository.save(membership);
        
        // Update team's teamLead field if necessary
        Team team = membership.getTeam();
        if (newRole == TeamMember.TeamRole.LEAD) {
            team.setTeamLead(membership.getUser());
            teamRepository.save(team);
        } else if (team.getTeamLead() != null && team.getTeamLead().getId().equals(userId)) {
            // If demoting current team lead, find another LEAD or set to null
            List<TeamMember> otherLeads = teamMemberRepository.findByTeamIdAndRole(teamId, TeamMember.TeamRole.LEAD);
            if (!otherLeads.isEmpty()) {
                team.setTeamLead(otherLeads.get(0).getUser());
            } else {
                team.setTeamLead(null);
            }
            teamRepository.save(team);
        }
        
        return mapMemberToDTO(membership);
    }

    /**
     * List team members
     */
    @PreAuthorize("@teamService.canViewTeam(#teamId, authentication.principal)")
    @Transactional(readOnly = true)
    public Page<TeamMemberDTO> getTeamMembers(UUID teamId, Pageable pageable) {
        Page<TeamMember> members = teamMemberRepository.findActiveByTeamId(teamId, pageable);
        return members.map(this::mapMemberToDTO);
    }

    // Helper methods for security checks
    public boolean canCreateTeamInWorkspace(UUID workspaceId, Object principal) {
        // Implementation would check if user is a member of the workspace with appropriate permissions
        // For now, return true - this should be properly implemented based on your security model
        return true;
    }

    public boolean canViewTeam(UUID teamId, Object principal) {
        // Check if user is a member of the team or has workspace-level access
        // For now, return true - this should be properly implemented
        return true;
    }

    public boolean canManageTeam(UUID teamId, Object principal) {
        // Check if user is a team lead or has workspace admin access
        // For now, return true - this should be properly implemented
        return true;
    }

    public boolean canViewWorkspace(UUID workspaceId, Object principal) {
        // Check if user is a member of the workspace
        // For now, return true - this should be properly implemented
        return true;
    }

    // Mapping methods
    private TeamDTO mapToDTO(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .color(team.getColor())
                .icon(team.getIcon())
                .workspaceId(team.getWorkspace().getId())
                .workspaceName(team.getWorkspace().getName())
                .teamLeadId(team.getTeamLead() != null ? team.getTeamLead().getId() : null)
                .teamLeadName(team.getTeamLead() != null ? team.getTeamLead().getFullName() : null)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .createdBy(team.getCreatedBy())
                .updatedBy(team.getUpdatedBy())
                .build();
    }

    private TeamMemberDTO mapMemberToDTO(TeamMember membership) {
        User inviter = null;
        if (membership.getInvitedBy() != null) {
            inviter = userRepository.findById(membership.getInvitedBy()).orElse(null);
        }
        
        return TeamMemberDTO.builder()
                .id(membership.getId())
                .teamId(membership.getTeam().getId())
                .teamName(membership.getTeam().getName())
                .userId(membership.getUser().getId())
                .userEmail(membership.getUser().getEmail())
                .userName(membership.getUser().getFullName())
                .userAvatar(membership.getUser().getAvatarUrl())
                .role(membership.getRole().toString())
                .invitedBy(membership.getInvitedBy())
                .invitedByName(inviter != null ? inviter.getFullName() : null)
                .invitedAt(membership.getInvitedAt())
                .joinedAt(membership.getJoinedAt())
                .active(membership.isActive())
                .build();
    }
}