# CLAUDE.md - Frontend Source Directory

This directory contains all source code for the Taskava React application. The structure follows feature-based organization with clear separation of concerns.

## Directory Structure

```
src/
├── components/          # All React components
│   ├── ui/             # Base UI components (Shadcn)
│   ├── common/         # Shared app components
│   ├── tasks/          # Task-related components
│   ├── projects/       # Project-related components
│   ├── workspaces/     # Workspace components
│   └── forms/          # Form-specific components
├── pages/              # Route page components
├── hooks/              # Custom React hooks
├── store/              # Zustand state management
├── services/           # API and external services
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
├── lib/                # Library configurations
├── styles/             # Global styles
└── config/             # App configuration
```

## Key Patterns and Conventions

### Component Organization

**UI Components** (`components/ui/`)
- Imported from Shadcn UI registry
- Pure, presentational components
- No business logic or API calls
- Fully themed with Tailwind

**Feature Components** (`components/{feature}/`)
- Domain-specific components
- Can contain business logic
- May connect to store or API
- Composed of UI components

**Page Components** (`pages/`)
- Top-level route components
- Handle route params
- Coordinate feature components
- Manage page-level state

### Import Aliases

Configured in `vite.config.ts`:
```typescript
'@/': './src/'
'@components': './src/components'
'@hooks': './src/hooks'
'@services': './src/services'
'@types': './src/types'
'@utils': './src/utils'
```

Usage:
```typescript
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
```

### State Management Patterns

**Zustand Stores** (`store/`)
```typescript
// store/authStore.ts
export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  login: async (credentials) => { /* ... */ },
  logout: () => set({ user: null })
}))
```

**React Query for Server State** (`services/`)
```typescript
// hooks/useProjects.ts
export function useProjects() {
  return useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.getAll,
    staleTime: 5 * 60 * 1000 // 5 minutes
  })
}
```

### Type Safety

**Shared Types** (`types/`)
- DTOs matching backend responses
- Form schemas with Zod
- Component prop types
- Utility types

Example:
```typescript
// types/task.ts
export interface Task {
  id: string
  title: string
  status: TaskStatus
  assignees: User[]
  // ... matches backend DTO
}

// types/forms.ts
export const createTaskSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  projectId: z.string().uuid()
})
```

### Custom Hooks Pattern

All custom hooks in `hooks/`:
- Prefix with `use`
- Return consistent shape
- Handle loading/error states
- Provide TypeScript types

```typescript
// hooks/useTasks.ts
export function useTasks(projectId: string) {
  const query = useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => tasksApi.getByProject(projectId)
  })
  
  return {
    tasks: query.data ?? [],
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch
  }
}
```

### Service Layer Pattern

API calls abstracted in `services/`:
```typescript
// services/api/client.ts
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// services/api/tasks.ts
export const tasksApi = {
  getAll: (params?: TaskFilters) => 
    apiClient.get<PaginatedResponse<Task>>('/tasks', { params }),
  
  create: (data: CreateTaskRequest) =>
    apiClient.post<Task>('/tasks', data)
}
```

### Component Patterns

**Container/Presenter Pattern**
```typescript
// components/tasks/TaskListContainer.tsx (Smart)
export function TaskListContainer() {
  const { tasks, isLoading } = useTasks()
  return <TaskList tasks={tasks} loading={isLoading} />
}

// components/tasks/TaskList.tsx (Dumb)
export function TaskList({ tasks, loading }: TaskListProps) {
  // Pure presentation
}
```

**Compound Components**
```typescript
// Usage
<TaskCard>
  <TaskCard.Header />
  <TaskCard.Body />
  <TaskCard.Actions />
</TaskCard>
```

## File Naming Conventions

- **Components**: PascalCase (`TaskCard.tsx`)
- **Hooks**: camelCase (`useTasks.ts`)
- **Utilities**: camelCase (`formatDate.ts`)
- **Types**: PascalCase (`Task.ts`)
- **Constants**: UPPER_SNAKE_CASE

## Performance Guidelines

1. Use `React.memo` for expensive components
2. Implement virtual scrolling for long lists
3. Lazy load routes with `React.lazy`
4. Optimize re-renders with proper dependencies
5. Use `useMemo` and `useCallback` appropriately

## Testing Approach

Each component should have:
- Unit tests for logic
- Integration tests for user flows
- Accessibility tests
- Visual regression tests (Storybook)

Test files co-located:
```
components/
  TaskCard.tsx
  TaskCard.test.tsx
  TaskCard.stories.tsx
```

## Important Patterns

1. **Error Boundaries**: Wrap feature sections
2. **Suspense Boundaries**: For lazy-loaded content
3. **Optimistic Updates**: For better UX
4. **Debounced Inputs**: For search/filters
5. **Infinite Scroll**: For large lists
6. **Skeleton Loading**: For perceived performance