import { Link, NavLink } from 'react-router-dom';

export function DashboardSidebar({ sections = [] }) {
  return (
    <aside className="dashboard-sidebar" aria-label="Portal Navigation">
      {sections.map((section, sIdx) => (
        <div key={section.title || sIdx} className="sidebar-section">
          {section.title && <div className="sidebar-section-title">{section.title}</div>}
          <nav className="sidebar-nav-list">
            {section.items.map((item) => {
              const Icon = item.icon;
              const content = (
                <>
                  <span className="sidebar-nav-icon-label">
                    {Icon && (
                      <span style={{ color: 'var(--text-muted)' }} aria-hidden="true">
                        <Icon />
                      </span>
                    )}
                    <span>{item.label}</span>
                  </span>
                  {item.badge && <span className="badge-coming-soon">{item.badge}</span>}
                </>
              );

              if (item.to) {
                return (
                  <NavLink
                    key={item.label}
                    to={item.to}
                    end={item.end}
                    className={({ isActive }) =>
                      `sidebar-nav-item${isActive ? ' sidebar-nav-item-active' : ''}`
                    }
                  >
                    {content}
                  </NavLink>
                );
              }

              return (
                <button
                  key={item.label}
                  type="button"
                  className={`sidebar-nav-item${item.active ? ' sidebar-nav-item-active' : ''}`}
                  onClick={item.onClick}
                  disabled={item.disabled}
                >
                  {content}
                </button>
              );
            })}
          </nav>
        </div>
      ))}
    </aside>
  );
}
