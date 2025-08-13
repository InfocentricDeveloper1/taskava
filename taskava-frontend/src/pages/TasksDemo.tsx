import React, { useState, useEffect } from 'react';
import { Plus, LayoutGrid, List } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  TaskCard,
  TaskList,
  TaskDialog,
  TaskDetailSheet,
  TaskFilters,
} from '@/components/tasks';
import type { Task, Project, User, TaskStatus } from '@/types';
import type { CreateTaskInput, TaskFilterInput } from '@/types/schemas/task';
import { useToast } from '@/hooks/use-toast';

export const TasksDemo: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<'table' | 'cards' | 'board'>('table');
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isDetailSheetOpen, setIsDetailSheetOpen] = useState(false);
  const { toast } = useToast();

  // Mock data for demo
  const mockUsers: User[] = [
    { id: '1', email: 'john@example.com', name: 'John Doe', role: 'admin' },
    { id: '2', email: 'jane@example.com', name: 'Jane Smith', role: 'user' },
    { id: '3', email: 'bob@example.com', name: 'Bob Johnson', role: 'user' },
  ];

  const mockProjects: Project[] = [
    {
      id: '1',
      workspaceId: 'ws1',
      name: 'Website Redesign',
      status: 'active',
      ownerId: '1',
      teamIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    },
    {
      id: '2',
      workspaceId: 'ws1',
      name: 'Mobile App',
      status: 'active',
      ownerId: '2',
      teamIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    },
  ];

  const mockTasks: Task[] = [
    {
      id: '1',
      title: 'Design homepage mockup',
      description: 'Create initial design concepts for the new homepage',
      status: 'in_progress',
      priority: 'high',
      projectIds: ['1'],
      assigneeId: '1',
      assignee: mockUsers[0],
      dueDate: new Date('2025-09-01'),
      tags: ['design', 'ui'],
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: '1',
      comments: [],
    },
    {
      id: '2',
      title: 'Implement authentication',
      description: 'Set up JWT authentication with refresh tokens',
      status: 'todo',
      priority: 'urgent',
      projectIds: ['2'],
      assigneeId: '2',
      assignee: mockUsers[1],
      dueDate: new Date('2025-08-20'),
      tags: ['backend', 'security'],
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: '1',
      comments: [],
    },
    {
      id: '3',
      title: 'Write API documentation',
      description: 'Document all REST endpoints with examples',
      status: 'in_review',
      priority: 'medium',
      projectIds: ['1', '2'],
      assigneeId: '3',
      assignee: mockUsers[2],
      dueDate: new Date('2025-08-25'),
      tags: ['documentation'],
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: '2',
      comments: [],
    },
    {
      id: '4',
      title: 'Performance optimization',
      status: 'done',
      priority: 'low',
      projectIds: ['1'],
      tags: ['performance'],
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: '3',
      comments: [],
    },
    {
      id: '5',
      title: 'Fix mobile responsive issues',
      description: 'Address layout problems on small screens',
      status: 'blocked',
      priority: 'high',
      projectIds: ['1'],
      assigneeId: '1',
      assignee: mockUsers[0],
      tags: ['bug', 'mobile'],
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: '2',
      comments: [],
    },
  ];

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      // In production, load from API
      // const [tasksRes, projectsRes] = await Promise.all([
      //   taskApi.getAll(),
      //   projectApi.getAll(),
      // ]);
      // setTasks(tasksRes.content);
      // setProjects(projectsRes.content);
      
      // For demo, use mock data
      setTasks(mockTasks);
      setProjects(mockProjects);
      setUsers(mockUsers);
    } catch (error) {
      console.error('Failed to load data:', error);
      toast({
        title: 'Error',
        description: 'Failed to load tasks',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async (data: CreateTaskInput) => {
    try {
      // In production: const response = await taskApi.create(data);
      const newTask: Task = {
        id: String(Date.now()),
        title: data.title,
        description: data.description,
        status: data.status,
        priority: data.priority,
        projectIds: data.projectIds,
        assigneeId: data.assigneeId,
        assignee: users.find(u => u.id === data.assigneeId),
        dueDate: data.dueDate ? new Date(data.dueDate) : undefined,
        tags: data.tags,
        customFields: data.customFields,
        createdAt: new Date(),
        updatedAt: new Date(),
        createdBy: '1',
        comments: [],
      };
      setTasks([newTask, ...tasks]);
      toast({
        title: 'Success',
        description: 'Task created successfully',
      });
    } catch (error) {
      console.error('Failed to create task:', error);
      toast({
        title: 'Error',
        description: 'Failed to create task',
        variant: 'destructive',
      });
    }
  };

  const handleUpdateTask = async (taskId: string, updates: Partial<Task>) => {
    try {
      // In production: await taskApi.update(taskId, updates);
      setTasks(tasks.map(task => 
        task.id === taskId 
          ? { ...task, ...updates, updatedAt: new Date() }
          : task
      ));
      toast({
        title: 'Success',
        description: 'Task updated successfully',
      });
    } catch (error) {
      console.error('Failed to update task:', error);
      toast({
        title: 'Error',
        description: 'Failed to update task',
        variant: 'destructive',
      });
    }
  };

  const handleDeleteTask = async (taskId: string) => {
    try {
      // In production: await taskApi.delete(taskId);
      setTasks(tasks.filter(task => task.id !== taskId));
      toast({
        title: 'Success',
        description: 'Task deleted successfully',
      });
    } catch (error) {
      console.error('Failed to delete task:', error);
      toast({
        title: 'Error',
        description: 'Failed to delete task',
        variant: 'destructive',
      });
    }
  };

  const handleStatusChange = async (taskId: string, status: TaskStatus) => {
    await handleUpdateTask(taskId, { status });
  };

  const handleAddComment = async (taskId: string, content: string) => {
    const comment = {
      id: String(Date.now()),
      taskId,
      userId: '1',
      user: mockUsers[0],
      content,
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    
    setTasks(tasks.map(task => 
      task.id === taskId 
        ? { ...task, comments: [...(task.comments || []), comment] }
        : task
    ));
    
    toast({
      title: 'Success',
      description: 'Comment added successfully',
    });
  };

  const handleFilterChange = (newFilters: TaskFilterInput) => {
    // Apply filters to tasks
    let filteredTasks = [...mockTasks];
    
    if (newFilters.status && newFilters.status.length > 0) {
      filteredTasks = filteredTasks.filter(task => 
        newFilters.status!.includes(task.status)
      );
    }
    
    if (newFilters.priority && newFilters.priority.length > 0) {
      filteredTasks = filteredTasks.filter(task => 
        newFilters.priority!.includes(task.priority)
      );
    }
    
    if (newFilters.assigneeId && newFilters.assigneeId.length > 0) {
      filteredTasks = filteredTasks.filter(task => 
        task.assigneeId && newFilters.assigneeId!.includes(task.assigneeId)
      );
    }
    
    if (newFilters.search) {
      const searchLower = newFilters.search.toLowerCase();
      filteredTasks = filteredTasks.filter(task => 
        task.title.toLowerCase().includes(searchLower) ||
        task.description?.toLowerCase().includes(searchLower)
      );
    }
    
    setTasks(filteredTasks);
  };

  const handleBulkAction = (taskIds: string[], action: string) => {
    if (action === 'delete') {
      setTasks(tasks.filter(task => !taskIds.includes(task.id)));
      toast({
        title: 'Success',
        description: `${taskIds.length} tasks deleted`,
      });
    }
  };

  const boardColumns = [
    { id: 'todo', title: 'To Do', tasks: tasks.filter(t => t.status === 'todo') },
    { id: 'in_progress', title: 'In Progress', tasks: tasks.filter(t => t.status === 'in_progress') },
    { id: 'in_review', title: 'In Review', tasks: tasks.filter(t => t.status === 'in_review') },
    { id: 'done', title: 'Done', tasks: tasks.filter(t => t.status === 'done') },
    { id: 'blocked', title: 'Blocked', tasks: tasks.filter(t => t.status === 'blocked') },
  ];

  return (
    <div className="container mx-auto py-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Tasks</h1>
          <p className="text-muted-foreground">Manage and track your tasks</p>
        </div>
        <div className="flex items-center gap-2">
          <TaskFilters
            onFilterChange={handleFilterChange}
            projects={projects}
            users={users}
          />
          <Button onClick={() => setIsCreateDialogOpen(true)}>
            <Plus className="h-4 w-4 mr-2" />
            New Task
          </Button>
        </div>
      </div>

      <Tabs value={viewMode} onValueChange={(v) => setViewMode(v as any)}>
        <TabsList>
          <TabsTrigger value="table">
            <List className="h-4 w-4 mr-2" />
            Table
          </TabsTrigger>
          <TabsTrigger value="cards">
            <LayoutGrid className="h-4 w-4 mr-2" />
            Cards
          </TabsTrigger>
          <TabsTrigger value="board">
            <LayoutGrid className="h-4 w-4 mr-2" />
            Board
          </TabsTrigger>
        </TabsList>

        <TabsContent value="table" className="mt-6">
          <TaskList
            tasks={tasks}
            loading={loading}
            onTaskClick={(task) => {
              setSelectedTask(task);
              setIsDetailSheetOpen(true);
            }}
            onTaskEdit={(task) => {
              setSelectedTask(task);
              setIsEditDialogOpen(true);
            }}
            onTaskDelete={handleDeleteTask}
            onTaskStatusChange={handleStatusChange}
            onBulkAction={handleBulkAction}
            viewMode="table"
          />
        </TabsContent>

        <TabsContent value="cards" className="mt-6">
          <TaskList
            tasks={tasks}
            loading={loading}
            onTaskClick={(task) => {
              setSelectedTask(task);
              setIsDetailSheetOpen(true);
            }}
            onTaskEdit={(task) => {
              setSelectedTask(task);
              setIsEditDialogOpen(true);
            }}
            onTaskDelete={handleDeleteTask}
            onTaskStatusChange={handleStatusChange}
            onBulkAction={handleBulkAction}
            viewMode="cards"
          />
        </TabsContent>

        <TabsContent value="board" className="mt-6">
          <div className="flex gap-4 overflow-x-auto pb-4">
            {boardColumns.map((column) => (
              <div key={column.id} className="flex-shrink-0 w-80">
                <div className="bg-muted rounded-lg p-4">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-semibold">{column.title}</h3>
                    <span className="text-sm text-muted-foreground bg-muted px-2 py-1 rounded">{column.tasks.length}</span>
                  </div>
                  <div className="space-y-3">
                    {column.tasks.map((task) => (
                      <TaskCard
                        key={task.id}
                        task={task}
                        onEdit={(task) => {
                          setSelectedTask(task);
                          setIsEditDialogOpen(true);
                        }}
                        onDelete={handleDeleteTask}
                        onStatusChange={handleStatusChange}
                        onClick={(task) => {
                          setSelectedTask(task);
                          setIsDetailSheetOpen(true);
                        }}
                        isDraggable
                        isCompact
                      />
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </TabsContent>
      </Tabs>

      <TaskDialog
        open={isCreateDialogOpen}
        onOpenChange={setIsCreateDialogOpen}
        projects={projects}
        users={users}
        onSubmit={handleCreateTask}
        mode="create"
      />

      <TaskDialog
        open={isEditDialogOpen}
        onOpenChange={setIsEditDialogOpen}
        task={selectedTask}
        projects={projects}
        users={users}
        onSubmit={async (data) => {
          if (selectedTask) {
            await handleUpdateTask(selectedTask.id, data as Partial<Task>);
            setIsEditDialogOpen(false);
          }
        }}
        mode="edit"
      />

      <TaskDetailSheet
        open={isDetailSheetOpen}
        onOpenChange={setIsDetailSheetOpen}
        task={selectedTask}
        users={users}
        onUpdate={handleUpdateTask}
        onDelete={handleDeleteTask}
        onAddComment={handleAddComment}
      />
    </div>
  );
};