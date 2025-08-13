package com.taskava.api.controller;

import com.taskava.common.dto.*;
import com.taskava.common.response.ApiResponse;
import com.taskava.service.WorkspaceService;
import com.taskava.service.TeamService;
import com.taskava.service.ProjectService;
import com.taskava.service.CustomFieldService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final TeamService teamService;
    private final ProjectService projectService;
    private final CustomFieldService customFieldService;

    @PostMapping
    @Operation(summary = "Create a new workspace", 
            description = "Creates a new workspace within an organization")
    @PreAuthorize("@securityExpressionService.hasOrganizationAccess(#request.organizationId, authentication)")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Creating workspace: {} in organization: {} by user: {}", 
                request.getName(), request.getOrganizationId(), userId);
        
        WorkspaceDTO created = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by ID", 
            description = "Retrieves workspace details including member and project counts")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> getWorkspace(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Fetching workspace: {} for user: {}", id, authentication.getName());
        WorkspaceDTO workspace = workspaceService.getWorkspace(id);
        return ResponseEntity.ok(ApiResponse.success(workspace));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace", 
            description = "Updates workspace details (name, description, visibility, etc.)")
    @PreAuthorize("@securityExpressionService.canModifyWorkspace(#id, authentication)")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            Authentication authentication) {
        log.info("Updating workspace: {} by user: {}", id, authentication.getName());
        WorkspaceDTO updated = workspaceService.updateWorkspace(id, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workspace", 
            description = "Soft deletes a workspace and all associated data")
    @PreAuthorize("@securityExpressionService.isWorkspaceAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Deleting workspace: {} by user: {}", id, userId);
        workspaceService.deleteWorkspace(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "List user's workspaces", 
            description = "Get all workspaces where the current user is a member")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<WorkspaceDTO>>> getUserWorkspaces(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching workspaces for user: {}", userId);
        Page<WorkspaceDTO> workspaces = workspaceService.getUserWorkspaces(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(workspaces));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List organization's workspaces", 
            description = "Get all workspaces in an organization")
    @PreAuthorize("@securityExpressionService.hasOrganizationAccess(#organizationId, authentication)")
    public ResponseEntity<ApiResponse<Page<WorkspaceDTO>>> getOrganizationWorkspaces(
            @PathVariable UUID organizationId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication) {
        log.info("Fetching workspaces for organization: {} by user: {}", 
                organizationId, authentication.getName());
        Page<WorkspaceDTO> workspaces = workspaceService.getOrganizationWorkspaces(organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(workspaces));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to workspace", 
            description = "Adds a new member to the workspace with specified role and permissions")
    @PreAuthorize("@securityExpressionService.canInviteToWorkspace(#id, authentication)")
    public ResponseEntity<ApiResponse<MembershipDTO>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorkspaceMemberRequest request,
            Authentication authentication) {
        UUID invitedBy = UUID.fromString(authentication.getName());
        log.info("Adding member to workspace {} with role {} by user {}", 
                id, request.getRole(), invitedBy);
        
        MembershipDTO membership = workspaceService.addMember(id, request, invitedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", membership));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from workspace", 
            description = "Removes a member from the workspace")
    @PreAuthorize("@securityExpressionService.isWorkspaceAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication authentication) {
        log.info("Removing member {} from workspace {} by user {}", 
                userId, id, authentication.getName());
        workspaceService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }

    @PutMapping("/{id}/members/{userId}")
    @Operation(summary = "Update member role", 
            description = "Updates a member's role and permissions in the workspace")
    @PreAuthorize("@securityExpressionService.isWorkspaceAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<MembershipDTO>> updateMemberRole(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateWorkspaceMemberRoleRequest request,
            Authentication authentication) {
        log.info("Updating member {} role in workspace {} to {} by user {}", 
                userId, id, request.getRole(), authentication.getName());
        MembershipDTO updated = workspaceService.updateMemberRole(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", updated));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List workspace members", 
            description = "Get all members of a workspace with their roles and permissions")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Page<MembershipDTO>>> getWorkspaceMembers(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching members for workspace: {}", id);
        Page<MembershipDTO> members = workspaceService.getWorkspaceMembers(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    // Team Management Endpoints

    @GetMapping("/{id}/teams")
    @Operation(summary = "List workspace teams", 
            description = "Get all teams in the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Page<TeamDTO>>> getWorkspaceTeams(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching teams for workspace: {}", id);
        Page<TeamDTO> teams = teamService.getWorkspaceTeams(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @GetMapping("/{id}/teams/count")
    @Operation(summary = "Count workspace teams", 
            description = "Get the total number of teams in the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Long>> countWorkspaceTeams(@PathVariable UUID id) {
        Long count = teamService.countWorkspaceTeams(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // Project Management Endpoints

    @GetMapping("/{id}/projects")
    @Operation(summary = "List workspace projects", 
            description = "Get all projects in the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Page<ProjectDTO>>> getWorkspaceProjects(
            @PathVariable UUID id,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) UUID teamId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching projects for workspace: {}, archived: {}, teamId: {}", id, archived, teamId);
        Page<ProjectDTO> projects = projectService.getWorkspaceProjects(id, archived, teamId, pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}/projects/recent")
    @Operation(summary = "Get recent projects", 
            description = "Get recently accessed projects in the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getRecentProjects(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching recent projects for user {} in workspace {}", userId, id);
        List<ProjectDTO> projects = projectService.getRecentProjects(id, userId, limit);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}/projects/count")
    @Operation(summary = "Count workspace projects", 
            description = "Get the total number of projects in the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Long>> countWorkspaceProjects(
            @PathVariable UUID id,
            @RequestParam(required = false) Boolean archived) {
        Long count = projectService.countWorkspaceProjects(id, archived);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // Custom Field Management Endpoints

    @GetMapping("/{id}/custom-fields")
    @Operation(summary = "List workspace custom fields", 
            description = "Get all custom field definitions for the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<List<CustomFieldDTO>>> getWorkspaceCustomFields(
            @PathVariable UUID id,
            @RequestParam(required = false) String fieldType) {
        log.info("Fetching custom fields for workspace: {}, type: {}", id, fieldType);
        List<CustomFieldDTO> fields = customFieldService.getWorkspaceCustomFields(id, fieldType);
        return ResponseEntity.ok(ApiResponse.success(fields));
    }

    @PostMapping("/{id}/custom-fields")
    @Operation(summary = "Create custom field", 
            description = "Creates a new custom field definition for the workspace")
    @PreAuthorize("@securityExpressionService.canManageCustomFields(#id, authentication)")
    public ResponseEntity<ApiResponse<CustomFieldDTO>> createCustomField(
            @PathVariable UUID id,
            @Valid @RequestBody CustomFieldDTO fieldDto,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Creating custom field {} in workspace {} by user {}", 
                fieldDto.getName(), id, userId);
        fieldDto.setWorkspaceId(id);
        CustomFieldDTO created = customFieldService.createCustomField(fieldDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Custom field created successfully", created));
    }

    @PutMapping("/{id}/custom-fields/{fieldId}")
    @Operation(summary = "Update custom field", 
            description = "Updates a custom field definition")
    @PreAuthorize("@securityExpressionService.canManageCustomFields(#id, authentication)")
    public ResponseEntity<ApiResponse<CustomFieldDTO>> updateCustomField(
            @PathVariable UUID id,
            @PathVariable UUID fieldId,
            @Valid @RequestBody CustomFieldDTO fieldDto,
            Authentication authentication) {
        log.info("Updating custom field {} in workspace {} by user {}", 
                fieldId, id, authentication.getName());
        CustomFieldDTO updated = customFieldService.updateCustomField(id, fieldId, fieldDto);
        return ResponseEntity.ok(ApiResponse.success("Custom field updated successfully", updated));
    }

    @DeleteMapping("/{id}/custom-fields/{fieldId}")
    @Operation(summary = "Delete custom field", 
            description = "Deletes a custom field definition (soft delete)")
    @PreAuthorize("@securityExpressionService.canManageCustomFields(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteCustomField(
            @PathVariable UUID id,
            @PathVariable UUID fieldId,
            Authentication authentication) {
        log.info("Deleting custom field {} from workspace {} by user {}", 
                fieldId, id, authentication.getName());
        customFieldService.deleteCustomField(id, fieldId);
        return ResponseEntity.ok(ApiResponse.success("Custom field deleted successfully"));
    }

    // Statistics and Analytics Endpoints

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get workspace statistics", 
            description = "Get comprehensive statistics about the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<WorkspaceStatisticsDTO>> getWorkspaceStatistics(
            @PathVariable UUID id) {
        log.info("Fetching statistics for workspace: {}", id);
        WorkspaceStatisticsDTO stats = workspaceService.getWorkspaceStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}/activity")
    @Operation(summary = "Get workspace activity", 
            description = "Get recent activity feed for the workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getWorkspaceActivity(
            @PathVariable UUID id,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching activity for workspace: {}", id);
        Page<ActivityDTO> activity = workspaceService.getWorkspaceActivity(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(activity));
    }

    // Archive and Restore Operations

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive workspace", 
            description = "Archives a workspace (can be restored later)")
    @PreAuthorize("@securityExpressionService.isWorkspaceAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> archiveWorkspace(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Archiving workspace: {} by user: {}", id, userId);
        workspaceService.archiveWorkspace(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace archived successfully"));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore workspace", 
            description = "Restores an archived workspace")
    @PreAuthorize("@securityExpressionService.isWorkspaceAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> restoreWorkspace(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Restoring workspace: {} by user: {}", id, userId);
        WorkspaceDTO restored = workspaceService.restoreWorkspace(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace restored successfully", restored));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate workspace", 
            description = "Creates a copy of the workspace with a new name")
    @PreAuthorize("@securityExpressionService.isWorkspaceAdmin(#id, authentication)")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> duplicateWorkspace(
            @PathVariable UUID id,
            @RequestParam String name,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Duplicating workspace: {} with name: {} by user: {}", id, name, userId);
        WorkspaceDTO duplicated = workspaceService.duplicateWorkspace(id, name, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace duplicated successfully", duplicated));
    }
}