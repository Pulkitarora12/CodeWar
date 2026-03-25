import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { loginUser, getCurrentUser } from "../api/auth";
import { useAuth } from "../context/AuthContext";
import { FaGithub } from "react-icons/fa";
import { FcGoogle } from "react-icons/fc";
import { BASE_URL } from "../utils/constants";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!username.trim() || !password.trim()) {
      setError("Please fill in all fields.");
      return;
    }

    setLoading(true);
    try {
      const res = await loginUser(username, password);
      const { jwtToken, username: uname, roles } = res.data;

      // Store token first, then fetch full user info
      localStorage.setItem("cw_token", jwtToken);
      const userRes = await getCurrentUser();
      login(jwtToken, userRes.data);
      navigate("/dashboard");
    } catch (err) {
      const msg =
        err.response?.data?.message || "Invalid credentials. Please try again.";
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
          <h1 className="auth-title">Welcome Back</h1>
          <p className="auth-subtitle">Sign in to your CodeWar arena</p>
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
          <span>or sign in with email</span>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="alert alert-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <div className="form-footer-link">
            <Link to="/forgot-password">Forgot password?</Link>
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? <span className="spinner-sm" /> : "Sign In"}
          </button>
        </form>

        <p className="auth-switch">
          Don&apos;t have an account?{" "}
          <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
