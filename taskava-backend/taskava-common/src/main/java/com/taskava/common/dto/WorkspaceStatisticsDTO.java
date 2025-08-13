package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Workspace statistics and metrics")
public class WorkspaceStatisticsDTO {

    @Schema(description = "Workspace ID")
    private UUID workspaceId;

    @Schema(description = "Workspace name")
    private String workspaceName;

    @Schema(description = "Total number of members")
    private Long totalMembers;

    @Schema(description = "Total number of teams")
    private Long totalTeams;

    @Schema(description = "Total number of projects")
    private Long totalProjects;

    @Schema(description = "Number of active projects")
    private Long activeProjects;

    @Schema(description = "Number of archived projects")
    private Long archivedProjects;

    @Schema(description = "Total number of tasks")
    private Long totalTasks;

    @Schema(description = "Number of completed tasks")
    private Long completedTasks;

    @Schema(description = "Number of pending tasks")
    private Long pendingTasks;

    @Schema(description = "Number of overdue tasks")
    private Long overdueTasks;

    @Schema(description = "Number of tasks due today")
    private Long tasksDueToday;

    @Schema(description = "Number of tasks due this week")
    private Long tasksDueThisWeek;

    @Schema(description = "Storage used in bytes")
    private Long storageUsedBytes;

    @Schema(description = "Storage limit in bytes")
    private Long storageLimitBytes;

    @Schema(description = "Number of custom fields defined")
    private Long customFieldsCount;

    @Schema(description = "Number of active integrations")
    private Long integrationsCount;
}