package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_projects",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_task_project", 
                            columnNames = {"task_id", "project_id"})
       },
       indexes = {
           @Index(name = "idx_task_projects_task", columnList = "task_id"),
           @Index(name = "idx_task_projects_project", columnList = "project_id"),
           @Index(name = "idx_task_projects_position", columnList = "project_id, position")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private ProjectSection section;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(name = "added_at", nullable = false)
    @Builder.Default
    private Instant addedAt = Instant.now();

    @Column(name = "added_by")
    private UUID addedBy;

    /**
     * Move task to a different section within the same project
     */
    public void moveToSection(ProjectSection newSection) {
        if (newSection != null && !newSection.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("Section must belong to the same project");
        }
        this.section = newSection;
    }

    /**
     * Update the position of the task within the project/section
     */
    public void updatePosition(Integer newPosition) {
        this.position = newPosition;
    }
}