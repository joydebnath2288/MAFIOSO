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
