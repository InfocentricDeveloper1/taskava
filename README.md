# Taskava - Enterprise Project Management Platform

Taskava is a privacy-focused, enterprise-ready project management platform designed as a powerful alternative to Asana and ClickUp. Built with modern technologies and best practices, it offers comprehensive project management capabilities with advanced features like multi-homing tasks, custom fields, automation, and real-time collaboration.

## Architecture Overview

### Technology Stack

#### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.0 with Java 17
- **Architecture**: Multi-module Gradle project with clean architecture
- **Database**: PostgreSQL with Flyway migrations
- **Security**: JWT-based authentication with Spring Security
- **API Documentation**: OpenAPI 3.0 with Swagger UI
- **Cloud Services**: AWS (S3, Cognito, SES)
- **Caching**: Redis/Caffeine
- **Messaging**: WebSocket for real-time updates

#### Frontend (React)
- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite
- **UI Components**: Shadcn UI with Radix UI primitives
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **Data Fetching**: TanStack Query (React Query)
- **Forms**: React Hook Form with Zod validation
- **Routing**: React Router v6

## Project Structure

```
taskava/
├── taskava-backend/           # Spring Boot backend
│   ├── taskava-parent/        # Parent POM
│   ├── taskava-common/        # Shared utilities, DTOs, exceptions
│   ├── taskava-data-access/   # JPA entities, repositories
│   ├── taskava-security/      # Authentication, authorization
│   ├── taskava-core-service/  # Business logic, services
│   ├── taskava-integration/   # External service integrations
│   └── taskava-api-gateway/   # REST controllers, API configuration
├── taskava-frontend/          # React frontend
│   ├── src/
│   │   ├── components/        # UI and feature components
│   │   ├── pages/            # Route pages
│   │   ├── services/         # API services
│   │   ├── store/            # State management
│   │   ├── hooks/            # Custom React hooks
│   │   ├── types/            # TypeScript type definitions
│   │   └── utils/            # Utility functions
│   └── public/               # Static assets
├── docker-compose.yml        # Local development environment
├── nginx/                    # Nginx configuration
└── terraform/               # Infrastructure as Code (optional)
```

## Key Features

### Core Functionality
- **Multi-tenant Architecture**: Complete workspace isolation with organization-level management
- **Task Management**: Comprehensive task tracking with subtasks, dependencies, and multi-homing
- **Project Views**: Multiple views including List, Board (Kanban), Calendar, Timeline, and Gantt
- **Custom Fields**: Flexible field system supporting various data types
- **Automation Engine**: Rule-based automation for repetitive tasks
- **Forms**: Dynamic forms with branching logic for data collection
- **Real-time Collaboration**: WebSocket-based real-time updates
- **File Management**: S3-integrated file attachments with versioning

### Enterprise Features
- **RBAC**: Role-based access control at multiple levels
- **Audit Logging**: Complete audit trail with Hibernate Envers
- **SSO Integration**: Support for SAML/OAuth providers
- **API Access**: RESTful API with comprehensive documentation
- **Data Export**: Bulk export capabilities
- **Advanced Search**: Full-text search with filters
- **Portfolios**: High-level project grouping and tracking

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 15+ (or use Docker)
- Gradle 8.5+ (or use the included wrapper)

### Backend Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/taskava.git
cd taskava
```

2. **Start infrastructure services**
```bash
docker-compose up -d postgres redis localstack mailhog
```

3. **Build the backend**
```bash
cd taskava-backend
./gradlew clean build
```

4. **Run database migrations**
```bash
./gradlew :taskava-data-access:flywayMigrate
```

5. **Start the application**
```bash
./gradlew bootRun
```

The backend will be available at `http://localhost:8080`
- API Documentation: `http://localhost:8080/api/swagger-ui.html`
- Health Check: `http://localhost:8080/api/actuator/health`

### Frontend Setup

1. **Install dependencies**
```bash
cd taskava-frontend
npm install
```

2. **Start development server**
```bash
npm run dev
```

The frontend will be available at `http://localhost:3000`

### Full Stack Development

For full-stack development with hot-reload:

```bash
# Start all services
docker-compose up

# Or start only infrastructure
docker-compose up postgres redis localstack mailhog

# Then run backend and frontend separately for hot-reload
cd taskava-backend && mvn spring-boot:run
cd taskava-frontend && npm run dev
```

## Environment Configuration

### Backend Configuration

Key environment variables (`.env` or system environment):

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/taskava
DATABASE_USERNAME=taskava
DATABASE_PASSWORD=taskava

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000

# AWS
AWS_REGION=us-east-1
S3_BUCKET_NAME=taskava-files
AWS_ACCESS_KEY_ID=your-key
AWS_SECRET_ACCESS_KEY=your-secret

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email
SMTP_PASSWORD=your-password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Frontend Configuration

Create `.env.local`:

```env
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/api/ws
```

## Database Schema

The application uses a comprehensive schema supporting:
- Multi-tenant organizations and workspaces
- Projects with sections and milestones
- Tasks with multi-homing (belonging to multiple projects)
- Custom fields with various data types
- User management with roles and permissions
- Comments and attachments
- Audit tables for compliance

## API Documentation

The API follows RESTful principles with:
- Consistent naming conventions
- Pagination support
- Filtering and sorting
- Comprehensive error handling
- Rate limiting
- CORS configuration

Access the interactive API documentation at:
`http://localhost:8080/api/swagger-ui.html`

## Testing

### Backend Testing
```bash
cd taskava-backend
./gradlew test                    # Unit tests
./gradlew integrationTest         # Integration tests
./gradlew test --tests TestClass  # Specific test
```

### Frontend Testing
```bash
cd taskava-frontend
npm test                    # Unit tests
npm run test:e2e           # E2E tests
npm run test:coverage      # Coverage report
```

## Deployment

### Docker Deployment
```bash
# Build images
docker-compose build

# Deploy
docker-compose up -d
```

### Kubernetes Deployment
```bash
# Apply configurations
kubectl apply -f k8s/

# Check status
kubectl get pods -n taskava
```

### AWS Deployment
Use the provided Terraform scripts in the `terraform/` directory for AWS infrastructure setup.

## Development Guidelines

### Backend
- Follow Spring Boot best practices
- Use DTOs for API responses
- Implement proper exception handling
- Write comprehensive tests
- Document APIs with OpenAPI annotations

### Frontend
- Use TypeScript strictly
- Follow React best practices
- Implement proper error boundaries
- Use React Query for server state
- Keep components small and focused

## Security Considerations

- JWT tokens with refresh mechanism
- CORS properly configured
- SQL injection prevention with JPA
- XSS protection
- Rate limiting on APIs
- Audit logging for sensitive operations
- Encryption at rest and in transit

## Performance Optimization

- Database indexing on frequently queried columns
- Redis caching for frequently accessed data
- Lazy loading for associations
- Pagination for large datasets
- Connection pooling
- Frontend code splitting
- Image optimization

## Monitoring

- Prometheus metrics exposed at `/api/actuator/prometheus`
- Health checks at `/api/actuator/health`
- Custom business metrics
- Error tracking integration ready
- Performance monitoring

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, email support@taskava.com or create an issue in the GitHub repository.

## Roadmap

- [ ] Mobile applications (iOS/Android)
- [ ] Advanced automation with workflow builder
- [ ] AI-powered task suggestions
- [ ] Time tracking integration
- [ ] Slack/Teams integration
- [ ] Advanced reporting and analytics
- [ ] Custom branding options
- [ ] Plugin system for extensions