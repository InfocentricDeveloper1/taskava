package com.taskava.service;

import com.taskava.common.dto.task.*;
import com.taskava.service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    
    // Basic CRUD operations
    Page<TaskDTO> getTasks(UUID projectId, UUID assigneeId, String status, Pageable pageable);
    TaskDTO getTaskById(UUID id);
    TaskDTO createTask(CreateTaskRequest request);
    TaskDTO updateTask(UUID id, UpdateTaskRequest request);
    void deleteTask(UUID id);
    
    // Task status management
    TaskDTO completeTask(UUID id);
    TaskDTO uncompleteTask(UUID id);
    TaskDTO updateTaskStatus(UUID taskId, String status);
    
    // Advanced filtering
    Page<TaskDTO> getTasksWithFilters(TaskFilterRequest filter, Pageable pageable);
    
    // Assignee management
    TaskDTO assignTask(UUID taskId, UUID userId);
    TaskDTO addAssignee(UUID taskId, AddTaskAssigneeRequest request);
    TaskDTO removeAssignee(UUID taskId, UUID userId);
    List<TaskAssigneeDTO> getAssignees(UUID taskId);
    
    // Multi-homing (task-project relationships)
    TaskDTO addTaskToProject(UUID taskId, UUID projectId);
    TaskDTO addToProject(UUID taskId, AddTaskToProjectRequest request);
    TaskDTO removeFromProject(UUID taskId, UUID projectId);
    TaskDTO moveToSection(UUID taskId, UUID projectId, MoveTaskToSectionRequest request);
    
    // Subtask management
    TaskDTO createSubtask(UUID parentTaskId, CreateSubtaskRequest request);
    List<TaskDTO> getSubtasks(UUID parentTaskId);
    TaskDTO promoteSubtaskToTask(UUID subtaskId);
    
    // Dependency management
    TaskDependencyDTO addDependency(UUID taskId, AddTaskDependencyRequest request);
    TaskDTO addDependency(UUID taskId, UUID dependsOnTaskId); // Legacy support
    void removeDependency(UUID taskId, UUID dependsOnTaskId);
    List<TaskDependencyDTO> getDependencies(UUID taskId);
    
    // Custom fields
    TaskDTO updateCustomField(UUID taskId, UUID fieldId, UpdateCustomFieldValueRequest request);
    
    // Task operations
    TaskDTO duplicateTask(UUID taskId, DuplicateTaskRequest request);
    List<TaskDTO> bulkUpdateTasks(BulkTaskUpdateRequest request);
    
    // Comments
    CommentDTO addComment(UUID taskId, CreateCommentRequest request);
    
    // Query methods
    List<TaskDTO> getTasksByProject(UUID projectId);
    List<TaskDTO> getTasksByUser(UUID userId);
    List<TaskDTO> searchTasks(String query, UUID workspaceId);
    
    // Legacy support - deprecated but maintained for backward compatibility
    @Deprecated
    TaskDTO createSubtask(UUID parentTaskId, CreateTaskRequest request);
}