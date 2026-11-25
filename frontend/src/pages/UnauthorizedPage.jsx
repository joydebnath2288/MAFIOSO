import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function UnauthorizedPage() {
  const { user, isAuthenticated, getDashboardPath } = useAuth();
  const location = useLocation();
  const attemptedPath = location.state && location.state.from;

  const returnPath = isAuthenticated && user ? getDashboardPath(user.role) : '/login';
  const returnLabel = isAuthenticated ? 'Return to Dashboard' : 'Sign in';

  return (
    <div className="container not-found-page">
      <div className="not-found-card">
        <span className="mono-label">HTTP 403 • Insufficient Permissions</span>
        <h1 className="page-title">Access restricted</h1>
        <p className="page-subtitle">
          {isAuthenticated && user
            ? `Your signed-in role (${user.role}) does not have permission to open ${
                attemptedPath || 'this route'
              }.`
            : 'Your account does not have permission to open the requested workspace.'}
        </p>
        <div className="hero-actions">
          <Link to={returnPath} className="btn btn-primary">
            {returnLabel}
          </Link>
          <Link to="/" className="btn btn-secondary">
            Overview
          </Link>
        </div>
      </div>
    </div>
  );
}
