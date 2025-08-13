package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_members",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_workspace_member", columnNames = {"workspace_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_workspace_member_workspace", columnList = "workspace_id"),
           @Index(name = "idx_workspace_member_user", columnList = "user_id"),
           @Index(name = "idx_workspace_member_role", columnList = "role")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class WorkspaceMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private WorkspaceRole role = WorkspaceRole.MEMBER;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "can_create_projects")
    private boolean canCreateProjects = true;

    @Column(name = "can_invite_members")
    private boolean canInviteMembers = false;

    public enum WorkspaceRole {
        ADMIN,      // Can manage workspace settings, all projects, all members
        MEMBER,     // Can access and contribute to projects
        GUEST       // Limited access, read-only by default
    }

    @PrePersist
    protected void onPrePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
        // Set default permissions based on role
        if (role == WorkspaceRole.ADMIN) {
            canCreateProjects = true;
            canInviteMembers = true;
        } else if (role == WorkspaceRole.GUEST) {
            canCreateProjects = false;
            canInviteMembers = false;
        }
    }
}