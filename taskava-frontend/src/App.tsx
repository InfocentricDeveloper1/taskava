import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { TestStyling } from './TestStyling';
import { TasksDemo } from './pages/TasksDemo';
import { KanbanView } from './components/tasks/KanbanView';
import { KiboKanbanPage } from './pages/KiboKanbanPage';
import { KiboGanttPage } from './pages/KiboGanttPage';
import { KiboCalendarPage } from './pages/KiboCalendarPage';
import { Button } from './components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from './components/ui/card';
import { Badge } from './components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from './components/ui/avatar';
import { Input } from './components/ui/input';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from './components/ui/dropdown-menu';
import { FolderKanban, CheckSquare, Users, Calendar, Settings, Search, Plus, LogOut, User, MoreVertical, BarChart3 } from 'lucide-react';
import { cn } from './lib/utils';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
      <div className="min-h-screen bg-background">
        {/* Header */}
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <div className="container flex h-14 items-center">
            <div className="mr-4 flex">
              <Link to="/" className="mr-6 flex items-center space-x-2">
                <CheckSquare className="h-6 w-6" />
                <span className="hidden font-bold sm:inline-block">Taskava</span>
              </Link>
              <nav className="flex items-center space-x-6 text-sm font-medium">
                <Link to="/" className="transition-colors hover:text-foreground/80 text-foreground">
                  Dashboard
                </Link>
                <Link to="/projects" className="transition-colors hover:text-foreground/80 text-muted-foreground">
                  Projects
                </Link>
                <Link to="/tasks" className="transition-colors hover:text-foreground/80 text-muted-foreground">
                  Tasks
                </Link>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="sm" className="h-auto px-2 py-1.5 text-sm font-medium">
                      Kibo UI
                      <MoreVertical className="ml-1 h-3 w-3" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="start">
                    <DropdownMenuLabel>Kibo UI Components</DropdownMenuLabel>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Link to="/kibo-kanban" className="flex items-center">
                        <FolderKanban className="mr-2 h-4 w-4" />
                        Kanban Board
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild>
                      <Link to="/kibo-gantt" className="flex items-center">
                        <BarChart3 className="mr-2 h-4 w-4" />
                        Gantt Chart
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild>
                      <Link to="/kibo-calendar" className="flex items-center">
                        <Calendar className="mr-2 h-4 w-4" />
                        Calendar View
                      </Link>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </nav>
            </div>
            <div className="flex flex-1 items-center justify-between space-x-2 md:justify-end">
              <div className="w-full flex-1 md:w-auto md:flex-none">
                <div className="relative">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="Search..."
                    className="pl-8 md:w-[300px] lg:w-[300px]"
                  />
                </div>
              </div>
              <Button variant="default" size="sm">
                <Plus className="h-4 w-4 mr-1" />
                New Project
              </Button>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                    <Avatar className="h-8 w-8">
                      <AvatarImage src="https://api.dicebear.com/7.x/avataaars/svg?seed=demo" alt="User" />
                      <AvatarFallback>U</AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">Demo User</p>
                      <p className="text-xs leading-none text-muted-foreground">demo@taskava.com</p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem>
                    <User className="mr-2 h-4 w-4" />
                    <span>Profile</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem>
                    <Settings className="mr-2 h-4 w-4" />
                    <span>Settings</span>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem>
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Log out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="container py-6">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/tasks" element={<Tasks />} />
            <Route path="/tasks-demo" element={<TasksDemo />} />
            <Route path="/test" element={<TestStyling />} />
            {/* Kibo UI Components */}
            <Route path="/kibo-kanban" element={<KiboKanbanPage />} />
            <Route path="/kibo-gantt" element={<KiboGanttPage />} />
            <Route path="/kibo-calendar" element={<KiboCalendarPage />} />
          </Routes>
        </main>
      </div>
    </Router>
    <ReactQueryDevtools initialIsOpen={false} />
  </QueryClientProvider>
  );
}

function Dashboard() {
  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">Welcome back! Here's an overview of your workspace.</p>
      </div>
      
      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Projects</CardTitle>
            <FolderKanban className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">12</div>
            <p className="text-xs text-muted-foreground">+2 from last month</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Open Tasks</CardTitle>
            <CheckSquare className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">48</div>
            <p className="text-xs text-muted-foreground">+12% from last week</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Team Members</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">8</div>
            <p className="text-xs text-muted-foreground">2 pending invites</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Due This Week</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">7</div>
            <p className="text-xs text-muted-foreground">3 high priority</p>
          </CardContent>
        </Card>
      </div>

      {/* Recent Projects */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Projects</CardTitle>
          <CardDescription>Your most recently updated projects</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {['Website Redesign', 'Mobile App', 'Marketing Campaign'].map((project, i) => (
            <div key={project} className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div className={cn("h-2 w-2 rounded-full", i === 0 ? "bg-green-500" : i === 1 ? "bg-blue-500" : "bg-yellow-500")} />
                <div>
                  <p className="font-medium">{project}</p>
                  <p className="text-sm text-muted-foreground">Updated 2 hours ago</p>
                </div>
              </div>
              <div className="flex items-center space-x-2">
                <Badge variant={i === 0 ? "default" : "secondary"}>Active</Badge>
                <Button variant="ghost" size="sm">
                  <MoreVertical className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}

function Projects() {
  const projects = [
    { id: 1, name: 'Website Redesign', status: 'Active', progress: 65, team: 'Design Team' },
    { id: 2, name: 'Mobile App Development', status: 'Active', progress: 40, team: 'Dev Team' },
    { id: 3, name: 'Marketing Campaign', status: 'Planning', progress: 15, team: 'Marketing' },
    { id: 4, name: 'Data Migration', status: 'Completed', progress: 100, team: 'IT Team' },
  ];

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Projects</h2>
          <p className="text-muted-foreground">Manage and track all your projects</p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Create Project
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {projects.map((project) => (
          <Card key={project.id} className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg">{project.name}</CardTitle>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="sm">
                      <MoreVertical className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem>Edit</DropdownMenuItem>
                    <DropdownMenuItem>Archive</DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem className="text-destructive">Delete</DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
              <CardDescription>{project.team}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Progress</span>
                  <span className="font-medium">{project.progress}%</span>
                </div>
                <div className="h-2 w-full bg-secondary rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-primary transition-all"
                    style={{ width: `${project.progress}%` }}
                  />
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-between">
              <Badge variant={project.status === 'Active' ? 'default' : project.status === 'Planning' ? 'secondary' : 'outline'}>
                {project.status}
              </Badge>
              <Button variant="ghost" size="sm">
                View Details
              </Button>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  );
}

function Tasks() {
  return <KanbanView />;
}

export default App;