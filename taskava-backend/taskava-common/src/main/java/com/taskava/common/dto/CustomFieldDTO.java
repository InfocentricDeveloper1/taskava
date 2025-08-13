package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Custom field definition for tasks and projects")
public class CustomFieldDTO {

    @Schema(description = "Unique identifier of the custom field")
    private UUID id;

    @Schema(description = "Display name of the field", example = "Priority")
    private String name;

    @Schema(description = "Unique key for the field (auto-generated from name)", example = "priority")
    private String fieldKey;

    @Schema(description = "Type of the custom field", example = "SELECT",
            allowableValues = {"TEXT", "NUMBER", "DATE", "DATETIME", "BOOLEAN", 
                            "SELECT", "MULTI_SELECT", "USER", "CURRENCY", 
                            "PERCENTAGE", "URL", "EMAIL", "PHONE"})
    private String fieldType;

    @Schema(description = "Description of what this field represents")
    private String description;

    @Schema(description = "Whether this field is required")
    private boolean required;

    @Schema(description = "Default value for the field")
    private String defaultValue;

    @Schema(description = "Available options for SELECT/MULTI_SELECT fields")
    private List<String> options;

    @Schema(description = "Validation rules for the field")
    private Map<String, Object> validationRules;

    @Schema(description = "ID of the workspace this field belongs to")
    private UUID workspaceId;

    @Schema(description = "Display order of the field")
    private Integer displayOrder;

    @Schema(description = "Whether the field is active")
    @Builder.Default
    private boolean active = true;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of user who created the field")
    private UUID createdBy;

    @Schema(description = "ID of user who last updated the field")
    private UUID updatedBy;
}