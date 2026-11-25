import { SearchIcon } from './Icons';

export function SearchFilterBar({
  searchValue,
  onSearchChange,
  statusValue,
  onStatusChange,
  onSubmit,
  onReset,
  searchPlaceholder = 'Search by name or email',
  disabled = false,
}) {
  function handleFormSubmit(event) {
    event.preventDefault();
    if (onSubmit) {
      onSubmit();
    }
  }

  const hasActiveFilter = Boolean((searchValue && searchValue.trim()) || (statusValue && statusValue !== 'ALL'));

  return (
    <form className="filter-bar" onSubmit={handleFormSubmit} role="search">
      <div className="filter-search-wrap">
        <SearchIcon className="filter-search-icon" />
        <input
          type="search"
          className="form-input filter-search-input"
          placeholder={searchPlaceholder}
          value={searchValue}
          onChange={(e) => onSearchChange(e.target.value)}
          disabled={disabled}
          aria-label={searchPlaceholder}
        />
      </div>

      <select
        className="form-input filter-select"
        value={statusValue}
        onChange={(e) => onStatusChange(e.target.value)}
        disabled={disabled}
        aria-label="Filter by account status"
      >
        <option value="ALL">All statuses</option>
        <option value="ACTIVE">Active</option>
        <option value="DISABLED">Disabled</option>
        <option value="SUSPENDED">Suspended</option>
      </select>

      <div className="filter-actions">
        <button type="submit" className="btn btn-secondary btn-compact" disabled={disabled}>
          Search
        </button>
        {hasActiveFilter && (
          <button
            type="button"
            className="btn btn-ghost btn-compact"
            onClick={onReset}
            disabled={disabled}
          >
            Reset
          </button>
        )}
      </div>
    </form>
  );
}
