import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DashboardHeader } from '../components/DashboardHeader';
import { DashboardSidebar } from '../components/DashboardSidebar';
import { DataTable } from '../components/DataTable';
import { EmptyState } from '../components/EmptyState';
import {
  ActivityIcon,
  ArrowRightIcon,
  FileTextIcon,
  GraduationCapIcon,
  ServerIcon,
  ShieldIcon,
  UserIcon,
  UsersIcon,
} from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatCard } from '../components/StatCard';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import { fetchAdminDashboard } from '../services/authService';
import { fetchConsultantDashboard } from '../services/consultantService';
import { formatDateOnly, formatDateTime } from '../utils/validation';

export function StaffDashboardPage() {
  const { user } = useAuth();
  const isAdmin = user && user.role === 'ADMIN';

  const [consultantData, setConsultantData] = useState(null);
  const [adminData, setAdminData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [staffTab, setStaffTab] = useState('overview');

  useEffect(() => {
    document.title = isAdmin ? 'Admin Operations — MAFIOSO' : 'Consultant Workspace — MAFIOSO';
    let active = true;

    async function load() {
      setLoading(true);
      setError('');

      const consultantPromise = fetchConsultantDashboard();
      const adminPromise = isAdmin ? fetchAdminDashboard() : Promise.resolve(null);
      const [consultantRes, adminRes] = await Promise.all([consultantPromise, adminPromise]);

      if (!active) return;

      if (consultantRes.ok && consultantRes.data && consultantRes.data.success) {
        setConsultantData(consultantRes.data.data);
      } else {
        setError(consultantRes.error || 'Unable to load staff operations workspace');
      }

      if (adminRes) {
        if (adminRes.ok && adminRes.data && adminRes.data.success) {
          setAdminData(adminRes.data.data);
        } else {
          setError(adminRes.error || 'Unable to load administration overview');
        }
      }

      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, [isAdmin]);

  const account = (adminData && adminData.account) || (consultantData && consultantData.account) || user;
  const metrics = (adminData && adminData.metrics) || null;
  const appMetrics = (adminData && adminData.applicationMetrics) || null;
  const recentStudents = (adminData && adminData.recentStudents) || [];
  const recentApplications = (adminData && adminData.recentApplications) || [];
  const consultants = (adminData && adminData.consultants) || [];

  const consultantMetrics = (consultantData && consultantData.metrics) || null;
  const consultantApps = (consultantData && consultantData.recentApplications) || [];

  const adminSidebarSections = [
    {
      title: 'Administration',
      items: [
        {
          key: 'overview',
          label: 'Overview',
          icon: ActivityIcon,
          active: staffTab === 'overview',
          onClick: () => setStaffTab('overview'),
        },
        {
          to: '/staff/applications',
          label: 'Applications',
          icon: FileTextIcon,
        },
        {
          to: '/staff/students',
          label: 'Students',
          icon: GraduationCapIcon,
        },
        {
          to: '/staff/consultants',
          label: 'Consultants',
          icon: UsersIcon,
        },
        {
          to: '/health',
          label: 'System Health',
          icon: ServerIcon,
        },
        {
          key: 'settings',
          label: 'Platform Settings',
          icon: ShieldIcon,
          active: staffTab === 'settings',
          onClick: () => setStaffTab('settings'),
        },
      ],
    },
  ];

  const consultantSidebarSections = [
    {
      title: 'Advisory caseload',
      items: [
        {
          key: 'overview',
          label: 'Overview',
          icon: ActivityIcon,
          active: staffTab === 'overview',
          onClick: () => setStaffTab('overview'),
        },
        {
          to: '/staff/assigned-students',
          label: 'Assigned Students',
          icon: UsersIcon,
        },
        {
          to: '/staff/applications',
          label: 'Assigned Applications',
          icon: FileTextIcon,
        },
        {
          key: 'profile',
          label: 'My Staff Profile',
          icon: UserIcon,
          active: staffTab === 'profile',
          onClick: () => setStaffTab('profile'),
        },
      ],
    },
  ];

  const recentStudentColumns = [
    {
      key: 'id',
      header: 'Student ID',
      render: (row) => <span className="mono-val">#{row.id}</span>,
    },
    {
      key: 'fullName',
      header: 'Full Name',
      render: (row) => <span className="table-primary-text">{row.fullName}</span>,
    },
    {
      key: 'email',
      header: 'Email',
      render: (row) => <span className="mono-val">{row.email}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'createdAt',
      header: 'Registered',
      render: (row) => <span className="mono-val">{formatDateOnly(row.createdAt)}</span>,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/staff/students/${row.id}`} className="text-link">
          View details
        </Link>
      ),
    },
  ];

  const consultantOverviewColumns = [
    {
      key: 'fullName',
      header: 'Consultant Name',
      render: (row) => <span className="table-primary-text">{row.fullName}</span>,
    },
    {
      key: 'email',
      header: 'Email',
      render: (row) => <span className="mono-val">{row.email}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'assignedStudentCount',
      header: 'Assigned Students',
      render: (row) => (
        <span className="mono-val">
          {row.assignmentTrackingAvailable && row.assignedStudentCount != null
            ? row.assignedStudentCount
            : '—'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/staff/consultants/${row.id}`} className="text-link">
          View details
        </Link>
      ),
    },
  ];

  const applicationColumns = [
    {
      key: 'referenceNumber',
      header: 'Reference',
      render: (row) => <span className="mono-val">{row.referenceNumber}</span>,
    },
    {
      key: 'studentName',
      header: 'Applicant',
      render: (row) => (
        <div>
          <div className="table-primary-text">{row.studentName}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            {row.degreeLevel} • {row.preferredSubject}
          </div>
        </div>
      ),
    },
    {
      key: 'preferredUniversity',
      header: 'University',
      render: (row) => <span>{row.preferredUniversity}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/staff/applications/${row.id}`} className="text-link">
          Manage
        </Link>
      ),
    },
  ];

  return (
    <div className="dashboard-container">
      <DashboardSidebar sections={isAdmin ? adminSidebarSections : consultantSidebarSections} />

      <main className="dashboard-main-content">
        <DashboardHeader
          kicker={`Staff Portal • ${isAdmin ? 'ADMINISTRATOR' : 'CONSULTANT'}`}
          title={
            isAdmin
              ? staffTab === 'settings'
                ? 'Platform Settings & Governance'
                : 'Administration & Consultancy Overview'
              : staffTab === 'profile'
              ? 'Consultant Profile Record'
              : 'Consultant Advisory Workspace'
          }
          subtitle={
            isAdmin
              ? 'Applications, consultant assignments, student records, and system metrics'
              : 'Assigned student caseload, application progress, and document verification'
          }
          badge={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="mono-badge">{account ? account.role : 'STAFF'}</span>
              {account?.status && <StatusBadge status={account.status} />}
            </div>
          }
          actions={
            <Link to="/staff/applications" className="btn btn-secondary btn-compact">
              <span>{isAdmin ? 'All Applications' : 'Assigned Applications'}</span>
              <ArrowRightIcon />
            </Link>
          }
        />

        {error && <NoticeBanner type="error">{error}</NoticeBanner>}

        {loading ? (
          <LoadingSpinner label="Loading workspace records" />
        ) : isAdmin ? (
          <div>
            {staffTab === 'overview' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                {metrics && (
                  <section aria-label="Platform account statistics">
                    <div className="stat-card-row">
                      <StatCard
                        label="Total Students"
                        value={metrics.totalStudents}
                        meta={`${metrics.activeStudents} active accounts`}
                      />
                      <StatCard
                        label="Consultants"
                        value={metrics.totalConsultants}
                        meta={`${metrics.activeConsultants} active advisors`}
                      />
                      <StatCard
                        label="Total Applications"
                        value={appMetrics ? appMetrics.totalApplications : 0}
                        meta={`${appMetrics ? appMetrics.unassignedApplications : 0} unassigned`}
                      />
                      <StatCard
                        label="Pending Document Reviews"
                        value={appMetrics ? appMetrics.pendingDocumentReviews : 0}
                        meta={`${appMetrics ? appMetrics.completedApplications : 0} completed apps`}
                      />
                    </div>
                  </section>
                )}

                <section className="section-card" aria-labelledby="recent-apps-heading">
                  <div className="section-card-header">
                    <div>
                      <h2 id="recent-apps-heading" className="health-card-title">
                        Recent Study Applications
                      </h2>
                      <div className="health-card-endpoint">Latest student submissions and assignments</div>
                    </div>
                    <Link to="/staff/applications" className="btn btn-secondary btn-compact">
                      <span>View all applications</span>
                      <ArrowRightIcon />
                    </Link>
                  </div>

                  <DataTable
                    columns={applicationColumns}
                    rows={recentApplications}
                    loading={loading}
                    emptyTitle="No study applications yet"
                    emptyMessage="Submitted student applications will appear here for consultant assignment."
                  />
                </section>

                <section className="section-card" aria-labelledby="recent-students-heading">
                  <div className="section-card-header">
                    <div>
                      <h2 id="recent-students-heading" className="health-card-title">
                        Recent Student Registrations
                      </h2>
                      <div className="health-card-endpoint">Registered student accounts</div>
                    </div>
                    <Link to="/staff/students" className="btn btn-secondary btn-compact">
                      <span>View all students</span>
                      <ArrowRightIcon />
                    </Link>
                  </div>

                  <DataTable
                    columns={recentStudentColumns}
                    rows={recentStudents}
                    loading={loading}
                    emptyTitle="No student registrations yet"
                    emptyMessage="Newly registered student accounts will appear here."
                  />
                </section>

                <section className="section-card" aria-labelledby="consultant-overview-heading">
                  <div className="section-card-header">
                    <div>
                      <h2 id="consultant-overview-heading" className="health-card-title">
                        Consultant Caseload Overview
                      </h2>
                      <div className="health-card-endpoint">Active student counts per consultant</div>
                    </div>
                    <Link to="/staff/consultants" className="btn btn-secondary btn-compact">
                      <span>Manage consultants</span>
                      <ArrowRightIcon />
                    </Link>
                  </div>

                  <DataTable
                    columns={consultantOverviewColumns}
                    rows={consultants}
                    loading={loading}
                    emptyTitle="No consultant accounts yet"
                    emptyMessage="Provision consultant accounts from Consultant Management."
                  />
                </section>
              </div>
            )}

            {staffTab === 'settings' && (
              <div className="section-card">
                <div className="section-card-header">
                  <div>
                    <h2 className="health-card-title">Platform Configuration & Governance</h2>
                    <div className="health-card-endpoint">MAFIOSO Infrastructure Settings</div>
                  </div>
                </div>
                <dl className="metric-list">
                  <div className="metric-row">
                    <dt>Application Target</dt>
                    <dd>Bangladesh → United Kingdom (BSc & MSc)</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Authentication Policy</dt>
                    <dd className="mono-val">HttpOnly Session Cookie + CSRF Tokens</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Database Authority</dt>
                    <dd className="mono-val">MySQL 8.0 (Containerized)</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Document Storage</dt>
                    <dd className="mono-val">Private Volume + Magic-Byte Validation</dd>
                  </div>
                </dl>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <Link to="/health" className="btn btn-secondary">
                    <ServerIcon />
                    <span>Inspect System Diagnostics</span>
                  </Link>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setStaffTab('overview')}
                  >
                    Return to Overview
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div>
            {staffTab === 'overview' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                {consultantMetrics && (
                  <section aria-label="Consultant caseload metrics">
                    <div className="stat-card-row">
                      <StatCard
                        label="Assigned Students"
                        value={consultantMetrics.assignedStudents}
                        meta="Active student caseload"
                      />
                      <StatCard
                        label="Assigned Applications"
                        value={consultantMetrics.totalApplications}
                        meta={`${consultantMetrics.inProgress || 0} in progress`}
                      />
                      <StatCard
                        label="Pending Document Reviews"
                        value={consultantMetrics.pendingDocumentReviews}
                        meta={`${consultantMetrics.documentsAttention || 0} apps awaiting docs`}
                      />
                      <StatCard
                        label="Completed Applications"
                        value={consultantMetrics.completed}
                        meta="Finalized dossiers"
                      />
                    </div>
                  </section>
                )}

                <section className="section-card">
                  <div className="section-card-header">
                    <div>
                      <h2 className="health-card-title">Assigned Applications</h2>
                      <div className="health-card-endpoint">
                        Applications currently assigned to your caseload
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <Link to="/staff/assigned-students" className="btn btn-secondary btn-compact">
                        Assigned Students
                      </Link>
                      <Link to="/staff/applications" className="btn btn-primary btn-compact">
                        All Assigned Apps
                      </Link>
                    </div>
                  </div>

                  {consultantApps.length === 0 ? (
                    <EmptyState
                      icon={FileTextIcon}
                      title="No Applications Assigned Yet"
                      description="Once an administrator assigns student applications to you, they will appear here for review."
                    />
                  ) : (
                    <DataTable
                      columns={applicationColumns}
                      rows={consultantApps}
                      loading={loading}
                    />
                  )}
                </section>
              </div>
            )}

            {staffTab === 'profile' && (
              <div className="section-card">
                <h2 className="health-card-title">Consultant Account Record</h2>
                <dl className="metric-list" style={{ maxWidth: '540px' }}>
                  <div className="metric-row">
                    <dt>Staff ID</dt>
                    <dd className="mono-val">#{account?.id || '—'}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Name</dt>
                    <dd>{account?.fullName || '—'}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Email</dt>
                    <dd className="mono-val">{account?.email || '—'}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Provisioned Date</dt>
                    <dd className="mono-val">{formatDateTime(account?.createdAt)}</dd>
                  </div>
                  <div className="metric-row">
                    <dt>Last Session</dt>
                    <dd className="mono-val">{formatDateTime(account?.lastLoginAt)}</dd>
                  </div>
                </dl>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
