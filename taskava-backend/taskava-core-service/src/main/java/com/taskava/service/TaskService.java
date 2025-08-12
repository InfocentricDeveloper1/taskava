package com.taskava.service;

import com.taskava.service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    
    Page<TaskDTO> getTasks(UUID projectId, UUID assigneeId, String status, Pageable pageable);
    
    TaskDTO getTaskById(UUID id);
    
    TaskDTO createTask(CreateTaskRequest request);
    
    TaskDTO updateTask(UUID id, UpdateTaskRequest request);
    
    void deleteTask(UUID id);
    
    TaskDTO assignTask(UUID taskId, UUID userId);
    
    TaskDTO updateTaskStatus(UUID taskId, String status);
    
    TaskDTO addTaskToProject(UUID taskId, UUID projectId);
    
    List<TaskDTO> getSubtasks(UUID parentTaskId);
    
    CommentDTO addComment(UUID taskId, CreateCommentRequest request);
    
    List<TaskDTO> getTasksByProject(UUID projectId);
    
    List<TaskDTO> getTasksByUser(UUID userId);
    
    TaskDTO createSubtask(UUID parentTaskId, CreateTaskRequest request);
    
    TaskDTO addDependency(UUID taskId, UUID dependsOnTaskId);
    
    void removeDependency(UUID taskId, UUID dependsOnTaskId);
    
    List<TaskDTO> searchTasks(String query, UUID workspaceId);
}