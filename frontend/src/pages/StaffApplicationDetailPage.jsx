import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApplicationTimeline } from '../components/ApplicationTimeline';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { DataTable } from '../components/DataTable';
import { DetailCard } from '../components/DetailCard';
import { DocumentVaultPanel } from '../components/DocumentVaultPanel';
import { ArrowLeftIcon, FileTextIcon, UserIcon } from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import {
  assignAdminConsultant,
  fetchAdminApplicationDetail,
  removeAdminConsultantAssignment,
  reviewAdminDocument,
  updateAdminApplicationStatus,
} from '../services/adminService';
import {
  fetchConsultantApplicationDetail,
  reviewConsultantDocument,
  updateConsultantApplicationStatus,
} from '../services/consultantService';
import { formatDateTime } from '../utils/validation';

const ADMIN_STATUSES = [
  'SUBMITTED',
  'UNDER_REVIEW',
  'ASSIGNED',
  'DOCUMENTS_PENDING',
  'DOCUMENTS_REVIEW',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
];

const CONSULTANT_STATUSES = [
  'UNDER_REVIEW',
  'DOCUMENTS_PENDING',
  'DOCUMENTS_REVIEW',
  'IN_PROGRESS',
  'COMPLETED',
];

export function StaffApplicationDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const isAdmin = user && user.role === 'ADMIN';

  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [nextStatus, setNextStatus] = useState('UNDER_REVIEW');
  const [statusNote, setStatusNote] = useState('');
  const [savingStatus, setSavingStatus] = useState(false);

  const [selectedConsultantId, setSelectedConsultantId] = useState('');
  const [assignNote, setAssignNote] = useState('');
  const [assigning, setAssigning] = useState(false);
  const [removeDialogOpen, setRemoveDialogOpen] = useState(false);

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = isAdmin
      ? await fetchAdminApplicationDetail(id)
      : await fetchConsultantApplicationDetail(id);
    if (res.ok && res.data && res.data.success && res.data.data) {
      const data = res.data.data;
      setDetail(data);
      if (data.application) {
        const currentStatus = data.application.status;
        if (isAdmin) {
          setNextStatus(currentStatus);
        } else {
          setNextStatus(
            CONSULTANT_STATUSES.includes(currentStatus) ? currentStatus : 'UNDER_REVIEW'
          );
        }
        setSelectedConsultantId(
          data.application.assignedConsultant ? String(data.application.assignedConsultant.id) : ''
        );
      }
    } else {
      setError(res.error || 'Unable to load application dossier');
    }
    setLoading(false);
  }, [id, isAdmin]);

  useEffect(() => {
    document.title = 'Application Dossier — MAFIOSO';
    loadDetail();
  }, [loadDetail]);

  const app = detail && detail.application;
  const studentProfile = detail && detail.studentProfile;
  const statusHistory = (detail && detail.statusHistory) || [];
  const assignmentHistory = (detail && detail.assignmentHistory) || [];
  const documents = (detail && detail.documents) || [];
  const availableConsultants = (detail && detail.availableConsultants) || [];

  async function handleStatusSubmit(event) {
    event.preventDefault();
    if (!app || savingStatus) return;

    setSavingStatus(true);
    setError('');
    setFeedback('');
    try {
      const res = isAdmin
        ? await updateAdminApplicationStatus(app.id, { status: nextStatus, note: statusNote })
        : await updateConsultantApplicationStatus(app.id, { status: nextStatus, note: statusNote });
      if (res.ok && res.data && res.data.success) {
        setFeedback(`Application status updated to ${nextStatus.replace(/_/g, ' ')}.`);
        setStatusNote('');
        await loadDetail();
      } else {
        setError(res.error || 'Unable to update application status');
      }
    } finally {
      setSavingStatus(false);
    }
  }

  async function handleAssignSubmit(event) {
    event.preventDefault();
    if (!app || assigning || !selectedConsultantId) {
      if (!selectedConsultantId) {
        setError('Please select an active consultant to assign.');
      }
      return;
    }

    setAssigning(true);
    setError('');
    setFeedback('');
    try {
      const res = await assignAdminConsultant(app.id, {
        consultantId: Number(selectedConsultantId),
        note: assignNote,
      });
      if (res.ok && res.data && res.data.success) {
        setFeedback('Consultant assignment saved.');
        setAssignNote('');
        await loadDetail();
      } else {
        setError(res.error || 'Unable to assign consultant');
      }
    } finally {
      setAssigning(false);
    }
  }

  async function handleConfirmRemoveAssignment() {
    if (!app || assigning) return;
    setAssigning(true);
    setError('');
    setFeedback('');
    try {
      const res = await removeAdminConsultantAssignment(app.id, assignNote);
      if (res.ok && res.data && res.data.success) {
        setFeedback('Consultant assignment removed.');
        setAssignNote('');
        setRemoveDialogOpen(false);
        await loadDetail();
      } else {
        setError(res.error || 'Unable to remove assignment');
        setRemoveDialogOpen(false);
      }
    } finally {
      setAssigning(false);
    }
  }

  async function handleDocumentReview(documentId, payload) {
    setFeedback('');
    const res = isAdmin
      ? await reviewAdminDocument(documentId, payload)
      : await reviewConsultantDocument(documentId, payload);
    if (res.ok && res.data && res.data.success) {
      setFeedback('Document review updated.');
      await loadDetail();
    }
    return res;
  }

  const assignmentColumns = [
    {
      key: 'action',
      header: 'Action',
      render: (row) => <StatusBadge status={row.action} />,
    },
    {
      key: 'consultantName',
      header: 'Consultant',
      render: (row) => (
        <div>
          <div className="table-primary-text">{row.consultantName}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{row.consultantEmail}</div>
        </div>
      ),
    },
    {
      key: 'assignedByName',
      header: 'Updated by',
      render: (row) => <span>{row.assignedByName}</span>,
    },
    {
      key: 'note',
      header: 'Note',
      render: (row) => <span>{row.note || '—'}</span>,
    },
    {
      key: 'assignedAt',
      header: 'Timestamp',
      render: (row) => <span className="mono-val">{formatDateTime(row.assignedAt)}</span>,
    },
  ];

  const statusChoices = isAdmin ? ADMIN_STATUSES : CONSULTANT_STATUSES;

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/staff/applications" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Applications</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">
            Application Dossier • {app ? app.referenceNumber : `#${id}`}
          </div>
          <h1 className="page-title">
            {app ? `${app.degreeLevel} — ${app.preferredSubject}` : 'Application Details'}
          </h1>
          <p className="page-subtitle">
            {app ? `${app.preferredUniversity} • ${app.intendedIntake}` : 'Workflow management'}
          </p>
        </div>

        {app && (
          <div className="workspace-status-group">
            <StatusBadge status={app.status} />
            <Link
              to={
                isAdmin
                  ? `/staff/students/${app.studentId}`
                  : `/staff/assigned-students/${app.studentId}`
              }
              className="btn btn-secondary btn-compact"
            >
              View Student Profile
            </Link>
          </div>
        )}
      </header>

      {feedback && <NoticeBanner type="success">{feedback}</NoticeBanner>}
      {error && <NoticeBanner type="error">{error}</NoticeBanner>}

      {loading ? (
        <LoadingSpinner label="Loading application dossier" />
      ) : app ? (
        <div className="admin-overview-stack">
          <div className="workspace-grid">
            <DetailCard
              title="Application Details"
              subtitle={app.referenceNumber}
              icon={FileTextIcon}
              badge={<StatusBadge status={app.status} />}
              rows={[
                { label: 'Reference Number', value: app.referenceNumber, mono: true },
                { label: 'Degree Level', value: app.degreeLevel, mono: true },
                { label: 'Preferred Subject', value: app.preferredSubject },
                { label: 'Preferred University', value: app.preferredUniversity },
                { label: 'Intended Intake', value: app.intendedIntake },
                { label: 'Preferred Location', value: app.preferredLocation || 'Not specified' },
                { label: 'Submitted At', value: formatDateTime(app.submittedAt), mono: true },
              ]}
            />

            <DetailCard
              title="Applicant Summary"
              subtitle={`Student #${app.studentId}`}
              icon={UserIcon}
              rows={[
                { label: 'Full Name', value: app.studentName },
                { label: 'Email', value: app.studentEmail, mono: true },
                { label: 'Phone', value: app.studentPhoneNumber || 'Not provided', mono: true },
                { label: 'Nationality', value: studentProfile?.nationality || 'Not provided' },
                { label: 'Highest Qualification', value: studentProfile?.highestQualification || 'Not provided' },
                { label: 'IELTS / PTE / TOEFL', value: studentProfile?.ieltsScore || studentProfile?.pteScore || studentProfile?.toeflScore || 'Not provided', mono: true },
                {
                  label: 'Assigned Consultant',
                  value: app.assignedConsultant ? app.assignedConsultant.fullName : 'Unassigned',
                },
              ]}
            />
          </div>

          {/* Workflow Management Controls */}
          <div className="workspace-grid">
            <section className="section-card">
              <div>
                <h2 className="health-card-title">Update Application Status</h2>
                <div className="health-card-endpoint">
                  {isAdmin ? 'Administrator workflow control' : 'Consultant progress update'}
                </div>
              </div>

              <form className="auth-form" onSubmit={handleStatusSubmit}>
                <div className="form-field">
                  <label htmlFor="staff-app-status" className="form-label">Target status</label>
                  <select
                    id="staff-app-status"
                    className="form-input"
                    value={nextStatus}
                    onChange={(e) => setNextStatus(e.target.value)}
                    disabled={savingStatus}
                  >
                    {statusChoices.map((st) => (
                      <option key={st} value={st}>
                        {st.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-field">
                  <label htmlFor="staff-status-note" className="form-label">
                    Status note <span className="form-label-optional">(optional)</span>
                  </label>
                  <textarea
                    id="staff-status-note"
                    rows={2}
                    className="form-input form-textarea"
                    placeholder="Add context for the status history log"
                    value={statusNote}
                    onChange={(e) => setStatusNote(e.target.value)}
                    disabled={savingStatus}
                  />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button type="submit" className="btn btn-primary" disabled={savingStatus}>
                    {savingStatus ? 'Updating' : 'Update status'}
                  </button>
                </div>
              </form>
            </section>

            {isAdmin && (
              <section className="section-card">
                <div>
                  <h2 className="health-card-title">Consultant Assignment</h2>
                  <div className="health-card-endpoint">
                    {app.assignedConsultant
                      ? `Currently assigned to ${app.assignedConsultant.fullName}`
                      : 'No consultant assigned'}
                  </div>
                </div>

                <form className="auth-form" onSubmit={handleAssignSubmit}>
                  <div className="form-field">
                    <label htmlFor="assign-consultant-select" className="form-label">
                      Active consultant
                    </label>
                    <select
                      id="assign-consultant-select"
                      className="form-input"
                      value={selectedConsultantId}
                      onChange={(e) => setSelectedConsultantId(e.target.value)}
                      disabled={assigning}
                    >
                      <option value="">Select consultant</option>
                      {availableConsultants.map((c) => (
                        <option key={c.id} value={String(c.id)}>
                          {c.fullName} ({c.email})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-field">
                    <label htmlFor="assign-note" className="form-label">
                      Assignment note <span className="form-label-optional">(optional)</span>
                    </label>
                    <input
                      id="assign-note"
                      type="text"
                      className="form-input"
                      placeholder="Reason or caseload instructions"
                      value={assignNote}
                      onChange={(e) => setAssignNote(e.target.value)}
                      disabled={assigning}
                    />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: '12px', flexWrap: 'wrap' }}>
                    {app.assignedConsultant ? (
                      <button
                        type="button"
                        className="btn btn-danger btn-compact"
                        onClick={() => setRemoveDialogOpen(true)}
                        disabled={assigning}
                      >
                        Remove assignment
                      </button>
                    ) : (
                      <span />
                    )}
                    <button type="submit" className="btn btn-primary" disabled={assigning}>
                      {assigning
                        ? 'Saving'
                        : app.assignedConsultant
                        ? 'Change consultant'
                        : 'Assign consultant'}
                    </button>
                  </div>
                </form>
              </section>
            )}
          </div>

          <section className="section-card">
            <div>
              <h2 className="health-card-title">Progress & Status History</h2>
              <div className="health-card-endpoint">Transactional audit log</div>
            </div>
            <ApplicationTimeline currentStatus={app.status} history={statusHistory} />
          </section>

          {assignmentHistory.length > 0 && (
            <section className="section-card">
              <div>
                <h2 className="health-card-title">Consultant Assignment History</h2>
                <div className="health-card-endpoint">Assignment audit records</div>
              </div>
              <DataTable columns={assignmentColumns} rows={assignmentHistory} />
            </section>
          )}

          <section className="section-card">
            <div>
              <h2 className="health-card-title">Application Documents</h2>
              <div className="health-card-endpoint">Download and review submitted files</div>
            </div>
            <DocumentVaultPanel
              documents={documents}
              canUpload={false}
              canReview={true}
              onReview={handleDocumentReview}
            />
          </section>
        </div>
      ) : null}

      <ConfirmDialog
        open={removeDialogOpen}
        title="Remove consultant assignment"
        message={
          app && app.assignedConsultant
            ? `Remove ${app.assignedConsultant.fullName} from application ${app.referenceNumber}?`
            : ''
        }
        confirmLabel="Remove assignment"
        confirmVariant="danger"
        busy={assigning}
        onConfirm={handleConfirmRemoveAssignment}
        onCancel={() => setRemoveDialogOpen(false)}
      />
    </div>
  );
}
