package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
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
public class ProjectSectionDTO {
    
    private UUID id;
    
    @NotBlank(message = "Section name is required")
    @Size(min = 1, max = 255, message = "Section name must be between 1 and 255 characters")
    private String name;
    
    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be non-negative")
    private Integer position;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private String projectName;
    
    // Statistics
    private Integer taskCount;
    private Integer completedTaskCount;
    
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}