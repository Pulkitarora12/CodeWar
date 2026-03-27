import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getRoom,
  updateRoomStatus,
  getRoomRatings,
} from "../api/room";
import { startContest, getContestsByRoom } from "../api/contest";
import { useAuth } from "../context/AuthContext";
import ParticipantList from "../components/ParticipantList";

const Room = () => {
  const { roomCode } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [cfRatings, setCfRatings] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [contests, setContests] = useState([]);

  const fetchRoom = useCallback(async () => {
    try {
      const res = await getRoom(roomCode);
      setRoom(res.data);

      getRoomRatings(roomCode)
        .then((r) => setCfRatings(r.data.participants || []))
        .catch(() => setCfRatings([]));

      // ✅ add this
      getContestsByRoom(roomCode)
        .then((r) => setContests(r.data || []))
        .catch(() => setContests([]));
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

  const handleStartContest = async () => {
    setActionLoading(true);
    setError("");
    try {
      const res = await startContest(roomCode);
      const contestId = res.data.contestId || res.data.id;
      if (contestId) {
        navigate(`/contest/${roomCode}/${contestId}`);
      } else {
        setError("Contest started but no ID returned.");
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to start contest.");
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
        {/* LEFT: Contest History */}
        <div className="room-col room-col-left">
          {/* Contest History */}
          <div className="room-section">
            <h2 className="room-section-title">
              <span>📜</span> Contest History
            </h2>

            {contests && contests.length > 0 ? (
              <div className="problem-history-list">
                {contests.map((contest, index) => {
                  return (
                    <div
                      key={index}
                      className="problem-card"
                      style={{ marginBottom: "12px" }}
                    >
                      <div className="problem-header">
                        <h3 style={{ fontSize: "16px" }}>
                          {contest.problemName}
                        </h3>

                        <span
                          className={`badge ${
                            contest.status === "ACTIVE" ? "badge-active" : "badge-waiting"
                          }`}
                        >
                          {contest.status === "ACTIVE" ? "ACTIVE" : `COMPLETED`}
                        </span>
                      </div>

                      <p className="text-muted" style={{ fontSize: "13px" }}>
                        Start Time: {new Date(contest.startTime).toLocaleString()}
                        <br />
                        Rating: {contest.rating} | Codeforces ID: {contest.cfContestId}
                      </p>

                      <div style={{ marginTop: "12px", display: "flex", gap: "8px" }}>
                        <a
                          href={contest.problemUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="btn btn-outline btn-sm"
                        >
                          View Problem ↗
                        </a>
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={() => navigate(`/contest/${roomCode}/${contest.contestId}`)}
                        >
                          View Leaderboard 🏆
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <p className="text-muted">No contests held yet.</p>
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
                      onClick={handleStartContest}
                      disabled={actionLoading}
                    >
                      {actionLoading ? (
                        <span className="spinner-sm" />
                      ) : (
                        "⚔️ Start Contest"
                      )}
                    </button>
                    <button
                      className="btn btn-outline btn-danger"
                      style={{ marginLeft: 8 }}
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
