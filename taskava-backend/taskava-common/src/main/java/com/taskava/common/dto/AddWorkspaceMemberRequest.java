package com.taskava.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request to add a member to a workspace")
public class AddWorkspaceMemberRequest {

    @Schema(description = "User ID to add as member (if user exists in system)")
    private UUID userId;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address to invite (if user doesn't exist yet)")
    private String email;

    @NotNull(message = "Role is required")
    @Schema(description = "Role to assign to the member", example = "MEMBER",
            allowableValues = {"ADMIN", "MEMBER", "GUEST"})
    private String role;

    @Schema(description = "Whether the member can create projects")
    @Builder.Default
    private Boolean canCreateProjects = false;

    @Schema(description = "Whether the member can invite other members")
    @Builder.Default
    private Boolean canInviteMembers = false;

    @Schema(description = "Custom message to include in the invitation email")
    private String invitationMessage;
}