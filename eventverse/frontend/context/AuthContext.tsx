// context/AuthContext.tsx
"use client";
import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '@/lib/api';
import { useRouter } from 'next/navigation';

interface User {
  id: number;
  email: string;
  createdAt?: string;
}

interface AuthContextType {
  user: User | null;
  login: (token: string) => void;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('token');
      // Only attempt /auth/me when a non-empty token is present
      if (token && token !== 'undefined' && token !== 'null') {
        try {
          const { data } = await api.get('/auth/me');
          setUser(data);
        } catch (error: any) {
          // For unauthenticated state, quietly clear the token without noisy console
          if (error?.response?.status === 401) {
            localStorage.removeItem('token');
          } else {
            console.error("Session check failed:", error);
          }
          localStorage.removeItem('token');
        }
      }
      setLoading(false);
    };
    checkAuth();
  }, []);

  const login = async (token: string) => {
    try {
      localStorage.setItem('token', token);
      const { data } = await api.get('/auth/me');
      setUser(data);
      router.push('/events');
    } catch (error: any) {
      console.error('Failed to fetch user profile:', error);
      // If /auth/me fails, clear token and show error
      localStorage.removeItem('token');
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    router.push('/login');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

