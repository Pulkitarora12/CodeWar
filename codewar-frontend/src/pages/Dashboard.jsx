import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { createRoom, getMyRooms } from "../api/room";
import RoomCard from "../components/RoomCard";

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [loadingRooms, setLoadingRooms] = useState(true);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    try {
      const res = await getMyRooms();
      setRooms(res.data || []);
    } catch {
      // User might not have any rooms yet
      setRooms([]);
    } finally {
      setLoadingRooms(false);
    }
  };

  const handleCreateRoom = async () => {
    setError("");
    setCreating(true);
    try {
      const res = await createRoom();
      const { roomCode } = res.data;
      navigate(`/room/${roomCode}`);
    } catch (err) {
      const msg = err.response?.data?.message || "Failed to create room.";
      setError(msg);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="dashboard-page">
      {/* Welcome */}
      <div className="dashboard-welcome">
        <h1 className="dashboard-title">
          Welcome, <span className="accent">{user?.username || user?.userName || "Warrior"}</span> ⚔️
        </h1>
        <p className="dashboard-subtitle">
          Your arena is ready. Create or join a room to start competing.
        </p>
      </div>

      {/* Quick Actions */}
      <div className="dashboard-actions">
        <button
          className="btn btn-primary"
          onClick={handleCreateRoom}
          disabled={creating}
        >
          {creating ? <span className="spinner-sm" /> : "🏟️ Create Room"}
        </button>
        <button className="btn btn-outline" onClick={() => navigate("/join")}>
          🚪 Join Room
        </button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 20 }}>{error}</div>}

      {/* My Rooms */}
      <div className="dashboard-rooms-section">
        <h2 className="section-title">Your Rooms</h2>

        {loadingRooms ? (
          <div className="page-center" style={{ minHeight: 120 }}>
            <div className="spinner" />
          </div>
        ) : rooms.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">🏟️</span>
            <p>No rooms yet. Create one to get started!</p>
          </div>
        ) : (
          <div className="rooms-grid">
            {rooms.map((room, idx) => (
              <RoomCard key={room.roomCode || room.room_code || idx} room={room} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
