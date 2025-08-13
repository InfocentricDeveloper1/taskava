import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { format } from 'date-fns';
import {
  Calendar,
  Flag,
  MessageSquare,
  MoreHorizontal,
  GripVertical,
} from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { Task, TaskPriority } from '@/types';
import { cn } from '@/lib/utils';

interface KanbanCardProps {
  task: Task;
  onEdit?: () => void;
  onDelete?: () => void;
  isDragging?: boolean;
}

const priorityConfig: Record<TaskPriority, { color: string; label: string }> = {
  low: { color: 'text-gray-400', label: 'Low' },
  medium: { color: 'text-blue-500', label: 'Medium' },
  high: { color: 'text-orange-500', label: 'High' },
  urgent: { color: 'text-red-500', label: 'Urgent' },
};

export const KanbanCard: React.FC<KanbanCardProps> = ({
  task,
  onEdit,
  onDelete,
  isDragging = false,
}) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging: isSortableDragging,
  } = useSortable({ id: task.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const handleCardClick = (e: React.MouseEvent) => {
    // Prevent card click when clicking on actions
    if ((e.target as HTMLElement).closest('[data-prevent-card-click]')) {
      e.stopPropagation();
      return;
    }
    onEdit?.();
  };

  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'done';

  return (
    <Card
      ref={setNodeRef}
      style={style}
      className={cn(
        "group cursor-pointer transition-all hover:shadow-md",
        (isDragging || isSortableDragging) && "opacity-50 shadow-lg rotate-2",
        "touch-none"
      )}
      onClick={handleCardClick}
    >
      <CardContent className="p-3">
        <div className="flex items-start gap-2">
          <div
            {...attributes}
            {...listeners}
            className="mt-1 cursor-grab active:cursor-grabbing opacity-0 group-hover:opacity-100 transition-opacity"
          >
            <GripVertical className="h-4 w-4 text-muted-foreground" />
          </div>
          
          <div className="flex-1 min-w-0">
            {/* Title */}
            <h4 className="font-medium text-sm mb-2 leading-tight">
              {task.title}
            </h4>
            
            {/* Description preview */}
            {task.description && (
              <p className="text-xs text-muted-foreground mb-2 line-clamp-2">
                {task.description}
              </p>
            )}
            
            {/* Tags */}
            {task.tags.length > 0 && (
              <div className="flex flex-wrap gap-1 mb-2">
                {task.tags.slice(0, 3).map((tag) => (
                  <Badge key={tag} variant="secondary" className="text-xs px-1.5 py-0">
                    {tag}
                  </Badge>
                ))}
                {task.tags.length > 3 && (
                  <Badge variant="secondary" className="text-xs px-1.5 py-0">
                    +{task.tags.length - 3}
                  </Badge>
                )}
              </div>
            )}
            
            {/* Footer with metadata */}
            <div className="flex items-center justify-between mt-3">
              <div className="flex items-center gap-2">
                {/* Priority */}
                {task.priority !== 'medium' && (
                  <div className="flex items-center" title={`${priorityConfig[task.priority].label} priority`}>
                    <Flag className={cn("h-3 w-3", priorityConfig[task.priority].color)} />
                  </div>
                )}
                
                {/* Due date */}
                {task.dueDate && (
                  <div 
                    className={cn(
                      "flex items-center gap-1 text-xs",
                      isOverdue ? "text-destructive" : "text-muted-foreground"
                    )}
                    title={`Due ${format(new Date(task.dueDate), 'PPP')}${isOverdue ? ' (Overdue)' : ''}`}
                  >
                    <Calendar className="h-3 w-3" />
                    <span>{format(new Date(task.dueDate), 'MMM d')}</span>
                  </div>
                )}
                
                {/* Comments count */}
                {task.comments && task.comments.length > 0 && (
                  <div className="flex items-center gap-1 text-xs text-muted-foreground">
                    <MessageSquare className="h-3 w-3" />
                    <span>{task.comments.length}</span>
                  </div>
                )}
              </div>
              
              <div className="flex items-center gap-1">
                {/* Assignee */}
                {task.assignee && (
                  <Avatar className="h-6 w-6" title={task.assignee.name}>
                    <AvatarImage src={task.assignee.avatarUrl} />
                    <AvatarFallback className="text-[10px] bg-primary/10">
                      {task.assignee.name.split(' ').map(n => n[0]).join('').toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                )}
                
                {/* Actions menu */}
                <DropdownMenu>
                  <DropdownMenuTrigger asChild data-prevent-card-click>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-6 w-6 p-0 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <MoreHorizontal className="h-3 w-3" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-40">
                    <DropdownMenuItem onClick={onEdit}>
                      Edit task
                    </DropdownMenuItem>
                    <DropdownMenuItem>Duplicate</DropdownMenuItem>
                    <DropdownMenuItem>Move to...</DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem 
                      onClick={onDelete}
                      className="text-destructive"
                    >
                      Delete task
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};