import api from "./axios";

export const startContest = (roomCode) => {
  return api.post(`/api/contest/start/${roomCode}`);
};

export const checkSubmission = (contestId) => {
  return api.post(`/api/contest/${contestId}/check-submission`);
};

export const endContest = (contestId) => {
  return api.post(`/api/contest/${contestId}/end`);
};

export const getLeaderboard = (contestId) => {
  return api.get(`/api/contest/${contestId}/leaderboard`);
};

export const getContestsByRoom = (roomCode) => {
  return api.get(`/api/contest/room/${roomCode}`);
};

export const getContestDetails = (contestId) => {
  return api.get(`/api/contest/${contestId}/details`);
};
