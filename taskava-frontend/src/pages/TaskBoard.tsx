import { useEffect, useState } from 'react';
import { 
  Plus, 
  MoreVertical,
  Calendar,
  User,
  AlertCircle,
  Clock,
  CheckCircle2,
  XCircle,
  Filter
} from 'lucide-react';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { taskApi } from '@/services/api';
import { Task, TaskStatus } from '@/types';
import { useAuthStore } from '@/store/useAuthStore';
import { useMockDataStore } from '@/store/useMockData';

interface Column {
  id: TaskStatus;
  title: string;
  color: string;
  icon: React.ElementType;
}

const columns: Column[] = [
  { id: 'todo', title: 'To Do', color: 'bg-gray-500', icon: Clock },
  { id: 'in_progress', title: 'In Progress', color: 'bg-blue-500', icon: AlertCircle },
  { id: 'in_review', title: 'In Review', color: 'bg-purple-500', icon: CheckCircle2 },
  { id: 'done', title: 'Done', color: 'bg-green-500', icon: CheckCircle2 },
  { id: 'blocked', title: 'Blocked', color: 'bg-red-500', icon: XCircle },
];

export function TaskBoard() {
  const { currentWorkspace } = useAuthStore();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [draggedTask, setDraggedTask] = useState<Task | null>(null);
  const [dragOverColumn, setDragOverColumn] = useState<TaskStatus | null>(null);
  
  useEffect(() => {
    if (currentWorkspace) {
      fetchTasks();
    }
  }, [currentWorkspace]);
  
  const fetchTasks = async () => {
    setLoading(true);
    try {
      // Use mock data for demo
      const { mockTasks } = useMockDataStore.getState();
      setTasks(mockTasks);
      
      // In production, use this:
      /*
      const response = await taskApi.getAll({ size: 100 });
      setTasks(response.content || []);
      */
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const getTasksByStatus = (status: TaskStatus) => {
    return tasks.filter(task => task.status === status);
  };
  
  const handleDragStart = (task: Task) => {
    setDraggedTask(task);
  };
  
  const handleDragOver = (e: React.DragEvent, status: TaskStatus) => {
    e.preventDefault();
    setDragOverColumn(status);
  };
  
  const handleDragLeave = () => {
    setDragOverColumn(null);
  };
  
  const handleDrop = async (e: React.DragEvent, newStatus: TaskStatus) => {
    e.preventDefault();
    setDragOverColumn(null);
    
    if (draggedTask && draggedTask.status !== newStatus) {
      try {
        await taskApi.updateStatus(draggedTask.id, newStatus);
        setTasks(prevTasks => 
          prevTasks.map(task => 
            task.id === draggedTask.id 
              ? { ...task, status: newStatus }
              : task
          )
        );
      } catch (error) {
        console.error('Failed to update task status:', error);
      }
    }
    setDraggedTask(null);
  };
  
  const getPriorityIcon = (priority: string) => {
    switch (priority) {
      case 'urgent':
        return <AlertCircle className="h-4 w-4 text-red-500" />;
      case 'high':
        return <AlertCircle className="h-4 w-4 text-orange-500" />;
      case 'medium':
        return <AlertCircle className="h-4 w-4 text-yellow-500" />;
      case 'low':
        return <AlertCircle className="h-4 w-4 text-gray-400" />;
      default:
        return null;
    }
  };
  
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'urgent': return 'destructive';
      case 'high': return 'destructive';
      case 'medium': return 'default';
      case 'low': return 'secondary';
      default: return 'outline';
    }
  };
  
  const TaskCard = ({ task }: { task: Task }) => (
    <Card
      draggable
      onDragStart={() => handleDragStart(task)}
      className="cursor-move hover:shadow-md transition-shadow mb-3"
    >
      <CardContent className="p-4">
        {/* Task Header */}
        <div className="flex items-start justify-between mb-2">
          <h4 className="font-medium text-sm line-clamp-2 flex-1">{task.title}</h4>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-6 w-6">
                <MoreVertical className="h-3 w-3" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>Actions</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem>Edit Task</DropdownMenuItem>
              <DropdownMenuItem>Assign To</DropdownMenuItem>
              <DropdownMenuItem>Add Comment</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="text-red-600">Delete Task</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
        
        {/* Task Description */}
        {task.description && (
          <p className="text-xs text-gray-600 mb-3 line-clamp-2">
            {task.description}
          </p>
        )}
        
        {/* Task Tags */}
        {task.tags && task.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mb-3">
            {task.tags.slice(0, 3).map((tag, index) => (
              <span 
                key={index}
                className="text-xs px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full"
              >
                {tag}
              </span>
            ))}
            {task.tags.length > 3 && (
              <span className="text-xs text-gray-500">+{task.tags.length - 3}</span>
            )}
          </div>
        )}
        
        {/* Task Footer */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            {/* Priority Badge */}
            <Badge variant={getPriorityColor(task.priority)} className="text-xs">
              {task.priority}
            </Badge>
            
            {/* Due Date */}
            {task.dueDate && (
              <div className="flex items-center text-xs text-gray-500">
                <Calendar className="h-3 w-3 mr-1" />
                {new Date(task.dueDate).toLocaleDateString()}
              </div>
            )}
          </div>
          
          {/* Assignee */}
          {task.assignee ? (
            <Avatar className="h-6 w-6">
              <AvatarImage src={task.assignee.avatarUrl} />
              <AvatarFallback className="text-xs">
                {task.assignee.name?.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>
          ) : (
            <div className="h-6 w-6 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center">
              <User className="h-3 w-3 text-gray-400" />
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading tasks...</p>
        </div>
      </div>
    );
  }
  
  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="p-6 pb-0">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Task Board</h1>
            <p className="text-gray-600 mt-2">
              Visualize and manage your tasks in a Kanban board
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <Button variant="outline">
              <Filter className="h-4 w-4 mr-2" />
              Filter
            </Button>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              New Task
            </Button>
          </div>
        </div>
      </div>
      
      {/* Kanban Board */}
      <div className="flex-1 px-6 pb-6 overflow-x-auto">
        <div className="flex space-x-4 h-full">
          {columns.map((column) => {
            const columnTasks = getTasksByStatus(column.id);
            const isDropTarget = dragOverColumn === column.id;
            
            return (
              <div
                key={column.id}
                className="flex-shrink-0 w-80"
                onDragOver={(e) => handleDragOver(e, column.id)}
                onDragLeave={handleDragLeave}
                onDrop={(e) => handleDrop(e, column.id)}
              >
                {/* Column Header */}
                <div className="mb-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <div className={`w-2 h-2 rounded-full ${column.color}`} />
                      <h3 className="font-semibold text-gray-700">{column.title}</h3>
                      <span className="text-sm text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full">
                        {columnTasks.length}
                      </span>
                    </div>
                    <Button variant="ghost" size="icon" className="h-6 w-6">
                      <Plus className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
                
                {/* Column Content */}
                <div 
                  className={`
                    flex-1 overflow-y-auto rounded-lg p-2 transition-colors
                    ${isDropTarget ? 'bg-purple-50 border-2 border-dashed border-purple-300' : 'bg-gray-50'}
                  `}
                  style={{ maxHeight: 'calc(100vh - 250px)' }}
                >
                  {columnTasks.length > 0 ? (
                    columnTasks.map((task) => (
                      <TaskCard key={task.id} task={task} />
                    ))
                  ) : (
                    <div className="text-center py-8 text-gray-400">
                      <column.icon className="h-8 w-8 mx-auto mb-2 opacity-50" />
                      <p className="text-sm">No tasks</p>
                    </div>
                  )}
                  
                  {/* Add Task Button */}
                  <Button
                    variant="ghost"
                    className="w-full justify-start text-gray-500 hover:text-gray-700"
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    Add task
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}