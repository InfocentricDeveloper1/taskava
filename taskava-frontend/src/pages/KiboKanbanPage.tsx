import React, { useState } from 'react';
import { 
  KanbanProvider, 
  KanbanBoard, 
  KanbanCards, 
  KanbanCard,
  KanbanHeader
} from '@/components/ui/kibo-ui/kanban';
import type { DragEndEvent } from '@/components/ui/kibo-ui/kanban';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { cn } from '@/lib/utils';

// Define our task type matching Kibo UI's expected structure
type Task = {
  id: string;
  name: string;
  column: string;
  description?: string;
  assignee?: string;
  priority?: 'low' | 'medium' | 'high';
};

// Define our column type matching Kibo UI's expected structure
type Column = {
  id: string;
  name: string;
};

const initialColumns: Column[] = [
  { id: 'todo', name: 'To Do' },
  { id: 'in-progress', name: 'In Progress' },
  { id: 'review', name: 'Review' },
  { id: 'done', name: 'Done' },
];

const initialTasks: Task[] = [
  {
    id: '1',
    name: 'Implement authentication flow',
    column: 'todo',
    description: 'Add JWT authentication to the API',
    priority: 'high',
  },
  {
    id: '2',
    name: 'Design dashboard UI',
    column: 'in-progress',
    description: 'Create mockups for the main dashboard',
    priority: 'medium',
  },
  {
    id: '3',
    name: 'Fix navigation bug',
    column: 'review',
    description: 'Navigation menu not closing on mobile',
    priority: 'low',
  },
  {
    id: '4',
    name: 'Deploy to staging',
    column: 'done',
    description: 'Deploy latest changes to staging environment',
    priority: 'medium',
  },
];

export function KiboKanbanPage() {
  const [columns] = useState<Column[]>(initialColumns);
  const [tasks, setTasks] = useState<Task[]>(initialTasks);

  // Note: The onDragEnd is optional since KanbanProvider handles drag operations internally
  // We're keeping this for any custom logic we might want to add
  const handleDragEnd = (event: DragEndEvent) => {
    // The KanbanProvider already handles the drag and drop logic internally
    // and updates the data through the onDataChange callback
    // This handler is here if we need to perform additional actions after drag ends
    console.log('Drag ended:', event);
  };

  const getPriorityColor = (priority?: string) => {
    switch (priority) {
      case 'high':
        return 'text-red-600 bg-red-50';
      case 'medium':
        return 'text-yellow-600 bg-yellow-50';
      case 'low':
        return 'text-green-600 bg-green-50';
      default:
        return 'text-gray-600 bg-gray-50';
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">Kibo UI Kanban Board</h1>
        <p className="text-gray-600">
          This is a demonstration of the Kibo UI Kanban component with drag and drop functionality.
        </p>
      </div>

      <KanbanProvider 
        columns={columns} 
        data={tasks} 
        onDataChange={setTasks}
        onDragEnd={handleDragEnd}
        className="w-full"
      >
        {(column) => (
          <KanbanBoard key={column.id} id={column.id} className="flex-1">
            <KanbanHeader className="p-3 bg-gray-50">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold">{column.name}</h3>
                <span className="text-sm text-gray-500">
                  {tasks.filter(task => task.column === column.id).length}
                </span>
              </div>
            </KanbanHeader>
            
            <KanbanCards 
              id={column.id}
              className="p-3 space-y-3 min-h-[200px] overflow-y-auto"
            >
              {(task) => (
                <KanbanCard key={task.id} {...task}>
                  <div className="space-y-2">
                    <h4 className="font-medium">{task.name}</h4>
                    {task.description && (
                      <p className="text-sm text-gray-600">{task.description}</p>
                    )}
                    {task.priority && (
                      <span className={cn(
                        "inline-block px-2 py-1 text-xs font-medium rounded",
                        getPriorityColor(task.priority)
                      )}>
                        {task.priority}
                      </span>
                    )}
                  </div>
                </KanbanCard>
              )}
            </KanbanCards>
            
            <div className="p-3 border-t">
              <Button 
                variant="ghost" 
                size="sm" 
                className="w-full justify-start"
              >
                <Plus className="h-4 w-4 mr-2" />
                Add task
              </Button>
            </div>
          </KanbanBoard>
        )}
      </KanbanProvider>
    </div>
  );
}