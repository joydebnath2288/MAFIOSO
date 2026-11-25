import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DashboardHeader } from '../components/DashboardHeader';
import { DashboardSidebar } from '../components/DashboardSidebar';
import { DataTable } from '../components/DataTable';
import { EmptyState } from '../components/EmptyState';
import {
  ActivityIcon,
  ArrowRightIcon,
  CheckCircleIcon,
  ClockIcon,
  DatabaseIcon,
  GraduationCapIcon,
  LockIcon,
  MessageSquareIcon,
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
import { fetchAdminDashboard, fetchConsultantDashboard } from '../services/authService';
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

      if (!active) {
        return;
      }

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
  const permissions = (consultantData && consultantData.permissions) || {};
  const capabilities = (consultantData && consultantData.authorizedCapabilities) || [];
  const metrics = (adminData && adminData.metrics) || null;
  const recentStudents = (adminData && adminData.recentStudents) || [];
  const consultants = (adminData && adminData.consultants) || [];

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
      title: 'Advisory Caseload',
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
          key: 'profile',
          label: 'My Staff Profile',
          icon: UserIcon,
          active: staffTab === 'profile',
          onClick: () => setStaffTab('profile'),
        },
        {
          key: 'conversations',
          label: 'Conversations',
          icon: MessageSquareIcon,
          badge: 'M4',
          active: staffTab === 'conversations',
          onClick: () => setStaffTab('conversations'),
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
      key: 'createdAt',
      header: 'Provisioned',
      render: (row) => <span className="mono-val">{formatDateOnly(row.createdAt)}</span>,
    },
    {
      key: 'assignedStudentCount',
      header: 'Assigned Students',
      render: (row) => (
        <span className="mono-label">
          {row.assignmentTrackingAvailable && row.assignedStudentCount != null
            ? row.assignedStudentCount
            : 'Unassigned'}
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
                : 'Administration & Staff Overview'
              : staffTab === 'profile'
              ? 'Consultant Profile Record'
              : staffTab === 'conversations'
              ? 'Student Conversations'
              : 'Consultant Advisory Workspace'
          }
          subtitle={
            isAdmin
              ? 'Live platform metrics, student admissions management, and consultant provisioning'
              : 'Applicant caseload advisory and student guidance console'
          }
          badge={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="status-pill status-pill-up">Session Active</span>
              <span className="mono-badge">{account ? account.role : 'STAFF'}</span>
              {account?.status && <StatusBadge status={account.status} />}
            </div>
          }
          actions={
            isAdmin ? (
              <Link to="/staff/students" className="btn btn-secondary btn-compact">
                <span>Manage Students</span>
                <ArrowRightIcon />
              </Link>
            ) : (
              <Link to="/staff/assigned-students" className="btn btn-secondary btn-compact">
                <span>Assigned Caseload</span>
                <ArrowRightIcon />
              </Link>
            )
          }
        />

        {error && <NoticeBanner type="error">{error}</NoticeBanner>}

        {loading ? (
          <LoadingSpinner label="Authenticating staff session and loading records..." />
        ) : (
          <>
            {isAdmin ? (
              // ================= ADMIN VIEW =================
              <div>
                {staffTab === 'overview' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
                    {/* Stat Cards Row */}
                    {metrics && (
                      <section aria-label="Platform account statistics">
                        <div className="stat-card-row">
                          <StatCard
                            label="Total Students"
                            value={metrics.totalStudents}
                            meta={`${metrics.activeStudents} active accounts`}
                            icon={GraduationCapIcon}
                          />
                          <StatCard
                            label="Active Students"
                            value={metrics.activeStudents}
                            meta="Eligible for applications"
                            icon={CheckCircleIcon}
                          />
                          <StatCard
                            label="Total Consultants"
                            value={metrics.totalConsultants}
                            meta={`${metrics.activeConsultants} active consultants`}
                            icon={UsersIcon}
                          />
                          <StatCard
                            label="Administrators"
                            value={metrics.totalAdministrators}
                            meta="Platform governance"
                            icon={ShieldIcon}
                          />
                        </div>
                      </section>
                    )}

                    {/* Recent Students Table */}
                    <section className="section-card" aria-labelledby="recent-students-heading">
                      <div className="section-card-header">
                        <div>
                          <h2 id="recent-students-heading" className="health-card-title">
                            Recent Student Registrations
                          </h2>
                          <div className="health-card-endpoint">Authentic applicant records from MySQL</div>
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

                    {/* Consultant Overview Table */}
                    <section className="section-card" aria-labelledby="consultant-overview-heading">
                      <div className="section-card-header">
                        <div>
                          <h2 id="consultant-overview-heading" className="health-card-title">
                            Consultant Caseload Overview
                          </h2>
                          <div className="health-card-endpoint">
                            Provisioned staff accounts and assignment tracking
                          </div>
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
                        emptyMessage="Provision consultant accounts from the Consultant Management page."
                      />
                    </section>
                  </div>
                )}

                {staffTab === 'settings' && (
                  <div className="section-card" style={{ padding: '32px' }}>
                    <div className="section-card-header" style={{ marginBottom: '24px' }}>
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
                        <dt>Servlet Container</dt>
                        <dd className="mono-val">Apache Tomcat 9.0 (Java 8 Monolith)</dd>
                      </div>
                      <div className="metric-row">
                        <dt>Administrative Authority</dt>
                        <dd className="mono-val">ROLE_ADMIN (Full Governance)</dd>
                      </div>
                    </dl>

                    <div style={{ marginTop: '24px', display: 'flex', gap: '12px' }}>
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
              // ================= CONSULTANT VIEW =================
              <div>
                {staffTab === 'overview' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                      {/* Consultant Profile Details */}
                      <article className="health-card">
                        <div className="health-card-top">
                          <div className="health-card-title-group">
                            <UserIcon className="card-icon" />
                            <div>
                              <h2 className="health-card-title">Consultant Profile</h2>
                              <div className="health-card-endpoint">GET /api/v1/consultant/dashboard</div>
                            </div>
                          </div>
                          <span className="status-pill status-pill-up">VERIFIED</span>
                        </div>

                        <dl className="metric-list">
                          <div className="metric-row">
                            <dt>Full Name</dt>
                            <dd>{account?.fullName || '—'}</dd>
                          </div>
                          <div className="metric-row">
                            <dt>Email Address</dt>
                            <dd className="mono-val">{account?.email || '—'}</dd>
                          </div>
                          <div className="metric-row">
                            <dt>Role Authority</dt>
                            <dd className="mono-val">{account?.role || 'CONSULTANT'}</dd>
                          </div>
                          <div className="metric-row">
                            <dt>Caseload Access</dt>
                            <dd className="mono-val">Assigned Applicants Only</dd>
                          </div>
                          <div className="metric-row">
                            <dt>Administrative Routes</dt>
                            <dd className="mono-val">Restricted (403 Forbidden)</dd>
                          </div>
                        </dl>
                      </article>

                      {/* Authorized Capabilities */}
                      <article className="health-card">
                        <div className="health-card-top">
                          <div className="health-card-title-group">
                            <ShieldIcon className="card-icon" />
                            <div>
                              <h2 className="health-card-title">Authorized Capabilities</h2>
                              <div className="health-card-endpoint">Consultant Role Scope</div>
                            </div>
                          </div>
                        </div>

                        <div className="dossier-list">
                          {capabilities.map((cap, index) => (
                            <div key={cap} className="dossier-row">
                              <span className="dossier-index">{String(index + 1).padStart(2, '0')}</span>
                              <div className="dossier-row-title">{cap}</div>
                              <span className="badge-muted">Permitted</span>
                            </div>
                          ))}
                        </div>
                      </article>
                    </div>

                    {/* Assigned Caseload Section */}
                    <section className="section-card">
                      <div className="section-card-header">
                        <div>
                          <h2 className="health-card-title">Assigned Student Caseload</h2>
                          <div className="health-card-endpoint">
                            Applicant files assigned to your consultant profile
                          </div>
                        </div>
                        <Link to="/staff/assigned-students" className="btn btn-secondary btn-compact">
                          <span>Caseload Details</span>
                          <ArrowRightIcon />
                        </Link>
                      </div>

                      <EmptyState
                        icon={UsersIcon}
                        title="No Students Assigned Yet"
                        description="Student-to-consultant assignment relationships will be established in upcoming milestones. As applicants register and activate dossiers, your caseload queue will populate here."
                      />
                    </section>
                  </div>
                )}

                {staffTab === 'profile' && (
                  <div className="section-card" style={{ padding: '32px' }}>
                    <h2 className="health-card-title" style={{ marginBottom: '16px' }}>
                      Consultant Account Record
                    </h2>
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

                {staffTab === 'conversations' && (
                  <div className="section-card" style={{ padding: '40px 32px', textAlign: 'center' }}>
                    <div
                      style={{
                        width: '48px',
                        height: '48px',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: 'var(--bg-raised)',
                        color: 'var(--text-muted)',
                        display: 'inline-flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        marginBottom: '16px',
                      }}
                    >
                      <LockIcon />
                    </div>
                    <h2 style={{ fontSize: '20px', fontWeight: 600, margin: '0 0 8px' }}>
                      Real-time Messaging Scheduled for Milestone 4
                    </h2>
                    <p style={{ fontSize: '14px', color: 'var(--text-secondary)', maxWidth: '480px', margin: '0 auto 20px' }}>
                      Applicant-consultant live messaging and dossier discussion threads will be
                      implemented with JSR-356 WebSockets in an upcoming milestone.
                    </p>
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => setStaffTab('overview')}
                    >
                      Return to Overview
                    </button>
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
