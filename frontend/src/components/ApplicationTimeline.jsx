import { StatusBadge } from './StatusBadge';
import { formatDateTime } from '../utils/validation';

const WORKFLOW_STAGES = [
  { key: 'SUBMITTED', label: 'Submitted' },
  { key: 'UNDER_REVIEW', label: 'Under review' },
  { key: 'ASSIGNED', label: 'Assigned' },
  { key: 'DOCUMENTS_PENDING', label: 'Documents pending' },
  { key: 'DOCUMENTS_REVIEW', label: 'Documents review' },
  { key: 'IN_PROGRESS', label: 'In progress' },
  { key: 'COMPLETED', label: 'Completed' },
];

export function ApplicationTimeline({ currentStatus, history = [] }) {
  const normalizedCurrent = (currentStatus || '').toUpperCase();
  const isCancelled = normalizedCurrent === 'CANCELLED';
  const currentIdx = WORKFLOW_STAGES.findIndex((s) => s.key === normalizedCurrent);

  return (
    <div className="timeline-stack">
      <div className="workflow-stepper" aria-label="Application progress stages">
        {WORKFLOW_STAGES.map((stage, index) => {
          let stateClass = 'workflow-step-upcoming';
          if (!isCancelled) {
            if (index < currentIdx) {
              stateClass = 'workflow-step-done';
            } else if (index === currentIdx) {
              stateClass = 'workflow-step-active';
            }
          }
          return (
            <div key={stage.key} className={`workflow-step ${stateClass}`}>
              <span className="workflow-step-dot" aria-hidden="true" />
              <span className="workflow-step-label">{stage.label}</span>
            </div>
          );
        })}
        {isCancelled && (
          <div className="workflow-step workflow-step-cancelled">
            <span className="workflow-step-dot" aria-hidden="true" />
            <span className="workflow-step-label">Cancelled</span>
          </div>
        )}
      </div>

      <div className="timeline-history">
        <div className="timeline-history-heading">Status history</div>
        {history.length === 0 ? (
          <div className="mono-label">No status transitions recorded yet.</div>
        ) : (
          <ol className="timeline-list">
            {history.map((entry) => (
              <li key={entry.id} className="timeline-entry">
                <div className="timeline-entry-dot" aria-hidden="true" />
                <div className="timeline-entry-body">
                  <div className="timeline-entry-top">
                    <div className="timeline-entry-statuses">
                      {entry.previousStatus ? (
                        <>
                          <StatusBadge status={entry.previousStatus} />
                          <span className="mono-label" aria-hidden="true">→</span>
                          <StatusBadge status={entry.newStatus} />
                        </>
                      ) : (
                        <StatusBadge status={entry.newStatus} />
                      )}
                    </div>
                    <span className="mono-val">{formatDateTime(entry.changedAt)}</span>
                  </div>
                  <div className="timeline-entry-meta">
                    Updated by {entry.changedByName || 'System'} ({entry.changedByRole || 'STAFF'})
                  </div>
                  {entry.note && <div className="timeline-entry-note">{entry.note}</div>}
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>
    </div>
  );
}
