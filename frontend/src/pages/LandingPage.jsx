import { Link } from 'react-router-dom';
import { ArrowRightIcon, DatabaseIcon, GlobeIcon, ServerIcon } from '../components/Icons';

const PATHWAY_STAGES = [
  {
    code: '01',
    title: 'Academic Credential Review',
    detail: 'HSC, A-Level, and BSc transcript verification for Bangladesh applicants',
    status: 'Planned — M2',
  },
  {
    code: '02',
    title: 'UK University Shortlisting',
    detail: 'BSc and MSc entry criteria, tuition bands, and scholarship matching',
    status: 'Planned — M3',
  },
  {
    code: '03',
    title: 'Application & Document Vault',
    detail: 'Private persistent document storage for SOPs, IELTS, and passports',
    status: 'Storage Ready',
  },
  {
    code: '04',
    title: 'Consultancy Fee & CAS Readiness',
    detail: 'SSLCOMMERZ payment settlement and real-time consultant guidance',
    status: 'Planned — M4',
  },
];

const ARCHITECTURE_PILLARS = [
  {
    icon: ServerIcon,
    title: 'Java 8 & Tomcat 9 Core',
    description:
      'Modular monolithic Servlet 4.0 WAR packaged as ROOT.war with Jackson JSON serialization and JSR-356 WebSocket support.',
    meta: '/api/v1/* • /ws/*',
  },
  {
    icon: GlobeIcon,
    title: 'React + Nginx Edge',
    description:
      'Multi-stage Vite production bundle served by Nginx with SPA fallback and same-origin reverse proxying.',
    meta: 'Port 80 • Same-origin',
  },
  {
    icon: DatabaseIcon,
    title: 'Private MySQL 8 & Storage',
    description:
      'Isolated MySQL 8 database reachable only on the private Docker network, backed by named persistent volumes.',
    meta: 'mafioso_db • Internal',
  },
];

export function LandingPage() {
  return (
    <div className="landing-page">
      <section className="hero-section">
        <div className="container hero-grid">
          <div className="hero-copy">
            <div className="hero-kicker">
              <span className="status-dot status-dot-accent" aria-hidden="true" />
              <span className="mono-label">UK Admissions Infrastructure • Milestone 1</span>
            </div>

            <h1 className="hero-title">
              UK BSc and MSc admissions, structured from Bangladesh to London
            </h1>

            <p className="hero-subtitle">
              MAFIOSO is a study-abroad consultancy platform built for Bangladeshi students
              pursuing undergraduate and postgraduate degrees in the United Kingdom. This release
              establishes the containerized backend, frontend, reverse proxy, and database
              foundation.
            </p>

            <div className="hero-actions">
              <Link to="/health" className="btn btn-primary">
                <span>Inspect System Health</span>
                <ArrowRightIcon />
              </Link>
              <a href="#foundation" className="btn btn-secondary">
                Architecture Overview
              </a>
            </div>
          </div>

          <aside className="dossier-card" aria-label="MAFIOSO Platform Roadmap Preview">
            <div className="dossier-header">
              <div>
                <div className="dossier-title">Admissions Dossier Structure</div>
                <div className="dossier-subtitle">Target corridor: Bangladesh → United Kingdom</div>
              </div>
              <span className="mono-badge">UK-BD // M1</span>
            </div>

            <div className="dossier-list">
              {PATHWAY_STAGES.map((stage) => (
                <div key={stage.code} className="dossier-row">
                  <span className="dossier-index">{stage.code}</span>
                  <div className="dossier-row-body">
                    <div className="dossier-row-title">{stage.title}</div>
                    <div className="dossier-row-detail">{stage.detail}</div>
                  </div>
                  <span className="dossier-stage-meta">{stage.status}</span>
                </div>
              ))}
            </div>

            <div className="dossier-footer">
              <span>Containerized modular monolith</span>
              <span className="mono-label">Java 8 • Tomcat 9 • MySQL 8</span>
            </div>
          </aside>
        </div>
      </section>

      <section id="foundation" className="foundation-section">
        <div className="container">
          <div className="section-header">
            <h2 className="section-title">Verified Foundation Stack</h2>
            <p className="section-subtitle">
              Three isolated Docker services wired over a private network with persistent storage
              and single-origin Nginx routing.
            </p>
          </div>

          <div className="pillar-grid">
            {ARCHITECTURE_PILLARS.map((pillar) => {
              const Icon = pillar.icon;
              return (
                <article key={pillar.title} className="pillar-card">
                  <div className="pillar-icon-wrap">
                    <Icon />
                  </div>
                  <h3 className="pillar-title">{pillar.title}</h3>
                  <p className="pillar-desc">{pillar.description}</p>
                  <div className="pillar-meta">{pillar.meta}</div>
                </article>
              );
            })}
          </div>
        </div>
      </section>
    </div>
  );
}
