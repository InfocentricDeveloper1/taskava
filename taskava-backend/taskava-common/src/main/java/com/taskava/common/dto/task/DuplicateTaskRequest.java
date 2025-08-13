package com.taskava.common.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateTaskRequest {
    
    private String newTitle; // If not provided, will append " (Copy)" to original title
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private UUID sectionId;
    
    @Builder.Default
    private boolean includeSubtasks = false;
    
    @Builder.Default
    private boolean includeAttachments = false;
    
    @Builder.Default
    private boolean includeComments = false;
    
    @Builder.Default
    private boolean includeDependencies = false;
    
    @Builder.Default
    private boolean includeCustomFields = true;
    
    @Builder.Default
    private boolean includeTags = true;
    
    @Builder.Default
    private boolean includeFollowers = false;
    
    private Set<UUID> assigneeIds; // New assignees for the duplicated task
}