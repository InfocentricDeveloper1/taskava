package com.taskava.common.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubtaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    private String description;
    
    @Builder.Default
    private String status = "TODO";
    
    @Builder.Default
    private String priority = "MEDIUM";
    
    private LocalDate startDate;
    
    private LocalDate dueDate;
    
    private UUID assigneeId;
    
    private BigDecimal estimatedHours;
    
    private Integer storyPoints;
}