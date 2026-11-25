import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightIcon,
  AwardIcon,
  BanknoteIcon,
  BookOpenIcon,
  BriefcaseIcon,
  CompassIcon,
  FileTextIcon,
  GraduationCapIcon,
  HomeIcon,
  PassportIcon,
  PhoneCallIcon,
} from '../components/Icons';

const SERVICES_CATALOG = [
  {
    id: 'academic-evaluation',
    icon: GraduationCapIcon,
    title: 'Academic Profile Evaluation',
    description:
      'Detailed review of your secondary (SSC, HSC, O/A-Levels) or undergraduate academic records. We evaluate subject scores, GPA equivalencies, and English language credentials against UK university admission benchmarks.',
    cta: 'Request Evaluation',
    link: '/contact',
  },
  {
    id: 'university-selection',
    icon: CompassIcon,
    title: 'University Selection',
    description:
      'Personalized university matching based on your target discipline, academic profile, location preference, and tuition budget across leading institutions throughout England, Scotland, Wales, and Northern Ireland.',
    cta: 'Shortlist Universities',
    link: '/contact',
  },
  {
    id: 'admission-guidance',
    icon: BookOpenIcon,
    title: 'Admission Guidance',
    description:
      'Comprehensive support for direct university submissions and UCAS undergraduate applications. We guide you through application timelines, intake deadlines, conditional offer terms, and final CAS issuance requirements.',
    cta: 'Start Application',
    link: '/register',
  },
  {
    id: 'scholarship-guidance',
    icon: AwardIcon,
    title: 'Scholarship Guidance',
    description:
      'Identification of relevant merit awards, international tuition fee discounts, and competitive government scholarships. We provide guidance on scholarship eligibility criteria and motivation statement refinement.',
    cta: 'Explore Scholarships',
    link: '/scholarships',
  },
  {
    id: 'document-preparation',
    icon: FileTextIcon,
    title: 'Document Preparation Guidance',
    description:
      'Structured review of academic dossiers including Statements of Purpose (SOP), letters of recommendation (LOR), academic resumes/CVs, and official translation standards required by UK university admissions boards.',
    cta: 'Review Documents',
    link: '/contact',
  },
  {
    id: 'visa-assistance',
    icon: PassportIcon,
    title: 'Visa Application Assistance',
    description:
      'Guidance on the UK Student Visa (formerly Tier 4) process: preparing required documentation, booking biometric appointments, obtaining approved tuberculosis screening, and understanding UKVI compliance regulations.',
    cta: 'Visa Guidance',
    link: '/contact',
  },
  {
    id: 'financial-documentation',
    icon: BanknoteIcon,
    title: 'Financial Documentation Guidance',
    description:
      'Advisory on UKVI financial maintenance requirements, calculating required living costs for London and outside London, understanding acceptable sponsor relationships, and adhering strictly to the consecutive 28-day holding rule.',
    cta: 'Financial Advisory',
    link: '/contact',
  },
  {
    id: 'accommodation-assistance',
    icon: HomeIcon,
    title: 'Accommodation Assistance',
    description:
      'Guidance on exploring university-managed halls of residence and private student accommodation (PBSA) across the UK, understanding tenancy terms, deposit requirements, and booking timelines before arrival.',
    cta: 'Inquire Accommodation',
    link: '/contact',
  },
  {
    id: 'pre-departure-guidance',
    icon: BriefcaseIcon,
    title: 'Pre-departure Guidance',
    description:
      'Essential briefings covering flight arrangements, UK airport arrivals, BRP collection or eVisa digital status, opening a UK bank account, registering with the National Health Service (NHS), and adapting to student life.',
    cta: 'Pre-departure Checklist',
    link: '/contact',
  },
  {
    id: 'consultation',
    icon: PhoneCallIcon,
    title: 'Professional Consultation',
    description:
      'One-on-one consultation sessions with our education advisors to address specific questions regarding course transitions, gap years, family sponsorship, intake selection, or career pathway alignment in the UK.',
    cta: 'Book Consultation',
    link: '/contact',
  },
];

export function ServicesPage() {
  useEffect(() => {
    document.title = 'Consultancy Services — MAFIOSO UK Education';
  }, []);

  return (
    <div className="services-page">
      {/* Page Header */}
      <section className="content-section" style={{ paddingBottom: '32px' }}>
        <div className="container">
          <div className="section-lead">
            <div className="section-kicker">
              <span className="badge-gold">Our Services</span>
            </div>
            <h1 className="section-heading">Professional UK Admissions Services</h1>
            <p className="section-description">
              MAFIOSO provides end-to-end consultancy for Bangladeshi applicants pursuing
              undergraduate and postgraduate degrees in the United Kingdom. Explore our structured
              service suite below.
            </p>
          </div>
        </div>
      </section>

      {/* Services Grid */}
      <section className="content-section content-section-alt">
        <div className="container">
          <div className="services-grid">
            {SERVICES_CATALOG.map((service) => {
              const Icon = service.icon;
              return (
                <article key={service.id} className="service-card" id={service.id}>
                  <div className="service-icon-box" aria-hidden="true">
                    <Icon />
                  </div>
                  <h2 className="service-title">{service.title}</h2>
                  <p className="service-desc">{service.description}</p>
                  <div className="service-footer">
                    <Link
                      to={service.link}
                      className="btn btn-secondary btn-compact"
                      style={{ width: '100%', justifyContent: 'center' }}
                    >
                      <span>{service.cta}</span>
                      <ArrowRightIcon />
                    </Link>
                  </div>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      {/* Process Commitment */}
      <section className="content-section">
        <div className="container">
          <div className="cta-banner">
            <div className="container cta-banner-inner">
              <div>
                <h2 className="cta-banner-title">Need Tailored Guidance on Your Options?</h2>
                <p className="cta-banner-subtitle">
                  Register for a student account to evaluate your academic background and begin your
                  application plan with our consultancy team.
                </p>
              </div>
              <div className="cta-banner-actions">
                <Link to="/register" className="btn btn-white">
                  <span>Register Student Account</span>
                  <ArrowRightIcon />
                </Link>
                <Link to="/contact" className="btn btn-ghost-white">
                  <span>Send an Inquiry</span>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
