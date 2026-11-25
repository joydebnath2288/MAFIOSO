import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function ProtectedRoute({ allowedRoles, children }) {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="container auth-loading-shell">
        <div className="status-panel">
          <span className="status-dot status-dot-pending" aria-hidden="true" />
          <span className="mono-label">Verifying active session</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (Array.isArray(allowedRoles) && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    return (
      <Navigate
        to="/unauthorized"
        replace
        state={{ from: location.pathname, requiredRoles: allowedRoles }}
      />
    );
  }

  return children;
}

export function PublicOnlyRoute({ children }) {
  const { user, isAuthenticated, loading, getDashboardPath } = useAuth();

  if (loading) {
    return (
      <div className="container auth-loading-shell">
        <div className="status-panel">
          <span className="status-dot status-dot-pending" aria-hidden="true" />
          <span className="mono-label">Checking session state</span>
        </div>
      </div>
    );
  }

  if (isAuthenticated && user) {
    return <Navigate to={getDashboardPath(user.role)} replace />;
  }

  return children;
}
