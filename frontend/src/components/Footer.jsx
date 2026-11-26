import { Link } from 'react-router-dom';
import { env } from '../config/env';

export function Footer() {
  return (
    <footer className="site-footer-expanded">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand-col">
            <div className="footer-brand-title">
              <span className="brand-mark" aria-hidden="true">
                <svg width="22" height="22" viewBox="0 0 32 32" fill="none">
                  <rect width="32" height="32" rx="7" fill="var(--accent)" />
                  <path
                    d="M8 22V10L16 17L24 10V22"
                    stroke="var(--on-accent)"
                    strokeWidth="2.4"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </span>
              <span>{env.appName}</span>
              <span className="badge-gold">BD → UK</span>
            </div>
            <p className="footer-brand-desc">
              Specialized international education consultancy guiding Bangladeshi students toward
              undergraduate (BSc) and postgraduate (MSc) admissions across the United Kingdom.
            </p>
          </div>

          <div>
            <div className="footer-col-title">Navigation</div>
            <ul className="footer-link-list">
              <li>
                <Link to="/" className="footer-link">Home</Link>
              </li>
              <li>
                <Link to="/about" className="footer-link">About Us</Link>
              </li>
              <li>
                <Link to="/services" className="footer-link">Our Services</Link>
              </li>
              <li>
                <Link to="/study-in-uk" className="footer-link">Study in the UK</Link>
              </li>
              <li>
                <Link to="/scholarships" className="footer-link">Scholarships</Link>
              </li>
              <li>
                <Link to="/contact" className="footer-link">Contact & Inquiries</Link>
              </li>
            </ul>
          </div>

          <div>
            <div className="footer-col-title">Admissions & Guidance</div>
            <ul className="footer-link-list">
              <li>
                <Link to="/services" className="footer-link">Academic Evaluation</Link>
              </li>
              <li>
                <Link to="/services" className="footer-link">University Selection</Link>
              </li>
              <li>
                <Link to="/services" className="footer-link">Admission Guidance</Link>
              </li>
              <li>
                <Link to="/services" className="footer-link">Visa & CAS Guidance</Link>
              </li>
              <li>
                <Link to="/services" className="footer-link">Pre-Departure Briefing</Link>
              </li>
            </ul>
          </div>

          <div>
            <div className="footer-col-title">Portal Access</div>
            <ul className="footer-link-list">
              <li>
                <Link to="/login" className="footer-link">Sign In</Link>
              </li>
              <li>
                <Link to="/register" className="footer-link">Create Student Account</Link>
              </li>
              <li>
                <Link to="/health" className="footer-link">System Health</Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom-row">
          <div className="footer-disclaimer">
            MAFIOSO provides professional educational advisory and application guidance. We do not
            issue visas, guarantee university acceptance, or act as an immigration authority. Official
            admission decisions rest solely with UK educational institutions.
          </div>
          <div>
            &copy; {new Date().getFullYear()} {env.appName}. All rights reserved.
          </div>
        </div>
      </div>
    </footer>
  );
}
