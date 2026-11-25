import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaqAccordion } from '../components/FaqAccordion';
import {
  ArrowRightIcon,
  AwardIcon,
  BookOpenIcon,
  BriefcaseIcon,
  CheckCircleIcon,
  CompassIcon,
  FileTextIcon,
  GraduationCapIcon,
  PassportIcon,
  ShieldIcon,
  UsersIcon,
} from '../components/Icons';

const PATHWAY_STAGES = [
  {
    code: '01',
    title: 'Academic Profile Evaluation',
    detail: 'HSC, A-Level, and Bachelor transcript assessment for UK university entry benchmarks',
    meta: 'Initial Step',
  },
  {
    code: '02',
    title: 'UK University Shortlisting',
    detail: 'Tailored course selection matching BSc and MSc entry requirements across UK institutions',
    meta: 'Curated Options',
  },
  {
    code: '03',
    title: 'Admission & Document Preparation',
    detail: 'Statement of Purpose (SOP), academic reference, and credential verification guidance',
    meta: 'Application Dossier',
  },
  {
    code: '04',
    title: 'Financial & CAS Readiness',
    detail: 'Maintenance fund structuring, 28-day rule compliance, and CAS issuance guidance',
    meta: 'CAS & Visa Prep',
  },
  {
    code: '05',
    title: 'Pre-Departure Briefing',
    detail: 'Accommodation guidance, UK travel checklist, and student orientation before departure',
    meta: 'UK Transition',
  },
];

const OVERVIEW_SERVICES = [
  {
    icon: GraduationCapIcon,
    title: 'Academic Profile Evaluation',
    description:
      'Thorough evaluation of Bangladeshi academic credentials (HSC, A-Levels, BSc) against UK university admission criteria and grade equivalencies.',
    link: '/services',
  },
  {
    icon: CompassIcon,
    title: 'University & Course Selection',
    description:
      'Personalized shortlisting across Russell Group and modern UK universities based on your academic background, career goals, and budget.',
    link: '/services',
  },
  {
    icon: BookOpenIcon,
    title: 'Admission Application Guidance',
    description:
      'Step-by-step guidance on direct university submissions, UCAS undergraduate applications, and postgraduate admissions dossiers.',
    link: '/services',
  },
  {
    icon: AwardIcon,
    title: 'Scholarship Guidance',
    description:
      'Identification of eligible merit-based tuition discounts, university awards, and government fellowships, with application essay support.',
    link: '/scholarships',
  },
  {
    icon: FileTextIcon,
    title: 'Document Preparation Guidance',
    description:
      'Structured reviews for Statements of Purpose (SOP), letters of recommendation, CVs, and certified academic translations.',
    link: '/services',
  },
  {
    icon: PassportIcon,
    title: 'Visa Application Assistance',
    description:
      'Comprehensive guidance on UK Student Visa requirements, financial maintenance calculations, tuberculosis testing, and CAS compliance.',
    link: '/services',
  },
];

const VALUE_PROPOSITIONS = [
  {
    index: '01',
    title: 'UK Corridor Specialization',
    desc: 'Dedicated exclusively to higher education pathways in England, Scotland, Wales, and Northern Ireland for students from Bangladesh.',
  },
  {
    index: '02',
    title: 'Profile-First Assessment',
    desc: 'We review your transcripts, test scores, and career ambitions before recommending realistic, achievable university shortlists.',
  },
  {
    index: '03',
    title: 'Transparent Advisory',
    desc: 'Honest appraisal of university entry criteria, tuition costs, and living expense requirements with no unrealistic guarantees.',
  },
  {
    index: '04',
    title: 'Structured Documentation',
    desc: 'Clear guidance on academic transcripts, Statements of Purpose, financial proofs, and Confirmation of Acceptance for Studies (CAS).',
  },
];

