import { useState } from 'react';
import { DownloadIcon, FolderIcon, UploadIcon } from './Icons';
import { StatusBadge } from './StatusBadge';
import { formatDateTime } from '../utils/validation';

export const DOCUMENT_CATEGORIES = [
  { value: 'PASSPORT', label: 'Passport' },
  { value: 'ACADEMIC_CERTIFICATE', label: 'Academic certificate' },
  { value: 'ACADEMIC_TRANSCRIPT', label: 'Academic transcript' },
  { value: 'ENGLISH_CERTIFICATE', label: 'English certificate (IELTS / PTE / TOEFL)' },
  { value: 'CV', label: 'Curriculum Vitae (CV)' },
  { value: 'FINANCIAL_DOCUMENT', label: 'Financial document' },
  { value: 'OTHER', label: 'Other supporting document' },
];

function formatFileSize(bytes) {
  if (bytes == null) return '—';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function formatCategory(cat) {
  const found = DOCUMENT_CATEGORIES.find((c) => c.value === cat);
  return found ? found.label : (cat || 'OTHER').replace(/_/g, ' ');
}

export function DocumentVaultPanel({
  documents = [],
  canUpload = false,
  canReview = false,
  applications = [],
  fixedApplicationId = null,
  onUpload,
  onReview,
  loading = false,
}) {
  const [category, setCategory] = useState('PASSPORT');
  const [selectedAppId, setSelectedAppId] = useState(fixedApplicationId ? String(fixedApplicationId) : '');
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState('');
  const [fileInputKey, setFileInputKey] = useState(0);

  const [reviewingDoc, setReviewingDoc] = useState(null);
  const [reviewStatus, setReviewStatus] = useState('APPROVED');
  const [reviewNote, setReviewNote] = useState('');
  const [reviewBusy, setReviewBusy] = useState(false);
  const [reviewError, setReviewError] = useState('');

  async function handleUploadSubmit(event) {
    event.preventDefault();
    if (!selectedFile || uploading || !onUpload) {
      if (!selectedFile) {
        setUploadError('Please select a PDF, PNG, JPG, or DOCX file.');
      }
      return;
    }
    setUploading(true);
    setUploadError('');
    setUploadSuccess('');
    try {
      const targetAppId = fixedApplicationId || (selectedAppId ? Number(selectedAppId) : null);
      const res = await onUpload({
        file: selectedFile,
        category,
        applicationId: targetAppId,
      });
      if (res.ok && res.data && res.data.success) {
        setUploadSuccess(`Uploaded ${selectedFile.name}`);
        setSelectedFile(null);
        setFileInputKey((prev) => prev + 1);
      } else {
        const fieldMsg = res.fieldErrors && (res.fieldErrors.file || res.fieldErrors.category);
        setUploadError(fieldMsg || res.error || 'Unable to upload document');
      }
    } finally {
      setUploading(false);
    }
  }

  function openReviewModal(doc) {
    setReviewingDoc(doc);
    setReviewStatus(doc.reviewStatus === 'PENDING' ? 'APPROVED' : doc.reviewStatus);
    setReviewNote(doc.reviewNote || '');
    setReviewError('');
  }

  async function handleReviewSubmit(event) {
    event.preventDefault();
    if (!reviewingDoc || reviewBusy || !onReview) {
      return;
    }
    if (reviewStatus === 'REJECTED' && !reviewNote.trim()) {
      setReviewError('Please enter a review note explaining what needs correction.');
      return;
    }
    setReviewBusy(true);
    setReviewError('');
    try {
      const res = await onReview(reviewingDoc.id, {
        reviewStatus,
        reviewNote: reviewNote.trim(),
      });
      if (res.ok && res.data && res.data.success) {
        setReviewingDoc(null);
      } else {
        const fieldMsg = res.fieldErrors && (res.fieldErrors.reviewNote || res.fieldErrors.reviewStatus);
        setReviewError(fieldMsg || res.error || 'Unable to update document review');
      }
    } finally {
      setReviewBusy(false);
    }
  }

  return (
    <div className="document-vault-panel">
      {canUpload && (
        <form className="document-upload-form" onSubmit={handleUploadSubmit}>
          <div className="document-upload-grid">
            <div className="form-field">
              <label htmlFor="doc-category" className="form-label">
                Document category
              </label>
              <select
                id="doc-category"
                className="form-input"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                disabled={uploading}
              >
                {DOCUMENT_CATEGORIES.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            </div>

            {!fixedApplicationId && applications.length > 0 && (
              <div className="form-field">
                <label htmlFor="doc-application" className="form-label">
                  Link to application <span className="form-label-optional">(optional)</span>
                </label>
                <select
                  id="doc-application"
                  className="form-input"
                  value={selectedAppId}
                  onChange={(e) => setSelectedAppId(e.target.value)}
                  disabled={uploading}
                >
                  <option value="">General student profile document</option>
                  {applications.map((app) => (
                    <option key={app.id} value={String(app.id)}>
                      {app.referenceNumber} — {app.preferredSubject}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="form-field">
              <label htmlFor="doc-file" className="form-label">
                File (PDF, PNG, JPG, DOCX up to 10 MB)
              </label>
              <input
                key={fileInputKey}
                id="doc-file"
                type="file"
                accept=".pdf,.png,.jpg,.jpeg,.docx"
                className="form-input form-input-file"
                onChange={(e) => setSelectedFile(e.target.files && e.target.files[0] ? e.target.files[0] : null)}
                disabled={uploading}
              />
            </div>

            <div className="document-upload-submit">
              <button type="submit" className="btn btn-primary" disabled={uploading}>
                <UploadIcon />
                <span>{uploading ? 'Uploading' : 'Upload document'}</span>
              </button>
            </div>
          </div>

          {uploadError && (
            <div className="notice-banner notice-banner-error" style={{ marginTop: '12px', marginBottom: 0 }} role="alert">
              {uploadError}
            </div>
          )}
          {uploadSuccess && (
            <div className="notice-banner notice-banner-success" style={{ marginTop: '12px', marginBottom: 0 }} role="status">
              {uploadSuccess}
            </div>
          )}
        </form>
      )}

      {loading ? (
        <div className="table-state-card" role="status">
          <span className="status-dot status-dot-pending" aria-hidden="true" />
          <span className="mono-label">Loading documents</span>
        </div>
      ) : documents.length === 0 ? (
        <div className="table-state-card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-muted)' }}>
            <FolderIcon />
            <span className="table-empty-title">No documents uploaded yet</span>
          </div>
          <div className="table-empty-desc">
            {canUpload
              ? 'Upload your passport, academic transcripts, English scorecards, or financial documents above.'
              : 'No documents have been uploaded for this record yet.'}
          </div>
        </div>
      ) : (
        <div className="data-table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Category</th>
                <th>Filename</th>
                {!fixedApplicationId && <th>Application</th>}
                <th>Size</th>
                <th>Review status</th>
                <th>Uploaded</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {documents.map((doc) => (
                <tr key={doc.id}>
                  <td>
                    <span className="table-primary-text">{formatCategory(doc.category)}</span>
                  </td>
                  <td>
                    <div>{doc.originalFilename}</div>
                    {doc.reviewNote && (
                      <div className="doc-review-note-inline">
                        Note: {doc.reviewNote}
                      </div>
                    )}
                  </td>
                  {!fixedApplicationId && (
                    <td>
                      <span className="mono-val">
                        {doc.applicationReference || 'General'}
                      </span>
                    </td>
                  )}
                  <td>
                    <span className="mono-val">{formatFileSize(doc.fileSizeBytes)}</span>
                  </td>
                  <td>
                    <StatusBadge status={doc.reviewStatus} />
                  </td>
                  <td>
                    <span className="mono-val">{formatDateTime(doc.createdAt)}</span>
                  </td>
                  <td className="text-right">
                    <div className="table-action-group">
                      <a
                        href={doc.downloadUrl}
                        className="btn btn-secondary btn-compact"
                        download={doc.originalFilename}
                      >
                        <DownloadIcon />
                        <span>Download</span>
                      </a>
                      {canReview && (
                        <button
                          type="button"
                          className="btn btn-nav-outline btn-compact"
                          onClick={() => openReviewModal(doc)}
                        >
                          Review
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {reviewingDoc && (
        <div
          className="modal-backdrop"
          role="presentation"
          onClick={(e) => {
            if (e.target === e.currentTarget && !reviewBusy) {
              setReviewingDoc(null);
            }
          }}
        >
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="doc-review-title">
            <div className="modal-header">
              <div>
                <div className="mono-label">{formatCategory(reviewingDoc.category)}</div>
                <h2 id="doc-review-title" className="modal-title">
                  Review document
                </h2>
              </div>
            </div>
            <p className="modal-subtitle">{reviewingDoc.originalFilename}</p>

            {reviewError && (
              <div className="notice-banner notice-banner-error" role="alert">
                {reviewError}
              </div>
            )}

            <form className="auth-form" onSubmit={handleReviewSubmit}>
              <div className="form-field">
                <label htmlFor="doc-review-status" className="form-label">
                  Review decision
                </label>
                <select
                  id="doc-review-status"
                  className="form-input"
                  value={reviewStatus}
                  onChange={(e) => setReviewStatus(e.target.value)}
                  disabled={reviewBusy}
                >
                  <option value="PENDING">Pending</option>
                  <option value="UNDER_REVIEW">Under review</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                </select>
              </div>

              <div className="form-field">
                <label htmlFor="doc-review-note" className="form-label">
                  Review note {reviewStatus === 'REJECTED' ? '(required for rejection)' : '(optional)'}
                </label>
                <textarea
                  id="doc-review-note"
                  rows={3}
                  className="form-input form-textarea"
                  placeholder="Provide feedback or specify corrections needed"
                  value={reviewNote}
                  onChange={(e) => setReviewNote(e.target.value)}
                  disabled={reviewBusy}
                />
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setReviewingDoc(null)}
                  disabled={reviewBusy}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={reviewBusy}>
                  {reviewBusy ? 'Saving' : 'Save review'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
