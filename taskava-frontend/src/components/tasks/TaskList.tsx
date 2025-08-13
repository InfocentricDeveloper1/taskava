import React, { useState } from 'react';
import { format } from 'date-fns';
import { 
  ArrowUpDown, 
  Calendar, 
  MoreHorizontal,
  Flag,
  FileText
} from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { Task, TaskStatus, TaskPriority } from '@/types';
import { TaskCard } from './TaskCard';
import { cn } from '@/lib/utils';

interface TaskListProps {
  tasks: Task[];
  loading?: boolean;
  onTaskClick?: (task: Task) => void;
  onTaskEdit?: (task: Task) => void;
  onTaskDelete?: (taskId: string) => void;
  onTaskStatusChange?: (taskId: string, status: TaskStatus) => void;
  onBulkAction?: (taskIds: string[], action: string) => void;
  viewMode?: 'table' | 'cards';
  className?: string;
}

type SortField = 'title' | 'status' | 'priority' | 'dueDate' | 'assignee';
type SortOrder = 'asc' | 'desc';

const statusOptions: { value: TaskStatus; label: string; color: string }[] = [
  { value: 'todo', label: 'To Do', color: 'bg-gray-100 text-gray-800' },
  { value: 'in_progress', label: 'In Progress', color: 'bg-blue-100 text-blue-800' },
  { value: 'in_review', label: 'In Review', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'done', label: 'Done', color: 'bg-green-100 text-green-800' },
  { value: 'blocked', label: 'Blocked', color: 'bg-red-100 text-red-800' },
];

const priorityConfig: Record<TaskPriority, { label: string; color: string }> = {
  low: { label: 'Low', color: 'text-gray-400' },
  medium: { label: 'Medium', color: 'text-blue-500' },
  high: { label: 'High', color: 'text-orange-500' },
  urgent: { label: 'Urgent', color: 'text-red-500' },
};

