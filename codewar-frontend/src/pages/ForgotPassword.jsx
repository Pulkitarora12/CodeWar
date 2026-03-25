import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../api/auth";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!email.trim()) {
      setError("Please enter your email address.");
      return;
    }

    setLoading(true);
    try {
      const res = await forgotPassword(email);
      setSuccess(res.data?.message || "Reset link sent! Check your email inbox.");
    } catch (err) {
      const msg = err.response?.data?.message || "Failed to send reset email. Please try again.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <span className="auth-logo">🔑</span>
          <h1 className="auth-title">Forgot Password</h1>
          <p className="auth-subtitle">
            Enter your email and we&apos;ll send you a reset link
          </p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <div className="form-group">
            <label htmlFor="fp-email">Email Address</label>
            <input
              id="fp-email"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? <span className="spinner-sm" /> : "Send Reset Link"}
          </button>
        </form>

        <p className="auth-switch">
          Remember your password?{" "}
          <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
};

export default ForgotPassword;
