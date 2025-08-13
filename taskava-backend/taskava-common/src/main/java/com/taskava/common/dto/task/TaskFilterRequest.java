package com.taskava.common.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterRequest {
    
    private UUID projectId;
    private UUID assigneeId;
    private String status;
    private String priority;
    private Set<UUID> tagIds;
    private UUID parentTaskId;
    private Boolean isSubtask;
    private Boolean isCompleted;
    private Boolean isOverdue;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDateTo;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate completedFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate completedTo;
    
    private String search; // Search in title and description
    private UUID createdById;
    private UUID followerId;
    private Boolean hasAttachments;
    private Boolean hasComments;
    private Boolean hasDependencies;
    private Boolean isRecurring;
    private String taskType; // task, milestone, approval
    
    // Sorting
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}