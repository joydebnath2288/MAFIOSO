import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { DataTable } from '../components/DataTable';
import { DetailCard } from '../components/DetailCard';
import { DocumentVaultPanel } from '../components/DocumentVaultPanel';
import { ArrowLeftIcon, BookOpenIcon, PassportIcon, UserIcon } from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import { fetchAssignedStudentDetail, reviewConsultantDocument } from '../services/consultantService';
import { formatDateOnly } from '../utils/validation';

export function ConsultantStudentDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = await fetchAssignedStudentDetail(id);
    if (res.ok && res.data && res.data.success && res.data.data) {
      setDetail(res.data.data);
    } else {
      setError(res.error || 'Unable to load assigned student dossier');
    }
    setLoading(false);
  }, [id]);

  useEffect(() => {
    document.title = 'Assigned Student Dossier — MAFIOSO';
    loadDetail();
  }, [loadDetail]);

  async function handleReviewDocument(documentId, payload) {
    setFeedback('');
    const res = await reviewConsultantDocument(documentId, payload);
    if (res.ok && res.data && res.data.success) {
      setFeedback('Document review updated.');
      await loadDetail();
    }
    return res;
  }

  const student = detail && detail.student;
  const profile = detail && detail.profile;
  const applications = (detail && detail.applications) || [];
  const documents = (detail && detail.documents) || [];

  const appColumns = [
    {
      key: 'referenceNumber',
      header: 'Reference',
      render: (row) => <span className="mono-val">{row.referenceNumber}</span>,
    },
    {
      key: 'preferredSubject',
      header: 'Program',
      render: (row) => (
        <div>
          <div className="table-primary-text">{row.degreeLevel} — {row.preferredSubject}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{row.preferredUniversity}</div>
        </div>
      ),
    },
    {
      key: 'intendedIntake',
      header: 'Intake',
      render: (row) => <span>{row.intendedIntake}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'submittedAt',
      header: 'Submitted',
      render: (row) => <span className="mono-val">{formatDateOnly(row.submittedAt)}</span>,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/staff/applications/${row.id}`} className="btn btn-secondary btn-compact">
          Open application
        </Link>
      ),
    },
  ];

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/staff/assigned-students" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Assigned Students</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">Assigned Student Dossier • #{id}</div>
          <h1 className="page-title">{student ? student.fullName : 'Student Dossier'}</h1>
          <p className="page-subtitle">{student ? student.email : 'Assigned student profile'}</p>
        </div>
        {student && <StatusBadge status={student.status} />}
      </header>

      {feedback && <NoticeBanner type="success">{feedback}</NoticeBanner>}
      {error && <NoticeBanner type="error">{error}</NoticeBanner>}

      {loading ? (
        <LoadingSpinner label="Loading student dossier" />
      ) : student ? (
        <div className="admin-overview-stack">
          <div className="workspace-grid">
            <DetailCard
              title="Personal Information"
              subtitle={`Completion: ${profile ? profile.completionPercent : 0}%`}
              icon={UserIcon}
              rows={[
                { label: 'Full Name', value: profile?.fullName || student.fullName },
                { label: 'Email', value: student.email, mono: true },
                { label: 'Phone', value: profile?.phoneNumber || student.phoneNumber || 'Not provided', mono: true },
                { label: 'Date of Birth', value: profile?.dateOfBirth || 'Not provided', mono: true },
                { label: 'Gender', value: profile?.gender || 'Not specified' },
                { label: 'Nationality', value: profile?.nationality || 'Not provided' },
                { label: 'Present Address', value: profile?.presentAddress || 'Not provided' },
                { label: 'Permanent Address', value: profile?.permanentAddress || 'Not provided' },
              ]}
            />

            <DetailCard
              title="Passport & Study Preferences"
              subtitle="Verified profile details"
              icon={PassportIcon}
              rows={[
                { label: 'Passport Number', value: profile?.passportNumber || 'Not provided', mono: true },
                { label: 'Issue / Expiry', value: `${profile?.passportIssueDate || '—'} / ${profile?.passportExpiryDate || '—'}`, mono: true },
                { label: 'Issuing Country', value: profile?.passportIssuingCountry || 'Not provided' },
                { label: 'Preferred Degree', value: profile?.preferredDegreeLevel || 'Not provided', mono: true },
                { label: 'Preferred Subject', value: profile?.preferredSubject || 'Not provided' },
                { label: 'Preferred University', value: profile?.preferredUniversity || 'Not provided' },
                { label: 'Preferred Intake', value: profile?.preferredIntake || 'Not provided' },
                {
                  label: 'Estimated Budget',
                  value: profile?.estimatedBudget != null ? `${profile.budgetCurrency || 'GBP'} ${profile.estimatedBudget}` : 'Not provided',
                  mono: true,
                },
                { label: 'Funding Source', value: profile?.fundingSource || 'Not provided' },
              ]}
            />
          </div>

          <DetailCard
            title="Academic & English Qualifications"
            subtitle="Educational background"
            icon={BookOpenIcon}
            rows={[
              { label: 'Highest Qualification', value: profile?.highestQualification || 'Not provided' },
              { label: 'SSC / Equivalent', value: profile?.sscResult || 'Not provided' },
              { label: 'HSC / Equivalent', value: profile?.hscResult || 'Not provided' },
              { label: 'Bachelor Institution', value: profile?.bachelorInstitution || 'Not provided' },
              { label: 'Bachelor Subject / CGPA', value: `${profile?.bachelorSubject || '—'} (${profile?.bachelorCgpa || '—'})` },
              { label: 'IELTS / PTE / TOEFL', value: `IELTS: ${profile?.ieltsScore || '—'} | PTE: ${profile?.pteScore || '—'} | TOEFL: ${profile?.toeflScore || '—'}`, mono: true },
            ]}
          />

          <section className="section-card">
            <div>
              <h2 className="health-card-title">Assigned Applications</h2>
              <div className="health-card-endpoint">Applications under your advisory scope</div>
            </div>
            <DataTable columns={appColumns} rows={applications} loading={false} />
          </section>

          <section className="section-card">
            <div>
              <h2 className="health-card-title">Student Documents</h2>
              <div className="health-card-endpoint">Download and review uploaded documents</div>
            </div>
            <DocumentVaultPanel
              documents={documents}
              canUpload={false}
              canReview={true}
              onReview={handleReviewDocument}
            />
          </section>
        </div>
      ) : null}
    </div>
  );
}
