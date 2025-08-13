package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tasks",
       indexes = {
           @Index(name = "idx_task_assignee", columnList = "assignee_id"),
           @Index(name = "idx_task_created_by", columnList = "created_by_user_id"),
           @Index(name = "idx_task_parent", columnList = "parent_task_id"),
           @Index(name = "idx_task_deleted", columnList = "is_deleted"),
           @Index(name = "idx_task_status", columnList = "status"),
           @Index(name = "idx_task_due_date", columnList = "due_date")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class Task extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_number", nullable = false)
    private Long taskNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 10, scale = 2)
    private BigDecimal actualHours;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "story_points")
    private Integer storyPoints;

    // Multi-homing support - task can belong to multiple projects
    @ManyToMany
    @JoinTable(
        name = "task_projects",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    // Parent-child relationship for subtasks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Task> subtasks = new HashSet<>();

    // Task dependencies
    @ManyToMany
    @JoinTable(
        name = "task_dependencies",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "depends_on_task_id")
    )
    private Set<Task> dependencies = new HashSet<>();

    @ManyToMany(mappedBy = "dependencies")
    private Set<Task> dependents = new HashSet<>();

    // Collaborators
    @ManyToMany
    @JoinTable(
        name = "task_followers",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> followers = new HashSet<>();

    // Tags
    @ManyToMany
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Attachments
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Attachment> attachments = new HashSet<>();

    // Comments
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    // Custom fields
    @ElementCollection
    @CollectionTable(name = "task_custom_field_values",
                    joinColumns = @JoinColumn(name = "task_id"))
    @MapKeyJoinColumn(name = "custom_field_id")
    @Column(name = "field_value", columnDefinition = "TEXT")
    private Map<CustomField, String> customFieldValues = new HashMap<>();

    // Recurring task settings
    @Embedded
    private RecurrenceSettings recurrenceSettings;

    @Column(name = "is_recurring")
    private boolean recurring = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_task_template_id")
    private Task recurringTaskTemplate;

    public enum TaskStatus {
        TODO, IN_PROGRESS, IN_REVIEW, BLOCKED, COMPLETED, CANCELLED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @Embeddable
    @Data
    public static class RecurrenceSettings {
        @Enumerated(EnumType.STRING)
        @Column(name = "recurrence_type")
        private RecurrenceType type;
        
        @Column(name = "recurrence_interval")
        private Integer interval;
        
        @Column(name = "recurrence_days_of_week")
        private String daysOfWeek; // JSON array
        
        @Column(name = "recurrence_day_of_month")
        private Integer dayOfMonth;
        
        @Column(name = "recurrence_end_date")
        private LocalDate endDate;
        
        @Column(name = "recurrence_max_occurrences")
        private Integer maxOccurrences;
    }

    public enum RecurrenceType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}