# Local Development Setup

This guide will help you set up a complete local development environment for Taskava without requiring any AWS services or cloud infrastructure. Everything runs on your local machine using Docker containers.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Architecture](#architecture)
4. [Initial Setup](#initial-setup)
5. [Starting Services](#starting-services)
6. [Development Workflow](#development-workflow)
7. [Service URLs](#service-urls)
8. [Common Tasks](#common-tasks)
9. [Troubleshooting](#troubleshooting)
10. [AWS Service Mapping](#aws-service-mapping)

## Overview

The local development environment mimics the production AWS infrastructure using Docker containers:

- **PostgreSQL** replaces AWS RDS
- **Redis** replaces AWS ElastiCache
- **LocalStack** provides AWS service emulation (S3, SES, etc.)
- **Mailhog** captures emails instead of AWS SES
- **Nginx** serves as a reverse proxy

This setup allows you to develop and test all features locally without incurring AWS costs or requiring internet connectivity.

## Prerequisites

### Required Software

1. **Docker Desktop** (4.20+ recommended)
   - macOS: [Download Docker Desktop](https://docs.docker.com/desktop/install/mac-install/)
   - Windows: [Download Docker Desktop](https://docs.docker.com/desktop/install/windows-install/)
   - Linux: [Install Docker Engine](https://docs.docker.com/engine/install/)

2. **Java Development Kit (JDK) 17+**
   ```bash
   # macOS with Homebrew
   brew install openjdk@17
   
   # Ubuntu/Debian
   sudo apt-get install openjdk-17-jdk
   
   # Verify installation
   java -version
   ```

3. **Node.js 18+ and npm**
   ```bash
   # macOS with Homebrew
   brew install node
   
   # Using Node Version Manager (recommended)
   curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
   nvm install 18
   nvm use 18
   
   # Verify installation
   node -version
   npm -version
   ```

4. **Git**
   ```bash
   # macOS
   brew install git
   
   # Ubuntu/Debian
   sudo apt-get install git
   ```

5. **Maven** (optional, wrapper included)
   ```bash
   # macOS
   brew install maven
   
   # Ubuntu/Debian
   sudo apt-get install maven
   ```

### Recommended Tools

- **IDE**: IntelliJ IDEA (Community or Ultimate) for Java development
- **Code Editor**: VS Code with extensions:
  - Java Extension Pack
  - Spring Boot Extension Pack
  - ESLint
  - Prettier
  - Tailwind CSS IntelliSense
- **Database Client**: 
  - DBeaver
  - pgAdmin
  - TablePlus
- **API Testing**:
  - Postman
  - Insomnia
  - VS Code REST Client

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  React Frontend │────▶│  Nginx Proxy    │────▶│ Spring Boot API │
│  (Port 5173)    │     │  (Port 80)      │     │  (Port 8080)    │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └───────┬─────────┘
                                                         │
                              ┌──────────────────────────┴─────────────────────────┐
                              │                                                    │
                    ┌─────────▼─────────┐     ┌─────────────────┐     ┌───────────▼────────┐
                    │                   │     │                 │     │                    │
                    │    PostgreSQL     │     │     Redis       │     │    LocalStack      │
                    │    (Port 5432)    │     │   (Port 6379)   │     │ (Ports 4566-4599) │
                    │                   │     │                 │     │                    │
                    └───────────────────┘     └─────────────────┘     └────────────────────┘
                                                                                  │
                                                                       ┌──────────▼──────────┐
                                                                       │                     │
                                                                       │      Mailhog        │
                                                                       │ (SMTP: 1025)       │
                                                                       │ (UI: 8025)         │
                                                                       │                     │
                                                                       └─────────────────────┘
```

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/InfocentricDeveloper1/taskava.git
cd taskava
```

### 2. Environment Configuration

Create local environment files:

```bash
# Backend environment
cp taskava-backend/taskava-api-gateway/src/main/resources/application.yml \
   taskava-backend/taskava-api-gateway/src/main/resources/application-local.yml

# Frontend environment
cat > taskava-frontend/.env.local << EOF
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
VITE_PUBLIC_URL=http://localhost:5173
EOF
```

### 3. Configure Local Services

The `docker-compose.yml` file is already configured with sensible defaults. You can customize it if needed:

```yaml
# docker-compose.override.yml (optional)
version: '3.8'

services:
  postgres:
    environment:
      POSTGRES_PASSWORD: your-custom-password
    volumes:
      - ./data/postgres:/var/lib/postgresql/data
```

## Starting Services

### 1. Start Docker Services

```bash
# Start all services in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps
```

### 2. Initialize Database

The database will be automatically initialized with Flyway migrations on first startup. To manually run migrations:

```bash
cd taskava-backend
./mvnw flyway:migrate
```

### 3. Start Backend Application

```bash
cd taskava-backend

# Using Maven wrapper
./mvnw spring-boot:run -Dspring.profiles.active=local

# Or using your IDE
# 1. Open the project in IntelliJ IDEA
# 2. Run TaskavaApplication with profile: local
```

### 4. Start Frontend Application

```bash
cd taskava-frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev

# The application will open at http://localhost:5173
```

## Development Workflow

### Backend Development

1. **Hot Reload**: Spring Boot DevTools provides automatic restart
   ```xml
   <!-- Already included in pom.xml -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. **Database Changes**:
   ```bash
   # Create new migration
   touch taskava-backend/taskava-data-access/src/main/resources/db/migration/V2__your_migration.sql
   
   # Apply migrations
   ./mvnw flyway:migrate
   ```

3. **Running Tests**:
   ```bash
   # All tests
   ./mvnw test
   
   # Specific module
   ./mvnw test -pl taskava-core-service
   
   # Integration tests
   ./mvnw verify
   ```

### Frontend Development

1. **Hot Module Replacement**: Vite provides instant updates
   
2. **Component Development**:
   ```bash
   # Add new Shadcn component
   npx shadcn-ui@latest add dialog
   
   # Create custom component
   mkdir -p src/components/tasks
   touch src/components/tasks/TaskBoard.tsx
   ```

3. **Running Tests**:
   ```bash
   # Unit tests
   npm test
   
   # E2E tests
   npm run test:e2e
   
   # Type checking
   npm run type-check
   ```

## Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Frontend | http://localhost:5173 | React application |
| Backend API | http://localhost:8080 | Spring Boot API |
| API Documentation | http://localhost:8080/swagger-ui.html | OpenAPI/Swagger UI |
| PostgreSQL | localhost:5432 | Database (user: taskava, pass: taskava) |
| Redis Commander | http://localhost:8081 | Redis GUI |
| Mailhog | http://localhost:8025 | Email testing UI |
| LocalStack | http://localhost:4566 | AWS services |
| pgAdmin | http://localhost:5050 | Database management (optional) |

## Common Tasks

### Database Management

```bash
# Connect to PostgreSQL
docker exec -it taskava-postgres psql -U taskava -d taskava

# Backup database
docker exec taskava-postgres pg_dump -U taskava taskava > backup.sql

# Restore database
docker exec -i taskava-postgres psql -U taskava taskava < backup.sql

# Reset database
docker-compose down -v
docker-compose up -d postgres
./mvnw flyway:migrate
```

### LocalStack AWS Services

```bash
# Create S3 bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://taskava-files

# List S3 buckets
aws --endpoint-url=http://localhost:4566 s3 ls

# Upload file
aws --endpoint-url=http://localhost:4566 s3 cp file.txt s3://taskava-files/
```

### Redis Operations

```bash
# Connect to Redis CLI
docker exec -it taskava-redis redis-cli

# Common commands
> KEYS *
> GET "session:123"
> FLUSHALL
```

### Viewing Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres

# Backend application logs
tail -f taskava-backend/logs/application.log

# Frontend build logs
cd taskava-frontend && npm run build
```

## Troubleshooting

### Port Conflicts

If you encounter port conflicts:

```bash
# Check what's using a port (e.g., 5432)
lsof -i :5432  # macOS/Linux
netstat -ano | findstr :5432  # Windows

# Change port in docker-compose.yml
services:
  postgres:
    ports:
      - "5433:5432"  # Use 5433 instead
```

### Docker Issues

```bash
# Clean up containers
docker-compose down

# Remove volumes (warning: deletes data)
docker-compose down -v

# Rebuild containers
docker-compose build --no-cache
docker-compose up -d

# Check Docker resources
docker system df
docker system prune -a
```

### Backend Won't Start

1. Check Java version: `java -version` (must be 17+)
2. Clear Maven cache: `rm -rf ~/.m2/repository`
3. Rebuild: `./mvnw clean install`
4. Check application logs for specific errors

### Frontend Issues

1. Clear node modules: `rm -rf node_modules package-lock.json`
2. Reinstall: `npm install`
3. Clear Vite cache: `rm -rf node_modules/.vite`
4. Check for TypeScript errors: `npm run type-check`

### Database Connection Issues

```bash
# Test connection
docker exec taskava-postgres pg_isready

# Check logs
docker logs taskava-postgres

# Verify credentials in application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskava
    username: taskava
    password: taskava
```

## AWS Service Mapping

When you're ready to deploy to AWS, here's how local services map to AWS:

| Local Service | AWS Service | Configuration Changes |
|---------------|-------------|----------------------|
| PostgreSQL | RDS PostgreSQL | Update connection string |
| Redis | ElastiCache Redis | Update Redis endpoint |
| LocalStack S3 | S3 | Remove endpoint override |
| Mailhog | SES | Configure SES credentials |
| Local JWT | Cognito | Add Cognito configuration |
| Docker Volumes | EBS/EFS | Automatic with ECS |
| Nginx | ALB + CloudFront | Terraform manages this |

### Transitioning to AWS

1. **Keep local config**: Maintain `application-local.yml`
2. **Add AWS config**: Create `application-aws.yml`
3. **Use Spring profiles**: `-Dspring.profiles.active=aws`
4. **Environment variables**: Use AWS Systems Manager Parameter Store
5. **Gradual migration**: Test one service at a time

## Next Steps

1. **Explore the API**: Visit http://localhost:8080/swagger-ui.html
2. **Create test data**: Use the provided seed scripts
3. **Try features**: Create projects, tasks, and test multi-homing
4. **Customize**: Modify docker-compose.yml for your needs
5. **Learn more**: 
   - [Backend Development Guide](../development/backend-guide.md)
   - [Frontend Development Guide](../development/frontend-guide.md)
   - [API Reference](../development/api-reference.md)

## Useful Commands Reference

```bash
# Start everything
docker-compose up -d
cd taskava-backend && ./mvnw spring-boot:run &
cd taskava-frontend && npm run dev

# Stop everything
docker-compose down
pkill -f spring-boot

# View all logs
docker-compose logs -f

# Database console
docker exec -it taskava-postgres psql -U taskava

# Redis console
docker exec -it taskava-redis redis-cli

# Clean restart
docker-compose down -v
docker-compose up -d
```

Remember: You can develop entirely locally without any AWS account or costs. Only deploy to AWS when you're ready for staging or production environments!