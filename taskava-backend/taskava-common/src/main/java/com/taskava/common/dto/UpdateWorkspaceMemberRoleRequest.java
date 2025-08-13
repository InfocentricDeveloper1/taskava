package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a workspace member's role")
public class UpdateWorkspaceMemberRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New role for the member", example = "ADMIN",
            allowableValues = {"ADMIN", "MEMBER", "GUEST"})
    private String role;

    @Schema(description = "Whether the member can create projects")
    private Boolean canCreateProjects;

    @Schema(description = "Whether the member can invite other members")
    private Boolean canInviteMembers;
}