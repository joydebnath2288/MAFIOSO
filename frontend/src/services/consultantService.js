import { requestJson } from './apiClient';

export function fetchConsultantDashboard() {
  return requestJson('/api/v1/consultant/dashboard', { method: 'GET' });
}

export function fetchAssignedStudents({ search = '', page = 1, pageSize = 10 } = {}) {
  const params = new URLSearchParams();
  if (search && search.trim()) {
    params.set('search', search.trim());
  }
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));
  return requestJson(`/api/v1/consultant/assigned-students?${params.toString()}`, { method: 'GET' });
}

export function fetchAssignedStudentDetail(studentId) {
  return requestJson(`/api/v1/consultant/assigned-students/${studentId}`, { method: 'GET' });
}

export function fetchConsultantApplications({ search = '', status = 'ALL', page = 1, pageSize = 10 } = {}) {
  const params = new URLSearchParams();
  if (search && search.trim()) {
    params.set('search', search.trim());
  }
  if (status && status !== 'ALL') {
    params.set('status', status);
  }
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));
  return requestJson(`/api/v1/consultant/applications?${params.toString()}`, { method: 'GET' });
}

export function fetchConsultantApplicationDetail(applicationId) {
  return requestJson(`/api/v1/consultant/applications/${applicationId}`, { method: 'GET' });
}

export function updateConsultantApplicationStatus(applicationId, { status, note = '' }) {
  return requestJson(`/api/v1/consultant/applications/${applicationId}/status`, {
    method: 'PUT',
    body: { status, note: note || null },
  });
}

export function reviewConsultantDocument(documentId, { reviewStatus, reviewNote = '' }) {
  return requestJson(`/api/v1/consultant/documents/${documentId}/review`, {
    method: 'PUT',
    body: { reviewStatus, reviewNote: reviewNote || null },
  });
}
