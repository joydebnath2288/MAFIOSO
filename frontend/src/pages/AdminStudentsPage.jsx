import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { DataTable } from '../components/DataTable';
import { Pagination } from '../components/Pagination';
import { SearchFilterBar } from '../components/SearchFilterBar';
import { StatusBadge } from '../components/StatusBadge';
import { fetchAdminStudents, updateAdminStudentStatus } from '../services/adminService';
import { formatDateOnly, formatDateTime } from '../utils/validation';

const PAGE_SIZE = 10;

export function AdminStudentsPage() {
  const [searchInput, setSearchInput] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [pendingToggle, setPendingToggle] = useState(null);
  const [toggling, setToggling] = useState(false);

  const loadStudents = useCallback(async () => {
    setLoading(true);
    setError('');
    const res = await fetchAdminStudents({
      search: activeSearch,
      status: statusFilter,
      page,
      pageSize: PAGE_SIZE,
    });

    if (res.ok && res.data && res.data.success && res.data.data) {
      setPagedData(res.data.data);
    } else {
      setError(res.error || 'Unable to load student records');
    }
    setLoading(false);
  }, [activeSearch, statusFilter, page]);

  useEffect(() => {
    loadStudents();
  }, [loadStudents]);

  function handleSearchSubmit() {
    setPage(1);
    setActiveSearch(searchInput.trim());
  }

  function handleStatusChange(nextStatus) {
    setPage(1);
    setStatusFilter(nextStatus);
  }

  function handleReset() {
    setSearchInput('');
    setActiveSearch('');
    setStatusFilter('ALL');
    setPage(1);
  }

  async function confirmStatusToggle() {
    if (!pendingToggle || toggling) {
      return;
    }
    setToggling(true);
    setFeedback('');
    setError('');
    try {
      const nextStatus = pendingToggle.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
      const res = await updateAdminStudentStatus(pendingToggle.id, nextStatus);
      if (res.ok && res.data && res.data.success) {
        setFeedback(
          `Student account ${pendingToggle.email} set to ${nextStatus}`
        );
        setPendingToggle(null);
        await loadStudents();
      } else {
        setError(res.error || 'Unable to update student status');
        setPendingToggle(null);
      }
    } finally {
      setToggling(false);
    }
  }

  const columns = [
    {
      key: 'id',
      header: 'ID',
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
      key: 'lastLoginAt',
      header: 'Last Login',
      render: (row) => <span className="mono-val">{formatDateTime(row.lastLoginAt)}</span>,
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (row) => (
        <div className="table-action-group">
          <Link to={`/staff/students/${row.id}`} className="btn btn-secondary btn-compact">
            Details
          </Link>
          <button
            type="button"
            className="btn btn-nav-outline btn-compact"
            onClick={() => setPendingToggle(row)}
          >
            {row.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="container workspace-page">
      <header className="workspace-header">
        <div>
          <div className="mono-label">Administration • /staff/students</div>
          <h1 className="page-title">Student Management</h1>
          <p className="page-subtitle">
            Search, filter, inspect, and manage activation status for registered student accounts
          </p>
        </div>
      </header>

      {feedback && (
        <div className="notice-banner notice-banner-success" role="status">
          {feedback}
        </div>
      )}

      <section className="section-card">
        <SearchFilterBar
          searchValue={searchInput}
          onSearchChange={setSearchInput}
          statusValue={statusFilter}
          onStatusChange={handleStatusChange}
          onSubmit={handleSearchSubmit}
          onReset={handleReset}
          searchPlaceholder="Search students by name or email"
          disabled={loading}
        />

        <DataTable
          columns={columns}
          rows={pagedData.items}
          loading={loading}
          error={error}
          onRetry={loadStudents}
          emptyTitle="No matching students"
          emptyMessage="Adjust your search query or account status filter."
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

      <ConfirmDialog
        open={Boolean(pendingToggle)}
        title={
          pendingToggle && pendingToggle.status === 'ACTIVE'
            ? 'Deactivate student account'
            : 'Activate student account'
        }
        message={
          pendingToggle
            ? pendingToggle.status === 'ACTIVE'
              ? `Deactivating ${pendingToggle.fullName} (${pendingToggle.email}) will block login and invalidate any active session immediately.`
              : `Activating ${pendingToggle.fullName} (${pendingToggle.email}) will allow them to sign in again.`
            : ''
        }
        confirmLabel={
          pendingToggle && pendingToggle.status === 'ACTIVE' ? 'Deactivate' : 'Activate'
        }
        confirmVariant={pendingToggle && pendingToggle.status === 'ACTIVE' ? 'danger' : 'primary'}
        busy={toggling}
        onConfirm={confirmStatusToggle}
        onCancel={() => setPendingToggle(null)}
      />
    </div>
  );
}
