export function DetailCard({ title, subtitle, icon: Icon, badge, rows }) {
  return (
    <article className="health-card">
      <div className="health-card-top">
        <div className="health-card-title-group">
          {Icon && <Icon className="card-icon" />}
          <div>
            <h2 className="health-card-title">{title}</h2>
            {subtitle && <div className="health-card-endpoint">{subtitle}</div>}
          </div>
        </div>
        {badge}
      </div>

      <dl className="metric-list">
        {rows.map((item) => (
          <div key={item.label} className="metric-row">
            <dt>{item.label}</dt>
            <dd className={item.mono ? 'mono-val' : ''}>{item.value ?? '—'}</dd>
          </div>
        ))}
      </dl>
    </article>
  );
}
