# AWS vs Local Development Guide

This guide explains the differences between local development and AWS deployment, helping you understand when and how to transition from local to cloud infrastructure.

## Overview

Taskava is designed with a **local-first development** approach:
- **Develop locally** with Docker containers mimicking AWS services
- **No AWS account required** for development
- **Deploy to AWS** only when ready for staging/production
- **Seamless transition** with environment-based configuration

## Service Comparison

| Feature | Local Development | AWS Deployment |
|---------|------------------|----------------|
| **Database** | PostgreSQL in Docker | RDS PostgreSQL |
| **Cache** | Redis in Docker | ElastiCache Redis |
| **File Storage** | LocalStack S3 | Amazon S3 |
| **Email** | Mailhog | Amazon SES |
| **Authentication** | Local JWT | AWS Cognito |
| **Message Queue** | Redis Pub/Sub | Amazon SQS/SNS |
| **Search** | PostgreSQL Full-text | OpenSearch |
| **CDN** | Nginx proxy | CloudFront |
| **Load Balancer** | Nginx | Application Load Balancer |
| **Container Runtime** | Docker Compose | ECS Fargate |
| **Secrets** | Environment files | AWS Secrets Manager |
| **Monitoring** | Local logs | CloudWatch |

## Architecture Comparison

### Local Architecture
```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│   Browser   │────▶│    Nginx     │────▶│ Spring Boot  │
│  Port 5173  │     │   Port 80    │     │  Port 8080   │
└─────────────┘     └──────────────┘     └──────┬───────┘
                                                 │
                    ┌────────────────────────────┴────────┐
                    │                                     │
            ┌───────▼────────┐              ┌────────────▼─────────┐
            │   PostgreSQL   │              │       Redis          │
            │   Port 5432    │              │     Port 6379        │
            └────────────────┘              └──────────────────────┘
```

### AWS Architecture
```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Browser   │────▶│ CloudFront   │────▶│     ALB      │────▶│ ECS Fargate  │
│             │     │     CDN      │     │Load Balancer │     │   Containers │
└─────────────┘     └──────────────┘     └──────────────┘     └──────┬───────┘
                                                                       │
                    ┌──────────────────────────────────────────────────┴───────┐
                    │                                                          │
            ┌───────▼────────┐    ┌──────────────┐    ┌──────────────────────▼─────────┐
            │  RDS PostgreSQL│    │  ElastiCache │    │      S3 + Cognito            │
            │  Multi-AZ      │    │    Redis     │    │   File Storage + Auth        │
            └────────────────┘    └──────────────┘    └────────────────────────────┘
```

## Configuration Management

### Environment-Based Configuration

The application uses Spring profiles and environment variables to switch between local and AWS:

```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskava
  redis:
    host: localhost
    port: 6379
  
aws:
  s3:
    endpoint: http://localhost:4566  # LocalStack
    region: us-east-1
    bucket: taskava-local

# application-aws.yml  
spring:
  datasource:
    url: ${DATABASE_URL}  # From AWS Secrets Manager
  redis:
    host: ${REDIS_HOST}   # ElastiCache endpoint
    port: 6379

aws:
  s3:
    region: ${AWS_REGION}
    bucket: ${S3_BUCKET_NAME}
```

### Frontend Configuration

```javascript
// config/environment.js
const config = {
  local: {
    API_URL: 'http://localhost:8080/api',
    WS_URL: 'ws://localhost:8080/ws',
    AUTH_PROVIDER: 'local'
  },
  production: {
    API_URL: 'https://api.taskava.com/api',
    WS_URL: 'wss://api.taskava.com/ws',
    AUTH_PROVIDER: 'cognito'
  }
};
```

## Development Workflow

### 1. Local Development (No AWS)

```bash
# Start local services
docker-compose up -d

# Develop with hot reload
cd taskava-backend && ./mvnw spring-boot:run
cd taskava-frontend && npm run dev

# Test features locally
# - File uploads go to LocalStack S3
# - Emails captured in Mailhog
# - Database in local PostgreSQL
```

### 2. AWS Integration Testing (Optional)

```bash
# Use hybrid mode - local app with some AWS services
export SPRING_PROFILES_ACTIVE=local,aws-s3
export AWS_PROFILE=taskava-dev

# Now file uploads go to real S3, but database stays local
./mvnw spring-boot:run
```

### 3. Full AWS Deployment

```bash
# Deploy infrastructure
cd infrastructure/terraform
terraform apply -var-file=environments/dev.tfvars

# Deploy application
./scripts/deploy.sh -e dev -a apply
```

## Cost Comparison

