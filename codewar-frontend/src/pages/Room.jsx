import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getRoom,
  updateRoomStatus,
  getRoomRatings,
  getCurrentProblem,
  pickProblem,
} from "../api/room";
import { useAuth } from "../context/AuthContext";
import ParticipantList from "../components/ParticipantList";

const Room = () => {
  const { roomCode } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [cfRatings, setCfRatings] = useState([]);
  const [currentProblem, setCurrentProblem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [problemHistory, setProblemHistory] = useState([]);

  const fetchRoom = useCallback(async () => {
    try {
      const res = await getRoom(roomCode);
      setRoom(res.data);

      getRoomRatings(roomCode)
        .then((r) => {
          console.log("RATINGS API:", r.data);
          setCfRatings(r.data.participants || []);
        })
        .catch(() => setCfRatings([]));

      getCurrentProblem(roomCode)
        .then((r) => setCurrentProblem(r.data))
        .catch(() => setCurrentProblem(null));
    } catch (err) {
      setError(err.response?.data?.message || "Room not found.");
    } finally {
      setLoading(false);
    }
  }, [roomCode]);

  useEffect(() => {
    fetchRoom();
  }, [fetchRoom]);

  const handleStatusChange = async (newStatus) => {
    setActionLoading(true);
    try {
      await updateRoomStatus(roomCode, newStatus);
      await fetchRoom();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update status.");
    } finally {
      setActionLoading(false);
    }
  };

  const fetchProblemHistory = useCallback(async () => {
    try {
      const res = await getRoomProblems(roomCode);

      // Backend already returns list → just store it
      setProblemHistory(res.data.problems || []);
    } catch (err) {
      console.error("Failed to fetch problem history", err);
      setProblemHistory([]);
    }
  }, [roomCode]);

  const handlePickProblem = async () => {
    setActionLoading(true);
    setError("");
    try {
      const res = await pickProblem(roomCode);
      setCurrentProblem(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to pick problem.");
    } finally {
      setActionLoading(false);
    }
  };

  const copyInviteLink = () => {
    const link = `${window.location.origin}/join?code=${roomCode}`;
    navigator.clipboard.writeText(link);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (loading) {
    return (
      <div className="page-center">
        <div className="spinner" />
      </div>
    );
  }

  if (error && !room) {
    return (
      <div className="page-center">
        <div className="auth-card" style={{ textAlign: "center" }}>
          <div className="alert alert-error">{error}</div>
          <button
            className="btn btn-outline"
            onClick={() => navigate("/dashboard")}
            style={{ marginTop: 16 }}
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const isHost =
    user?.username === room?.createdBy || user?.userName === room?.createdBy;
  const status = room?.status || "WAITING";

  return (
    <div className="room-page">
      {/* Room Header */}
      <div className="room-header">
        <div className="room-header-top">
          <div>
            <h1 className="room-title">
              Room <span className="accent">{roomCode}</span>
            </h1>
            <p className="room-host">
              Hosted by <strong>{room?.createdBy}</strong>
              {isHost && <span className="participant-badge">You</span>}
            </p>
          </div>
          <div className="room-header-actions">
            <span
              className={`badge badge-lg ${
                status === "WAITING"
                  ? "badge-waiting"
                  : status === "IN_PROGRESS"
                    ? "badge-active"
                    : "badge-completed"
              }`}
            >
              {status === "IN_PROGRESS" ? "IN PROGRESS" : status}
            </span>
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: 20 }}>
          {error}
        </div>
      )}

      {/* Two-Column Layout */}
      <div className="room-split-layout">
        {/* LEFT: Active Problem */}
        <div className="room-col room-col-left">
          {/* Active Problem */}
          {currentProblem ? (
            <div className="room-section problem-display">
              <h2 className="room-section-title">
                <span>⚔️</span> Active Problem
              </h2>

              <div className="problem-card">
                <div className="problem-header">
                  <h3>{currentProblem.problemName}</h3>
                  <span className="badge badge-waiting">
                    Rating: {currentProblem.rating || "N/A"}
                  </span>
                </div>

                <p className="text-muted" style={{ marginBottom: "16px" }}>
                  Contest: {currentProblem.contestId} | Index:{" "}
                  {currentProblem.problemIndex}
                </p>

                <a
                  href={currentProblem.problemUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-primary"
                >
                  Go to Problem (Codeforces) ↗
                </a>
              </div>
            </div>
          ) : (
            <div className="room-section problem-display problem-empty">
              <h2 className="room-section-title">
                <span>⚔️</span> Active Problem
              </h2>
              <p className="text-muted">No problem selected yet.</p>
            </div>
          )}

          {/* Problem History */}
          <div className="room-section">
            <h2 className="room-section-title">
              <span>📜</span> Problem History
            </h2>

            {problemHistory && problemHistory.length > 0 ? (
              <div className="problem-history-list">
                {problemHistory
                  .slice()
                  .reverse()
                  .map((problem, index) => {
                    const isActive =
                      currentProblem &&
                      problem.problemUrl === currentProblem.problemUrl;

                    return (
                      <div
                        key={index}
                        className={`problem-card ${
                          isActive ? "problem-active-highlight" : ""
                        }`}
                        style={{ marginBottom: "12px" }}
                      >
                        <div className="problem-header">
                          <h3 style={{ fontSize: "16px" }}>
                            {problem.problemName}
                          </h3>

                          <span
                            className={`badge ${
                              isActive ? "badge-active" : "badge-waiting"
                            }`}
                          >
                            {isActive
                              ? "ACTIVE"
                              : `Rating: ${problem.rating || "N/A"}`}
                          </span>
                        </div>

                        <p className="text-muted" style={{ fontSize: "13px" }}>
                          Contest: {problem.contestId} | Index:{" "}
                          {problem.problemIndex}
                        </p>

                        <a
                          href={problem.problemUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="btn btn-outline btn-sm"
                          style={{ marginTop: "8px" }}
                        >
                          View Problem ↗
                        </a>
                      </div>
                    );
                  })}
              </div>
            ) : (
              <p className="text-muted">No problems attempted yet.</p>
            )}
          </div>
        </div>

        {/* RIGHT: Participants + Controls + Invite Link */}
        <div className="room-col room-col-right">
          {/* Invite Link */}
          {status === "WAITING" && (
            <div className="room-section">
              <h2 className="room-section-title">
                <span>🔗</span> Invite Link
              </h2>
              <div className="invite-bar">
                <div className="invite-link-display">
                  <code className="invite-code">{`${roomCode}`}</code>
                </div>
                <button
                  className="btn btn-outline btn-sm"
                  onClick={copyInviteLink}
                >
                  {copied ? "✓ Copied!" : "📋 Copy"}
                </button>
              </div>
            </div>
          )}

          {/* Participants */}
          <div className="room-section">
            <h2 className="room-section-title">
              <span>👥</span> Participants ({room?.participants?.length || 0})
            </h2>
            <ParticipantList
              participants={room?.participants || []}
              createdBy={room?.createdBy}
              cfRatings={cfRatings}
            />
          </div>

          {/* Host Controls */}
          {isHost && (
            <div className="room-section">
              <h2 className="room-section-title">
                <span>⚙️</span> Room Controls
              </h2>
              <div className="room-controls">
                {status === "WAITING" && (
                  <>
                    <button
                      className="btn btn-primary"
                      onClick={() => handleStatusChange("IN_PROGRESS")}
                      disabled={actionLoading}
                    >
                      {actionLoading ? (
                        <span className="spinner-sm" />
                      ) : (
                        "🚀 Start Battle"
                      )}
                    </button>
                    <button
                      className="btn btn-outline btn-danger"
                      onClick={() => handleStatusChange("COMPLETED")}
                      disabled={actionLoading}
                    >
                      {actionLoading ? (
                        <span className="spinner-sm" />
                      ) : (
                        "🔒 Close Entries"
                      )}
                    </button>
                  </>
                )}
                {status === "IN_PROGRESS" && (
                  <>
                    <button
                      className="btn btn-primary"
                      onClick={handlePickProblem}
                      disabled={actionLoading}
                    >
                      {actionLoading ? (
                        <span className="spinner-sm" />
                      ) : (
                        "🎲 Pick Problem"
                      )}
                    </button>
                    <button
                      className="btn btn-outline btn-danger"
                      onClick={() => handleStatusChange("COMPLETED")}
                      disabled={actionLoading}
                    >
                      {actionLoading ? (
                        <span className="spinner-sm" />
                      ) : (
                        "🏁 Complete Room"
                      )}
                    </button>
                  </>
                )}
                {status === "COMPLETED" && (
                  <p className="text-muted">
                    This room has been closed. No more entries allowed.
                  </p>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="room-footer" />
    </div>
  );
};

export default Room;
