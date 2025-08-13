package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Activity log entry for workspace events")
public class ActivityDTO {

    @Schema(description = "Activity ID")
    private UUID id;

    @Schema(description = "Type of activity", example = "TASK_CREATED",
            allowableValues = {"TASK_CREATED", "TASK_UPDATED", "TASK_COMPLETED", "TASK_DELETED",
                            "PROJECT_CREATED", "PROJECT_UPDATED", "PROJECT_ARCHIVED",
                            "MEMBER_ADDED", "MEMBER_REMOVED", "MEMBER_ROLE_CHANGED",
                            "COMMENT_ADDED", "ATTACHMENT_ADDED", "CUSTOM_FIELD_UPDATED"})
    private String activityType;

    @Schema(description = "Entity type that was affected", example = "TASK",
            allowableValues = {"WORKSPACE", "PROJECT", "TASK", "TEAM", "USER", "COMMENT", "ATTACHMENT"})
    private String entityType;

    @Schema(description = "ID of the affected entity")
    private UUID entityId;

    @Schema(description = "Name of the affected entity")
    private String entityName;

    @Schema(description = "User who performed the action")
    private UUID userId;

    @Schema(description = "Name of the user who performed the action")
    private String userName;

    @Schema(description = "Avatar URL of the user who performed the action")
    private String userAvatar;

    @Schema(description = "Description of the activity")
    private String description;

    @Schema(description = "Additional metadata about the activity")
    private Map<String, Object> metadata;

    @Schema(description = "Previous values (for update activities)")
    private Map<String, Object> oldValues;

    @Schema(description = "New values (for update activities)")
    private Map<String, Object> newValues;

    @Schema(description = "Workspace where the activity occurred")
    private UUID workspaceId;

    @Schema(description = "Project where the activity occurred (if applicable)")
    private UUID projectId;

    @Schema(description = "Team where the activity occurred (if applicable)")
    private UUID teamId;

    @Schema(description = "When the activity occurred")
    private LocalDateTime createdAt;

    @Schema(description = "IP address of the user (for security tracking)")
    private String ipAddress;

    @Schema(description = "User agent (for security tracking)")
    private String userAgent;
}