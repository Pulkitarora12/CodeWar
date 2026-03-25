import { useAuth } from "../context/AuthContext";

const Dashboard = () => {
  const { user, logout } = useAuth();

  return (
    <div className="dashboard-page">
      <div className="dashboard-welcome">
        <h1 className="dashboard-title">
          Welcome, <span className="accent">{user?.username || user?.userName || "Warrior"}</span> ⚔️
        </h1>
        <p className="dashboard-subtitle">
          Your arena is ready. Create or join a room to start competing.
        </p>
      </div>

      <div className="dashboard-grid">
        <div className="dash-card">
          <div className="dash-card-icon">🏟️</div>
          <h3>Create Room</h3>
          <p>Host a new coding battle and invite friends.</p>
          <button className="btn btn-primary" disabled>
            Coming Soon
          </button>
        </div>

        <div className="dash-card">
          <div className="dash-card-icon">🚪</div>
          <h3>Join Room</h3>
          <p>Enter a room code to join an existing match.</p>
          <button className="btn btn-outline" disabled>
            Coming Soon
          </button>
        </div>

        <div className="dash-card">
          <div className="dash-card-icon">📊</div>
          <h3>Your Stats</h3>
          <p>View your Codeforces rating and match history.</p>
          <button className="btn btn-outline" disabled>
            Coming Soon
          </button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
