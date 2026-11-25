export function DashboardHeader({
  kicker,
  title,
  subtitle,
  badge,
  actions,
}) {
  return (
    <header className="workspace-header">
      <div>
        {kicker && <div className="mono-label">{kicker}</div>}
        <h1 className="page-title">{title}</h1>
        {subtitle && <p className="page-subtitle">{subtitle}</p>}
      </div>

      <div className="workspace-status-group">
        {badge}
        {actions}
      </div>
    </header>
  );
}
