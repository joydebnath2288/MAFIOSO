import { Navbar } from './Navbar';

export function Layout({ children }) {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-main">{children}</main>
      <footer className="site-footer">
        <div className="container footer-inner">
          <span className="footer-brand">MAFIOSO Study Abroad Consultancy Platform</span>
          <span className="footer-meta">Milestone 1 Foundation • Bangladesh to UK (BSc / MSc)</span>
        </div>
      </footer>
    </div>
  );
}
