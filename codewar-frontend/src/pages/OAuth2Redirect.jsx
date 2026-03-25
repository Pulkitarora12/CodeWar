import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getCurrentUser } from "../api/auth";

const OAuth2Redirect = () => {
  const [searchParams] = useSearchParams();
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState("");

  useEffect(() => {
    const token = searchParams.get("token");

    if (!token) {
      setError("OAuth login failed — no token received.");
      setTimeout(() => navigate("/login"), 3000);
      return;
    }

    const handleOAuthToken = async () => {
      try {
        // Store token so the axios interceptor can use it
        localStorage.setItem("cw_token", token);
        const res = await getCurrentUser();
        login(token, res.data);
        navigate("/dashboard");
      } catch {
        setError("Failed to fetch user info. Please try again.");
        localStorage.removeItem("cw_token");
        setTimeout(() => navigate("/login"), 3000);
      }
    };

    handleOAuthToken();
  }, [searchParams, login, navigate]);

  return (
    <div className="page-center">
      {error ? (
        <div className="auth-card" style={{ textAlign: "center" }}>
          <div className="alert alert-error">{error}</div>
          <p className="auth-subtitle">Redirecting to login...</p>
        </div>
      ) : (
        <div className="oauth-loading">
          <div className="spinner" />
          <p className="auth-subtitle">Completing login...</p>
        </div>
      )}
    </div>
  );
};

export default OAuth2Redirect;
