import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { deleteRoom } from "../api/room";

const RoomCard = ({ room, onDeleteSuccess }) => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const getStatusBadge = (status) => {
    const statusMap = {
      WAITING: { label: "Waiting", className: "badge-waiting" },
      ACTIVE: { label: "Active", className: "badge-active" },
      COMPLETED: { label: "Completed", className: "badge-completed" },
    };
    const s = statusMap[status] || { label: status, className: "badge-default" };
    return <span className={`badge ${s.className}`}>{s.label}</span>;
  };

  const roomCode = room.roomCode || room.room_code || "—";
  const status = room.status || "WAITING";
  const createdAt = room.createdAt || room.created_at;
  const participantCount = room.participants?.length ?? "—";

  const isCreator =
    user &&
    room.createdBy &&
    (typeof room.createdBy === "string"
      ? room.createdBy === user.username || room.createdBy === user.userName
      : room.createdBy.userName === user.userName ||
        room.createdBy.username === user.username ||
        room.createdBy.userName === user.username ||
        room.createdBy.username === user.userName);

  const handleDelete = async (e) => {
    e.stopPropagation();
    if (window.confirm(`Are you sure you want to delete room ${roomCode}?`)) {
      try {
        await deleteRoom(roomCode);
        if (onDeleteSuccess) {
          onDeleteSuccess();
        }
      } catch (err) {
        alert(err.response?.data?.message || "Failed to delete room.");
      }
    }
  };

  return (
    <div
      className="room-card"
      onClick={() => navigate(`/room/${roomCode}`)}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === "Enter" && navigate(`/room/${roomCode}`)}
    >
      <div className="room-card-header">
        <span className="room-code">{roomCode}</span>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          {getStatusBadge(status)}
          {isCreator && (
            <button
              onClick={handleDelete}
              title="Delete Room"
              style={{
                background: "none",
                border: "none",
                cursor: "pointer",
                padding: "4px",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                color: "var(--error)",
                fontSize: "0.95rem",
                transition: "transform 0.2s ease, opacity 0.2s ease",
                borderRadius: "4px",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "rgba(239, 68, 68, 0.1)";
                e.currentTarget.style.transform = "scale(1.15)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "transparent";
                e.currentTarget.style.transform = "scale(1)";
              }}
            >
              🗑️
            </button>
          )}
        </div>
      </div>

      <div className="room-card-body">
        <div className="room-stat">
          <span className="room-stat-icon">👥</span>
          <span>{participantCount} participant{participantCount !== 1 ? "s" : ""}</span>
        </div>
        {createdAt && (
          <div className="room-stat">
            <span className="room-stat-icon">🕐</span>
            <span>{new Date(createdAt).toLocaleDateString()}</span>
          </div>
        )}
      </div>

      <div className="room-card-footer">
        <span className="room-card-link">View Room →</span>
      </div>
    </div>
  );
};

export default RoomCard;
