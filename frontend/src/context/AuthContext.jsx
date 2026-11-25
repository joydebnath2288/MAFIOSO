import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { clearCsrfToken, setUnauthorizedHandler } from '../services/apiClient';
import {
  fetchCsrfToken,
  fetchCurrentUser,
  loginUser,
  logoutUser,
  registerStudent,
} from '../services/authService';

export const AuthContext = createContext(null);

export function getDashboardPathForRole(role) {
  if (role === 'STUDENT') {
    return '/student/dashboard';
  }
  if (role === 'CONSULTANT' || role === 'ADMIN') {
    return '/staff/dashboard';
  }
  return '/';
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sessionExpiredNotice, setSessionExpiredNotice] = useState(false);

  const restoreSession = useCallback(async () => {
    setLoading(true);
    try {
      const meRes = await fetchCurrentUser();
      if (meRes.ok && meRes.data && meRes.data.success && meRes.data.data) {
        setUser(meRes.data.data);
        setSessionExpiredNotice(false);
      } else {
        setUser(null);
      }
      await fetchCsrfToken();
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      setSessionExpiredNotice(true);
      clearCsrfToken();
    });
    restoreSession();
    return () => setUnauthorizedHandler(null);
  }, [restoreSession]);

  const clearSessionNotice = useCallback(() => {
    setSessionExpiredNotice(false);
  }, []);

  const login = useCallback(async (credentials) => {
    const res = await loginUser(credentials);
    if (res.ok && res.data && res.data.success && res.data.data && res.data.data.user) {
      const authenticatedUser = res.data.data.user;
      setUser(authenticatedUser);
      setSessionExpiredNotice(false);
      return {
        ok: true,
        user: authenticatedUser,
        redirectTo: res.data.data.redirectTo || getDashboardPathForRole(authenticatedUser.role),
        message: res.data.message || 'Signed in.',
      };
    }
    return {
      ok: false,
      status: res.status,
      error: res.error || 'Unable to sign in.',
      fieldErrors: res.fieldErrors || null,
    };
  }, []);

  const register = useCallback(async (payload) => {
    const res = await registerStudent(payload);
    if (res.ok && res.data && res.data.success) {
      return {
        ok: true,
        user: res.data.data ? res.data.data.user : null,
        message: res.data.message || 'Student account created.',
      };
    }
    return {
      ok: false,
      status: res.status,
      error: res.error || 'Unable to create account.',
      fieldErrors: res.fieldErrors || null,
    };
  }, []);

  const logout = useCallback(async () => {
    await logoutUser();
    setUser(null);
    setSessionExpiredNotice(false);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      loading,
      sessionExpiredNotice,
      clearSessionNotice,
      login,
      register,
      logout,
      restoreSession,
      getDashboardPath: getDashboardPathForRole,
    }),
    [user, loading, sessionExpiredNotice, clearSessionNotice, login, register, logout, restoreSession]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
