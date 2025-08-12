# CLAUDE.md - API Gateway Module

This module serves as the web layer for Taskava, handling all HTTP requests and WebSocket connections. It's the only module that produces an executable Spring Boot application.

## Purpose

The api-gateway module is responsible for:
- REST API endpoints
- Request/response handling
- Input validation
- Authentication/authorization filters
- API documentation (OpenAPI/Swagger)
- WebSocket endpoints
- Error handling
- CORS configuration

## API Structure

### RESTful Conventions
All endpoints follow REST principles:
```
GET    /api/v1/projects          # List projects
POST   /api/v1/projects          # Create project
GET    /api/v1/projects/{id}     # Get specific project
PUT    /api/v1/projects/{id}     # Update project
DELETE /api/v1/projects/{id}     # Delete project

# Nested resources
GET    /api/v1/projects/{id}/tasks
POST   /api/v1/projects/{id}/tasks
```

### Controller Organization
Each domain has its own controller:
- `TaskController` - Task operations
- `ProjectController` - Project management
- `WorkspaceController` - Workspace operations
- `UserController` - User management
- `AuthController` - Authentication endpoints

## Request/Response Flow

1. **Request arrives** → Security filters
2. **Authentication** → JWT validation
3. **Controller method** → Input validation
4. **Service call** → Business logic
5. **Response mapping** → DTO transformation
6. **Global exception handler** → Error formatting

## Key Components

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT filter configuration
    // CORS settings
    // Public endpoints
}
```

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles all exceptions
    // Returns consistent error format
    // Logs appropriately
}
```

### Request Validation
Uses JSR-303 annotations:
```java
@PostMapping("/tasks")
public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
    // @Valid triggers validation
    // Errors handled by GlobalExceptionHandler
}
```

### API Documentation
OpenAPI 3.0 with Swagger UI:
- Auto-generated from annotations
- Available at `/api/swagger-ui.html`
- Includes authentication details
- Request/response examples

## WebSocket Support

Real-time updates via STOMP over WebSocket:
```java
@Controller
public class TaskWebSocketController {
    @MessageMapping("/task.update")
    @SendTo("/topic/tasks")
    public TaskUpdateMessage handleTaskUpdate(TaskUpdateRequest request) {
        // Broadcast to subscribers
    }
}
```

## Authentication Flow

1. **Login** → `/api/v1/auth/login` → Returns JWT
2. **Requests** → Include `Authorization: Bearer {token}`
3. **JWT Contains** → User ID, workspace ID, roles
4. **Validation** → Every request validates token

## Common Patterns

### Pagination
All list endpoints support pagination:
```
GET /api/v1/tasks?page=0&size=20&sort=createdAt,desc
```

### Filtering
Query parameters for filtering:
```
GET /api/v1/tasks?status=IN_PROGRESS&assignee=user-id&project=project-id
```

### Response Headers
Custom headers for metadata:
```
X-Total-Count: 150
X-Page-Number: 0
X-Page-Size: 20
```

## Error Response Format
```json
{
  "timestamp": "2023-12-08T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tasks",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

## Performance Considerations

1. **Rate Limiting**: Bucket4j implementation
2. **Compression**: Enabled for responses > 1KB
3. **Caching Headers**: ETags for static resources
4. **Async Operations**: Use `@Async` for long operations
5. **Connection Pooling**: Configured in application.yml

## Testing

- Use `@WebMvcTest` for controller tests
- Mock service layer dependencies
- Test security configurations
- Verify error handling
- Test with MockMvc

## Important Notes

- Never expose entities in responses
- Always validate input at controller level
- Use appropriate HTTP status codes
- Include correlation IDs for tracing
- Document all endpoints with OpenAPI