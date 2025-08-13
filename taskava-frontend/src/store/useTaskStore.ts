import { create } from 'zustand';
import type { Task, TaskSection } from '@/types';
import type { TaskStatus } from '@/types/schemas/task';
import { taskApi, projectApi } from '@/services/api';
import { mockSections, mockTasks } from '@/mocks/mockData';

interface TaskStore {
  sections: TaskSection[];
  tasks: Map<string, Task[]>; // Map of sectionId -> tasks
  isLoading: boolean;
  error: string | null;
  selectedProjectId: string | null;
  
  // Actions
  setSelectedProject: (projectId: string) => void;
  fetchSections: (projectId: string) => Promise<void>;
  fetchTasksForSection: (sectionId: string) => Promise<void>;
  moveTask: (taskId: string, fromSectionId: string, toSectionId: string, newIndex: number) => Promise<void>;
  updateTaskStatus: (taskId: string, status: TaskStatus, sectionId: string) => Promise<void>;
  updateTaskOrder: (sectionId: string, taskId: string, newIndex: number) => Promise<void>;
  createTask: (task: Partial<Task>, sectionId: string) => Promise<void>;
  updateTask: (taskId: string, updates: Partial<Task>) => Promise<void>;
  deleteTask: (taskId: string, sectionId: string) => Promise<void>;
  reset: () => void;
}

