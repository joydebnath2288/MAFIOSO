const STATUS_CONFIG = {
  ACTIVE: { cls: 'status-pill-up', label: 'ACTIVE' },
  APPROVED: { cls: 'status-pill-up', label: 'APPROVED' },
  COMPLETED: { cls: 'status-pill-up', label: 'COMPLETED' },

  DISABLED: { cls: 'status-pill-down', label: 'DISABLED' },
  SUSPENDED: { cls: 'status-pill-down', label: 'SUSPENDED' },
  REJECTED: { cls: 'status-pill-down', label: 'REJECTED' },
  CANCELLED: { cls: 'status-pill-down', label: 'CANCELLED' },

  SUBMITTED: { cls: 'status-pill-accent', label: 'SUBMITTED' },
  ASSIGNED: { cls: 'status-pill-accent', label: 'ASSIGNED' },
  IN_PROGRESS: { cls: 'status-pill-accent', label: 'IN PROGRESS' },
  UNDER_REVIEW: { cls: 'status-pill-accent', label: 'UNDER REVIEW' },
  DOCUMENTS_REVIEW: { cls: 'status-pill-accent', label: 'DOCUMENTS REVIEW' },

  DOCUMENTS_PENDING: { cls: 'status-pill-pending', label: 'DOCUMENTS PENDING' },
  PENDING: { cls: 'status-pill-pending', label: 'PENDING' },
};

export function StatusBadge({ status }) {
  const normalized = (status || 'UNKNOWN').toUpperCase();
  const cfg = STATUS_CONFIG[normalized] || { cls: 'status-pill-pending', label: normalized.replace(/_/g, ' ') };
  return <span className={`status-pill ${cfg.cls}`}>{cfg.label}</span>;
}
