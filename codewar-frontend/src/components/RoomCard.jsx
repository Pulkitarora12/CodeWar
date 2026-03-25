import { useNavigate } from "react-router-dom";

const RoomCard = ({ room }) => {
  const navigate = useNavigate();

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
        {getStatusBadge(status)}
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
