package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new workspace")
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
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

    @NotNull(message = "Organization ID is required")
    @Schema(description = "ID of the organization this workspace belongs to")
    private UUID organizationId;

    @Schema(description = "Visibility of the workspace", example = "PRIVATE", 
            allowableValues = {"PUBLIC", "ORGANIZATION", "PRIVATE"})
    @Builder.Default
    private String visibility = "PRIVATE";

    @Schema(description = "Whether to enable time tracking for this workspace")
    @Builder.Default
    private Boolean enableTimeTracking = false;

    @Schema(description = "Whether to enable custom fields for this workspace")
    @Builder.Default
    private Boolean enableCustomFields = true;

    @Schema(description = "Default project view for this workspace", 
            allowableValues = {"LIST", "BOARD", "TIMELINE", "CALENDAR"})
    @Builder.Default
    private String defaultProjectView = "LIST";
}