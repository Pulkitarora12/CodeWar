import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/auth";
import { FaGithub } from "react-icons/fa";
import { FcGoogle } from "react-icons/fc";
import { BASE_URL } from "../utils/constants";

const Register = () => {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const validate = () => {
    if (!username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      return "Please fill in all fields.";
    }
    if (username.length < 3 || username.length > 20) {
      return "Username must be 3–20 characters.";
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return "Please enter a valid email address.";
    }
    if (password.length < 6) {
      return "Password must be at least 6 characters.";
    }
    if (password !== confirmPassword) {
      return "Passwords do not match.";
    }
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      await registerUser(username, email, password);
      setSuccess("Account created successfully! Redirecting to login...");
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      const msg = err.response?.data?.message || "Registration failed. Please try again.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleOAuth = (provider) => {
    window.location.href = `${BASE_URL}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        {/* Header */}
        <div className="auth-header">
          <span className="auth-logo">⚔️</span>
          <h1 className="auth-title">Join the Arena</h1>
          <p className="auth-subtitle">Create your CodeWar account</p>
        </div>

        {/* OAuth Buttons */}
        <div className="oauth-buttons">
          <button
            type="button"
            className="oauth-btn oauth-google"
            onClick={() => handleOAuth("google")}
          >
            <FcGoogle size={20} />
            <span>Continue with Google</span>
          </button>
          <button
            type="button"
            className="oauth-btn oauth-github"
            onClick={() => handleOAuth("github")}
          >
            <FaGithub size={20} />
            <span>Continue with GitHub</span>
          </button>
        </div>

        <div className="auth-divider">
          <span>or register with email</span>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <div className="form-group">
            <label htmlFor="reg-username">Username</label>
            <input
              id="reg-username"
              type="text"
              placeholder="Choose a username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-email">Email</label>
            <input
              id="reg-email"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-password">Password</label>
            <input
              id="reg-password"
              type="password"
              placeholder="Min. 6 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-confirm">Confirm Password</label>
            <input
              id="reg-confirm"
              type="password"
              placeholder="Re-enter your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? <span className="spinner-sm" /> : "Create Account"}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account?{" "}
          <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
