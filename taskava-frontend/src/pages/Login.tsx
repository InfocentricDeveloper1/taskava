import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../components/ui/card';
import { useAuthStore } from '../store/useAuthStore';
import { CheckSquare } from 'lucide-react';

export function Login() {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      // For demo purposes, we'll simulate a login
      // In production, this would call the actual login API
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Simulate successful login
      localStorage.setItem('authToken', 'demo-token');
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  const handleDemoLogin = () => {
    setEmail('demo@taskava.com');
    setPassword('demo123');
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-50 to-purple-100">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center mb-4">
            <div className="bg-purple-600 p-3 rounded-lg">
              <CheckSquare className="h-8 w-8 text-white" />
            </div>
          </div>
          <h1 className="text-3xl font-bold text-gray-900">Taskava</h1>
          <p className="text-gray-600 mt-2">Enterprise Project Management</p>
        </div>
        
        {/* Login Card */}
        <Card>
          <CardHeader>
            <CardTitle>Sign in to your account</CardTitle>
            <CardDescription>
              Enter your email and password to access your workspace
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                  Email address
                </label>
                <Input
                  id="email"
                  type="email"
                  placeholder="you@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                  Password
                </label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              
              {error && (
                <div className="bg-red-50 text-red-600 text-sm p-3 rounded-md">
                  {error}
                </div>
              )}
              
              <div className="flex items-center justify-between">
                <label className="flex items-center">
                  <input type="checkbox" className="rounded border-gray-300" />
                  <span className="ml-2 text-sm text-gray-600">Remember me</span>
                </label>
                <a href="#" className="text-sm text-purple-600 hover:text-purple-700">
                  Forgot password?
                </a>
              </div>
              
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Signing in...' : 'Sign in'}
              </Button>
            </form>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <div className="relative w-full">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-white px-2 text-gray-500">Or</span>
              </div>
            </div>
            
            <Button 
              type="button" 
              variant="outline" 
              className="w-full"
              onClick={handleDemoLogin}
            >
              Try Demo Account
            </Button>
            
            <p className="text-center text-sm text-gray-600">
              Don't have an account?{' '}
              <a href="#" className="text-purple-600 hover:text-purple-700 font-medium">
                Sign up
              </a>
            </p>
          </CardFooter>
        </Card>
        
        {/* Footer */}
        <p className="text-center text-xs text-gray-500 mt-8">
          © 2024 Taskava. All rights reserved.
        </p>
      </div>
    </div>
  );
}