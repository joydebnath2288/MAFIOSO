import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { DataTable } from '../components/DataTable';
import { DetailCard } from '../components/DetailCard';
import { DocumentVaultPanel } from '../components/DocumentVaultPanel';
import { ArrowLeftIcon, BookOpenIcon, PassportIcon, UserIcon } from '../components/Icons';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import {
  fetchAdminStudentDetail,
  reviewAdminDocument,
  updateAdminStudentStatus,
} from '../services/adminService';
import { formatDateOnly, formatDateTime } from '../utils/validation';

export function AdminStudentDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [toggling, setToggling] = useState(false);

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = await fetchAdminStudentDetail(id);
    if (res.ok && res.data && res.data.success && res.data.data) {
      setDetail(res.data.data);
    } else {
      setError(res.error || 'Unable to load student account details');
    }
    setLoading(false);
  }, [id]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  const student = detail && detail.student;
  const profile = detail && detail.profile;
  const applications = (detail && detail.applications) || [];
  const documents = (detail && detail.documents) || [];

  async function handleConfirmToggle() {
    if (!student || toggling) return;
    setToggling(true);
    setFeedback('');
    setError('');
    try {
      const nextStatus = student.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
      const res = await updateAdminStudentStatus(student.id, nextStatus);
      if (res.ok && res.data && res.data.success) {
        setFeedback(`Student account status updated to ${nextStatus}`);
        setConfirmOpen(false);
        await loadDetail();
      } else {
        setError(res.error || 'Unable to update student status');
        setConfirmOpen(false);
      }
    } finally {
      setToggling(false);
    }
  }

  async function handleReviewDocument(documentId, payload) {
    setFeedback('');
    const res = await reviewAdminDocument(documentId, payload);
    if (res.ok && res.data && res.data.success) {
      setFeedback('Document review updated.');
      await loadDetail();
    }
    return res;
  }

  const appColumns = [
    {
      key: 'referenceNumber',
      header: 'Reference',
      render: (row) => <span className="mono-val">{row.referenceNumber}</span>,
    },
    {
      key: 'preferredSubject',
      header: 'Program & University',
      render: (row) => (
        <div>
          <div className="table-primary-text">
            {row.degreeLevel} — {row.preferredSubject}
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{row.preferredUniversity}</div>
        </div>
      ),
    },
    {
      key: 'assignedConsultant',
      header: 'Consultant',
      render: (row) =>
        row.assignedConsultant ? (
          <span>{row.assignedConsultant.fullName}</span>
        ) : (
          <span className="mono-label">Unassigned</span>
        ),
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
          Manage
        </Link>
      ),
    },
  ];

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/staff/students" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Students</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">Student Record • #{id}</div>
          <h1 className="page-title">{student ? student.fullName : 'Student Details'}</h1>
          <p className="page-subtitle">
            {student ? student.email : 'Persisted applicant identity and account lifecycle state'}
          </p>
        </div>

        {student && (
          <div className="workspace-status-group">
            <StatusBadge status={student.status} />
            <button
              type="button"
              className="btn btn-primary btn-compact"
              onClick={() => setConfirmOpen(true)}
            >
              {student.status === 'ACTIVE' ? 'Deactivate Account' : 'Activate Account'}
            </button>
          </div>
        )}
      </header>

      {feedback && <NoticeBanner type="success">{feedback}</NoticeBanner>}
      {error && <NoticeBanner type="error">{error}</NoticeBanner>}

      {loading ? (
        <div className="status-panel">
          <span className="status-dot status-dot-pending" aria-hidden="true" />
          <span className="mono-label">Loading student details</span>
        </div>
      ) : student ? (
        <div className="admin-overview-stack">
          <div className="workspace-grid">
            <DetailCard
              title="Account & Personal Details"
              subtitle={`GET /api/v1/admin/students/${student.id}`}
              icon={UserIcon}
              badge={<StatusBadge status={student.status} />}
              rows={[
                { label: 'Student ID', value: `#${student.id}`, mono: true },
                { label: 'Full Name', value: student.fullName },
                { label: 'Email Address', value: student.email, mono: true },
                { label: 'Phone Number', value: student.phoneNumber || 'Not provided', mono: true },
                { label: 'Date of Birth', value: profile?.dateOfBirth || 'Not provided', mono: true },
                { label: 'Nationality', value: profile?.nationality || 'Not provided' },
                { label: 'Present Address', value: profile?.presentAddress || 'Not provided' },
                { label: 'Registered On', value: formatDateTime(student.createdAt), mono: true },
                { label: 'Last Login', value: student.lastLoginAt ? formatDateTime(student.lastLoginAt) : 'Never signed in', mono: true },
              ]}
            />

            <DetailCard
              title="Passport & Study Preferences"
              subtitle={`Profile completeness: ${profile ? profile.completionPercent : 0}%`}
              icon={PassportIcon}
              rows={[
                { label: 'Passport Number', value: profile?.passportNumber || 'Not provided', mono: true },
                { label: 'Passport Expiry', value: profile?.passportExpiryDate || 'Not provided', mono: true },
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
            title="Academic & English Proficiency"
            subtitle="Educational credentials"
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
              <h2 className="health-card-title">Student Applications</h2>
              <div className="health-card-endpoint">{applications.length} application(s)</div>
            </div>
            <DataTable
              columns={appColumns}
              rows={applications}
              emptyTitle="No applications submitted"
              emptyMessage="This student has not submitted any study applications yet."
            />
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

      <ConfirmDialog
        open={confirmOpen}
        title={
          student && student.status === 'ACTIVE'
            ? 'Deactivate student account'
            : 'Activate student account'
        }
        message={
          student
            ? student.status === 'ACTIVE'
              ? `Deactivating ${student.fullName} (${student.email}) will end any active session and block future sign-ins.`
              : `Activating ${student.fullName} (${student.email}) will restore login access.`
            : ''
        }
        confirmLabel={student && student.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        confirmVariant={student && student.status === 'ACTIVE' ? 'danger' : 'primary'}
        busy={toggling}
        onConfirm={handleConfirmToggle}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  );
}
