import axios, { AxiosInstance } from 'axios';
import { 
  ApiResponse, 
  PaginatedResponse, 
  Project, 
  Task, 
  Team, 
  User, 
  Workspace,
  TaskSection 
} from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance with default config
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor for auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Health check endpoints
export async function testHealthEndpoint() {
  try {
    const response = await fetch(`${API_BASE_URL}/actuator/health`);
    const data = await response.json();
    return { success: true, data };
  } catch (error: any) {
    return { success: false, error: error.message };
  }
}

export async function testSwaggerDocs() {
  try {
    const response = await fetch(`${API_BASE_URL}/v3/api-docs`);
    const data = await response.json();
    return { success: true, endpoints: Object.keys(data.paths || {}) };
  } catch (error: any) {
    return { success: false, error: error.message };
  }
}

// Auth API
export const authApi = {
  login: async (email: string, password: string) => {
    const response = await apiClient.post<ApiResponse<{ token: string; user: User }>>(
      '/auth/login',
      { email, password }
    );
    return response.data;
  },
  
  register: async (data: { email: string; password: string; name: string }) => {
    const response = await apiClient.post<ApiResponse<{ token: string; user: User }>>(
      '/auth/register',
      data
    );
    return response.data;
  },
  
  logout: async () => {
    const response = await apiClient.post('/auth/logout');
    return response.data;
  },
  
  getCurrentUser: async () => {
    const response = await apiClient.get<ApiResponse<User>>('/auth/me');
    return response.data;
  },
};

// Workspace API
export const workspaceApi = {
  getAll: async () => {
    const response = await apiClient.get<ApiResponse<Workspace[]>>('/workspaces');
    return response.data;
  },
  
  getById: async (id: string) => {
    const response = await apiClient.get<ApiResponse<Workspace>>(`/workspaces/${id}`);
    return response.data;
  },
  
  create: async (data: Partial<Workspace>) => {
    const response = await apiClient.post<ApiResponse<Workspace>>('/workspaces', data);
    return response.data;
  },
  
  update: async (id: string, data: Partial<Workspace>) => {
    const response = await apiClient.put<ApiResponse<Workspace>>(`/workspaces/${id}`, data);
    return response.data;
  },
  
  delete: async (id: string) => {
    const response = await apiClient.delete(`/workspaces/${id}`);
    return response.data;
  },
};

// Project API
export const projectApi = {
  getAll: async (workspaceId?: string, page = 0, size = 20) => {
    const params = new URLSearchParams();
    if (workspaceId) params.append('workspaceId', workspaceId);
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await apiClient.get<PaginatedResponse<Project>>(
      `/projects?${params.toString()}`
    );
    return response.data;
  },
  
  getById: async (id: string) => {
    const response = await apiClient.get<ApiResponse<Project>>(`/projects/${id}`);
    return response.data;
  },
  
  create: async (data: Partial<Project>) => {
    const response = await apiClient.post<ApiResponse<Project>>('/projects', data);
    return response.data;
  },
  
  update: async (id: string, data: Partial<Project>) => {
    const response = await apiClient.put<ApiResponse<Project>>(`/projects/${id}`, data);
    return response.data;
  },
  
  delete: async (id: string) => {
    const response = await apiClient.delete(`/projects/${id}`);
    return response.data;
  },
  
  getSections: async (projectId: string) => {
    const response = await apiClient.get<ApiResponse<TaskSection[]>>(
      `/projects/${projectId}/sections`
    );
    return response.data;
  },
};

// Task API
export const taskApi = {
  getAll: async (filters?: {
    projectId?: string;
    assigneeId?: string;
    status?: string;
    priority?: string;
    page?: number;
    size?: number;
  }) => {
    const params = new URLSearchParams();
    if (filters?.projectId) params.append('projectId', filters.projectId);
    if (filters?.assigneeId) params.append('assigneeId', filters.assigneeId);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.priority) params.append('priority', filters.priority);
    params.append('page', (filters?.page || 0).toString());
    params.append('size', (filters?.size || 20).toString());
    
    const response = await apiClient.get<PaginatedResponse<Task>>(
      `/tasks?${params.toString()}`
    );
    return response.data;
  },
  
  getById: async (id: string) => {
    const response = await apiClient.get<ApiResponse<Task>>(`/tasks/${id}`);
    return response.data;
  },
  
  create: async (data: Partial<Task>) => {
    const response = await apiClient.post<ApiResponse<Task>>('/tasks', data);
    return response.data;
  },
  
  update: async (id: string, data: Partial<Task>) => {
    const response = await apiClient.put<ApiResponse<Task>>(`/tasks/${id}`, data);
    return response.data;
  },
  
  updateStatus: async (id: string, status: string) => {
    const response = await apiClient.patch<ApiResponse<Task>>(
      `/tasks/${id}/status`,
      { status }
    );
    return response.data;
  },
  
  delete: async (id: string) => {
    const response = await apiClient.delete(`/tasks/${id}`);
    return response.data;
  },
  
  assignTask: async (taskId: string, userId: string) => {
    const response = await apiClient.patch<ApiResponse<Task>>(
      `/tasks/${taskId}/assign`,
      { userId }
    );
    return response.data;
  },
};

// Team API
export const teamApi = {
  getAll: async (workspaceId?: string) => {
    const params = workspaceId ? `?workspaceId=${workspaceId}` : '';
    const response = await apiClient.get<ApiResponse<Team[]>>(`/teams${params}`);
    return response.data;
  },
  
  getById: async (id: string) => {
    const response = await apiClient.get<ApiResponse<Team>>(`/teams/${id}`);
    return response.data;
  },
  
  create: async (data: Partial<Team>) => {
    const response = await apiClient.post<ApiResponse<Team>>('/teams', data);
    return response.data;
  },
  
  update: async (id: string, data: Partial<Team>) => {
    const response = await apiClient.put<ApiResponse<Team>>(`/teams/${id}`, data);
    return response.data;
  },
  
  delete: async (id: string) => {
    const response = await apiClient.delete(`/teams/${id}`);
    return response.data;
  },
  
  addMember: async (teamId: string, userId: string) => {
    const response = await apiClient.post(
      `/teams/${teamId}/members`,
      { userId }
    );
    return response.data;
  },
  
  removeMember: async (teamId: string, userId: string) => {
    const response = await apiClient.delete(
      `/teams/${teamId}/members/${userId}`
    );
    return response.data;
  },
};

export default apiClient;