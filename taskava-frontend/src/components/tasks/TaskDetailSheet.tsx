import React, { useState } from 'react';
import { format } from 'date-fns';
import { 
  Flag, 
  MessageSquare, 
  Plus,
  Trash2,
  CheckSquare,
  Link2
} from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { Task, TaskStatus, TaskPriority, User as UserType } from '@/types';
import { cn } from '@/lib/utils';

interface TaskDetailSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  task: Task | null;
  users: UserType[];
  onUpdate: (taskId: string, updates: Partial<Task>) => Promise<void>;
  onDelete: (taskId: string) => Promise<void>;
  onAddComment: (taskId: string, content: string) => Promise<void>;
  onDeleteComment?: (taskId: string, commentId: string) => Promise<void>;
}

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

export const TaskDetailSheet: React.FC<TaskDetailSheetProps> = ({
  open,
  onOpenChange,
  task,
  users,
  onUpdate,
  onDelete,
  onAddComment,
  onDeleteComment,
}) => {
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [isEditingDescription, setIsEditingDescription] = useState(false);
  const [editedTitle, setEditedTitle] = useState('');
  const [editedDescription, setEditedDescription] = useState('');
  const [newComment, setNewComment] = useState('');
  const [activeTab, setActiveTab] = useState('overview');

  if (!task) return null;

  const handleTitleSave = async () => {
    if (editedTitle.trim() && editedTitle !== task.title) {
      await onUpdate(task.id, { title: editedTitle });
    }
    setIsEditingTitle(false);
  };

  const handleDescriptionSave = async () => {
    if (editedDescription !== task.description) {
      await onUpdate(task.id, { description: editedDescription });
    }
    setIsEditingDescription(false);
  };

  const handleStatusChange = async (status: TaskStatus) => {
    await onUpdate(task.id, { status });
  };

  const handlePriorityChange = async (priority: TaskPriority) => {
    await onUpdate(task.id, { priority });
  };

  const handleAssigneeChange = async (assigneeId: string) => {
    await onUpdate(task.id, { assigneeId: assigneeId || undefined });
  };

  const handleDueDateChange = async (dueDate: string) => {
    await onUpdate(task.id, { dueDate: dueDate ? new Date(dueDate) : undefined });
  };

  const handleAddComment = async () => {
    if (newComment.trim()) {
      await onAddComment(task.id, newComment);
      setNewComment('');
    }
  };

  const handleDeleteTask = async () => {
    if (confirm('Are you sure you want to delete this task?')) {
      await onDelete(task.id);
      onOpenChange(false);
    }
  };

  // Mock activity data (in real app, this would come from API)
  const activities = [
    {
      id: '1',
      type: 'status_change',
      user: task.assignee || { id: '1', name: 'System', email: '', role: 'user' as const, avatarUrl: undefined } as UserType,
      timestamp: new Date(task.updatedAt),
      description: `Status changed to ${task.status}`,
    },
    {
      id: '2',
      type: 'created',
      user: { id: task.createdBy, name: 'Creator', email: '', role: 'user' },
      timestamp: new Date(task.createdAt),
      description: 'Task created',
    },
  ];

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full sm:max-w-2xl">
        <SheetHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              {isEditingTitle ? (
                <div className="flex items-center gap-2">
                  <Input
                    value={editedTitle}
                    onChange={(e) => setEditedTitle(e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') handleTitleSave();
                    }}
                    onBlur={handleTitleSave}
                    autoFocus
                    className="text-lg font-semibold"
                  />
                </div>
              ) : (
                <SheetTitle 
                  className="cursor-pointer hover:bg-muted rounded px-2 py-1 -ml-2"
                  onClick={() => {
                    setEditedTitle(task.title);
                    setIsEditingTitle(true);
                  }}
                >
                  {task.title}
                </SheetTitle>
              )}
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="ghost"
                size="icon"
                onClick={handleDeleteTask}
                className="text-red-600 hover:text-red-700"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </SheetHeader>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="mt-6">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="activity">Activity</TabsTrigger>
            <TabsTrigger value="comments">
              Comments {task.comments && task.comments.length > 0 && `(${task.comments.length})`}
            </TabsTrigger>
            <TabsTrigger value="subtasks">Subtasks</TabsTrigger>
            <TabsTrigger value="dependencies">Links</TabsTrigger>
          </TabsList>

          <ScrollArea className="h-[calc(100vh-200px)] mt-6">
            <TabsContent value="overview" className="space-y-6">
              {/* Description */}
              <div>
                <label className="text-sm font-medium mb-2 block">Description</label>
                {isEditingDescription ? (
                  <div className="space-y-2">
                    <Textarea
                      value={editedDescription}
                      onChange={(e) => setEditedDescription(e.target.value)}
                      className="min-h-[100px]"
                      placeholder="Add a description..."
                    />
                    <div className="flex gap-2">
                      <Button size="sm" onClick={handleDescriptionSave}>
                        Save
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          setIsEditingDescription(false);
                          setEditedDescription(task.description || '');
                        }}
                      >
                        Cancel
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div
                    className="min-h-[60px] p-3 rounded-md border cursor-pointer hover:bg-muted"
                    onClick={() => {
                      setEditedDescription(task.description || '');
                      setIsEditingDescription(true);
                    }}
                  >
                    {task.description || (
                      <span className="text-muted-foreground">Click to add description</span>
                    )}
                  </div>
                )}
              </div>

              {/* Properties */}
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium mb-1 block">Status</label>
                    <Select value={task.status} onValueChange={handleStatusChange}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {statusOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            <Badge variant="secondary" className={cn('text-xs', option.color)}>
                              {option.label}
                            </Badge>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <label className="text-sm font-medium mb-1 block">Priority</label>
                    <Select value={task.priority} onValueChange={handlePriorityChange}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.entries(priorityConfig).map(([value, config]) => (
                          <SelectItem key={value} value={value}>
                            <div className="flex items-center gap-2">
                              <Flag className={cn('h-4 w-4', config.color)} />
                              {config.label}
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <label className="text-sm font-medium mb-1 block">Assignee</label>
                    <Select 
                      value={task.assigneeId || ''} 
                      onValueChange={handleAssigneeChange}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Unassigned">
                          {task.assignee && (
                            <div className="flex items-center gap-2">
                              <Avatar className="h-5 w-5">
                                <AvatarImage src={task.assignee.avatarUrl} />
                                <AvatarFallback className="text-xs">
                                  {task.assignee.name.charAt(0)}
                                </AvatarFallback>
                              </Avatar>
                              {task.assignee.name}
                            </div>
                          )}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="">Unassigned</SelectItem>
                        {users.map((user) => (
                          <SelectItem key={user.id} value={user.id}>
                            <div className="flex items-center gap-2">
                              <Avatar className="h-5 w-5">
                                <AvatarImage src={user.avatarUrl} />
                                <AvatarFallback className="text-xs">
                                  {user.name.charAt(0)}
                                </AvatarFallback>
                              </Avatar>
                              {user.name}
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <label className="text-sm font-medium mb-1 block">Due Date</label>
                    <Input
                      type="date"
                      value={task.dueDate ? format(new Date(task.dueDate), 'yyyy-MM-dd') : ''}
                      onChange={(e) => handleDueDateChange(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              {/* Tags */}
              <div>
                <label className="text-sm font-medium mb-2 block">Tags</label>
                <div className="flex flex-wrap gap-2">
                  {task.tags.map((tag) => (
                    <Badge key={tag} variant="outline">
                      {tag}
                    </Badge>
                  ))}
                  <Button variant="outline" size="sm">
                    <Plus className="h-3 w-3 mr-1" />
                    Add tag
                  </Button>
                </div>
              </div>

              {/* Metadata */}
              <div className="pt-4 border-t space-y-2 text-sm text-muted-foreground">
                <div className="flex justify-between">
                  <span>Created</span>
                  <span>{format(new Date(task.createdAt), 'MMM d, yyyy h:mm a')}</span>
                </div>
                <div className="flex justify-between">
                  <span>Updated</span>
                  <span>{format(new Date(task.updatedAt), 'MMM d, yyyy h:mm a')}</span>
                </div>
                {task.completedAt && (
                  <div className="flex justify-between">
                    <span>Completed</span>
                    <span>{format(new Date(task.completedAt), 'MMM d, yyyy h:mm a')}</span>
                  </div>
                )}
              </div>
            </TabsContent>

            <TabsContent value="activity" className="space-y-4">
              {activities.map((activity) => (
                <div key={activity.id} className="flex gap-3">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={'avatarUrl' in activity.user ? activity.user.avatarUrl : undefined} />
                    <AvatarFallback className="text-xs">
                      {activity.user.name.charAt(0)}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-sm">{activity.user.name}</span>
                      <span className="text-xs text-muted-foreground">
                        {format(activity.timestamp, 'MMM d, h:mm a')}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground">{activity.description}</p>
                  </div>
                </div>
              ))}
            </TabsContent>

            <TabsContent value="comments" className="space-y-4">
              <div className="space-y-4">
                {task.comments?.map((comment) => (
                  <div key={comment.id} className="flex gap-3">
                    <Avatar className="h-8 w-8">
                      <AvatarImage src={comment.user?.avatarUrl} />
                      <AvatarFallback className="text-xs">
                        {comment.user?.name.charAt(0) || 'U'}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="font-medium text-sm">{comment.user?.name}</span>
                          <span className="text-xs text-muted-foreground">
                            {format(new Date(comment.createdAt), 'MMM d, h:mm a')}
                          </span>
                        </div>
                        {onDeleteComment && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onDeleteComment(task.id, comment.id)}
                          >
                            <Trash2 className="h-3 w-3" />
                          </Button>
                        )}
                      </div>
                      <p className="text-sm mt-1">{comment.content}</p>
                    </div>
                  </div>
                ))}
              </div>
              
              <div className="flex gap-2 pt-4 border-t">
                <Textarea
                  placeholder="Add a comment..."
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  className="min-h-[80px]"
                />
                <Button onClick={handleAddComment} disabled={!newComment.trim()}>
                  <MessageSquare className="h-4 w-4 mr-1" />
                  Comment
                </Button>
              </div>
            </TabsContent>

            <TabsContent value="subtasks" className="space-y-4">
              <div className="text-center py-8 text-muted-foreground">
                <CheckSquare className="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p>No subtasks yet</p>
                <Button variant="outline" size="sm" className="mt-4">
                  <Plus className="h-4 w-4 mr-1" />
                  Add subtask
                </Button>
              </div>
            </TabsContent>

            <TabsContent value="dependencies" className="space-y-4">
              <div className="text-center py-8 text-muted-foreground">
                <Link2 className="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p>No dependencies or links</p>
                <Button variant="outline" size="sm" className="mt-4">
                  <Plus className="h-4 w-4 mr-1" />
                  Add dependency
                </Button>
              </div>
            </TabsContent>
          </ScrollArea>
        </Tabs>
      </SheetContent>
    </Sheet>
  );
};