import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApplicationTimeline } from '../components/ApplicationTimeline';
import { DetailCard } from '../components/DetailCard';
import { DocumentVaultPanel } from '../components/DocumentVaultPanel';
import { ArrowLeftIcon, FileTextIcon, UsersIcon } from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import { fetchStudentApplicationDetail, uploadStudentDocument } from '../services/studentService';
import { formatDateTime } from '../utils/validation';

export function StudentApplicationDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = await fetchStudentApplicationDetail(id);
    if (res.ok && res.data && res.data.success && res.data.data) {
      setDetail(res.data.data);
    } else {
      setError(res.error || 'Unable to load application details');
    }
    setLoading(false);
  }, [id]);

  useEffect(() => {
    document.title = 'Application Details — MAFIOSO';
    loadDetail();
  }, [loadDetail]);

  const app = detail && detail.application;
  const statusHistory = (detail && detail.statusHistory) || [];
  const documents = (detail && detail.documents) || [];

  async function handleDocumentUpload({ file, category }) {
    const res = await uploadStudentDocument({
      file,
      category,
      applicationId: Number(id),
    });
    if (res.ok && res.data && res.data.success) {
      await loadDetail();
    }
    return res;
  }

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/student/applications" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Applications</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">
            Application Record • {app ? app.referenceNumber : `#${id}`}
          </div>
          <h1 className="page-title">
            {app ? `${app.degreeLevel} — ${app.preferredSubject}` : 'Application Details'}
          </h1>
          <p className="page-subtitle">
            {app ? `${app.preferredUniversity} • ${app.intendedIntake}` : 'Application dossier and timeline'}
          </p>
        </div>

        {app && <StatusBadge status={app.status} />}
      </header>

      {error && <NoticeBanner type="error">{error}</NoticeBanner>}

      {loading ? (
        <LoadingSpinner label="Loading application dossier" />
      ) : app ? (
        <div className="admin-overview-stack">
          <div className="workspace-grid">
            <DetailCard
              title="Application Summary"
              subtitle={app.referenceNumber}
              icon={FileTextIcon}
              badge={<StatusBadge status={app.status} />}
              rows={[
                { label: 'Reference Number', value: app.referenceNumber, mono: true },
                { label: 'Degree Level', value: app.degreeLevel, mono: true },
                { label: 'Preferred Subject', value: app.preferredSubject },
                { label: 'Preferred University', value: app.preferredUniversity },
                { label: 'Intended Intake', value: app.intendedIntake },
                { label: 'Preferred City', value: app.preferredLocation || 'Not specified' },
                { label: 'Submitted At', value: formatDateTime(app.submittedAt), mono: true },
              ]}
            />

            <DetailCard
              title="Assigned Consultant"
              subtitle="Advisory pairing"
              icon={UsersIcon}
              badge={
                app.assignedConsultant ? (
                  <StatusBadge status={app.assignedConsultant.status} />
                ) : (
                  <span className="badge-muted">Unassigned</span>
                )
              }
              rows={
                app.assignedConsultant
                  ? [
                      { label: 'Consultant Name', value: app.assignedConsultant.fullName },
                      { label: 'Email Address', value: app.assignedConsultant.email, mono: true },
                      {
                        label: 'Phone Number',
                        value: app.assignedConsultant.phoneNumber || 'Available upon active contact',
                        mono: true,
                      },
                    ]
                  : [
                      {
                        label: 'Assignment Status',
                        value: 'Pending administrator assignment',
                      },
                    ]
              }
            />
          </div>

          {app.personalStatement && (
            <section className="section-card">
              <h2 className="health-card-title">Applicant Notes</h2>
              <p style={{ margin: 0, fontSize: '14px', color: 'var(--text-secondary)' }}>
                {app.personalStatement}
              </p>
            </section>
          )}

          <section className="section-card">
            <div>
              <h2 className="health-card-title">Application Progress & Status History</h2>
              <div className="health-card-endpoint">Audit trail from submission to completion</div>
            </div>
            <ApplicationTimeline currentStatus={app.status} history={statusHistory} />
          </section>

          <section className="section-card">
            <div>
              <h2 className="health-card-title">Application Documents</h2>
              <div className="health-card-endpoint">
                Upload supporting files and track consultant review feedback
              </div>
            </div>
            <DocumentVaultPanel
              documents={documents}
              canUpload={true}
              canReview={false}
              fixedApplicationId={app.id}
              onUpload={handleDocumentUpload}
            />
          </section>
        </div>
      ) : null}
    </div>
  );
}
