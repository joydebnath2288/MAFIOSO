import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightIcon,
  AwardIcon,
  BookOpenIcon,
  CheckCircleIcon,
  FileTextIcon,
  ShieldIcon,
} from '../components/Icons';

export function ScholarshipsPage() {
  useEffect(() => {
    document.title = 'Scholarship Guidance — MAFIOSO UK Education';
  }, []);

  return (
    <div className="scholarships-page">
      {/* Page Header */}
      <section className="content-section" style={{ paddingBottom: '32px' }}>
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">UK Funding Guidance</span>
            </div>
            <h1 className="section-heading">Scholarship Guidance for UK Studies</h1>
            <p className="section-description">
              Pursuing higher education in the United Kingdom is a significant investment. MAFIOSO
              helps students from Bangladesh identify eligible funding categories, evaluate
              admissions criteria, and prepare competitive scholarship applications.
            </p>
          </div>
        </div>
      </section>

      {/* Directory Notice Badge */}
      <section className="container" style={{ marginBottom: '32px' }}>
        <div className="notice-banner notice-banner-info" role="status">
          <span style={{ fontWeight: 600 }}>Directory Notice:</span> A comprehensive, searchable
          university and scholarship directory will be launched in an upcoming milestone. Current
          services focus on individualized student profile assessment and scholarship application
          preparation.
        </div>
      </section>

      {/* Types of Funding Opportunities */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-muted">Funding Classifications</span>
            </div>
            <h2 className="section-heading">Types of Funding Opportunities in the UK</h2>
            <p className="section-description">
              Financial support for international students in the UK spans several categories, each
              governed by specific academic and administrative requirements.
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-box">
              <div className="feature-index">CATEGORY 01</div>
              <h3 className="feature-title">University Merit Scholarships</h3>
              <p className="feature-desc">
                Competitive tuition fee reductions awarded by academic faculties to applicants with
                exceptional undergraduate grades, high school GPAs, or strong standardized test scores.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CATEGORY 02</div>
              <h3 className="feature-title">International Student Discounts</h3>
              <p className="feature-desc">
                Fee concessions automatically credited upon receiving an unconditional offer or
                submitting tuition fee deposits prior to designated early-bird deadlines.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CATEGORY 03</div>
              <h3 className="feature-title">Government & External Fellowships</h3>
              <p className="feature-desc">
                Flagship competitive schemes such as Commonwealth Scholarships and Chevening
                Fellowships for outstanding postgraduate candidates demonstrating public leadership.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CATEGORY 04</div>
              <h3 className="feature-title">Departmental Bursaries</h3>
              <p className="feature-desc">
                Subject-specific awards provided by university departments, often targeted toward
                STEM, sustainable energy, advanced computing, or social science research initiatives.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How MAFIOSO Can Guide Students */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Advisory Process</span>
            </div>
            <h2 className="section-heading">How MAFIOSO Guides Students</h2>
            <p className="section-description">
              Securing a competitive scholarship requires an application that highlights academic
              excellence, intellectual curiosity, and clear future potential.
            </p>
          </div>

          <div className="services-grid">
            <div className="service-card">
              <div className="service-icon-box" aria-hidden="true">
                <FileTextIcon />
              </div>
              <h3 className="service-title">Profile Benchmarking</h3>
              <p className="service-desc">
                We review your transcript scores, class rank, and research or extracurricular
                background to identify university tiers where your academic profile stands out as
                a competitive scholarship candidate.
              </p>
            </div>

            <div className="service-card">
              <div className="service-icon-box" aria-hidden="true">
                <BookOpenIcon />
              </div>
              <h3 className="service-title">Motivation Statement Refinement</h3>
              <p className="service-desc">
                Scholarship committees evaluate why an award matters to your career and community. We
                guide you in articulating your academic journey, leadership experiences, and post-study
                impact without generic cliches.
              </p>
            </div>

            <div className="service-card">
              <div className="service-icon-box" aria-hidden="true">
                <ShieldIcon />
              </div>
              <h3 className="service-title">Application Timeline Alignment</h3>
              <p className="service-desc">
                Many scholarship deadlines close 4 to 8 months prior to program commencement. We
                structure your admissions submissions early so you hold unconditional offers before
                scholarship portals close.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Eligibility Assessment Explanation */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-muted">Assessment Criteria</span>
            </div>
            <h2 className="section-heading">Understanding Scholarship Eligibility</h2>
            <p className="section-description">
              UK institutions evaluate candidates across balanced academic, personal, and professional
              criteria.
            </p>
          </div>

          <div className="services-grid services-grid-2col">
            <div className="service-card">
              <h3 className="service-title">Undergraduate (BSc) Criteria</h3>
              <ul className="metric-list" style={{ marginTop: '12px' }}>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Academic Standing:</span>
                  <span>Consistently high marks in HSC (GPA 4.5–5.0) or A-Levels (AAA–AAB).</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>English Proficiency:</span>
                  <span>Solid test results meeting or exceeding university requirements.</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Personal Statement:</span>
                  <span>Clarity regarding why the subject is your chosen life ambition.</span>
                </li>
              </ul>
            </div>

            <div className="service-card">
              <h3 className="service-title">Postgraduate (MSc) Criteria</h3>
              <ul className="metric-list" style={{ marginTop: '12px' }}>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Bachelor’s Degree:</span>
                  <span>Strong CGPA (typically 3.25+ on a 4.0 scale) or First-Class honours.</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Relevant Experience:</span>
                  <span>Published papers, research, internships, or professional tenure.</span>
                </li>
                <li className="metric-row">
                  <span style={{ fontWeight: 500 }}>Career Trajectory:</span>
                  <span>Demonstrated plan for applying UK knowledge upon graduation.</span>
                </li>
              </ul>
            </div>
          </div>

          <div style={{ marginTop: '36px', textAlign: 'center' }}>
            <Link to="/register" className="btn btn-primary">
              <span>Create Account for Profile Evaluation</span>
              <ArrowRightIcon />
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
