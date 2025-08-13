package com.taskava.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    private UUID assigneeId;
    private UUID sectionId;
    
    private String status;
    private String priority;
    
    private LocalDate startDate;
    private LocalDate dueDate;
    
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private Integer progressPercentage;
    private Integer storyPoints;
    
    private Set<UUID> projectIds;
    private Set<UUID> followerIds;
    private Set<UUID> tagIds;
    
    private Map<UUID, String> customFieldValues;
    
    private TaskDTO.RecurrenceSettingsDTO recurrenceSettings;
}