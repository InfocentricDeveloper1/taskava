package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceDTO {
    
    private UUID id;
    
    @NotBlank(message = "Workspace name is required")
    @Size(min = 2, max = 255, message = "Workspace name must be between 2 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String color;
    private String icon;
    private String visibility;
    
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;
    
    private String organizationName;
    private Long totalMembers;
    private Long totalTeams;
    private Long totalProjects;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}