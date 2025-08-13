import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { 
  Filter, 
  X, 
  Save,
  Calendar,
  User,
  Flag,
  CheckCircle
} from 'lucide-react';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
} from '@/components/ui/form';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import { 
  TaskFilterSchema
} from '@/types/schemas/task';
import type { 
  TaskFilterInput
} from '@/types/schemas/task';
import type { Project, User as UserType } from '@/types';
import { cn } from '@/lib/utils';

interface TaskFiltersProps {
  onFilterChange: (filters: TaskFilterInput) => void;
  projects: Project[];
  users: UserType[];
  savedFilters?: SavedFilter[];
  onSaveFilter?: (name: string, filters: TaskFilterInput) => void;
  onDeleteFilter?: (filterId: string) => void;
  className?: string;
}

interface SavedFilter {
  id: string;
  name: string;
  filters: TaskFilterInput;
}

const statusOptions = [
  { value: 'todo', label: 'To Do', icon: <CheckCircle className="h-4 w-4" /> },
  { value: 'in_progress', label: 'In Progress', icon: <CheckCircle className="h-4 w-4" /> },
  { value: 'in_review', label: 'In Review', icon: <CheckCircle className="h-4 w-4" /> },
  { value: 'done', label: 'Done', icon: <CheckCircle className="h-4 w-4" /> },
  { value: 'blocked', label: 'Blocked', icon: <CheckCircle className="h-4 w-4" /> },
];

const priorityOptions = [
  { value: 'low', label: 'Low', color: 'text-gray-400' },
  { value: 'medium', label: 'Medium', color: 'text-blue-500' },
  { value: 'high', label: 'High', color: 'text-orange-500' },
  { value: 'urgent', label: 'Urgent', color: 'text-red-500' },
];

