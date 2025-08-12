provider "aws" {
  region = var.region
}

locals {
  common_tags = merge(
    var.tags,
    {
      Environment = var.environment
      Project     = var.project_name
      ManagedBy   = "Terraform"
    }
  )
}

# Data sources
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr          = var.vpc_cidr
  availability_zones = var.availability_zones
  tags              = local.common_tags
}

# Security Module
module "security" {
  source = "./modules/security"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.vpc.vpc_id
  tags         = local.common_tags
}

# RDS Module
module "rds" {
  source = "./modules/rds"

  project_name               = var.project_name
  environment                = var.environment
  database_name              = var.database_name
  database_username          = var.database_username
  instance_class             = var.database_instance_class
  subnet_ids                 = module.vpc.database_subnet_ids
  security_group_id          = module.security.rds_security_group_id
  backup_retention_period    = var.backup_retention_period
  multi_az                   = var.multi_az
  enable_deletion_protection = var.enable_deletion_protection
  tags                       = local.common_tags
}

# ElastiCache Module
module "elasticache" {
  source = "./modules/elasticache"

  project_name      = var.project_name
  environment       = var.environment
  subnet_ids        = module.vpc.private_subnet_ids
  security_group_id = module.security.elasticache_security_group_id
  node_type         = var.elasticache_node_type
  tags              = local.common_tags
}

# S3 Module
module "s3" {
  source = "./modules/s3"

  project_name = var.project_name
  environment  = var.environment
  tags         = local.common_tags
}

# Cognito Module
module "cognito" {
  source = "./modules/cognito"

  project_name = var.project_name
  environment  = var.environment
  domain_name  = var.domain_name
  tags         = local.common_tags
}

# ECS Module
module "ecs" {
  source = "./modules/ecs"

  project_name     = var.project_name
  environment      = var.environment
  vpc_id           = module.vpc.vpc_id
  private_subnets  = module.vpc.private_subnet_ids
  public_subnets   = module.vpc.public_subnet_ids
  security_groups  = [module.security.ecs_security_group_id]
  certificate_arn  = var.certificate_arn
  
  # Application configuration
  task_cpu         = var.ecs_task_cpu
  task_memory      = var.ecs_task_memory
  min_capacity     = var.min_capacity
  max_capacity     = var.max_capacity
  
  # Environment variables for the application
  app_environment = {
    SPRING_PROFILES_ACTIVE           = var.environment
    DATABASE_HOST                    = module.rds.endpoint
    DATABASE_PORT                    = "5432"
    DATABASE_NAME                    = var.database_name
    REDIS_HOST                       = module.elasticache.primary_endpoint
    REDIS_PORT                       = "6379"
    AWS_REGION                       = var.region
    S3_BUCKET_NAME                   = module.s3.bucket_name
    COGNITO_USER_POOL_ID             = module.cognito.user_pool_id
    COGNITO_CLIENT_ID                = module.cognito.client_id
    CORS_ALLOWED_ORIGINS             = "https://${var.domain_name}"
  }
  
  # Secrets from Parameter Store
  app_secrets = {
    DATABASE_USERNAME = module.rds.username_parameter_arn
    DATABASE_PASSWORD = module.rds.password_parameter_arn
    JWT_SECRET        = module.security.jwt_secret_parameter_arn
  }
  
  enable_deletion_protection = var.enable_deletion_protection
  tags                      = local.common_tags
}

# CloudFront Module
module "cloudfront" {
  source = "./modules/cloudfront"

  project_name    = var.project_name
  environment     = var.environment
  domain_name     = var.domain_name
  certificate_arn = var.certificate_arn
  alb_domain_name = module.ecs.alb_dns_name
  s3_bucket_name  = module.s3.bucket_name
  tags            = local.common_tags
}

# WAF Module
module "waf" {
  source = "./modules/waf"

  project_name = var.project_name
  environment  = var.environment
  alb_arn      = module.ecs.alb_arn
  tags         = local.common_tags
}

# Monitoring Module
module "monitoring" {
  source = "./modules/monitoring"

  project_name = var.project_name
  environment  = var.environment
  alb_arn      = module.ecs.alb_arn
  tags         = local.common_tags
}