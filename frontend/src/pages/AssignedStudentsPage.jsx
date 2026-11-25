import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { UsersIcon } from '../components/Icons';
import { fetchAssignedStudents } from '../services/authService';

export function AssignedStudentsPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    async function load() {
      setLoading(true);
      setError('');
      const res = await fetchAssignedStudents();
      if (!active) {
        return;
      }
      if (res.ok && res.data && res.data.success) {
        setData(res.data.data);
      } else {
        setError(res.error || 'Unable to load assigned students');
      }
      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="container workspace-page">
      <header className="workspace-header">
        <div>
          <div className="mono-label">Staff Operations • /staff/assigned-students</div>
          <h1 className="page-title">Assigned Students</h1>
          <p className="page-subtitle">
            Applicant caseload assigned to your consultant profile
          </p>
        </div>

        <Link to="/staff/dashboard" className="btn btn-secondary btn-compact">
          Return to Staff Dashboard
        </Link>
      </header>

      {error && (
        <div className="notice-banner notice-banner-error" role="alert">
          {error}
        </div>
      )}

      <section className="section-card">
        <div className="health-card-top">
          <div className="health-card-title-group">
            <UsersIcon className="card-icon" />
            <div>
              <h2 className="health-card-title">Caseload Assignment Status</h2>
              <div className="health-card-endpoint">GET /api/v1/consultant/assigned-students</div>
            </div>
          </div>
          <span className="status-pill status-pill-pending">
            {loading ? 'CHECKING' : 'NOT YET ENABLED'}
          </span>
        </div>

        <div className="table-state-card">
          <div className="table-empty-title">No student assignment records yet</div>
          <div className="table-empty-desc">
            {(data && data.message) ||
              'Student-to-consultant assignment relationships will be added in upcoming milestones.'}
          </div>
        </div>
      </section>
    </div>
  );
}
