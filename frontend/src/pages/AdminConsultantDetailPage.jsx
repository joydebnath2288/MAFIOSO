import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { DetailCard } from '../components/DetailCard';
import { ArrowLeftIcon, UsersIcon, UserIcon } from '../components/Icons';
import { StatusBadge } from '../components/StatusBadge';
import {
  fetchAdminConsultantDetail,
  updateAdminConsultantStatus,
} from '../services/adminService';
import { formatDateTime } from '../utils/validation';

export function AdminConsultantDetailPage() {
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
    const res = await fetchAdminConsultantDetail(id);
    if (res.ok && res.data && res.data.success && res.data.data) {
      setDetail(res.data.data);
    } else {
      setError(res.error || 'Unable to load consultant account details');
    }
    setLoading(false);
  }, [id]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  const consultant = detail && detail.consultant;

  async function handleConfirmToggle() {
    if (!consultant || toggling) {
      return;
    }
    setToggling(true);
    setFeedback('');
    setError('');
    try {
      const nextStatus = consultant.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
      const res = await updateAdminConsultantStatus(consultant.id, nextStatus);
      if (res.ok && res.data && res.data.success) {
        setFeedback(`Consultant account status updated to ${nextStatus}`);
        setConfirmOpen(false);
        await loadDetail();
      } else {
        setError(res.error || 'Unable to update consultant status');
        setConfirmOpen(false);
      }
    } finally {
      setToggling(false);
    }
  }

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/staff/consultants" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Consultants</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">Consultant Record • #{id}</div>
          <h1 className="page-title">{consultant ? consultant.fullName : 'Consultant Details'}</h1>
          <p className="page-subtitle">
            {consultant ? consultant.email : 'Staff account overview and lifecycle status'}
          </p>
        </div>

        {consultant && (
          <div className="workspace-status-group">
            <StatusBadge status={consultant.status} />
            <button
              type="button"
              className="btn btn-primary btn-compact"
              onClick={() => setConfirmOpen(true)}
            >
              {consultant.status === 'ACTIVE' ? 'Deactivate Account' : 'Activate Account'}
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
          <span className="mono-label">Loading consultant details</span>
        </div>
      ) : consultant ? (
        <div className="workspace-grid">
          <DetailCard
            title="Consultant Account"
            subtitle={`GET /api/v1/admin/consultants/${consultant.id}`}
            icon={UserIcon}
            badge={<StatusBadge status={consultant.status} />}
            rows={[
              { label: 'Consultant ID', value: `#${consultant.id}`, mono: true },
              { label: 'Full Name', value: consultant.fullName },
              { label: 'Email Address', value: consultant.email, mono: true },
              { label: 'Phone Number', value: consultant.phoneNumber || 'Not provided', mono: true },
              { label: 'Role', value: consultant.role, mono: true },
              { label: 'Registration Date', value: formatDateTime(consultant.createdAt), mono: true },
              { label: 'Last Updated', value: formatDateTime(consultant.updatedAt), mono: true },
              {
                label: 'Last Login',
                value: consultant.lastLoginAt ? formatDateTime(consultant.lastLoginAt) : 'Never signed in',
                mono: true,
              },
            ]}
          />

          <DetailCard
            title="Student Assignments"
            subtitle="Assignment relationship tracking"
            icon={UsersIcon}
            badge={<span className="status-pill status-pill-pending">UNAVAILABLE</span>}
            rows={[
              {
                label: 'Assigned Student Count',
                value:
                  consultant.assignmentTrackingAvailable && consultant.assignedStudentCount != null
                    ? consultant.assignedStudentCount
                    : 'Unavailable (not tracked yet)',
                mono: true,
              },
              {
                label: 'Status',
                value: detail.assignmentNote,
              },
            ]}
          />
        </div>
      ) : null}

      <ConfirmDialog
        open={confirmOpen}
        title={
          consultant && consultant.status === 'ACTIVE'
            ? 'Deactivate consultant account'
            : 'Activate consultant account'
        }
        message={
          consultant
            ? consultant.status === 'ACTIVE'
              ? `Deactivating ${consultant.fullName} (${consultant.email}) will invalidate active staff sessions and block sign-ins.`
              : `Activating ${consultant.fullName} (${consultant.email}) will restore login access.`
            : ''
        }
        confirmLabel={consultant && consultant.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        confirmVariant={consultant && consultant.status === 'ACTIVE' ? 'danger' : 'primary'}
        busy={toggling}
        onConfirm={handleConfirmToggle}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  );
}
