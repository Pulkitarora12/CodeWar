import api from "./axios";

export const loginUser = (username, password) => {
  return api.post("/api/auth/public/signin", { username, password });
};

export const registerUser = (username, email, password) => {
  return api.post("/api/auth/public/signup", { username, email, password });
};

export const forgotPassword = (email) => {
  return api.post(`/api/auth/public/forgot-password?email=${encodeURIComponent(email)}`);
};

export const resetPassword = (token, newPassword) => {
  return api.post(
    `/api/auth/public/reset-password?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`
  );
};

export const getCurrentUser = () => {
  return api.get("/api/auth/user");
};

export const getUsername = () => {
  return api.get("/api/auth/username");
};
