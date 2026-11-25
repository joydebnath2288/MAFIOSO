import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightIcon,
  CheckCircleIcon,
  CompassIcon,
  GraduationCapIcon,
  HelpCircleIcon,
  MailIcon,
  ShieldIcon,
  UserIcon,
} from '../components/Icons';

const EMAIL_REGEX = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$/;

export function ContactPage() {
  useEffect(() => {
    document.title = 'Contact & Inquiries — MAFIOSO UK Education';
  }, []);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [targetDegree, setTargetDegree] = useState('BSc');
  const [targetIntake, setTargetIntake] = useState('September 2026');
  const [inquiryText, setInquiryText] = useState('');

  const [fieldErrors, setFieldErrors] = useState({});
  const [submittedData, setSubmittedData] = useState(null);

  function validate() {
    const errs = {};
    if (!fullName.trim()) {
      errs.fullName = 'Full name is required.';
    }
    const cleanEmail = email.trim().toLowerCase();
    if (!cleanEmail) {
      errs.email = 'Email address is required.';
    } else if (!EMAIL_REGEX.test(cleanEmail)) {
      errs.email = 'Please provide a valid email address.';
    }
    if (!inquiryText.trim()) {
      errs.inquiryText = 'Please enter your inquiry or question.';
    }
    return errs;
  }

  function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setFieldErrors(errs);
      return;
    }
    setFieldErrors({});

    // Honest handling: no fake backend success response
    setSubmittedData({
      fullName: fullName.trim(),
      email: email.trim(),
      phoneNumber: phoneNumber.trim() || 'Not specified',
      targetDegree,
      targetIntake,
      inquiryText: inquiryText.trim(),
    });
  }

  function handleReset() {
    setSubmittedData(null);
    setFullName('');
    setEmail('');
    setPhoneNumber('');
    setInquiryText('');
  }

  return (
    <div className="contact-page">
      {/* Page Header */}
      <section className="content-section" style={{ paddingBottom: '32px' }}>
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Get in Touch</span>
            </div>
            <h1 className="section-heading">Contact & Student Inquiries</h1>
            <p className="section-description">
              Have questions regarding UK university admissions, degree eligibility, or our
              consultancy workflow? Complete an inquiry form or create your student account for
              direct advisory access.
            </p>
          </div>
        </div>
      </section>

      {/* Main Grid */}
      <section className="content-section content-section-alt">
        <div className="container contact-grid">
          {/* Informational Guidance Panel */}
          <aside className="contact-info-card">
            <div>
              <div className="section-kicker">
                <span className="badge-muted">Portal Advisory</span>
              </div>
              <h2 className="service-title" style={{ fontSize: '20px' }}>
                How Inquiries are Handled
              </h2>
              <p className="service-desc">
                MAFIOSO manages student dossiers and admissions guidance through individual student
                accounts. To upload transcripts, receive individualized university shortlists, and
                communicate with assigned consultants, we recommend registering your student account.
              </p>
            </div>

            <div className="contact-info-item">
              <div className="contact-info-icon" aria-hidden="true">
                <GraduationCapIcon />
              </div>
              <div>
                <div className="contact-info-title">Admissions Scope</div>
                <div className="contact-info-text">
                  Exclusively focused on undergraduate (BSc) and postgraduate (MSc) programs across
                  the United Kingdom.
                </div>
              </div>
            </div>

            <div className="contact-info-item">
              <div className="contact-info-icon" aria-hidden="true">
                <CompassIcon />
              </div>
              <div>
                <div className="contact-info-title">Target Student Community</div>
                <div className="contact-info-text">
                  Supporting applicants from Bangladesh with HSC, A-Levels, or recognized Bachelor’s
                  degrees.
                </div>
              </div>
            </div>

            <div className="contact-info-item">
              <div className="contact-info-icon" aria-hidden="true">
                <ShieldIcon />
              </div>
              <div>
                <div className="contact-info-title">Consultancy Integrity</div>
                <div className="contact-info-text">
                  Direct, transparent evaluations with no unauthorized fee structures or unrealistic
                  guarantees.
                </div>
              </div>
            </div>

            <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid var(--border-hairline)' }}>
              <Link to="/register" className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }}>
                <span>Create Student Account</span>
                <ArrowRightIcon />
              </Link>
            </div>
          </aside>

          {/* Inquiry Form */}
          <section className="contact-form-card" aria-labelledby="inquiry-form-title">
            <h2 id="inquiry-form-title" className="service-title" style={{ fontSize: '20px', marginBottom: '8px' }}>
              Admissions Inquiry Form
            </h2>
            <p className="modal-subtitle" style={{ marginBottom: '24px' }}>
              Submit your academic background and question to receive guidance from our team.
            </p>

            {submittedData ? (
              <div role="status" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div className="notice-banner notice-banner-info">
                  <div style={{ fontWeight: 600, marginBottom: '4px' }}>Inquiry Recorded Locally</div>
                  <div>
                    Thank you, <span style={{ fontWeight: 600 }}>{submittedData.fullName}</span>.
                    General inquiries for <span style={{ fontWeight: 600 }}>{submittedData.targetDegree}</span> (
                    {submittedData.targetIntake}) are reviewed by our consultancy team.
                  </div>
                </div>

                <div className="health-card" style={{ padding: '16px 20px' }}>
                  <div className="metric-row">
                    <span style={{ fontWeight: 500 }}>Name:</span>
                    <span>{submittedData.fullName}</span>
                  </div>
                  <div className="metric-row">
                    <span style={{ fontWeight: 500 }}>Email:</span>
                    <span className="mono-val">{submittedData.email}</span>
                  </div>
                  <div className="metric-row">
                    <span style={{ fontWeight: 500 }}>Phone:</span>
                    <span className="mono-val">{submittedData.phoneNumber}</span>
                  </div>
                  <div className="metric-row">
                    <span style={{ fontWeight: 500 }}>Program Level:</span>
                    <span>{submittedData.targetDegree}</span>
                  </div>
                  <div className="metric-row">
                    <span style={{ fontWeight: 500 }}>Intake:</span>
                    <span>{submittedData.targetIntake}</span>
                  </div>
                </div>

                <div className="notice-banner notice-banner-pending" style={{ fontSize: '13px' }}>
                  Note: A public backend contact endpoint is not part of this release milestone. To
                  activate full profile review, upload credentials, and unlock personalized
                  consultation, please create a student account.
                </div>

                <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                  <Link to="/register" className="btn btn-primary">
                    <span>Register Student Account</span>
                    <ArrowRightIcon />
                  </Link>
                  <button type="button" className="btn btn-secondary" onClick={handleReset}>
                    Submit Another Inquiry
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleSubmit} noValidate className="auth-form">
                <div className="form-field">
                  <label htmlFor="contact-fullname" className="form-label">
                    Full Name <span aria-hidden="true">*</span>
                  </label>
                  <input
                    id="contact-fullname"
                    type="text"
                    className={`form-input${fieldErrors.fullName ? ' form-input-error' : ''}`}
                    placeholder="Enter your legal name"
                    value={fullName}
                    onChange={(e) => {
                      setFullName(e.target.value);
                      if (fieldErrors.fullName) {
                        setFieldErrors((prev) => ({ ...prev, fullName: '' }));
                      }
                    }}
                    aria-invalid={Boolean(fieldErrors.fullName)}
                    aria-describedby={fieldErrors.fullName ? 'contact-fullname-error' : undefined}
                    required
                  />
                  {fieldErrors.fullName && (
                    <div id="contact-fullname-error" className="field-error">
                      {fieldErrors.fullName}
                    </div>
                  )}
                </div>

                <div className="form-field">
                  <label htmlFor="contact-email" className="form-label">
                    Email Address <span aria-hidden="true">*</span>
                  </label>
                  <input
                    id="contact-email"
                    type="email"
                    className={`form-input${fieldErrors.email ? ' form-input-error' : ''}`}
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => {
                      setEmail(e.target.value);
                      if (fieldErrors.email) {
                        setFieldErrors((prev) => ({ ...prev, email: '' }));
                      }
                    }}
                    aria-invalid={Boolean(fieldErrors.email)}
                    aria-describedby={fieldErrors.email ? 'contact-email-error' : undefined}
                    required
                  />
                  {fieldErrors.email && (
                    <div id="contact-email-error" className="field-error">
                      {fieldErrors.email}
                    </div>
                  )}
                </div>

                <div className="form-field">
                  <label htmlFor="contact-phone" className="form-label">
                    Phone Number (Optional)
                  </label>
                  <input
                    id="contact-phone"
                    type="tel"
                    className="form-input"
                    placeholder="+8801XXXXXXXXX"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div className="form-field">
                    <label htmlFor="contact-degree" className="form-label">
                      Target Study Level
                    </label>
                    <select
                      id="contact-degree"
                      className="form-input"
                      value={targetDegree}
                      onChange={(e) => setTargetDegree(e.target.value)}
                    >
                      <option value="BSc">Undergraduate (BSc)</option>
                      <option value="MSc">Postgraduate (MSc / MA)</option>
                      <option value="Foundation">Foundation Pathway</option>
                      <option value="Other">General Inquiry</option>
                    </select>
                  </div>

                  <div className="form-field">
                    <label htmlFor="contact-intake" className="form-label">
                      Intended Intake
                    </label>
                    <select
                      id="contact-intake"
                      className="form-input"
                      value={targetIntake}
                      onChange={(e) => setTargetIntake(e.target.value)}
                    >
                      <option value="September 2026">September 2026</option>
                      <option value="January 2027">January 2027</option>
                      <option value="September 2027">September 2027</option>
                      <option value="Not Decided">Still Exploring</option>
                    </select>
                  </div>
                </div>

                <div className="form-field">
                  <label htmlFor="contact-inquiry" className="form-label">
                    Your Inquiry or Question <span aria-hidden="true">*</span>
                  </label>
                  <textarea
                    id="contact-inquiry"
                    rows="4"
                    className={`form-input${fieldErrors.inquiryText ? ' form-input-error' : ''}`}
                    placeholder="Briefly describe your current qualifications (e.g. HSC GPA / Bachelor's degree) and the UK courses you wish to explore..."
                    value={inquiryText}
                    onChange={(e) => {
                      setInquiryText(e.target.value);
                      if (fieldErrors.inquiryText) {
                        setFieldErrors((prev) => ({ ...prev, inquiryText: '' }));
                      }
                    }}
                    aria-invalid={Boolean(fieldErrors.inquiryText)}
                    aria-describedby={fieldErrors.inquiryText ? 'contact-inquiry-error' : undefined}
                    required
                  />
                  {fieldErrors.inquiryText && (
                    <div id="contact-inquiry-error" className="field-error">
                      {fieldErrors.inquiryText}
                    </div>
                  )}
                </div>

                <button type="submit" className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }}>
                  <span>Submit Inquiry</span>
                  <ArrowRightIcon />
                </button>
              </form>
            )}
          </section>
        </div>
      </section>
    </div>
  );
}
