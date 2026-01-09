// lib/api.ts
import axios from 'axios';

const API_URL = 'http://localhost:8085';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Interceptor to add JWT to requests
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle timeout errors
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      console.error('=== Request Timeout Error ===');
      console.error('Error message:', error.message);
      console.error('Error code:', error.code);
      console.error('Request URL:', error.config?.baseURL + error.config?.url);
      console.error('Request method:', error.config?.method?.toUpperCase());
      console.error('Timeout configured:', error.config?.timeout, 'ms');
      console.error('Note: The server did not respond within the timeout period.');
      console.error('This could indicate:');
      console.error('  1. Server is not running or not accessible');
      console.error('  2. CORS preflight (OPTIONS) request is hanging');
      console.error('  3. Server is processing but taking too long');
      console.error('  4. Network connectivity issues');
      console.error('=====================================');
      
      error.isTimeoutError = true;
      error.message = 'Request timeout: Server did not respond. Please check if the backend is running and accessible.';
      return Promise.reject(error);
    }
    
    // Handle CORS errors (browser blocks request, no response received)
    if (!error.response && error.request && !error.isTimeoutError) {
      const isCorsError = error.message?.includes('Network Error') || 
                         error.message?.includes('CORS') ||
                         error.code === 'ERR_NETWORK' ||
                         error.code === 'ERR_CORS';
      
      if (isCorsError) {
        // Log error details separately to avoid serialization issues
        console.error('=== CORS or Network Error detected ===');
        console.error('Error message:', error.message);
        console.error('Error code:', error.code);
        console.error('Request URL:', error.config?.url);
        console.error('Request method:', error.config?.method);
        console.error('Request config:', {
          baseURL: error.config?.baseURL,
          url: error.config?.url,
          method: error.config?.method,
          headers: error.config?.headers
        });
        console.error('Error request object exists:', !!error.request);
        console.error('Error response object exists:', !!error.response);
        console.error('Note: If curl works but browser doesn\'t, this is likely a CORS issue. Check backend CORS configuration.');
        console.error('=====================================');
        
        error.isCorsError = true;
        error.message = 'CORS error: Cannot connect to server. Please check CORS configuration on the backend.';
        return Promise.reject(error);
      }
    }
    
    // Handle actual connection refused errors
    if (error.code === 'ECONNREFUSED') {
      console.error('Connection refused. Make sure the API gateway is running on port 8085');
      error.message = 'Cannot connect to server. Please check if the backend is running.';
      return Promise.reject(error);
    }
    
    // Handle response errors (server responded but with error status)
    if (error.response) {
      const errorInfo = {
        status: error.response.status,
        statusText: error.response.statusText,
        url: error.config?.url || 'unknown',
        method: error.config?.method?.toUpperCase() || 'unknown',
        data: error.response.data,
        message: error.message
      };
      
      // Log with better formatting
      console.error('API Error Details:', JSON.stringify(errorInfo, null, 2));
      
      // Also log individual parts for easier debugging
      console.error('Status:', error.response.status, error.response.statusText);
      console.error('URL:', error.config?.method?.toUpperCase(), error.config?.url);
      console.error('Response Data:', error.response.data);
    } else if (error.request) {
      // Request was made but no response received (could be CORS, timeout, or network issue)
      console.error('No response received:', {
        url: error.config?.url,
        method: error.config?.method,
        message: error.message,
        code: error.code,
        note: 'This could be a CORS issue, network problem, or server not responding'
      });
    } else {
      // Something else happened
      console.error('Request setup error:', error.message || error);
    }
    
    return Promise.reject(error);
  }
);

export default api;

