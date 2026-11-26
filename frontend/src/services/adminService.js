import { requestJson } from './apiClient';

function buildQueryString({ search = '', status = 'ALL', page = 1, pageSize = 10 }) {
  const params = new URLSearchParams();
  if (search && search.trim()) {
    params.set('search', search.trim());
  }
  if (status && status !== 'ALL') {
    params.set('status', status);
  }
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));
  return params.toString();
}

export function fetchAdminStudents(query = {}) {
  const qs = buildQueryString(query);
  return requestJson(`/api/v1/admin/students?${qs}`, { method: 'GET' });
}

export function fetchAdminStudentDetail(studentId) {
  return requestJson(`/api/v1/admin/students/${studentId}`, { method: 'GET' });
}

export function updateAdminStudentStatus(studentId, status) {
  return requestJson(`/api/v1/admin/students/${studentId}/status`, {
    method: 'PUT',
    body: { status },
  });
}

export function fetchAdminConsultants(query = {}) {
  const qs = buildQueryString(query);
  return requestJson(`/api/v1/admin/consultants?${qs}`, { method: 'GET' });
}

export function fetchAdminConsultantDetail(consultantId) {
  return requestJson(`/api/v1/admin/consultants/${consultantId}`, { method: 'GET' });
}

export function createAdminConsultant(payload) {
  return requestJson('/api/v1/admin/consultants', {
    method: 'POST',
    body: {
      fullName: payload.fullName,
      email: payload.email,
      phoneNumber: payload.phoneNumber || null,
      password: payload.password,
      confirmPassword: payload.confirmPassword,
    },
  });
}

export function updateAdminConsultantStatus(consultantId, status) {
  return requestJson(`/api/v1/admin/consultants/${consultantId}/status`, {
    method: 'PUT',
    body: { status },
  });
}

export function fetchAdminApplications({
  search = '',
  status = 'ALL',
  degreeLevel = 'ALL',
  consultantId = '',
  unassignedOnly = false,
  page = 1,
  pageSize = 10,
} = {}) {
  const params = new URLSearchParams();
  if (search && search.trim()) {
    params.set('search', search.trim());
  }
  if (status && status !== 'ALL') {
    params.set('status', status);
  }
  if (degreeLevel && degreeLevel !== 'ALL') {
    params.set('degreeLevel', degreeLevel);
  }
  if (consultantId) {
    params.set('consultantId', String(consultantId));
  }
  if (unassignedOnly) {
    params.set('unassignedOnly', 'true');
  }
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));
  return requestJson(`/api/v1/admin/applications?${params.toString()}`, { method: 'GET' });
}

export function fetchAdminApplicationDetail(applicationId) {
  return requestJson(`/api/v1/admin/applications/${applicationId}`, { method: 'GET' });
}

export function updateAdminApplicationStatus(applicationId, { status, note = '' }) {
  return requestJson(`/api/v1/admin/applications/${applicationId}/status`, {
    method: 'PUT',
    body: { status, note: note || null },
  });
}

export function assignAdminConsultant(applicationId, { consultantId, note = '' }) {
  return requestJson(`/api/v1/admin/applications/${applicationId}/assign-consultant`, {
    method: 'POST',
    body: { consultantId: Number(consultantId), note: note || null },
  });
}

export function removeAdminConsultantAssignment(applicationId, note = '') {
  const params = new URLSearchParams();
  if (note && note.trim()) {
    params.set('note', note.trim());
  }
  const qs = params.toString();
  return requestJson(
    `/api/v1/admin/applications/${applicationId}/assign-consultant${qs ? `?${qs}` : ''}`,
    { method: 'DELETE' }
  );
}

export function reviewAdminDocument(documentId, { reviewStatus, reviewNote = '' }) {
  return requestJson(`/api/v1/admin/documents/${documentId}/review`, {
    method: 'PUT',
    body: { reviewStatus, reviewNote: reviewNote || null },
  });
}
