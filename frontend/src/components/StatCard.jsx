export function StatCard({ label, value, meta, badge }) {
  return (
    <article className="stat-card-item">
      <div className="stat-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span className="stat-card-label">{label}</span>
        {badge}
      </div>
      <span className="stat-card-number">{value != null ? value : '—'}</span>
      {meta && <span className="stat-card-meta">{meta}</span>}
    </article>
  );
}
