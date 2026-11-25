import { Link, NavLink } from 'react-router-dom';
import { env } from '../config/env';

export function Navbar() {
  return (
    <header className="site-header">
      <div className="container header-inner">
        <Link to="/" className="brand" aria-label="MAFIOSO Home">
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
        </nav>

        <Link to="/health" className="btn btn-nav">
          Diagnostics
        </Link>
      </div>
    </header>
  );
}
