package com.taskava.common.dto;

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
public class TeamMemberDTO {
    
    private UUID id;
    private UUID teamId;
    private String teamName;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String userAvatar;
    private String role; // LEAD, MEMBER, GUEST
    private UUID invitedBy;
    private String invitedByName;
    private Instant invitedAt;
    private Instant joinedAt;
    private boolean active;
}