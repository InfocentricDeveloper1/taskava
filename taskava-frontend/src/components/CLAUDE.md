# CLAUDE.md - Components Directory

This directory contains all React components organized by feature domains and UI primitives.

## Structure Overview

```
components/
├── ui/               # Shadcn UI base components
├── common/           # Shared across features
├── tasks/            # Task management components
├── projects/         # Project-related components
├── workspaces/       # Workspace components
├── forms/            # Complex form components
├── layout/           # Layout components
└── auth/             # Authentication components
```

## Component Categories

### UI Components (`ui/`)

Base components from Shadcn UI:
- **Purpose**: Foundational, reusable UI primitives
- **Examples**: Button, Card, Dialog, Input, Select
- **Characteristics**:
  - No business logic
  - Highly reusable
  - Fully accessible (via Radix UI)
  - Styled with Tailwind
  - Can be customized after import

To add new UI components:
```bash
npx shadcn-ui@latest add button
npx shadcn-ui@latest add dialog
```

### Common Components (`common/`)

Shared application components:
- **Header**: Top navigation bar
- **Sidebar**: Main navigation
- **PageHeader**: Consistent page titles
- **EmptyState**: When no data
- **ErrorBoundary**: Error handling
- **LoadingSpinner**: Loading states

### Feature Components

#### Tasks (`tasks/`)
- **TaskCard**: Individual task display
- **TaskList**: List of tasks
- **TaskBoard**: Kanban board view
- **TaskForm**: Create/edit task
- **TaskDetails**: Full task view
- **TaskFilters**: Filter controls

#### Projects (`projects/`)
- **ProjectCard**: Project summary
- **ProjectGrid**: Grid layout
- **ProjectHeader**: Project info
- **ProjectSettings**: Configuration
- **ProjectMembers**: Team display

#### Workspaces (`workspaces/`)
- **WorkspaceSwitcher**: Dropdown selector
- **WorkspaceSettings**: Admin panel
- **WorkspaceMembers**: User management

## Component Patterns

### Component File Structure
```
TaskCard/
├── TaskCard.tsx         # Main component
├── TaskCard.test.tsx    # Tests
├── TaskCard.stories.tsx # Storybook stories
├── index.ts            # Barrel export
└── types.ts            # Component-specific types
```

### Prop Patterns

**Interface Definition**
```typescript
interface TaskCardProps {
  task: Task
  onUpdate?: (task: Task) => void
  onDelete?: (id: string) => void
  variant?: 'default' | 'compact' | 'detailed'
  className?: string
}
```

**Component Implementation**
```typescript
export function TaskCard({ 
  task, 
  onUpdate, 
  onDelete,
  variant = 'default',
  className 
}: TaskCardProps) {
  // Implementation
}
```

### Composition Pattern
```typescript
// Compound component
export const TaskCard = {
  Root: TaskCardRoot,
  Header: TaskCardHeader,
  Body: TaskCardBody,
  Actions: TaskCardActions
}

// Usage
<TaskCard.Root>
  <TaskCard.Header task={task} />
  <TaskCard.Body>{task.description}</TaskCard.Body>
  <TaskCard.Actions onEdit={handleEdit} />
</TaskCard.Root>
```

### State Management in Components

**Local State Only**
```typescript
function TaskCard() {
  const [isEditing, setIsEditing] = useState(false)
  // Component-specific UI state
}
```

**Connected to Store**
```typescript
function ProjectList() {
  const { projects, fetchProjects } = useProjectStore()
  // Connected to Zustand store
}
```

**Server State with React Query**
```typescript
function TaskBoard({ projectId }) {
  const { data: tasks, isLoading } = useTasks(projectId)
  // Server state via React Query
}
```

## Styling Guidelines

### Tailwind Classes
```typescript
// Use cn() utility for conditional classes
import { cn } from '@/lib/utils'

<div className={cn(
  "rounded-lg border p-4",
  isActive && "border-primary",
  className
)}>
```

### Consistent Spacing
- Use Tailwind spacing scale: `p-2`, `m-4`, `gap-3`
- Follow 4px base unit
- Consistent padding in cards: `p-4` or `p-6`

### Color Usage
- Use semantic colors: `text-primary`, `bg-secondary`
- Avoid hardcoded colors
- Follow theme variables

## Accessibility Standards

All components must:
1. Have proper ARIA labels
2. Support keyboard navigation
3. Include focus indicators
4. Provide screen reader context
5. Follow WCAG 2.1 AA standards

Example:
```typescript
<Button
  aria-label="Delete task"
  aria-describedby="delete-warning"
  onKeyDown={handleKeyDown}
>
  <TrashIcon className="h-4 w-4" />
</Button>
```

## Performance Best Practices

1. **Memoization**
```typescript
export const TaskCard = React.memo(({ task }) => {
  // Prevents unnecessary re-renders
})
```

2. **Lazy Loading**
```typescript
const TaskDetails = lazy(() => import('./TaskDetails'))
```

3. **Event Handler Optimization**
```typescript
const handleClick = useCallback((id: string) => {
  // Memoized handler
}, [dependency])
```

## Testing Requirements

Each component should have:
1. **Unit tests**: Component logic
2. **Render tests**: Proper rendering
3. **Interaction tests**: User events
4. **Accessibility tests**: A11y compliance

Example test:
```typescript
describe('TaskCard', () => {
  it('renders task title', () => {
    render(<TaskCard task={mockTask} />)
    expect(screen.getByText(mockTask.title)).toBeInTheDocument()
  })
  
  it('calls onUpdate when edited', async () => {
    const onUpdate = jest.fn()
    render(<TaskCard task={mockTask} onUpdate={onUpdate} />)
    
    await userEvent.click(screen.getByLabel('Edit task'))
    // ... edit actions
    
    expect(onUpdate).toHaveBeenCalledWith(expect.objectContaining({
      id: mockTask.id
    }))
  })
})
```

## Common Pitfalls to Avoid

1. **Over-abstraction**: Don't create components for single use
2. **Prop drilling**: Use composition or context
3. **Large components**: Break down into smaller pieces
4. **Business logic in UI**: Keep in services/hooks
5. **Inline functions**: Use useCallback for handlers
6. **Missing keys**: Always provide stable keys in lists