package com.taskava.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @NotNull(message = "At least one project is required")
    private Set<UUID> projectIds;
    
    private UUID sectionId;
    private UUID assigneeId;
    private UUID parentTaskId;
    
    @Builder.Default
    private String status = "TODO";
    
    @Builder.Default
    private String priority = "MEDIUM";
    
    private LocalDate startDate;
    private LocalDate dueDate;
    
    private BigDecimal estimatedHours;
    private Integer storyPoints;
    
    private Set<UUID> followerIds;
    private Set<UUID> tagIds;
    private Set<UUID> dependencyIds;
    
    private Map<UUID, Object> customFieldValues; // Support different value types
    
    private TaskDTO.RecurrenceSettingsDTO recurrenceSettings;
    
    private List<CreateSubtaskRequest> subtasks;
    
    @Builder.Default
    private String taskType = "task"; // task, milestone, approval
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSubtaskRequest {
        @NotBlank
        private String title;
        private String description;
        private UUID assigneeId;
        @Builder.Default
        private String priority = "MEDIUM";
        private LocalDate dueDate;
    }
}