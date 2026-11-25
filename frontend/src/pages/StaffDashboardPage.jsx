import { useEffect, useState } from 'react';
import { ServerIcon, ShieldIcon, UserIcon } from '../components/Icons';
import { useAuth } from '../hooks/useAuth';
import { fetchAdminDashboard, fetchConsultantDashboard } from '../services/authService';

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

      if (adminRes && adminRes.ok && adminRes.data && adminRes.data.success) {
        setAdminData(adminRes.data.data);
      }

      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, [isAdmin]);

  const account = (consultantData && consultantData.account) || user;
  const permissions = (consultantData && consultantData.permissions) || {};
  const capabilities = (consultantData && consultantData.authorizedCapabilities) || [];
  const roleCounts = (adminData && adminData.roleCounts) || null;

  return (
    <div className="container workspace-page">
      <header className="workspace-header">
        <div>
          <div className="mono-label">Protected Route • /staff/dashboard</div>
          <h1 className="page-title">Staff Dashboard</h1>
          <p className="page-subtitle">
            Shared layout for Consultant and Administrator roles with server-enforced permission
            boundaries
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
              <dd className="mono-val">
                {isAdmin ? 'Permitted (ADMIN)' : 'Restricted (403 Forbidden)'}
              </dd>
            </div>
          </dl>
        </article>

        {isAdmin ? (
          <article className="health-card">
            <div className="health-card-top">
              <div className="health-card-title-group">
                <ShieldIcon className="card-icon" />
                <div>
                  <h2 className="health-card-title">Administrator Overview</h2>
                  <div className="health-card-endpoint">GET /api/v1/admin/dashboard</div>
                </div>
              </div>
              <span className="status-pill status-pill-up">ADMIN ONLY</span>
            </div>

            {roleCounts ? (
              <dl className="metric-list">
                <div className="metric-row">
                  <dt>Registered Students</dt>
                  <dd className="mono-val">{roleCounts.STUDENT ?? 0}</dd>
                </div>
                <div className="metric-row">
                  <dt>Active Consultants</dt>
                  <dd className="mono-val">{roleCounts.CONSULTANT ?? 0}</dd>
                </div>
                <div className="metric-row">
                  <dt>Administrators</dt>
                  <dd className="mono-val">{roleCounts.ADMIN ?? 0}</dd>
                </div>
              </dl>
            ) : (
              <div className="mono-label">Loading administration summary</div>
            )}

            {adminData && adminData.staffProvisioningNotice && (
              <div className="workspace-note">{adminData.staffProvisioningNotice}</div>
            )}
          </article>
        ) : (
          <article className="health-card">
            <div className="health-card-top">
              <div className="health-card-title-group">
                <ServerIcon className="card-icon" />
                <div>
                  <h2 className="health-card-title">Consultant Operations</h2>
                  <div className="health-card-endpoint">Authorized staff capabilities</div>
                </div>
              </div>
              <span className="status-pill status-pill-pending">STAFF SCOPE</span>
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
        )}
      </div>
    </div>
  );
}
