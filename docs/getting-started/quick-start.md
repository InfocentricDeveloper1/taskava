# Quick Start Guide

Get Taskava running on your local machine in 5 minutes! This guide assumes you have all [prerequisites](./prerequisites.md) installed.

## 🚀 5-Minute Setup

### Step 1: Clone and Navigate

```bash
git clone https://github.com/InfocentricDeveloper1/taskava.git
cd taskava
```

### Step 2: Start Infrastructure

```bash
# Start all Docker services (PostgreSQL, Redis, LocalStack, Mailhog)
docker-compose up -d

# Verify services are running
docker-compose ps
```

### Step 3: Start Backend

```bash
# In a new terminal
cd taskava-backend
./mvnw spring-boot:run

# Backend will be available at http://localhost:8080
```

### Step 4: Start Frontend

```bash
# In another new terminal
cd taskava-frontend
npm install
npm run dev

# Frontend will open at http://localhost:5173
```

### Step 5: Access the Application

Open your browser and navigate to:
- **Application**: http://localhost:5173
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Email Testing**: http://localhost:8025

## 🎉 That's it! You're up and running!

## Default Credentials

### Application Login
- **Email**: admin@taskava.local
- **Password**: admin123

### Database Access
- **Host**: localhost:5432
- **Database**: taskava
- **Username**: taskava
- **Password**: taskava

### pgAdmin (if enabled)
- **URL**: http://localhost:5050
- **Email**: admin@taskava.local
- **Password**: admin

## Quick Verification

### 1. Check Backend Health
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### 2. Check Frontend
Navigate to http://localhost:5173 and you should see the login page.

### 3. Check Database
```bash
docker exec -it taskava-postgres psql -U taskava -c "\dt"
```

You should see all the Taskava tables.

## Creating Your First Project

1. **Login** with the default credentials
2. **Create an Organization**: Click "New Organization"
3. **Create a Workspace**: Click "New Workspace" 
4. **Create a Project**: Click "New Project"
5. **Add Tasks**: Click "Add Task" or press `n`

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| New Task | `n` |
| Search | `cmd/ctrl + k` |
| Toggle Sidebar | `cmd/ctrl + b` |
| Quick Actions | `cmd/ctrl + shift + p` |
| Mark Complete | `cmd/ctrl + enter` |

## Stopping Services

```bash
# Stop frontend (Ctrl+C in terminal)
# Stop backend (Ctrl+C in terminal)

# Stop Docker services
docker-compose down

# Stop and remove all data
docker-compose down -v
```

## Common Quick Start Issues

### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Use different port
SERVER_PORT=8081 ./mvnw spring-boot:run
```

### Frontend Won't Start

```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Database Connection Failed

```bash
# Ensure PostgreSQL is running
docker-compose ps
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

## Quick Development Tips

### 1. Hot Reload
- **Backend**: Spring DevTools auto-restarts on changes
- **Frontend**: Vite provides instant HMR (Hot Module Replacement)

### 2. Quick Database Reset
```bash
# Reset database with fresh schema
docker-compose down -v
docker-compose up -d postgres
cd taskava-backend && ./mvnw flyway:migrate
```

### 3. View Logs
```bash
# All Docker services
docker-compose logs -f

# Backend application
tail -f taskava-backend/logs/application.log

# Frontend build
cd taskava-frontend && npm run build
```

### 4. Quick API Testing
```bash
# Create a task via API
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "title": "My First Task",
    "description": "Created via API",
    "projectId": "your-project-id"
  }'
```

## What's Next?

Now that you have Taskava running:

1. **Explore Features**
   - Create projects and tasks
   - Try different views (List, Board, Calendar)
   - Test task multi-homing
   - Create custom fields
   - Set up automations

2. **Development**
   - [Backend Development Guide](../development/backend-guide.md)
   - [Frontend Development Guide](../development/frontend-guide.md)
   - [API Documentation](../development/api-reference.md)

3. **Customization**
   - Modify the theme in `taskava-frontend/src/index.css`
   - Add new API endpoints
   - Create custom components

4. **Testing**
   - Run backend tests: `./mvnw test`
   - Run frontend tests: `npm test`
   - Run E2E tests: `npm run test:e2e`

## Useful Scripts

We've included helpful scripts in the project:

```bash
# Full reset and restart
./scripts/reset-local.sh

# Seed with sample data
./scripts/seed-data.sh

# Run all tests
./scripts/run-tests.sh

# Generate API documentation
./scripts/generate-api-docs.sh
```

## Getting Help

- 📖 [Full Documentation](../README.md)
- 🐛 [Report Issues](https://github.com/InfocentricDeveloper1/taskava/issues)
- 💬 [Discussions](https://github.com/InfocentricDeveloper1/taskava/discussions)
- 📧 Email: support@taskava.com

## Pro Tips

1. **Use Multiple Terminals**: Keep separate terminals for backend, frontend, and Docker logs
2. **Browser DevTools**: Use React DevTools extension for debugging
3. **API Testing**: Import the Postman collection from `docs/api/postman-collection.json`
4. **Database GUI**: Use pgAdmin at http://localhost:5050 for database exploration

Congratulations! You're now ready to develop with Taskava! 🎉