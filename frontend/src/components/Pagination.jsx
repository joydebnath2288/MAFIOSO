import { ChevronLeftIcon, ChevronRightIcon } from './Icons';

export function Pagination({
  page,
  totalPages,
  totalItems,
  hasPrevious,
  hasNext,
  onPageChange,
  disabled = false,
}) {
  if (totalItems === 0 && page === 1) {
    return null;
  }

  return (
    <nav className="pagination-bar" aria-label="Table pagination">
      <div className="pagination-summary">
        Page <span className="mono-val">{page}</span> of{' '}
        <span className="mono-val">{Math.max(1, totalPages)}</span> •{' '}
        <span className="mono-val">{totalItems}</span> total
      </div>

      <div className="pagination-controls">
        <button
          type="button"
          className="btn btn-secondary btn-compact"
          onClick={() => onPageChange(page - 1)}
          disabled={disabled || !hasPrevious}
        >
          <ChevronLeftIcon />
          <span>Previous</span>
        </button>
        <button
          type="button"
          className="btn btn-secondary btn-compact"
          onClick={() => onPageChange(page + 1)}
          disabled={disabled || !hasNext}
        >
          <span>Next</span>
          <ChevronRightIcon />
        </button>
      </div>
    </nav>
  );
}