export const TaskList: React.FC<TaskListProps> = ({
  tasks,
  loading = false,
  onTaskClick,
  onTaskEdit,
  onTaskDelete,
  onTaskStatusChange,
  onBulkAction,
  viewMode = 'table',
  className,
}) => {
  const [selectedTasks, setSelectedTasks] = useState<Set<string>>(new Set());
  const [sortField, setSortField] = useState<SortField>('title');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedTasks(new Set(tasks.map(t => t.id)));
    } else {
      setSelectedTasks(new Set());
    }
  };

  const handleSelectTask = (taskId: string, checked: boolean) => {
    const newSelection = new Set(selectedTasks);
    if (checked) {
      newSelection.add(taskId);
    } else {
      newSelection.delete(taskId);
    }
    setSelectedTasks(newSelection);
  };

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('asc');
    }
  };

  const sortedTasks = [...tasks].sort((a, b) => {
    let aValue: any = a[sortField as keyof Task];
    let bValue: any = b[sortField as keyof Task];

    if (sortField === 'assignee') {
      aValue = a.assignee?.name || '';
      bValue = b.assignee?.name || '';
    }

    if (aValue === bValue) return 0;
    if (aValue === null || aValue === undefined) return 1;
    if (bValue === null || bValue === undefined) return -1;

    const comparison = aValue > bValue ? 1 : -1;
    return sortOrder === 'asc' ? comparison : -comparison;
  });

  const handleBulkAction = (action: string) => {
    if (selectedTasks.size > 0) {
      onBulkAction?.(Array.from(selectedTasks), action);
      setSelectedTasks(new Set());
    }
  };

  if (loading) {
    return (
      <div className={cn('flex items-center justify-center py-8', className)}>
        <div className="text-muted-foreground">Loading tasks...</div>
      </div>
    );
  }

  if (tasks.length === 0) {
    return (
      <div className={cn('flex flex-col items-center justify-center py-12', className)}>
        <FileText className="h-12 w-12 text-muted-foreground mb-4" />
        <h3 className="text-lg font-medium mb-2">No tasks found</h3>
        <p className="text-sm text-muted-foreground">
          Create your first task to get started
        </p>
      </div>
    );
  }

  // Mobile/Card view
  if (viewMode === 'cards') {
    return (
      <div className={cn('space-y-4', className)}>
        {selectedTasks.size > 0 && (
          <div className="flex items-center gap-2 p-2 bg-muted rounded-md">
            <span className="text-sm font-medium">
              {selectedTasks.size} selected
            </span>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleBulkAction('delete')}
            >
              Delete
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => setSelectedTasks(new Set())}
            >
              Clear
            </Button>
          </div>
        )}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {sortedTasks.map((task) => (
            <div key={task.id} className="relative">
              <div className="absolute top-2 left-2 z-10">
                <Checkbox
                  checked={selectedTasks.has(task.id)}
                  onCheckedChange={(checked) => 
                    handleSelectTask(task.id, checked as boolean)
                  }
                  onClick={(e) => e.stopPropagation()}
                />
              </div>
              <TaskCard
                task={task}
                onEdit={onTaskEdit}
                onDelete={onTaskDelete}
                onStatusChange={onTaskStatusChange}
                onClick={onTaskClick}
              />
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Desktop/Table view
  return (
    <div className={cn('space-y-4', className)}>
      {selectedTasks.size > 0 && (
        <div className="flex items-center gap-2 p-2 bg-muted rounded-md">
          <span className="text-sm font-medium">
            {selectedTasks.size} selected
          </span>
          <Button
            size="sm"
            variant="outline"
            onClick={() => handleBulkAction('delete')}
          >
            Delete
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={() => handleBulkAction('archive')}
          >
            Archive
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={() => setSelectedTasks(new Set())}
          >
            Clear
          </Button>
        </div>
      )}
      
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-12">
              <Checkbox
                checked={selectedTasks.size === tasks.length && tasks.length > 0}
                onCheckedChange={handleSelectAll}
              />
            </TableHead>
            <TableHead>
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 gap-1"
                onClick={() => handleSort('title')}
              >
                Title
                <ArrowUpDown className="h-4 w-4" />
              </Button>
            </TableHead>
            <TableHead>
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 gap-1"
                onClick={() => handleSort('status')}
              >
                Status
                <ArrowUpDown className="h-4 w-4" />
              </Button>
            </TableHead>
            <TableHead>
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 gap-1"
                onClick={() => handleSort('priority')}
              >
                Priority
                <ArrowUpDown className="h-4 w-4" />
              </Button>
            </TableHead>
            <TableHead>
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 gap-1"
                onClick={() => handleSort('assignee')}
              >
                Assignee
                <ArrowUpDown className="h-4 w-4" />
              </Button>
            </TableHead>
            <TableHead>
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 gap-1"
                onClick={() => handleSort('dueDate')}
              >
                Due Date
                <ArrowUpDown className="h-4 w-4" />
              </Button>
            </TableHead>
            <TableHead className="w-12"></TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {sortedTasks.map((task) => (
            <TableRow
              key={task.id}
              className="cursor-pointer"
              onClick={() => onTaskClick?.(task)}
            >
              <TableCell onClick={(e) => e.stopPropagation()}>
                <Checkbox
                  checked={selectedTasks.has(task.id)}
                  onCheckedChange={(checked) => 
                    handleSelectTask(task.id, checked as boolean)
                  }
                />
              </TableCell>
              <TableCell className="font-medium">
                {task.title}
                {task.description && (
                  <p className="text-sm text-muted-foreground truncate max-w-md">
                    {task.description}
                  </p>
                )}
              </TableCell>
              <TableCell onClick={(e) => e.stopPropagation()}>
                <Select
                  value={task.status}
                  onValueChange={(value) => 
                    onTaskStatusChange?.(task.id, value as TaskStatus)
                  }
                >
                  <SelectTrigger className="w-32 h-8">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {statusOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        <Badge 
                          variant="secondary" 
                          className={cn('text-xs', option.color)}
                        >
                          {option.label}
                        </Badge>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </TableCell>
              <TableCell>
                <div className="flex items-center gap-1">
                  <Flag className={cn('h-4 w-4', priorityConfig[task.priority].color)} />
                  <span className="text-sm">{priorityConfig[task.priority].label}</span>
                </div>
              </TableCell>
              <TableCell>
                {task.assignee ? (
                  <div className="flex items-center gap-2">
                    <Avatar className="h-7 w-7">
                      <AvatarImage src={task.assignee.avatarUrl} />
                      <AvatarFallback className="text-xs">
                        {task.assignee.name.charAt(0).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    <span className="text-sm">{task.assignee.name}</span>
                  </div>
                ) : (
                  <span className="text-sm text-muted-foreground">Unassigned</span>
                )}
              </TableCell>
              <TableCell>
                {task.dueDate ? (
                  <div className="flex items-center gap-1 text-sm">
                    <Calendar className="h-4 w-4" />
                    {format(new Date(task.dueDate), 'MMM d, yyyy')}
                  </div>
                ) : (
                  <span className="text-sm text-muted-foreground">No due date</span>
                )}
              </TableCell>
              <TableCell onClick={(e) => e.stopPropagation()}>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => onTaskEdit?.(task)}>
                      Edit
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => onTaskClick?.(task)}>
                      View Details
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem
                      onClick={() => onTaskDelete?.(task.id)}
                      className="text-red-600"
                    >
                      Delete
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};