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
public class TaskProjectDTO {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private UUID sectionId;
    private String sectionName;
    private Integer position;
    private Instant addedAt;
    private UUID addedBy;
}