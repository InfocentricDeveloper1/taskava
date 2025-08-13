import type { Project, TaskSection, Task, User, TaskStatus, TaskPriority } from '@/types';

// Mock users
export const mockUsers: User[] = [
  {
    id: 'user-1',
    email: 'john.doe@taskava.com',
    name: 'John Doe',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=john',
    role: 'admin',
  },
  {
    id: 'user-2',
    email: 'jane.smith@taskava.com',
    name: 'Jane Smith',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=jane',
    role: 'user',
  },
  {
    id: 'user-3',
    email: 'alex.kim@taskava.com',
    name: 'Alex Kim',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=alex',
    role: 'user',
  },
  {
    id: 'user-4',
    email: 'sarah.miller@taskava.com',
    name: 'Sarah Miller',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=sarah',
    role: 'user',
  },
];

// Mock projects
export const mockProjects: Project[] = [
  {
    id: 'proj-1',
    workspaceId: 'ws-1',
    name: 'Website Redesign',
    description: 'Complete overhaul of the company website with modern design',
    color: '#3B82F6',
    status: 'active',
    ownerId: 'user-1',
    teamIds: ['team-1'],
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date('2024-01-15'),
    taskCount: 24,
    completedTaskCount: 8,
  },
  {
    id: 'proj-2',
    workspaceId: 'ws-1',
    name: 'Mobile App Development',
    description: 'Native iOS and Android app for our platform',
    color: '#10B981',
    status: 'active',
    ownerId: 'user-2',
    teamIds: ['team-2'],
    createdAt: new Date('2024-01-20'),
    updatedAt: new Date('2024-01-20'),
    taskCount: 32,
    completedTaskCount: 12,
  },
  {
    id: 'proj-3',
    workspaceId: 'ws-1',
    name: 'Marketing Campaign Q1',
    description: 'Q1 2024 marketing initiatives and content calendar',
    color: '#F59E0B',
    status: 'active',
    ownerId: 'user-3',
    teamIds: ['team-3'],
    createdAt: new Date('2024-01-10'),
    updatedAt: new Date('2024-01-10'),
    taskCount: 18,
    completedTaskCount: 15,
  },
];

// Mock sections for each project
export const mockSections: Record<string, TaskSection[]> = {
  'proj-1': [
    { id: 'sec-1-1', projectId: 'proj-1', name: 'Backlog', order: 0 },
    { id: 'sec-1-2', projectId: 'proj-1', name: 'To Do', order: 1 },
    { id: 'sec-1-3', projectId: 'proj-1', name: 'In Progress', order: 2 },
    { id: 'sec-1-4', projectId: 'proj-1', name: 'Review', order: 3 },
    { id: 'sec-1-5', projectId: 'proj-1', name: 'Done', order: 4 },
  ],
  'proj-2': [
    { id: 'sec-2-1', projectId: 'proj-2', name: 'Planning', order: 0 },
    { id: 'sec-2-2', projectId: 'proj-2', name: 'Development', order: 1 },
    { id: 'sec-2-3', projectId: 'proj-2', name: 'Testing', order: 2 },
    { id: 'sec-2-4', projectId: 'proj-2', name: 'Deployment', order: 3 },
  ],
  'proj-3': [
    { id: 'sec-3-1', projectId: 'proj-3', name: 'Ideas', order: 0 },
    { id: 'sec-3-2', projectId: 'proj-3', name: 'In Progress', order: 1 },
    { id: 'sec-3-3', projectId: 'proj-3', name: 'Scheduled', order: 2 },
    { id: 'sec-3-4', projectId: 'proj-3', name: 'Published', order: 3 },
  ],
};

// Helper function to generate mock tasks
const generateTask = (
  id: string,
  title: string,
  _sectionId: string,
  projectId: string,
  status: TaskStatus = 'todo',
  priority: TaskPriority = 'medium',
  assignee?: User,
  dueDate?: Date,
  description?: string,
  tags: string[] = []
): Task => ({
  id,
  title,
  description,
  status,
  priority,
  projectIds: [projectId],
  assigneeId: assignee?.id,
  assignee,
  dueDate,
  tags,
  createdAt: new Date(),
  updatedAt: new Date(),
  createdBy: 'user-1',
  comments: [],
  attachments: [],
});

