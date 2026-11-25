import { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { env } from '../config/env';
import { useAuth } from '../hooks/useAuth';
import { CloseIcon, LogOutIcon, MenuIcon } from './Icons';

function getNavItemsForUser(user) {
  if (!user) {
    return [
      { to: '/', label: 'Home', end: true },
      { to: '/about', label: 'About', end: false },
      { to: '/services', label: 'Services', end: false },
      { to: '/study-in-uk', label: 'Study in UK', end: false },
      { to: '/scholarships', label: 'Scholarships', end: false },
      { to: '/contact', label: 'Contact', end: false },
    ];
  }
  if (user.role === 'ADMIN') {
    return [
      { to: '/', label: 'Home', end: true },
      { to: '/staff/dashboard', label: 'Overview', end: true },
      { to: '/staff/students', label: 'Students', end: false },
      { to: '/staff/consultants', label: 'Consultants', end: false },
      { to: '/health', label: 'System Health', end: false },
    ];
  }
  if (user.role === 'CONSULTANT') {
    return [
      { to: '/', label: 'Home', end: true },
      { to: '/staff/dashboard', label: 'Overview', end: true },
      { to: '/staff/assigned-students', label: 'Assigned Students', end: false },
    ];
  }
  if (user.role === 'STUDENT') {
    return [
      { to: '/', label: 'Home', end: true },
      { to: '/services', label: 'Services', end: false },
      { to: '/study-in-uk', label: 'Study in UK', end: false },
      { to: '/scholarships', label: 'Scholarships', end: false },
      { to: '/student/dashboard', label: 'Student Dashboard', end: false },
      { to: '/student/profile', label: 'Profile', end: false },
    ];
  }
  return [];
}

export function Navbar() {
  const { user, isAuthenticated, loading, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [signingOut, setSigningOut] = useState(false);

  const navItems = getNavItemsForUser(isAuthenticated ? user : null);

  // Close drawer on Escape key
  useEffect(() => {
    function handleKeyDown(e) {
      if (e.key === 'Escape') {
        setMobileOpen(false);
      }
    }
    if (mobileOpen) {
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [mobileOpen]);

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
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
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
                Get Started
              </Link>
            </>
          )}
        </div>

        <button
          type="button"
          className="mobile-menu-btn"
          aria-label={mobileOpen ? 'Close navigation menu' : 'Open navigation menu'}
          aria-expanded={mobileOpen}
          aria-controls="mobile-navigation-drawer"
          onClick={() => setMobileOpen((prev) => !prev)}
        >
          {mobileOpen ? <CloseIcon /> : <MenuIcon />}
        </button>
      </div>

      {mobileOpen && (
        <div id="mobile-navigation-drawer" className="mobile-drawer" role="dialog" aria-modal="true">
          <div className="container mobile-drawer-inner">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
                onClick={() => setMobileOpen(false)}
              >
                {item.label}
              </NavLink>
            ))}

            {isAuthenticated && user ? (
              <div style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
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
              </div>
            ) : (
              <div className="mobile-auth-actions" style={{ marginTop: '16px', display: 'flex', gap: '10px' }}>
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
                  Get Started
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
