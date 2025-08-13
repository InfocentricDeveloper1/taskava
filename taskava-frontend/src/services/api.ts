// Simple API test for backend connectivity
const API_BASE_URL = 'http://localhost:8080/api';

export async function testHealthEndpoint() {
  try {
    const response = await fetch(`${API_BASE_URL}/actuator/health`);
    const data = await response.json();
    return { success: true, data };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

export async function testSwaggerDocs() {
  try {
    const response = await fetch(`${API_BASE_URL}/v3/api-docs`);
    const data = await response.json();
    return { success: true, endpoints: Object.keys(data.paths || {}) };
  } catch (error) {
    return { success: false, error: error.message };
  }
}