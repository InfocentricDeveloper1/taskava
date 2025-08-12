#!/bin/bash

# Taskava Deployment Script
# This script handles deployment of the Taskava platform

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT=""
ACTION=""
SKIP_TESTS=false
SKIP_BUILD=false

# Function to print colored output
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to show usage
usage() {
    echo "Usage: $0 -e <environment> -a <action> [options]"
    echo ""
    echo "Required arguments:"
    echo "  -e <environment>  Environment (dev, staging, prod)"
    echo "  -a <action>       Action (plan, apply, destroy)"
    echo ""
    echo "Optional arguments:"
    echo "  -s               Skip tests"
    echo "  -b               Skip build"
    echo "  -h               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -e dev -a plan"
    echo "  $0 -e prod -a apply -s"
    exit 1
}

# Parse command line arguments
while getopts "e:a:sbh" opt; do
    case ${opt} in
        e)
            ENVIRONMENT=$OPTARG
            ;;
        a)
            ACTION=$OPTARG
            ;;
        s)
            SKIP_TESTS=true
            ;;
        b)
            SKIP_BUILD=true
            ;;
        h)
            usage
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            usage
            ;;
    esac
done

# Validate required arguments
if [ -z "$ENVIRONMENT" ] || [ -z "$ACTION" ]; then
    print_message $RED "Error: Missing required arguments"
    usage
fi

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|prod)$ ]]; then
    print_message $RED "Error: Invalid environment. Must be dev, staging, or prod"
    exit 1
fi

# Validate action
if [[ ! "$ACTION" =~ ^(plan|apply|destroy)$ ]]; then
    print_message $RED "Error: Invalid action. Must be plan, apply, or destroy"
    exit 1
fi

print_message $GREEN "Starting deployment for environment: $ENVIRONMENT"

# Set working directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname $(dirname $SCRIPT_DIR))"
TERRAFORM_DIR="$PROJECT_ROOT/infrastructure/terraform"

# Check if AWS credentials are configured
if ! aws sts get-caller-identity &> /dev/null; then
    print_message $RED "Error: AWS credentials not configured"
    exit 1
fi

# Run tests if not skipped
if [ "$SKIP_TESTS" = false ]; then
    print_message $YELLOW "Running tests..."
    cd $PROJECT_ROOT/taskava-backend
    ./mvnw test
    cd $PROJECT_ROOT/taskava-frontend
    npm test
fi

# Build applications if not skipped
if [ "$SKIP_BUILD" = false ]; then
    print_message $YELLOW "Building applications..."
    
    # Build backend
    cd $PROJECT_ROOT/taskava-backend
    ./mvnw clean package -DskipTests
    
    # Build frontend
    cd $PROJECT_ROOT/taskava-frontend
    npm run build
    
    # Build Docker images
    print_message $YELLOW "Building Docker images..."
    cd $PROJECT_ROOT
    docker build -t taskava-backend:latest -f taskava-backend/Dockerfile taskava-backend/
    docker build -t taskava-frontend:latest -f taskava-frontend/Dockerfile taskava-frontend/
    
    # Push to ECR (if not local dev)
    if [ "$ENVIRONMENT" != "dev" ]; then
        print_message $YELLOW "Pushing images to ECR..."
        # Get ECR login token
        aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ECR_REGISTRY
        
        # Tag and push images
        docker tag taskava-backend:latest $ECR_REGISTRY/taskava-backend:$ENVIRONMENT
        docker tag taskava-frontend:latest $ECR_REGISTRY/taskava-frontend:$ENVIRONMENT
        docker push $ECR_REGISTRY/taskava-backend:$ENVIRONMENT
        docker push $ECR_REGISTRY/taskava-frontend:$ENVIRONMENT
    fi
fi

# Run Terraform
cd $TERRAFORM_DIR

# Initialize Terraform if needed
if [ ! -d ".terraform" ]; then
    print_message $YELLOW "Initializing Terraform..."
    terraform init -backend-config=backend-config.hcl
fi

# Select workspace
print_message $YELLOW "Selecting Terraform workspace: $ENVIRONMENT"
terraform workspace select $ENVIRONMENT || terraform workspace new $ENVIRONMENT

# Execute Terraform action
case $ACTION in
    plan)
        print_message $YELLOW "Running Terraform plan..."
        terraform plan -var-file=environments/$ENVIRONMENT.tfvars -out=tfplan
        ;;
    apply)
        if [ "$ENVIRONMENT" = "prod" ]; then
            print_message $YELLOW "WARNING: You are about to deploy to PRODUCTION!"
            read -p "Are you sure? (yes/no): " confirm
            if [ "$confirm" != "yes" ]; then
                print_message $RED "Deployment cancelled"
                exit 1
            fi
        fi
        
        print_message $YELLOW "Running Terraform apply..."
        terraform apply -var-file=environments/$ENVIRONMENT.tfvars -auto-approve
        
        # Run post-deployment tasks
        print_message $YELLOW "Running post-deployment tasks..."
        
        # Update ECS service with new image
        if [ "$SKIP_BUILD" = false ] && [ "$ENVIRONMENT" != "dev" ]; then
            aws ecs update-service \
                --cluster taskava-$ENVIRONMENT \
                --service taskava-api \
                --force-new-deployment
        fi
        
        # Run database migrations
        print_message $YELLOW "Running database migrations..."
        # This would typically be done through a separate job or as part of app startup
        
        print_message $GREEN "Deployment completed successfully!"
        ;;
    destroy)
        print_message $RED "WARNING: You are about to DESTROY the $ENVIRONMENT environment!"
        read -p "Type 'destroy-$ENVIRONMENT' to confirm: " confirm
        if [ "$confirm" != "destroy-$ENVIRONMENT" ]; then
            print_message $RED "Destruction cancelled"
            exit 1
        fi
        
        print_message $YELLOW "Running Terraform destroy..."
        terraform destroy -var-file=environments/$ENVIRONMENT.tfvars -auto-approve
        ;;
esac

print_message $GREEN "Operation completed successfully!"