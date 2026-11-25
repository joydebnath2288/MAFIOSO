import { useEffect, useState } from 'react';
import { ShieldIcon, UserIcon } from '../components/Icons';
import { useAuth } from '../hooks/useAuth';
import { fetchStudentDashboard } from '../services/authService';

export function StudentDashboardPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    async function load() {
      setLoading(true);
      setError('');
      const res = await fetchStudentDashboard();
      if (!active) {
        return;
      }
      if (res.ok && res.data && res.data.success) {
        setDashboard(res.data.data);
      } else {
        setError(res.error || 'Unable to load student workspace');
      }
      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, []);

  const account = (dashboard && dashboard.account) || user;
  const plannedModules = (dashboard && dashboard.plannedModules) || [];

  return (
    <div className="container workspace-page">
      <header className="workspace-header">
        <div>
          <div className="mono-label">Protected Route • /student/dashboard</div>
          <h1 className="page-title">Student Dashboard</h1>
          <p className="page-subtitle">
            Authenticated applicant workspace scoped strictly to your personal MAFIOSO record
          </p>
        </div>

        <div className="workspace-status-group">
          <span className="status-pill status-pill-up">Session Active</span>
          <span className="mono-badge">{account ? account.role : 'STUDENT'}</span>
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
                <h2 className="health-card-title">Authenticated Account</h2>
                <div className="health-card-endpoint">GET /api/v1/student/dashboard</div>
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
              <dt>Phone Number</dt>
              <dd className="mono-val">{account?.phoneNumber || 'Not provided'}</dd>
            </div>
            <div className="metric-row">
              <dt>Role Authority</dt>
              <dd className="mono-val">{account?.role || '—'}</dd>
            </div>
            <div className="metric-row">
              <dt>Access Scope</dt>
              <dd className="mono-val">{dashboard?.accessScope || 'SELF_ONLY'}</dd>
            </div>
          </dl>
        </article>

        <article className="health-card">
          <div className="health-card-top">
            <div className="health-card-title-group">
              <ShieldIcon className="card-icon" />
              <div>
                <h2 className="health-card-title">Admissions Workspace Modules</h2>
                <div className="health-card-endpoint">Scheduled for upcoming milestones</div>
              </div>
            </div>
          </div>

          <div className="dossier-list">
            {plannedModules.map((moduleName, index) => (
              <div key={moduleName} className="dossier-row">
                <span className="dossier-index">{String(index + 1).padStart(2, '0')}</span>
                <div className="dossier-row-title">{moduleName}</div>
                <span className="dossier-stage-meta">Pending M3+</span>
              </div>
            ))}
            {!loading && plannedModules.length === 0 && (
              <div className="mono-label">No modules returned</div>
            )}
          </div>
        </article>
      </div>
    </div>
  );
}
