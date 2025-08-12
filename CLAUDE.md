# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Taskava is an enterprise project management platform (Asana/ClickUp alternative) with a multi-module Spring Boot backend and React TypeScript frontend. The architecture emphasizes local-first development with Docker containers mimicking AWS services.

## Build and Run Commands

### Full Stack Development
```bash
# Start all infrastructure services
docker-compose up -d postgres redis localstack mailhog

# Backend (from taskava-backend/)
./mvnw clean install              # Build all modules
./mvnw spring-boot:run -pl taskava-api-gateway  # Run application
./mvnw test                       # Run all unit tests
./mvnw verify                     # Run integration tests
./mvnw test -Dtest=TestClassName  # Run specific test class
./mvnw flyway:migrate -pl taskava-data-access  # Run database migrations

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

The backend follows a multi-module Maven architecture where each module has a specific responsibility:

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

## Debugging Tips

- Enable SQL logging: Set `logging.level.org.hibernate.SQL=DEBUG`
- Check workspace context: Ensure `TenantContext` is set
- Validate migrations: Run `./mvnw flyway:validate`
- API testing: Swagger UI at `http://localhost:8080/api/swagger-ui.html`
- Email testing: Mailhog UI at `http://localhost:8025`