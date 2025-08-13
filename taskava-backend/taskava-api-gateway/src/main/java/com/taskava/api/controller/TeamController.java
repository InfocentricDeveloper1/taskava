package com.taskava.api.controller;

import com.taskava.common.dto.*;
import com.taskava.common.response.ApiResponse;
import com.taskava.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    /**
     * Create a new team within a workspace
     */
    @PostMapping("/workspaces/{workspaceId}/teams")
    @Operation(summary = "Create a new team", description = "Creates a new team within the specified workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#workspaceId, authentication)")
    public ResponseEntity<ApiResponse<TeamDTO>> createTeam(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateTeamRequest request,
            Authentication authentication) {
        
        // Ensure workspace ID in path matches the one in request body
        if (!workspaceId.equals(request.getWorkspaceId())) {
            throw new IllegalArgumentException("Workspace ID in path does not match request body");
        }
        
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Creating team: {} in workspace: {} by user: {}", request.getName(), workspaceId, userId);
        
        TeamDTO team = teamService.createTeam(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully", team));
    }

    /**
     * Get team details by ID
     */
    @GetMapping("/teams/{id}")
    @Operation(summary = "Get team by ID", description = "Retrieves detailed information about a specific team")
    @PreAuthorize("@securityExpressionService.hasTeamAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<TeamDTO>> getTeam(
            @PathVariable UUID id) {
        log.info("Fetching team: {}", id);
        TeamDTO team = teamService.getTeam(id);
        return ResponseEntity.ok(ApiResponse.success(team));
    }

    /**
     * Update team details
     */
    @PutMapping("/teams/{id}")
    @Operation(summary = "Update team", description = "Updates team information")
    @PreAuthorize("@securityExpressionService.isTeamAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<TeamDTO>> updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request) {
        log.info("Updating team: {}", id);
        TeamDTO team = teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", team));
    }

    /**
     * Delete/archive a team
     */
    @DeleteMapping("/teams/{id}")
    @Operation(summary = "Delete team", description = "Soft deletes a team (can be restored)")
    @PreAuthorize("@securityExpressionService.isTeamAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Deleting team: {} by user: {}", id, userId);
        teamService.deleteTeam(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully"));
    }

    /**
     * List teams in a workspace
     */
    @GetMapping("/workspaces/{workspaceId}/teams")
    @Operation(summary = "List workspace teams", description = "Retrieves all teams in the specified workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#workspaceId, authentication)")
    public ResponseEntity<ApiResponse<Page<TeamDTO>>> getWorkspaceTeams(
            @PathVariable UUID workspaceId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching teams for workspace: {}", workspaceId);
        Page<TeamDTO> teams = teamService.getWorkspaceTeams(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    /**
     * Add a member to a team
     */
    @PostMapping("/teams/{id}/members")
    @Operation(summary = "Add team member", description = "Adds a new member to the team with specified role")
    @PreAuthorize("@securityExpressionService.canInviteToTeam(#id, authentication)")
    public ResponseEntity<ApiResponse<TeamMemberDTO>> addTeamMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddTeamMemberRequest request,
            Authentication authentication) {
        UUID invitedBy = UUID.fromString(authentication.getName());
        log.info("Adding member {} to team {} with role {} by user {}", 
                request.getUserId(), id, request.getRole(), invitedBy);
        
        TeamMemberDTO member = teamService.addTeamMember(id, request, invitedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team member added successfully", member));
    }

    /**
     * Remove a member from a team
     */
    @DeleteMapping("/teams/{id}/members/{userId}")
    @Operation(summary = "Remove team member", description = "Removes a member from the team")
    @PreAuthorize("@securityExpressionService.isTeamAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> removeTeamMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication authentication) {
        UUID removedBy = UUID.fromString(authentication.getName());
        log.info("Removing member {} from team {} by user {}", userId, id, removedBy);
        
        teamService.removeTeamMember(id, userId, removedBy);
        return ResponseEntity.ok(ApiResponse.success("Team member removed successfully"));
    }

    /**
     * Update a team member's role
     */
    @PutMapping("/teams/{id}/members/{userId}")
    @Operation(summary = "Update team member role", description = "Updates the role of a team member")
    @PreAuthorize("@securityExpressionService.isTeamAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<TeamMemberDTO>> updateTeamMemberRole(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateTeamMemberRoleRequest request) {
        log.info("Updating member {} role in team {} to {}", userId, id, request.getRole());
        
        TeamMemberDTO member = teamService.updateTeamMemberRole(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Team member role updated successfully", member));
    }

    /**
     * List team members
     */
    @GetMapping("/teams/{id}/members")
    @Operation(summary = "List team members", description = "Retrieves all members of a team with their roles")
    @PreAuthorize("@securityExpressionService.hasTeamAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Page<TeamMemberDTO>>> getTeamMembers(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching members for team: {}", id);
        Page<TeamMemberDTO> members = teamService.getTeamMembers(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(members));
    }
}