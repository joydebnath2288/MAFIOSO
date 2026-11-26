import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DashboardHeader } from '../components/DashboardHeader';
import { DashboardSidebar } from '../components/DashboardSidebar';
import { EmptyState } from '../components/EmptyState';
import {
  ArrowRightIcon,
  CalendarIcon,
  ClockIcon,
  FileTextIcon,
  FolderIcon,
  GraduationCapIcon,
  HelpCircleIcon,
  PlusIcon,
  UserIcon,
  UsersIcon,
} from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import { CreateApplicationModal } from '../components/CreateApplicationModal';
import { useAuth } from '../hooks/useAuth';
import { fetchStudentDashboard } from '../services/studentService';
import { formatDateOnly, formatDateTime } from '../utils/validation';

export function StudentDashboardPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('overview');
  const [createOpen, setCreateOpen] = useState(false);

  async function loadDashboard() {
    setLoading(true);
    setError('');
    const res = await fetchStudentDashboard();
    if (res.ok && res.data && res.data.success) {
      setDashboard(res.data.data);
    } else {
      setError(res.error || 'Unable to load student workspace');
    }
    setLoading(false);
  }

  useEffect(() => {
    document.title = 'Student Dashboard — MAFIOSO';
    loadDashboard();
  }, []);

  const account = (dashboard && dashboard.account) || user;
  const profile = (dashboard && dashboard.profile) || null;
  const recentApplications = (dashboard && dashboard.recentApplications) || [];
  const totalApplications = (dashboard && dashboard.totalApplications) || 0;
  const totalDocuments = (dashboard && dashboard.totalDocuments) || 0;
  const assignedConsultant = (dashboard && dashboard.assignedConsultant) || null;
  const completionPercent = profile ? profile.completionPercent : 0;
  const missingFields = (profile && profile.missingFields) || [];

  const sidebarSections = [
    {
      title: 'Admissions portal',
      items: [
        {
          key: 'overview',
          label: 'Overview',
          icon: GraduationCapIcon,
          active: activeTab === 'overview',
          onClick: () => setActiveTab('overview'),
        },
        {
          to: '/student/profile',
          label: 'My Profile',
          icon: UserIcon,
        },
        {
          to: '/student/applications',
          label: 'My Applications',
          icon: FileTextIcon,
        },
        {
          to: '/student/documents',
          label: 'My Documents',
          icon: FolderIcon,
        },
      ],
    },
    {
      title: 'Support',
      items: [
        {
          key: 'help',
          label: 'Admissions Guide',
          icon: HelpCircleIcon,
          active: activeTab === 'help',
          onClick: () => setActiveTab('help'),
        },
      ],
    },
  ];

  async function handleApplicationCreated() {
    setCreateOpen(false);
    await loadDashboard();
  }

  return (
    <div className="dashboard-container">
      <DashboardSidebar sections={sidebarSections} />

      <main className="dashboard-main-content">
        <DashboardHeader
          kicker={`Student Portal • ${account?.fullName || 'Applicant'}`}
          title={activeTab === 'help' ? 'Admissions Preparation Guide' : 'Student Admissions Workspace'}
          subtitle="Manage your UK study profile, applications, consultant pairing, and supporting documents"
          badge={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="mono-badge">{account ? account.role : 'STUDENT'}</span>
              {account?.status && <StatusBadge status={account.status} />}
            </div>
          }
          actions={
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <Link to="/student/profile" className="btn btn-secondary btn-compact">
                <span>Edit Profile</span>
                <ArrowRightIcon />
              </Link>
              <button
                type="button"
                className="btn btn-primary btn-compact"
                onClick={() => setCreateOpen(true)}
              >
                <PlusIcon />
                <span>New Application</span>
              </button>
            </div>
          }
        />

        {error && <NoticeBanner type="error">{error}</NoticeBanner>}

        {loading ? (
          <LoadingSpinner label="Loading student workspace" />
        ) : activeTab === 'overview' ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* Profile Completeness Card */}
            <section className="section-card" aria-label="Profile completeness">
              <div className="section-card-header">
                <div>
                  <h2 className="health-card-title">
                    Profile Completeness — {completionPercent}%
                  </h2>
                  <div className="health-card-endpoint">
                    {missingFields.length === 0
                      ? 'All key dossier sections are filled'
                      : `${missingFields.length} remaining field${missingFields.length === 1 ? '' : 's'} recommended before submission`}
                  </div>
                </div>
                <Link to="/student/profile" className="btn btn-secondary btn-compact">
                  {completionPercent === 100 ? 'Review Profile' : 'Complete Profile'}
                </Link>
              </div>

              <div className="profile-completion-bar-track" aria-hidden="true">
                <div
                  className="profile-completion-bar-fill"
                  style={{ width: `${completionPercent}%` }}
                />
              </div>

              {missingFields.length > 0 && (
                <div className="missing-fields-list">
                  {missingFields.map((item) => (
                    <span key={item} className="badge-muted">
                      {item}
                    </span>
                  ))}
                </div>
              )}
            </section>

            {/* 2-Column Grid: Applications & Assigned Consultant */}
            <div className="workspace-grid">
              <article className="health-card">
                <div className="health-card-top">
                  <div className="health-card-title-group">
                    <FileTextIcon className="card-icon" />
                    <div>
                      <h2 className="health-card-title">My Applications</h2>
                      <div className="health-card-endpoint">UK Study Requests</div>
                    </div>
                  </div>
                  <span className="badge-muted" style={{ fontVariantNumeric: 'tabular-nums' }}>
                    {totalApplications} {totalApplications === 1 ? 'Application' : 'Applications'}
                  </span>
                </div>

                {recentApplications.length === 0 ? (
                  <EmptyState
                    icon={FileTextIcon}
                    title="No Applications Submitted"
                    description="Create a BSc or MSc study application to receive a unique reference number and begin consultant review."
                    action={
                      <button
                        type="button"
                        className="btn btn-primary btn-compact"
                        onClick={() => setCreateOpen(true)}
                      >
                        Create Application
                      </button>
                    }
                  />
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div className="dossier-list" style={{ margin: 0 }}>
                      {recentApplications.map((app) => (
                        <Link
                          key={app.id}
                          to={`/student/applications/${app.id}`}
                          className="dossier-row"
                        >
                          <span className="dossier-index">{app.degreeLevel}</span>
                          <div>
                            <div className="dossier-row-title">
                              {app.preferredSubject} — {app.preferredUniversity}
                            </div>
                            <div className="dossier-row-detail">
                              <span className="mono-val">{app.referenceNumber}</span> • {app.intendedIntake}
                            </div>
                          </div>
                          <StatusBadge status={app.status} />
                        </Link>
                      ))}
                    </div>
                    <div>
                      <Link to="/student/applications" className="text-link" style={{ fontSize: '13px' }}>
                        View all applications →
                      </Link>
                    </div>
                  </div>
                )}
              </article>

              <article className="health-card">
                <div className="health-card-top">
                  <div className="health-card-title-group">
                    <UsersIcon className="card-icon" />
                    <div>
                      <h2 className="health-card-title">Assigned Consultant</h2>
                      <div className="health-card-endpoint">Dedicated UK Advisor</div>
                    </div>
                  </div>
                  <span className={assignedConsultant ? 'status-pill status-pill-up' : 'badge-muted'}>
                    {assignedConsultant ? 'Assigned' : 'Unassigned'}
                  </span>
                </div>

                {!assignedConsultant ? (
                  <EmptyState
                    icon={UsersIcon}
                    title="No Consultant Assigned Yet"
                    description="An administrator will pair your submitted application with an advisor based on your degree level and target intake."
                  />
                ) : (
                  <dl className="metric-list">
                    <div className="metric-row">
                      <dt>Advisor Name</dt>
                      <dd className="table-primary-text">{assignedConsultant.fullName}</dd>
                    </div>
                    <div className="metric-row">
                      <dt>Email Address</dt>
                      <dd className="mono-val">{assignedConsultant.email}</dd>
                    </div>
                    {assignedConsultant.phoneNumber && (
                      <div className="metric-row">
                        <dt>Phone Number</dt>
                        <dd className="mono-val">{assignedConsultant.phoneNumber}</dd>
                      </div>
                    )}
                    <div className="metric-row">
                      <dt>Advisor Status</dt>
                      <dd>
                        <StatusBadge status={assignedConsultant.status} />
                      </dd>
                    </div>
                  </dl>
                )}
              </article>
            </div>

            {/* 2-Column Grid: Preparation Checklist & Account Summary */}
            <div className="workspace-grid">
              <article className="health-card">
                <div className="health-card-top">
                  <div className="health-card-title-group">
                    <ClockIcon className="card-icon" />
                    <div>
                      <h2 className="health-card-title">Admissions Checklist</h2>
                      <div className="health-card-endpoint">Live Workspace State</div>
                    </div>
                  </div>
                </div>

                <div className="dossier-list" style={{ margin: 0 }}>
                  <div className="dossier-row">
                    <span className="status-dot status-dot-up" aria-hidden="true" />
                    <div>
                      <div className="dossier-row-title">Student Account Active</div>
                      <div className="dossier-row-detail">Registered on {formatDateOnly(account?.createdAt)}</div>
                    </div>
                    <span className="badge-muted">Done</span>
                  </div>

                  <div className="dossier-row">
                    <span
                      className={`status-dot ${completionPercent >= 80 ? 'status-dot-up' : 'status-dot-pending'}`}
                      aria-hidden="true"
                    />
                    <div>
                      <div className="dossier-row-title">Student Profile & Passport Details</div>
                      <div className="dossier-row-detail">
                        {completionPercent}% completed
                      </div>
                    </div>
                    <Link to="/student/profile" className="text-link" style={{ fontSize: '12px' }}>
                      Open
                    </Link>
                  </div>

                  <div className="dossier-row">
                    <span
                      className={`status-dot ${totalDocuments > 0 ? 'status-dot-up' : 'status-dot-pending'}`}
                      aria-hidden="true"
                    />
                    <div>
                      <div className="dossier-row-title">Supporting Documents</div>
                      <div className="dossier-row-detail">
                        {totalDocuments} document{totalDocuments === 1 ? '' : 's'} uploaded
                      </div>
                    </div>
                    <Link to="/student/documents" className="text-link" style={{ fontSize: '12px' }}>
                      Vault
                    </Link>
                  </div>
                </div>
              </article>

              <article className="health-card">
                <div className="health-card-top">
                  <div className="health-card-title-group">
                    <CalendarIcon className="card-icon" />
                    <div>
                      <h2 className="health-card-title">Account Summary</h2>
                      <div className="health-card-endpoint">Session Scope</div>
                    </div>
                  </div>
                </div>

                <dl className="metric-list">
                  <div className="metric-row">
                    <dt>Student ID</dt>
                    <dd className="mono-val">#{account?.id || '—'}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Email</dt>
                    <dd className="mono-val">{account?.email || '—'}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Documents Uploaded</dt>
                    <dd className="mono-val">{totalDocuments}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Last Session</dt>
                    <dd className="mono-val">{formatDateTime(account?.lastLoginAt)}</dd>
                  </div>
                </dl>
              </article>
            </div>
          </div>
        ) : (
          <div className="section-card">
            <h2 className="health-card-title">Admissions Preparation Guide</h2>
            <p className="page-subtitle">
              Guidance on preparing your academic credentials for UK university evaluation
            </p>
            <div className="dossier-list" style={{ margin: 0 }}>
              <div className="dossier-row">
                <span className="dossier-index">01</span>
                <div>
                  <div className="dossier-row-title">Complete your student profile</div>
                  <div className="dossier-row-detail">
                    Provide accurate passport, SSC/HSC or Bachelor results, English proficiency scores, and funding plan.
                  </div>
                </div>
              </div>
              <div className="dossier-row">
                <span className="dossier-index">02</span>
                <div>
                  <div className="dossier-row-title">Submit a study application</div>
                  <div className="dossier-row-detail">
                    Choose BSc or MSc, specify your intended intake, subject, and preferred UK university.
                  </div>
                </div>
              </div>
              <div className="dossier-row">
                <span className="dossier-index">03</span>
                <div>
                  <div className="dossier-row-title">Upload verified documents</div>
                  <div className="dossier-row-detail">
                    Upload clear PDF, PNG, JPG, or DOCX copies of your passport, transcripts, and English certificates.
                  </div>
                </div>
              </div>
            </div>
            <div>
              <button
                type="button"
                className="btn btn-primary btn-compact"
                onClick={() => setActiveTab('overview')}
              >
                Return to Overview
              </button>
            </div>
          </div>
        )}
      </main>

      <CreateApplicationModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={handleApplicationCreated}
        defaultPreferences={profile}
      />
    </div>
  );
}
