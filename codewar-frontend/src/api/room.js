import api from "./axios";

export const createRoom = () => {
  return api.post("/api/room/create");
};

export const joinRoom = (roomCode) => {
  return api.post(`/api/room/join/${roomCode}`);
};

export const getRoom = (roomCode) => {
  return api.get(`/api/room/${roomCode}`);
};

export const getMyRooms = () => {
  return api.get("/api/room/my-rooms");
};

export const updateRoomStatus = (roomCode, status) => {
  return api.put(`/api/room/${roomCode}/status?status=${status}`);
};

export const getRoomRatings = (roomCode) => {
  return api.get(`/api/room/${roomCode}/ratings`);
};

export const getProblemRating = (roomCode) => {
  return api.get(`/api/room/${roomCode}/problem-rating`);
};

export const pickProblem = (roomCode) => {
  return api.post(`/api/room/${roomCode}/pick-problem`);
};

export const getRoomProblems = (roomCode) => {
  return api.get(`/api/room/${roomCode}/problems`);
};

export const getCurrentProblem = (roomCode) => {
  return api.get(`/api/room/${roomCode}/current-problem`);
};
