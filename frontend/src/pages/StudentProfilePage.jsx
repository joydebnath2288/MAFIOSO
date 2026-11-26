import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeftIcon } from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import { fetchStudentProfile, updateStudentProfile } from '../services/studentService';

const EMPTY_FORM = {
  fullName: '',
  phoneNumber: '',
  dateOfBirth: '',
  gender: '',
  nationality: '',
  presentAddress: '',
  permanentAddress: '',
  passportNumber: '',
  passportIssueDate: '',
  passportExpiryDate: '',
  passportIssuingCountry: '',
  highestQualification: '',
  sscResult: '',
  hscResult: '',
  bachelorInstitution: '',
  bachelorSubject: '',
  bachelorCgpa: '',
  bachelorGraduationYear: '',
  ieltsScore: '',
  pteScore: '',
  toeflScore: '',
  otherEnglishQualification: '',
  preferredDegreeLevel: '',
  preferredSubject: '',
  preferredUniversity: '',
  preferredIntake: '',
  preferredStudyLocation: '',
  estimatedBudget: '',
  budgetCurrency: 'GBP',
  fundingSource: '',
  sponsorInfo: '',
};

function profileToForm(account, profile) {
  return {
    fullName: (profile && profile.fullName) || (account && account.fullName) || '',
    phoneNumber: (profile && profile.phoneNumber) || (account && account.phoneNumber) || '',
    dateOfBirth: (profile && profile.dateOfBirth) || '',
    gender: (profile && profile.gender) || '',
    nationality: (profile && profile.nationality) || '',
    presentAddress: (profile && profile.presentAddress) || '',
    permanentAddress: (profile && profile.permanentAddress) || '',
    passportNumber: (profile && profile.passportNumber) || '',
    passportIssueDate: (profile && profile.passportIssueDate) || '',
    passportExpiryDate: (profile && profile.passportExpiryDate) || '',
    passportIssuingCountry: (profile && profile.passportIssuingCountry) || '',
    highestQualification: (profile && profile.highestQualification) || '',
    sscResult: (profile && profile.sscResult) || '',
    hscResult: (profile && profile.hscResult) || '',
    bachelorInstitution: (profile && profile.bachelorInstitution) || '',
    bachelorSubject: (profile && profile.bachelorSubject) || '',
    bachelorCgpa: (profile && profile.bachelorCgpa) || '',
    bachelorGraduationYear:
      profile && profile.bachelorGraduationYear != null ? String(profile.bachelorGraduationYear) : '',
    ieltsScore: (profile && profile.ieltsScore) || '',
    pteScore: (profile && profile.pteScore) || '',
    toeflScore: (profile && profile.toeflScore) || '',
    otherEnglishQualification: (profile && profile.otherEnglishQualification) || '',
    preferredDegreeLevel: (profile && profile.preferredDegreeLevel) || '',
    preferredSubject: (profile && profile.preferredSubject) || '',
    preferredUniversity: (profile && profile.preferredUniversity) || '',
    preferredIntake: (profile && profile.preferredIntake) || '',
    preferredStudyLocation: (profile && profile.preferredStudyLocation) || '',
    estimatedBudget: profile && profile.estimatedBudget != null ? String(profile.estimatedBudget) : '',
    budgetCurrency: (profile && profile.budgetCurrency) || 'GBP',
    fundingSource: (profile && profile.fundingSource) || '',
    sponsorInfo: (profile && profile.sponsorInfo) || '',
  };
}

