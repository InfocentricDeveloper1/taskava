# Task UI Components

This directory contains comprehensive Task management components for the Taskava frontend, built following strict ShadCN UI development patterns.

## Components

### TaskCard (`TaskCard.tsx`)
A reusable card component for displaying task information in both list and board views.

**Features:**
- Draggable for Kanban boards
- Priority indicators with color coding
- Due date display
- Assignee avatars
- Quick actions dropdown menu
- Status badges
- Tag display
- Compact mode for board view

**Props:**
- `task`: Task object to display
- `onEdit`: Edit handler
- `onDelete`: Delete handler
- `onStatusChange`: Status change handler
- `onClick`: Click handler for opening details
- `isDraggable`: Enable drag functionality
- `isCompact`: Compact display mode

### TaskList (`TaskList.tsx`)
A versatile list component that supports both table and card view modes.

**Features:**
- Sortable columns (title, status, priority, due date, assignee)
- Inline status editing
- Bulk selection with checkboxes
- Empty state handling
- Responsive design (table on desktop, cards on mobile)
- Loading state

**Props:**
- `tasks`: Array of tasks to display
- `loading`: Loading state flag
- `onTaskClick`: Task click handler
- `onTaskEdit`: Edit handler
- `onTaskDelete`: Delete handler
- `onTaskStatusChange`: Status change handler
- `onBulkAction`: Bulk action handler
- `viewMode`: 'table' or 'cards'

### TaskDialog (`TaskDialog.tsx`)
A comprehensive dialog for creating and editing tasks with form validation.

**Features:**
- Create and Edit modes
- Form validation with Zod schema
- Multi-project support (task multi-homing)
- Assignee selection with avatars
- Priority and status selection
- Due date picker
- Tag management
- Custom fields support

**Props:**
- `open`: Dialog open state
- `onOpenChange`: Open state change handler
- `task`: Task to edit (optional)
- `projects`: Available projects
- `users`: Available users for assignment
- `onSubmit`: Form submission handler
- `mode`: 'create' or 'edit'

### TaskDetailSheet (`TaskDetailSheet.tsx`)
A slide-out sheet for detailed task view and inline editing.

**Features:**
- Tabbed interface (Overview, Activity, Comments, Subtasks, Dependencies)
- Inline editing for all fields
- Activity timeline
- Comment system
- Task metadata display
- Real-time updates

**Props:**
- `open`: Sheet open state
- `onOpenChange`: Open state change handler
- `task`: Task to display
- `users`: Available users
- `onUpdate`: Update handler
- `onDelete`: Delete handler
- `onAddComment`: Comment handler
- `onDeleteComment`: Comment deletion handler

### TaskFilters (`TaskFilters.tsx`)
An advanced filtering popover for task lists.

**Features:**
- Filter by status, priority, assignee, project, tags
- Date range filtering
- Search functionality
- Save filter presets
- Clear all option
- Active filter count badge

**Props:**
- `onFilterChange`: Filter change handler
- `projects`: Available projects
- `users`: Available users
- `savedFilters`: Saved filter presets
- `onSaveFilter`: Save filter handler
- `onDeleteFilter`: Delete filter handler

## Zod Schemas (`/types/schemas/task.ts`)

### CreateTaskSchema
Validation schema for creating new tasks with required fields:
- `title` (required, max 255 chars)
- `projectIds` (required, at least one)
- Default values for status and priority

### UpdateTaskSchema
Validation schema for updating existing tasks with all fields optional.

### TaskFilterSchema
Validation schema for task filtering with support for multiple criteria.

## Usage Example

```tsx
import {
  TaskCard,
  TaskList,
  TaskDialog,
  TaskDetailSheet,
  TaskFilters,
} from '@/components/tasks';

// In your component
const [tasks, setTasks] = useState<Task[]>([]);
const [selectedTask, setSelectedTask] = useState<Task | null>(null);
const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);

// Render components
<TaskFilters
  onFilterChange={handleFilterChange}
  projects={projects}
  users={users}
/>

<TaskList
  tasks={tasks}
  onTaskClick={(task) => setSelectedTask(task)}
  onTaskEdit={handleEdit}
  onTaskDelete={handleDelete}
  viewMode="table"
/>

<TaskDialog
  open={isCreateDialogOpen}
  onOpenChange={setIsCreateDialogOpen}
  projects={projects}
  users={users}
  onSubmit={handleCreateTask}
  mode="create"
/>
```

## Design Patterns

All components follow these patterns:
1. **Type Safety**: Full TypeScript with proper type imports
2. **Validation**: Zod schemas for all forms
3. **Composition**: Small, reusable components
4. **Accessibility**: ARIA labels and keyboard navigation
5. **Performance**: Memoization where appropriate
6. **Responsiveness**: Mobile-first design
7. **ShadCN UI**: Consistent use of base components

## Dependencies

- React Hook Form for form management
- Zod for validation
- date-fns for date formatting
- Lucide React for icons
- ShadCN UI components (Card, Dialog, Sheet, Table, etc.)
- Tailwind CSS for styling