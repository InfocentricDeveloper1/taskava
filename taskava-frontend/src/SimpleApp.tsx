import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './index.css';

function SimpleApp() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        {/* Header */}
        <header className="bg-white shadow-sm border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center h-16">
              <div className="flex items-center">
                <h1 className="text-2xl font-bold text-gray-900">Taskava</h1>
                <nav className="ml-10 space-x-4">
                  <Link to="/" className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                    Dashboard
                  </Link>
                  <Link to="/projects" className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                    Projects
                  </Link>
                  <Link to="/tasks" className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                    Tasks
                  </Link>
                </nav>
              </div>
              <div className="flex items-center space-x-4">
                <button className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700">
                  New Project
                </button>
                <div className="w-8 h-8 bg-gray-300 rounded-full"></div>
              </div>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/tasks" element={<Tasks />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

function Dashboard() {
  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-8">Dashboard</h2>
      
      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">Active Projects</h3>
          <p className="text-3xl font-bold text-blue-600 mt-2">12</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">Open Tasks</h3>
          <p className="text-3xl font-bold text-green-600 mt-2">48</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">Team Members</h3>
          <p className="text-3xl font-bold text-purple-600 mt-2">8</p>
        </div>
      </div>

      {/* Recent Projects */}
      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b">
          <h3 className="text-lg font-medium text-gray-900">Recent Projects</h3>
        </div>
        <div className="p-6">
          <div className="space-y-4">
            {['Website Redesign', 'Mobile App', 'Marketing Campaign'].map((project) => (
              <div key={project} className="flex items-center justify-between p-4 bg-gray-50 rounded-md">
                <div>
                  <h4 className="font-medium text-gray-900">{project}</h4>
                  <p className="text-sm text-gray-500">Updated 2 hours ago</p>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded-full">
                    Active
                  </span>
                  <button className="text-gray-400 hover:text-gray-600">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
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
    <div>
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-900">Projects</h2>
        <button className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700">
          Create Project
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {projects.map((project) => (
          <div key={project.id} className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow">
            <div className="p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-2">{project.name}</h3>
              <p className="text-sm text-gray-500 mb-4">{project.team}</p>
              
              {/* Progress Bar */}
              <div className="mb-4">
                <div className="flex justify-between text-sm text-gray-600 mb-1">
                  <span>Progress</span>
                  <span>{project.progress}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-blue-600 h-2 rounded-full"
                    style={{ width: `${project.progress}%` }}
                  ></div>
                </div>
              </div>

              <div className="flex justify-between items-center">
                <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                  project.status === 'Active' ? 'bg-green-100 text-green-800' :
                  project.status === 'Planning' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {project.status}
                </span>
                <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                  View Details →
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function Tasks() {
  const taskColumns = {
    todo: [
      { id: 1, title: 'Design landing page', priority: 'High', assignee: 'JD' },
      { id: 2, title: 'Set up database', priority: 'Medium', assignee: 'AK' },
    ],
    inProgress: [
      { id: 3, title: 'Implement authentication', priority: 'High', assignee: 'SM' },
      { id: 4, title: 'Create API endpoints', priority: 'Medium', assignee: 'JD' },
    ],
    done: [
      { id: 5, title: 'Project setup', priority: 'Low', assignee: 'AK' },
      { id: 6, title: 'Design system', priority: 'High', assignee: 'SM' },
    ],
  };

  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-8">Task Board</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* To Do Column */}
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-4">To Do ({taskColumns.todo.length})</h3>
          <div className="space-y-3">
            {taskColumns.todo.map((task) => (
              <TaskCard key={task.id} task={task} />
            ))}
          </div>
        </div>

        {/* In Progress Column */}
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-4">In Progress ({taskColumns.inProgress.length})</h3>
          <div className="space-y-3">
            {taskColumns.inProgress.map((task) => (
              <TaskCard key={task.id} task={task} />
            ))}
          </div>
        </div>

        {/* Done Column */}
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-4">Done ({taskColumns.done.length})</h3>
          <div className="space-y-3">
            {taskColumns.done.map((task) => (
              <TaskCard key={task.id} task={task} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function TaskCard({ task }: { task: any }) {
  const priorityColors = {
    High: 'bg-red-100 text-red-800',
    Medium: 'bg-yellow-100 text-yellow-800',
    Low: 'bg-green-100 text-green-800',
  };

  return (
    <div className="bg-white p-4 rounded-md shadow-sm hover:shadow-md transition-shadow cursor-pointer">
      <h4 className="font-medium text-gray-900 mb-2">{task.title}</h4>
      <div className="flex justify-between items-center">
        <span className={`px-2 py-1 text-xs font-medium rounded-full ${priorityColors[task.priority as keyof typeof priorityColors]}`}>
          {task.priority}
        </span>
        <div className="w-6 h-6 bg-gray-300 rounded-full flex items-center justify-center text-xs font-medium text-gray-600">
          {task.assignee}
        </div>
      </div>
    </div>
  );
}

export default SimpleApp;