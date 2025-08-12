# ShadCN MCP Server Automation Setup Guide

## Overview
This guide will help you set up the shadcn-ui-mcp-server to automate UI development using AI tools like Cursor and Claude. The workflow consists of three main parts:
1. Setting up the MCP server for context
2. Creating rule files to guide AI behavior
3. Using TweakCN for custom theming

## Prerequisites
- Node.js installed on your system
- A Next.js project (or create one)
- Cursor IDE installed
- GitHub account for API token

## Step 1: GitHub Personal Access Token Setup

1. Go to [GitHub Settings > Tokens](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Give it a descriptive name (e.g., "shadcn-mcp-server")
4. Select scopes:
   - `repo` (full control)
   - `read:org` (if working with organization repos)
5. Generate and **save the token immediately** (you won't see it again!)

## Step 2: Install ShadCN UI MCP Server

### Option A: Quick Test (Rate Limited - 60 requests/hour)
```bash
npx @jpisnice/shadcn-ui-mcp-server
```

### Option B: Production Setup (5000 requests/hour)
```bash
npx @jpisnice/shadcn-ui-mcp-server --github-api-key ghp_your_token_here
```

## Step 3: Configure Cursor IDE

1. Open Cursor Settings:
   - Mac: `Cmd + Shift + J`
   - Windows/Linux: `Ctrl + Shift + J`

2. Navigate to: **Tools & Integrations → New MCP Server**

3. Add this configuration:
```json
{
  "mcpServers": {
    "shadcn-ui": {
      "command": "npx",
      "args": ["@jpisnice/shadcn-ui-mcp-server", "--github-api-key", "ghp_your_token_here"]
    }
  }
}
```

4. Restart Cursor to activate the MCP server

## Step 4: Available MCP Tools

Once installed, you'll have access to these tools:

- **`get_component`**: Get component source code
- **`get_component_demo`**: Get component usage examples
- **`list_components`**: List all available components
- **`get_component_metadata`**: Get component dependencies and info
- **`get_block`**: Get block implementations (dashboard-01, calendar-01, etc.)
- **`list_blocks`**: List all available blocks with categories

## Step 5: Create Your Rule File

Create a `.cursorrule` or `rule.mdc` file in your project root:

```markdown
# ShadCN UI Development Rules

## Core Principles
1. Always use the shadcn-ui MCP server tools before implementing any UI component
2. Check component demos using `get_component_demo` for proper usage patterns
3. Follow ShadCN's design system and component composition patterns
4. Maintain consistency across the application

## Component Usage Rules
1. **Before using any component:**
   - Use `get_component_metadata` to check dependencies
   - Use `get_component_demo` to understand proper implementation
   - Never guess component props or structure

2. **Component Installation:**
   - Always install components via CLI: `npx shadcn-ui@latest add [component-name]`
   - Check if component is already installed before adding

3. **Styling Rules:**
   - Use Tailwind CSS utility classes
   - Respect the existing theme variables
   - Avoid inline styles unless absolutely necessary
   - Use cn() utility for conditional classes

## Project Structure
- `/components/ui/` - ShadCN UI components (don't modify directly)
- `/components/` - Custom components that use ShadCN
- `/app/` - Next.js app directory pages
- `/lib/` - Utility functions and helpers

## Implementation Process
1. Analyze the task requirements
2. List required ShadCN components using `list_components`
3. Get component demos for each required component
4. Build the UI structure following the demos
5. Apply custom business logic
6. Test responsiveness and accessibility

## Code Quality Standards
- Use TypeScript for all components
- Add proper type definitions
- Include loading and error states
- Implement proper form validation
- Ensure mobile responsiveness
- Follow accessibility best practices

## Common Patterns
1. **Forms**: Use Form, Input, Button, Label components with react-hook-form
2. **Data Display**: Use Table, Card, Badge components
3. **Navigation**: Use NavigationMenu, Tabs, Breadcrumb
4. **Feedback**: Use Toast, Alert, Dialog for user feedback
5. **Layout**: Use proper spacing with space-y-*, space-x-* utilities
```

## Step 6: Create a Task File Structure

Create a `task.md` file to define your UI requirements:

```markdown
# MCP Server Dashboard UI Implementation

## Project Overview
Build a modern web interface for managing MCP servers using ShadCN components.

## Pages Required

### 1. Login Page (`/login`)
- **Components Needed:**
  - Card (container)
  - Form (authentication form)
  - Input (email & password fields)
  - Button (submit)
  - Label (field labels)
  - Alert (error messages)

- **Features:**
  - Email/password authentication
  - Form validation
  - Error handling
  - Redirect to dashboard after login

### 2. Dashboard Page (`/dashboard`)
- **Components Needed:**
  - Card (server cards)
  - Badge (status indicators)
  - Button (actions)
  - Grid layout

- **Features:**
  - Grid of MCP server cards
  - Each card shows: name, status, region, version
  - Click to view details
  - Search/filter functionality

### 3. Server Detail Page (`/server/[id]`)
- **Components Needed:**
  - Card (main container)
  - Tabs (organize content)
  - Badge (metadata display)
  - Collapsible (installation steps)
  - Button (actions)

- **Features:**
  - Header image
  - Server metadata display
  - Installation instructions
  - Configuration options
  - Action buttons (start/stop/restart)

## Component Hierarchy

```
App
├── Layout (with navigation)
├── LoginPage
│   └── LoginForm
├── DashboardPage
│   ├── ServerGrid
│   └── ServerCard
└── ServerDetailPage
    ├── ServerHeader
    ├── ServerMetadata
    └── InstallationSteps
```

## Data Structure

```typescript
interface MCPServer {
  id: string;
  name: string;
  status: 'running' | 'stopped' | 'error';
  region: string;
  version: string;
  imageUrl: string;
  installationSteps: string[];
  lastUpdated: Date;
}
```
```

## Step 7: Implement with Cursor

1. Open your Next.js project in Cursor
2. Create the rule and task files as shown above
3. Use this prompt to start implementation:

```
@rule.mdc
Please implement the UI plan outlined in @task.md for our Next.js app.
Start by:
1. Installing necessary ShadCN components
2. Creating the login page
3. Setting up the basic routing structure

Use the shadcn-ui MCP server tools to get proper component usage examples.
```

## Step 8: Customize with TweakCN

1. Visit [TweakCN Editor](https://tweakcn.com/editor/theme)
2. Customize:
   - Colors (primary, secondary, accent)
   - Border radius
   - Typography
   - Spacing
   - Shadows

3. Export your theme
4. Apply to your project:
   - Copy the CSS variables
   - Replace in your `globals.css` or `app.css`

## Best Practices & Tips

### 1. Always Plan Before Building
- Create detailed task.md files
- List all components needed
- Define the data structure
- Sketch the component hierarchy

### 2. Use MCP Tools Effectively
```
// Good: Check demo first
Use get_component_demo for Button
Then implement based on the demo

// Bad: Guess implementation
Just write <Button>Click me</Button> without checking
```

### 3. Iterate on Your Rules
- Start with basic rules
- Add specific patterns as you discover them
- Document common issues and solutions

### 4. Component Installation Order
1. Layout components first (Card, Container)
2. Form components (Input, Button, Form)
3. Data display (Table, List)
4. Feedback components (Toast, Alert)

### 5. Common Gotchas to Avoid
- Don't modify `/components/ui/` files directly
- Always use the `cn()` utility for conditional classes
- Check component dependencies before using
- Test on mobile devices
- Ensure proper TypeScript types

## Troubleshooting

### MCP Server Not Working
1. Check your GitHub token is valid
2. Ensure Cursor was restarted after configuration
3. Check the MCP server logs in Cursor

### Components Not Installing
1. Ensure you're in the project root
2. Check your package.json has required dependencies
3. Try manual installation if automated fails

### Styling Issues
1. Check Tailwind configuration
2. Ensure CSS variables are properly set
3. Use browser DevTools to debug

## Example Workflow

1. **Define the task:**
   "I need a user profile page with tabs for settings, activity, and preferences"

2. **Check available components:**
   ```
   Use list_components tool
   Identify: Tabs, Card, Form, Input, Button, Avatar
   ```

3. **Get component demos:**
   ```
   Use get_component_demo for Tabs
   Use get_component_demo for Form
   ```

4. **Implement following demos:**
   - Create the page structure
   - Add components based on demos
   - Apply custom logic

5. **Customize appearance:**
   - Use TweakCN for theming
   - Apply custom styles where needed

## Conclusion

This workflow dramatically speeds up UI development by:
- Providing proper context to AI tools
- Ensuring correct component usage
- Maintaining consistency
- Allowing easy customization

Remember: The key is to let the MCP server provide context while you focus on the business logic and user experience!