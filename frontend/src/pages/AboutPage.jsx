import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightIcon,
  CheckCircleIcon,
  CompassIcon,
  GraduationCapIcon,
  ShieldIcon,
  UsersIcon,
} from '../components/Icons';

export function AboutPage() {
  useEffect(() => {
    document.title = 'About Us — MAFIOSO UK Study Abroad Consultancy';
  }, []);

  return (
    <div className="about-page">
      {/* Page Header */}
      <section className="content-section" style={{ paddingBottom: '32px' }}>
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">About MAFIOSO</span>
            </div>
            <h1 className="section-heading">Dedicated UK Education Advisory</h1>
            <p className="section-description">
              MAFIOSO is an international education consultancy platform established to support
              students from Bangladesh who aspire to pursue higher education in the United Kingdom.
              We provide structured, ethical, and merit-grounded advisory services for undergraduate
              and postgraduate admissions.
            </p>
          </div>
        </div>
      </section>

      {/* Mission & Vision */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="services-grid services-grid-2col">
            <article className="service-card" style={{ padding: '36px 32px' }}>
              <div className="section-kicker">
                <span className="badge-muted">Our Purpose</span>
              </div>
              <h2 className="service-title" style={{ fontSize: '22px' }}>Our Mission</h2>
              <p className="service-desc" style={{ fontSize: '15px' }}>
                To empower Bangladeshi students with accurate, transparent, and structured admissions
                guidance, bridging academic credentials with UK institutional standards to facilitate
                credible, compliant higher education pathways.
              </p>
              <div className="service-footer">
                <span className="mono-label">Guiding Principle: Academic Honesty & Clarity</span>
              </div>
            </article>

            <article className="service-card" style={{ padding: '36px 32px' }}>
              <div className="section-kicker">
                <span className="badge-gold">Our Aspiration</span>
              </div>
              <h2 className="service-title" style={{ fontSize: '22px' }}>Our Vision</h2>
              <p className="service-desc" style={{ fontSize: '15px' }}>
                To become the most dependable and ethical education consultancy for UK higher
                education in Bangladesh, recognized for rigorous profile assessments, authentic
                application dossiers, and student-centered support.
              </p>
              <div className="service-footer">
                <span className="mono-label">Destination Focus: United Kingdom Higher Education</span>
              </div>
            </article>
          </div>
        </div>
      </section>

      {/* What We Do */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-muted">Core Capabilities</span>
            </div>
            <h2 className="section-heading">What We Do</h2>
            <p className="section-description">
              Our consultancy addresses every critical phase of the UK admissions process for BSc and
              MSc candidates.
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-box">
              <div className="feature-index">CAPABILITY 01</div>
              <h3 className="feature-title">Credential Evaluation</h3>
              <p className="feature-desc">
                Analyzing SSC, HSC, A-Level, and Bachelor transcripts to determine UK degree
                equivalencies, entry thresholds, and English requirements.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CAPABILITY 02</div>
              <h3 className="feature-title">Strategic Shortlisting</h3>
              <p className="feature-desc">
                Recommending universities and programs that align with student career aspirations,
                financial plans, and academic backgrounds.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CAPABILITY 03</div>
              <h3 className="feature-title">Application Structuring</h3>
              <p className="feature-desc">
                Reviewing Statements of Purpose, recommendation letters, and resumes to ensure
                persuasive, authentic admissions dossiers.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">CAPABILITY 04</div>
              <h3 className="feature-title">CAS & Visa Advisory</h3>
              <p className="feature-desc">
                Guiding students through financial evidence compliance, TB testing requirements,
                and UKVI visa documentation standards.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Our Approach to Student Guidance */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead section-lead-centered">
            <div className="section-kicker">
              <span className="badge-gold">Consultancy Philosophy</span>
            </div>
            <h2 className="section-heading">Our Approach to Student Guidance</h2>
            <p className="section-description">
              We operate on the conviction that every student’s academic pathway is unique. We do not
              use automated formulas or push predetermined universities.
            </p>
          </div>

          <div className="services-grid">
            <div className="service-card">
              <h3 className="service-title">1. Profile-First Analysis</h3>
              <p className="service-desc">
                Every consultation begins with a granular review of the student’s actual academic
                history, subject scores, and test proficiency. We assess strengths and address
                potential qualification gaps candidly.
              </p>
            </div>

            <div className="service-card">
              <h3 className="service-title">2. Ethical & Transparent Counsel</h3>
              <p className="service-desc">
                We believe in straightforward facts. We never promise guaranteed university admission
                or visa issuance, because those decisions rest exclusively with university admissions
                committees and UK Visas and Immigration.
              </p>
            </div>

            <div className="service-card">
              <h3 className="service-title">3. Documentation Rigor</h3>
              <p className="service-desc">
                UK universities look for genuine academic intent and clear motivation. We guide
                students in crafting authentic personal statements and assembling verified supporting
                documents that meet stringent compliance tests.
              </p>
            </div>
          </div>

          <div style={{ marginTop: '48px', textAlign: 'center' }}>
            <Link to="/services" className="btn btn-primary">
              <span>Explore Our Services</span>
              <ArrowRightIcon />
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