### Local Development Costs
- **Infrastructure**: $0 (runs on your machine)
- **Services**: $0 (all containerized)
- **Data Transfer**: $0 (all local)
- **Total**: **$0/month**

### AWS Development Environment
- **RDS** (db.t3.micro): ~$15/month
- **ElastiCache** (cache.t3.micro): ~$13/month
- **ECS Fargate** (0.25 vCPU): ~$10/month
- **Load Balancer**: ~$20/month
- **Data Transfer**: ~$5/month
- **Total**: **~$63/month**

### AWS Production Environment
- **RDS** (db.r6g.large, Multi-AZ): ~$200/month
- **ElastiCache** (cache.r6g.large): ~$100/month
- **ECS Fargate** (2 vCPU, 3 tasks): ~$100/month
- **Load Balancer**: ~$20/month
- **CloudFront**: ~$10/month
- **S3 & Transfer**: ~$50/month
- **Total**: **~$480/month**

## When to Use Each Environment

### Use Local Development When:
- Building new features
- Running tests
- Debugging issues
- Learning the codebase
- Demonstrating to stakeholders
- Working offline
- Minimizing costs

### Use AWS Development When:
- Testing AWS-specific features (Cognito, SES)
- Performance testing with real services
- Integration testing with other AWS services
- Demonstrating to clients
- Testing deployment procedures

### Use AWS Production When:
- Launching to real users
- Need high availability
- Require geographic distribution
- Need production-grade security
- Scaling beyond single machine

## Migration Path

### Step 1: Start Local
```bash
# Everything runs locally
docker-compose up -d
# Develop features, test thoroughly
```

### Step 2: Gradual AWS Integration
```bash
# Test with real S3
export USE_AWS_S3=true
export AWS_PROFILE=taskava-dev

# Test with real email
export USE_AWS_SES=true
```

### Step 3: Deploy to AWS Dev
```bash
# Create dev environment
terraform workspace new dev
terraform apply -var-file=environments/dev.tfvars

# Deploy application
./scripts/deploy.sh -e dev -a apply
```

### Step 4: Production Deployment
```bash
# Create production environment
terraform workspace new prod
terraform apply -var-file=environments/prod.tfvars

# Deploy with zero downtime
./scripts/deploy.sh -e prod -a apply
```

## Feature Parity

### Features That Work Identically

✅ **Core Functionality**
- Task management
- Project organization  
- User authentication
- File uploads
- Email notifications
- Real-time updates
- Search functionality

✅ **Development Experience**
- Hot reload
- Debugging
- Testing
- Database migrations

### Features Requiring AWS

❌ **Local Limitations**
- Auto-scaling (ECS manages this)
- Multi-region deployment
- CloudFront CDN caching
- AWS WAF protection
- Cognito social logins
- SES email reputation
- CloudWatch alerting

## Best Practices

### 1. Configuration Management

```bash
# Never commit AWS credentials
echo ".env" >> .gitignore
echo "application-local.yml" >> .gitignore

# Use environment variables
export DATABASE_URL=$(aws secretsmanager get-secret-value...)
```

### 2. Data Management

```bash
# Backup local data before AWS migration
docker exec taskava-postgres pg_dump -U taskava > backup.sql

# Restore to AWS RDS
psql -h your-rds-endpoint.amazonaws.com -U taskava < backup.sql
```

### 3. Testing Strategy

```yaml
# Run tests locally first
local:
  - unit tests
  - integration tests  
  - e2e tests with Docker

# Then test on AWS
aws-dev:
  - smoke tests
  - performance tests
  - security scans
```

### 4. Cost Optimization

- Use local development for all feature work
- Share AWS dev environment among team
- Use AWS Free Tier where possible
- Enable auto-shutdown for dev environments
- Use Reserved Instances for production

## Troubleshooting

### Common Local Issues

```bash
# Service won't start
docker-compose logs service-name
docker-compose restart service-name

# Port conflicts
lsof -i :PORT
kill -9 PID
```

### Common AWS Issues

```bash
# Connection refused
# Check security groups
aws ec2 describe-security-groups

# Permission denied
# Check IAM roles
aws sts get-caller-identity
```

## Migration Checklist

- [ ] All tests pass locally
- [ ] Environment variables documented
- [ ] Secrets moved to AWS Secrets Manager
- [ ] Database migrated successfully
- [ ] File storage moved to S3
- [ ] Email sending configured
- [ ] Monitoring set up
- [ ] Backup procedures tested
- [ ] Rollback plan ready

## Conclusion

The local-first approach allows you to:
- **Develop without AWS costs**
- **Test everything locally**
- **Deploy when ready**
- **Scale gradually**

Start local, validate your features, then deploy to AWS with confidence!