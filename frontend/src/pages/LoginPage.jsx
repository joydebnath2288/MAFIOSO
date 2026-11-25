import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRightIcon, EyeIcon, EyeOffIcon, LockIcon } from '../components/Icons';
import { useAuth } from '../hooks/useAuth';

const EMAIL_REGEX = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$/;

const ROLE_DESTINATIONS = [
  {
    code: '01',
    role: 'STUDENT',
    route: '/student/dashboard',
    summary: 'Self-registered applicant access scoped strictly to personal profile records',
  },
  {
    code: '02',
    role: 'CONSULTANT',
    route: '/staff/dashboard',
    summary: 'Provisioned staff access for applicant guidance without admin privileges',
  },
  {
    code: '03',
    role: 'ADMIN',
    route: '/staff/dashboard',
    summary: 'Platform administration and restricted staff governance controls',
  },
];

export function LoginPage() {
  const { login, sessionExpiredNotice, clearSessionNotice } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const initialEmail =
    (location.state && typeof location.state.registeredEmail === 'string' && location.state.registeredEmail) ||
    '';

  const [email, setEmail] = useState(initialEmail);
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function validateForm() {
    const errors = {};
    const trimmedEmail = email.trim().toLowerCase();
    if (!trimmedEmail) {
      errors.email = 'Email address is required.';
    } else if (!EMAIL_REGEX.test(trimmedEmail)) {
      errors.email = 'Enter a valid email address.';
    }
    if (!password) {
      errors.password = 'Password is required.';
    }
    return errors;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (submitting) {
      return;
    }

    clearSessionNotice();
    setFormError('');
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setSubmitting(true);

    try {
      const result = await login({ email: email.trim(), password });
      if (result.ok) {
        navigate(result.redirectTo, { replace: true });
        return;
      }
      if (result.fieldErrors) {
        setFieldErrors(result.fieldErrors);
      }
      setFormError(result.error || 'Unable to sign in.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="container auth-grid">
        <section className="auth-card" aria-labelledby="login-heading">
          <div className="auth-card-header">
            <div className="mono-label">Account Access</div>
            <h1 id="login-heading" className="auth-title">
              Sign in to MAFIOSO
            </h1>
            <p className="auth-subtitle">
              Unified sign-in for students, consultants, and platform administrators
            </p>
          </div>

          {sessionExpiredNotice && (
            <div className="notice-banner notice-banner-info" role="status">
              Your session ended or expired. Sign in again to continue
            </div>
          )}

          {location.state && location.state.justRegistered && (
            <div className="notice-banner notice-banner-success" role="status">
              Student account created. Enter your password to sign in
            </div>
          )}

          {formError && (
            <div className="notice-banner notice-banner-error" role="alert">
              {formError}
            </div>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="login-email" className="form-label">
                Email address
              </label>
              <input
                id="login-email"
                name="email"
                type="email"
                autoComplete="email"
                placeholder="name@example.com"
                className={`form-input${fieldErrors.email ? ' form-input-error' : ''}`}
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (fieldErrors.email) {
                    setFieldErrors((prev) => ({ ...prev, email: '' }));
                  }
                }}
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={fieldErrors.email ? 'login-email-error' : undefined}
                disabled={submitting}
              />
              {fieldErrors.email && (
                <div id="login-email-error" className="field-error">
                  {fieldErrors.email}
                </div>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="login-password" className="form-label">
                Password
              </label>
              <div className="input-password-wrap">
                <input
                  id="login-password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="Enter your password"
                  className={`form-input form-input-with-toggle${
                    fieldErrors.password ? ' form-input-error' : ''
                  }`}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (fieldErrors.password) {
                      setFieldErrors((prev) => ({ ...prev, password: '' }));
                    }
                  }}
                  aria-invalid={Boolean(fieldErrors.password)}
                  aria-describedby={fieldErrors.password ? 'login-password-error' : undefined}
                  disabled={submitting}
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  disabled={submitting}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {fieldErrors.password && (
                <div id="login-password-error" className="field-error">
                  {fieldErrors.password}
                </div>
              )}
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={submitting}>
              <span>{submitting ? 'Signing in' : 'Sign in'}</span>
              <ArrowRightIcon />
            </button>
          </form>

          <div className="auth-card-footer">
            <span>New student applicant?</span>
            <Link to="/register" className="text-link">
              Create a student account
            </Link>
          </div>
        </section>

        <aside className="dossier-card auth-context-card" aria-label="Role-based session routing">
          <div className="dossier-header">
            <div>
              <div className="dossier-title">Session & Role Routing</div>
              <div className="dossier-subtitle">Authority enforced by Tomcat HttpSession</div>
            </div>
            <span className="auth-icon-box" aria-hidden="true">
              <LockIcon />
            </span>
          </div>

          <div className="dossier-list">
            {ROLE_DESTINATIONS.map((item) => (
              <div key={item.code} className="dossier-row">
                <span className="dossier-index">{item.code}</span>
                <div>
                  <div className="dossier-row-title">{item.role}</div>
                  <div className="dossier-row-detail">{item.summary}</div>
                </div>
                <span className="dossier-stage-meta">{item.route}</span>
              </div>
            ))}
          </div>

          <div className="dossier-footer">
            <span>HttpOnly cookie • CSRF token rotation</span>
            <span className="mono-label">BCrypt verified</span>
          </div>
        </aside>
      </div>
    </div>
  );
}