export const useTaskStore = create<TaskStore>((set, get) => ({
  sections: [],
  tasks: new Map(),
  isLoading: false,
  error: null,
  selectedProjectId: null,
  
  setSelectedProject: (projectId: string) => {
    set({ selectedProjectId: projectId });
  },
  
  fetchSections: async (projectId: string) => {
    set({ isLoading: true, error: null });
    
    // Check if we should use mock data (development mode or API unavailable)
    // TODO: Set to false once backend is properly configured
    const useMockData = false; // Backend is now fixed!
    
    try {
      let sections: TaskSection[] = [];
      
      if (useMockData && mockSections[projectId]) {
        // Use mock data
        sections = mockSections[projectId];
        
        // Initialize task arrays with mock data
        const tasksMap = new Map<string, Task[]>();
        sections.forEach(section => {
          tasksMap.set(section.id, mockTasks[section.id] || []);
        });
        
        set({ 
          sections, 
          tasks: tasksMap,
          selectedProjectId: projectId,
          isLoading: false 
        });
      } else {
        // Use real API
        const response = await projectApi.getSections(projectId);
        sections = response.data || [];
        
        // Initialize empty task arrays for each section
        const tasksMap = new Map<string, Task[]>();
        sections.forEach(section => {
          tasksMap.set(section.id, []);
        });
        
        set({ 
          sections, 
          tasks: tasksMap,
          selectedProjectId: projectId,
          isLoading: false 
        });
        
        // Fetch tasks for each section
        await Promise.all(
          sections.map(section => get().fetchTasksForSection(section.id))
        );
      }
    } catch (error: any) {
      // Fallback to mock data on error
      console.warn('API error, falling back to mock data:', error);
      
      if (mockSections[projectId]) {
        const sections = mockSections[projectId];
        const tasksMap = new Map<string, Task[]>();
        sections.forEach(section => {
          tasksMap.set(section.id, mockTasks[section.id] || []);
        });
        
        set({ 
          sections, 
          tasks: tasksMap,
          selectedProjectId: projectId,
          isLoading: false,
          error: null // Clear error since we have mock data
        });
      } else {
        set({ 
          error: error.response?.data?.message || 'Failed to fetch sections',
          isLoading: false 
        });
      }
    }
  },
  
  fetchTasksForSection: async (sectionId: string) => {
    try {
      const response = await taskApi.getAll({
        sectionId,
        page: 0,
        size: 100 // Get all tasks for the section
      });
      
      const tasks = response.content || [];
      
      set(state => ({
        tasks: new Map(state.tasks).set(sectionId, tasks)
      }));
    } catch (error: any) {
      console.error(`Failed to fetch tasks for section ${sectionId}:`, error);
    }
  },
  
  moveTask: async (taskId: string, fromSectionId: string, toSectionId: string, newIndex: number) => {
    const state = get();
    const fromTasks = [...(state.tasks.get(fromSectionId) || [])];
    const toTasks = fromSectionId === toSectionId 
      ? fromTasks 
      : [...(state.tasks.get(toSectionId) || [])];
    
    // Find the task
    const taskIndex = fromTasks.findIndex(t => t.id === taskId);
    if (taskIndex === -1) return;
    
    const task = fromTasks[taskIndex];
    
    // Optimistic update
    if (fromSectionId === toSectionId) {
      // Reorder within the same section
      fromTasks.splice(taskIndex, 1);
      fromTasks.splice(newIndex, 0, task);
      
      set(state => ({
        tasks: new Map(state.tasks).set(fromSectionId, fromTasks)
      }));
    } else {
      // Move between sections
      fromTasks.splice(taskIndex, 1);
      toTasks.splice(newIndex, 0, task);
      
      set(state => {
        const newTasks = new Map(state.tasks);
        newTasks.set(fromSectionId, fromTasks);
        newTasks.set(toSectionId, toTasks);
        return { tasks: newTasks };
      });
    }
    
    try {
      // Update on backend
      await taskApi.update(taskId, {
        sectionId: toSectionId,
        order: newIndex
      });
      
      // Update the task's status if moving to a different section
      if (fromSectionId !== toSectionId) {
        const targetSection = state.sections.find(s => s.id === toSectionId);
        if (targetSection) {
          // Map section name to status (you might need to adjust this logic)
          const statusMap: Record<string, TaskStatus> = {
            'To Do': 'todo',
            'In Progress': 'in_progress',
            'In Review': 'in_review',
            'Done': 'done',
            'Blocked': 'blocked'
          };
          
          const newStatus = statusMap[targetSection.name] || 'todo';
          await taskApi.updateStatus(taskId, newStatus);
        }
      }
    } catch (error: any) {
      // Revert on error
      await get().fetchTasksForSection(fromSectionId);
      if (fromSectionId !== toSectionId) {
        await get().fetchTasksForSection(toSectionId);
      }
      
      set({ 
        error: error.response?.data?.message || 'Failed to move task' 
      });
    }
  },
  
  updateTaskStatus: async (taskId: string, status: TaskStatus, sectionId: string) => {
    try {
      await taskApi.updateStatus(taskId, status);
      
      // Update local state
      set(state => {
        const sectionTasks = state.tasks.get(sectionId) || [];
        const updatedTasks = sectionTasks.map(task => 
          task.id === taskId ? { ...task, status } : task
        );
        
        return {
          tasks: new Map(state.tasks).set(sectionId, updatedTasks)
        };
      });
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Failed to update task status' 
      });
    }
  },
  
  updateTaskOrder: async (sectionId: string, taskId: string, newIndex: number) => {
    const tasks = [...(get().tasks.get(sectionId) || [])];
    const currentIndex = tasks.findIndex(t => t.id === taskId);
    
    if (currentIndex === -1) return;
    
    const [task] = tasks.splice(currentIndex, 1);
    tasks.splice(newIndex, 0, task);
    
    // Optimistic update
    set(state => ({
      tasks: new Map(state.tasks).set(sectionId, tasks)
    }));
    
    try {
      // Update order on backend
      await taskApi.update(taskId, { order: newIndex });
    } catch (error: any) {
      // Revert on error
      await get().fetchTasksForSection(sectionId);
      set({ 
        error: error.response?.data?.message || 'Failed to update task order' 
      });
    }
  },
  
  createTask: async (taskData: Partial<Task>, sectionId: string) => {
    try {
      const response = await taskApi.create({
        ...taskData,
        sectionId,
        projectIds: get().selectedProjectId ? [get().selectedProjectId!] : []
      });
      
      const newTask = response.data;
      
      set(state => {
        const sectionTasks = state.tasks.get(sectionId) || [];
        return {
          tasks: new Map(state.tasks).set(sectionId, [...sectionTasks, newTask])
        };
      });
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Failed to create task' 
      });
    }
  },
  
  updateTask: async (taskId: string, updates: Partial<Task>) => {
    try {
      const response = await taskApi.update(taskId, updates);
      const updatedTask = response.data;
      
      // Update task in all sections it might appear
      set(state => {
        const newTasks = new Map(state.tasks);
        
        newTasks.forEach((sectionTasks, sectionId) => {
          const taskIndex = sectionTasks.findIndex(t => t.id === taskId);
          if (taskIndex !== -1) {
            const updatedSectionTasks = [...sectionTasks];
            updatedSectionTasks[taskIndex] = updatedTask;
            newTasks.set(sectionId, updatedSectionTasks);
          }
        });
        
        return { tasks: newTasks };
      });
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Failed to update task' 
      });
    }
  },
  
  deleteTask: async (taskId: string, sectionId: string) => {
    // Optimistic update
    set(state => {
      const sectionTasks = state.tasks.get(sectionId) || [];
      const filteredTasks = sectionTasks.filter(t => t.id !== taskId);
      
      return {
        tasks: new Map(state.tasks).set(sectionId, filteredTasks)
      };
    });
    
    try {
      await taskApi.delete(taskId);
    } catch (error: any) {
      // Revert on error
      await get().fetchTasksForSection(sectionId);
      set({ 
        error: error.response?.data?.message || 'Failed to delete task' 
      });
    }
  },
  
  reset: () => {
    set({
      sections: [],
      tasks: new Map(),
      isLoading: false,
      error: null,
      selectedProjectId: null
    });
  }
}));