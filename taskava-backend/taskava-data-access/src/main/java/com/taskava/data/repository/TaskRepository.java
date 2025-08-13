package com.taskava.data.repository;

import com.taskava.data.entity.Task;
import com.taskava.data.entity.Task.TaskStatus;
import com.taskava.data.entity.Task.Priority;
import com.taskava.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    @Modifying
    @Query("UPDATE Task t SET t.isDeleted = true WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id);
    
    @EntityGraph(attributePaths = {"assignee", "createdByUser", "projects", "tags", "followers"})
    Optional<Task> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT DISTINCT t FROM Task t JOIN t.projects p WHERE p.workspace.id = :workspaceId AND t.isDeleted = false")
    Page<Task> findByWorkspaceId(@Param("workspaceId") UUID workspaceId, Pageable pageable);
    
    @Query("SELECT t FROM Task t JOIN t.projects p WHERE p.id = :projectId AND t.isDeleted = false")
    Page<Task> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId AND t.isDeleted = false")
    Page<Task> findByAssigneeId(@Param("assigneeId") UUID assigneeId, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.isDeleted = false")
    Page<Task> findByStatus(@Param("status") TaskStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.parentTask.id = :parentTaskId AND t.isDeleted = false ORDER BY t.taskNumber")
    List<Task> findSubtasksByParentTaskId(@Param("parentTaskId") UUID parentTaskId);
    
    @Query("SELECT t FROM Task t JOIN t.dependencies d WHERE d.id = :taskId AND t.isDeleted = false")
    List<Task> findDependentTasks(@Param("taskId") UUID taskId);
    
    @Query("SELECT t FROM Task t JOIN t.dependents d WHERE d.id = :taskId AND t.isDeleted = false")
    List<Task> findDependencies(@Param("taskId") UUID taskId);
    
    @Query("SELECT MAX(t.taskNumber) FROM Task t JOIN t.projects p WHERE p.workspace.id = :workspaceId")
    Optional<Long> findMaxTaskNumberByWorkspaceId(@Param("workspaceId") UUID workspaceId);
    
    @Query("""
        SELECT t FROM Task t 
        WHERE (:projectId IS NULL OR t.id IN (SELECT tp.task.id FROM TaskProject tp WHERE tp.project.id = :projectId))
        AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:startDateFrom IS NULL OR t.startDate >= :startDateFrom)
        AND (:startDateTo IS NULL OR t.startDate <= :startDateTo)
        AND (:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom)
        AND (:dueDateTo IS NULL OR t.dueDate <= :dueDateTo)
        AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND t.isDeleted = false
    """)
    Page<Task> findWithFilters(
        @Param("projectId") UUID projectId,
        @Param("assigneeId") UUID assigneeId,
        @Param("status") TaskStatus status,
        @Param("priority") Priority priority,
        @Param("startDateFrom") LocalDate startDateFrom,
        @Param("startDateTo") LocalDate startDateTo,
        @Param("dueDateFrom") LocalDate dueDateFrom,
        @Param("dueDateTo") LocalDate dueDateTo,
        @Param("search") String search,
        Pageable pageable
    );
    
    @Query("""
        SELECT t FROM Task t 
        JOIN t.followers f 
        WHERE f.id = :userId AND t.isDeleted = false
    """)
    Page<Task> findTasksFollowedByUser(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("""
        SELECT t FROM Task t 
        WHERE t.dueDate = :date 
        AND t.status != 'COMPLETED' 
        AND t.status != 'CANCELLED'
        AND t.isDeleted = false
    """)
    List<Task> findTasksDueOn(@Param("date") LocalDate date);
    
    @Query("""
        SELECT t FROM Task t 
        WHERE t.dueDate < :date 
        AND t.status != 'COMPLETED' 
        AND t.status != 'CANCELLED'
        AND t.isDeleted = false
    """)
    List<Task> findOverdueTasks(@Param("date") LocalDate date);
    
    @Modifying
    @Query("UPDATE Task t SET t.isDeleted = true, t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void softDelete(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE Task t SET t.status = :status, t.completedAt = CASE WHEN :status = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE NULL END WHERE t.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.id = :childId AND t.parentTask.id = :parentId")
    boolean isSubtaskOf(@Param("childId") UUID childId, @Param("parentId") UUID parentId);
    
    @Query(value = """
        WITH RECURSIVE task_hierarchy AS (
            SELECT id, parent_task_id, 0 as depth
            FROM tasks
            WHERE id = :taskId
            
            UNION ALL
            
            SELECT t.id, t.parent_task_id, th.depth + 1
            FROM tasks t
            INNER JOIN task_hierarchy th ON t.parent_task_id = th.id
            WHERE th.depth < 10
        )
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM task_hierarchy
        WHERE parent_task_id = :taskId AND id != :taskId
    """, nativeQuery = true)
    boolean hasCircularDependency(@Param("taskId") UUID taskId);
}