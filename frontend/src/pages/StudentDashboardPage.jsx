import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DashboardHeader } from '../components/DashboardHeader';
import { DashboardSidebar } from '../components/DashboardSidebar';
import { EmptyState } from '../components/EmptyState';
import {
  ArrowRightIcon,
  AwardIcon,
  BookOpenIcon,
  CalendarIcon,
  CheckCircleIcon,
  ClockIcon,
  CreditCardIcon,
  FileTextIcon,
  FolderIcon,
  GraduationCapIcon,
  HelpCircleIcon,
  HomeIcon,
  LockIcon,
  MailIcon,
  MessageSquareIcon,
  PhoneCallIcon,
  ShieldIcon,
  UserIcon,
  UsersIcon,
} from '../components/Icons';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { NoticeBanner } from '../components/NoticeBanner';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import { fetchStudentDashboard } from '../services/authService';
import { formatDateTime } from '../utils/validation';

export function StudentDashboardPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    document.title = 'Student Dashboard — MAFIOSO';
    let active = true;

    async function load() {
      setLoading(true);
      setError('');
      const res = await fetchStudentDashboard();
      if (!active) {
        return;
      }
      if (res.ok && res.data && res.data.success) {
        setDashboard(res.data.data);
      } else {
        setError(res.error || 'Unable to load student workspace');
      }
      setLoading(false);
    }
    load();
    return () => {
      active = false;
    };
  }, []);

  const account = (dashboard && dashboard.account) || user;
  const plannedModules = (dashboard && dashboard.plannedModules) || [];

  const sidebarSections = [
    {
      title: 'Admissions Portal',
      items: [
        {
          key: 'overview',
          label: 'Overview',
          icon: GraduationCapIcon,
          active: activeTab === 'overview',
          onClick: () => setActiveTab('overview'),
        },
        {
          to: '/student/profile',
          label: 'My Profile',
          icon: UserIcon,
        },
        {
          key: 'applications',
          label: 'My Applications',
          icon: FileTextIcon,
          badge: 'M4',
          active: activeTab === 'applications',
          onClick: () => setActiveTab('applications'),
        },
        {
          key: 'documents',
          label: 'Documents',
          icon: FolderIcon,
          badge: 'M4',
          active: activeTab === 'documents',
          onClick: () => setActiveTab('documents'),
        },
      ],
    },
    {
      title: 'Consultancy & Services',
      items: [
        {
          key: 'consultations',
          label: 'Consultations',
          icon: PhoneCallIcon,
          badge: 'M4',
          active: activeTab === 'consultations',
          onClick: () => setActiveTab('consultations'),
        },
        {
          key: 'payments',
          label: 'Payments',
          icon: CreditCardIcon,
          badge: 'M4',
          active: activeTab === 'payments',
          onClick: () => setActiveTab('payments'),
        },
        {
          key: 'messages',
          label: 'Messages',
          icon: MessageSquareIcon,
          badge: 'M4',
          active: activeTab === 'messages',
          onClick: () => setActiveTab('messages'),
        },
        {
          key: 'help',
          label: 'Help Center',
          icon: HelpCircleIcon,
          active: activeTab === 'help',
          onClick: () => setActiveTab('help'),
        },
      ],
    },
  ];

  return (
    <div className="dashboard-container">
      <DashboardSidebar sections={sidebarSections} />

      <main className="dashboard-main-content">
        <DashboardHeader
          kicker="Student Portal • Protected Workspace"
          title={
            activeTab === 'overview'
              ? `Welcome back, ${account?.fullName || 'Applicant'}`
              : activeTab === 'applications'
              ? 'My Applications'
              : activeTab === 'documents'
              ? 'Document Vault'
              : activeTab === 'consultations'
              ? 'My Consultations'
              : activeTab === 'payments'
              ? 'Payments & Settlement'
              : activeTab === 'messages'
              ? 'Applicant Messages'
              : 'Help Center'
          }
          subtitle="Your personal study abroad workspace scoped strictly to your authenticated session"
          badge={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="status-pill status-pill-up">Session Active</span>
              <span className="mono-badge">{account ? account.role : 'STUDENT'}</span>
              {account?.status && <StatusBadge status={account.status} />}
            </div>
          }
          actions={
            <Link to="/student/profile" className="btn btn-secondary btn-compact">
              <span>View Profile Record</span>
              <ArrowRightIcon />
            </Link>
          }
        />

        {error && <NoticeBanner type="error">{error}</NoticeBanner>}

        {loading ? (
          <LoadingSpinner label="Authenticating and loading student workspace..." />
        ) : (
          <>
            {activeTab === 'overview' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                {/* Profile Completion Warning */}
                <div
                  className="section-card"
                  style={{
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border-hairline)',
                    padding: '20px 24px',
                    borderRadius: 'var(--radius-lg)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    flexWrap: 'wrap',
                    gap: '16px',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                    <div
                      style={{
                        width: '36px',
                        height: '36px',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: 'var(--accent-gold-soft)',
                        color: 'var(--accent-gold)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      <ClockIcon />
                    </div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '15px' }}>
                        Academic Profile Incomplete
                      </div>
                      <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                        Basic account verified ({account?.email}). Detailed academic credential forms
                        and document uploads will be enabled in upcoming milestones.
                      </div>
                    </div>
                  </div>
                  <Link to="/student/profile" className="btn btn-secondary btn-compact">
                    Check Profile Details
                  </Link>
                </div>

                {/* 2-Column Grid: Applications & Assigned Consultant */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                  {/* Application Overview */}
                  <article className="health-card">
                    <div className="health-card-top">
                      <div className="health-card-title-group">
                        <FileTextIcon className="card-icon" />
                        <div>
                          <h2 className="health-card-title">Application Overview</h2>
                          <div className="health-card-endpoint">UK Admissions Dossiers</div>
                        </div>
                      </div>
                      <span className="badge-muted">0 Applications</span>
                    </div>

                    <EmptyState
                      icon={FileTextIcon}
                      title="No Applications Submitted"
                      description="University application records will appear here once application submission workflows are activated in upcoming milestones."
                    />
                  </article>

                  {/* My Consultant */}
                  <article className="health-card">
                    <div className="health-card-top">
                      <div className="health-card-title-group">
                        <UsersIcon className="card-icon" />
                        <div>
                          <h2 className="health-card-title">Assigned Consultant</h2>
                          <div className="health-card-endpoint">Dedicated Advisory</div>
                        </div>
                      </div>
                      <span className="badge-muted">Unassigned</span>
                    </div>

                    <EmptyState
                      icon={UsersIcon}
                      title="No Consultant Assigned Yet"
                      description="You will be paired with a dedicated UK education consultant upon dossier activation. Consultants review accounts based on chosen intake and degree level."
                    />
                  </article>
                </div>

                {/* 2-Column Grid: Upcoming Tasks & Recent Activity */}
                <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 0.8fr', gap: '20px' }}>
                  {/* Upcoming Tasks */}
                  <article className="health-card">
                    <div className="health-card-top">
                      <div className="health-card-title-group">
                        <ClockIcon className="card-icon" />
                        <div>
                          <h2 className="health-card-title">Upcoming Tasks & Checklist</h2>
                          <div className="health-card-endpoint">Preparation Roadmap</div>
                        </div>
                      </div>
                      <span className="badge-gold">Onboarding</span>
                    </div>

                    <div className="dossier-list" style={{ margin: '8px 0' }}>
                      <div className="dossier-row">
                        <span className="status-dot status-dot-up" aria-hidden="true" />
                        <div>
                          <div className="dossier-row-title">Student Account Registered</div>
                          <div className="dossier-row-detail">
                            Verified on MAFIOSO admissions platform
                          </div>
                        </div>
                        <span className="badge-muted">Completed</span>
                      </div>

                      <div className="dossier-row">
                        <span className="status-dot status-dot-pending" aria-hidden="true" />
                        <div>
                          <div className="dossier-row-title">Prepare Academic Transcripts</div>
                          <div className="dossier-row-detail">
                            Gather SSC, HSC, A-Level, or Bachelor degree marksheets
                          </div>
                        </div>
                        <span className="badge-muted">Action Required</span>
                      </div>

                      <div className="dossier-row">
                        <span className="status-dot status-dot-pending" aria-hidden="true" />
                        <div>
                          <div className="dossier-row-title">English Test Planning</div>
                          <div className="dossier-row-detail">
                            Verify IELTS Academic or PTE requirements for your target course
                          </div>
                        </div>
                        <span className="badge-muted">Pending</span>
                      </div>
                    </div>
                  </article>

                  {/* Recent Activity */}
                  <article className="health-card">
                    <div className="health-card-top">
                      <div className="health-card-title-group">
                        <CalendarIcon className="card-icon" />
                        <div>
                          <h2 className="health-card-title">Account Activity</h2>
                          <div className="health-card-endpoint">Audit Log</div>
                        </div>
                      </div>
                    </div>

                    <dl className="metric-list">
                      <div className="metric-row">
                        <dt>Student ID</dt>
                        <dd className="mono-val">#{account?.id || '—'}</dd>
                      </div>
                      <div className="metric-row">
                        <dt>Registration Date</dt>
                        <dd className="mono-val">{formatDateTime(account?.createdAt)}</dd>
                      </div>
                      <div className="metric-row">
                        <dt>Last Session</dt>
                        <dd className="mono-val">{formatDateTime(account?.lastLoginAt)}</dd>
                      </div>
                      <div className="metric-row">
                        <dt>Access Authority</dt>
                        <dd className="mono-val">{account?.role || 'STUDENT'}</dd>
                      </div>
                      <div className="metric-row">
                        <dt>Access Boundary</dt>
                        <dd className="mono-val">{dashboard?.accessScope || 'SELF_ONLY'}</dd>
                      </div>
                    </dl>
                  </article>
                </div>

                {/* Quick Access Cards */}
                <div>
                  <div style={{ marginBottom: '16px' }}>
                    <h2 style={{ fontSize: '18px', fontWeight: 600, margin: '0 0 4px' }}>
                      Admissions Workspace Modules
                    </h2>
                    <p style={{ fontSize: '14px', color: 'var(--text-secondary)', margin: 0 }}>
                      Quick access to active and upcoming student portal features
                    </p>
                  </div>

                  <div className="quick-access-grid">
                    <article className="quick-card">
                      <div className="quick-card-header">
                        <div className="quick-card-icon" aria-hidden="true">
                          <UserIcon />
                        </div>
                        <span className="status-pill status-pill-up">Active</span>
                      </div>
                      <h3 className="quick-card-title">My Profile</h3>
                      <p className="quick-card-desc">
                        Review your verified personal credentials, registered email, and security
                        settings.
                      </p>
                      <Link to="/student/profile" className="text-link" style={{ fontSize: '13px' }}>
                        Open Profile Record →
                      </Link>
                    </article>

                    <article className="quick-card">
                      <div className="quick-card-header">
                        <div className="quick-card-icon" aria-hidden="true">
                          <FileTextIcon />
                        </div>
                        <span className="badge-coming-soon">Planned M4</span>
                      </div>
                      <h3 className="quick-card-title">My Applications</h3>
                      <p className="quick-card-desc">
                        Track submissions across UK universities, monitor offer letters, and CAS
                        status.
                      </p>
                      <button
                        type="button"
                        className="text-link"
                        style={{ fontSize: '13px', background: 'none', border: 'none', padding: 0, textAlign: 'left', cursor: 'pointer' }}
                        onClick={() => setActiveTab('applications')}
                      >
                        Module details →
                      </button>
                    </article>

                    <article className="quick-card">
                      <div className="quick-card-header">
                        <div className="quick-card-icon" aria-hidden="true">
                          <FolderIcon />
                        </div>
                        <span className="badge-coming-soon">Planned M4</span>
                      </div>
                      <h3 className="quick-card-title">Document Vault</h3>
                      <p className="quick-card-desc">
                        Private encrypted storage for transcripts, SOPs, English tests, and passport
                        copies.
                      </p>
                      <button
                        type="button"
                        className="text-link"
                        style={{ fontSize: '13px', background: 'none', border: 'none', padding: 0, textAlign: 'left', cursor: 'pointer' }}
                        onClick={() => setActiveTab('documents')}
                      >
                        Module details →
                      </button>
                    </article>

                    <article className="quick-card">
                      <div className="quick-card-header">
                        <div className="quick-card-icon" aria-hidden="true">
                          <PhoneCallIcon />
                        </div>
                        <span className="badge-coming-soon">Planned M4</span>
                      </div>
                      <h3 className="quick-card-title">My Consultations</h3>
                      <p className="quick-card-desc">
                        Schedule structured video or telephone advisory sessions with your assigned
                        consultant.
                      </p>
                      <button
                        type="button"
                        className="text-link"
                        style={{ fontSize: '13px', background: 'none', border: 'none', padding: 0, textAlign: 'left', cursor: 'pointer' }}
                        onClick={() => setActiveTab('consultations')}
                      >
                        Module details →
                      </button>
                    </article>

                    <article className="quick-card">
                      <div className="quick-card-header">
                        <div className="quick-card-icon" aria-hidden="true">
                          <CreditCardIcon />
                        </div>
                        <span className="badge-coming-soon">Planned M4</span>
                      </div>
                      <h3 className="quick-card-title">Payments & CAS</h3>
                      <p className="quick-card-desc">
                        Transparent fee tracking and payment settlement via SSLCOMMERZ gateway.
                      </p>
                      <button
                        type="button"
                        className="text-link"
                        style={{ fontSize: '13px', background: 'none', border: 'none', padding: 0, textAlign: 'left', cursor: 'pointer' }}
                        onClick={() => setActiveTab('payments')}
                      >
                        Module details →
                      </button>
                    </article>

                    <article className="quick-card">
                      <div className="quick-card-header">
                        <div className="quick-card-icon" aria-hidden="true">
                          <MessageSquareIcon />
                        </div>
                        <span className="badge-coming-soon">Planned M4</span>
                      </div>
                      <h3 className="quick-card-title">Messages</h3>
                      <p className="quick-card-desc">
                        Direct messaging and real-time notifications from your admissions team.
                      </p>
                      <button
                        type="button"
                        className="text-link"
                        style={{ fontSize: '13px', background: 'none', border: 'none', padding: 0, textAlign: 'left', cursor: 'pointer' }}
                        onClick={() => setActiveTab('messages')}
                      >
                        Module details →
                      </button>
                    </article>
                  </div>
                </div>
              </div>
            )}

            {activeTab !== 'overview' && activeTab !== 'help' && (
              <div className="section-card" style={{ padding: '40px 32px' }}>
                <div style={{ maxWidth: '640px', margin: '0 auto', textAlign: 'center' }}>
                  <div
                    style={{
                      width: '48px',
                      height: '48px',
                      borderRadius: 'var(--radius-md)',
                      backgroundColor: 'var(--bg-raised)',
                      color: 'var(--text-muted)',
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      marginBottom: '16px',
                    }}
                  >
                    <LockIcon />
                  </div>
                  <h2 style={{ fontSize: '20px', fontWeight: 600, margin: '0 0 8px' }}>
                    Module Scheduled for Milestone 4
                  </h2>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6, margin: '0 0 24px' }}>
                    The{' '}
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                      {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}
                    </span>{' '}
                    module is currently in development under the MAFIOSO platform roadmap. In
                    accordance with strict implementation guidelines, fake interactions and hardcoded
                    mock records are disabled until backend APIs are integrated.
                  </p>

                  <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => setActiveTab('overview')}
                    >
                      Return to Overview
                    </button>
                    <Link to="/student/profile" className="btn btn-secondary">
                      View My Profile
                    </Link>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'help' && (
              <div className="section-card" style={{ padding: '32px' }}>
                <h2 style={{ fontSize: '20px', fontWeight: 600, marginBottom: '8px' }}>
                  Student Help & Frequently Asked Inquiries
                </h2>
                <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '24px' }}>
                  Guidance on preparing your academic credentials for UK university evaluation.
                </p>

                <div className="dossier-list">
                  <div className="dossier-row">
                    <span className="dossier-index">01</span>
                    <div>
                      <div className="dossier-row-title">What documents should I prepare first?</div>
                      <div className="dossier-row-detail">
                        Your valid passport, all academic transcripts and certificates (SSC, HSC, or
                        Bachelor’s), and proof of English language proficiency (such as IELTS or PTE).
                      </div>
                    </div>
                  </div>

                  <div className="dossier-row">
                    <span className="dossier-index">02</span>
                    <div>
                      <div className="dossier-row-title">When should I begin applying?</div>
                      <div className="dossier-row-detail">
                        For the September intake, we recommend beginning preparations by November–March.
                        For the January intake, applications should be structured by June–September.
                      </div>
                    </div>
                  </div>

                  <div className="dossier-row">
                    <span className="dossier-index">03</span>
                    <div>
                      <div className="dossier-row-title">How can I contact my advisor?</div>
                      <div className="dossier-row-detail">
                        Once full consultant pairing is active, you can send inquiries directly from
                        the portal or reach out via our public inquiry channels.
                      </div>
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: '24px', display: 'flex', gap: '12px' }}>
                  <Link to="/contact" className="btn btn-secondary">
                    Visit Public Inquiries Page
                  </Link>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setActiveTab('overview')}
                  >
                    Return to Overview
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
