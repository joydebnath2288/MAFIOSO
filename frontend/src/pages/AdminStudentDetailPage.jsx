import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { DetailCard } from '../components/DetailCard';
import { ArrowLeftIcon, ShieldIcon, UserIcon } from '../components/Icons';
import { StatusBadge } from '../components/StatusBadge';
import { fetchAdminStudentDetail, updateAdminStudentStatus } from '../services/adminService';
import { formatDateTime } from '../utils/validation';

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

  async function handleConfirmToggle() {
    if (!student || toggling) {
      return;
    }
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

      {feedback && (
        <div className="notice-banner notice-banner-success" role="status">
          {feedback}
        </div>
      )}

      {error && (
        <div className="notice-banner notice-banner-error" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <div className="status-panel">
          <span className="status-dot status-dot-pending" aria-hidden="true" />
          <span className="mono-label">Loading student details</span>
        </div>
      ) : student ? (
        <div className="workspace-grid">
          <DetailCard
            title="Account Record"
            subtitle={`GET /api/v1/admin/students/${student.id}`}
            icon={UserIcon}
            badge={<StatusBadge status={student.status} />}
            rows={[
              { label: 'Student ID', value: `#${student.id}`, mono: true },
              { label: 'Full Name', value: student.fullName },
              { label: 'Email Address', value: student.email, mono: true },
              { label: 'Phone Number', value: student.phoneNumber || 'Not provided', mono: true },
              { label: 'Role', value: student.role, mono: true },
              { label: 'Registration Date', value: formatDateTime(student.createdAt), mono: true },
              { label: 'Last Updated', value: formatDateTime(student.updatedAt), mono: true },
              {
                label: 'Last Login',
                value: student.lastLoginAt ? formatDateTime(student.lastLoginAt) : 'Never signed in',
                mono: true,
              },
            ]}
          />

          <DetailCard
            title="Academic Profile & Admissions"
            subtitle="Structured for upcoming milestones"
            icon={ShieldIcon}
            badge={<span className="status-pill status-pill-pending">UNAVAILABLE</span>}
            rows={[
              { label: 'Academic Credentials', value: 'Not submitted yet', mono: true },
              { label: 'UK University Shortlist', value: 'Not implemented yet', mono: true },
              { label: 'Document Vault', value: 'Pending upcoming milestone', mono: true },
              {
                label: 'Module Status',
                value: detail.academicProfileNote,
              },
            ]}
          />
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
