import React from 'react';
import { format } from 'date-fns';
import { 
  MoreHorizontal, 
  Calendar, 
  AlertCircle, 
  CheckCircle2,
  Circle,
  Clock,
  Flag
} from 'lucide-react';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button';
import type { Task, TaskStatus, TaskPriority } from '@/types';
import { cn } from '@/lib/utils';

interface TaskCardProps {
  task: Task;
  onEdit?: (task: Task) => void;
  onDelete?: (taskId: string) => void;
  onStatusChange?: (taskId: string, status: TaskStatus) => void;
  onClick?: (task: Task) => void;
  isDraggable?: boolean;
  isCompact?: boolean;
}

const statusIcons: Record<TaskStatus, React.ReactNode> = {
  todo: <Circle className="h-4 w-4" />,
  in_progress: <Clock className="h-4 w-4" />,
  in_review: <AlertCircle className="h-4 w-4" />,
  done: <CheckCircle2 className="h-4 w-4" />,
  blocked: <AlertCircle className="h-4 w-4 text-red-500" />,
};

const statusColors: Record<TaskStatus, string> = {
  todo: 'bg-gray-100 text-gray-800',
  in_progress: 'bg-blue-100 text-blue-800',
  in_review: 'bg-yellow-100 text-yellow-800',
  done: 'bg-green-100 text-green-800',
  blocked: 'bg-red-100 text-red-800',
};

const priorityColors: Record<TaskPriority, string> = {
  low: 'text-gray-400',
  medium: 'text-blue-500',
  high: 'text-orange-500',
  urgent: 'text-red-500',
};

const priorityLabels: Record<TaskPriority, string> = {
  low: 'Low',
  medium: 'Medium',
  high: 'High',
  urgent: 'Urgent',
};

export const TaskCard: React.FC<TaskCardProps> = ({
  task,
  onEdit,
  onDelete,
  onStatusChange,
  onClick,
  isDraggable = false,
  isCompact = false,
}) => {
  const handleCardClick = (e: React.MouseEvent) => {
    // Prevent card click when clicking on actions
    if ((e.target as HTMLElement).closest('[data-prevent-card-click]')) {
      return;
    }
    onClick?.(task);
  };

  const handleStatusChange = (status: TaskStatus) => {
    onStatusChange?.(task.id, status);
  };

  return (
    <Card
      className={cn(
        'group hover:shadow-md transition-shadow cursor-pointer',
        isDraggable && 'cursor-move',
        isCompact && 'p-2'
      )}
      onClick={handleCardClick}
      draggable={isDraggable}
      data-task-id={task.id}
    >
      <CardHeader className={cn('pb-3', isCompact && 'p-2 pb-1')}>
        <div className="flex items-start justify-between gap-2">
          <div className="flex-1 min-w-0">
            <h3 className={cn(
              'font-medium leading-none truncate',
              isCompact ? 'text-sm' : 'text-base'
            )}>
              {task.title}
            </h3>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild data-prevent-card-click>
              <Button
                variant="ghost"
                size="sm"
                className="h-8 w-8 p-0 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => onEdit?.(task)}>
                Edit task
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => handleStatusChange('todo')}>
                Mark as Todo
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleStatusChange('in_progress')}>
                Mark as In Progress
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleStatusChange('in_review')}>
                Mark as In Review
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleStatusChange('done')}>
                Mark as Done
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => onDelete?.(task.id)}
                className="text-red-600"
              >
                Delete task
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent className={cn('space-y-3', isCompact && 'p-2 pt-0 space-y-2')}>
        {!isCompact && task.description && (
          <p className="text-sm text-muted-foreground line-clamp-2">
            {task.description}
          </p>
        )}
        
        <div className="flex flex-wrap gap-2">
          <Badge 
            variant="secondary" 
            className={cn('gap-1', statusColors[task.status])}
          >
            {statusIcons[task.status]}
            <span className="text-xs">{task.status.replace('_', ' ')}</span>
          </Badge>
          
          {task.priority !== 'medium' && (
            <Badge variant="outline" className="gap-1">
              <Flag className={cn('h-3 w-3', priorityColors[task.priority])} />
              <span className="text-xs">{priorityLabels[task.priority]}</span>
            </Badge>
          )}
          
          {task.tags.map((tag) => (
            <Badge key={tag} variant="outline" className="text-xs">
              {tag}
            </Badge>
          ))}
        </div>
        
        <div className="flex items-center justify-between text-xs text-muted-foreground">
          <div className="flex items-center gap-3">
            {task.dueDate && (
              <div className="flex items-center gap-1">
                <Calendar className="h-3 w-3" />
                <span>{format(new Date(task.dueDate), 'MMM d')}</span>
              </div>
            )}
            
            {task.assignee && (
              <div className="flex items-center gap-1">
                <Avatar className="h-5 w-5">
                  <AvatarImage src={task.assignee.avatarUrl} />
                  <AvatarFallback className="text-[10px]">
                    {task.assignee.name.charAt(0).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <span className="truncate max-w-[100px]">{task.assignee.name}</span>
              </div>
            )}
          </div>
          
          {task.comments && task.comments.length > 0 && (
            <span className="text-xs">{task.comments.length} comments</span>
          )}
        </div>
      </CardContent>
    </Card>
  );
};