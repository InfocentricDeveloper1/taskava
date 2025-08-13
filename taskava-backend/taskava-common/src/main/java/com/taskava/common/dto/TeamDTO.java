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
public class TeamDTO {
    
    private UUID id;
    
    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 255, message = "Team name must be between 2 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String color;
    private String icon;
    
    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;
    
    private String workspaceName;
    private UUID teamLeadId;
    private String teamLeadName;
    private Long totalMembers;
    private Long totalProjects;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}