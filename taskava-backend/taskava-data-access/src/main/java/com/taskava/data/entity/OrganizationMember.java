package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organization_members",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_org_member", columnNames = {"organization_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_org_member_org", columnList = "organization_id"),
           @Index(name = "idx_org_member_user", columnList = "user_id"),
           @Index(name = "idx_org_member_role", columnList = "role")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class OrganizationMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private OrganizationRole role = OrganizationRole.MEMBER;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public enum OrganizationRole {
        OWNER,      // Can delete organization, manage billing
        ADMIN,      // Can manage all settings, users, workspaces
        MEMBER,     // Can create workspaces, access assigned workspaces
        GUEST       // Limited access, can't create workspaces
    }

    @PrePersist
    protected void onPrePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}