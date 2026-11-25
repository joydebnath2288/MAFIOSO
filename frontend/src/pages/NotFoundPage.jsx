import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="container not-found-page">
      <div className="not-found-card">
        <span className="mono-label">HTTP 404 • SPA Route</span>
        <h1 className="page-title">Page not found</h1>
        <p className="page-subtitle">
          This route is not defined in the Milestone 1 shell. Nginx correctly returned the React
          SPA entry point so client-side routing handles direct refreshes.
        </p>
        <div className="hero-actions">
          <Link to="/" className="btn btn-primary">
            Return to Overview
          </Link>
          <Link to="/health" className="btn btn-secondary">
            System Health
          </Link>
        </div>
      </div>
    </div>
  );
}
