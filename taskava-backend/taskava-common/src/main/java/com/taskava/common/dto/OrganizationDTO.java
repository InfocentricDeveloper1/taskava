package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationDTO {
    
    private UUID id;
    
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
    private String name;
    
    private String displayName;
    
    @Size(max = 255, message = "Domain must not exceed 255 characters")
    private String domain;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String logoUrl;
    private String planType;
    private String status;
    private Integer maxUsers;
    private Integer maxProjects;
    private Integer maxStorageGb;
    private Map<String, String> settings;
    private Set<String> enabledFeatures;
    private Long totalUsers;
    private Long totalWorkspaces;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}