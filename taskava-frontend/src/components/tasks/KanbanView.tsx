import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Plus, Settings, Filter, ChevronDown } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { KanbanBoard } from './KanbanBoard';
import { projectApi } from '@/services/api';
import { mockProjects } from '@/mocks/mockData';

export const KanbanView: React.FC = () => {
  const [selectedProjectId, setSelectedProjectId] = useState<string>('');
  
  // Fetch projects
  const { data: projectsData, isLoading: isLoadingProjects } = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
      try {
        const response = await projectApi.getAll();
        return response.content || [];
      } catch (error) {
        console.warn('Failed to fetch projects, using mock data:', error);
        return mockProjects;
      }
    },
  });

  const projects = projectsData || [];

  // Auto-select first project if none selected
  useEffect(() => {
    if (projects.length > 0 && !selectedProjectId) {
      setSelectedProjectId(projects[0].id);
    }
  }, [projects, selectedProjectId]);

  const selectedProject = projects.find(p => p.id === selectedProjectId);

  if (isLoadingProjects) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <p className="text-muted-foreground">Loading projects...</p>
        </div>
      </div>
    );
  }

  if (projects.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-96 space-y-4">
        <div className="text-center">
          <h3 className="text-lg font-semibold mb-2">No Projects Found</h3>
          <p className="text-muted-foreground mb-4">
            Create a project to start managing tasks
          </p>
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            Create First Project
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <h2 className="text-2xl font-bold">Task Board</h2>
          
          {/* Project Selector */}
          <Select
            value={selectedProjectId}
            onValueChange={setSelectedProjectId}
          >
            <SelectTrigger className="w-[250px]">
              <SelectValue placeholder="Select a project">
                {selectedProject && (
                  <div className="flex items-center gap-2">
                    <div 
                      className="w-3 h-3 rounded-full" 
                      style={{ backgroundColor: selectedProject.color || '#6B7280' }}
                    />
                    <span>{selectedProject.name}</span>
                  </div>
                )}
              </SelectValue>
            </SelectTrigger>
            <SelectContent>
              {projects.map((project) => (
                <SelectItem key={project.id} value={project.id}>
                  <div className="flex items-center gap-2">
                    <div 
                      className="w-3 h-3 rounded-full" 
                      style={{ backgroundColor: project.color || '#6B7280' }}
                    />
                    <span>{project.name}</span>
                    {project.taskCount !== undefined && (
                      <span className="text-xs text-muted-foreground ml-auto">
                        {project.taskCount} tasks
                      </span>
                    )}
                  </div>
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex items-center gap-2">
          {/* View Options */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm">
                <Filter className="h-4 w-4 mr-2" />
                Filter
                <ChevronDown className="h-3 w-3 ml-1" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>Filter Tasks</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem>My Tasks</DropdownMenuItem>
              <DropdownMenuItem>Unassigned</DropdownMenuItem>
              <DropdownMenuItem>Due This Week</DropdownMenuItem>
              <DropdownMenuItem>High Priority</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem>Clear Filters</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm">
                <Settings className="h-4 w-4 mr-2" />
                View
                <ChevronDown className="h-3 w-3 ml-1" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>View Options</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem>Show Subtasks</DropdownMenuItem>
              <DropdownMenuItem>Show Completed</DropdownMenuItem>
              <DropdownMenuItem>Compact View</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem>Group by Assignee</DropdownMenuItem>
              <DropdownMenuItem>Group by Priority</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          <Button size="sm">
            <Plus className="h-4 w-4 mr-2" />
            New Task
          </Button>
        </div>
      </div>

      {/* Project Info Bar */}
      {selectedProject && (
        <div className="flex items-center justify-between px-4 py-2 bg-muted/50 rounded-lg">
          <div className="flex items-center gap-4 text-sm">
            <span className="text-muted-foreground">
              {selectedProject.description || 'No description'}
            </span>
          </div>
          <div className="flex items-center gap-4 text-sm text-muted-foreground">
            {selectedProject.taskCount !== undefined && (
              <>
                <span>{selectedProject.taskCount} total tasks</span>
                {selectedProject.completedTaskCount !== undefined && (
                  <span>{selectedProject.completedTaskCount} completed</span>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {/* Kanban Board */}
      {selectedProjectId && (
        <div className="flex-1 overflow-hidden">
          <KanbanBoard 
            projectId={selectedProjectId} 
            className="h-full"
          />
        </div>
      )}
    </div>
  );
};