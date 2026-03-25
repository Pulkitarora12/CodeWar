import { createContext, useContext, useState, useEffect } from "react";
import { getCurrentUser } from "../api/auth";

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem("cw_token"));
  const [loading, setLoading] = useState(true);

  // On mount, if token exists fetch current user
  useEffect(() => {
    const hydrate = async () => {
      if (token) {
        try {
          const res = await getCurrentUser();
          setUser(res.data);
        } catch {
          // Token expired or invalid
          logout();
        }
      }
      setLoading(false);
    };
    hydrate();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const login = (jwtToken, userData) => {
    localStorage.setItem("cw_token", jwtToken);
    setToken(jwtToken);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem("cw_token");
    localStorage.removeItem("cw_user");
    setToken(null);
    setUser(null);
  };

  const fetchUser = async () => {
    try {
      const res = await getCurrentUser();
      setUser(res.data);
      return res.data;
    } catch {
      logout();
      return null;
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, fetchUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
