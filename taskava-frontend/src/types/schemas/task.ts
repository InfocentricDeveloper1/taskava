import { z } from 'zod';

// Task status and priority enums
export const TaskStatusEnum = z.enum(['todo', 'in_progress', 'in_review', 'done', 'blocked']);
export const TaskPriorityEnum = z.enum(['low', 'medium', 'high', 'urgent']);

// Schema for creating a new task
export const CreateTaskSchema = z.object({
  title: z.string().min(1, 'Title is required').max(255, 'Title must be less than 255 characters'),
  description: z.string().optional(),
  status: TaskStatusEnum.default('todo'),
  priority: TaskPriorityEnum.default('medium'),
  projectIds: z.array(z.string()).min(1, 'At least one project is required'),
  assigneeId: z.string().optional(),
  dueDate: z.string().optional(), // ISO date string
  tags: z.array(z.string()).default([]),
  customFields: z.record(z.any()).optional(),
});

// Schema for updating an existing task
export const UpdateTaskSchema = z.object({
  title: z.string().min(1, 'Title is required').max(255, 'Title must be less than 255 characters').optional(),
  description: z.string().optional(),
  status: TaskStatusEnum.optional(),
  priority: TaskPriorityEnum.optional(),
  projectIds: z.array(z.string()).min(1, 'At least one project is required').optional(),
  assigneeId: z.string().nullable().optional(),
  dueDate: z.string().nullable().optional(), // ISO date string
  tags: z.array(z.string()).optional(),
  customFields: z.record(z.any()).optional(),
});

// Schema for filtering tasks
export const TaskFilterSchema = z.object({
  status: z.array(TaskStatusEnum).optional(),
  priority: z.array(TaskPriorityEnum).optional(),
  assigneeId: z.array(z.string()).optional(),
  projectId: z.array(z.string()).optional(),
  tags: z.array(z.string()).optional(),
  dueDateFrom: z.string().optional(), // ISO date string
  dueDateTo: z.string().optional(), // ISO date string
  search: z.string().optional(),
});

// Type exports
export type CreateTaskInput = z.infer<typeof CreateTaskSchema>;
export type UpdateTaskInput = z.infer<typeof UpdateTaskSchema>;
export type TaskFilterInput = z.infer<typeof TaskFilterSchema>;
export type TaskStatus = z.infer<typeof TaskStatusEnum>;
export type TaskPriority = z.infer<typeof TaskPriorityEnum>;