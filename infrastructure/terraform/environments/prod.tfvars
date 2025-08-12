environment = "prod"
region      = "us-east-1"

# VPC Configuration
vpc_cidr           = "10.1.0.0/16"
availability_zones = ["us-east-1a", "us-east-1b", "us-east-1c"]

# Database Configuration
database_instance_class = "db.r6g.large"
database_name           = "taskava_prod"
database_username       = "taskava_admin"
backup_retention_period = 30
multi_az                = true

# ElastiCache Configuration
elasticache_node_type = "cache.r6g.large"

# ECS Configuration
ecs_task_cpu    = "2048"
ecs_task_memory = "4096"
min_capacity    = 3
max_capacity    = 20

# Domain Configuration
domain_name     = "app.taskava.com"
certificate_arn = "" # Must be provided for production

# Security Configuration
enable_deletion_protection = true

# Tags
tags = {
  Environment = "Production"
  Team        = "Engineering"
  CostCenter  = "Production"
  Compliance  = "SOC2"
}