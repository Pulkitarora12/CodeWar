import axios from "axios";
import { BASE_URL } from "../utils/constants";

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor — attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("cw_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let isRedirecting = false;

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      if (!isRedirecting) {
        isRedirecting = true;
        localStorage.removeItem("cw_token");
        localStorage.removeItem("cw_user");
        const publicPaths = ["/login", "/register", "/forgot-password", "/reset-password"];
        if (!publicPaths.some((p) => window.location.pathname.startsWith(p))) {
          window.location.href = "/login";
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
