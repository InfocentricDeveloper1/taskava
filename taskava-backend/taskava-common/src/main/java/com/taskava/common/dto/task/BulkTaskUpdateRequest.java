package com.taskava.common.dto.task;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkTaskUpdateRequest {
    
    @NotEmpty(message = "Task IDs are required")
    private Set<UUID> taskIds;
    
    @NotNull(message = "Operation is required")
    private BulkOperation operation;
    
    // Fields for update operation
    private String status;
    private String priority;
    private UUID assigneeId;
    private LocalDate dueDate;
    private Set<UUID> tagIds;
    private UUID projectId;
    private UUID sectionId;
    private Map<UUID, Object> customFieldValues; // customFieldId -> value
    
    // Fields for specific operations
    private Set<UUID> addFollowerIds;
    private Set<UUID> removeFollowerIds;
    private Set<UUID> addProjectIds;
    private Set<UUID> removeProjectIds;
    
    public enum BulkOperation {
        UPDATE_STATUS,
        UPDATE_PRIORITY,
        UPDATE_ASSIGNEE,
        UPDATE_DUE_DATE,
        ADD_TAGS,
        REMOVE_TAGS,
        MOVE_TO_PROJECT,
        MOVE_TO_SECTION,
        UPDATE_CUSTOM_FIELDS,
        ADD_FOLLOWERS,
        REMOVE_FOLLOWERS,
        ADD_TO_PROJECTS,
        REMOVE_FROM_PROJECTS,
        COMPLETE,
        DELETE,
        ARCHIVE
    }
}