import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DataTable } from '../components/DataTable';
import { ArrowRightIcon, ServerIcon, UserIcon } from '../components/Icons';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import { fetchAdminDashboard, fetchConsultantDashboard } from '../services/authService';
import { formatDateOnly } from '../utils/validation';

export function StaffDashboardPage() {
  const { user } = useAuth();
  const isAdmin = user && user.role === 'ADMIN';

  const [consultantData, setConsultantData] = useState(null);
  const [adminData, setAdminData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
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

  const statCards = metrics
    ? [
        { label: 'Total Students', value: metrics.totalStudents },
        { label: 'Active Students', value: metrics.activeStudents },
        { label: 'Total Consultants', value: metrics.totalConsultants },
        { label: 'Active Consultants', value: metrics.activeConsultants },
        { label: 'Total Administrators', value: metrics.totalAdministrators },
      ]
    : [];

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
      header: 'Account Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'createdAt',
      header: 'Registration Date',
      render: (row) => <span className="mono-val">{formatDateOnly(row.createdAt)}</span>,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/staff/students/${row.id}`} className="text-link">
          View
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
      header: 'Account Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'createdAt',
      header: 'Registration Date',
      render: (row) => <span className="mono-val">{formatDateOnly(row.createdAt)}</span>,
    },
    {
      key: 'assignedStudentCount',
      header: 'Assigned Students',
      render: (row) => (
        <span className="mono-label">
          {row.assignmentTrackingAvailable && row.assignedStudentCount != null
            ? row.assignedStudentCount
            : 'Not tracked yet'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/staff/consultants/${row.id}`} className="text-link">
          View
        </Link>
      ),
    },
  ];

  return (
    <div className="container workspace-page">
      <header className="workspace-header">
        <div>
          <div className="mono-label">Protected Route • /staff/dashboard</div>
          <h1 className="page-title">
            {isAdmin ? 'Administration & Staff Overview' : 'Staff Dashboard'}
          </h1>
          <p className="page-subtitle">
            {isAdmin
              ? 'Live account metrics, recent student registrations, and consultant overview backed by MySQL'
              : 'Consultant workspace with server-enforced access boundaries'}
          </p>
        </div>

        <div className="workspace-status-group">
          <span className="status-pill status-pill-up">Session Active</span>
          <span className="mono-badge">{account ? account.role : 'STAFF'}</span>
        </div>
      </header>

      {error && (
        <div className="notice-banner notice-banner-error" role="alert">
          {error}
        </div>
      )}

      {isAdmin ? (
        <div className="admin-overview-stack">
          <section aria-label="Account summary statistics">
            <div className="stats-grid">
              {statCards.map((stat) => (
                <article key={stat.label} className="stat-card">
                  <div className="stat-label">{stat.label}</div>
                  <div className="stat-value">{loading ? '—' : stat.value}</div>
                </article>
              ))}
            </div>
          </section>

          <section className="section-card" aria-labelledby="recent-students-heading">
            <div className="section-card-header">
              <div>
                <h2 id="recent-students-heading" className="health-card-title">
                  Recent Student Registrations
                </h2>
                <div className="health-card-endpoint">Latest student accounts from MySQL</div>
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
                  Consultant Overview
                </h2>
                <div className="health-card-endpoint">
                  Provisioned staff accounts and assignment readiness
                </div>
              </div>
              <Link to="/staff/consultants" className="btn btn-secondary btn-compact">
                <span>View all consultants</span>
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
      ) : (
        <div className="workspace-grid">
          <article className="health-card">
            <div className="health-card-top">
              <div className="health-card-title-group">
                <UserIcon className="card-icon" />
                <div>
                  <h2 className="health-card-title">Staff Profile & Access</h2>
                  <div className="health-card-endpoint">GET /api/v1/consultant/dashboard</div>
                </div>
              </div>
              <span className="status-pill status-pill-up">
                {loading ? 'VERIFYING' : 'VERIFIED'}
              </span>
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
                <dt>Role</dt>
                <dd className="mono-val">{account?.role || '—'}</dd>
              </div>
              <div className="metric-row">
                <dt>Consultant Endpoints</dt>
                <dd className="mono-val">
                  {permissions.canAccessConsultantWorkspace ? 'Permitted' : 'Checking'}
                </dd>
              </div>
              <div className="metric-row">
                <dt>Admin Endpoints</dt>
                <dd className="mono-val">Restricted (403 Forbidden)</dd>
              </div>
            </dl>
          </article>

          <article className="health-card">
            <div className="health-card-top">
              <div className="health-card-title-group">
                <ServerIcon className="card-icon" />
                <div>
                  <h2 className="health-card-title">Consultant Operations</h2>
                  <div className="health-card-endpoint">Authorized staff capabilities</div>
                </div>
              </div>
              <Link to="/staff/assigned-students" className="btn btn-secondary btn-compact">
                <span>Assigned Students</span>
                <ArrowRightIcon />
              </Link>
            </div>

            <div className="dossier-list">
              {capabilities.map((cap, index) => (
                <div key={cap} className="dossier-row">
                  <span className="dossier-index">{String(index + 1).padStart(2, '0')}</span>
                  <div className="dossier-row-title">{cap}</div>
                  <span className="dossier-stage-meta">CONSULTANT</span>
                </div>
              ))}
            </div>

            <div className="workspace-note">
              Administrative endpoints under /api/v1/admin/* remain restricted to ADMIN accounts
            </div>
          </article>
        </div>
      )}
    </div>
  );
}
