import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRightIcon, EyeIcon, EyeOffIcon, ShieldIcon } from '../components/Icons';
import { useAuth } from '../hooks/useAuth';
import { EMAIL_REGEX, evaluatePasswordRules, validatePhoneInput } from '../utils/validation';

const REGISTRATION_GUARDS = [
  {
    code: '01',
    title: 'Student Role Assignment',
    detail: 'Public sign-up always assigns STUDENT server-side; staff roles require admin provisioning',
    meta: 'ROLE_STUDENT',
  },
  {
    code: '02',
    title: 'BCrypt Credential Protection',
    detail: 'Passwords are salted and hashed server-side before storage in MySQL 8',
    meta: 'Cost 12',
  },
  {
    code: '03',
    title: 'CSRF Token Verification',
    detail: 'Registration requests require a session-bound X-CSRF-Token header',
    meta: 'Filter Guard',
  },
];

export function RegisterPage() {
  const { register } = useAuth();

  useEffect(() => {
    document.title = 'Student Registration — MAFIOSO';
  }, []);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [createdEmail, setCreatedEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const passwordRules = useMemo(() => evaluatePasswordRules(password), [password]);

  function validateForm() {
    const errors = {};
    const cleanName = fullName.trim().replace(/\s+/g, ' ');
    const cleanEmail = email.trim().toLowerCase();

    if (!cleanName) {
      errors.fullName = 'Full name is required.';
    } else if (cleanName.length < 2 || cleanName.length > 120) {
      errors.fullName = 'Full name must be between 2 and 120 characters.';
    }

    if (!cleanEmail) {
      errors.email = 'Email address is required.';
    } else if (!EMAIL_REGEX.test(cleanEmail) || cleanEmail.includes('..')) {
      errors.email = 'Enter a valid email address.';
    }

    const phoneError = validatePhoneInput(phoneNumber);
    if (phoneError) {
      errors.phoneNumber = phoneError;
    }

    const failingRule = passwordRules.find((rule) => !rule.passed);
    if (!password) {
      errors.password = 'Password is required.';
    } else if (failingRule) {
      errors.password =
        'Password must be 8 to 72 bytes and include uppercase, lowercase, a number, and a special character.';
    }

    if (!confirmPassword) {
      errors.confirmPassword = 'Confirm password is required.';
    } else if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match.';
    }

    return errors;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (submitting) {
      return;
    }

    setFormError('');
    setCreatedEmail('');
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setSubmitting(true);

    try {
      const result = await register({
        fullName: fullName.trim(),
        email: email.trim(),
        phoneNumber: phoneNumber.trim() || null,
        password,
        confirmPassword,
      });

      if (result.ok) {
        const registeredAddress = (result.user && result.user.email) || email.trim().toLowerCase();
        setCreatedEmail(registeredAddress);
        setPassword('');
        setConfirmPassword('');
        return;
      }

      if (result.fieldErrors) {
        setFieldErrors(result.fieldErrors);
      }
      if (result.status === 409) {
        setFieldErrors((prev) => ({
          ...prev,
          email: result.error || 'An account with this email address already exists.',
        }));
      }
      setFormError(result.error || 'Unable to complete registration.');
    } finally {
      setSubmitting(false);
    }
  }

  function clearFieldError(field) {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: '' }));
    }
  }

  return (
    <div className="auth-page">
      <div className="container auth-grid">
        <section className="auth-card" aria-labelledby="register-heading">
          <div className="auth-card-header">
            <div className="mono-label">Student Enrollment</div>
            <h1 id="register-heading" className="auth-title">
              Create student account
            </h1>
            <p className="auth-subtitle">
              Register to begin UK undergraduate or postgraduate admissions guidance
            </p>
          </div>

          {createdEmail && (
            <div className="notice-banner notice-banner-success" role="status">
              <div>
                Student account created for <span className="mono-val">{createdEmail}</span>
              </div>
              <Link
                to="/login"
                state={{ justRegistered: true, registeredEmail: createdEmail }}
                className="text-link"
              >
                Continue to sign in
              </Link>
            </div>
          )}

          {formError && (
            <div className="notice-banner notice-banner-error" role="alert">
              {formError}
            </div>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="reg-full-name" className="form-label">
                Full name
              </label>
              <input
                id="reg-full-name"
                name="fullName"
                type="text"
                autoComplete="name"
                placeholder="Full name"
                className={`form-input${fieldErrors.fullName ? ' form-input-error' : ''}`}
                value={fullName}
                onChange={(e) => {
                  setFullName(e.target.value);
                  clearFieldError('fullName');
                }}
                aria-invalid={Boolean(fieldErrors.fullName)}
                aria-describedby={fieldErrors.fullName ? 'reg-full-name-error' : undefined}
                disabled={submitting}
              />
              {fieldErrors.fullName && (
                <div id="reg-full-name-error" className="field-error">
                  {fieldErrors.fullName}
                </div>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="reg-email" className="form-label">
                Email address
              </label>
              <input
                id="reg-email"
                name="email"
                type="email"
                autoComplete="email"
                placeholder="name@example.com"
                className={`form-input${fieldErrors.email ? ' form-input-error' : ''}`}
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  clearFieldError('email');
                }}
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={fieldErrors.email ? 'reg-email-error' : undefined}
                disabled={submitting}
              />
              {fieldErrors.email && (
                <div id="reg-email-error" className="field-error">
                  {fieldErrors.email}
                </div>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="reg-phone" className="form-label">
                Phone number <span className="form-label-optional">(optional)</span>
              </label>
              <input
                id="reg-phone"
                name="phoneNumber"
                type="tel"
                autoComplete="tel"
                placeholder="+880 1711 000000"
                className={`form-input${fieldErrors.phoneNumber ? ' form-input-error' : ''}`}
                value={phoneNumber}
                onChange={(e) => {
                  setPhoneNumber(e.target.value);
                  clearFieldError('phoneNumber');
                }}
                aria-invalid={Boolean(fieldErrors.phoneNumber)}
                aria-describedby={fieldErrors.phoneNumber ? 'reg-phone-error' : undefined}
                disabled={submitting}
              />
              {fieldErrors.phoneNumber && (
                <div id="reg-phone-error" className="field-error">
                  {fieldErrors.phoneNumber}
                </div>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="reg-password" className="form-label">
                Password
              </label>
              <div className="input-password-wrap">
                <input
                  id="reg-password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Create password"
                  className={`form-input form-input-with-toggle${
                    fieldErrors.password ? ' form-input-error' : ''
                  }`}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    clearFieldError('password');
                  }}
                  aria-invalid={Boolean(fieldErrors.password)}
                  aria-describedby="reg-password-rules"
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
              <ul id="reg-password-rules" className="password-checklist">
                {passwordRules.map((rule) => (
                  <li
                    key={rule.key}
                    className={`password-rule${rule.passed ? ' password-rule-met' : ''}`}
                  >
                    <span
                      className={`status-dot ${
                        rule.passed ? 'status-dot-up' : 'status-dot-muted'
                      }`}
                      aria-hidden="true"
                    />
                    <span>{rule.label}</span>
                  </li>
                ))}
              </ul>
              {fieldErrors.password && (
                <div className="field-error">{fieldErrors.password}</div>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="reg-confirm-password" className="form-label">
                Confirm password
              </label>
              <div className="input-password-wrap">
                <input
                  id="reg-confirm-password"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Repeat password"
                  className={`form-input form-input-with-toggle${
                    fieldErrors.confirmPassword ? ' form-input-error' : ''
                  }`}
                  value={confirmPassword}
                  onChange={(e) => {
                    setConfirmPassword(e.target.value);
                    clearFieldError('confirmPassword');
                  }}
                  aria-invalid={Boolean(fieldErrors.confirmPassword)}
                  aria-describedby={
                    fieldErrors.confirmPassword ? 'reg-confirm-error' : undefined
                  }
                  disabled={submitting}
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowConfirmPassword((prev) => !prev)}
                  aria-label={showConfirmPassword ? 'Hide confirm password' : 'Show confirm password'}
                  disabled={submitting}
                >
                  {showConfirmPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {fieldErrors.confirmPassword && (
                <div id="reg-confirm-error" className="field-error">
                  {fieldErrors.confirmPassword}
                </div>
              )}
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={submitting}>
              <span>{submitting ? 'Creating account' : 'Create student account'}</span>
              <ArrowRightIcon />
            </button>
          </form>

          <div className="auth-card-footer">
            <span>Already have an account?</span>
            <Link to="/login" className="text-link">
              Sign in
            </Link>
          </div>
        </section>

        <aside className="dossier-card auth-context-card" aria-label="Account registration controls">
          <div className="dossier-header">
            <div>
              <div className="dossier-title">Registration Safeguards</div>
              <div className="dossier-subtitle">Server-side controls applied on every sign-up</div>
            </div>
            <span className="auth-icon-box" aria-hidden="true">
              <ShieldIcon />
            </span>
          </div>

          <div className="dossier-list">
            {REGISTRATION_GUARDS.map((item) => (
              <div key={item.code} className="dossier-row">
                <span className="dossier-index">{item.code}</span>
                <div>
                  <div className="dossier-row-title">{item.title}</div>
                  <div className="dossier-row-detail">{item.detail}</div>
                </div>
                <span className="dossier-stage-meta">{item.meta}</span>
              </div>
            ))}
          </div>

          <div className="dossier-footer">
            <span>Staff accounts require admin provisioning</span>
            <span className="mono-label">Bangladesh → UK</span>
          </div>
        </aside>
      </div>
    </div>
  );
}
