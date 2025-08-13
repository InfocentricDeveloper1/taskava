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
public class AddTaskDependencyRequest {
    
    @NotNull(message = "Predecessor task ID is required")
    private UUID predecessorId;
    
    @Builder.Default
    private String dependencyType = "finish_start"; // finish_start, finish_finish, start_start, start_finish
    
    @Builder.Default
    private Integer lagDays = 0;
}