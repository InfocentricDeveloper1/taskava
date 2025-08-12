package com.taskava.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDTO {
    
    private UUID id;
    private String title;
    private String description;
    private Long taskNumber;
    private String status;
    private String priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Instant completedAt;
    private Double estimatedHours;
    private Double actualHours;
    private Integer progressPercentage;
    private Integer storyPoints;
    
    // Relationships
    private UserDTO assignee;
    private UserDTO createdBy;
    private List<ProjectDTO> projects;
    private SectionDTO section;
    private TaskDTO parentTask;
    private List<TaskDTO> subtasks;
    private Set<TaskDTO> dependencies;
    private Set<UserDTO> followers;
    private List<TagDTO> tags;
    private List<AttachmentDTO> attachments;
    private Integer commentCount;
    
    // Custom fields
    private Map<String, Object> customFields;
    
    // Recurrence
    private boolean recurring;
    private RecurrenceSettingsDTO recurrenceSettings;
    
    // Metadata
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdById;
    private UUID updatedById;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurrenceSettingsDTO {
        private String type;
        private Integer interval;
        private List<String> daysOfWeek;
        private Integer dayOfMonth;
        private LocalDate endDate;
        private Integer maxOccurrences;
    }
}