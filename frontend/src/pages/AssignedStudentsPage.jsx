import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DataTable } from '../components/DataTable';
import { ArrowLeftIcon, SearchIcon } from '../components/Icons';
import { Pagination } from '../components/Pagination';
import { StatusBadge } from '../components/StatusBadge';
import { fetchAssignedStudents } from '../services/consultantService';
import { formatDateOnly } from '../utils/validation';

const PAGE_SIZE = 10;

export function AssignedStudentsPage() {
  const [searchInput, setSearchInput] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
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

  const loadAssigned = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = await fetchAssignedStudents({
      search: activeSearch,
      page,
      pageSize: PAGE_SIZE,
    });
    if (res.ok && res.data && res.data.success && res.data.data) {
      setPagedData(res.data.data);
    } else {
      setError(res.error || 'Unable to load assigned students');
    }
    setLoading(false);
  }, [activeSearch, page]);

  useEffect(() => {
    document.title = 'Assigned Students — MAFIOSO';
    loadAssigned();
  }, [loadAssigned]);

  function handleSearchSubmit(e) {
    e.preventDefault();
    setPage(1);
    setActiveSearch(searchInput.trim());
  }

  const columns = [
    {
      key: 'studentId',
      header: 'Student ID',
      render: (row) => <span className="mono-val">#{row.studentId}</span>,
    },
    {
      key: 'fullName',
      header: 'Student Name',
      render: (row) => (
        <div>
          <div className="table-primary-text">{row.fullName}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{row.email}</div>
        </div>
      ),
    },
    {
      key: 'latestApplicationReference',
      header: 'Latest Application',
      render: (row) => (
        <div>
          <div className="mono-val">{row.latestApplicationReference || '—'}</div>
          {row.latestApplicationStatus && <StatusBadge status={row.latestApplicationStatus} />}
        </div>
      ),
    },
    {
      key: 'activeApplicationCount',
      header: 'Assigned Apps',
      render: (row) => <span className="mono-val">{row.activeApplicationCount}</span>,
    },
    {
      key: 'assignedSince',
      header: 'Assigned Since',
      render: (row) => <span className="mono-val">{formatDateOnly(row.assignedSince)}</span>,
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (row) => (
        <Link
          to={`/staff/assigned-students/${row.studentId}`}
          className="btn btn-secondary btn-compact"
        >
          Inspect dossier
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
          <div className="mono-label">Consultant Caseload • Assigned Students</div>
          <h1 className="page-title">Assigned Students</h1>
          <p className="page-subtitle">
            Students actively paired with your consultant account
          </p>
        </div>
      </header>

      <section className="section-card">
        <form className="filter-bar" onSubmit={handleSearchSubmit} role="search">
          <div className="filter-search-wrap">
            <SearchIcon className="filter-search-icon" />
            <input
              type="search"
              className="form-input filter-search-input"
              placeholder="Search by student name, email, or reference"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              disabled={loading}
            />
          </div>
          <div className="filter-actions">
            <button type="submit" className="btn btn-secondary btn-compact" disabled={loading}>
              Search
            </button>
            {activeSearch && (
              <button
                type="button"
                className="btn btn-ghost btn-compact"
                onClick={() => {
                  setSearchInput('');
                  setActiveSearch('');
                  setPage(1);
                }}
              >
                Reset
              </button>
            )}
          </div>
        </form>

        <DataTable
          columns={columns}
          rows={pagedData.items}
          rowKey={(row) => row.studentId}
          loading={loading}
          error={error}
          onRetry={loadAssigned}
          emptyTitle="No assigned students"
          emptyMessage="Students assigned to you by an administrator will appear here."
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
