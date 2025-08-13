package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project_sections",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_project_section_position", 
                            columnNames = {"project_id", "position"})
       },
       indexes = {
           @Index(name = "idx_section_project", columnList = "project_id"),
           @Index(name = "idx_section_position", columnList = "position")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class ProjectSection extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "position", nullable = false)
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Tasks in this section (for the multi-homing architecture)
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TaskProject> taskProjects = new HashSet<>();

    /**
     * Move this section to a new position
     */
    public void moveTo(Integer newPosition) {
        this.position = newPosition;
    }

    /**
     * Get the number of tasks in this section
     */
    public int getTaskCount() {
        return taskProjects != null ? taskProjects.size() : 0;
    }

    /**
     * Check if this section belongs to the specified project
     */
    public boolean belongsToProject(Project proj) {
        return project != null && project.getId().equals(proj.getId());
    }
}