import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import './index.css';

// Layout
import { MainLayout } from './components/layout/MainLayout';

// Pages
import { Dashboard } from './pages/Dashboard';
import { Projects } from './pages/Projects';
import { TaskBoard } from './pages/TaskBoard';
import { Login } from './pages/Login';

// Store
import { useAuthStore } from './store/useAuthStore';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      gcTime: 1000 * 60 * 10, // 10 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function AppContent() {
  const { isAuthenticated, initAuth, isLoading } = useAuthStore();
  
  useEffect(() => {
    initAuth();
  }, []);
  
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }
  
  // For now, we'll bypass authentication to show the UI
  // In production, you'd check isAuthenticated and redirect to login
  const shouldShowApp = true; // Change to isAuthenticated in production
  
  if (!shouldShowApp) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }
  
  return (
    <Routes>
      <Route path="/" element={<MainLayout><Dashboard /></MainLayout>} />
      <Route path="/projects" element={<MainLayout><Projects /></MainLayout>} />
      <Route path="/projects/:id" element={<MainLayout><TaskBoard /></MainLayout>} />
      <Route path="/tasks" element={<MainLayout><TaskBoard /></MainLayout>} />
      <Route path="/teams" element={<MainLayout><div className="p-6"><h1 className="text-3xl font-bold">Teams</h1><p className="text-gray-600 mt-2">Team management coming soon...</p></div></MainLayout>} />
      <Route path="/calendar" element={<MainLayout><div className="p-6"><h1 className="text-3xl font-bold">Calendar</h1><p className="text-gray-600 mt-2">Calendar view coming soon...</p></div></MainLayout>} />
      <Route path="/login" element={<Login />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <AppContent />
      </Router>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}

export default App;