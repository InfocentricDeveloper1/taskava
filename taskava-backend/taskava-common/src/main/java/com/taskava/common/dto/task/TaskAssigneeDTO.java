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
public class TaskAssigneeDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String userAvatar;
    private Instant assignedAt;
    private UUID assignedBy;
    private String assignedByName;
}