import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { CreateApplicationModal } from '../components/CreateApplicationModal';
import { DataTable } from '../components/DataTable';
import { ArrowLeftIcon, PlusIcon } from '../components/Icons';
import { NoticeBanner } from '../components/NoticeBanner';
import { Pagination } from '../components/Pagination';
import { StatusBadge } from '../components/StatusBadge';
import { fetchStudentApplications, fetchStudentProfile } from '../services/studentService';
import { formatDateOnly } from '../utils/validation';

const PAGE_SIZE = 10;

const STATUS_OPTIONS = [
  'ALL',
  'SUBMITTED',
  'UNDER_REVIEW',
  'ASSIGNED',
  'DOCUMENTS_PENDING',
  'DOCUMENTS_REVIEW',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
];

export function StudentApplicationsPage() {
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(1);
  const [pagedData, setPagedData] = useState({
    items: [],
    page: 1,
    pageSize: PAGE_SIZE,
    totalItems: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false,
  });
  const [profilePrefs, setProfilePrefs] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [createOpen, setCreateOpen] = useState(false);

  const loadApplications = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = await fetchStudentApplications({
      status: statusFilter,
      page,
      pageSize: PAGE_SIZE,
    });
    if (res.ok && res.data && res.data.success && res.data.data) {
      setPagedData(res.data.data);
    } else {
      setError(res.error || 'Unable to load applications');
    }
    setLoading(false);
  }, [statusFilter, page]);

  useEffect(() => {
    document.title = 'My Applications — MAFIOSO';
    loadApplications();
  }, [loadApplications]);

  useEffect(() => {
    fetchStudentProfile().then((res) => {
      if (res.ok && res.data && res.data.success && res.data.data) {
        setProfilePrefs(res.data.data.profile || null);
      }
    });
  }, []);

  async function handleCreated(created) {
    setCreateOpen(false);
    setFeedback(`Application ${created.referenceNumber} submitted.`);
    setPage(1);
    await loadApplications();
  }

  const columns = [
    {
      key: 'referenceNumber',
      header: 'Reference',
      render: (row) => <span className="mono-val">{row.referenceNumber}</span>,
    },
    {
      key: 'degreeLevel',
      header: 'Degree',
      render: (row) => <span className="mono-badge">{row.degreeLevel}</span>,
    },
    {
      key: 'preferredSubject',
      header: 'Subject & University',
      render: (row) => (
        <div>
          <div className="table-primary-text">{row.preferredSubject}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{row.preferredUniversity}</div>
        </div>
      ),
    },
    {
      key: 'intendedIntake',
      header: 'Intake',
      render: (row) => <span>{row.intendedIntake}</span>,
    },
    {
      key: 'assignedConsultant',
      header: 'Consultant',
      render: (row) =>
        row.assignedConsultant ? (
          <span>{row.assignedConsultant.fullName}</span>
        ) : (
          <span className="mono-label">Unassigned</span>
        ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'submittedAt',
      header: 'Submitted',
      render: (row) => <span className="mono-val">{formatDateOnly(row.submittedAt)}</span>,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'right',
      render: (row) => (
        <Link to={`/student/applications/${row.id}`} className="btn btn-secondary btn-compact">
          Track progress
        </Link>
      ),
    },
  ];

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/student/dashboard" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Dashboard</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">Student Portal • Applications</div>
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">
            Create BSc or MSc study applications, track workflow milestones, and attach documents
          </p>
        </div>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setCreateOpen(true)}
        >
          <PlusIcon />
          <span>New Application</span>
        </button>
      </header>

      {feedback && <NoticeBanner type="success">{feedback}</NoticeBanner>}

      <section className="section-card">
        <div className="section-card-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <label htmlFor="stu-app-status-filter" className="form-label">
              Filter status
            </label>
            <select
              id="stu-app-status-filter"
              className="form-input filter-select"
              style={{ width: '220px' }}
              value={statusFilter}
              onChange={(e) => {
                setPage(1);
                setStatusFilter(e.target.value);
              }}
              disabled={loading}
            >
              {STATUS_OPTIONS.map((st) => (
                <option key={st} value={st}>
                  {st === 'ALL' ? 'All statuses' : st.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>
          <span className="mono-label">{pagedData.totalItems} total</span>
        </div>

        <DataTable
          columns={columns}
          rows={pagedData.items}
          loading={loading}
          error={error}
          onRetry={loadApplications}
          emptyTitle="No applications found"
          emptyMessage="Submit your first UK study application to receive a reference number."
        />

        <Pagination
          page={pagedData.page}
          totalPages={pagedData.totalPages}
          totalItems={pagedData.totalItems}
          hasPrevious={pagedData.hasPrevious}
          hasNext={pagedData.hasNext}
          onPageChange={setPage}
          disabled={loading}
        />
      </section>

      <CreateApplicationModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={handleCreated}
        defaultPreferences={profilePrefs}
      />
    </div>
  );
}
