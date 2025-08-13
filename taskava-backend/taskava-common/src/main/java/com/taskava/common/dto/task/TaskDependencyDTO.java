package com.taskava.common.dto.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDependencyDTO {
    private UUID id;
    private UUID predecessorId;
    private String predecessorTitle;
    private String predecessorStatus;
    private UUID successorId;
    private String successorTitle;
    private String successorStatus;
    private String dependencyType; // finish_start, finish_finish, start_start, start_finish
    private Integer lagDays;
    private Instant createdAt;
    private UUID createdBy;
}