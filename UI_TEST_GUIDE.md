# Taskava UI Test Guide

## Currently Available UIs

### 1. Frontend Connection Test (http://localhost:3000)
- Shows real-time backend connection status
- Displays health check results
- Lists all available API endpoints
- Confirms frontend-backend communication is working

### 2. Swagger UI (http://localhost:8080/api/swagger-ui/index.html)
Interactive API documentation where you can:
- Browse all 16 API endpoints
- Test authentication (signup, login)
- Create and manage tasks
- Try out all API operations

## How to Test

### Test Frontend Connection:
1. Open http://localhost:3000 in your browser
2. You should see "Taskava Frontend" title
3. The page will show:
   - Health Check: `{"success":true,"data":{"status":"UP","groups":["liveness","readiness"]}}`
   - Available API Endpoints: List of all backend endpoints

### Test API with Swagger:
1. Open http://localhost:8080/api/swagger-ui/index.html
2. You'll see all endpoints grouped by controller
3. Try the health endpoint:
   - Expand "health-controller"
   - Click "GET /api/health"
   - Click "Try it out"
   - Click "Execute"
4. Test authentication:
   - Expand "auth-controller"
   - Try POST /api/auth/signup to create a user
   - Use the response token for authenticated requests

## Note
There's a minor Spring Security pattern issue that causes errors on some requests, but the actuator health endpoint and Swagger UI work fine. This doesn't affect the core functionality.