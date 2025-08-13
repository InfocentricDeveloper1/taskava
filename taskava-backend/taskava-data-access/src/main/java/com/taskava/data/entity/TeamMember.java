package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "team_members",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_team_member", columnNames = {"team_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_team_member_team", columnList = "team_id"),
           @Index(name = "idx_team_member_user", columnList = "user_id"),
           @Index(name = "idx_team_member_role", columnList = "role")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class TeamMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private TeamRole role = TeamRole.MEMBER;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public enum TeamRole {
        LEAD,       // Team lead, can manage team settings and members
        MEMBER,     // Regular team member
        GUEST       // Guest member with limited access
    }

    @PrePersist
    protected void onPrePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}