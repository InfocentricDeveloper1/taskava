# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Taskava is an enterprise project management platform (Asana/ClickUp alternative) with a multi-module Spring Boot backend and React TypeScript frontend. The architecture emphasizes local-first development with Docker containers mimicking AWS services.

## Context Files (CLAUDE.md)

This project uses CLAUDE.md files in major directories to maintain context. Always check for and update these files when working in these areas:

### Backend Context Files
- `/taskava-backend/CLAUDE.md` - Backend architecture overview
- `/taskava-backend/taskava-data-access/CLAUDE.md` - Database and JPA patterns
- `/taskava-backend/taskava-core-service/CLAUDE.md` - Business logic patterns
- `/taskava-backend/taskava-api-gateway/CLAUDE.md` - REST API and controllers

### Frontend Context Files
- `/taskava-frontend/CLAUDE.md` - Frontend architecture and stack
- `/taskava-frontend/src/CLAUDE.md` - Source code organization
- `/taskava-frontend/src/components/CLAUDE.md` - Component patterns and guidelines

### Infrastructure Context Files
- `/infrastructure/CLAUDE.md` - Deployment and AWS infrastructure

**Important**: When making significant changes to any module, update the corresponding CLAUDE.md file to maintain accurate context for future development.

## Build and Run Commands

### Full Stack Development
```bash
# Start all infrastructure services
docker-compose up -d postgres redis localstack mailhog

# Backend (from taskava-backend/)
./gradlew clean build             # Build all modules
./gradlew bootRun                 # Run application
./gradlew test                    # Run all unit tests
./gradlew integrationTest         # Run integration tests (if configured)
./gradlew test --tests TestClassName  # Run specific test class
./gradlew :taskava-data-access:flywayMigrate  # Run database migrations
./gradlew createMigration -Pname=add_new_table  # Create new migration

# Frontend (from taskava-frontend/)
npm install                       # Install dependencies
npm run dev                       # Start dev server (port 5173)
npm run build                     # Production build
npm run lint                      # Run ESLint
npm test                          # Run tests
```

### Database Operations
```bash
# Connect to local PostgreSQL
docker exec -it taskava-postgres psql -U taskava -d taskava

# Create new migration
touch taskava-backend/taskava-data-access/src/main/resources/db/migration/V{number}__{description}.sql
```

## Architecture Overview

### Backend Module Structure

The backend follows a multi-module Gradle architecture where each module has a specific responsibility:

- **taskava-api-gateway**: REST controllers, API documentation, request/response handling. This is the only module that starts Spring Boot.
- **taskava-core-service**: Business logic, service layer, orchestration. No direct database access.
- **taskava-data-access**: JPA entities, repositories, database migrations. Contains all database-related code.
- **taskava-security**: JWT authentication, authorization, security configurations.
- **taskava-common**: Shared DTOs, exceptions, utilities used across modules.
- **taskava-integration**: External service integrations (AWS S3, email, etc.).

Dependencies flow downward: api-gateway → core-service → data-access. Never create circular dependencies.

### Key Architectural Patterns

**Multi-Tenancy**: Implemented via workspace isolation. Every request must have a workspace context set via `TenantContext` ThreadLocal.

**Task Multi-Homing**: Tasks can belong to multiple projects through the `task_projects` join table. This is a core differentiator requiring careful handling in queries.

**Audit Trail**: All entities extend `BaseEntity` with automatic audit fields. Soft deletes via `deleted_at` timestamp.

**Event-Driven**: Domain events are published for major operations (task created, project updated) using Spring's ApplicationEventPublisher.

### Frontend Architecture

React application using:
- **Shadcn UI** components in `src/components/ui/`
- **Zustand** for global state management
- **React Query** for server state and caching
- **React Hook Form + Zod** for form validation
- Path aliases configured: `@/` maps to `src/`

## Critical Implementation Details

### Database Schema

The schema supports full Asana feature parity:
- Organizations contain Workspaces
- Workspaces contain Teams and Projects
- Projects have Sections and can share Tasks (multi-homing)
- Tasks have CustomFields that vary by Project
- Everything is soft-deleted and audited