// Mock tasks for Website Redesign project
export const mockTasks: Record<string, Task[]> = {
  // Website Redesign - Backlog
  'sec-1-1': [
    generateTask('task-1', 'Research competitor websites', 'sec-1-1', 'proj-1', 'todo', 'low', mockUsers[2], undefined, 'Analyze top 10 competitor sites for design trends', ['research', 'design']),
    generateTask('task-2', 'Create mood board', 'sec-1-1', 'proj-1', 'todo', 'medium', mockUsers[1], undefined, 'Compile visual inspiration and color palettes', ['design', 'creative']),
    generateTask('task-3', 'Define site architecture', 'sec-1-1', 'proj-1', 'todo', 'high', mockUsers[0], new Date('2024-02-15'), 'Map out all pages and navigation structure', ['planning', 'ux']),
  ],
  
  // Website Redesign - To Do
  'sec-1-2': [
    generateTask('task-4', 'Design homepage mockup', 'sec-1-2', 'proj-1', 'todo', 'high', mockUsers[1], new Date('2024-02-10'), 'Create high-fidelity mockup for homepage', ['design', 'urgent']),
    generateTask('task-5', 'Write homepage copy', 'sec-1-2', 'proj-1', 'todo', 'medium', mockUsers[3], new Date('2024-02-12'), 'Draft compelling copy for all homepage sections', ['content', 'marketing']),
    generateTask('task-6', 'Select hero images', 'sec-1-2', 'proj-1', 'todo', 'medium', mockUsers[2], undefined, 'Choose and optimize hero section images', ['design', 'assets']),
  ],
  
  // Website Redesign - In Progress
  'sec-1-3': [
    generateTask('task-7', 'Implement responsive navigation', 'sec-1-3', 'proj-1', 'in_progress', 'high', mockUsers[0], new Date('2024-02-08'), 'Build mobile-responsive navigation menu', ['development', 'frontend']),
    generateTask('task-8', 'Set up CMS integration', 'sec-1-3', 'proj-1', 'in_progress', 'urgent', mockUsers[2], new Date('2024-02-07'), 'Connect Contentful CMS to website', ['backend', 'integration']),
  ],
  
  // Website Redesign - Review
  'sec-1-4': [
    generateTask('task-9', 'Review contact form functionality', 'sec-1-4', 'proj-1', 'in_review', 'medium', mockUsers[1], undefined, 'Test all form validations and email notifications', ['testing', 'qa']),
    generateTask('task-10', 'Accessibility audit', 'sec-1-4', 'proj-1', 'in_review', 'high', mockUsers[3], new Date('2024-02-09'), 'Ensure WCAG 2.1 AA compliance', ['accessibility', 'qa']),
  ],
  
  // Website Redesign - Done
  'sec-1-5': [
    generateTask('task-11', 'Setup project repository', 'sec-1-5', 'proj-1', 'done', 'low', mockUsers[0], undefined, 'Initialize Git repo and CI/CD pipeline', ['setup', 'devops']),
    generateTask('task-12', 'Configure development environment', 'sec-1-5', 'proj-1', 'done', 'medium', mockUsers[2], undefined, 'Set up local dev environment for team', ['setup', 'development']),
  ],
  
  // Mobile App - Planning
  'sec-2-1': [
    generateTask('task-13', 'Define user personas', 'sec-2-1', 'proj-2', 'todo', 'high', mockUsers[1], new Date('2024-02-11'), 'Create detailed user personas for app', ['research', 'ux']),
    generateTask('task-14', 'Feature prioritization', 'sec-2-1', 'proj-2', 'todo', 'urgent', mockUsers[0], new Date('2024-02-10'), 'Prioritize features for MVP release', ['planning', 'product']),
  ],
  
  // Mobile App - Development
  'sec-2-2': [
    generateTask('task-15', 'Implement authentication flow', 'sec-2-2', 'proj-2', 'in_progress', 'high', mockUsers[2], new Date('2024-02-13'), 'Build login/signup screens with OAuth', ['development', 'security']),
    generateTask('task-16', 'Create onboarding screens', 'sec-2-2', 'proj-2', 'in_progress', 'medium', mockUsers[3], undefined, 'Design and implement app onboarding', ['development', 'ux']),
    generateTask('task-17', 'Build push notifications', 'sec-2-2', 'proj-2', 'todo', 'medium', mockUsers[0], new Date('2024-02-15'), 'Integrate FCM for push notifications', ['development', 'backend']),
  ],
  
  // Mobile App - Testing
  'sec-2-3': [
    generateTask('task-18', 'Unit test authentication', 'sec-2-3', 'proj-2', 'todo', 'medium', mockUsers[2], undefined, 'Write comprehensive unit tests for auth module', ['testing', 'qa']),
  ],
  
  // Mobile App - Deployment
  'sec-2-4': [],
  
  // Marketing Campaign - Ideas
  'sec-3-1': [
    generateTask('task-19', 'Brainstorm social media campaign', 'sec-3-1', 'proj-3', 'todo', 'low', mockUsers[3], undefined, 'Generate ideas for Q1 social campaign', ['marketing', 'creative']),
    generateTask('task-20', 'Research influencer partnerships', 'sec-3-1', 'proj-3', 'todo', 'medium', mockUsers[1], undefined, 'Identify potential influencer collaborations', ['marketing', 'partnerships']),
  ],
  
  // Marketing Campaign - In Progress
  'sec-3-2': [
    generateTask('task-21', 'Write blog post series', 'sec-3-2', 'proj-3', 'in_progress', 'high', mockUsers[3], new Date('2024-02-14'), 'Create 5-part blog series on industry trends', ['content', 'blog']),
    generateTask('task-22', 'Design email newsletter', 'sec-3-2', 'proj-3', 'in_progress', 'medium', mockUsers[1], new Date('2024-02-12'), 'Create responsive email template', ['design', 'email']),
  ],
  
  // Marketing Campaign - Scheduled
  'sec-3-3': [
    generateTask('task-23', 'Valentine\'s Day promotion', 'sec-3-3', 'proj-3', 'todo', 'urgent', mockUsers[0], new Date('2024-02-14'), 'Launch special Valentine\'s Day offer', ['campaign', 'promotion']),
  ],
  
  // Marketing Campaign - Published
  'sec-3-4': [
    generateTask('task-24', 'January newsletter', 'sec-3-4', 'proj-3', 'done', 'medium', mockUsers[3], undefined, 'Monthly newsletter sent to subscribers', ['email', 'newsletter']),
    generateTask('task-25', 'New Year campaign', 'sec-3-4', 'proj-3', 'done', 'high', mockUsers[1], undefined, 'New Year resolution campaign completed', ['campaign', 'social']),
  ],
};