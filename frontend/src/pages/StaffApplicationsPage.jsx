import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DataTable } from '../components/DataTable';
import { ArrowLeftIcon, SearchIcon } from '../components/Icons';
import { Pagination } from '../components/Pagination';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import { fetchAdminApplications } from '../services/adminService';
import { fetchConsultantApplications } from '../services/consultantService';
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

export function StaffApplicationsPage() {
  const { user } = useAuth();
  const isAdmin = user && user.role === 'ADMIN';

  const [searchInput, setSearchInput] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [degreeFilter, setDegreeFilter] = useState('ALL');
  const [unassignedOnly, setUnassignedOnly] = useState(false);
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadApplications = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = isAdmin
      ? await fetchAdminApplications({
          search: activeSearch,
          status: statusFilter,
          degreeLevel: degreeFilter,
          unassignedOnly,
          page,
          pageSize: PAGE_SIZE,
        })
      : await fetchConsultantApplications({
          search: activeSearch,
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
  }, [isAdmin, activeSearch, statusFilter, degreeFilter, unassignedOnly, page]);

  useEffect(() => {
    document.title = isAdmin ? 'All Applications — MAFIOSO' : 'Assigned Applications — MAFIOSO';
    loadApplications();
  }, [isAdmin, loadApplications]);

  function handleSearchSubmit(e) {
    e.preventDefault();
    setPage(1);
    setActiveSearch(searchInput.trim());
  }

  function handleReset() {
    setSearchInput('');
    setActiveSearch('');
    setStatusFilter('ALL');
    setDegreeFilter('ALL');
    setUnassignedOnly(false);
    setPage(1);
  }

  const columns = [
    {
      key: 'referenceNumber',
      header: 'Reference',
      render: (row) => <span className="mono-val">{row.referenceNumber}</span>,
    },
    {
      key: 'studentName',
      header: 'Student',
      render: (row) => (
        <div>
          <div className="table-primary-text">{row.studentName}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{row.studentEmail}</div>
        </div>
      ),
    },
    {
      key: 'preferredSubject',
      header: 'Program & University',
      render: (row) => (
        <div>
          <div className="table-primary-text">
            {row.degreeLevel} — {row.preferredSubject}
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            {row.preferredUniversity} ({row.intendedIntake})
          </div>
        </div>
      ),
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
        <Link to={`/staff/applications/${row.id}`} className="btn btn-secondary btn-compact">
          Manage
        </Link>
      ),
    },
  ];

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/staff/dashboard" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Staff Dashboard</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">
            {isAdmin ? 'Administration • All Applications' : 'Consultant Caseload • Applications'}
          </div>
          <h1 className="page-title">
            {isAdmin ? 'Application Management' : 'Assigned Applications'}
          </h1>
          <p className="page-subtitle">
            {isAdmin
              ? 'Search applications, assign consultants, update statuses, and verify documents'
              : 'Review assigned student applications, update progress, and verify documents'}
          </p>
        </div>
      </header>

      <section className="section-card">
        <form className={isAdmin ? 'filter-bar-wide' : 'filter-bar'} onSubmit={handleSearchSubmit} role="search">
          <div className="filter-search-wrap">
            <SearchIcon className="filter-search-icon" />
            <input
              type="search"
              className="form-input filter-search-input"
              placeholder="Search reference, student, subject, or university"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              disabled={loading}
            />
          </div>

          <select
            aria-label="Filter by status"
            className="form-input filter-select"
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

          {isAdmin && (
            <select
              aria-label="Filter by degree level"
              className="form-input filter-select"
              value={degreeFilter}
              onChange={(e) => {
                setPage(1);
                setDegreeFilter(e.target.value);
              }}
              disabled={loading}
            >
              <option value="ALL">All degrees</option>
              <option value="BSc">BSc</option>
              <option value="MSc">MSc</option>
            </select>
          )}

          <div className="filter-actions">
            <button type="submit" className="btn btn-secondary btn-compact" disabled={loading}>
              Search
            </button>
            {(activeSearch || statusFilter !== 'ALL' || degreeFilter !== 'ALL' || unassignedOnly) && (
              <button type="button" className="btn btn-ghost btn-compact" onClick={handleReset}>
                Reset
              </button>
            )}
          </div>
        </form>

        {isAdmin && (
          <label style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)', cursor: 'pointer' }}>
            <input
              type="checkbox"
              checked={unassignedOnly}
              onChange={(e) => {
                setPage(1);
                setUnassignedOnly(e.target.checked);
              }}
            />
            <span>Show unassigned applications only</span>
          </label>
        )}

        <DataTable
          columns={columns}
          rows={pagedData.items}
          loading={loading}
          error={error}
          onRetry={loadApplications}
          emptyTitle="No matching applications"
          emptyMessage="Adjust your search or status filter."
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
    </div>
  );
}
