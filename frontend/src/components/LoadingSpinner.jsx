export function LoadingSpinner({ label = 'Loading information...' }) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '12px',
        padding: '36px 16px',
        color: 'var(--text-secondary)',
        fontFamily: 'var(--font-mono)',
        fontSize: '13px',
      }}
      role="status"
    >
      <span
        style={{
          width: '18px',
          height: '18px',
          border: '2px solid var(--border-strong)',
          borderTopColor: 'var(--accent)',
          borderRadius: '50%',
          display: 'inline-block',
          animation: 'spin 0.8s linear infinite',
        }}
        aria-hidden="true"
      />
      <span>{label}</span>
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
