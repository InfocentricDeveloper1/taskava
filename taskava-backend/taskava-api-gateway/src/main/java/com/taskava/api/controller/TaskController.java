package com.taskava.api.controller;

import com.taskava.common.dto.BaseResponse;
import com.taskava.common.dto.task.*;
import com.taskava.service.TaskService;
import com.taskava.service.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TaskController {

    private final TaskService taskService;

    // ===== Basic Task Operations =====

    @GetMapping("/tasks")
    @Operation(summary = "Get all tasks", description = "Retrieve a paginated list of tasks with optional filters")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<Page<TaskDTO>>> getAllTasks(
            @ModelAttribute TaskFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting tasks with filters");
        
        Page<TaskDTO> tasks = taskService.getTasksWithFilters(filter, pageable);
        
        BaseResponse<Page<TaskDTO>> response = BaseResponse.success(tasks, "Tasks retrieved successfully");
        response.setPageInfo(BaseResponse.PageInfo.builder()
                .page(tasks.getNumber())
                .size(tasks.getSize())
                .totalElements(tasks.getTotalElements())
                .totalPages(tasks.getTotalPages())
                .hasNext(tasks.hasNext())
                .hasPrevious(tasks.hasPrevious())
                .build());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a single task by its ID with all details")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.debug("Getting task with id: {}", id);
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(BaseResponse.success(task, "Task retrieved successfully"));
    }

    @PostMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Create task in project", description = "Create a new task in a specific project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> createTaskInProject(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        
        log.info("Creating new task in project: {}", projectId);
        
        // Ensure the task is created in the specified project
        if (request.getProjectIds() == null || request.getProjectIds().isEmpty()) {
            request.setProjectIds(Set.of(projectId));
        } else {
            request.getProjectIds().add(projectId);
        }
        
        TaskDTO createdTask = taskService.createTask(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(createdTask, "Task created successfully"));
    }

    @PostMapping("/tasks")
    @Operation(summary = "Create new task", description = "Create a new task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        
        log.info("Creating new task: {}", request.getTitle());
        TaskDTO createdTask = taskService.createTask(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(createdTask, "Task created successfully"));
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Update task", description = "Update an existing task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> updateTask(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        
        log.info("Updating task with id: {}", id);
        TaskDTO updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(BaseResponse.success(updatedTask, "Task updated successfully"));
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Delete task", description = "Soft delete a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.info("Deleting task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Task deleted successfully"));
    }

    // ===== Task Status Management =====

    @PostMapping("/tasks/{id}/complete")
    @Operation(summary = "Mark task complete", description = "Mark a task as completed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> completeTask(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.info("Completing task: {}", id);
        TaskDTO task = taskService.completeTask(id);
        return ResponseEntity.ok(BaseResponse.success(task, "Task completed successfully"));
    }

    @DeleteMapping("/tasks/{id}/complete")
    @Operation(summary = "Mark task incomplete", description = "Mark a task as incomplete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> uncompleteTask(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.info("Marking task as incomplete: {}", id);
        TaskDTO task = taskService.uncompleteTask(id);
        return ResponseEntity.ok(BaseResponse.success(task, "Task marked as incomplete"));
    }

    @PutMapping("/tasks/{id}/status")
    @Operation(summary = "Update task status", description = "Update the status of a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> updateTaskStatus(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "New status") @RequestParam String status) {
        
        log.info("Updating task {} status to {}", id, status);
        TaskDTO task = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(BaseResponse.success(task, "Task status updated successfully"));
    }

    // ===== Assignee Management =====

    @PostMapping("/tasks/{id}/assignees")
    @Operation(summary = "Add assignee", description = "Add an assignee to a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> addAssignee(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody AddTaskAssigneeRequest request) {
        
        log.info("Adding assignee to task: {}", id);
        TaskDTO task = taskService.addAssignee(id, request);
        return ResponseEntity.ok(BaseResponse.success(task, "Assignee added successfully"));
    }

    @DeleteMapping("/tasks/{id}/assignees/{userId}")
    @Operation(summary = "Remove assignee", description = "Remove an assignee from a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> removeAssignee(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        log.info("Removing assignee {} from task {}", userId, id);
        TaskDTO task = taskService.removeAssignee(id, userId);
        return ResponseEntity.ok(BaseResponse.success(task, "Assignee removed successfully"));
    }

    @GetMapping("/tasks/{id}/assignees")
    @Operation(summary = "List assignees", description = "Get all assignees of a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<List<TaskAssigneeDTO>>> getAssignees(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.debug("Getting assignees for task: {}", id);
        List<TaskAssigneeDTO> assignees = taskService.getAssignees(id);
        return ResponseEntity.ok(BaseResponse.success(assignees, "Assignees retrieved successfully"));
    }

    // ===== Multi-homing (Project Management) =====

    @PostMapping("/tasks/{id}/projects")
    @Operation(summary = "Add to project", description = "Add a task to a project (multi-homing)")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> addTaskToProject(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody AddTaskToProjectRequest request) {
        
        log.info("Adding task {} to project {}", id, request.getProjectId());
        TaskDTO task = taskService.addToProject(id, request);
        return ResponseEntity.ok(BaseResponse.success(task, "Task added to project successfully"));
    }

    @DeleteMapping("/tasks/{id}/projects/{projectId}")
    @Operation(summary = "Remove from project", description = "Remove a task from a project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> removeFromProject(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "Project ID") @PathVariable UUID projectId) {
        
        log.info("Removing task {} from project {}", id, projectId);
        TaskDTO task = taskService.removeFromProject(id, projectId);
        return ResponseEntity.ok(BaseResponse.success(task, "Task removed from project successfully"));
    }

    @PutMapping("/tasks/{id}/projects/{projectId}/section")
    @Operation(summary = "Move to section", description = "Move a task to a different section within a project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> moveToSection(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Valid @RequestBody MoveTaskToSectionRequest request) {
        
        log.info("Moving task {} to section in project {}", id, projectId);
        TaskDTO task = taskService.moveToSection(id, projectId, request);
        return ResponseEntity.ok(BaseResponse.success(task, "Task moved to section successfully"));
    }

    // ===== Subtask Management =====

    @PostMapping("/tasks/{id}/subtasks")
    @Operation(summary = "Create subtask", description = "Create a subtask for a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> createSubtask(
            @Parameter(description = "Parent task ID") @PathVariable UUID id,
            @Valid @RequestBody CreateSubtaskRequest request) {
        
        log.info("Creating subtask for task: {}", id);
        TaskDTO subtask = taskService.createSubtask(id, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(subtask, "Subtask created successfully"));
    }

    @GetMapping("/tasks/{id}/subtasks")
    @Operation(summary = "Get subtasks", description = "Get all subtasks of a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<List<TaskDTO>>> getSubtasks(
            @Parameter(description = "Parent task ID") @PathVariable UUID id) {
        
        log.debug("Getting subtasks for task: {}", id);
        List<TaskDTO> subtasks = taskService.getSubtasks(id);
        return ResponseEntity.ok(BaseResponse.success(subtasks, "Subtasks retrieved successfully"));
    }

    @PostMapping("/tasks/{id}/promote")
    @Operation(summary = "Promote subtask", description = "Promote a subtask to a regular task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> promoteSubtask(
            @Parameter(description = "Subtask ID") @PathVariable UUID id) {
        
        log.info("Promoting subtask {} to task", id);
        TaskDTO task = taskService.promoteSubtaskToTask(id);
        return ResponseEntity.ok(BaseResponse.success(task, "Subtask promoted successfully"));
    }

    // ===== Dependency Management =====

    @PostMapping("/tasks/{id}/dependencies")
    @Operation(summary = "Add dependency", description = "Add a dependency to a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDependencyDTO>> addDependency(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody AddTaskDependencyRequest request) {
        
        log.info("Adding dependency to task: {}", id);
        TaskDependencyDTO dependency = taskService.addDependency(id, request);
        return ResponseEntity.ok(BaseResponse.success(dependency, "Dependency added successfully"));
    }

    @DeleteMapping("/tasks/{id}/dependencies/{dependencyId}")
    @Operation(summary = "Remove dependency", description = "Remove a dependency from a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<Void>> removeDependency(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "Dependency task ID") @PathVariable UUID dependencyId) {
        
        log.info("Removing dependency {} from task {}", dependencyId, id);
        taskService.removeDependency(id, dependencyId);
        return ResponseEntity.ok(BaseResponse.success(null, "Dependency removed successfully"));
    }

    @GetMapping("/tasks/{id}/dependencies")
    @Operation(summary = "Get dependencies", description = "Get all dependencies of a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<List<TaskDependencyDTO>>> getDependencies(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.debug("Getting dependencies for task: {}", id);
        List<TaskDependencyDTO> dependencies = taskService.getDependencies(id);
        return ResponseEntity.ok(BaseResponse.success(dependencies, "Dependencies retrieved successfully"));
    }

    // ===== Custom Fields =====

    @PutMapping("/tasks/{id}/custom-fields/{fieldId}")
    @Operation(summary = "Update custom field", description = "Update a custom field value for a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> updateCustomField(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "Custom field ID") @PathVariable UUID fieldId,
            @Valid @RequestBody UpdateCustomFieldValueRequest request) {
        
        log.info("Updating custom field {} for task {}", fieldId, id);
        TaskDTO task = taskService.updateCustomField(id, fieldId, request);
        return ResponseEntity.ok(BaseResponse.success(task, "Custom field updated successfully"));
    }

    // ===== Task Operations =====

    @PostMapping("/tasks/{id}/duplicate")
    @Operation(summary = "Duplicate task", description = "Create a duplicate of an existing task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> duplicateTask(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody DuplicateTaskRequest request) {
        
        log.info("Duplicating task: {}", id);
        TaskDTO duplicate = taskService.duplicateTask(id, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(duplicate, "Task duplicated successfully"));
    }

    @PostMapping("/tasks/bulk")
    @Operation(summary = "Bulk update tasks", description = "Perform bulk operations on multiple tasks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<List<TaskDTO>>> bulkUpdateTasks(
            @Valid @RequestBody BulkTaskUpdateRequest request) {
        
        log.info("Bulk updating {} tasks", request.getTaskIds().size());
        List<TaskDTO> tasks = taskService.bulkUpdateTasks(request);
        return ResponseEntity.ok(BaseResponse.success(tasks, "Tasks updated successfully"));
    }

    // ===== Comments =====

    @PostMapping("/tasks/{id}/comments")
    @Operation(summary = "Add comment", description = "Add a comment to a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<CommentDTO>> addComment(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest request) {
        
        log.info("Adding comment to task: {}", id);
        CommentDTO comment = taskService.addComment(id, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(comment, "Comment added successfully"));
    }

    // ===== Search =====

    @GetMapping("/tasks/search")
    @Operation(summary = "Search tasks", description = "Search tasks by query")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<List<TaskDTO>>> searchTasks(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Workspace ID") @RequestParam(required = false) UUID workspaceId) {
        
        log.debug("Searching tasks with query: {}", q);
        List<TaskDTO> tasks = taskService.searchTasks(q, workspaceId);
        return ResponseEntity.ok(BaseResponse.success(tasks, "Search completed successfully"));
    }

    // ===== Legacy Endpoints (for backward compatibility) =====

    @PostMapping("/tasks/{id}/assign")
    @Operation(summary = "Assign task", description = "Assign a task to a user (legacy endpoint)")
    @PreAuthorize("hasRole('USER')")
    @Deprecated
    public ResponseEntity<BaseResponse<TaskDTO>> assignTask(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        
        log.info("Assigning task {} to user {}", id, userId);
        TaskDTO task = taskService.assignTask(id, userId);
        return ResponseEntity.ok(BaseResponse.success(task, "Task assigned successfully"));
    }

    @PostMapping("/tasks/{id}/projects/{projectId}")
    @Operation(summary = "Add task to project", description = "Add a task to a project (legacy endpoint)")
    @PreAuthorize("hasRole('USER')")
    @Deprecated
    public ResponseEntity<BaseResponse<TaskDTO>> addTaskToProjectLegacy(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "Project ID") @PathVariable UUID projectId) {
        
        log.info("Adding task {} to project {}", id, projectId);
        TaskDTO task = taskService.addTaskToProject(id, projectId);
        return ResponseEntity.ok(BaseResponse.success(task, "Task added to project successfully"));
    }
}