// Core types for Taskava

// Re-export from schemas
export * from './schemas/task';

export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
  role: 'admin' | 'user' | 'viewer';
}

export interface Workspace {
  id: string;
  name: string;
  description?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface Project {
  id: string;
  workspaceId: string;
  name: string;
  description?: string;
  color?: string;
  icon?: string;
  status: 'active' | 'archived' | 'completed';
  ownerId: string;
  teamIds: string[];
  createdAt: Date;
  updatedAt: Date;
  taskCount?: number;
  completedTaskCount?: number;
}

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  projectIds: string[]; // Multi-homing support
  assigneeId?: string;
  assignee?: User;
  dueDate?: Date;
  completedAt?: Date;
  tags: string[];
  customFields?: Record<string, any>;
  attachments?: Attachment[];
  comments?: Comment[];
  createdAt: Date;
  updatedAt: Date;
  createdBy: string;
}

// TaskStatus and TaskPriority are exported from ./schemas/task

export interface TaskSection {
  id: string;
  projectId: string;
  name: string;
  order: number;
  tasks?: Task[];
}

export interface Team {
  id: string;
  workspaceId: string;
  name: string;
  description?: string;
  memberIds: string[];
  createdAt: Date;
  updatedAt: Date;
}

export interface Comment {
  id: string;
  taskId: string;
  userId: string;
  user?: User;
  content: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface Attachment {
  id: string;
  name: string;
  url: string;
  type: string;
  size: number;
  uploadedBy: string;
  uploadedAt: Date;
}

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
  errors?: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}