export function StudentProfilePage() {
  const [account, setAccount] = useState(null);
  const [profileMeta, setProfileMeta] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    document.title = 'My Profile — MAFIOSO';
    let active = true;
    async function load() {
      setLoading(true);
      setError('');
      const res = await fetchStudentProfile();
      if (!active) return;
      if (res.ok && res.data && res.data.success && res.data.data) {
        const data = res.data.data;
        setAccount(data.account || null);
        setProfileMeta(data.profile || null);
        setForm(profileToForm(data.account, data.profile));
      } else {
        setError(res.error || 'Unable to load student profile');
      }
      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, []);

  function updateField(name, value) {
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (saving) return;

    setSaving(true);
    setError('');
    setFeedback('');
    setFieldErrors({});
    try {
      const payload = {
        ...form,
        bachelorGraduationYear: form.bachelorGraduationYear ? Number(form.bachelorGraduationYear) : null,
      };
      const res = await updateStudentProfile(payload);
      if (res.ok && res.data && res.data.success && res.data.data) {
        const data = res.data.data;
        setAccount(data.account || account);
        setProfileMeta(data.profile || null);
        setForm(profileToForm(data.account || account, data.profile));
        setFeedback('Profile details saved.');
      } else {
        setFieldErrors(res.fieldErrors || {});
        setError(res.error || 'Unable to save profile');
      }
    } finally {
      setSaving(false);
    }
  }

  const completionPercent = profileMeta ? profileMeta.completionPercent : 0;
  const missingFields = (profileMeta && profileMeta.missingFields) || [];

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/student/dashboard" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Dashboard</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">Student Portal • {account?.email || 'Profile'}</div>
          <h1 className="page-title">My Student Profile</h1>
          <p className="page-subtitle">
            Personal, passport, academic, English proficiency, study preferences, and financial details
          </p>
        </div>

        {account && (
          <div className="workspace-status-group">
            <StatusBadge status={account.status} />
            <span className="mono-badge" style={{ fontVariantNumeric: 'tabular-nums' }}>
              {completionPercent}% Complete
            </span>
          </div>
        )}
      </header>

      {feedback && <NoticeBanner type="success">{feedback}</NoticeBanner>}
      {error && <NoticeBanner type="error">{error}</NoticeBanner>}

      {loading ? (
        <LoadingSpinner label="Loading student profile" />
      ) : (
        <div className="admin-overview-stack">
          <section className="section-card">
            <div className="section-card-header">
              <div>
                <h2 className="health-card-title">Dossier Completion</h2>
                <div className="health-card-endpoint">
                  {missingFields.length === 0
                    ? 'All core consultancy sections are complete'
                    : `Missing items: ${missingFields.join(', ')}`}
                </div>
              </div>
              <span className="mono-val">{completionPercent}%</span>
            </div>
            <div className="profile-completion-bar-track">
              <div className="profile-completion-bar-fill" style={{ width: `${completionPercent}%` }} />
            </div>
          </section>

          <form className="admin-overview-stack" onSubmit={handleSubmit} noValidate>
            {/* 1. Personal Information */}
            <section className="section-card">
              <div>
                <h2 className="health-card-title">Personal Information</h2>
                <div className="health-card-endpoint">Identity & contact details</div>
              </div>

              <div className="form-grid-3">
                <div className="form-field">
                  <label htmlFor="prof-fullname" className="form-label">Full name</label>
                  <input
                    id="prof-fullname"
                    type="text"
                    className={`form-input${fieldErrors.fullName ? ' form-input-error' : ''}`}
                    value={form.fullName}
                    onChange={(e) => updateField('fullName', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.fullName && <span className="field-error">{fieldErrors.fullName}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-phone" className="form-label">Phone number</label>
                  <input
                    id="prof-phone"
                    type="tel"
                    className={`form-input${fieldErrors.phoneNumber ? ' form-input-error' : ''}`}
                    placeholder="+8801711000000"
                    value={form.phoneNumber}
                    onChange={(e) => updateField('phoneNumber', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.phoneNumber && <span className="field-error">{fieldErrors.phoneNumber}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-dob" className="form-label">Date of birth</label>
                  <input
                    id="prof-dob"
                    type="date"
                    className={`form-input${fieldErrors.dateOfBirth ? ' form-input-error' : ''}`}
                    value={form.dateOfBirth}
                    onChange={(e) => updateField('dateOfBirth', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.dateOfBirth && <span className="field-error">{fieldErrors.dateOfBirth}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-gender" className="form-label">
                    Gender <span className="form-label-optional">(optional)</span>
                  </label>
                  <select
                    id="prof-gender"
                    className="form-input"
                    value={form.gender}
                    onChange={(e) => updateField('gender', e.target.value)}
                    disabled={saving}
                  >
                    <option value="">Prefer not to specify</option>
                    <option value="Female">Female</option>
                    <option value="Male">Male</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-field">
                  <label htmlFor="prof-nationality" className="form-label">Nationality</label>
                  <input
                    id="prof-nationality"
                    type="text"
                    className={`form-input${fieldErrors.nationality ? ' form-input-error' : ''}`}
                    placeholder="e.g. Bangladeshi"
                    value={form.nationality}
                    onChange={(e) => updateField('nationality', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.nationality && <span className="field-error">{fieldErrors.nationality}</span>}
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-field">
                  <label htmlFor="prof-present-addr" className="form-label">Present address</label>
                  <textarea
                    id="prof-present-addr"
                    rows={2}
                    className={`form-input form-textarea${fieldErrors.presentAddress ? ' form-input-error' : ''}`}
                    value={form.presentAddress}
                    onChange={(e) => updateField('presentAddress', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.presentAddress && <span className="field-error">{fieldErrors.presentAddress}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-perm-addr" className="form-label">Permanent address</label>
                  <textarea
                    id="prof-perm-addr"
                    rows={2}
                    className={`form-input form-textarea${fieldErrors.permanentAddress ? ' form-input-error' : ''}`}
                    value={form.permanentAddress}
                    onChange={(e) => updateField('permanentAddress', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.permanentAddress && <span className="field-error">{fieldErrors.permanentAddress}</span>}
                </div>
              </div>
            </section>

            {/* 2. Passport Information */}
            <section className="section-card">
              <div>
                <h2 className="health-card-title">Passport Information</h2>
                <div className="health-card-endpoint">Protected travel identity fields</div>
              </div>

              <div className="form-grid-2">
                <div className="form-field">
                  <label htmlFor="prof-passport-num" className="form-label">Passport number</label>
                  <input
                    id="prof-passport-num"
                    type="text"
                    className={`form-input${fieldErrors.passportNumber ? ' form-input-error' : ''}`}
                    value={form.passportNumber}
                    onChange={(e) => updateField('passportNumber', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.passportNumber && <span className="field-error">{fieldErrors.passportNumber}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-passport-country" className="form-label">Issuing country</label>
                  <input
                    id="prof-passport-country"
                    type="text"
                    className={`form-input${fieldErrors.passportIssuingCountry ? ' form-input-error' : ''}`}
                    placeholder="e.g. Bangladesh"
                    value={form.passportIssuingCountry}
                    onChange={(e) => updateField('passportIssuingCountry', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.passportIssuingCountry && (
                    <span className="field-error">{fieldErrors.passportIssuingCountry}</span>
                  )}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-passport-issue" className="form-label">Issue date</label>
                  <input
                    id="prof-passport-issue"
                    type="date"
                    className={`form-input${fieldErrors.passportIssueDate ? ' form-input-error' : ''}`}
                    value={form.passportIssueDate}
                    onChange={(e) => updateField('passportIssueDate', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.passportIssueDate && <span className="field-error">{fieldErrors.passportIssueDate}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-passport-expiry" className="form-label">Expiry date</label>
                  <input
                    id="prof-passport-expiry"
                    type="date"
                    className={`form-input${fieldErrors.passportExpiryDate ? ' form-input-error' : ''}`}
                    value={form.passportExpiryDate}
                    onChange={(e) => updateField('passportExpiryDate', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.passportExpiryDate && <span className="field-error">{fieldErrors.passportExpiryDate}</span>}
                </div>
              </div>
            </section>

            {/* 3. Academic & English Proficiency */}
            <section className="section-card">
              <div>
                <h2 className="health-card-title">Academic & English Proficiency</h2>
                <div className="health-card-endpoint">Qualifications and test scores</div>
              </div>

              <div className="form-grid-3">
                <div className="form-field">
                  <label htmlFor="prof-highest-qual" className="form-label">Highest qualification</label>
                  <input
                    id="prof-highest-qual"
                    type="text"
                    className="form-input"
                    placeholder="e.g. HSC / A-Levels / BSc"
                    value={form.highestQualification}
                    onChange={(e) => updateField('highestQualification', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-ssc" className="form-label">SSC or equivalent result</label>
                  <input
                    id="prof-ssc"
                    type="text"
                    className="form-input"
                    placeholder="e.g. GPA 5.00"
                    value={form.sscResult}
                    onChange={(e) => updateField('sscResult', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-hsc" className="form-label">HSC or equivalent result</label>
                  <input
                    id="prof-hsc"
                    type="text"
                    className="form-input"
                    placeholder="e.g. GPA 5.00 / AAB"
                    value={form.hscResult}
                    onChange={(e) => updateField('hscResult', e.target.value)}
                    disabled={saving}
                  />
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-field">
                  <label htmlFor="prof-bachelor-inst" className="form-label">
                    Bachelor institution <span className="form-label-optional">(if applicable)</span>
                  </label>
                  <input
                    id="prof-bachelor-inst"
                    type="text"
                    className="form-input"
                    value={form.bachelorInstitution}
                    onChange={(e) => updateField('bachelorInstitution', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-bachelor-subj" className="form-label">
                    Bachelor subject <span className="form-label-optional">(if applicable)</span>
                  </label>
                  <input
                    id="prof-bachelor-subj"
                    type="text"
                    className="form-input"
                    value={form.bachelorSubject}
                    onChange={(e) => updateField('bachelorSubject', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-bachelor-cgpa" className="form-label">CGPA or equivalent result</label>
                  <input
                    id="prof-bachelor-cgpa"
                    type="text"
                    className="form-input"
                    placeholder="e.g. 3.72 / 4.00"
                    value={form.bachelorCgpa}
                    onChange={(e) => updateField('bachelorCgpa', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-bachelor-year" className="form-label">Graduation year</label>
                  <input
                    id="prof-bachelor-year"
                    type="number"
                    min="1960"
                    max="2035"
                    className={`form-input${fieldErrors.bachelorGraduationYear ? ' form-input-error' : ''}`}
                    placeholder="e.g. 2025"
                    value={form.bachelorGraduationYear}
                    onChange={(e) => updateField('bachelorGraduationYear', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.bachelorGraduationYear && (
                    <span className="field-error">{fieldErrors.bachelorGraduationYear}</span>
                  )}
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-field">
                  <label htmlFor="prof-ielts" className="form-label">IELTS score</label>
                  <input
                    id="prof-ielts"
                    type="text"
                    className="form-input"
                    placeholder="e.g. 7.0 overall (6.5 min)"
                    value={form.ieltsScore}
                    onChange={(e) => updateField('ieltsScore', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-pte" className="form-label">PTE score</label>
                  <input
                    id="prof-pte"
                    type="text"
                    className="form-input"
                    value={form.pteScore}
                    onChange={(e) => updateField('pteScore', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-toefl" className="form-label">TOEFL score</label>
                  <input
                    id="prof-toefl"
                    type="text"
                    className="form-input"
                    value={form.toeflScore}
                    onChange={(e) => updateField('toeflScore', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-other-eng" className="form-label">Other English qualification</label>
                  <input
                    id="prof-other-eng"
                    type="text"
                    className="form-input"
                    placeholder="e.g. Medium of Instruction letter"
                    value={form.otherEnglishQualification}
                    onChange={(e) => updateField('otherEnglishQualification', e.target.value)}
                    disabled={saving}
                  />
                </div>
              </div>
            </section>

            {/* 4. Study Preferences & Financial Plan */}
            <section className="section-card">
              <div>
                <h2 className="health-card-title">Study Preferences & Financial Plan</h2>
                <div className="health-card-endpoint">Target UK program and funding details</div>
              </div>

              <div className="form-grid-3">
                <div className="form-field">
                  <label htmlFor="prof-degree" className="form-label">Preferred degree level</label>
                  <select
                    id="prof-degree"
                    className={`form-input${fieldErrors.preferredDegreeLevel ? ' form-input-error' : ''}`}
                    value={form.preferredDegreeLevel}
                    onChange={(e) => updateField('preferredDegreeLevel', e.target.value)}
                    disabled={saving}
                  >
                    <option value="">Select degree level</option>
                    <option value="BSc">BSc</option>
                    <option value="MSc">MSc</option>
                  </select>
                  {fieldErrors.preferredDegreeLevel && (
                    <span className="field-error">{fieldErrors.preferredDegreeLevel}</span>
                  )}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-pref-subj" className="form-label">Preferred subject</label>
                  <input
                    id="prof-pref-subj"
                    type="text"
                    className="form-input"
                    value={form.preferredSubject}
                    onChange={(e) => updateField('preferredSubject', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-pref-uni" className="form-label">Preferred university</label>
                  <input
                    id="prof-pref-uni"
                    type="text"
                    className="form-input"
                    value={form.preferredUniversity}
                    onChange={(e) => updateField('preferredUniversity', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-pref-intake" className="form-label">Preferred intake</label>
                  <input
                    id="prof-pref-intake"
                    type="text"
                    className="form-input"
                    placeholder="e.g. September 2026"
                    value={form.preferredIntake}
                    onChange={(e) => updateField('preferredIntake', e.target.value)}
                    disabled={saving}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="prof-pref-loc" className="form-label">Preferred study location</label>
                  <input
                    id="prof-pref-loc"
                    type="text"
                    className="form-input"
                    placeholder="e.g. London, Manchester"
                    value={form.preferredStudyLocation}
                    onChange={(e) => updateField('preferredStudyLocation', e.target.value)}
                    disabled={saving}
                  />
                </div>
              </div>

              <div className="form-grid-3">
                <div className="form-field">
                  <label htmlFor="prof-budget" className="form-label">Estimated annual study budget</label>
                  <input
                    id="prof-budget"
                    type="number"
                    min="0"
                    step="100"
                    className={`form-input${fieldErrors.estimatedBudget ? ' form-input-error' : ''}`}
                    placeholder="e.g. 22000"
                    value={form.estimatedBudget}
                    onChange={(e) => updateField('estimatedBudget', e.target.value)}
                    disabled={saving}
                  />
                  {fieldErrors.estimatedBudget && <span className="field-error">{fieldErrors.estimatedBudget}</span>}
                </div>

                <div className="form-field">
                  <label htmlFor="prof-currency" className="form-label">Currency</label>
                  <select
                    id="prof-currency"
                    className="form-input"
                    value={form.budgetCurrency}
                    onChange={(e) => updateField('budgetCurrency', e.target.value)}
                    disabled={saving}
                  >
                    <option value="GBP">GBP (£)</option>
                    <option value="USD">USD ($)</option>
                    <option value="BDT">BDT (৳)</option>
                    <option value="EUR">EUR (€)</option>
                  </select>
                </div>

                <div className="form-field">
                  <label htmlFor="prof-funding" className="form-label">Funding source</label>
                  <input
                    id="prof-funding"
                    type="text"
                    className="form-input"
                    placeholder="e.g. Family Sponsor, Personal Savings, Education Loan"
                    value={form.fundingSource}
                    onChange={(e) => updateField('fundingSource', e.target.value)}
                    disabled={saving}
                  />
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="prof-sponsor" className="form-label">
                  Sponsor information <span className="form-label-optional">(where applicable)</span>
                </label>
                <textarea
                  id="prof-sponsor"
                  rows={2}
                  className="form-input form-textarea"
                  placeholder="Sponsor name, relationship, and financial support notes"
                  value={form.sponsorInfo}
                  onChange={(e) => updateField('sponsorInfo', e.target.value)}
                  disabled={saving}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <Link to="/student/dashboard" className="btn btn-secondary">
                  Cancel
                </Link>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving' : 'Save profile'}
                </button>
              </div>
            </section>
          </form>
        </div>
      )}
    </div>
  );
}
