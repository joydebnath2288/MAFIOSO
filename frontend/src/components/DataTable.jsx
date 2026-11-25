export function DataTable({
  columns,
  rows,
  rowKey = (row) => row.id,
  loading = false,
  error = '',
  onRetry,
  emptyTitle = 'No records found',
  emptyMessage = 'No matching entries exist in the database.',
}) {
  if (error) {
    return (
      <div className="table-state-card notice-banner-error" role="alert">
        <div>{error}</div>
        {onRetry && (
          <button type="button" className="btn btn-secondary btn-compact" onClick={onRetry}>
            Retry
          </button>
        )}
      </div>
    );
  }

  if (loading) {
    return (
      <div className="table-state-card" role="status">
        <span className="status-dot status-dot-pending" aria-hidden="true" />
        <span className="mono-label">Loading records</span>
      </div>
    );
  }

  if (!rows || rows.length === 0) {
    return (
      <div className="table-state-card">
        <div className="table-empty-title">{emptyTitle}</div>
        <div className="table-empty-desc">{emptyMessage}</div>
      </div>
    );
  }

  return (
    <div className="data-table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key} className={col.align === 'right' ? 'text-right' : ''}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)}>
              {columns.map((col) => (
                <td key={col.key} className={col.align === 'right' ? 'text-right' : ''}>
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