const FAQ_ITEMS = [
  {
    question: 'Can I apply for UK universities with Bangladeshi HSC or A-Level qualifications?',
    answer:
      'Yes. Students with A-Levels can often apply directly for undergraduate (BSc) degree programs. Students completing the Bangladeshi Higher Secondary Certificate (HSC) typically enter via an International Foundation Year or direct entry depending on their GPA and chosen university subject requirements.',
  },
  {
    question: 'What are the main intake seasons for UK universities?',
    answer:
      'The primary intake for almost all UK undergraduate and postgraduate degrees is September / October (Autumn). A secondary intake with a broad selection of programs runs in January / February (Winter / Spring). We recommend beginning application preparation at least 6 to 9 months before your intended intake.',
  },
  {
    question: 'Do I need IELTS to apply for BSc or MSc courses in the UK?',
    answer:
      'Most UK universities require demonstrated English language proficiency, commonly through IELTS Academic, PTE Academic, or approved equivalents. Some institutions may offer English test waivers for applicants with high English scores in secondary curricula or Medium of Instruction (MOI) certificates from accredited universities, evaluated on an individual basis.',
  },
  {
    question: 'What financial evidence is needed for the UK Student Visa?',
    answer:
      'Under UK Visas and Immigration (UKVI) guidelines, applicants must demonstrate proof of funds covering remaining course tuition plus living expenses for up to 9 months (higher for London institutions than outside London). These funds must be maintained continuously for a consecutive 28-day period in an approved financial institution before visa submission.',
  },
  {
    question: 'Does MAFIOSO guarantee admission or visa approval?',
    answer:
      'No. MAFIOSO provides professional educational advisory, profile evaluation, and document guidance. Final admission decisions are made strictly by UK university admissions boards, and student visas are granted exclusively by UK Visas and Immigration (UKVI). We ensure your application is accurate, credible, and thorough.',
  },
];

