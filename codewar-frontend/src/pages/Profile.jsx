import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { getCfStatus, getCfRatingByUserId, generateCfToken, verifyCfHandle, unlinkCfHandle } from "../api/codeforces";

const Profile = () => {
  const { user } = useAuth();

  // CF status
  const [cfStatus, setCfStatus] = useState({ handle: "", verified: false });
  const [cfRating, setCfRating] = useState(null);
  const [loadingCf, setLoadingCf] = useState(true);

  // Linking flow
  const [linkStep, setLinkStep] = useState("idle"); // idle | entering | token-generated | verifying
  const [handleInput, setHandleInput] = useState("");
  const [verificationToken, setVerificationToken] = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    fetchCfData();
  }, []);

  const fetchCfData = async () => {
    setLoadingCf(true);
    try {
      const statusRes = await getCfStatus();
      setCfStatus(statusRes.data);

      // If verified, also fetch rating
      if (statusRes.data.verified && user?.id) {
        try {
          const ratingRes = await getCfRatingByUserId(user.id);
          setCfRating(ratingRes.data);
        } catch {
          // Rating fetch failed, that's ok
        }
      }
    } catch {
      // CF status not available
    } finally {
      setLoadingCf(false);
    }
  };

  const handleGenerateToken = async () => {
    setError("");
    if (!handleInput.trim()) {
      setError("Please enter your Codeforces handle.");
      return;
    }
    setActionLoading(true);
    try {
      const res = await generateCfToken(handleInput.trim());
      setVerificationToken(res.data.token);
      setLinkStep("token-generated");
      setSuccess(res.data.message || "Token generated! Add it to your Codeforces profile.");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to generate token.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleVerify = async () => {
    setError("");
    setSuccess("");
    setActionLoading(true);
    try {
      const res = await verifyCfHandle();
      setSuccess(res.data.message || "Codeforces account linked successfully!");
      setLinkStep("idle");
      await fetchCfData();
    } catch (err) {
      setError(err.response?.data?.message || "Verification failed. Make sure the token is in your Codeforces first name.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleUnlink = async () => {
    setError("");
    setSuccess("");
    setActionLoading(true);
    try {
      await unlinkCfHandle();
      setCfStatus({ handle: "", verified: false });
      setCfRating(null);
      setSuccess("Codeforces account unlinked.");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to unlink.");
    } finally {
      setActionLoading(false);
    }
  };

  const getRankColor = (rank) => {
    const colors = {
      newbie: "#808080",
      pupil: "#008000",
      specialist: "#03a89e",
      expert: "#0000ff",
      "candidate master": "#aa00aa",
      master: "#ff8c00",
      "international master": "#ff8c00",
      grandmaster: "#ff0000",
      "international grandmaster": "#ff0000",
      "legendary grandmaster": "#ff0000",
    };
    return colors[(rank || "").toLowerCase()] || "#94a3b8";
  };

  return (
    <div className="profile-page">
      {/* User Info Section */}
      <div className="profile-header">
        <div className="profile-avatar-lg">
          {(user?.username || user?.userName || "U").charAt(0).toUpperCase()}
        </div>
        <div className="profile-info">
          <h1 className="profile-name">{user?.username || user?.userName}</h1>
          <p className="profile-email">{user?.email}</p>
          <div className="profile-meta">
            {user?.roles?.map((role, i) => (
              <span key={i} className="badge badge-waiting">{role.replace("ROLE_", "")}</span>
            ))}
            {user?.signUpMethod && (
              <span className="profile-signup-method">via {user.signUpMethod}</span>
            )}
          </div>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 20 }}>{error}</div>}
      {success && <div className="alert alert-success" style={{ marginBottom: 20 }}>{success}</div>}

      {/* Codeforces Section */}
      <div className="profile-section">
        <h2 className="profile-section-title">
          <span>🏆</span> Codeforces Profile
        </h2>

        {loadingCf ? (
          <div style={{ display: "flex", justifyContent: "center", padding: 24 }}>
            <div className="spinner" />
          </div>
        ) : cfStatus.verified && cfStatus.handle ? (
          /* LINKED — show CF details */
          <div className="cf-linked">
            <div className="cf-card">
              <div className="cf-card-header">
                <div className="cf-handle-section">
                  <span className="cf-handle-label">Handle</span>
                  <a
                    href={`https://codeforces.com/profile/${cfStatus.handle}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="cf-handle-link"
                  >
                    {cfStatus.handle}
                    <span className="cf-verified-badge">✓ Verified</span>
                  </a>
                </div>
                <button
                  className="btn btn-outline btn-sm btn-danger"
                  onClick={handleUnlink}
                  disabled={actionLoading}
                >
                  Unlink
                </button>
              </div>

              {cfRating && (
                <div className="cf-stats-grid">
                  <div className="cf-stat-item">
                    <span className="cf-stat-label">Rating</span>
                    <span className="cf-stat-value" style={{ color: getRankColor(cfRating.rank) }}>
                      {cfRating.rating || "Unrated"}
                    </span>
                  </div>
                  <div className="cf-stat-item">
                    <span className="cf-stat-label">Rank</span>
                    <span className="cf-stat-value" style={{ color: getRankColor(cfRating.rank) }}>
                      {cfRating.rank || "Unrated"}
                    </span>
                  </div>
                  <div className="cf-stat-item">
                    <span className="cf-stat-label">Max Rating</span>
                    <span className="cf-stat-value" style={{ color: getRankColor(cfRating.maxRank) }}>
                      {cfRating.maxRating || "—"}
                    </span>
                  </div>
                  <div className="cf-stat-item">
                    <span className="cf-stat-label">Max Rank</span>
                    <span className="cf-stat-value" style={{ color: getRankColor(cfRating.maxRank) }}>
                      {cfRating.maxRank || "—"}
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>
        ) : (
          /* NOT LINKED — show linking flow */
          <div className="cf-unlinked">
            {linkStep === "idle" && (
              <div className="cf-empty">
                <p className="cf-empty-text">
                  Link your Codeforces account to compete in rated rooms.
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => setLinkStep("entering")}
                >
                  🔗 Link Codeforces Handle
                </button>
              </div>
            )}

            {linkStep === "entering" && (
              <div className="cf-link-form">
                <p className="cf-step-label">Step 1: Enter your Codeforces handle</p>
                <div className="cf-input-row">
                  <input
                    type="text"
                    placeholder="e.g. tourist"
                    value={handleInput}
                    onChange={(e) => setHandleInput(e.target.value)}
                    className="cf-handle-input"
                  />
                  <button
                    className="btn btn-primary"
                    onClick={handleGenerateToken}
                    disabled={actionLoading}
                  >
                    {actionLoading ? <span className="spinner-sm" /> : "Generate Token"}
                  </button>
                </div>
                <button className="link-btn" onClick={() => setLinkStep("idle")} style={{ marginTop: 8 }}>
                  Cancel
                </button>
              </div>
            )}

            {linkStep === "token-generated" && (
              <div className="cf-link-form">
                <p className="cf-step-label">Step 2: Add this token to your Codeforces "First Name"</p>
                <div className="cf-token-display">
                  <code className="cf-token">{verificationToken}</code>
                  <button
                    className="btn btn-outline btn-sm"
                    onClick={() => {
                      navigator.clipboard.writeText(verificationToken);
                    }}
                  >
                    📋 Copy
                  </button>
                </div>
                <p className="cf-step-hint">
                  Go to{" "}
                  <a href="https://codeforces.com/settings/social" target="_blank" rel="noopener noreferrer">
                    Codeforces Settings → Social
                  </a>
                  , paste the token in the "First Name" field, and save.
                </p>
                <p className="cf-step-label" style={{ marginTop: 16 }}>Step 3: Click verify</p>
                <button
                  className="btn btn-primary"
                  onClick={handleVerify}
                  disabled={actionLoading}
                >
                  {actionLoading ? <span className="spinner-sm" /> : "✓ Verify Handle"}
                </button>
                <button className="link-btn" onClick={() => setLinkStep("idle")} style={{ marginTop: 8 }}>
                  Cancel
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Account Info */}
      <div className="profile-section">
        <h2 className="profile-section-title">
          <span>🛡️</span> Account Details
        </h2>
        <div className="account-details-grid">
          <div className="account-detail-item">
            <span className="account-detail-label">User ID</span>
            <span className="account-detail-value">{user?.id || "—"}</span>
          </div>
          <div className="account-detail-item">
            <span className="account-detail-label">Account Status</span>
            <span className="account-detail-value">
              {user?.enabled !== false ? "✅ Active" : "❌ Disabled"}
            </span>
          </div>
          <div className="account-detail-item">
            <span className="account-detail-label">2FA</span>
            <span className="account-detail-value">
              {user?.isTwoFactorEnabled ? "Enabled" : "Disabled"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
