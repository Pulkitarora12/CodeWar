import React, { useState, useEffect, useCallback, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getContestDetails, checkSubmission, endContest } from "../api/contest";
import { getRoom } from "../api/room";
import SockJS from "sockjs-client";
import { over } from "stompjs";
import { BASE_URL } from "../utils/constants";

const Contest = () => {
  const { roomCode, contestId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [problem, setProblem] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [contestStatus, setContestStatus] = useState("");
  const [isHost, setIsHost] = useState(false);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");
  const stompClientRef = useRef(null);

  const fetchContestData = useCallback(async () => {
    try {
      const roomRes = await getRoom(roomCode);
      const hostName = roomRes.data?.createdBy;
      setIsHost(user?.username === hostName || user?.userName === hostName);

      const res = await getContestDetails(contestId);
      setProblem({
        problemName: res.data.problemName,
        rating: res.data.rating,
        contestId: res.data.cfContestId,
        problemIndex: res.data.problemIndex,
        problemUrl: res.data.problemUrl
      });
      setLeaderboard(res.data.leaderboard || []);
      setContestStatus(res.data.status);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load contest data.");
    } finally {
      setLoading(false);
    }
  }, [contestId]);

  // Initial data fetch
  useEffect(() => {
    fetchContestData();
  }, [fetchContestData]);

  // WebSocket connection for real-time leaderboard updates
  useEffect(() => {
    const socket = new SockJS(`${BASE_URL}/ws-leaderboard`);
    const stompClient = over(socket);
    stompClient.debug = null; // Disable noisy STOMP debug logs

    stompClient.connect({}, () => {
      stompClient.subscribe(
        `/topic/contest/${contestId}/leaderboard`,
        (message) => {
          const data = JSON.parse(message.body);
          setLeaderboard(data.entries || []);
        }
      );
    }, (err) => {
      console.error("WebSocket connection error:", err);
    });

    stompClientRef.current = stompClient;

    return () => {
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.disconnect();
      }
    };
  }, [contestId]);

  const handleCheckSubmission = async () => {
    setActionLoading(true);
    try {
      await checkSubmission(contestId);
      await fetchContestData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to check submissions.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleEndContest = async () => {
    setActionLoading(true);
    try {
      await endContest(contestId);
      navigate(`/room/${roomCode}`);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to end contest.");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading && !problem) {
    return (
      <div className="page-center">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div className="room-page">
      <div className="room-header">
        <div className="room-header-top">
          <div>
            <h1 className="room-title">
              Contest <span className="accent">#{contestId}</span>
            </h1>
            <p className="room-host">
              Room Code: <strong>{roomCode}</strong>
            </p>
          </div>
          <div className="room-header-actions">
            <button
              className="btn btn-outline"
              onClick={() => navigate(`/room/${roomCode}`)}
            >
              Back to Room
            </button>
            {contestStatus !== "COMPLETED" && (
              <>
                <button
                  className="btn btn-primary"
                  style={{ marginLeft: 8 }}
                  onClick={handleCheckSubmission}
                  disabled={actionLoading}
                >
                  {actionLoading ? "Syncing..." : "🔄 Refresh Submissions"}
                </button>
                {isHost && (
                  <button
                    className="btn btn-outline btn-danger"
                    style={{ marginLeft: 8 }}
                    onClick={handleEndContest}
                    disabled={actionLoading}
                  >
                    🏁 End Contest
                  </button>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: 20 }}>
          {error}
        </div>
      )}

      {/* Active Problem Segment */}
      <div className="room-section problem-display" style={{ marginBottom: "32px", background: "var(--card)" }}>
        <h2 className="room-section-title">
          <span>🎯</span> Target Problem
        </h2>
        {problem ? (
          <div className="problem-card" style={{ background: "var(--bg)", border: "1px solid var(--border)" }}>
            <div className="problem-header">
              <h3>{problem.problemName}</h3>
              <span className="badge badge-waiting">
                Rating: {problem.rating || "N/A"}
              </span>
            </div>
            <p className="text-muted" style={{ marginBottom: "16px" }}>
              Contest: {problem.contestId} | Index: {problem.problemIndex}
            </p>
            <a
              href={problem.problemUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-primary"
            >
              Solve on Codeforces ↗
            </a>
          </div>
        ) : (
          <p className="text-muted">No active problem found.</p>
        )}
      </div>

      {/* Leaderboard Segment */}
      <div className="room-section">
        <h2 className="room-section-title">
          <span>🏆</span> Leaderboard
        </h2>
        <div className="participant-list" style={{ marginTop: 16 }}>
          {leaderboard.length > 0 ? (
            <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "left" }}>
              <thead>
                <tr style={{ borderBottom: "1px solid var(--border)", color: "var(--text-muted)" }}>
                  <th style={{ padding: "12px 0" }}>Rank</th>
                  <th style={{ padding: "12px 0" }}>Username</th>
                  <th style={{ padding: "12px 0" }}>Score</th>
                  <th style={{ padding: "12px 0" }}>Submissions</th>
                  <th style={{ padding: "12px 0" }}>Penalty</th>
                </tr>
              </thead>
              <tbody>
                {leaderboard.map((entry, idx) => (
                  <tr key={idx} style={{ borderBottom: "1px solid var(--border)" }}>
                    <td style={{ padding: "12px 0", fontWeight: "bold" }}>
                      {idx + 1}
                    </td>
                    <td style={{ padding: "12px 0", fontWeight: "bold", color: "var(--primary)" }}>
                      {entry.username || entry.user?.username || "Unknown"}
                    </td>
                    <td style={{ padding: "12px 0" }}>
                      <span className="badge badge-active">{entry.score || "0"}</span>
                    </td>
                    <td style={{ padding: "12px 0", color: "var(--text-muted)" }}>
                      {entry.submissionCount || 0}
                    </td>
                    <td style={{ padding: "12px 0", color: "var(--text-muted)" }}>
                      {entry.penalty || 0}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="text-muted" style={{ padding: "16px", background: "var(--bg)", borderRadius: 8, textAlign: "center" }}>
              No submissions or leaderboard data available yet.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Contest;

