// app/register/page.tsx
"use client";
import { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import api from '@/lib/api';
import { motion } from 'framer-motion';
import Link from 'next/link';

export default function RegisterPage() {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await api.post('/auth/register', formData);
      // Backend returns { accessToken: "..." }
      if (data.accessToken) {
        login(data.accessToken);
      } else {
        setError('Invalid response from server');
      }
    } catch (err: any) {
      console.error('Registration error caught in component:', err);
      
      // Handle different error types
      if (err.response) {
        const status = err.response.status;
        const errorMessage = err.response.data?.message || err.response.data?.error || err.response.statusText;
        
        if (status === 400) {
          setError(errorMessage || 'Email is already registered or invalid input');
        } else if (status === 401) {
          setError('Authentication failed. Please try again.');
        } else if (status === 403) {
          setError('Access forbidden. Please check your permissions.');
        } else if (status === 429) {
          setError('Too many requests. Please try again later.');
        } else if (status >= 500) {
          setError('Server error. Please try again later.');
        } else {
          setError(errorMessage || 'Registration failed. Please try again.');
        }
      } else if (err.isTimeoutError) {
        setError('Request timeout: The server did not respond in time. This might indicate the CORS preflight request is hanging. Please check if the backend services are running and CORS is properly configured.');
      } else if (err.isCorsError || (err.request && !err.response)) {
        setError('CORS error: Cannot connect to server. The backend may not be configured to allow requests from this origin. Please check CORS settings.');
      } else if (err.request) {
        setError('Cannot connect to server. Please check if the backend is running on port 8085.');
      } else {
        setError(err.message || 'An unexpected error occurred. Please try again.');
      }
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 relative text-foreground">
      <div className="absolute inset-0 bg-gradient-to-br from-[#1d130c] via-[#24160f] to-[#1a110b]" />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_80%_30%,rgba(232,195,158,0.2),transparent_25%)]" />
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="relative max-w-md w-full space-y-8 glass rounded-3xl border border-white/10 p-8 shadow-smooth"
      >
        <div className="text-center space-y-2">
          <p className="pill inline-flex px-3 py-1 rounded-full text-xs text-foreground">Create account</p>
          <h2 className="text-3xl font-semibold text-foreground">Join Samvaya</h2>
          <p className="text-sm text-muted">Save your favorites and book without friction.</p>
        </div>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <input
              type="email"
              required
              placeholder="Email"
              className="w-full px-4 py-3 rounded-xl bg-white/10 border border-white/20 focus:ring-2 focus:ring-[#c27a48]/50 outline-none text-foreground placeholder:text-muted"
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                required
                placeholder="Password"
                className="w-full px-4 py-3 pr-24 rounded-xl bg-white/10 border border-white/20 focus:ring-2 focus:ring-[#c27a48]/50 outline-none text-foreground placeholder:text-muted"
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="absolute inset-y-0 right-3 text-sm font-medium text-foreground hover:text-foreground/80"
              >
                {showPassword ? "Hide" : "Show"}
              </button>
            </div>
          </div>

          {error && <p className="text-red-300 text-sm text-center">{error}</p>}

          <button
            type="submit"
            className="w-full flex justify-center py-3 px-4 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-[#c27a48] via-[#e8c39e] to-[#8a5a44] shadow-lg shadow-orange-500/30 hover:-translate-y-[1px] transition-transform"
          >
            Register
          </button>

          <p className="text-center text-sm text-muted">
            Already have an account?{' '}
            <Link href="/login" className="text-foreground hover:text-foreground/80 font-medium">
              Login
            </Link>
          </p>
        </form>
      </motion.div>
    </div>
  );
}

