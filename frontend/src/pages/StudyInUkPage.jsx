import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightIcon,
  BanknoteIcon,
  BookOpenIcon,
  BuildingIcon,
  CalendarIcon,
  CheckCircleIcon,
  CompassIcon,
  GraduationCapIcon,
  MapPinIcon,
} from '../components/Icons';

export function StudyInUkPage() {
  useEffect(() => {
    document.title = 'Study in the UK Guide — MAFIOSO';
  }, []);

  return (
    <div className="study-in-uk-page">
      {/* Page Header */}
      <section className="content-section" style={{ paddingBottom: '32px' }}>
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Destination UK</span>
            </div>
            <h1 className="section-heading">Studying in the United Kingdom</h1>
            <p className="section-description">
              A comprehensive introduction to British higher education for students from Bangladesh.
              Learn about undergraduate (BSc) and postgraduate (MSc) structures, intake seasons,
              institutional types, and admissions preparation.
            </p>
          </div>
        </div>
      </section>

      {/* BSc vs MSc */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="services-grid services-grid-2col">
            <article className="service-card" style={{ padding: '32px' }}>
              <div className="service-icon-box" aria-hidden="true">
                <GraduationCapIcon />
              </div>
              <h2 className="service-title" style={{ fontSize: '20px' }}>
                Undergraduate Study (BSc / BA / BEng)
              </h2>
              <p className="service-desc">
                Undergraduate degrees in England, Wales, and Northern Ireland are focused 3-year
                programs (4 years in Scotland or with a placement year). Students concentrate on
                their chosen discipline immediately upon enrollment without general education
                requirements.
              </p>
              <ul className="metric-list" style={{ marginTop: '16px', borderTop: '1px solid var(--border-hairline)', paddingTop: '16px' }}>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Duration:</span>
                  <span className="mono-val">3 Years (4 in Scotland)</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Entry from Bangladesh:</span>
                  <span>A-Levels, or HSC + International Foundation Year</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Application Route:</span>
                  <span className="mono-val">UCAS / Direct Institution Portal</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Primary Intake:</span>
                  <span className="mono-val">September / October</span>
                </li>
              </ul>
            </article>

            <article className="service-card" style={{ padding: '32px' }}>
              <div className="service-icon-box" aria-hidden="true">
                <BookOpenIcon />
              </div>
              <h2 className="service-title" style={{ fontSize: '20px' }}>
                Postgraduate Study (MSc / MA / MBA)
              </h2>
              <p className="service-desc">
                Taught master’s programs in the UK are typically completed in 1 intensive calendar
                year (12 months), combining 8 to 9 months of coursework with a summer dissertation or
                applied industry project. This enables students to minimize living expenses and enter
                their professional career sooner.
              </p>
              <ul className="metric-list" style={{ marginTop: '16px', borderTop: '1px solid var(--border-hairline)', paddingTop: '16px' }}>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Duration:</span>
                  <span className="mono-val">1 Year (12 Months full-time)</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Entry from Bangladesh:</span>
                  <span>4-Year Bachelor’s degree from an accredited university</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Application Route:</span>
                  <span className="mono-val">Direct University Application</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Key Intakes:</span>
                  <span className="mono-val">September & January</span>
                </li>
              </ul>
            </article>
          </div>
        </div>
      </section>

      {/* University Selection & Considerations */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-muted">Institutional Landscape</span>
            </div>
            <h2 className="section-heading">Selecting the Right UK University</h2>
            <p className="section-description">
              The UK higher education sector features diverse institutions, each with distinct
              pedagogical strengths, industry networks, and geographic settings.
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-box">
              <div className="feature-index">RESEARCH EXCELLENCE</div>
              <h3 className="feature-title">Russell Group Universities</h3>
              <p className="feature-desc">
                A prestigious association of 24 research-intensive universities known for world-class
                laboratories, high academic thresholds, and global reputation.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CAREER FOCUS</div>
              <h3 className="feature-title">Modern & Applied Universities</h3>
              <p className="feature-desc">
                Institutions focused on industry partnerships, employability, practical coursework,
                and state-of-the-art facilities tailored to contemporary job markets.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">LOCATION FACTORS</div>
              <h3 className="feature-title">London vs Regional Campuses</h3>
              <p className="feature-desc">
                London offers unparalleled corporate headquarters and cultural hubs, while regional
                cities (Manchester, Birmingham, Leeds, Glasgow) feature lower living costs and vibrant
                student communities.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">TEACHING QUALITY</div>
              <h3 className="feature-title">Subject Specializations</h3>
              <p className="feature-desc">
                University league tables vary significantly by subject; a university may lead globally
                in civil engineering or artificial intelligence while maintaining an accessible overall
                admissions tariff.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Financial Preparation Guidance */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Financial Compliance</span>
            </div>
            <h2 className="section-heading">Financial Preparation & UKVI Maintenance</h2>
            <p className="section-description">
              Understanding tuition structures and student visa financial evidence is critical. The
              UK Visas and Immigration (UKVI) service enforces strict documentation guidelines.
            </p>
          </div>

          <div className="services-grid">
            <div className="service-card">
              <h3 className="service-title">Tuition Fee Deposits & CAS</h3>
              <p className="service-desc">
                Upon meeting conditional offer criteria, UK institutions require an initial tuition
                deposit (typically £2,000 to £5,000) to confirm your seat and generate your official
                Confirmation of Acceptance for Studies (CAS) reference number.
              </p>
            </div>

            <div className="service-card">
              <h3 className="service-title">Living Expense Calculations</h3>
              <p className="service-desc">
                UKVI stipulates mandatory living costs per month for up to 9 months. The threshold is
                higher for institutions located within Inner and Outer London compared to institutions
                located across the rest of the UK.
              </p>
            </div>

            <div className="service-card">
              <h3 className="service-title">The 28-Day Holding Rule</h3>
              <p className="service-desc">
                Required maintenance funds must remain in an eligible bank account continuously for at
                least 28 consecutive days without dropping below the required balance. The closing
                bank statement date must be within 31 days of your visa application date.
              </p>
            </div>
          </div>

          <div style={{ marginTop: '24px' }}>
            <p className="mono-label" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              * Official tuition amounts and UKVI financial thresholds are set by institutions and
              the UK Home Office. MAFIOSO provides compliance verification and calculation guidance.
            </p>
          </div>
        </div>
      </section>

      {/* Student Journey with MAFIOSO */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead section-lead-centered">
            <div className="section-kicker">
              <span className="badge-muted">Your Pathway</span>
            </div>
            <h2 className="section-heading">Your Student Journey with MAFIOSO</h2>
            <p className="section-description">
              How our consultancy accompanies you from initial inquiry to landing in the UK.
            </p>
          </div>

          <div className="pathway-grid">
            <div className="pathway-step-card">
              <span className="pathway-step-num">1</span>
              <h3 className="pathway-step-title">Profile Review</h3>
              <p className="pathway-step-desc">
                Evaluate transcripts, English qualifications, and degree aspirations.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">2</span>
              <h3 className="pathway-step-title">Course Selection</h3>
              <p className="pathway-step-desc">
                Shortlist accredited UK institutions matching your academic profile.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">3</span>
              <h3 className="pathway-step-title">Application Dossier</h3>
              <p className="pathway-step-desc">
                Draft compliant SOPs, gather academic recommendations, and apply.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">4</span>
              <h3 className="pathway-step-title">Offer & CAS</h3>
              <p className="pathway-step-desc">
                Clear conditions, pay tuition deposit, and obtain CAS statement.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">5</span>
              <h3 className="pathway-step-title">Visa & Pre-Departure</h3>
              <p className="pathway-step-desc">
                Submit UKVI visa application, complete TB tests, and prepare for travel.
              </p>
            </div>
          </div>

          <div style={{ marginTop: '48px', textAlign: 'center' }}>
            <Link to="/register" className="btn btn-primary">
              <span>Start Your UK Admissions Plan</span>
              <ArrowRightIcon />
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
