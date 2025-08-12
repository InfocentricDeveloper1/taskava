# CLAUDE.md - Infrastructure Directory

This directory contains all infrastructure as code (IaC) and deployment configurations for Taskava.

## Directory Structure

```
infrastructure/
├── terraform/          # AWS infrastructure as code
│   ├── environments/   # Environment-specific configs
│   ├── modules/        # Reusable Terraform modules
│   └── README.md       # Terraform documentation
├── scripts/            # Deployment and utility scripts
├── docker/            # Docker configurations
└── k8s/               # Kubernetes manifests (future)
```

## Infrastructure Overview

### Local Development
- **Docker Compose**: Complete local environment
- **Services**: PostgreSQL, Redis, LocalStack (AWS emulation), Mailhog
- **Zero AWS costs**: Everything runs locally

### AWS Production Architecture
```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│ CloudFront  │────▶│     ALB      │────▶│ ECS Fargate  │
│    (CDN)    │     │Load Balancer │     │ (Containers) │
└─────────────┘     └──────────────┘     └──────┬───────┘
                                                 │
                    ┌────────────────────────────┴─────────────┐
                    │                                          │
         ┌──────────▼──────────┐              ┌───────────────▼──────────┐
         │   RDS PostgreSQL    │              │   ElastiCache Redis      │
         │   (Multi-AZ)        │              │   (Cluster Mode)         │
         └─────────────────────┘              └──────────────────────────┘
                    │
         ┌──────────▼──────────┐              ┌──────────────────────────┐
         │      S3 Buckets     │              │     Cognito User Pool    │
         │  (Files & Backups)  │              │   (Authentication)       │
         └─────────────────────┘              └──────────────────────────┘
```

## Terraform Structure

### Module Organization
Each module in `terraform/modules/` handles specific AWS services:
- **vpc/**: Network infrastructure
- **rds/**: PostgreSQL database
- **ecs/**: Container orchestration
- **s3/**: File storage
- **cognito/**: User authentication
- **cloudfront/**: CDN setup
- **monitoring/**: CloudWatch, alarms

### Environment Management
```bash
# Development
terraform workspace select dev
terraform apply -var-file=environments/dev.tfvars

# Production
terraform workspace select prod
terraform apply -var-file=environments/prod.tfvars
```

### Key Variables
- `environment`: dev, staging, prod
- `region`: AWS region
- `instance_sizes`: EC2/RDS instance types
- `backup_retention`: Days to retain backups
- `multi_az`: High availability setting

## Deployment Strategy

### Local Development
```bash
# Start everything
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f [service]

# Stop everything
docker-compose down
```

### AWS Deployment
1. **Infrastructure**: Terraform creates all AWS resources
2. **Application**: Docker images pushed to ECR
3. **Deployment**: ECS rolling updates with zero downtime
4. **Rollback**: Automatic on health check failure

### CI/CD Pipeline
GitHub Actions workflow:
1. Run tests
2. Build Docker images
3. Push to ECR
4. Update ECS service
5. Run smoke tests

## Service Configuration

### Database (RDS)
- Engine: PostgreSQL 15
- Multi-AZ for production
- Automated backups
- Point-in-time recovery
- Performance Insights enabled

### Caching (ElastiCache)
- Engine: Redis 7
- Cluster mode for production
- Automatic failover
- Backup and restore

### File Storage (S3)
- Versioning enabled
- Lifecycle policies
- Cross-region replication (prod)
- Server-side encryption

### Authentication (Cognito)
- User pools for auth
- MFA support
- Social login providers
- Custom domain

## Security Measures

### Network Security
- VPC with public/private subnets
- Security groups (least privilege)
- NACLs for additional control
- VPC Flow Logs

### Application Security
- WAF rules for common attacks
- SSL/TLS everywhere
- Secrets in Parameter Store
- IAM roles (not keys)

### Compliance
- Encryption at rest (KMS)
- Encryption in transit
- Audit logging (CloudTrail)
- Backup encryption

## Monitoring & Observability

### CloudWatch Metrics
- Application metrics
- Infrastructure metrics
- Custom business metrics
- Log aggregation

### Alarms
- High CPU/Memory
- Database connections
- Error rates
- Response times

### Dashboards
- System health overview
- Business metrics
- Cost tracking
- Performance trends

## Cost Optimization

### Development Environment
- Smaller instances
- Single NAT gateway
- No Multi-AZ
- Shorter retention

### Production Environment
- Reserved instances
- Spot instances for workers
- S3 lifecycle policies
- Auto-scaling based on load

### Estimated Costs
- **Dev**: ~$65/month
- **Staging**: ~$150/month
- **Prod**: ~$500-1000/month (varies with load)

## Disaster Recovery

### Backup Strategy
- **RDS**: Daily automated backups, 30-day retention
- **S3**: Cross-region replication
- **Code**: Git repository
- **Infrastructure**: Terraform state in S3

### Recovery Procedures
- **RTO**: 4 hours
- **RPO**: 1 hour
- Documented runbooks
- Regular DR drills

## Scripts

### Deployment Script (`scripts/deploy.sh`)
```bash
./deploy.sh -e prod -a apply
```
- Runs tests
- Builds applications
- Deploys infrastructure
- Updates services

### Backup Script (`scripts/backup.sh`)
- Database dump
- S3 sync
- Configuration backup

## Important Notes

1. **State Management**: Terraform state stored in S3 with locking
2. **Secrets**: Never commit secrets, use AWS Secrets Manager
3. **Tagging**: All resources tagged for cost tracking
4. **Naming**: Consistent naming convention: `taskava-{env}-{service}`
5. **Documentation**: Update README when adding modules

## Common Tasks

### Scale ECS Tasks
```bash
aws ecs update-service --cluster taskava-prod --service taskava-api --desired-count 5
```

### Database Connection
```bash
aws rds describe-db-instances --db-instance-identifier taskava-prod
```

### View Logs
```bash
aws logs tail /aws/ecs/taskava-prod --follow
```

### Cost Analysis
```bash
aws ce get-cost-and-usage --time-period Start=2024-01-01,End=2024-01-31
```