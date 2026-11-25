import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DetailCard } from '../components/DetailCard';
import { ShieldIcon, UserIcon } from '../components/Icons';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import { fetchStudentProfile } from '../services/authService';
import { formatDateTime } from '../utils/validation';

export function StudentProfilePage() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    async function load() {
      setLoading(true);
      setError('');
      const res = await fetchStudentProfile();
      if (!active) {
        return;
      }
      if (res.ok && res.data && res.data.success) {
        setData(res.data.data);
      } else {
        setError(res.error || 'Unable to load student profile');
      }
      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, []);

  const account = (data && data.account) || user;

  return (
    <div className="container workspace-page">
      <header className="workspace-header">
        <div>
          <div className="mono-label">Student Portal • /student/profile</div>
          <h1 className="page-title">Student Profile</h1>
          <p className="page-subtitle">
            Your personal account details scoped strictly to your authenticated session
          </p>
        </div>

        <Link to="/student/dashboard" className="btn btn-secondary btn-compact">
          Return to Student Dashboard
        </Link>
      </header>

      {error && (
        <div className="notice-banner notice-banner-error" role="alert">
          {error}
        </div>
      )}

      <div className="workspace-grid">
        <DetailCard
          title="Personal Account"
          subtitle="GET /api/v1/student/profile"
          icon={UserIcon}
          badge={
            account ? (
              <StatusBadge status={account.status || 'ACTIVE'} />
            ) : (
              <span className="status-pill status-pill-pending">
                {loading ? 'LOADING' : 'UNKNOWN'}
              </span>
            )
          }
          rows={[
            { label: 'Student ID', value: account ? `#${account.id}` : '—', mono: true },
            { label: 'Full Name', value: account?.fullName || '—' },
            { label: 'Email Address', value: account?.email || '—', mono: true },
            { label: 'Phone Number', value: account?.phoneNumber || 'Not provided', mono: true },
            { label: 'Role', value: account?.role || 'STUDENT', mono: true },
            { label: 'Registered On', value: formatDateTime(account?.createdAt), mono: true },
            { label: 'Last Login', value: formatDateTime(account?.lastLoginAt), mono: true },
          ]}
        />

        <DetailCard
          title="Academic Profile & Documents"
          subtitle="Prepared for upcoming milestones"
          icon={ShieldIcon}
          badge={<span className="status-pill status-pill-pending">PLANNED</span>}
          rows={[
            { label: 'Academic Qualifications', value: 'Pending upcoming milestone', mono: true },
            { label: 'IELTS / English Test', value: 'Pending upcoming milestone', mono: true },
            { label: 'Target UK Intake', value: 'Pending upcoming milestone', mono: true },
            {
              label: 'Availability',
              value:
                (data && data.academicProfileNote) ||
                'Academic profile forms and document uploads will be added in upcoming milestones.',
            },
          ]}
        />
      </div>
    </div>
  );
}
