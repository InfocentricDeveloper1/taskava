import { create } from 'zustand';
import { Workspace, Project, Task, User } from '../types';

interface MockDataState {
  mockWorkspace: Workspace;
  mockProjects: Project[];
  mockTasks: Task[];
  mockUser: User;
}

// Create mock data for demo purposes
const mockUser: User = {
  id: '1',
  email: 'demo@taskava.com',
  name: 'Demo User',
  avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo',
  role: 'admin'
};

const mockWorkspace: Workspace = {
  id: 'ws-1',
  name: 'Acme Corporation',
  description: 'Main workspace for Acme Corp',
  createdAt: new Date('2024-01-01'),
  updatedAt: new Date()
};

const mockProjects: Project[] = [
  {
    id: 'proj-1',
    workspaceId: 'ws-1',
    name: 'Website Redesign',
    description: 'Complete redesign of the company website with modern UI/UX',
    color: '#8B5CF6',
    status: 'active',
    ownerId: '1',
    teamIds: ['team-1', 'team-2'],
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date(),
    taskCount: 24,
    completedTaskCount: 8
  },
  {
    id: 'proj-2',
    workspaceId: 'ws-1',
    name: 'Mobile App Development',
    description: 'Native iOS and Android apps for customer portal',
    color: '#06B6D4',
    status: 'active',
    ownerId: '1',
    teamIds: ['team-2'],
    createdAt: new Date('2024-02-01'),
    updatedAt: new Date(),
    taskCount: 45,
    completedTaskCount: 12
  },
  {
    id: 'proj-3',
    workspaceId: 'ws-1',
    name: 'Marketing Campaign Q1',
    description: 'Q1 2024 marketing initiatives and campaigns',
    color: '#10B981',
    status: 'completed',
    ownerId: '1',
    teamIds: ['team-3'],
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date(),
    taskCount: 18,
    completedTaskCount: 18
  },
  {
    id: 'proj-4',
    workspaceId: 'ws-1',
    name: 'Data Migration',
    description: 'Migrate legacy systems to cloud infrastructure',
    color: '#F59E0B',
    status: 'active',
    ownerId: '1',
    teamIds: ['team-1', 'team-4'],
    createdAt: new Date('2024-03-01'),
    updatedAt: new Date(),
    taskCount: 32,
    completedTaskCount: 5
  },
];

const mockTasks: Task[] = [
  {
    id: 'task-1',
    title: 'Design homepage mockup',
    description: 'Create initial design mockups for the new homepage layout',
    status: 'in_progress',
    priority: 'high',
    projectIds: ['proj-1'],
    assigneeId: '1',
    assignee: mockUser,
    dueDate: new Date('2024-12-20'),
    tags: ['design', 'ui', 'homepage'],
    createdAt: new Date('2024-12-01'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-2',
    title: 'Set up CI/CD pipeline',
    description: 'Configure GitHub Actions for automated testing and deployment',
    status: 'todo',
    priority: 'urgent',
    projectIds: ['proj-2'],
    dueDate: new Date('2024-12-15'),
    tags: ['devops', 'infrastructure'],
    createdAt: new Date('2024-12-05'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-3',
    title: 'User authentication module',
    description: 'Implement JWT-based authentication system',
    status: 'in_review',
    priority: 'high',
    projectIds: ['proj-2'],
    assigneeId: '1',
    assignee: mockUser,
    tags: ['backend', 'security', 'auth'],
    createdAt: new Date('2024-12-03'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-4',
    title: 'Write API documentation',
    description: 'Document all REST API endpoints with examples',
    status: 'done',
    priority: 'medium',
    projectIds: ['proj-2', 'proj-4'],
    tags: ['documentation', 'api'],
    completedAt: new Date('2024-12-10'),
    createdAt: new Date('2024-12-01'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-5',
    title: 'Database schema review',
    description: 'Review and optimize database schema for performance',
    status: 'blocked',
    priority: 'low',
    projectIds: ['proj-4'],
    tags: ['database', 'optimization'],
    createdAt: new Date('2024-12-08'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-6',
    title: 'Create email templates',
    description: 'Design responsive email templates for marketing campaigns',
    status: 'todo',
    priority: 'medium',
    projectIds: ['proj-3'],
    dueDate: new Date('2024-12-25'),
    tags: ['marketing', 'email', 'design'],
    createdAt: new Date('2024-12-10'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-7',
    title: 'Performance testing',
    description: 'Run load tests and performance benchmarks',
    status: 'in_progress',
    priority: 'high',
    projectIds: ['proj-2', 'proj-4'],
    assigneeId: '1',
    assignee: mockUser,
    tags: ['testing', 'performance'],
    createdAt: new Date('2024-12-09'),
    updatedAt: new Date(),
    createdBy: '1'
  },
  {
    id: 'task-8',
    title: 'Mobile app wireframes',
    description: 'Create wireframes for all mobile app screens',
    status: 'done',
    priority: 'medium',
    projectIds: ['proj-2'],
    tags: ['design', 'mobile', 'wireframes'],
    completedAt: new Date('2024-12-08'),
    createdAt: new Date('2024-11-28'),
    updatedAt: new Date(),
    createdBy: '1'
  }
];

export const useMockDataStore = create<MockDataState>(() => ({
  mockWorkspace,
  mockProjects,
  mockTasks,
  mockUser
}));