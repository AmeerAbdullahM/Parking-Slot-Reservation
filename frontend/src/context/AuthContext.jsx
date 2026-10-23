import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');

      if (storedToken && storedUser) {
        try {
          setUser(JSON.parse(storedUser));
          setToken(storedToken);
        } catch (e) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email, password) => {
    const data = await api.login(email, password);
    const authUser = {
      id: data.userId,
      fullName: data.fullName,
      email: data.email,
      role: data.role,
      assignedLotId: data.assignedLotId
    };

    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(authUser));

    setToken(data.token);
    setUser(authUser);
    return authUser;
  };

  const register = async (fullName, email, password, phone) => {
    const data = await api.register(fullName, email, password, phone);
    const authUser = {
      id: data.userId,
      fullName: data.fullName,
      email: data.email,
      role: data.role,
      assignedLotId: null
    };

    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(authUser));

    setToken(data.token);
    setUser(authUser);
    return authUser;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, role: user?.role, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