export function LandingPage() {
  useEffect(() => {
    document.title = 'MAFIOSO — UK Study Abroad Consultancy for Bangladesh';
  }, []);

  return (
    <div className="landing-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="container hero-grid">
          <div className="hero-copy">
            <div className="hero-kicker">
              <span className="status-dot status-dot-accent" aria-hidden="true" />
              <span className="mono-label">Bangladesh to United Kingdom Higher Education</span>
            </div>

            <h1 className="hero-title">Your UK Study Journey Starts Here.</h1>

            <p className="hero-subtitle">
              MAFIOSO is an international education consultancy platform built for students in
              Bangladesh seeking undergraduate (BSc) and postgraduate (MSc) degrees in the United
              Kingdom. Receive structured guidance from academic evaluation through visa
              readiness.
            </p>

            <div className="hero-actions">
              <Link to="/register" className="btn btn-primary">
                <span>Start Your Journey</span>
                <ArrowRightIcon />
              </Link>
              <Link to="/services" className="btn btn-secondary">
                <span>Explore Our Services</span>
              </Link>
            </div>
          </div>

          <aside className="dossier-card" aria-label="Structured UK Admissions Pathway">
            <div className="dossier-header">
              <div>
                <div className="dossier-title">Admissions Pathway Framework</div>
                <div className="dossier-subtitle">Target Corridor: Bangladesh → United Kingdom</div>
              </div>
              <span className="badge-gold">BSc & MSc</span>
            </div>

            <div className="dossier-list">
              {PATHWAY_STAGES.map((stage) => (
                <div key={stage.code} className="dossier-row">
                  <span className="dossier-index">{stage.code}</span>
                  <div className="dossier-row-body">
                    <div className="dossier-row-title">{stage.title}</div>
                    <div className="dossier-row-detail">{stage.detail}</div>
                  </div>
                  <span className="dossier-stage-meta">{stage.meta}</span>
                </div>
              ))}
            </div>

            <div className="dossier-footer">
              <span>Structured Consultancy Workflow</span>
              <span className="mono-label">Transparent • Academic • Ethical</span>
            </div>
          </aside>
        </div>
      </section>

      {/* Introduction to MAFIOSO */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead section-lead-centered">
            <div className="section-kicker">
              <span className="badge-muted">About MAFIOSO</span>
            </div>
            <h2 className="section-heading">Dedicated UK Educational Advisory</h2>
            <p className="section-description">
              Navigating university admissions in the United Kingdom requires rigorous academic
              preparation, strategic institution selection, and precise documentation. MAFIOSO
              bridges the requirements between Bangladeshi educational systems and UK university
              standards, providing clear, ethical counsel at every milestone.
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-box">
              <div className="feature-index">BSc PATHWAYS</div>
              <h3 className="feature-title">Undergraduate Admissions</h3>
              <p className="feature-desc">
                Guidance on A-Level and HSC equivalencies, UCAS application deadlines, and foundation
                programs for direct undergraduate progression.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">MSc PATHWAYS</div>
              <h3 className="feature-title">Postgraduate Admissions</h3>
              <p className="feature-desc">
                Specialized matching for 1-year master’s degrees across management, STEM, computing,
                humanities, and healthcare disciplines.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">DOCUMENT RIGOR</div>
              <h3 className="feature-title">Academic Credentials</h3>
              <p className="feature-desc">
                Structuring Statements of Purpose (SOP), academic reference letters, and transcript
                evaluations that meet UK institutional guidelines.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">COMPLIANCE</div>
              <h3 className="feature-title">Visa & CAS Readiness</h3>
              <p className="feature-desc">
                Comprehensive advisory on CAS issuance, 28-day financial proof requirements, and UKVI
                student immigration regulations.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Core Services Overview */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Comprehensive Services</span>
            </div>
            <h2 className="section-heading">Our Consultancy Services</h2>
            <p className="section-description">
              From your initial profile assessment to pre-departure preparations, explore the
              consultancy solutions designed to guide your UK academic progression.
            </p>
          </div>

          <div className="services-grid">
            {OVERVIEW_SERVICES.map((service) => {
              const Icon = service.icon;
              return (
                <article key={service.title} className="service-card">
                  <div className="service-icon-box" aria-hidden="true">
                    <Icon />
                  </div>
                  <h3 className="service-title">{service.title}</h3>
                  <p className="service-desc">{service.description}</p>
                  <div className="service-footer">
                    <Link to={service.link} className="text-link" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                      <span>Learn more</span>
                      <ArrowRightIcon />
                    </Link>
                  </div>
                </article>
              );
            })}
          </div>

          <div style={{ textAlign: 'center', marginTop: '36px' }}>
            <Link to="/services" className="btn btn-secondary">
              <span>View All 10 Consultancy Services</span>
              <ArrowRightIcon />
            </Link>
          </div>
        </div>
      </section>

      {/* Why Choose MAFIOSO */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead section-lead-centered">
            <div className="section-kicker">
              <span className="badge-muted">Core Values</span>
            </div>
            <h2 className="section-heading">Why Choose MAFIOSO?</h2>
            <p className="section-description">
              We focus on clarity, academic integrity, and realistic student placement. Here is what
              sets our consultancy approach apart.
            </p>
          </div>

          <div className="features-grid">
            {VALUE_PROPOSITIONS.map((prop) => (
              <div key={prop.index} className="feature-box">
                <div className="feature-index">PILLAR {prop.index}</div>
                <h3 className="feature-title">{prop.title}</h3>
                <p className="feature-desc">{prop.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Step-by-Step Pathway</span>
            </div>
            <h2 className="section-heading">How It Works</h2>
            <p className="section-description">
              A structured five-step pathway guiding Bangladeshi applicants from initial qualification
              assessment to UK arrival.
            </p>
          </div>

          <div className="pathway-grid">
            <div className="pathway-step-card">
              <span className="pathway-step-num">1</span>
              <h3 className="pathway-step-title">Profile Evaluation</h3>
              <p className="pathway-step-desc">
                Review your academic background, GPA, English proficiency, and intended intake
                to establish candidate eligibility.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">2</span>
              <h3 className="pathway-step-title">Shortlisting</h3>
              <p className="pathway-step-desc">
                Select suitable UK universities and courses balancing academic ambition, budget,
                and career prospects.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">3</span>
              <h3 className="pathway-step-title">Application Submission</h3>
              <p className="pathway-step-desc">
                Prepare strong SOPs, academic references, and required transcripts for official
                admissions submission.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">4</span>
              <h3 className="pathway-step-title">CAS & Visa Guidance</h3>
              <p className="pathway-step-desc">
                Fulfill university offer conditions, verify CAS statements, and compile compliant
                financial documentation.
              </p>
            </div>

            <div className="pathway-step-card">
              <span className="pathway-step-num">5</span>
              <h3 className="pathway-step-title">Pre-Departure</h3>
              <p className="pathway-step-desc">
                Receive accommodation advice, travel checklist, and orientation on living and studying
                in the United Kingdom.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Study in the UK Section */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-muted">Destination UK</span>
            </div>
            <h2 className="section-heading">Study in the United Kingdom</h2>
            <p className="section-description">
              The UK offers globally recognized degrees, accelerated program durations, and diverse
              cultural environments for international students.
            </p>
          </div>

          <div className="services-grid services-grid-2col">
            <div className="service-card">
              <h3 className="service-title">Undergraduate Study (BSc)</h3>
              <p className="service-desc">
                Undergraduate degrees in England, Wales, and Northern Ireland typically span 3 years
                (4 years in Scotland), offering focused subject curricula from day one. Bangladeshi
                students with A-Levels can apply directly; those with HSC can access integrated
                foundation pathways.
              </p>
              <div className="service-footer">
                <span className="mono-label">Primary Intake: September</span>
                <Link to="/study-in-uk" className="text-link">Explore BSc guidance →</Link>
              </div>
            </div>

            <div className="service-card">
              <h3 className="service-title">Postgraduate Study (MSc)</h3>
              <p className="service-desc">
                Most UK master’s degrees are intensive 1-year programs, allowing graduates to earn a
                prestigious qualification and re-enter the workforce faster. Options include taught
                master’s (MSc / MA / MBA) and research-oriented degrees.
              </p>
              <div className="service-footer">
                <span className="mono-label">Intakes: September & January</span>
                <Link to="/study-in-uk" className="text-link">Explore MSc guidance →</Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Scholarship Guidance Section */}
      <section className="content-section">
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Funding Opportunities</span>
            </div>
            <h2 className="section-heading">Scholarship Guidance</h2>
            <p className="section-description">
              Securing scholarships for UK studies requires early planning, strong academic merit, and
              carefully articulated application essays. Learn how MAFIOSO supports your funding search.
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-box">
              <div className="feature-index">TYPE 01</div>
              <h3 className="feature-title">Merit-Based Awards</h3>
              <p className="feature-desc">
                Tuition fee reductions awarded by individual universities based on outstanding
                academic achievements in prior studies.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">TYPE 02</div>
              <h3 className="feature-title">International Discounts</h3>
              <p className="feature-desc">
                Automatic or early-bird fee discounts offered by UK institutions to encourage diverse
                international student enrollments.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">TYPE 03</div>
              <h3 className="feature-title">Government Scholarships</h3>
              <p className="feature-desc">
                Prestigious competitive schemes such as Commonwealth and Chevening scholarships for
                exceptional postgraduate applicants.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-index">MAFIOSO SUPPORT</div>
              <h3 className="feature-title">Application Essay Review</h3>
              <p className="feature-desc">
                Guidance on structuring compelling scholarship motivation letters and highlighting
                leadership and academic potential.
              </p>
            </div>
          </div>

          <div style={{ marginTop: '28px', textAlign: 'center' }}>
            <Link to="/scholarships" className="btn btn-secondary">
              <span>Read Scholarship Guidance Overview</span>
              <ArrowRightIcon />
            </Link>
          </div>
        </div>
      </section>

      {/* Frequently Asked Questions */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="section-lead section-lead-centered">
            <div className="section-kicker">
              <span className="badge-muted">Frequently Asked Questions</span>
            </div>
            <h2 className="section-heading">Common Student Inquiries</h2>
            <p className="section-description">
              Answers to key questions Bangladeshi applicants ask when planning their UK education.
            </p>
          </div>

          <FaqAccordion items={FAQ_ITEMS} />
        </div>
      </section>

      {/* Conversion CTA Banner */}
      <section className="container">
        <div className="cta-banner">
          <div className="container cta-banner-inner">
            <div>
              <h2 className="cta-banner-title">Ready to Begin Your UK Application?</h2>
              <p className="cta-banner-subtitle">
                Create your student account today to explore services, organize your profile, and
                connect with our consultancy team.
              </p>
            </div>
            <div className="cta-banner-actions">
              <Link to="/register" className="btn btn-white">
                <span>Create Student Account</span>
                <ArrowRightIcon />
              </Link>
              <Link to="/contact" className="btn btn-ghost-white">
                <span>Inquire Online</span>
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
