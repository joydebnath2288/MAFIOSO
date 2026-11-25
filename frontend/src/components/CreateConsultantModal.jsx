import { useEffect, useMemo, useState } from 'react';
import { createAdminConsultant } from '../services/adminService';
import { EMAIL_REGEX, evaluatePasswordRules, validatePhoneInput } from '../utils/validation';
import { CloseIcon, EyeIcon, EyeOffIcon } from './Icons';

export function CreateConsultantModal({ open, onClose, onCreated }) {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const passwordRules = useMemo(() => evaluatePasswordRules(password), [password]);

  useEffect(() => {
    if (!open) {
      setFullName('');
      setEmail('');
      setPhoneNumber('');
      setPassword('');
      setConfirmPassword('');
      setShowPassword(false);
      setShowConfirmPassword(false);
      setFieldErrors({});
      setFormError('');
      return undefined;
    }

    function handleKeyDown(event) {
      if (event.key === 'Escape' && !submitting) {
        onClose();
      }
    }
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open, submitting, onClose]);

  if (!open) {
    return null;
  }

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

    const phoneErr = validatePhoneInput(phoneNumber);
    if (phoneErr) {
      errors.phoneNumber = phoneErr;
    }

    const failingRule = passwordRules.find((r) => !r.passed);
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
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setSubmitting(true);

    try {
      const res = await createAdminConsultant({
        fullName: fullName.trim(),
        email: email.trim(),
        phoneNumber: phoneNumber.trim() || null,
        password,
        confirmPassword,
      });

      if (res.ok && res.data && res.data.success) {
        const createdUser = res.data.data;
        setPassword('');
        setConfirmPassword('');
        onCreated(createdUser);
        return;
      }

      if (res.fieldErrors) {
        setFieldErrors(res.fieldErrors);
      }
      if (res.status === 409) {
        setFieldErrors((prev) => ({
          ...prev,
          email: res.error || 'An account with this email address already exists.',
        }));
      }
      setFormError(res.error || 'Unable to create consultant account.');
    } finally {
      setSubmitting(false);
    }
  }

  function clearError(field) {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: '' }));
    }
  }

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(e) => {
        if (e.target === e.currentTarget && !submitting) {
          onClose();
        }
      }}
    >
      <div
        className="modal-card modal-card-wide"
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-consultant-title"
      >
        <div className="modal-header">
          <div>
            <div className="mono-label">Admin Provisioning</div>
            <h2 id="create-consultant-title" className="modal-title">
              Create consultant account
            </h2>
          </div>
          <button
            type="button"
            className="modal-close-btn"
            onClick={onClose}
            disabled={submitting}
            aria-label="Close modal"
          >
            <CloseIcon />
          </button>
        </div>

        <p className="modal-subtitle">
          Role CONSULTANT is enforced server-side. Enter an initial credential directly without
          storing default passwords.
        </p>

        {formError && (
          <div className="notice-banner notice-banner-error" role="alert">
            {formError}
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="cons-full-name" className="form-label">
              Full name
            </label>
            <input
              id="cons-full-name"
              type="text"
              placeholder="Full name"
              className={`form-input${fieldErrors.fullName ? ' form-input-error' : ''}`}
              value={fullName}
              onChange={(e) => {
                setFullName(e.target.value);
                clearError('fullName');
              }}
              disabled={submitting}
            />
            {fieldErrors.fullName && <div className="field-error">{fieldErrors.fullName}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="cons-email" className="form-label">
              Email address
            </label>
            <input
              id="cons-email"
              type="email"
              placeholder="consultant@example.com"
              className={`form-input${fieldErrors.email ? ' form-input-error' : ''}`}
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                clearError('email');
              }}
              disabled={submitting}
            />
            {fieldErrors.email && <div className="field-error">{fieldErrors.email}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="cons-phone" className="form-label">
              Phone number <span className="form-label-optional">(optional)</span>
            </label>
            <input
              id="cons-phone"
              type="tel"
              placeholder="+880 1711 000000"
              className={`form-input${fieldErrors.phoneNumber ? ' form-input-error' : ''}`}
              value={phoneNumber}
              onChange={(e) => {
                setPhoneNumber(e.target.value);
                clearError('phoneNumber');
              }}
              disabled={submitting}
            />
            {fieldErrors.phoneNumber && <div className="field-error">{fieldErrors.phoneNumber}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="cons-password" className="form-label">
              Initial password
            </label>
            <div className="input-password-wrap">
              <input
                id="cons-password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Create initial password"
                className={`form-input form-input-with-toggle${
                  fieldErrors.password ? ' form-input-error' : ''
                }`}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  clearError('password');
                }}
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
            <ul className="password-checklist">
              {passwordRules.map((rule) => (
                <li
                  key={rule.key}
                  className={`password-rule${rule.passed ? ' password-rule-met' : ''}`}
                >
                  <span
                    className={`status-dot ${rule.passed ? 'status-dot-up' : 'status-dot-muted'}`}
                    aria-hidden="true"
                  />
                  <span>{rule.label}</span>
                </li>
              ))}
            </ul>
            {fieldErrors.password && <div className="field-error">{fieldErrors.password}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="cons-confirm" className="form-label">
              Confirm password
            </label>
            <div className="input-password-wrap">
              <input
                id="cons-confirm"
                type={showConfirmPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Repeat initial password"
                className={`form-input form-input-with-toggle${
                  fieldErrors.confirmPassword ? ' form-input-error' : ''
                }`}
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value);
                  clearError('confirmPassword');
                }}
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
              <div className="field-error">{fieldErrors.confirmPassword}</div>
            )}
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Creating consultant' : 'Create consultant'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
