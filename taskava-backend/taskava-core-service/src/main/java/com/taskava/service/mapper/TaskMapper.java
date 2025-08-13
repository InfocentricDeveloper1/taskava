package com.taskava.service.mapper;

import com.taskava.data.entity.Task;
import com.taskava.data.entity.User;
import com.taskava.data.entity.Tag;
import com.taskava.data.entity.Attachment;
import com.taskava.service.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TaskMapper {
    
    public TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskDTO dto = TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskNumber(task.getTaskNumber())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .progressPercentage(task.getProgressPercentage())
                .storyPoints(task.getStoryPoints())
                .recurring(task.isRecurring())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
        
        // Map assignee
        if (task.getAssignee() != null) {
            dto.setAssignee(toUserDTO(task.getAssignee()));
        }
        
        // Map created by
        if (task.getCreatedByUser() != null) {
            dto.setCreatedBy(toUserDTO(task.getCreatedByUser()));
            dto.setCreatedById(task.getCreatedByUser().getId());
        }
        
        // Map projects (basic info, avoid circular reference)
        if (task.getProjects() != null && !task.getProjects().isEmpty()) {
            dto.setProjects(task.getProjects().stream()
                    .map(this::toProjectDTO)
                    .collect(Collectors.toList()));
        }
        
        // Map parent task (basic info only to avoid deep recursion)
        if (task.getParentTask() != null) {
            dto.setParentTask(toBasicTaskDTO(task.getParentTask()));
        }
        
        // Map subtasks count or basic list
        if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
            dto.setSubtasks(task.getSubtasks().stream()
                    .map(this::toBasicTaskDTO)
                    .collect(Collectors.toList()));
        }
        
        // Map dependencies (basic info)
        if (task.getDependencies() != null && !task.getDependencies().isEmpty()) {
            dto.setDependencies(task.getDependencies().stream()
                    .map(this::toBasicTaskDTO)
                    .collect(Collectors.toSet()));
        }
        
        // Map followers
        if (task.getFollowers() != null && !task.getFollowers().isEmpty()) {
            dto.setFollowers(task.getFollowers().stream()
                    .map(this::toUserDTO)
                    .collect(Collectors.toSet()));
        }
        
        // Map tags
        if (task.getTags() != null && !task.getTags().isEmpty()) {
            dto.setTags(task.getTags().stream()
                    .map(this::toTagDTO)
                    .collect(Collectors.toList()));
        }
        
        // Map attachments
        if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
            dto.setAttachments(task.getAttachments().stream()
                    .map(this::toAttachmentDTO)
                    .collect(Collectors.toList()));
        }
        
        // Set comment count
        if (task.getComments() != null) {
            dto.setCommentCount(task.getComments().size());
        }
        
        // Map recurrence settings
        if (task.getRecurrenceSettings() != null) {
            dto.setRecurrenceSettings(toRecurrenceSettingsDTO(task.getRecurrenceSettings()));
        }
        
        return dto;
    }
    
    public TaskDTO toBasicTaskDTO(Task task) {
        if (task == null) {
            return null;
        }
        
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .taskNumber(task.getTaskNumber())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .build();
    }
    
    private UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
    
    private ProjectDTO toProjectDTO(com.taskava.data.entity.Project project) {
        if (project == null) {
            return null;
        }
        
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .color(project.getColor())
                .icon(project.getIcon())
                .build();
    }
    
    private TagDTO toTagDTO(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
    
    private AttachmentDTO toAttachmentDTO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        
        return AttachmentDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .fileUrl(attachment.getFileUrl())
                .uploadedAt(attachment.getCreatedAt())
                .build();
    }
    
    private TaskDTO.RecurrenceSettingsDTO toRecurrenceSettingsDTO(Task.RecurrenceSettings settings) {
        if (settings == null) {
            return null;
        }
        
        TaskDTO.RecurrenceSettingsDTO dto = new TaskDTO.RecurrenceSettingsDTO();
        dto.setType(settings.getType() != null ? settings.getType().name() : null);
        dto.setInterval(settings.getInterval());
        dto.setDayOfMonth(settings.getDayOfMonth());
        dto.setEndDate(settings.getEndDate());
        dto.setMaxOccurrences(settings.getMaxOccurrences());
        
        // Parse days of week from JSON string
        if (settings.getDaysOfWeek() != null) {
            // Assuming it's a comma-separated string or JSON array
            dto.setDaysOfWeek(List.of(settings.getDaysOfWeek().split(",")));
        }
        
        return dto;
    }
    
    public List<TaskDTO> toDTOList(List<Task> tasks) {
        if (tasks == null) {
            return List.of();
        }
        return tasks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}