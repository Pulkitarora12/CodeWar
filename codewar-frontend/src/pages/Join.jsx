import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { joinRoom } from "../api/room";

const Join = () => {
  const [searchParams] = useSearchParams();
  const codeFromUrl = searchParams.get("code") || "";
  const [roomCode, setRoomCode] = useState(codeFromUrl);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleJoin = async (e) => {
    e.preventDefault();
    setError("");

    const code = roomCode.trim().toUpperCase();
    if (!code) {
      setError("Please enter a room code.");
      return;
    }

    setLoading(true);
    try {
      await joinRoom(code);
      navigate(`/room/${code}`);
    } catch (err) {
      const msg = err.response?.data?.message || "Failed to join room. Check the code and try again.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <span className="auth-logo">🚪</span>
          <h1 className="auth-title">Join a Room</h1>
          <p className="auth-subtitle">Enter the room code to join a battle</p>
        </div>

        <form onSubmit={handleJoin} className="auth-form">
          {error && <div className="alert alert-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="room-code">Room Code</label>
            <input
              id="room-code"
              type="text"
              placeholder="e.g. CW-AB12CD"
              value={roomCode}
              onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
              autoComplete="off"
              className="room-code-input"
            />
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? <span className="spinner-sm" /> : "Join Room"}
          </button>
        </form>

        <p className="auth-switch">
          Want to host?{" "}
          <button
            type="button"
            className="link-btn"
            onClick={() => navigate("/dashboard")}
          >
            Create a room instead
          </button>
        </p>
      </div>
    </div>
  );
};

export default Join;
