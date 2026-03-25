import { useState } from "react";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import { resetPassword } from "../api/auth";

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!newPassword.trim() || !confirmPassword.trim()) {
      setError("Please fill in all fields.");
      return;
    }
    if (newPassword.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    if (!token) {
      setError("Invalid or missing reset token.");
      return;
    }

    setLoading(true);
    try {
      await resetPassword(token, newPassword);
      setSuccess("Password reset successfully! Redirecting to login...");
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      const msg = err.response?.data?.message || "Reset failed. The token may have expired.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <span className="auth-logo">🔒</span>
          <h1 className="auth-title">Reset Password</h1>
          <p className="auth-subtitle">Choose a new password for your account</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <div className="form-group">
            <label htmlFor="rp-password">New Password</label>
            <input
              id="rp-password"
              type="password"
              placeholder="Min. 6 characters"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          <div className="form-group">
            <label htmlFor="rp-confirm">Confirm Password</label>
            <input
              id="rp-confirm"
              type="password"
              placeholder="Re-enter your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? <span className="spinner-sm" /> : "Reset Password"}
          </button>
        </form>

        <p className="auth-switch">
          <Link to="/login">Back to Sign In</Link>
        </p>
      </div>
    </div>
  );
};

export default ResetPassword;
