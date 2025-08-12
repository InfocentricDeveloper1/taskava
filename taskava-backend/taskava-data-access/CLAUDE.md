# CLAUDE.md - Data Access Module

This module handles all database interactions for Taskava using Spring Data JPA with PostgreSQL.

## Purpose

The data-access module is responsible for:
- JPA entity definitions
- Repository interfaces
- Database migrations (Flyway)
- Query specifications
- Database-level validation

## Key Components

### Entities

All entities extend `BaseEntity` for common fields:
```java
- id (UUID)
- createdAt
- createdBy
- updatedAt
- updatedBy
- version (optimistic locking)
- deletedAt (soft delete)
```

Major entities:
- **Organization**: Top-level tenant
- **Workspace**: Container for projects within org
- **Project**: Contains tasks and configurations
- **Task**: Core work item with multi-homing support
- **User**: System users with workspace memberships
- **CustomField**: Dynamic field definitions

### Multi-Tenancy Strategy

Implemented via workspace-level isolation:
- All queries filtered by workspace context
- Row-level security planned for future
- Soft deletes via `@Where(clause = "deleted_at IS NULL")`

### Task Multi-Homing

Critical feature where tasks can belong to multiple projects:
- `Task` entity has no direct project reference
- `TaskProject` join table manages relationships
- Each task-project relationship can have different:
  - Section placement
  - Custom field values
  - Position/ordering

### Database Migrations

Located in `src/main/resources/db/migration/`:
- Naming: `V{timestamp}__{description}.sql`
- Run automatically on startup
- Create new migration: `./gradlew createMigration -Pname=description`

### Repository Pattern

Each entity has a corresponding repository:
```java
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Custom query methods
    // Specifications for complex queries
}
```

### Performance Considerations

1. **Lazy Loading**: Default for all associations
2. **Entity Graphs**: Use `@EntityGraph` to prevent N+1
3. **Projections**: Use DTOs for read-only queries
4. **Indexes**: Add on foreign keys and search columns
5. **Batch Processing**: Configure Hibernate batch size

## Common Patterns

### Soft Delete
```java
@Entity
@Where(clause = "deleted_at IS NULL")
public class Task extends BaseEntity {
    // Soft deleted records automatically filtered
}
```

### Audit Trail
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
@Audited  // If using Envers
public class Task extends BaseEntity {
    // Automatically tracks who and when
}
```

### Multi-Workspace Query
```java
@Query("SELECT t FROM Task t JOIN t.projects p WHERE p.workspace.id = :workspaceId")
List<Task> findByWorkspace(@Param("workspaceId") UUID workspaceId);
```

## Testing

Use `@DataJpaTest` for repository tests:
- Runs with in-memory H2 by default
- Use `@AutoConfigureTestDatabase(replace = Replace.NONE)` for PostgreSQL
- TestContainers for integration tests

## Important Notes

- Never expose entities directly to API layer
- Always use DTOs for data transfer
- Validate at service layer, not just database
- Consider query performance early
- Use database views for complex reports