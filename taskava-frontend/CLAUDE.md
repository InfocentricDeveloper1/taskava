# CLAUDE.md - Frontend Root

This directory contains the React TypeScript frontend for Taskava, built with Vite and modern React patterns.

## Technology Stack

- **Build Tool**: Vite 7.x (Lightning fast HMR and builds)
- **Framework**: React 19 with TypeScript
- **UI Components**: Shadcn UI (built on Radix UI primitives)
- **Styling**: Tailwind CSS with custom design system
- **State Management**: Zustand (lightweight, TypeScript-first)
- **Data Fetching**: TanStack Query (React Query) with Axios
- **Forms**: React Hook Form + Zod validation
- **Routing**: React Router v7
- **Development**: ESLint, Prettier, TypeScript strict mode

## Project Structure

```
taskava-frontend/
├── src/
│   ├── components/       # Reusable UI components
│   │   ├── ui/          # Shadcn UI primitives
│   │   ├── common/      # Shared components (Header, Sidebar, etc.)
│   │   ├── tasks/       # Task-related components
│   │   ├── projects/    # Project-related components
│   │   └── forms/       # Form components
│   ├── pages/           # Route page components
│   ├── hooks/           # Custom React hooks
│   ├── store/           # Zustand state stores
│   ├── services/        # API service layer
│   ├── types/           # TypeScript type definitions
│   ├── utils/           # Utility functions
│   ├── lib/             # Third-party library configs
│   └── styles/          # Global styles and Tailwind config
├── public/              # Static assets
└── index.html          # Entry HTML file
```

## Key Architectural Decisions

### 1. Vite over Next.js
- Pure SPA with client-side routing (no SSR needed)
- Faster development experience
- Simpler deployment (static files)
- Better suited for internal enterprise app

### 2. Shadcn UI Component Library
- Fully customizable, owns the code
- Built on Radix UI for accessibility
- Tailwind-based styling
- Tree-shakeable, only import what's used

### 3. Zustand for State Management
- Simpler than Redux
- TypeScript-first
- Great DevTools
- No boilerplate

### 4. TanStack Query for Server State
- Automatic caching and invalidation
- Optimistic updates
- Infinite queries for lists
- Background refetching

## Development Commands

```bash
# Install dependencies
npm install

# Start dev server (port 3000 configured)
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Build for production
npm run build

# Preview production build
npm run preview

# Add Shadcn component
npx shadcn-ui@latest add <component-name>
```

## Component Patterns

### Page Components
Located in `src/pages/`, these are top-level route components:
```tsx
// src/pages/ProjectsPage.tsx
export function ProjectsPage() {
  // Page-level data fetching
  // Route-specific logic
}
```

### Feature Components
Grouped by domain in `src/components/`:
```tsx
// src/components/tasks/TaskBoard.tsx
export function TaskBoard({ projectId }: TaskBoardProps) {
  // Feature-specific component
}
```

### UI Components
Shadcn components in `src/components/ui/`:
- Imported from Shadcn registry
- Fully customizable
- Follow Radix UI patterns

## State Management Strategy

### Global State (Zustand)
- User authentication state
- Workspace context
- UI preferences (theme, sidebar)
- Notifications

### Server State (React Query)
- All API data
- Caching with stale-while-revalidate
- Optimistic updates for better UX
- Automatic background refetching

### Local State (useState/useReducer)
- Form inputs
- UI toggles
- Temporary states

## API Integration

All API calls go through service layer:
```typescript
// src/services/api/tasks.ts
export const tasksApi = {
  getAll: (params) => apiClient.get('/tasks', { params }),
  create: (data) => apiClient.post('/tasks', data),
  update: (id, data) => apiClient.put(`/tasks/${id}`, data),
  delete: (id) => apiClient.delete(`/tasks/${id}`)
};
```

## Routing Strategy

Client-side routing with React Router:
- Protected routes with auth guards
- Lazy loading for code splitting
- Breadcrumb generation
- Route-based code splitting

## Performance Optimizations

1. **Code Splitting**: Route-based with React.lazy
2. **Bundle Optimization**: Vite's automatic chunking
3. **Image Optimization**: Lazy loading, WebP format
4. **Memoization**: React.memo for expensive components
5. **Virtual Scrolling**: For large lists

## Testing Strategy

- **Unit Tests**: Vitest for components and hooks
- **Integration Tests**: React Testing Library
- **E2E Tests**: Playwright (planned)
- **Visual Regression**: Storybook + Chromatic (planned)

## Build and Deployment

Production build creates static files:
```bash
npm run build
# Output in dist/ folder
# Deploy to any static host (S3, Nginx, etc.)
```

## Important Notes

- Always use TypeScript strict mode
- Follow Tailwind utility-first approach
- Keep components small and focused
- Use custom hooks for logic reuse
- Prefer composition over inheritance
- Maintain consistent file naming (PascalCase for components)

## ShadCN UI Development Rules (MANDATORY)

### MCP Server Usage
We use the ShadCN UI MCP server for ALL component development. The following tools are available in Claude Code:

- `mcp__shadcn-ui__list_components` - List all available components
- `mcp__shadcn-ui__get_component` - Get component source code
- `mcp__shadcn-ui__get_component_demo` - Get usage examples
- `mcp__shadcn-ui__get_component_metadata` - Get dependencies
- `mcp__shadcn-ui__get_block` - Get pre-built blocks
- `mcp__shadcn-ui__list_blocks` - List available blocks

### Development Workflow
1. **Before implementing ANY UI component**:
   - Use `mcp__shadcn-ui__list_components` to see what's available
   - Use `mcp__shadcn-ui__get_component_demo` to understand usage
   - Use `mcp__shadcn-ui__get_component_metadata` for dependencies

2. **Implementation Rules**:
   - NEVER guess component props or structure
   - ALWAYS follow the exact patterns from demos
   - NEVER modify files in `/components/ui/` directly
   - Use the `cn()` utility for conditional classes
   - Install components via: `npx shadcn-ui@latest add [component-name]`

3. **Common Patterns for Taskava**:
   - **Task Cards**: Use Card with Header, Content, Footer composition
   - **Forms**: Use Form + react-hook-form + Zod validation
   - **Data Display**: Use Table/DataTable for lists, Card for items
   - **Feedback**: Toast for success, Alert for errors, Dialog for confirmations
   - **Navigation**: Command for search, Tabs for sections, Breadcrumb for hierarchy

### Quality Standards
- All components must be accessible (keyboard nav, ARIA labels)
- All components must be responsive (mobile-first)
- All components must have loading and error states
- All forms must have proper validation
- All lists must handle empty states

See `/taskava-frontend/docs/design/shadcn-mcp-setup-guide.md` for detailed setup instructions.