package com.taskava.api.controller;

import com.taskava.common.dto.WorkspaceDTO;
import com.taskava.common.dto.MembershipDTO;
import com.taskava.service.WorkspaceService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace management endpoints")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<WorkspaceDTO> createWorkspace(
            @Valid @RequestBody WorkspaceDTO workspaceDTO,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        WorkspaceDTO created = workspaceService.createWorkspace(workspaceDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by ID")
    public ResponseEntity<WorkspaceDTO> getWorkspace(@PathVariable UUID id) {
        WorkspaceDTO workspace = workspaceService.getWorkspace(id);
        return ResponseEntity.ok(workspace);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace")
    public ResponseEntity<WorkspaceDTO> updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody WorkspaceDTO workspaceDTO) {
        WorkspaceDTO updated = workspaceService.updateWorkspace(id, workspaceDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workspace (soft delete)")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        workspaceService.deleteWorkspace(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get user's workspaces")
    public ResponseEntity<Page<WorkspaceDTO>> getUserWorkspaces(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        UUID userId = UUID.fromString(authentication.getName());
        Page<WorkspaceDTO> workspaces = workspaceService.getUserWorkspaces(userId, pageable);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get organization's workspaces")
    public ResponseEntity<Page<WorkspaceDTO>> getOrganizationWorkspaces(
            @PathVariable UUID organizationId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<WorkspaceDTO> workspaces = workspaceService.getOrganizationWorkspaces(organizationId, pageable);
        return ResponseEntity.ok(workspaces);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to workspace")
    public ResponseEntity<MembershipDTO> addMember(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        UUID userId = UUID.fromString(request.get("userId").toString());
        String role = request.get("role").toString();
        UUID invitedBy = UUID.fromString(authentication.getName());
        
        MembershipDTO membership = workspaceService.addMember(id, userId, role, invitedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from workspace")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        workspaceService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/members/{userId}/role")
    @Operation(summary = "Update member role in workspace")
    public ResponseEntity<MembershipDTO> updateMemberRole(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {
        String newRole = request.get("role");
        MembershipDTO updated = workspaceService.updateMemberRole(id, userId, newRole);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get workspace members")
    public ResponseEntity<Page<MembershipDTO>> getWorkspaceMembers(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MembershipDTO> members = workspaceService.getWorkspaceMembers(id, pageable);
        return ResponseEntity.ok(members);
    }
}