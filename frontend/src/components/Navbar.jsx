import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { env } from '../config/env';
import { useAuth } from '../hooks/useAuth';
import { CloseIcon, LogOutIcon, MenuIcon } from './Icons';

export function Navbar() {
  const { user, isAuthenticated, loading, logout, getDashboardPath } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [signingOut, setSigningOut] = useState(false);

  const dashboardPath = user ? getDashboardPath(user.role) : null;
  const dashboardLabel = user && user.role === 'STUDENT' ? 'Student Dashboard' : 'Staff Dashboard';

  async function handleLogout() {
    if (signingOut) {
      return;
    }
    setSigningOut(true);
    try {
      await logout();
      setMobileOpen(false);
      navigate('/login', { replace: true });
    } finally {
      setSigningOut(false);
    }
  }

  return (
    <header className="site-header">
      <div className="container header-inner">
        <Link to="/" className="brand" aria-label="MAFIOSO Home" onClick={() => setMobileOpen(false)}>
          <span className="brand-mark" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 32 32" fill="none">
              <rect width="32" height="32" rx="7" fill="#1B4D3E" />
              <path
                d="M8 22V10L16 17L24 10V22"
                stroke="#FDFBF7"
                strokeWidth="2.4"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </span>
          <span className="brand-wordmark">{env.appName}</span>
          <span className="brand-tag">BD → UK</span>
        </Link>

        <nav className="header-nav" aria-label="Primary navigation">
          <NavLink
            to="/"
            end
            className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
          >
            Overview
          </NavLink>
          <NavLink
            to="/health"
            className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
          >
            System Health
          </NavLink>
          {isAuthenticated && dashboardPath && (
            <NavLink
              to={dashboardPath}
              className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
            >
              {dashboardLabel}
            </NavLink>
          )}
        </nav>

        <div className="header-actions">
          {loading ? (
            <span className="mono-label">Checking session</span>
          ) : isAuthenticated && user ? (
            <>
              <div className="header-user-meta">
                <span className="header-user-name">{user.fullName}</span>
                <span className="mono-badge">{user.role}</span>
              </div>
              <button
                type="button"
                className="btn btn-nav-outline"
                onClick={handleLogout}
                disabled={signingOut}
              >
                <LogOutIcon />
                <span>{signingOut ? 'Signing out' : 'Sign out'}</span>
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">
                Sign in
              </Link>
              <Link to="/register" className="btn btn-nav">
                Register
              </Link>
            </>
          )}
        </div>

        <button
          type="button"
          className="mobile-menu-btn"
          aria-label={mobileOpen ? 'Close navigation menu' : 'Open navigation menu'}
          aria-expanded={mobileOpen}
          onClick={() => setMobileOpen((prev) => !prev)}
        >
          {mobileOpen ? <CloseIcon /> : <MenuIcon />}
        </button>
      </div>

      {mobileOpen && (
        <div className="mobile-drawer">
          <div className="container mobile-drawer-inner">
            <NavLink
              to="/"
              end
              className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
              onClick={() => setMobileOpen(false)}
            >
              Overview
            </NavLink>
            <NavLink
              to="/health"
              className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
              onClick={() => setMobileOpen(false)}
            >
              System Health
            </NavLink>
            {isAuthenticated && dashboardPath ? (
              <>
                <NavLink
                  to={dashboardPath}
                  className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
                  onClick={() => setMobileOpen(false)}
                >
                  {dashboardLabel}
                </NavLink>
                <div className="mobile-user-row">
                  <span className="header-user-name">{user.fullName}</span>
                  <span className="mono-badge">{user.role}</span>
                </div>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleLogout}
                  disabled={signingOut}
                >
                  <LogOutIcon />
                  <span>{signingOut ? 'Signing out' : 'Sign out'}</span>
                </button>
              </>
            ) : (
              <div className="mobile-auth-actions">
                <Link
                  to="/login"
                  className="btn btn-secondary"
                  onClick={() => setMobileOpen(false)}
                >
                  Sign in
                </Link>
                <Link
                  to="/register"
                  className="btn btn-primary"
                  onClick={() => setMobileOpen(false)}
                >
                  Register
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
