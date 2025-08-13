package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update workspace details")
public class UpdateWorkspaceRequest {

    @Size(min = 2, max = 100, message = "Workspace name must be between 2 and 100 characters")
    @Schema(description = "Name of the workspace", example = "Engineering Team")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the workspace", example = "Main workspace for engineering team")
    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    @Schema(description = "Color theme for the workspace", example = "#4285F4")
    private String color;

    @Schema(description = "Icon for the workspace", example = "🚀")
    private String icon;

    @Schema(description = "Visibility of the workspace", example = "PRIVATE", 
            allowableValues = {"PUBLIC", "ORGANIZATION", "PRIVATE"})
    private String visibility;

    @Schema(description = "Whether to enable time tracking for this workspace")
    private Boolean enableTimeTracking;

    @Schema(description = "Whether to enable custom fields for this workspace")
    private Boolean enableCustomFields;

    @Schema(description = "Default project view for this workspace", 
            allowableValues = {"LIST", "BOARD", "TIMELINE", "CALENDAR"})
    private String defaultProjectView;
}