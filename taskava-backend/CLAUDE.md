# CLAUDE.md - Backend Root

This directory contains the Spring Boot backend for Taskava, organized as a multi-module Gradle project.

## Module Architecture

The backend follows Domain-Driven Design (DDD) and Hexagonal Architecture principles with clear separation of concerns:

```
taskava-backend/
├── taskava-common/        # Shared DTOs, exceptions, utilities
├── taskava-data-access/   # Database layer (entities, repositories, migrations)
├── taskava-security/      # Authentication, authorization, JWT handling
├── taskava-core-service/  # Business logic and domain services
├── taskava-integration/   # External service integrations (AWS, email)
└── taskava-api-gateway/   # REST controllers, API configuration (main app)
```

## Module Dependencies

Dependencies flow in one direction to prevent circular dependencies:
```
api-gateway → core-service → data-access
     ↓             ↓             ↓
  security    integration     common
     ↓             ↓             ↓
   common       common        (base)
```

## Key Technical Decisions

1. **Gradle over Maven**: Better performance for multi-module builds (~20% faster)
2. **Module separation**: Each module has a single responsibility
3. **No circular dependencies**: Enforced through Gradle configuration
4. **Shared DTOs in common**: Prevents entity leakage to API layer

## Running the Application

```bash
# Full build
./gradlew clean build

# Run application (from backend root)
./gradlew bootRun

# Run specific module tests
./gradlew :taskava-core-service:test

# Database migrations
./gradlew :taskava-data-access:flywayMigrate
```

## Development Workflow

1. **Database changes**: Start in `taskava-data-access` with migrations
2. **Business logic**: Implement in `taskava-core-service`
3. **API endpoints**: Expose in `taskava-api-gateway`
4. **Cross-cutting concerns**: Place in `taskava-common`

## Configuration

- Application properties: `taskava-api-gateway/src/main/resources/application.yml`
- Per-module configuration: Each module can have its own `application.yml`
- Environment-specific: Use Spring profiles (local, dev, staging, prod)

## Testing Strategy

- **Unit tests**: Mock all dependencies, test in isolation
- **Integration tests**: Use `@SpringBootTest` in api-gateway module
- **Repository tests**: Use `@DataJpaTest` with TestContainers
- **Service tests**: Mock repositories, test business logic

## Important Notes

- Only `taskava-api-gateway` creates an executable JAR
- Database migrations run automatically on startup (configurable)
- All modules share version from parent `build.gradle`
- Use `@Profile` annotations for environment-specific beans