import { create } from 'zustand';
import { User, Workspace } from '@/types';
import { authApi, workspaceApi } from '@/services/api';
import { useMockDataStore } from './useMockData';

interface AuthState {
  user: User | null;
  token: string | null;
  currentWorkspace: Workspace | null;
  workspaces: Workspace[];
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // Actions
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  setCurrentWorkspace: (workspace: Workspace) => void;
  fetchWorkspaces: () => Promise<void>;
  initAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
      user: null,
      token: null,
      currentWorkspace: null,
      workspaces: [],
      isAuthenticated: false,
      isLoading: false,
      
      login: async (email: string, password: string) => {
        set({ isLoading: true });
        try {
          const response = await authApi.login(email, password);
          if (response.success && response.data) {
            const { token, user } = response.data;
            localStorage.setItem('authToken', token);
            set({ 
              user, 
              token, 
              isAuthenticated: true,
              isLoading: false 
            });
            
            // Fetch workspaces after login
            await get().fetchWorkspaces();
          }
        } catch (error) {
          console.error('Login failed:', error);
          set({ isLoading: false });
          throw error;
        }
      },
      
      logout: () => {
        localStorage.removeItem('authToken');
        set({ 
          user: null, 
          token: null, 
          currentWorkspace: null,
          workspaces: [],
          isAuthenticated: false 
        });
      },
      
      setCurrentWorkspace: (workspace: Workspace) => {
        set({ currentWorkspace: workspace });
      },
      
      fetchWorkspaces: async () => {
        try {
          const response = await workspaceApi.getAll();
          if (response.success && response.data) {
            set({ 
              workspaces: response.data,
              currentWorkspace: response.data[0] || null 
            });
          }
        } catch (error) {
          console.error('Failed to fetch workspaces:', error);
        }
      },
      
      initAuth: async () => {
        const token = localStorage.getItem('authToken');
        
        // For demo, use mock data
        const { mockUser, mockWorkspace } = useMockDataStore.getState();
        set({ 
          user: mockUser,
          token: 'demo-token',
          currentWorkspace: mockWorkspace,
          workspaces: [mockWorkspace],
          isAuthenticated: true,
          isLoading: false 
        });
        
        // In production, use this:
        /*
        if (token) {
          set({ isLoading: true });
          try {
            const response = await authApi.getCurrentUser();
            if (response.success && response.data) {
              set({ 
                user: response.data, 
                token,
                isAuthenticated: true,
                isLoading: false 
              });
              await get().fetchWorkspaces();
            }
          } catch (error) {
            console.error('Failed to init auth:', error);
            get().logout();
          } finally {
            set({ isLoading: false });
          }
        }
        */
      },
}));