export const TaskFilters: React.FC<TaskFiltersProps> = ({
  onFilterChange,
  projects,
  users,
  savedFilters = [],
  onSaveFilter,
  onDeleteFilter,
  className,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [filterName, setFilterName] = useState('');
  const [showSaveInput, setShowSaveInput] = useState(false);
  const [activeFilterCount, setActiveFilterCount] = useState(0);

  const form = useForm<TaskFilterInput>({
    resolver: zodResolver(TaskFilterSchema),
    defaultValues: {
      status: [],
      priority: [],
      assigneeId: [],
      projectId: [],
      tags: [],
      dueDateFrom: undefined,
      dueDateTo: undefined,
      search: '',
    },
  });

  const updateActiveFilterCount = (values: TaskFilterInput) => {
    let count = 0;
    if (values.status && values.status.length > 0) count++;
    if (values.priority && values.priority.length > 0) count++;
    if (values.assigneeId && values.assigneeId.length > 0) count++;
    if (values.projectId && values.projectId.length > 0) count++;
    if (values.tags && values.tags.length > 0) count++;
    if (values.dueDateFrom || values.dueDateTo) count++;
    if (values.search) count++;
    setActiveFilterCount(count);
  };

  const handleApplyFilters = (values: TaskFilterInput) => {
    onFilterChange(values);
    updateActiveFilterCount(values);
    setIsOpen(false);
  };

  const handleClearFilters = () => {
    form.reset();
    onFilterChange({});
    setActiveFilterCount(0);
    setIsOpen(false);
  };

  const handleSaveFilter = () => {
    if (filterName.trim() && onSaveFilter) {
      onSaveFilter(filterName, form.getValues());
      setFilterName('');
      setShowSaveInput(false);
    }
  };

  const handleLoadFilter = (filter: SavedFilter) => {
    form.reset(filter.filters);
    updateActiveFilterCount(filter.filters);
  };

  const handleToggleStatus = (status: string) => {
    const currentStatuses = form.getValues('status') || [];
    const newStatuses = currentStatuses.includes(status as any)
      ? currentStatuses.filter(s => s !== status)
      : [...currentStatuses, status as any];
    form.setValue('status', newStatuses);
  };

  const handleTogglePriority = (priority: string) => {
    const currentPriorities = form.getValues('priority') || [];
    const newPriorities = currentPriorities.includes(priority as any)
      ? currentPriorities.filter(p => p !== priority)
      : [...currentPriorities, priority as any];
    form.setValue('priority', newPriorities);
  };

  const handleToggleUser = (userId: string) => {
    const currentUsers = form.getValues('assigneeId') || [];
    const newUsers = currentUsers.includes(userId)
      ? currentUsers.filter(u => u !== userId)
      : [...currentUsers, userId];
    form.setValue('assigneeId', newUsers);
  };

  const handleToggleProject = (projectId: string) => {
    const currentProjects = form.getValues('projectId') || [];
    const newProjects = currentProjects.includes(projectId)
      ? currentProjects.filter(p => p !== projectId)
      : [...currentProjects, projectId];
    form.setValue('projectId', newProjects);
  };

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" className={cn('gap-2', className)}>
          <Filter className="h-4 w-4" />
          Filters
          {activeFilterCount > 0 && (
            <Badge variant="secondary" className="ml-1">
              {activeFilterCount}
            </Badge>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-96 p-0" align="start">
        <div className="flex items-center justify-between p-4 border-b">
          <h3 className="font-semibold">Filter Tasks</h3>
          <div className="flex items-center gap-2">
            {activeFilterCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleClearFilters}
              >
                Clear all
              </Button>
            )}
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setIsOpen(false)}
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>

        <ScrollArea className="h-[500px]">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleApplyFilters)} className="p-4 space-y-4">
              {/* Search */}
              <FormField
                control={form.control}
                name="search"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Search</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Search tasks..."
                        {...field}
                      />
                    </FormControl>
                  </FormItem>
                )}
              />

              <Separator />

              {/* Status */}
              <FormField
                control={form.control}
                name="status"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4" />
                      Status
                    </FormLabel>
                    <div className="space-y-2">
                      {statusOptions.map((option) => (
                        <div key={option.value} className="flex items-center space-x-2">
                          <Checkbox
                            checked={field.value?.includes(option.value as any)}
                            onCheckedChange={() => handleToggleStatus(option.value)}
                          />
                          <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                            {option.label}
                          </label>
                        </div>
                      ))}
                    </div>
                  </FormItem>
                )}
              />

              <Separator />

              {/* Priority */}
              <FormField
                control={form.control}
                name="priority"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-2">
                      <Flag className="h-4 w-4" />
                      Priority
                    </FormLabel>
                    <div className="space-y-2">
                      {priorityOptions.map((option) => (
                        <div key={option.value} className="flex items-center space-x-2">
                          <Checkbox
                            checked={field.value?.includes(option.value as any)}
                            onCheckedChange={() => handleTogglePriority(option.value)}
                          />
                          <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                            <span className={option.color}>{option.label}</span>
                          </label>
                        </div>
                      ))}
                    </div>
                  </FormItem>
                )}
              />

              <Separator />

              {/* Assignee */}
              <FormField
                control={form.control}
                name="assigneeId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-2">
                      <User className="h-4 w-4" />
                      Assignee
                    </FormLabel>
                    <div className="space-y-2 max-h-32 overflow-y-auto">
                      {users.map((user) => (
                        <div key={user.id} className="flex items-center space-x-2">
                          <Checkbox
                            checked={field.value?.includes(user.id)}
                            onCheckedChange={() => handleToggleUser(user.id)}
                          />
                          <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                            {user.name}
                          </label>
                        </div>
                      ))}
                    </div>
                  </FormItem>
                )}
              />

              <Separator />

              {/* Projects */}
              <FormField
                control={form.control}
                name="projectId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Projects</FormLabel>
                    <div className="space-y-2 max-h-32 overflow-y-auto">
                      {projects.map((project) => (
                        <div key={project.id} className="flex items-center space-x-2">
                          <Checkbox
                            checked={field.value?.includes(project.id)}
                            onCheckedChange={() => handleToggleProject(project.id)}
                          />
                          <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                            {project.name}
                          </label>
                        </div>
                      ))}
                    </div>
                  </FormItem>
                )}
              />

              <Separator />

              {/* Due Date Range */}
              <div className="space-y-2">
                <FormLabel className="flex items-center gap-2">
                  <Calendar className="h-4 w-4" />
                  Due Date Range
                </FormLabel>
                <div className="grid grid-cols-2 gap-2">
                  <FormField
                    control={form.control}
                    name="dueDateFrom"
                    render={({ field }) => (
                      <FormItem>
                        <FormControl>
                          <Input
                            type="date"
                            placeholder="From"
                            {...field}
                            value={field.value || ''}
                          />
                        </FormControl>
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="dueDateTo"
                    render={({ field }) => (
                      <FormItem>
                        <FormControl>
                          <Input
                            type="date"
                            placeholder="To"
                            {...field}
                            value={field.value || ''}
                          />
                        </FormControl>
                      </FormItem>
                    )}
                  />
                </div>
              </div>

              {/* Saved Filters */}
              {savedFilters.length > 0 && (
                <>
                  <Separator />
                  <div>
                    <FormLabel>Saved Filters</FormLabel>
                    <div className="space-y-2 mt-2">
                      {savedFilters.map((filter) => (
                        <div key={filter.id} className="flex items-center justify-between">
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={() => handleLoadFilter(filter)}
                            className="justify-start"
                          >
                            {filter.name}
                          </Button>
                          {onDeleteFilter && (
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => onDeleteFilter(filter.id)}
                            >
                              <X className="h-3 w-3" />
                            </Button>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}

              {/* Save Filter */}
              {onSaveFilter && (
                <>
                  <Separator />
                  {showSaveInput ? (
                    <div className="flex gap-2">
                      <Input
                        placeholder="Filter name..."
                        value={filterName}
                        onChange={(e) => setFilterName(e.target.value)}
                      />
                      <Button
                        type="button"
                        size="sm"
                        onClick={handleSaveFilter}
                        disabled={!filterName.trim()}
                      >
                        Save
                      </Button>
                      <Button
                        type="button"
                        size="sm"
                        variant="ghost"
                        onClick={() => {
                          setShowSaveInput(false);
                          setFilterName('');
                        }}
                      >
                        Cancel
                      </Button>
                    </div>
                  ) : (
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => setShowSaveInput(true)}
                      className="w-full"
                    >
                      <Save className="h-4 w-4 mr-2" />
                      Save current filter
                    </Button>
                  )}
                </>
              )}

              <div className="flex gap-2 pt-2">
                <Button type="submit" className="flex-1">
                  Apply Filters
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleClearFilters}
                >
                  Clear
                </Button>
              </div>
            </form>
          </Form>
        </ScrollArea>
      </PopoverContent>
    </Popover>
  );
};