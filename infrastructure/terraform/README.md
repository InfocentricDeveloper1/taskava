# Taskava Infrastructure as Code

This directory contains Terraform configurations for deploying the Taskava platform on AWS.

## Architecture Overview

The infrastructure includes:
- **VPC** with public/private/database subnets across multiple AZs
- **ECS Fargate** for containerized application deployment
- **Application Load Balancer** with auto-scaling
- **RDS PostgreSQL** with Multi-AZ for production
- **ElastiCache Redis** for caching and sessions
- **S3** for file storage
- **CloudFront** CDN for static assets
- **Cognito** for authentication
- **WAF** for application security
- **CloudWatch** for monitoring and logging

## Prerequisites

- Terraform >= 1.5.0
- AWS CLI configured with appropriate credentials
- S3 bucket for Terraform state (backend configuration)
- DynamoDB table for state locking
- ACM certificate for HTTPS (production)

## Directory Structure

```
terraform/
├── environments/          # Environment-specific configurations
│   ├── dev.tfvars
│   ├── staging.tfvars
│   └── prod.tfvars
├── modules/              # Reusable Terraform modules
│   ├── vpc/             # VPC and networking
│   ├── security/        # Security groups and IAM
│   ├── rds/             # RDS PostgreSQL
│   ├── elasticache/     # Redis cluster
│   ├── s3/              # S3 buckets
│   ├── cognito/         # User authentication
│   ├── ecs/             # ECS cluster and services
│   ├── cloudfront/      # CDN configuration
│   ├── waf/             # Web Application Firewall
│   └── monitoring/      # CloudWatch and alarms
├── main.tf              # Root module
├── variables.tf         # Variable definitions
├── outputs.tf           # Output values
├── versions.tf          # Provider versions
└── README.md            # This file
```

## Getting Started

### 1. Backend Configuration

Create a backend configuration file `backend-config.hcl`:

```hcl
bucket         = "your-terraform-state-bucket"
key            = "taskava/terraform.tfstate"
region         = "us-east-1"
encrypt        = true
dynamodb_table = "terraform-state-locks"
```

### 2. Initialize Terraform

```bash
terraform init -backend-config=backend-config.hcl
```

### 3. Create Workspace

```bash
# For development
terraform workspace new dev

# For production
terraform workspace new prod
```

### 4. Deploy Infrastructure

```bash
# Development
terraform plan -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars

# Production
terraform plan -var-file=environments/prod.tfvars
terraform apply -var-file=environments/prod.tfvars
```

## Environment Variables

Sensitive values should be provided through environment variables:

```bash
export TF_VAR_database_username="taskava_admin"
export TF_VAR_certificate_arn="arn:aws:acm:..."
```

## Modules

### VPC Module
- Creates VPC with public, private, and database subnets
- Configures NAT gateways for private subnet internet access
- Sets up VPC flow logs for security monitoring

### Security Module
- Creates security groups for each service
- Manages IAM roles and policies
- Stores secrets in Parameter Store

### RDS Module
- Deploys PostgreSQL with encryption at rest
- Configures automated backups
- Implements Multi-AZ for production

### ECS Module
- Creates ECS cluster and task definitions
- Configures auto-scaling based on CPU/memory
- Sets up ALB with health checks

### Monitoring Module
- CloudWatch dashboards
- Log groups and metric filters
- SNS topics for alerts

## Cost Optimization

### Development Environment
- Single NAT gateway
- Smaller instance types
- No Multi-AZ deployments
- Shorter backup retention

### Production Environment
- NAT gateway per AZ
- Reserved instances for predictable workloads
- Multi-AZ for high availability
- 30-day backup retention

## Security Best Practices

1. **Encryption**
   - All data encrypted at rest
   - TLS 1.2+ for data in transit
   - KMS keys with automatic rotation

2. **Network Security**
   - Private subnets for application and database
   - Security groups with least privilege
   - WAF rules for common attacks

3. **Access Control**
   - IAM roles for service access
   - No hardcoded credentials
   - MFA required for production

## Monitoring and Alerts

The infrastructure includes comprehensive monitoring:

- **Application Metrics**: Response time, error rate, throughput
- **Infrastructure Metrics**: CPU, memory, disk usage
- **Security Metrics**: Failed logins, WAF blocks
- **Cost Alerts**: Budget thresholds

## Disaster Recovery

- **RTO**: 4 hours
- **RPO**: 1 hour
- Automated backups to S3
- Cross-region replication available
- Runbooks in `docs/disaster-recovery/`

## Compliance

The infrastructure is designed to support:
- SOC 2 Type II
- HIPAA (with additional configuration)
- GDPR
- ISO 27001

## Troubleshooting

### Common Issues

1. **State Lock Error**
   ```bash
   terraform force-unlock <lock-id>
   ```

2. **Insufficient Permissions**
   - Ensure IAM user has required policies
   - Check CloudTrail for permission errors

3. **Resource Limits**
   - Request limit increases through AWS Support
   - Check Service Quotas dashboard

## Destroy Infrastructure

```bash
# Remove deletion protection first
terraform apply -var-file=environments/dev.tfvars -var="enable_deletion_protection=false"

# Destroy resources
terraform destroy -var-file=environments/dev.tfvars
```

## Contributing

1. Create feature branch
2. Make changes in modules
3. Test in dev environment
4. Submit PR with plan output
5. Apply after review

## Support

For issues or questions:
- Check CloudWatch logs
- Review Terraform state
- Contact DevOps team