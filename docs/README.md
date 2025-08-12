# Taskava Documentation

Welcome to the Taskava documentation! This guide will help you get started with development, deployment, and operations of the Taskava project management platform.

## Documentation Structure

```
docs/
├── getting-started/
│   ├── local-development.md    # Local development setup
│   ├── prerequisites.md         # System requirements
│   └── quick-start.md          # 5-minute quick start
├── development/
│   ├── backend-guide.md        # Spring Boot development
│   ├── frontend-guide.md       # React development
│   ├── database-guide.md       # Database schema and migrations
│   └── api-reference.md        # REST API documentation
├── deployment/
│   ├── aws-setup.md           # AWS account setup
│   ├── terraform-guide.md     # Infrastructure deployment
│   ├── ci-cd-pipeline.md      # GitHub Actions setup
│   └── production-deploy.md   # Production deployment guide
├── architecture/
│   ├── system-design.md       # High-level architecture
│   ├── database-schema.md     # Database design
│   ├── security.md            # Security architecture
│   └── scalability.md         # Scaling strategies
└── operations/
    ├── monitoring.md          # Monitoring and alerts
    ├── troubleshooting.md     # Common issues
    ├── backup-restore.md      # Backup procedures
    └── disaster-recovery.md   # DR procedures
```

## Quick Links

### For Developers
- [Local Development Setup](./getting-started/local-development.md) - Get running in 10 minutes
- [Backend Development Guide](./development/backend-guide.md) - Spring Boot best practices
- [Frontend Development Guide](./development/frontend-guide.md) - React and Shadcn UI
- [API Reference](./development/api-reference.md) - REST API documentation

### For DevOps
- [AWS Setup Guide](./deployment/aws-setup.md) - AWS account configuration
- [Terraform Guide](./deployment/terraform-guide.md) - Infrastructure as Code
- [CI/CD Pipeline](./deployment/ci-cd-pipeline.md) - Automated deployments
- [Monitoring Setup](./operations/monitoring.md) - CloudWatch and alerts

### For Architects
- [System Design](./architecture/system-design.md) - Architecture overview
- [Security Architecture](./architecture/security.md) - Security best practices
- [Database Schema](./architecture/database-schema.md) - Data model design
- [Scalability Guide](./architecture/scalability.md) - Scaling strategies

## Getting Started

### Prerequisites
- Docker Desktop installed
- Java 17+ (for backend development)
- Node.js 18+ (for frontend development)
- Git
- Your favorite IDE (IntelliJ IDEA, VS Code)

### Quick Start (5 minutes)
```bash
# Clone the repository
git clone https://github.com/InfocentricDeveloper1/taskava.git
cd taskava

# Start local services
docker-compose up -d

# Start backend (in a new terminal)
cd taskava-backend
./mvnw spring-boot:run

# Start frontend (in another terminal)
cd taskava-frontend
npm install
npm run dev

# Open http://localhost:5173 in your browser
```

## Technology Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **API**: RESTful with OpenAPI 3.0

### Frontend
- **Language**: TypeScript
- **Framework**: React 18
- **UI Library**: Shadcn UI
- **Styling**: Tailwind CSS
- **Build Tool**: Vite
- **State Management**: Zustand/React Query

### Infrastructure
- **Cloud**: AWS
- **IaC**: Terraform
- **Container**: Docker
- **Orchestration**: ECS Fargate
- **CI/CD**: GitHub Actions

## Development Workflow

1. **Local Development**: Use Docker Compose for all services
2. **Feature Development**: Create feature branches
3. **Testing**: Run unit and integration tests
4. **Code Review**: Submit PR for review
5. **CI/CD**: Automated testing and deployment
6. **Deployment**: Progressive rollout (dev → staging → prod)

## Contributing

Please read our [Contributing Guide](../CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## Support

- **Documentation Issues**: Open a GitHub issue
- **Security Issues**: Email security@taskava.com
- **General Questions**: Use GitHub Discussions

## License

This project is proprietary software. See [LICENSE](../LICENSE) for details.