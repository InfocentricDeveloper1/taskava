package com.taskava.api.controller;

import com.taskava.common.dto.*;
import com.taskava.common.response.ApiResponse;
import com.taskava.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project in the current workspace")
    @PreAuthorize("@securityExpressionService.hasWorkspaceAccess(#createDto.workspaceId, authentication)")
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
            @Valid @RequestBody ProjectCreateDTO createDto) {
        log.info("Creating new project: {}", createDto.getName());
        ProjectDTO project = projectService.createProject(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves project details including sections")
    @PreAuthorize("@securityExpressionService.hasProjectAccess(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectDTO>> getProject(
            @PathVariable UUID id) {
        log.info("Fetching project: {}", id);
        ProjectDTO project = projectService.getProject(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Updates project details")
    @PreAuthorize("@securityExpressionService.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDTO updateDto) {
        log.info("Updating project: {}", id);
        ProjectDTO project = projectService.updateProject(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Soft deletes a project and all its data")
    @PreAuthorize("@securityExpressionService.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID id) {
        log.info("Deleting project: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive project", description = "Archives a project (can be restored)")
    @PreAuthorize("@securityExpressionService.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> archiveProject(
            @PathVariable UUID id) {
        log.info("Archiving project: {}", id);
        projectService.archiveProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project archived successfully"));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore project", description = "Restores an archived project")
    @PreAuthorize("@securityExpressionService.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectDTO>> restoreProject(
            @PathVariable UUID id) {
        log.info("Restoring project: {}", id);
        ProjectDTO project = projectService.restoreProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project restored successfully", project));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate project", description = "Creates a copy of the project with a new name")
    @PreAuthorize("@securityExpressionService.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectDTO>> duplicateProject(
            @PathVariable UUID id,
            @RequestParam String name) {
        log.info("Duplicating project: {} with name: {}", id, name);
        ProjectDTO project = projectService.duplicateProject(id, name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project duplicated successfully", project));
    }

    @GetMapping
    @Operation(summary = "List workspace projects", description = "Get all projects in the current workspace")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectDTO>>> getWorkspaceProjects(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching workspace projects");
        Page<ProjectDTO> projects = projectService.getWorkspaceProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/my")
    @Operation(summary = "List user's projects", description = "Get projects where the current user is a member")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectDTO>>> getUserProjects(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching user's projects");
        Page<ProjectDTO> projects = projectService.getUserProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/search")
    @Operation(summary = "Search projects", description = "Search projects by name or description")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectDTO>>> searchProjects(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Searching projects with query: {}", query);
        Page<ProjectDTO> projects = projectService.searchProjects(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    // Section Management Endpoints

    @PostMapping("/{projectId}/sections")
    @Operation(summary = "Create section", description = "Creates a new section in the project")
    @PreAuthorize("@securityExpressionService.canModifyProject(#projectId, authentication)")
    public ResponseEntity<ApiResponse<ProjectSectionDTO>> createSection(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectSectionDTO sectionDto) {
        log.info("Creating section in project: {}", projectId);
        sectionDto.setProjectId(projectId);
        ProjectSectionDTO section = projectService.createSection(projectId, sectionDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Section created successfully", section));
    }

    @GetMapping("/{projectId}/sections")
    @Operation(summary = "List project sections", description = "Get all sections in a project")
    @PreAuthorize("@securityExpressionService.hasProjectAccess(#projectId, authentication)")
    public ResponseEntity<ApiResponse<List<ProjectSectionDTO>>> getProjectSections(
            @PathVariable UUID projectId) {
        log.info("Fetching sections for project: {}", projectId);
        List<ProjectSectionDTO> sections = projectService.getProjectSections(projectId);
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @PutMapping("/{projectId}/sections/{sectionId}")
    @Operation(summary = "Update section", description = "Updates a section's details")
    @PreAuthorize("@securityExpressionService.canModifyProject(#projectId, authentication)")
    public ResponseEntity<ApiResponse<ProjectSectionDTO>> updateSection(
            @PathVariable UUID projectId,
            @PathVariable UUID sectionId,
            @Valid @RequestBody ProjectSectionDTO sectionDto) {
        log.info("Updating section: {} in project: {}", sectionId, projectId);
        ProjectSectionDTO section = projectService.updateSection(projectId, sectionId, sectionDto);
        return ResponseEntity.ok(ApiResponse.success("Section updated successfully", section));
    }

    @PutMapping("/{projectId}/sections/{sectionId}/reorder")
    @Operation(summary = "Reorder section", description = "Changes the position of a section")
    @PreAuthorize("@securityExpressionService.canModifyProject(#projectId, authentication)")
    public ResponseEntity<ApiResponse<Void>> reorderSection(
            @PathVariable UUID projectId,
            @PathVariable UUID sectionId,
            @RequestParam Integer position) {
        log.info("Reordering section: {} to position: {} in project: {}", sectionId, position, projectId);
        projectService.reorderSection(projectId, sectionId, position);
        return ResponseEntity.ok(ApiResponse.success("Section reordered successfully"));
    }

    @DeleteMapping("/{projectId}/sections/{sectionId}")
    @Operation(summary = "Delete section", description = "Deletes a section from the project")
    @PreAuthorize("@securityExpressionService.canModifyProject(#projectId, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable UUID projectId,
            @PathVariable UUID sectionId) {
        log.info("Deleting section: {} from project: {}", sectionId, projectId);
        projectService.deleteSection(projectId, sectionId);
        return ResponseEntity.ok(ApiResponse.success("Section deleted successfully"));
    }

    @PostMapping("/{projectId}/sections/batch-reorder")
    @Operation(summary = "Batch reorder sections", description = "Reorders multiple sections at once")
    @PreAuthorize("@securityExpressionService.canModifyProject(#projectId, authentication)")
    public ResponseEntity<ApiResponse<Void>> batchReorderSections(
            @PathVariable UUID projectId,
            @RequestBody Map<UUID, Integer> sectionPositions) {
        log.info("Batch reordering sections in project: {}", projectId);
        sectionPositions.forEach((sectionId, position) -> 
            projectService.reorderSection(projectId, sectionId, position));
        return ResponseEntity.ok(ApiResponse.success("Sections reordered successfully"));
    }
}