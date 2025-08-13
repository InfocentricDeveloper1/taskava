package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectCreateDTO {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    @Builder.Default
    private String color = "#1e90ff";
    
    @Size(max = 50, message = "Icon identifier must not exceed 50 characters")
    private String icon;
    
    private UUID teamId;
    
    @Builder.Default
    private String privacy = "TEAM_VISIBLE"; // TEAM_VISIBLE, WORKSPACE_VISIBLE, PUBLIC
    
    private Map<String, Object> settings;
    
    // Initial sections to create
    private List<String> sectionNames;
    
    // Initial members to add (user IDs)
    private List<UUID> memberIds;
    
    // Template to copy from
    private UUID templateProjectId;
}