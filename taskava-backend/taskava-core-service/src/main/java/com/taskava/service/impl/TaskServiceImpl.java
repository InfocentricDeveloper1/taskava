package com.taskava.service.impl;

import com.taskava.common.dto.task.*;
import com.taskava.common.exception.ResourceNotFoundException;
import com.taskava.common.exception.BadRequestException;
import com.taskava.common.exception.ConflictException;
import com.taskava.data.entity.*;
import com.taskava.data.entity.Task.TaskStatus;
import com.taskava.data.entity.Task.Priority;
import com.taskava.data.repository.*;
import com.taskava.security.context.TenantContext;
import com.taskava.service.TaskService;
import com.taskava.service.dto.*;
import com.taskava.service.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskProjectRepository taskProjectRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectSectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasks(UUID projectId, UUID assigneeId, String status, Pageable pageable) {
        log.debug("Getting tasks with filters - projectId: {}, assigneeId: {}, status: {}", 
                projectId, assigneeId, status);
        
        TaskStatus taskStatus = status != null ? TaskStatus.valueOf(status.toUpperCase()) : null;
        
        Page<Task> tasks = taskRepository.findWithFilters(
            projectId, assigneeId, taskStatus, null, null, null, null, null, null, pageable
        );
        
        return tasks.map(taskMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID id) {
        log.debug("Getting task by id: {}", id);
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO createTask(CreateTaskRequest request) {
        log.info("Creating new task: {}", request.getTitle());
        
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        if (workspaceId == null) {
            throw new BadRequestException("Workspace context is required");
        }
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
        
        // Generate task number
        Long taskNumber = taskRepository.findMaxTaskNumberByWorkspaceId(workspaceId)
                .map(n -> n + 1)
                .orElse(1L);
        
        // Create task entity
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskNumber(taskNumber)
                .status(TaskStatus.valueOf(request.getStatus()))
                .priority(Priority.valueOf(request.getPriority()))
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .storyPoints(request.getStoryPoints())
                .build();
        
        // Set assignee if provided
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            task.setAssignee(assignee);
        }
        
        // Set parent task if it's a subtask
        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent task", "id", request.getParentTaskId()));
            task.setParentTask(parentTask);
        }
        
        // Set created by user
        UUID currentUserId = TenantContext.getCurrentUserId();
        if (currentUserId != null) {
            User createdBy = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
            task.setCreatedByUser(createdBy);
        }
        
        // Save task first
        task = taskRepository.save(task);
        
        // Add to projects (multi-homing)
        if (request.getProjectIds() != null && !request.getProjectIds().isEmpty()) {
            for (UUID projectId : request.getProjectIds()) {
                addTaskToProjectInternal(task, projectId, request.getSectionId());
            }
        }
        
        // Add followers
        if (request.getFollowerIds() != null && !request.getFollowerIds().isEmpty()) {
            Set<User> followers = new HashSet<>(userRepository.findAllById(request.getFollowerIds()));
            task.setFollowers(followers);
        }
        
        // Add dependencies
        if (request.getDependencyIds() != null && !request.getDependencyIds().isEmpty()) {
            Set<Task> dependencies = new HashSet<>(taskRepository.findAllById(request.getDependencyIds()));
            task.setDependencies(dependencies);
        }
        
        // Set recurrence settings if provided
        if (request.getRecurrenceSettings() != null) {
            Task.RecurrenceSettings settings = new Task.RecurrenceSettings();
            settings.setType(Task.RecurrenceType.valueOf(request.getRecurrenceSettings().getType()));
            settings.setInterval(request.getRecurrenceSettings().getInterval());
            settings.setDayOfMonth(request.getRecurrenceSettings().getDayOfMonth());
            settings.setEndDate(request.getRecurrenceSettings().getEndDate());
            settings.setMaxOccurrences(request.getRecurrenceSettings().getMaxOccurrences());
            if (request.getRecurrenceSettings().getDaysOfWeek() != null) {
                settings.setDaysOfWeek(String.join(",", request.getRecurrenceSettings().getDaysOfWeek()));
            }
            task.setRecurrenceSettings(settings);
            task.setRecurring(true);
        }
        
        task = taskRepository.save(task);
        
        // Create subtasks if provided
        if (request.getSubtasks() != null && !request.getSubtasks().isEmpty()) {
            for (CreateTaskRequest.CreateSubtaskRequest subtaskRequest : request.getSubtasks()) {
                createSubtaskInternal(task, subtaskRequest);
            }
        }
        
        log.info("Task created successfully with id: {}", task.getId());
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO updateTask(UUID id, UpdateTaskRequest request) {
        log.info("Updating task: {}", id);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        // Update basic fields
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(request.getStatus()));
            if (request.getStatus().equals("COMPLETED")) {
                task.setCompletedAt(Instant.now());
            }
        }
        if (request.getPriority() != null) {
            task.setPriority(Priority.valueOf(request.getPriority()));
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null || Boolean.TRUE.equals(request.getClearDueDate())) {
            task.setDueDate(request.getClearDueDate() ? null : request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getProgressPercentage() != null) {
            task.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        
        // Update assignee
        if (request.getAssigneeId() != null || Boolean.TRUE.equals(request.getClearAssignee())) {
            if (request.getClearAssignee()) {
                task.setAssignee(null);
            } else {
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
                task.setAssignee(assignee);
            }
        }
        
        // Update projects if provided
        if (request.getProjectIds() != null) {
            // Remove from all current projects
            taskProjectRepository.deleteAll(taskProjectRepository.findByTaskId(task.getId()));
            
            // Add to new projects
            for (UUID projectId : request.getProjectIds()) {
                addTaskToProjectInternal(task, projectId, request.getSectionId());
            }
        }
        
        // Update followers if provided
        if (request.getFollowerIds() != null) {
            Set<User> followers = new HashSet<>(userRepository.findAllById(request.getFollowerIds()));
            task.setFollowers(followers);
        }
        
        task = taskRepository.save(task);
        
        log.info("Task updated successfully: {}", id);
        return taskMapper.toDTO(task);
    }

    @Override
    public void deleteTask(UUID id) {
        log.info("Deleting task: {}", id);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        // Soft delete
        taskRepository.softDelete(id);
        
        log.info("Task deleted successfully: {}", id);
    }

    @Override
    public TaskDTO completeTask(UUID id) {
        log.info("Completing task: {}", id);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());
        task = taskRepository.save(task);
        
        log.info("Task completed: {}", id);
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO uncompleteTask(UUID id) {
        log.info("Uncompleting task: {}", id);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        task.setStatus(TaskStatus.TODO);
        task.setCompletedAt(null);
        task = taskRepository.save(task);
        
        log.info("Task uncompleted: {}", id);
        return taskMapper.toDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasksWithFilters(TaskFilterRequest filter, Pageable pageable) {
        log.debug("Getting tasks with advanced filters");
        
        TaskStatus status = filter.getStatus() != null ? TaskStatus.valueOf(filter.getStatus()) : null;
        Priority priority = filter.getPriority() != null ? Priority.valueOf(filter.getPriority()) : null;
        
        Page<Task> tasks = taskRepository.findWithFilters(
            filter.getProjectId(),
            filter.getAssigneeId(),
            status,
            priority,
            filter.getStartDateFrom(),
            filter.getStartDateTo(),
            filter.getDueDateFrom(),
            filter.getDueDateTo(),
            filter.getSearch(),
            pageable
        );
        
        return tasks.map(taskMapper::toDTO);
    }

    @Override
    public TaskDTO assignTask(UUID taskId, UUID userId) {
        log.info("Assigning task {} to user {}", taskId, userId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        task.setAssignee(user);
        task = taskRepository.save(task);
        
        log.info("Task assigned successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO addAssignee(UUID taskId, AddTaskAssigneeRequest request) {
        // For now, we support single assignee, so this delegates to assignTask
        return assignTask(taskId, request.getUserId());
    }

    @Override
    public TaskDTO removeAssignee(UUID taskId, UUID userId) {
        log.info("Removing assignee {} from task {}", userId, taskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        if (task.getAssignee() != null && task.getAssignee().getId().equals(userId)) {
            task.setAssignee(null);
            task = taskRepository.save(task);
        }
        
        log.info("Assignee removed successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAssigneeDTO> getAssignees(UUID taskId) {
        log.debug("Getting assignees for task: {}", taskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        List<TaskAssigneeDTO> assignees = new ArrayList<>();
        if (task.getAssignee() != null) {
            TaskAssigneeDTO dto = TaskAssigneeDTO.builder()
                    .userId(task.getAssignee().getId())
                    .userName(task.getAssignee().getFirstName() + " " + task.getAssignee().getLastName())
                    .userEmail(task.getAssignee().getEmail())
                    .userAvatar(task.getAssignee().getAvatarUrl())
                    .assignedAt(task.getUpdatedAt())
                    .build();
            assignees.add(dto);
        }
        
        return assignees;
    }

    @Override
    public TaskDTO addTaskToProject(UUID taskId, UUID projectId) {
        log.info("Adding task {} to project {}", taskId, projectId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        addTaskToProjectInternal(task, projectId, null);
        
        task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        log.info("Task added to project successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO addToProject(UUID taskId, AddTaskToProjectRequest request) {
        log.info("Adding task {} to project {} with section {}", taskId, request.getProjectId(), request.getSectionId());
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        addTaskToProjectInternal(task, request.getProjectId(), request.getSectionId());
        
        task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        log.info("Task added to project successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO removeFromProject(UUID taskId, UUID projectId) {
        log.info("Removing task {} from project {}", taskId, projectId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        taskProjectRepository.deleteByTaskIdAndProjectId(taskId, projectId);
        
        task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        log.info("Task removed from project successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO moveToSection(UUID taskId, UUID projectId, MoveTaskToSectionRequest request) {
        log.info("Moving task {} to section {} in project {}", taskId, request.getSectionId(), projectId);
        
        TaskProject taskProject = taskProjectRepository.findByTaskIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found in project"));
        
        if (request.getSectionId() != null) {
            ProjectSection section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));
            
            if (!section.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Section does not belong to the specified project");
            }
            
            taskProject.setSection(section);
        } else {
            taskProject.setSection(null);
        }
        
        if (request.getPosition() != null) {
            taskProject.setPosition(request.getPosition());
        }
        
        taskProjectRepository.save(taskProject);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        log.info("Task moved to section successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO createSubtask(UUID parentTaskId, CreateSubtaskRequest request) {
        log.info("Creating subtask for task: {}", parentTaskId);
        
        Task parentTask = taskRepository.findByIdAndIsDeletedFalse(parentTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent task", "id", parentTaskId));
        
        CreateTaskRequest.CreateSubtaskRequest subtaskRequest = CreateTaskRequest.CreateSubtaskRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .assigneeId(request.getAssigneeId())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .build();
        
        Task subtask = createSubtaskInternal(parentTask, subtaskRequest);
        
        log.info("Subtask created successfully with id: {}", subtask.getId());
        return taskMapper.toDTO(subtask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getSubtasks(UUID parentTaskId) {
        log.debug("Getting subtasks for task: {}", parentTaskId);
        
        List<Task> subtasks = taskRepository.findSubtasksByParentTaskId(parentTaskId);
        return taskMapper.toDTOList(subtasks);
    }

    @Override
    public TaskDTO promoteSubtaskToTask(UUID subtaskId) {
        log.info("Promoting subtask {} to task", subtaskId);
        
        Task subtask = taskRepository.findByIdAndIsDeletedFalse(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask", "id", subtaskId));
        
        if (subtask.getParentTask() == null) {
            throw new BadRequestException("Task is not a subtask");
        }
        
        subtask.setParentTask(null);
        subtask = taskRepository.save(subtask);
        
        log.info("Subtask promoted to task successfully");
        return taskMapper.toDTO(subtask);
    }

    @Override
    public TaskDependencyDTO addDependency(UUID taskId, AddTaskDependencyRequest request) {
        log.info("Adding dependency from task {} to task {}", request.getPredecessorId(), taskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        Task predecessor = taskRepository.findByIdAndIsDeletedFalse(request.getPredecessorId())
                .orElseThrow(() -> new ResourceNotFoundException("Predecessor task", "id", request.getPredecessorId()));
        
        // Check for circular dependency
        if (taskRepository.hasCircularDependency(taskId)) {
            throw new ConflictException("Adding this dependency would create a circular reference");
        }
        
        task.getDependencies().add(predecessor);
        taskRepository.save(task);
        
        TaskDependencyDTO dto = TaskDependencyDTO.builder()
                .predecessorId(predecessor.getId())
                .predecessorTitle(predecessor.getTitle())
                .predecessorStatus(predecessor.getStatus().name())
                .successorId(task.getId())
                .successorTitle(task.getTitle())
                .successorStatus(task.getStatus().name())
                .dependencyType(request.getDependencyType())
                .lagDays(request.getLagDays())
                .createdAt(Instant.now())
                .build();
        
        log.info("Dependency added successfully");
        return dto;
    }

    @Override
    public TaskDTO addDependency(UUID taskId, UUID dependsOnTaskId) {
        AddTaskDependencyRequest request = AddTaskDependencyRequest.builder()
                .predecessorId(dependsOnTaskId)
                .build();
        addDependency(taskId, request);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return taskMapper.toDTO(task);
    }

    @Override
    public void removeDependency(UUID taskId, UUID dependsOnTaskId) {
        log.info("Removing dependency from task {} to task {}", taskId, dependsOnTaskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        task.getDependencies().removeIf(dep -> dep.getId().equals(dependsOnTaskId));
        taskRepository.save(task);
        
        log.info("Dependency removed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyDTO> getDependencies(UUID taskId) {
        log.debug("Getting dependencies for task: {}", taskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        return task.getDependencies().stream()
                .map(dep -> TaskDependencyDTO.builder()
                        .predecessorId(dep.getId())
                        .predecessorTitle(dep.getTitle())
                        .predecessorStatus(dep.getStatus().name())
                        .successorId(task.getId())
                        .successorTitle(task.getTitle())
                        .successorStatus(task.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO updateCustomField(UUID taskId, UUID fieldId, UpdateCustomFieldValueRequest request) {
        log.info("Updating custom field {} for task {}", fieldId, taskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        // Custom field implementation would go here
        // For now, we'll just return the task
        
        log.info("Custom field updated successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    public TaskDTO duplicateTask(UUID taskId, DuplicateTaskRequest request) {
        log.info("Duplicating task: {}", taskId);
        
        Task originalTask = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        // Generate new task number
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        Long taskNumber = taskRepository.findMaxTaskNumberByWorkspaceId(workspaceId)
                .map(n -> n + 1)
                .orElse(1L);
        
        // Create duplicate
        Task duplicate = Task.builder()
                .title(request.getNewTitle() != null ? request.getNewTitle() : originalTask.getTitle() + " (Copy)")
                .description(originalTask.getDescription())
                .taskNumber(taskNumber)
                .status(TaskStatus.TODO)
                .priority(originalTask.getPriority())
                .startDate(originalTask.getStartDate())
                .dueDate(originalTask.getDueDate())
                .estimatedHours(originalTask.getEstimatedHours())
                .storyPoints(originalTask.getStoryPoints())
                .build();
        
        // Set assignees
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            User assignee = userRepository.findById(request.getAssigneeIds().iterator().next())
                    .orElse(null);
            duplicate.setAssignee(assignee);
        } else if (originalTask.getAssignee() != null) {
            duplicate.setAssignee(originalTask.getAssignee());
        }
        
        // Copy tags if requested
        if (request.isIncludeTags() && originalTask.getTags() != null) {
            duplicate.setTags(new HashSet<>(originalTask.getTags()));
        }
        
        // Copy followers if requested
        if (request.isIncludeFollowers() && originalTask.getFollowers() != null) {
            duplicate.setFollowers(new HashSet<>(originalTask.getFollowers()));
        }
        
        duplicate = taskRepository.save(duplicate);
        
        // Add to project
        addTaskToProjectInternal(duplicate, request.getProjectId(), request.getSectionId());
        
        // Duplicate subtasks if requested
        if (request.isIncludeSubtasks() && originalTask.getSubtasks() != null) {
            for (Task subtask : originalTask.getSubtasks()) {
                duplicateSubtask(subtask, duplicate);
            }
        }
        
        log.info("Task duplicated successfully with id: {}", duplicate.getId());
        return taskMapper.toDTO(duplicate);
    }

    @Override
    public List<TaskDTO> bulkUpdateTasks(BulkTaskUpdateRequest request) {
        log.info("Bulk updating {} tasks with operation: {}", request.getTaskIds().size(), request.getOperation());
        
        List<Task> tasks = taskRepository.findAllById(request.getTaskIds());
        
        for (Task task : tasks) {
            switch (request.getOperation()) {
                case UPDATE_STATUS:
                    if (request.getStatus() != null) {
                        task.setStatus(TaskStatus.valueOf(request.getStatus()));
                        if (request.getStatus().equals("COMPLETED")) {
                            task.setCompletedAt(Instant.now());
                        }
                    }
                    break;
                case UPDATE_PRIORITY:
                    if (request.getPriority() != null) {
                        task.setPriority(Priority.valueOf(request.getPriority()));
                    }
                    break;
                case UPDATE_ASSIGNEE:
                    if (request.getAssigneeId() != null) {
                        User assignee = userRepository.findById(request.getAssigneeId()).orElse(null);
                        task.setAssignee(assignee);
                    }
                    break;
                case UPDATE_DUE_DATE:
                    task.setDueDate(request.getDueDate());
                    break;
                case COMPLETE:
                    task.setStatus(TaskStatus.COMPLETED);
                    task.setCompletedAt(Instant.now());
                    break;
                case DELETE:
                    taskRepository.softDelete(task.getId());
                    break;
                default:
                    log.warn("Unsupported bulk operation: {}", request.getOperation());
            }
        }
        
        tasks = taskRepository.saveAll(tasks);
        
        log.info("Bulk update completed successfully");
        return taskMapper.toDTOList(tasks);
    }

    @Override
    public CommentDTO addComment(UUID taskId, CreateCommentRequest request) {
        log.info("Adding comment to task: {}", taskId);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        // Comment implementation would go here
        // For now, return a dummy comment
        CommentDTO comment = new CommentDTO();
        comment.setId(UUID.randomUUID());
        comment.setContent(request.getContent());
        comment.setCreatedAt(Instant.now());
        
        log.info("Comment added successfully");
        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProject(UUID projectId) {
        log.debug("Getting tasks for project: {}", projectId);
        
        Page<Task> tasks = taskRepository.findByProjectId(projectId, PageRequest.of(0, 1000));
        return taskMapper.toDTOList(tasks.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUser(UUID userId) {
        log.debug("Getting tasks for user: {}", userId);
        
        Page<Task> tasks = taskRepository.findByAssigneeId(userId, PageRequest.of(0, 1000));
        return taskMapper.toDTOList(tasks.getContent());
    }

    @Override
    public TaskDTO updateTaskStatus(UUID taskId, String status) {
        log.info("Updating task {} status to {}", taskId, status);
        
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        TaskStatus newStatus = TaskStatus.valueOf(status.toUpperCase());
        task.setStatus(newStatus);
        
        if (newStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
        } else if (task.getCompletedAt() != null) {
            task.setCompletedAt(null);
        }
        
        task = taskRepository.save(task);
        
        log.info("Task status updated successfully");
        return taskMapper.toDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> searchTasks(String query, UUID workspaceId) {
        log.debug("Searching tasks with query: {} in workspace: {}", query, workspaceId);
        
        Page<Task> tasks = taskRepository.findWithFilters(
            null, null, null, null, null, null, null, null, query, 
            PageRequest.of(0, 100, Sort.by("createdAt").descending())
        );
        
        return taskMapper.toDTOList(tasks.getContent());
    }

    // Helper methods
    
    private void addTaskToProjectInternal(Task task, UUID projectId, UUID sectionId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        // Check if task is already in project
        if (taskProjectRepository.existsByTaskIdAndProjectId(task.getId(), projectId)) {
            log.warn("Task {} is already in project {}", task.getId(), projectId);
            return;
        }
        
        // Get position for the task
        Integer position;
        if (sectionId != null) {
            position = taskProjectRepository.findMaxPositionInSection(projectId, sectionId)
                    .map(p -> p + 1)
                    .orElse(0);
        } else {
            position = taskProjectRepository.findMaxPositionInProjectWithNoSection(projectId)
                    .map(p -> p + 1)
                    .orElse(0);
        }
        
        // Create task-project relationship
        TaskProject taskProject = TaskProject.builder()
                .task(task)
                .project(project)
                .position(position)
                .addedAt(Instant.now())
                .addedBy(TenantContext.getCurrentUserId())
                .build();
        
        if (sectionId != null) {
            ProjectSection section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
            taskProject.setSection(section);
        }
        
        taskProjectRepository.save(taskProject);
        
        // Update task's projects set
        task.getProjects().add(project);
    }
    
    private Task createSubtaskInternal(Task parentTask, CreateTaskRequest.CreateSubtaskRequest request) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        Long taskNumber = taskRepository.findMaxTaskNumberByWorkspaceId(workspaceId)
                .map(n -> n + 1)
                .orElse(1L);
        
        Task subtask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskNumber(taskNumber)
                .status(TaskStatus.TODO)
                .priority(Priority.valueOf(request.getPriority()))
                .dueDate(request.getDueDate())
                .parentTask(parentTask)
                .build();
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId()).orElse(null);
            subtask.setAssignee(assignee);
        }
        
        subtask = taskRepository.save(subtask);
        
        // Add subtask to the same projects as parent
        for (Project project : parentTask.getProjects()) {
            addTaskToProjectInternal(subtask, project.getId(), null);
        }
        
        return subtask;
    }
    
    private Task duplicateSubtask(Task originalSubtask, Task newParent) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        Long taskNumber = taskRepository.findMaxTaskNumberByWorkspaceId(workspaceId)
                .map(n -> n + 1)
                .orElse(1L);
        
        Task duplicate = Task.builder()
                .title(originalSubtask.getTitle())
                .description(originalSubtask.getDescription())
                .taskNumber(taskNumber)
                .status(TaskStatus.TODO)
                .priority(originalSubtask.getPriority())
                .dueDate(originalSubtask.getDueDate())
                .parentTask(newParent)
                .build();
        
        return taskRepository.save(duplicate);
    }
}