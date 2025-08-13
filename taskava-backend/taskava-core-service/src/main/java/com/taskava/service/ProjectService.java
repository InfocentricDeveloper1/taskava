package com.taskava.service;

import com.taskava.common.dto.*;
import com.taskava.data.entity.*;
import com.taskava.data.repository.*;
import com.taskava.security.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectSectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    // Overloaded method for workspace-specific projects
    @PreAuthorize("@workspaceService.canViewWorkspace(#workspaceId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getWorkspaceProjects(UUID workspaceId, Boolean archived, UUID teamId, Pageable pageable) {
        log.info("Fetching projects for workspace: {}, archived: {}, teamId: {}", workspaceId, archived, teamId);
        
        Page<Project> projects;
        if (teamId != null) {
            projects = projectRepository.findByWorkspaceIdAndTeamId(workspaceId, teamId, pageable);
        } else if (archived != null && archived) {
            projects = projectRepository.findArchivedByWorkspaceId(workspaceId, pageable);
        } else {
            projects = projectRepository.findActiveByWorkspaceId(workspaceId, pageable);
        }
        
        return projects.map(this::mapToDTO);
    }
    
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public List<ProjectDTO> getRecentProjects(UUID workspaceId, UUID userId, int limit) {
        log.info("Fetching recent projects for user {} in workspace {}", userId, workspaceId);
        
        // TODO: Implement actual recent project tracking based on user access logs
        List<Project> projects = projectRepository.findRecentByUserAndWorkspace(userId, workspaceId, limit);
        
        return projects.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("@workspaceService.canViewWorkspace(#workspaceId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Long countWorkspaceProjects(UUID workspaceId, Boolean archived) {
        if (archived != null && archived) {
            return projectRepository.countArchivedByWorkspaceId(workspaceId);
        }
        return projectRepository.countActiveByWorkspaceId(workspaceId);
    }
    
    /**
     * Create a new project
     */
    @PreAuthorize("@projectService.canCreateProject(authentication.principal.id)")
    public ProjectDTO createProject(ProjectCreateDTO dto) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        UUID userId = TenantContext.getCurrentUser();
        
        if (workspaceId == null) {
            throw new IllegalStateException("Workspace context is required");
        }
        
        log.info("Creating project: {} in workspace: {} by user: {}", 
                dto.getName(), workspaceId, userId);
        
        // Validate workspace exists
        Workspace workspace = workspaceRepository.findActiveById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));
        
        // Check if project name already exists in workspace
        if (projectRepository.existsByWorkspaceIdAndName(workspaceId, dto.getName())) {
            throw new IllegalArgumentException("Project with name already exists in this workspace: " + dto.getName());
        }
        
        // Validate team if provided
        Team team = null;
        if (dto.getTeamId() != null) {
            team = teamRepository.findActiveByIdAndWorkspace(dto.getTeamId(), workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found in workspace: " + dto.getTeamId()));
        }
        
        // Create project
        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .color(dto.getColor() != null ? dto.getColor() : "#1e90ff")
                .icon(dto.getIcon())
                .workspace(workspace)
                .team(team)
                .privacy(dto.getPrivacy() != null ? 
                    Project.ProjectPrivacy.valueOf(dto.getPrivacy()) : 
                    Project.ProjectPrivacy.TEAM_VISIBLE)
                .settings(dto.getSettings() != null ? dto.getSettings() : new HashMap<>())
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        
        project = projectRepository.save(project);
        
        // Create default sections if specified
        if (dto.getSectionNames() != null && !dto.getSectionNames().isEmpty()) {
            int position = 0;
            for (String sectionName : dto.getSectionNames()) {
                ProjectSection section = ProjectSection.builder()
                        .name(sectionName)
                        .position(position++)
                        .project(project)
                        .build();
                sectionRepository.save(section);
            }
        } else {
            // Create default sections
            createDefaultSections(project);
        }
        
        // Add creator as member
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        project.getMembers().add(creator);
        
        // Add additional members if specified
        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            for (UUID memberId : dto.getMemberIds()) {
                if (!memberId.equals(userId)) {
                    User member = userRepository.findById(memberId)
                            .orElse(null);
                    if (member != null && isUserInWorkspace(memberId, workspaceId)) {
                        project.getMembers().add(member);
                    }
                }
            }
        }
        
        project = projectRepository.save(project);
        
        return mapToDTO(project);
    }

    /**
     * Get project by ID
     */
    @PreAuthorize("@projectService.canViewProject(#id, authentication.principal.id)")
    @Transactional(readOnly = true)
    public ProjectDTO getProject(UUID id) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        Project project = projectRepository.findActiveByIdAndWorkspace(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        
        ProjectDTO dto = mapToDTO(project);
        
        // Load sections
        List<ProjectSection> sections = sectionRepository.findByProjectIdOrderByPosition(id);
        dto.setSections(sections.stream()
                .map(this::mapSectionToDTO)
                .collect(Collectors.toList()));
        
        // Load statistics
        dto.setTotalSections((long) sections.size());
        // TODO: Load task counts when Task entity is fully implemented
        
        return dto;
    }

    /**
     * Update project
     */
    @PreAuthorize("@projectService.canManageProject(#id, authentication.principal.id)")
    public ProjectDTO updateProject(UUID id, ProjectDTO dto) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        log.info("Updating project: {} in workspace: {}", id, workspaceId);
        
        Project project = projectRepository.findActiveByIdAndWorkspace(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        
        // Update fields
        if (dto.getName() != null) {
            if (!dto.getName().equals(project.getName()) && 
                projectRepository.existsByWorkspaceIdAndNameExcludingId(workspaceId, dto.getName(), id)) {
                throw new IllegalArgumentException("Project name already exists in this workspace");
            }
            project.setName(dto.getName());
        }
        
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        
        if (dto.getColor() != null) {
            project.setColor(dto.getColor());
        }
        
        if (dto.getIcon() != null) {
            project.setIcon(dto.getIcon());
        }
        
        if (dto.getPrivacy() != null) {
            project.setPrivacy(Project.ProjectPrivacy.valueOf(dto.getPrivacy()));
        }
        
        if (dto.getStatusUpdate() != null) {
            project.setStatusUpdate(dto.getStatusUpdate());
        }
        
        if (dto.getSettings() != null) {
            project.setSettings(dto.getSettings());
        }
        
        if (dto.getTeamId() != null) {
            Team team = teamRepository.findActiveByIdAndWorkspace(dto.getTeamId(), workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found: " + dto.getTeamId()));
            project.setTeam(team);
        }
        
        project = projectRepository.save(project);
        return mapToDTO(project);
    }

    /**
     * Archive project
     */
    @PreAuthorize("@projectService.canManageProject(#id, authentication.principal.id)")
    public void archiveProject(UUID id) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        log.info("Archiving project: {} in workspace: {}", id, workspaceId);
        
        Project project = projectRepository.findActiveByIdAndWorkspace(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        
        project.archive();
        projectRepository.save(project);
    }

    /**
     * Restore archived project
     */
    @PreAuthorize("@projectService.canManageProject(#id, authentication.principal.id)")
    public ProjectDTO restoreProject(UUID id) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        log.info("Restoring project: {} in workspace: {}", id, workspaceId);
        
        Project project = projectRepository.findActiveByIdAndWorkspace(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        
        if (project.getStatus() != Project.ProjectStatus.ARCHIVED) {
            throw new IllegalArgumentException("Project is not archived");
        }
        
        project.restore();
        project = projectRepository.save(project);
        return mapToDTO(project);
    }

    /**
     * Delete project (soft delete)
     */
    @PreAuthorize("@projectService.canManageProject(#id, authentication.principal.id)")
    public void deleteProject(UUID id) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        UUID userId = TenantContext.getCurrentUser();
        
        log.info("Soft deleting project: {} in workspace: {} by user: {}", id, workspaceId, userId);
        
        Project project = projectRepository.findActiveByIdAndWorkspace(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        
        // Soft delete all sections
        sectionRepository.softDeleteByProjectId(id);
        
        // Soft delete the project
        project.softDelete(userId);
        projectRepository.save(project);
    }

    /**
     * Duplicate project
     */
    @PreAuthorize("@projectService.canManageProject(#id, authentication.principal.id)")
    public ProjectDTO duplicateProject(UUID id, String newName) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        UUID userId = TenantContext.getCurrentUser();
        
        log.info("Duplicating project: {} in workspace: {} with new name: {}", id, workspaceId, newName);
        
        Project originalProject = projectRepository.findActiveByIdAndWorkspace(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        
        // Check if new name is available
        if (projectRepository.existsByWorkspaceIdAndName(workspaceId, newName)) {
            throw new IllegalArgumentException("Project with name already exists: " + newName);
        }
        
        // Create new project with copied attributes
        Project newProject = Project.builder()
                .name(newName)
                .description(originalProject.getDescription())
                .color(originalProject.getColor())
                .icon(originalProject.getIcon())
                .workspace(originalProject.getWorkspace())
                .team(originalProject.getTeam())
                .privacy(originalProject.getPrivacy())
                .settings(new HashMap<>(originalProject.getSettings()))
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        
        newProject = projectRepository.save(newProject);
        
        // Copy sections
        List<ProjectSection> originalSections = sectionRepository.findByProjectIdOrderByPosition(id);
        for (ProjectSection originalSection : originalSections) {
            ProjectSection newSection = ProjectSection.builder()
                    .name(originalSection.getName())
                    .position(originalSection.getPosition())
                    .project(newProject)
                    .build();
            sectionRepository.save(newSection);
        }
        
        // Copy members
        newProject.setMembers(new HashSet<>(originalProject.getMembers()));
        newProject = projectRepository.save(newProject);
        
        return mapToDTO(newProject);
    }

    /**
     * Get projects in workspace
     */
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getWorkspaceProjects(Pageable pageable) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        if (workspaceId == null) {
            throw new IllegalStateException("Workspace context is required");
        }
        
        Page<Project> projects = projectRepository.findByWorkspaceId(workspaceId, pageable);
        return projects.map(this::mapToDTO);
    }

    /**
     * Get user's projects in workspace
     */
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getUserProjects(Pageable pageable) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        UUID userId = TenantContext.getCurrentUser();
        
        if (workspaceId == null) {
            throw new IllegalStateException("Workspace context is required");
        }
        
        Page<Project> projects = projectRepository.findByWorkspaceIdAndMemberId(workspaceId, userId, pageable);
        return projects.map(this::mapToDTO);
    }

    /**
     * Search projects
     */
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public Page<ProjectDTO> searchProjects(String query, Pageable pageable) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        if (workspaceId == null) {
            throw new IllegalStateException("Workspace context is required");
        }
        
        Page<Project> projects = projectRepository.searchByNameOrDescription(workspaceId, query, pageable);
        return projects.map(this::mapToDTO);
    }

    // Section Management Methods

    /**
     * Create section in project
     */
    @PreAuthorize("@projectService.canManageProject(#projectId, authentication.principal.id)")
    public ProjectSectionDTO createSection(UUID projectId, ProjectSectionDTO dto) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        
        log.info("Creating section: {} in project: {}", dto.getName(), projectId);
        
        Project project = projectRepository.findActiveByIdAndWorkspace(projectId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        // Check if section name already exists
        if (sectionRepository.existsByProjectIdAndName(projectId, dto.getName())) {
            throw new IllegalArgumentException("Section with name already exists: " + dto.getName());
        }
        
        // Get position
        Integer position = dto.getPosition();
        if (position == null) {
            position = sectionRepository.getMaxPositionByProjectId(projectId) + 1;
        } else {
            // Shift existing sections if inserting in middle
            sectionRepository.updatePositionsAfter(projectId, position, 1);
        }
        
        ProjectSection section = ProjectSection.builder()
                .name(dto.getName())
                .position(position)
                .project(project)
                .build();
        
        section = sectionRepository.save(section);
        return mapSectionToDTO(section);
    }

    /**
     * Update section
     */
    @PreAuthorize("@projectService.canManageProject(#projectId, authentication.principal.id)")
    public ProjectSectionDTO updateSection(UUID projectId, UUID sectionId, ProjectSectionDTO dto) {
        log.info("Updating section: {} in project: {}", sectionId, projectId);
        
        ProjectSection section = sectionRepository.findByIdAndProjectId(sectionId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));
        
        if (dto.getName() != null && !dto.getName().equals(section.getName())) {
            if (sectionRepository.existsByProjectIdAndNameExcludingId(projectId, dto.getName(), sectionId)) {
                throw new IllegalArgumentException("Section name already exists: " + dto.getName());
            }
            section.setName(dto.getName());
        }
        
        section = sectionRepository.save(section);
        return mapSectionToDTO(section);
    }

    /**
     * Reorder sections
     */
    @PreAuthorize("@projectService.canManageProject(#projectId, authentication.principal.id)")
    public void reorderSection(UUID projectId, UUID sectionId, Integer newPosition) {
        log.info("Reordering section: {} to position: {} in project: {}", sectionId, newPosition, projectId);
        
        ProjectSection section = sectionRepository.findByIdAndProjectId(sectionId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));
        
        Integer oldPosition = section.getPosition();
        
        if (!oldPosition.equals(newPosition)) {
            if (newPosition < oldPosition) {
                // Moving up
                sectionRepository.updatePositionsBetween(projectId, newPosition, oldPosition - 1, 1);
            } else {
                // Moving down
                sectionRepository.updatePositionsBetween(projectId, oldPosition + 1, newPosition, -1);
            }
            
            section.setPosition(newPosition);
            sectionRepository.save(section);
        }
    }

    /**
     * Delete section
     */
    @PreAuthorize("@projectService.canManageProject(#projectId, authentication.principal.id)")
    public void deleteSection(UUID projectId, UUID sectionId) {
        UUID userId = TenantContext.getCurrentUser();
        
        log.info("Deleting section: {} in project: {}", sectionId, projectId);
        
        ProjectSection section = sectionRepository.findByIdAndProjectId(sectionId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));
        
        // Update positions of sections after this one
        sectionRepository.updatePositionsAfter(projectId, section.getPosition() + 1, -1);
        
        // Soft delete the section
        section.softDelete(userId);
        sectionRepository.save(section);
    }

    /**
     * Get project sections
     */
    @PreAuthorize("@projectService.canViewProject(#projectId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public List<ProjectSectionDTO> getProjectSections(UUID projectId) {
        List<ProjectSection> sections = sectionRepository.findByProjectIdOrderByPosition(projectId);
        return sections.stream()
                .map(this::mapSectionToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void createDefaultSections(Project project) {
        String[] defaultSections = {"To Do", "In Progress", "Done"};
        for (int i = 0; i < defaultSections.length; i++) {
            ProjectSection section = ProjectSection.builder()
                    .name(defaultSections[i])
                    .position(i)
                    .project(project)
                    .build();
            sectionRepository.save(section);
        }
    }

    private boolean isUserInWorkspace(UUID userId, UUID workspaceId) {
        return workspaceMemberRepository.isActiveMember(workspaceId, userId);
    }

    public boolean canCreateProject(UUID userId) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        if (workspaceId == null) return false;
        
        return workspaceMemberRepository.canCreateProjects(workspaceId, userId);
    }

    public boolean canViewProject(UUID projectId, UUID userId) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        if (workspaceId == null) return false;
        
        Project project = projectRepository.findActiveByIdAndWorkspace(projectId, workspaceId).orElse(null);
        if (project == null) return false;
        
        // Check project privacy
        switch (project.getPrivacy()) {
            case PUBLIC:
                return true;
            case WORKSPACE_VISIBLE:
                return isUserInWorkspace(userId, workspaceId);
            case TEAM_VISIBLE:
                // Check if user is a project member or team member
                if (project.isMember(userId)) return true;
                if (project.getTeam() != null) {
                    // TODO: Check team membership when TeamMember is implemented
                }
                return false;
            default:
                return false;
        }
    }

    public boolean canManageProject(UUID projectId, UUID userId) {
        UUID workspaceId = TenantContext.getCurrentWorkspace();
        if (workspaceId == null) return false;
        
        // Check if user is workspace admin
        if (workspaceMemberRepository.isAdmin(workspaceId, userId)) {
            return true;
        }
        
        // Check if user is project member
        return projectRepository.isMember(projectId, userId);
    }

    private ProjectDTO mapToDTO(Project project) {
        ProjectDTO dto = ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .icon(project.getIcon())
                .status(project.getStatus().toString())
                .statusUpdate(project.getStatusUpdate())
                .privacy(project.getPrivacy().toString())
                .settings(project.getSettings())
                .workspaceId(project.getWorkspace().getId())
                .workspaceName(project.getWorkspace().getName())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .archivedAt(project.getArchivedAt())
                .createdBy(project.getCreatedBy())
                .updatedBy(project.getUpdatedBy())
                .build();
        
        if (project.getTeam() != null) {
            dto.setTeamId(project.getTeam().getId());
            dto.setTeamName(project.getTeam().getName());
        }
        
        // Map members
        if (project.getMembers() != null && !project.getMembers().isEmpty()) {
            dto.setMembers(project.getMembers().stream()
                    .map(user -> ProjectDTO.MemberDTO.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .avatarUrl(user.getAvatarUrl())
                            .build())
                    .collect(Collectors.toList()));
            dto.setTotalMembers((long) project.getMembers().size());
        }
        
        return dto;
    }

    private ProjectSectionDTO mapSectionToDTO(ProjectSection section) {
        return ProjectSectionDTO.builder()
                .id(section.getId())
                .name(section.getName())
                .position(section.getPosition())
                .projectId(section.getProject().getId())
                .projectName(section.getProject().getName())
                .taskCount(section.getTaskCount())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .createdBy(section.getCreatedBy())
                .updatedBy(section.getUpdatedBy())
                .build();
    }
}