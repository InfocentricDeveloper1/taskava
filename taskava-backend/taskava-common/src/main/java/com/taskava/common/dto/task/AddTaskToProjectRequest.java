package com.taskava.common.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTaskToProjectRequest {
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private UUID sectionId;
    
    private Integer position;
}