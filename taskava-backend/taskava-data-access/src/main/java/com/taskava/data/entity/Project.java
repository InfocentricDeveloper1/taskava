package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projects",
       indexes = {
           @Index(name = "idx_project_workspace", columnList = "workspace_id"),
           @Index(name = "idx_project_team", columnList = "team_id"),
           @Index(name = "idx_project_status", columnList = "status"),
           @Index(name = "idx_project_deleted", columnList = "is_deleted")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_project_workspace_name", 
                            columnNames = {"workspace_id", "name", "is_deleted"})
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class Project extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color", length = 7)
    @Builder.Default
    private String color = "#1e90ff";

    @Column(name = "icon", length = 50)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_update", columnDefinition = "jsonb")
    private Map<String, Object> statusUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false, length = 50)
    @Builder.Default
    private ProjectPrivacy privacy = ProjectPrivacy.TEAM_VISIBLE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> settings = new HashMap<>();

    @Column(name = "archived_at")
    private Instant archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, 
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private Set<ProjectSection> sections = new HashSet<>();

    // For future multi-homing support - tasks can belong to multiple projects
    @ManyToMany
    @JoinTable(
        name = "task_projects",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "project_custom_fields",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "custom_field_id")
    )
    @Builder.Default
    private Set<CustomField> customFields = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Form> forms = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AutomationRule> automationRules = new HashSet<>();

    public enum ProjectStatus {
        ACTIVE,      // Project is actively being worked on
        ARCHIVED,    // Project is archived but not deleted
        COMPLETED    // Project is completed
    }

    public enum ProjectPrivacy {
        TEAM_VISIBLE,        // Visible to team members only
        WORKSPACE_VISIBLE,   // Visible to all workspace members
        PUBLIC              // Public to everyone with link
    }

    /**
     * Archive this project
     */
    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
        this.archivedAt = Instant.now();
    }

    /**
     * Restore this project from archive
     */
    public void restore() {
        this.status = ProjectStatus.ACTIVE;
        this.archivedAt = null;
    }

    /**
     * Mark project as completed
     */
    public void complete() {
        this.status = ProjectStatus.COMPLETED;
    }

    /**
     * Add a section to this project
     */
    public ProjectSection addSection(String name, Integer position) {
        ProjectSection section = ProjectSection.builder()
                .name(name)
                .position(position != null ? position : sections.size())
                .project(this)
                .build();
        sections.add(section);
        return section;
    }

    /**
     * Check if user is a member of this project
     */
    public boolean isMember(UUID userId) {
        return members.stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

    /**
     * Check if project is in workspace
     */
    public boolean isInWorkspace(UUID workspaceId) {
        return workspace != null && workspace.getId().equals(workspaceId);
    }
}