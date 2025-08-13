package com.taskava.common.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomFieldValueRequest {
    
    @NotNull(message = "Custom field ID is required")
    private UUID customFieldId;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private String textValue;
    private Double numberValue;
    private String dateValue; // ISO date string
    private Boolean booleanValue;
    private Object jsonValue; // For complex types like multi-select, people, etc.
}