### API Patterns

All endpoints follow REST conventions:
- Pagination: `?page=0&size=20&sort=createdAt,desc`
- Filtering: Query parameters mapped to `@ModelAttribute` filter objects
- Responses: Consistent DTOs, never expose entities directly
- Errors: Global exception handler returns standardized error responses

### Security Model

- JWT tokens with workspace claims
- Method-level security via `@PreAuthorize`
- Row-level security through workspace isolation
- API rate limiting per endpoint

### Local vs AWS Services

| Local (Docker) | AWS Service | Switch via |
|----------------|-------------|------------|
| PostgreSQL | RDS | DATABASE_URL env var |
| Redis | ElastiCache | REDIS_HOST env var |
| LocalStack S3 | S3 | S3_ENDPOINT env var |
| Mailhog | SES | MAIL_HOST env var |

## Common Development Tasks

### Adding a New Feature

1. Start with database migration in `taskava-data-access`
2. Create/update JPA entities
3. Add repository methods if needed
4. Implement service logic in `taskava-core-service`
5. Create DTOs in `taskava-common`
6. Add REST controller in `taskava-api-gateway`
7. Update frontend API client and components

### Testing Patterns

- Unit tests: Mock all dependencies with Mockito
- Integration tests: Use `@SpringBootTest` with TestContainers
- Repository tests: `@DataJpaTest` with H2 or TestContainers PostgreSQL
- API tests: `@WebMvcTest` or full integration with MockMvc

## Performance Considerations

- Use `@EntityGraph` to prevent N+1 queries
- Implement pagination for all list endpoints
- Cache frequently accessed data with Spring Cache + Redis
- Use database indexes on foreign keys and frequently queried columns
- Lazy load associations by default

## Gradle-Specific Tips

### Performance Optimization
- Enable build cache: `./gradlew build --build-cache`
- Run specific module: `./gradlew :taskava-api-gateway:build`
- Parallel execution: Already enabled in gradle.properties
- Continuous build: `./gradlew build --continuous`

### Useful Gradle Commands
```bash
./gradlew tasks                   # List all available tasks
./gradlew dependencies            # Show dependency tree
./gradlew dependencyInsight --dependency spring-boot  # Analyze specific dependency
./gradlew clean build -x test     # Build without tests
./gradlew bootJar                 # Create executable JAR
./gradlew jibDockerBuild         # Build Docker image locally
```

## Debugging Tips

- Enable SQL logging: Set `logging.level.org.hibernate.SQL=DEBUG`
- Check workspace context: Ensure `TenantContext` is set
- Validate migrations: Run `./gradlew :taskava-data-access:flywayValidate`
- API testing: Swagger UI at `http://localhost:8080/api/swagger-ui.html`
- Email testing: Mailhog UI at `http://localhost:8025`

## Frontend UI Development with ShadCN

**Critical**: We use the ShadCN UI MCP server for all frontend component development. This is mandatory to ensure consistency and proper implementation.

### MCP Server Tools
The following MCP tools are available and MUST be used:
- `mcp__shadcn-ui__list_components` - List all available components
- `mcp__shadcn-ui__get_component` - Get component source code
- `mcp__shadcn-ui__get_component_demo` - Get usage examples
- `mcp__shadcn-ui__get_component_metadata` - Get dependencies
- `mcp__shadcn-ui__get_block` - Get pre-built blocks
- `mcp__shadcn-ui__list_blocks` - List available blocks

### UI Development Workflow
1. **Always start by checking available components**: Use `list_components`
2. **Get component demos before implementing**: Use `get_component_demo`
3. **Check dependencies**: Use `get_component_metadata`
4. **Follow demo patterns exactly**: Don't guess implementation
5. **Never modify `/components/ui/` directly**: These are ShadCN base components

### Rules Enforcement
- `.cursorrules` file in frontend contains mandatory ShadCN UI rules
- All UI PRs must demonstrate MCP server usage
- Component implementations must match ShadCN patterns

See `/taskava-frontend/docs/design/shadcn-mcp-setup-guide.md` for complete setup instructions.