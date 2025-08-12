package com.taskava.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {
    private UUID id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private String status;
    private String priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer progressPercentage;
    private UserDTO owner;
}