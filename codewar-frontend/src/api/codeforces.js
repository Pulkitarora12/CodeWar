import api from "./axios";

export const generateCfToken = (handle) => {
  return api.post(`/api/codeforces/generate-token?handle=${encodeURIComponent(handle)}`);
};

export const verifyCfHandle = () => {
  return api.post("/api/codeforces/verify");
};

export const unlinkCfHandle = () => {
  return api.delete("/api/codeforces/unlink");
};

export const getCfStatus = () => {
  return api.get("/api/codeforces/status");
};

export const getCfRatingByUserId = (userId) => {
  return api.get(`/api/codeforces/rating/user/${userId}`);
};
