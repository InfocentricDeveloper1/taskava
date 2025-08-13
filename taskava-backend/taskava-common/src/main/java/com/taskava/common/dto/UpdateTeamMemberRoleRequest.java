package com.taskava.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateTeamMemberRoleRequest {
    
    @NotNull(message = "Role is required")
    @Pattern(regexp = "^(LEAD|MEMBER|GUEST)$", message = "Role must be LEAD, MEMBER, or GUEST")
    private String role;
}