package com.taskava.api.controller;

import com.taskava.common.dto.BaseResponse;
import com.taskava.service.TaskService;
import com.taskava.service.dto.TaskDTO;
import com.taskava.service.dto.CreateTaskRequest;
import com.taskava.service.dto.UpdateTaskRequest;
import com.taskava.service.dto.CommentDTO;
import com.taskava.service.dto.CreateCommentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management endpoints")
@Validated
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieve a paginated list of tasks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<Page<TaskDTO>>> getAllTasks(
            @Parameter(description = "Project ID filter") @RequestParam(required = false) UUID projectId,
            @Parameter(description = "Assignee ID filter") @RequestParam(required = false) UUID assigneeId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting tasks with filters - projectId: {}, assigneeId: {}, status: {}", 
                projectId, assigneeId, status);
        
        Page<TaskDTO> tasks = taskService.getTasks(projectId, assigneeId, status, pageable);
        
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

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a single task by its ID")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.debug("Getting task with id: {}", id);
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(BaseResponse.success(task, "Task retrieved successfully"));
    }

    @PostMapping
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

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update an existing task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> updateTask(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        
        log.info("Updating task with id: {}", id);
        TaskDTO updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(BaseResponse.success(updatedTask, "Task updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Soft delete a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        
        log.info("Deleting task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Task deleted successfully"));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign task", description = "Assign a task to a user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> assignTask(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        
        log.info("Assigning task {} to user {}", id, userId);
        TaskDTO task = taskService.assignTask(id, userId);
        return ResponseEntity.ok(BaseResponse.success(task, "Task assigned successfully"));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Update the status of a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> updateTaskStatus(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "New status") @RequestParam String status) {
        
        log.info("Updating task {} status to {}", id, status);
        TaskDTO task = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(BaseResponse.success(task, "Task status updated successfully"));
    }

    @PostMapping("/{id}/projects/{projectId}")
    @Operation(summary = "Add task to project", description = "Add a task to a project (multi-homing)")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<TaskDTO>> addTaskToProject(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "Project ID") @PathVariable UUID projectId) {
        
        log.info("Adding task {} to project {}", id, projectId);
        TaskDTO task = taskService.addTaskToProject(id, projectId);
        return ResponseEntity.ok(BaseResponse.success(task, "Task added to project successfully"));
    }

    @GetMapping("/{id}/subtasks")
    @Operation(summary = "Get subtasks", description = "Get all subtasks of a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<List<TaskDTO>>> getSubtasks(
            @Parameter(description = "Parent task ID") @PathVariable UUID id) {
        
        log.debug("Getting subtasks for task: {}", id);
        List<TaskDTO> subtasks = taskService.getSubtasks(id);
        return ResponseEntity.ok(BaseResponse.success(subtasks, "Subtasks retrieved successfully"));
    }

    @PostMapping("/{id}/comments")
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
}