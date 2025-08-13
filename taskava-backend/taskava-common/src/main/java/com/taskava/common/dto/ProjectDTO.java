package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {
    
    private UUID id;
    
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    private String color;
    
    @Size(max = 50, message = "Icon identifier must not exceed 50 characters")
    private String icon;
    
    private String status; // ACTIVE, ARCHIVED, COMPLETED
    
    private Map<String, Object> statusUpdate;
    
    private String privacy; // TEAM_VISIBLE, WORKSPACE_VISIBLE, PUBLIC
    
    private Map<String, Object> settings;
    
    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;
    
    private String workspaceName;
    
    private UUID teamId;
    
    private String teamName;
    
    // Sections
    private List<ProjectSectionDTO> sections;
    
    // Members
    private List<MemberDTO> members;
    
    // Statistics
    private Long totalTasks;
    private Long completedTasks;
    private Long totalMembers;
    private Long totalSections;
    private Integer progressPercentage;
    
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant archivedAt;
    private UUID createdBy;
    private UUID updatedBy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDTO {
        private UUID userId;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String role;
    }
}