import { useEffect, useState } from 'react';
import { 
  FolderKanban, 
  Plus, 
  MoreVertical, 
  Users,
  Calendar,
  CheckCircle,
  Grid,
  List,
  Search
} from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../components/ui/dropdown-menu';
import { projectApi } from '../services/api';
import { Project } from '../types';
import { useAuthStore } from '../store/useAuthStore';
import { useMockDataStore } from '../store/useMockData';
import { useNavigate } from 'react-router-dom';

export function Projects() {
  const navigate = useNavigate();
  const { currentWorkspace } = useAuthStore();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [searchTerm, setSearchTerm] = useState('');
  
  useEffect(() => {
    if (currentWorkspace) {
      fetchProjects();
    }
  }, [currentWorkspace]);
  
  const fetchProjects = async () => {
    setLoading(true);
    try {
      // Use mock data for demo
      const { mockProjects } = useMockDataStore.getState();
      setProjects(mockProjects);
      
      // In production, use this:
      /*
      const response = await projectApi.getAll(currentWorkspace?.id);
      setProjects(response.content || []);
      */
    } catch (error) {
      console.error('Failed to fetch projects:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const filteredProjects = projects.filter(project => 
    project.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    project.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );
  
  const getProjectProgress = (project: Project) => {
    if (!project.taskCount) return 0;
    return Math.round((project.completedTaskCount || 0) / project.taskCount * 100);
  };
  
  const ProjectCard = ({ project }: { project: Project }) => (
    <Card 
      className="hover:shadow-lg transition-all cursor-pointer group"
      onClick={() => navigate(`/projects/${project.id}`)}
    >
      <CardHeader>
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-3">
            <div className={`w-12 h-12 rounded-lg bg-purple-100 flex items-center justify-center`}>
              <FolderKanban className="h-6 w-6 text-purple-600" />
            </div>
            <div>
              <CardTitle className="text-lg">{project.name}</CardTitle>
              <CardDescription className="line-clamp-2 mt-1">
                {project.description || 'No description provided'}
              </CardDescription>
            </div>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
              <Button variant="ghost" size="icon" className="opacity-0 group-hover:opacity-100 transition-opacity">
                <MoreVertical className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>Actions</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation();
                navigate(`/projects/${project.id}`);
              }}>
                View Details
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation();
                navigate(`/projects/${project.id}/settings`);
              }}>
                Settings
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => e.stopPropagation()}>
                Duplicate
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem 
                className="text-red-600" 
                onClick={(e) => e.stopPropagation()}
              >
                Archive Project
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent>
        {/* Progress Bar */}
        <div className="mb-4">
          <div className="flex justify-between text-sm text-gray-600 mb-2">
            <span>Progress</span>
            <span>{getProjectProgress(project)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div 
              className="bg-gradient-to-r from-purple-500 to-purple-600 h-2 rounded-full transition-all"
              style={{ width: `${getProjectProgress(project)}%` }}
            />
          </div>
        </div>
        
        {/* Stats */}
        <div className="grid grid-cols-3 gap-4 text-sm">
          <div className="flex items-center space-x-2">
            <CheckCircle className="h-4 w-4 text-gray-400" />
            <span className="text-gray-600">{project.taskCount || 0} tasks</span>
          </div>
          <div className="flex items-center space-x-2">
            <Users className="h-4 w-4 text-gray-400" />
            <span className="text-gray-600">{project.teamIds?.length || 0} teams</span>
          </div>
          <div className="flex items-center space-x-2">
            <Calendar className="h-4 w-4 text-gray-400" />
            <span className="text-gray-600">
              {new Date(project.updatedAt).toLocaleDateString()}
            </span>
          </div>
        </div>
        
        {/* Status Badge */}
        <div className="mt-4 flex justify-between items-center">
          <Badge 
            variant={project.status === 'active' ? 'default' : project.status === 'completed' ? 'secondary' : 'outline'}
          >
            {project.status}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
  
  const ProjectListItem = ({ project }: { project: Project }) => (
    <div 
      className="bg-white border rounded-lg p-4 hover:shadow-md transition-all cursor-pointer"
      onClick={() => navigate(`/projects/${project.id}`)}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4 flex-1">
          <div className={`w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center`}>
            <FolderKanban className="h-5 w-5 text-purple-600" />
          </div>
          <div className="flex-1">
            <h3 className="font-semibold">{project.name}</h3>
            <p className="text-sm text-gray-600 line-clamp-1">
              {project.description || 'No description'}
            </p>
          </div>
          <div className="flex items-center space-x-6">
            <div className="text-sm text-gray-600">
              <span className="font-medium">{project.taskCount || 0}</span> tasks
            </div>
            <div className="text-sm text-gray-600">
              <span className="font-medium">{project.teamIds?.length || 0}</span> teams
            </div>
            <div className="w-32">
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-purple-600 h-2 rounded-full"
                  style={{ width: `${getProjectProgress(project)}%` }}
                />
              </div>
              <p className="text-xs text-gray-600 mt-1">{getProjectProgress(project)}% complete</p>
            </div>
            <Badge 
              variant={project.status === 'active' ? 'default' : project.status === 'completed' ? 'secondary' : 'outline'}
            >
              {project.status}
            </Badge>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
              <Button variant="ghost" size="icon">
                <MoreVertical className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation();
                navigate(`/projects/${project.id}`);
              }}>
                View Details
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => e.stopPropagation()}>
                Settings
              </DropdownMenuItem>
              <DropdownMenuItem className="text-red-600" onClick={(e) => e.stopPropagation()}>
                Archive
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>
  );
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading projects...</p>
        </div>
      </div>
    );
  }
  
  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Projects</h1>
          <p className="text-gray-600 mt-2">
            Manage and organize your team's projects
          </p>
        </div>
        <Button>
          <Plus className="h-4 w-4 mr-2" />
          New Project
        </Button>
      </div>
      
      {/* Filters and View Toggle */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center space-x-4 flex-1 max-w-md">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <Input
              type="search"
              placeholder="Search projects..."
              className="pl-10"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant={viewMode === 'grid' ? 'default' : 'ghost'}
            size="icon"
            onClick={() => setViewMode('grid')}
          >
            <Grid className="h-4 w-4" />
          </Button>
          <Button
            variant={viewMode === 'list' ? 'default' : 'ghost'}
            size="icon"
            onClick={() => setViewMode('list')}
          >
            <List className="h-4 w-4" />
          </Button>
        </div>
      </div>
      
      {/* Projects Display */}
      {filteredProjects.length > 0 ? (
        viewMode === 'grid' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredProjects.map((project) => (
              <ProjectCard key={project.id} project={project} />
            ))}
          </div>
        ) : (
          <div className="space-y-3">
            {filteredProjects.map((project) => (
              <ProjectListItem key={project.id} project={project} />
            ))}
          </div>
        )
      ) : (
        <div className="text-center py-12">
          <FolderKanban className="h-16 w-16 mx-auto text-gray-300 mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            {searchTerm ? 'No projects found' : 'No projects yet'}
          </h3>
          <p className="text-gray-600 mb-6">
            {searchTerm 
              ? 'Try adjusting your search terms' 
              : 'Create your first project to get started'}
          </p>
          {!searchTerm && (
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              Create Project
            </Button>
          )}
        </div>
      )}
    </div>
  );
}