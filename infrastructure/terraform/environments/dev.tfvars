environment = "dev"
region      = "us-east-1"

# VPC Configuration
vpc_cidr           = "10.0.0.0/16"
availability_zones = ["us-east-1a", "us-east-1b"]

# Database Configuration
database_instance_class = "db.t3.micro"
database_name           = "taskava_dev"
database_username       = "taskava_admin"
backup_retention_period = 1
multi_az                = false

# ElastiCache Configuration
elasticache_node_type = "cache.t3.micro"

# ECS Configuration
ecs_task_cpu    = "256"
ecs_task_memory = "512"
min_capacity    = 1
max_capacity    = 3

# Domain Configuration
domain_name     = "dev.taskava.example.com"
certificate_arn = "" # Will be created separately

# Security Configuration
enable_deletion_protection = false

# Tags
tags = {
  Environment = "Development"
  Team        = "Engineering"
  CostCenter  = "Development"
}