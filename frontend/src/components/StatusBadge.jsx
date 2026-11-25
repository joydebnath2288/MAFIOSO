export function StatusBadge({ status }) {
  const normalized = (status || 'UNKNOWN').toUpperCase();
  let variantClass = 'status-pill-pending';
  if (normalized === 'ACTIVE') {
    variantClass = 'status-pill-up';
  } else if (normalized === 'DISABLED') {
    variantClass = 'status-pill-down';
  }

  return <span className={`status-pill ${variantClass}`}>{normalized}</span>;
}
