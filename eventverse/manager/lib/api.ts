import axios from "axios";

// Base URL for talking to the backend / API gateway.
// In Docker, this is provided via NEXT_PUBLIC_API_BASE_URL (see docker-compose).
// For local dev, default to http://localhost:8085 where the gateway runs.
const API_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8085";
const ADMIN_BEARER =
  process.env.NEXT_PUBLIC_ADMIN_BEARER ||
  "Bearer dev-super-admin-token"; // fallback for local dev

const api = axios.create({
  baseURL: API_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 10000,
  withCredentials: true,
});

// Ensure manager UI always presents as super-admin for local/dev usage.
api.interceptors.request.use((config) => {
  config.headers = config.headers ?? {};
  if (!config.headers["Authorization"]) {
    config.headers["Authorization"] = ADMIN_BEARER;
  }
  if (!config.headers["X-Admin-Superuser"]) {
    config.headers["X-Admin-Superuser"] = "true";
  }
  return config;
});

export default api;

