import { useEffect, useState } from 'react';
import { 
  FolderKanban, 
  CheckSquare, 
  Users, 
  TrendingUp,
  Clock,
  AlertCircle,
  Calendar
} from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { projectApi, taskApi } from '@/services/api';
import { Project, Task } from '@/types';
import { useAuthStore } from '@/store/useAuthStore';
import { useMockDataStore } from '@/store/useMockData';

export function Dashboard() {
  const { currentWorkspace } = useAuthStore();
  const [projects, setProjects] = useState<Project[]>([]);
  const [recentTasks, setRecentTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    if (currentWorkspace) {
      fetchDashboardData();
    }
  }, [currentWorkspace]);
  
  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // Use mock data for demo
      const { mockProjects, mockTasks } = useMockDataStore.getState();
      setProjects(mockProjects);
      setRecentTasks(mockTasks.slice(0, 5));
      
      // In production, use this:
      /*
      const projectsResponse = await projectApi.getAll(currentWorkspace?.id);
      setProjects(projectsResponse.content || []);
      
      const tasksResponse = await taskApi.getAll({ size: 5 });
      setRecentTasks(tasksResponse.content || []);
      */
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const stats = [
    {
      title: 'Total Projects',
      value: projects.length,
      icon: FolderKanban,
      color: 'text-blue-600 bg-blue-100',
      change: '+12%'
    },
    {
      title: 'Active Tasks',
      value: recentTasks.filter(t => t.status !== 'done').length,
      icon: CheckSquare,
      color: 'text-green-600 bg-green-100',
      change: '+5%'
    },
    {
      title: 'Team Members',
      value: 24,
      icon: Users,
      color: 'text-purple-600 bg-purple-100',
      change: '+2'
    },
    {
      title: 'Completion Rate',
      value: '78%',
      icon: TrendingUp,
      color: 'text-orange-600 bg-orange-100',
      change: '+15%'
    },
  ];
  
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'urgent': return 'destructive';
      case 'high': return 'destructive';
      case 'medium': return 'default';
      case 'low': return 'secondary';
      default: return 'outline';
    }
  };
  
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'done': return 'bg-green-100 text-green-800';
      case 'in_progress': return 'bg-blue-100 text-blue-800';
      case 'in_review': return 'bg-purple-100 text-purple-800';
      case 'blocked': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }
  
  return (
    <div className="p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-2">
          Welcome back! Here's what's happening with your projects today.
        </p>
      </div>
      
      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {stats.map((stat) => (
          <Card key={stat.title}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <div className={`p-2 rounded-lg ${stat.color}`}>
                <stat.icon className="h-4 w-4" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-green-600 mt-1">
                {stat.change} from last month
              </p>
            </CardContent>
          </Card>
        ))}
      </div>
      
      {/* Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Projects */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Recent Projects</CardTitle>
              <CardDescription>Your active projects and their progress</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {projects.length > 0 ? (
                  projects.slice(0, 5).map((project) => (
                    <div key={project.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50 transition-colors">
                      <div className="flex items-center space-x-4">
                        <div className={`w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center`}>
                          <FolderKanban className="h-5 w-5 text-purple-600" />
                        </div>
                        <div>
                          <h4 className="font-semibold">{project.name}</h4>
                          <p className="text-sm text-gray-600">{project.description || 'No description'}</p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-4">
                        <div className="text-right">
                          <p className="text-sm font-medium">{project.taskCount || 0} tasks</p>
                          <div className="w-24 bg-gray-200 rounded-full h-2 mt-1">
                            <div 
                              className="bg-purple-600 h-2 rounded-full" 
                              style={{ width: `${project.completedTaskCount && project.taskCount ? (project.completedTaskCount / project.taskCount) * 100 : 0}%` }}
                            />
                          </div>
                        </div>
                        <Badge variant={project.status === 'active' ? 'default' : 'secondary'}>
                          {project.status}
                        </Badge>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <FolderKanban className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                    <p>No projects yet</p>
                    <Button className="mt-4" size="sm">Create your first project</Button>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
        
        {/* Recent Tasks */}
        <div>
          <Card>
            <CardHeader>
              <CardTitle>Recent Tasks</CardTitle>
              <CardDescription>Tasks that need your attention</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {recentTasks.length > 0 ? (
                  recentTasks.map((task) => (
                    <div key={task.id} className="border-l-2 border-purple-600 pl-3 py-2">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <h5 className="font-medium text-sm line-clamp-1">{task.title}</h5>
                          <div className="flex items-center gap-2 mt-1">
                            <Badge variant={getPriorityColor(task.priority)} className="text-xs">
                              {task.priority}
                            </Badge>
                            <span className={`text-xs px-2 py-0.5 rounded-full ${getStatusColor(task.status)}`}>
                              {task.status.replace('_', ' ')}
                            </span>
                          </div>
                        </div>
                      </div>
                      {task.dueDate && (
                        <div className="flex items-center text-xs text-gray-500 mt-2">
                          <Calendar className="h-3 w-3 mr-1" />
                          Due {new Date(task.dueDate).toLocaleDateString()}
                        </div>
                      )}
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <CheckSquare className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                    <p className="text-sm">No tasks assigned</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
          
          {/* Upcoming Deadlines */}
          <Card className="mt-6">
            <CardHeader>
              <CardTitle className="flex items-center">
                <Clock className="h-4 w-4 mr-2" />
                Upcoming Deadlines
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <AlertCircle className="h-4 w-4 text-red-500 mr-2" />
                    <span>Project Alpha Launch</span>
                  </div>
                  <span className="text-red-600 font-medium">Today</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <Clock className="h-4 w-4 text-orange-500 mr-2" />
                    <span>Design Review</span>
                  </div>
                  <span className="text-orange-600 font-medium">Tomorrow</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <Clock className="h-4 w-4 text-gray-400 mr-2" />
                    <span>Sprint Planning</span>
                  </div>
                  <span className="text-gray-600">In 3 days</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}