import React, { useEffect, useState } from 'react';
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  closestCorners,
} from '@dnd-kit/core';
import type {
  DragEndEvent,
  DragStartEvent,
  DragOverEvent,
} from '@dnd-kit/core';
import { Plus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useTaskStore } from '@/store/useTaskStore';
import { KanbanColumn } from './KanbanColumn';
import { KanbanCard } from './KanbanCard';
import { SimpleTaskDialog } from './SimpleTaskDialog';
import type { Task, TaskSection } from '@/types';
import { cn } from '@/lib/utils';

interface KanbanBoardProps {
  projectId: string;
  className?: string;
}

export const KanbanBoard: React.FC<KanbanBoardProps> = ({ projectId, className }) => {
  const {
    sections,
    tasks,
    isLoading,
    error,
    fetchSections,
    moveTask,
    updateTaskOrder,
    createTask,
    updateTask,
    deleteTask,
    reset,
  } = useTaskStore();

  const [, setActiveId] = useState<string | null>(null);
  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const [isAddingSection, setIsAddingSection] = useState(false);
  const [newSectionName, setNewSectionName] = useState('');
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [isTaskDialogOpen, setIsTaskDialogOpen] = useState(false);
  const [selectedSection, setSelectedSection] = useState<TaskSection | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  useEffect(() => {
    if (projectId) {
      fetchSections(projectId);
    }
    
    return () => {
      reset();
    };
  }, [projectId]);

  const handleDragStart = (event: DragStartEvent) => {
    const { active } = event;
    setActiveId(active.id as string);
    
    // Find the task being dragged
    for (const [sectionId, sectionTasks] of tasks.entries()) {
      const task = sectionTasks.find(t => t.id === active.id);
      if (task) {
        setActiveTask(task);
        break;
      }
    }
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    
    if (!over) {
      setActiveId(null);
      setActiveTask(null);
      return;
    }

    const activeId = active.id as string;
    const overId = over.id as string;

    // Find source section
    let sourceSectionId: string | null = null;
    let sourceTask: Task | null = null;
    let sourceIndex = -1;

    for (const [sectionId, sectionTasks] of tasks.entries()) {
      const index = sectionTasks.findIndex(t => t.id === activeId);
      if (index !== -1) {
        sourceSectionId = sectionId;
        sourceTask = sectionTasks[index];
        sourceIndex = index;
        break;
      }
    }

    if (!sourceSectionId || !sourceTask) {
      setActiveId(null);
      setActiveTask(null);
      return;
    }

    // Check if dropped on a section
    const targetSection = sections.find(s => s.id === overId);
    if (targetSection) {
      // Dropped on empty section or section header
      const targetTasks = tasks.get(targetSection.id) || [];
      if (sourceSectionId !== targetSection.id || sourceIndex !== targetTasks.length) {
        moveTask(activeId, sourceSectionId, targetSection.id, targetTasks.length);
      }
    } else {
      // Dropped on a task - find target section and position
      let targetSectionId: string | null = null;
      let targetIndex = -1;

      for (const [sectionId, sectionTasks] of tasks.entries()) {
        const index = sectionTasks.findIndex(t => t.id === overId);
        if (index !== -1) {
          targetSectionId = sectionId;
          targetIndex = index;
          break;
        }
      }

      if (targetSectionId !== null && targetIndex !== -1) {
        // Calculate new index based on whether we're moving within same section
        if (sourceSectionId === targetSectionId) {
          if (sourceIndex !== targetIndex) {
            const newIndex = sourceIndex < targetIndex ? targetIndex : targetIndex;
            updateTaskOrder(sourceSectionId, activeId, newIndex);
          }
        } else {
          moveTask(activeId, sourceSectionId, targetSectionId, targetIndex);
        }
      }
    }

    setActiveId(null);
    setActiveTask(null);
  };

  const handleDragOver = (event: DragOverEvent) => {
    const { active, over } = event;
    
    if (!over) return;

    const activeId = active.id as string;
    const overId = over.id as string;

    if (activeId === overId) return;

    // Find the sections for both items
    let activeSectionId: string | null = null;
    let overSectionId: string | null = null;

    // Check if over is a section
    const overSection = sections.find(s => s.id === overId);
    if (overSection) {
      overSectionId = overSection.id;
    } else {
      // Find which section the over task belongs to
      for (const [sectionId, sectionTasks] of tasks.entries()) {
        if (sectionTasks.find(t => t.id === overId)) {
          overSectionId = sectionId;
          break;
        }
      }
    }

    // Find which section the active task belongs to
    for (const [sectionId, sectionTasks] of tasks.entries()) {
      if (sectionTasks.find(t => t.id === activeId)) {
        activeSectionId = sectionId;
        break;
      }
    }

    if (activeSectionId && overSectionId && activeSectionId !== overSectionId) {
      // Moving to a different section - handled in handleDragEnd
    }
  };

  const handleCreateTask = async (taskData: Partial<Task>) => {
    if (selectedSection) {
      await createTask(taskData, selectedSection.id);
      setIsTaskDialogOpen(false);
      setSelectedSection(null);
    } else if (editingTask) {
      await updateTask(editingTask.id, taskData);
      setIsTaskDialogOpen(false);
      setEditingTask(null);
    }
  };

  const handleEditTask = (task: Task) => {
    setEditingTask(task);
    setIsTaskDialogOpen(true);
  };

  const handleDeleteTask = async (taskId: string) => {
    // Find which section contains the task
    for (const [sectionId, sectionTasks] of tasks.entries()) {
      if (sectionTasks.find(t => t.id === taskId)) {
        await deleteTask(taskId, sectionId);
        break;
      }
    }
  };

  const handleAddTaskToSection = (section: TaskSection) => {
    setSelectedSection(section);
    setEditingTask(null);
    setIsTaskDialogOpen(true);
  };

  const handleAddSection = async () => {
    if (newSectionName.trim()) {
      // This would need to be implemented in the store
      // For now, we'll just reset the input
      setNewSectionName('');
      setIsAddingSection(false);
    }
  };

  if (isLoading && sections.length === 0) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <p className="text-destructive mb-2">Error loading board</p>
          <p className="text-sm text-muted-foreground">{error}</p>
          <Button 
            onClick={() => fetchSections(projectId)} 
            variant="outline" 
            size="sm"
            className="mt-4"
          >
            Retry
          </Button>
        </div>
      </div>
    );
  }

  return (
    <>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onDragOver={handleDragOver}
      >
        <div className={cn("flex gap-4 h-full overflow-x-auto pb-4", className)}>
          {sections.map((section) => (
            <KanbanColumn
              key={section.id}
              section={section}
              tasks={tasks.get(section.id) || []}
              onAddTask={() => handleAddTaskToSection(section)}
              onEditTask={handleEditTask}
              onDeleteTask={handleDeleteTask}
            />
          ))}
          
          {/* Add Section Column */}
          <div className="flex-shrink-0 w-80">
            {isAddingSection ? (
              <Card className="h-fit">
                <CardContent className="p-3">
                  <Input
                    value={newSectionName}
                    onChange={(e) => setNewSectionName(e.target.value)}
                    placeholder="Section name..."
                    className="mb-2"
                    autoFocus
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        handleAddSection();
                      }
                    }}
                  />
                  <div className="flex gap-2">
                    <Button size="sm" onClick={handleAddSection}>
                      Add
                    </Button>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => {
                        setIsAddingSection(false);
                        setNewSectionName('');
                      }}
                    >
                      Cancel
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ) : (
              <Button
                variant="ghost"
                className="w-full justify-start"
                onClick={() => setIsAddingSection(true)}
              >
                <Plus className="h-4 w-4 mr-2" />
                Add Section
              </Button>
            )}
          </div>
        </div>

        <DragOverlay>
          {activeTask && (
            <KanbanCard
              task={activeTask}
              isDragging
            />
          )}
        </DragOverlay>
      </DndContext>

      <SimpleTaskDialog
        open={isTaskDialogOpen}
        onClose={() => {
          setIsTaskDialogOpen(false);
          setEditingTask(null);
          setSelectedSection(null);
        }}
        onSave={handleCreateTask}
        task={editingTask || undefined}
        defaultSection={selectedSection?.name}
      />
    </>
  );
};