# Backend Development Guide

This comprehensive guide covers Spring Boot development for the Taskava platform, including architecture, best practices, and common tasks.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Module Structure](#module-structure)
3. [Development Setup](#development-setup)
4. [Core Concepts](#core-concepts)
5. [Database Operations](#database-operations)
6. [API Development](#api-development)
7. [Security Implementation](#security-implementation)
8. [Testing Strategy](#testing-strategy)
9. [Performance Optimization](#performance-optimization)
10. [Common Tasks](#common-tasks)

## Architecture Overview

The backend follows a **multi-module Maven** architecture with clear separation of concerns:

```
taskava-backend/
├── pom.xml                    # Parent POM
├── taskava-api-gateway/       # REST controllers, API documentation
├── taskava-core-service/      # Business logic, service layer
├── taskava-data-access/       # JPA entities, repositories
├── taskava-security/          # Authentication, authorization
├── taskava-integration/       # External service integrations
└── taskava-common/           # Shared utilities, DTOs
```

### Design Principles

1. **Domain-Driven Design (DDD)**: Organize code around business domains
2. **Hexagonal Architecture**: Separate core logic from external dependencies
3. **SOLID Principles**: Maintain clean, maintainable code
4. **12-Factor App**: Follow cloud-native best practices

## Module Structure

### taskava-api-gateway

Handles all HTTP requests and API documentation.

```java
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {
    
    private final TaskService taskService;
    
    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
}
```

### taskava-core-service

Contains business logic and orchestrates operations.

```java
@Service
@Transactional
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final EventPublisher eventPublisher;
    
    public TaskDTO createTask(CreateTaskRequest request) {
        // Validate business rules
        validateTaskCreation(request);
        
        // Create entity
        Task task = taskMapper.toEntity(request);
        
        // Save to database
        task = taskRepository.save(task);
        
        // Publish event
        eventPublisher.publish(new TaskCreatedEvent(task.getId()));
        
        return taskMapper.toDto(task);
    }
}
```

### taskava-data-access

Manages database entities and repositories.

```java
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted_at IS NULL")
public class Task extends BaseEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Set<TaskProject> projects = new HashSet<>();
}
```

## Development Setup

### Running the Application

```bash
# Standard run
./mvnw spring-boot:run

# With specific profile
./mvnw spring-boot:run -Dspring.profiles.active=local

# With debug enabled
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Hot Reload Setup

Spring Boot DevTools is configured for automatic restart:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

IntelliJ IDEA Settings:
1. Enable "Build project automatically"
2. Enable "Allow auto-make to start"

### Environment Configuration

```yaml
# application-local.yml
spring:
  profiles:
    active: local
  
  datasource:
    url: jdbc:postgresql://localhost:5432/taskava
    username: taskava
    password: taskava
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    
  redis:
    host: localhost
    port: 6379
    
logging:
  level:
    com.taskava: DEBUG
    org.springframework.security: DEBUG
```

## Core Concepts

### Multi-Tenancy

We use a shared database with row-level security:

```java
@Component
public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenant(UUID workspaceId) {
        currentTenant.set(workspaceId);
    }
    
    public static UUID getCurrentTenant() {
        return currentTenant.get();
    }
}

@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain chain) {
        try {
            UUID workspaceId = extractWorkspaceId(request);
            TenantContext.setCurrentTenant(workspaceId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
```

### Audit Logging

All entities extend BaseEntity for automatic auditing:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    @LastModifiedBy
    private UUID updatedBy;
    
    @Version
    private Long version;
    
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
```

### Event-Driven Architecture

```java
@Component
@Slf4j
public class TaskEventListener {
    
    @EventListener
    @Async
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.info("Task created: {}", event.getTaskId());
        // Send notifications
        // Update search index
        // Trigger automations
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCompleted(TaskCompletedEvent event) {
        // Handle after transaction commits
    }
}
```

## Database Operations

### Flyway Migrations

Create migrations in `src/main/resources/db/migration/`:

```sql
-- V2__add_custom_fields.sql
CREATE TABLE custom_fields (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    name VARCHAR(255) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(workspace_id, name)
);

CREATE INDEX idx_custom_fields_workspace ON custom_fields(workspace_id);
```

### JPA Best Practices

```java
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    // Use @Query for complex queries
    @Query("SELECT t FROM Task t JOIN FETCH t.assignees WHERE t.workspace.id = :workspaceId")
    List<Task> findAllWithAssignees(@Param("workspaceId") UUID workspaceId);
    
    // Use Specification for dynamic queries
    default Page<Task> findByFilters(TaskFilter filter, Pageable pageable) {
        return findAll(TaskSpecification.withFilters(filter), pageable);
    }
    
    // Batch operations
    @Modifying
    @Query("UPDATE Task t SET t.status = :status WHERE t.id IN :ids")
    void updateStatusBatch(@Param("ids") List<UUID> ids, @Param("status") String status);
}
```

### Transaction Management

```java
@Service
@Slf4j
public class TaskService {
    
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasks(Pageable pageable) {
        // Read-only transaction for better performance
    }
    
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        rollbackFor = Exception.class
    )
    public TaskDTO createTask(CreateTaskRequest request) {
        // Transactional write operation
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditLog(String action) {
        // New transaction for audit logging
    }
}
```

## API Development

### RESTful Endpoints

Follow REST conventions:

```java
@RestController
@RequestMapping("/api/v1")
public class ProjectController {
    
    @GetMapping("/projects")
    public Page<ProjectDTO> listProjects(
            @PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable,
            @Valid ProjectFilter filter) {
        return projectService.findAll(filter, pageable);
    }
    
    @GetMapping("/projects/{id}")
    public ProjectDTO getProject(@PathVariable UUID id) {
        return projectService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }
    
    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDTO createProject(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.create(request);
    }
    
    @PutMapping("/projects/{id}")
    public ProjectDTO updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, request);
    }
    
    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id) {
        projectService.delete(id);
    }
}
```

### Request Validation

```java
@Data
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be less than 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @FutureOrPresent(message = "Due date must be in the future")
    private Instant dueDate;
    
    @Valid
    private List<CustomFieldValue> customFields;
}

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
            
        return ResponseEntity.badRequest()
            .body(ErrorResponse.validation(errors));
    }
}
```

### API Documentation

```java
@Configuration
@SecurityScheme(
    name = "bearer-jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Taskava API")
                .version("1.0")
                .description("Task management platform API")
                .contact(new Contact()
                    .name("Taskava Team")
                    .email("api@taskava.com")))
            .addServersItem(new Server()
                .url("http://localhost:8080")
                .description("Local Development"));
    }
}
```

## Security Implementation

### JWT Authentication

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpiration;
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userPrincipal.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .claim("workspace", userPrincipal.getWorkspaceId())
            .claim("roles", userPrincipal.getRoles())
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token", e);
            return false;
        }
    }
}
```

### Method Security

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    // Configuration
}

@Service
public class ProjectService {
    
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasAccess(#id, 'READ')")
    public ProjectDTO getProject(UUID id) {
        // Method secured by annotation
    }
    
    @PostAuthorize("returnObject.workspace.id == authentication.workspaceId")
    public TaskDTO getTask(UUID id) {
        // Post-authorization check
    }
}
```

## Testing Strategy

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskService taskService;
    
    @Test
    void createTask_ValidRequest_ReturnsCreatedTask() {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .projectId(UUID.randomUUID())
            .build();
            
        Task task = new Task();
        task.setId(UUID.randomUUID());
        
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(new TaskDTO());
        
        // When
        TaskDTO result = taskService.createTask(request);
        
        // Then
        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "USER")
    void createTask_ValidRequest_Returns201() throws Exception {
        String requestBody = """
            {
                "title": "Integration Test Task",
                "projectId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;
            
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Integration Test Task"));
    }
}
```

### Repository Tests

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Test
    void findByWorkspace_ReturnsOnlyWorkspaceTasks() {
        // Given
        Workspace workspace1 = createWorkspace("Workspace 1");
        Workspace workspace2 = createWorkspace("Workspace 2");
        
        Task task1 = createTask("Task 1", workspace1);
        Task task2 = createTask("Task 2", workspace2);
        
        entityManager.persistAndFlush(workspace1);
        entityManager.persistAndFlush(workspace2);
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);
        
        // When
        List<Task> tasks = taskRepository.findByWorkspaceId(workspace1.getId());
        
        // Then
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 1");
    }
}
```

## Performance Optimization

### Database Query Optimization

```java
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    // N+1 problem solution
    @EntityGraph(attributePaths = {"assignees", "projects", "customFieldValues"})
    Optional<Task> findWithDetailsById(UUID id);
    
    // Projection for list views
    @Query("SELECT new com.taskava.dto.TaskListItem(t.id, t.title, t.status, t.dueDate) " +
           "FROM Task t WHERE t.workspace.id = :workspaceId")
    Page<TaskListItem> findTaskListItems(@Param("workspaceId") UUID workspaceId, Pageable pageable);
}
```

### Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
            
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class WorkspaceService {
    
    @Cacheable(value = "workspaces", key = "#id")
    public WorkspaceDTO getWorkspace(UUID id) {
        // Cached method
    }
    
    @CacheEvict(value = "workspaces", key = "#id")
    public void updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        // Cache eviction on update
    }
}
```

### Async Processing

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Taskava-Async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {
    
    @Async
    public CompletableFuture<Void> sendTaskNotification(TaskCreatedEvent event) {
        // Async notification sending
        return CompletableFuture.completedFuture(null);
    }
}
```

## Common Tasks

### Adding a New Entity

1. Create entity class:
```java
@Entity
@Table(name = "workflows")
public class Workflow extends BaseEntity {
    // Entity definition
}
```

2. Create repository:
```java
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    // Custom queries
}
```

3. Create service:
```java
@Service
@Transactional
public class WorkflowService {
    // Business logic
}
```

4. Create controller:
```java
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    // REST endpoints
}
```

5. Add migration:
```sql
-- V3__create_workflows.sql
CREATE TABLE workflows (
    -- Table definition
);
```

### Adding Custom Validation

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email != null && !userRepository.existsByEmail(email);
    }
}
```

### Implementing Batch Operations

```java
@Service
public class BatchService {
    
    @Transactional
    public BatchResult processBatch(List<BatchOperation> operations) {
        List<CompletableFuture<OperationResult>> futures = operations.stream()
            .map(op -> CompletableFuture.supplyAsync(() -> processOperation(op)))
            .collect(Collectors.toList());
            
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return BatchResult.from(futures);
    }
}
```

## Debugging Tips

### Enable SQL Logging

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Request/Response Logging

```java
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) {
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} {} {} {}ms", 
                request.getMethod(), 
                request.getRequestURI(), 
                response.getStatus(), 
                duration);
        }
    }
}
```

### Memory Profiling

```bash
# Enable JMX
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9010
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false

# Connect with VisualVM or JConsole
```

## Best Practices Summary

1. **Use DTOs** for API contracts, not entities
2. **Validate early** at controller level
3. **Handle exceptions globally** with @ControllerAdvice
4. **Use transactions appropriately** - read-only when possible
5. **Optimize queries** with projections and fetch joins
6. **Cache strategically** for read-heavy operations
7. **Log meaningfully** with correlation IDs
8. **Test thoroughly** - unit, integration, and E2E
9. **Monitor performance** with metrics and APM
10. **Document APIs** with OpenAPI/Swagger

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Effective Java](https://www.oreilly.com/library/view/effective-java-3rd/9780134686097/)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

## Next Steps

- Explore the [API Reference](./api-reference.md)
- Learn about [Frontend Development](./frontend-guide.md)
- Review [Security Best Practices](../architecture/security.md)
- Set up [Monitoring](../operations/monitoring.md)