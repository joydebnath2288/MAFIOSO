import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DOCUMENT_CATEGORIES, DocumentVaultPanel } from '../components/DocumentVaultPanel';
import { ArrowLeftIcon } from '../components/Icons';
import { NoticeBanner } from '../components/NoticeBanner';
import {
  fetchStudentApplications,
  fetchStudentDocuments,
  uploadStudentDocument,
} from '../services/studentService';

export function StudentDocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [applications, setApplications] = useState([]);
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [reviewFilter, setReviewFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDocuments = useCallback(async () => {
    setLoading(true);
    setError('');
    const [docsRes, appsRes] = await Promise.all([
      fetchStudentDocuments({ category: categoryFilter, reviewStatus: reviewFilter }),
      fetchStudentApplications({ page: 1, pageSize: 50 }),
    ]);

    if (docsRes.ok && docsRes.data && docsRes.data.success && docsRes.data.data) {
      setDocuments(docsRes.data.data.items || []);
    } else {
      setError(docsRes.error || 'Unable to load documents');
    }

    if (appsRes.ok && appsRes.data && appsRes.data.success && appsRes.data.data) {
      setApplications(appsRes.data.data.items || []);
    }
    setLoading(false);
  }, [categoryFilter, reviewFilter]);

  useEffect(() => {
    document.title = 'My Documents — MAFIOSO';
    loadDocuments();
  }, [loadDocuments]);

  async function handleUpload(payload) {
    const res = await uploadStudentDocument(payload);
    if (res.ok && res.data && res.data.success) {
      await loadDocuments();
    }
    return res;
  }

  return (
    <div className="container workspace-page">
      <div className="detail-breadcrumb">
        <Link to="/student/dashboard" className="btn btn-secondary btn-compact">
          <ArrowLeftIcon />
          <span>Back to Dashboard</span>
        </Link>
      </div>

      <header className="workspace-header">
        <div>
          <div className="mono-label">Student Portal • Document Vault</div>
          <h1 className="page-title">My Documents</h1>
          <p className="page-subtitle">
            Upload, verify review status, and download your supporting study abroad files
          </p>
        </div>
      </header>

      {error && <NoticeBanner type="error">{error}</NoticeBanner>}

      <section className="section-card">
        <div className="section-card-header">
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
            <select
              aria-label="Filter by category"
              className="form-input filter-select"
              style={{ width: '240px' }}
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
            >
              <option value="ALL">All categories</option>
              {DOCUMENT_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>

            <select
              aria-label="Filter by review status"
              className="form-input filter-select"
              style={{ width: '200px' }}
              value={reviewFilter}
              onChange={(e) => setReviewFilter(e.target.value)}
            >
              <option value="ALL">All review statuses</option>
              <option value="PENDING">Pending</option>
              <option value="UNDER_REVIEW">Under review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
          <span className="mono-label">{documents.length} files</span>
        </div>

        <DocumentVaultPanel
          documents={documents}
          canUpload={true}
          canReview={false}
          applications={applications}
          onUpload={handleUpload}
          loading={loading}
        />
      </section>
    </div>
  );
}
