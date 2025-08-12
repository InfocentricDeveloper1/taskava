# CLAUDE.md - Core Service Module

This module contains all business logic and orchestration for Taskava. It sits between the API layer and data access layer.

## Purpose

The core-service module is responsible for:
- Business logic implementation
- Service orchestration
- Transaction management
- Business validation
- Event publishing
- DTO mapping

## Architecture Principles

1. **No Direct Database Access**: Uses repositories from data-access module
2. **Transaction Boundaries**: All service methods define transaction scope
3. **DTO-Only Returns**: Never return entities, always map to DTOs
4. **Event-Driven**: Publish domain events for major operations

## Key Services

### TaskService
Central service for task management:
- Task CRUD operations
- Multi-homing logic (adding/removing from projects)
- Dependency validation
- Status transitions
- Assignment management

### ProjectService
Project lifecycle management:
- Project creation with templates
- Section management
- Custom field configuration
- Member management
- Status updates

### WorkspaceService
Workspace and team operations:
- Workspace isolation
- Team CRUD
- Permission management
- Usage tracking

### AutomationService
Rule engine implementation:
- Trigger evaluation
- Action execution
- Rule validation
- Execution history

## Service Patterns

### Transactional Boundaries
```java
@Service
@Transactional(readOnly = true)  // Default read-only
public class TaskService {
    
    @Transactional  // Override for write operations
    public TaskDTO createTask(CreateTaskRequest request) {
        // Transactional write operation
    }
}
```

### Event Publishing
```java
@Service
public class TaskService {
    private final ApplicationEventPublisher eventPublisher;
    
    public TaskDTO createTask(CreateTaskRequest request) {
        Task task = // ... create task
        eventPublisher.publishEvent(new TaskCreatedEvent(task.getId()));
        return taskMapper.toDto(task);
    }
}
```

### DTO Mapping
Uses MapStruct for efficient mapping:
```java
@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDTO toDto(Task entity);
    Task toEntity(CreateTaskRequest dto);
    
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Task entity, UpdateTaskRequest dto);
}
```

### Validation Pattern
```java
@Service
public class TaskService {
    
    public TaskDTO createTask(CreateTaskRequest request) {
        // 1. Validate request
        validateTaskCreation(request);
        
        // 2. Check business rules
        checkProjectExists(request.getProjectId());
        checkUserPermissions(request.getAssigneeId());
        
        // 3. Execute business logic
        // ...
    }
}
```

## Multi-Homing Implementation

Critical business logic for task multi-homing:
1. Task creation doesn't require project (orphan tasks allowed)
2. Adding to project creates `TaskProject` relationship
3. Each project relationship has independent:
   - Section placement
   - Position ordering
   - Custom field values
4. Removing from last project doesn't delete task

## Caching Strategy

Uses Spring Cache abstraction:
```java
@Cacheable(value = "workspaces", key = "#id")
public WorkspaceDTO getWorkspace(UUID id) { }

@CacheEvict(value = "workspaces", key = "#id")
public void updateWorkspace(UUID id, UpdateRequest request) { }
```

## Testing Approach

- Mock all repository dependencies
- Test business logic in isolation
- Verify event publishing
- Test transaction rollback scenarios
- Use `@MockBean` for integration tests

## Important Patterns

1. **Never trust input**: Always validate
2. **Check permissions**: Every operation needs authorization
3. **Audit everything**: Important operations should be logged
4. **Fail fast**: Validate early in the method
5. **Use events**: Decouple side effects via events
6. **Keep it simple**: One service method = one business operation