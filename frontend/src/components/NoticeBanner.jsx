export function NoticeBanner({ type = 'info', children, role }) {
  const resolvedRole = role || (type === 'error' ? 'alert' : 'status');
  return (
    <div className={`notice-banner notice-banner-${type}`} role={resolvedRole}>
      {children}
    </div>
  );
}
