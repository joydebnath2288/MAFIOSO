import { useEffect, useState } from 'react';
import { CloseIcon } from './Icons';
import { createStudentApplication } from '../services/studentService';

export function CreateApplicationModal({ open, onClose, onCreated, defaultPreferences = null }) {
  const [degreeLevel, setDegreeLevel] = useState('MSc');
  const [preferredSubject, setPreferredSubject] = useState('');
  const [intendedIntake, setIntendedIntake] = useState('September 2026');
  const [preferredUniversity, setPreferredUniversity] = useState('');
  const [preferredLocation, setPreferredLocation] = useState('');
  const [personalStatement, setPersonalStatement] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (open) {
      setDegreeLevel(defaultPreferences?.preferredDegreeLevel || 'MSc');
      setPreferredSubject(defaultPreferences?.preferredSubject || '');
      setIntendedIntake(defaultPreferences?.preferredIntake || 'September 2026');
      setPreferredUniversity(defaultPreferences?.preferredUniversity || '');
      setPreferredLocation(defaultPreferences?.preferredStudyLocation || '');
      setPersonalStatement('');
      setFieldErrors({});
      setError('');
    }
  }, [open, defaultPreferences]);

  useEffect(() => {
    if (!open) return undefined;
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

  async function handleSubmit(event) {
    event.preventDefault();
    if (submitting) return;

    const nextErrors = {};
    if (!degreeLevel) nextErrors.degreeLevel = 'Select BSc or MSc.';
    if (!preferredSubject.trim() || preferredSubject.trim().length < 2) {
      nextErrors.preferredSubject = 'Enter your preferred subject.';
    }
    if (!intendedIntake.trim() || intendedIntake.trim().length < 3) {
      nextErrors.intendedIntake = 'Select or enter your intended intake.';
    }
    if (!preferredUniversity.trim() || preferredUniversity.trim().length < 2) {
      nextErrors.preferredUniversity = 'Enter your preferred UK university.';
    }
    if (Object.keys(nextErrors).length > 0) {
      setFieldErrors(nextErrors);
      return;
    }

    setSubmitting(true);
    setError('');
    setFieldErrors({});
    try {
      const res = await createStudentApplication({
        degreeLevel,
        preferredSubject: preferredSubject.trim(),
        intendedIntake: intendedIntake.trim(),
        preferredUniversity: preferredUniversity.trim(),
        preferredLocation: preferredLocation.trim() || null,
        personalStatement: personalStatement.trim() || null,
      });
      if (res.ok && res.data && res.data.success) {
        onCreated(res.data.data);
      } else {
        setFieldErrors(res.fieldErrors || {});
        setError(res.error || 'Unable to submit application');
      }
    } finally {
      setSubmitting(false);
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
      <div className="modal-card modal-card-wide" role="dialog" aria-modal="true" aria-labelledby="create-app-title">
        <div className="modal-header">
          <div>
            <div className="mono-label">New UK study request</div>
            <h2 id="create-app-title" className="modal-title">
              Create application
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
          A unique MAFIOSO reference number will be generated upon submission.
        </p>

        {error && (
          <div className="notice-banner notice-banner-error" role="alert">
            {error}
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-grid-2">
            <div className="form-field">
              <label htmlFor="app-degree" className="form-label">
                Degree level
              </label>
              <select
                id="app-degree"
                className={`form-input${fieldErrors.degreeLevel ? ' form-input-error' : ''}`}
                value={degreeLevel}
                onChange={(e) => setDegreeLevel(e.target.value)}
                disabled={submitting}
              >
                <option value="BSc">BSc (Undergraduate)</option>
                <option value="MSc">MSc (Postgraduate)</option>
              </select>
              {fieldErrors.degreeLevel && <span className="field-error">{fieldErrors.degreeLevel}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="app-intake" className="form-label">
                Intended intake
              </label>
              <select
                id="app-intake"
                className={`form-input${fieldErrors.intendedIntake ? ' form-input-error' : ''}`}
                value={intendedIntake}
                onChange={(e) => setIntendedIntake(e.target.value)}
                disabled={submitting}
              >
                <option value="September 2026">September 2026</option>
                <option value="January 2027">January 2027</option>
                <option value="May 2027">May 2027</option>
                <option value="September 2027">September 2027</option>
              </select>
              {fieldErrors.intendedIntake && <span className="field-error">{fieldErrors.intendedIntake}</span>}
            </div>
          </div>

          <div className="form-field">
            <label htmlFor="app-subject" className="form-label">
              Preferred subject
            </label>
            <input
              id="app-subject"
              type="text"
              className={`form-input${fieldErrors.preferredSubject ? ' form-input-error' : ''}`}
              placeholder="e.g. MSc Data Science or BSc Computer Science"
              value={preferredSubject}
              onChange={(e) => setPreferredSubject(e.target.value)}
              disabled={submitting}
            />
            {fieldErrors.preferredSubject && <span className="field-error">{fieldErrors.preferredSubject}</span>}
          </div>

          <div className="form-grid-2">
            <div className="form-field">
              <label htmlFor="app-university" className="form-label">
                Preferred university
              </label>
              <input
                id="app-university"
                type="text"
                className={`form-input${fieldErrors.preferredUniversity ? ' form-input-error' : ''}`}
                placeholder="e.g. University of Manchester"
                value={preferredUniversity}
                onChange={(e) => setPreferredUniversity(e.target.value)}
                disabled={submitting}
              />
              {fieldErrors.preferredUniversity && (
                <span className="field-error">{fieldErrors.preferredUniversity}</span>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="app-location" className="form-label">
                Preferred UK city <span className="form-label-optional">(optional)</span>
              </label>
              <input
                id="app-location"
                type="text"
                className="form-input"
                placeholder="e.g. London, Manchester, Birmingham"
                value={preferredLocation}
                onChange={(e) => setPreferredLocation(e.target.value)}
                disabled={submitting}
              />
            </div>
          </div>

          <div className="form-field">
            <label htmlFor="app-statement" className="form-label">
              Applicant notes <span className="form-label-optional">(optional)</span>
            </label>
            <textarea
              id="app-statement"
              rows={3}
              className="form-input form-textarea"
              placeholder="Any specific course preferences, scholarship goals, or timeline notes"
              value={personalStatement}
              onChange={(e) => setPersonalStatement(e.target.value)}
              disabled={submitting}
            />
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Submitting' : 'Submit application'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
