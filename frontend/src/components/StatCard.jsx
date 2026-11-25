export function StatCard({ label, value, meta, icon: Icon, badge }) {
  return (
    <article className="stat-card-item">
      <div className="stat-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span className="stat-card-label">{label}</span>
        {badge}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: '8px' }}>
        <span className="stat-card-number">{value != null ? value : '—'}</span>
        {Icon && (
          <span style={{ color: 'var(--text-muted)' }} aria-hidden="true">
            <Icon />
          </span>
        )}
      </div>
      {meta && <span className="stat-card-meta">{meta}</span>}
    </article>
  );
}
